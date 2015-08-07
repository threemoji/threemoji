#/usr/bin/python
import sys, json, xmpp, os, logging, time, random
import googledatastore as datastore
import psycopg2
from collections import OrderedDict

SERVER = 'gcm.googleapis.com'
PORT = 5236 # change to 5235 for production
USERNAME = os.environ.get('PROJECT_NUM')
PASSWORD = os.environ.get('GCM_API_KEY')

datastore.set_options(dataset=os.environ.get('PROJECT_ID'))

PG_SERVER = os.environ.get('POSTGIS_SERVER')
PG_USER = os.environ.get('POSTGIS_USER')
PG_KEY = os.environ.get('POSTGIS_KEY')
PG_DB = os.environ.get('POSTGIS_DB')
PG_TABLE = os.environ.get('POSTGIS_TABLE')
pg_conn = psycopg2.connect(host=PG_SERVER, user=PG_USER, password=PG_KEY, dbname=PG_DB)
pg_curs = pg_conn.cursor()

logging.basicConfig(format='%(asctime)-15s %(levelname)s: %(message)s', level=logging.INFO)

class LimitedSizeDict(OrderedDict):
  def __init__(self, *args, **kwds):
    self.size_limit = kwds.pop("size_limit", None)
    OrderedDict.__init__(self, *args, **kwds)
    self._check_size_limit()
  def __setitem__(self, key, value):
    OrderedDict.__setitem__(self, key, value)
    self._check_size_limit()
  def _check_size_limit(self):
    if self.size_limit is not None:
      while len(self) > self.size_limit:
        self.popitem(last=False)

message_id_cache = LimitedSizeDict(size_limit=1000)

def send(json_dict):
  template = ("<message><gcm xmlns=\"google:mobile:data\">{0}</gcm></message>")
  message = xmpp.protocol.Message(node=template.format(json.dumps(json_dict, separators=(',', ':'))))
  client.send(message)

def message_callback(session, message):
  gcm = message.getTags('gcm')

  if gcm:
    gcm_json = gcm[0].getData()
    msg = json.loads(gcm_json)
    msg_id = msg.get("message_id")
    device_reg_id = msg.get("from")

    if msg.has_key('message_type'):
      msg_type = msg["message_type"]
      if msg_type == "ack":
        logging.info('Received GCM ack: ' + msg_id)
        message_id_cache[msg_id] = 1
      elif msg_type == "nack" and msg.get("error") == "DEVICE_UNREGISTERED":
        logging.info('Received DEVICE_UNREGISTERED nack')
        del_user(None, msg.get("from"))
      elif msg_type == "control" and msg.get("control_type") == "CONNECTION_DRAINING":
        logging.info('Received CONNECTION_DRAINING control message')

    else:
      logging.info('Received GCM message: ' + msg_id)
      # Acknowledge the incoming message immediately.
      send({"to": device_reg_id,
            "message_id": msg_id,
            "message_type": "ack"})
      logging.info('Acknowledged GCM message: ' + msg_id)

      if (msg.has_key('data') and msg_id not in message_id_cache):
        # Handle action
        data = msg["data"]
        if data.has_key('action'):
          logging.info('Processing action: ' + data["action"])
          try:
            uid = data["uid"]
            password = data["password"]

            if data["action"] == "upload_profile":
              add_user(uid, password, data["token"], data["emoji_1"], data["emoji_2"], data["emoji_3"],
                       data["generated_name"], data["gender"], data.get("gender_pref", "All"), data["radius"])

            else:
              user = auth_user(uid, password, data["action"])

              if user == 404:
                if data["action"] == "update_profile":
                  add_user(uid, password, data["token"], data["emoji_1"], data["emoji_2"], data["emoji_3"],
                           data["generated_name"], data["gender"], data.get("gender_pref", "All"), data["radius"])
                else:
                  logging.error('User not found! Sending reupload profile request')
                  send_reupload_request(msg["from"], msg_id)

              elif user != 403:
                if data["action"] == "send_message":
                  send_message(uid, msg_id, data["to"], data["message"], get_timestamp())

                elif data["action"] == "send_match_notification":
                  send_match_notification(uid, msg_id, user, data["to"])

                elif data["action"] == "lookup_nearby":
                  if data.has_key("latitude") and data.has_key("longitude"):
                    lookup_nearby(uid, msg_id, user, data["latitude"], data["longitude"], data["radius"])
                  else:
                    old_lookup_nearby(uid, msg_id, user)

                elif data["action"] == "lookup_profile":
                  lookup_profile(uid, msg_id, user, data["profile"])

                elif data["action"] == "update_location":
                  update_location(uid, user, data["latitude"], data["longitude"])

                elif data["action"] == "update_profile":
                  data_dict = {}
                  for param in data:
                    if param in ['token', 'emoji_1', 'emoji_2', 'emoji_3', 'generated_name', 'gender', 'gender_pref', 'radius']:
                      data_dict[param] = data[param]
                  update_user(uid, user, data_dict, data["action"])

                elif data["action"] == "update_token":
                  update_user(uid, user, {"token": data["token"]}, data["action"])

                elif data["action"] == "delete_profile":
                  del_user(uid)

          except KeyError as e:
            logging.exception('Invalid request: missing parameters for action: ' + data["action"])

          except datastore.RPCError as e:
            # RPCError is raised if any error happened during a RPC.
            # It includes the `method` called and the `reason` of the
            # failure as well as the original `HTTPResponse` object.
            logging.error('Error while doing datastore operation')
            logging.error('RPCError: %(method)s %(reason)s',
                          {'method': e.method,
                           'reason': e.reason})
            logging.error('HTTPError: %(status)s %(reason)s',
                          {'status': e.response.status,
                           'reason': e.response.reason})

          except psycopg2.Error as e:
            pg_conn.rollback()
            logging.exception('Error doing PostGIS database operation for action: ' + data["action"])

def add_user(uid, password, token, emoji_1, emoji_2, emoji_3, generated_name, gender, gender_pref, radius):
  req = datastore.CommitRequest()
  req.mode = datastore.CommitRequest.NON_TRANSACTIONAL

  user = req.mutation.insert.add()
  path_element = user.key.path_element.add()
  path_element.name = uid
  path_element.kind = 'User'
  current_time = long(time.time() * 1e6)

  password_property = user.property.add()
  password_property.name = 'password'
  password_property.value.string_value = password
  token_property = user.property.add()
  token_property.name = 'token'
  token_property.value.string_value = token

  emoji_1_property = user.property.add()
  emoji_1_property.name = 'emoji_1'
  emoji_1_property.value.string_value = emoji_1
  emoji_2_property = user.property.add()
  emoji_2_property.name = 'emoji_2'
  emoji_2_property.value.string_value = emoji_2
  emoji_3_property = user.property.add()
  emoji_3_property.name = 'emoji_3'
  emoji_3_property.value.string_value = emoji_3
  generated_name_property = user.property.add()
  generated_name_property.name = 'generated_name'
  generated_name_property.value.string_value = generated_name
  gender_property = user.property.add()
  gender_property.name = 'gender'
  gender_property.value.string_value = gender
  gender_pref_property = user.property.add()
  gender_pref_property.name = 'gender_pref'
  gender_pref_property.value.string_value = gender_pref

  date_created_property = user.property.add()
  date_created_property.name = 'date_created'
  date_created_property.value.timestamp_microseconds_value = current_time
  date_modified_property = user.property.add()
  date_modified_property.name = 'date_modified'
  date_modified_property.value.timestamp_microseconds_value = current_time
  datastore.commit(req)

  pg_curs.execute('INSERT INTO ' + PG_TABLE + ' VALUES(%s, NULL, %s);', (uid, int(radius)))
  pg_conn.commit()

  logging.info('Added user: ' + uid + ' (' + generated_name + ')')

def del_user(uid, token=None):
  if token is not None:
    req = datastore.RunQueryRequest()
    query = req.query
    query.kind.add().name = 'User'
    query.projection.add().property.name = '__key__'

    token_filter = query.filter.property_filter
    token_filter.property.name = 'token'
    token_filter.operator = datastore.PropertyFilter.EQUAL
    token_filter.value.string_value = token

    results = datastore.run_query(req).batch.entity_result
    if len(results) == 0:
      logging.info("Unregistered user token not in database")
      return
    uid = results[0].entity.key.path_element[0].name
    logging.info("Deleting unregistered user: " + uid)

  user_key = datastore.Key()
  path = user_key.path_element.add()
  path.kind = 'User'
  path.name = uid

  req = datastore.CommitRequest()
  req.mode = datastore.CommitRequest.NON_TRANSACTIONAL
  req.mutation.delete.extend([user_key])
  datastore.commit(req)

  pg_curs.execute('DELETE FROM ' + PG_TABLE + ' WHERE uid = %s;', (uid,))
  pg_conn.commit()

  logging.info('Deleted user: ' + uid)

def auth_user(uid, password, action):
  user_key = datastore.Key()
  path = user_key.path_element.add()
  path.name = uid
  path.kind = 'User'

  req = datastore.LookupRequest()
  req.key.extend([user_key])
  resp = datastore.lookup(req)

  if len(resp.missing) is 1:
    logging.error('Entity not found for user: ' + uid + ' action: ' + action)
    return 404
  user = resp.found[0].entity

  if action != "lookup":
    for prop in user.property:
      if prop.name == 'password':
        if password != prop.value.string_value:
          logging.error('Access denied for user: ' + uid + ' action: ' + action)
          return 403
    logging.info('Authenticated user: ' + uid + ' action: ' + action)

  return user

def lookup_profile(uid, message_id, user, target_uid):
  target_user = auth_user(target_uid, "", "lookup")
  user_dict = {}

  if target_user != 404:
    logging.info("Target user exists")
    data_dict = {}
    for prop in target_user.property:
      if prop.name in ['emoji_1', 'emoji_2', 'emoji_3', 'generated_name', 'gender']:
        data_dict[prop.name] = prop.value.string_value
    user_dict[target_uid] = data_dict
  else:
    logging.info("Target user does not exist")
    user_dict[target_uid] = "404"

  send({"to": get_token(user),
        "message_id": message_id,
        "data": {
          "response_type": "lookup_profile",
          "body": user_dict,
          "timestamp": get_timestamp()
        }})

  logging.info('Profile lookup response sent to user: ' + uid + ' message_id: ' + message_id)

def lookup_nearby(uid, message_id, user, lat, lon, radius):
  pg_curs.execute('SELECT uid, ST_Distance(location, ST_SetSRID(ST_MakePoint(%s, %s),4326)::GEOGRAPHY)' +
                  ' FROM ' + PG_TABLE +
                  ' WHERE ST_DWithin(ST_SetSRID(ST_MakePoint(%s, %s),4326)::GEOGRAPHY, ' \
                                    'location, LEAST(radius * 1000, %s)) AND uid != %s;',
                  (lon, lat, lon, lat, int(radius)*1e3, uid))
  results = pg_curs.fetchall()
  results = random.sample(results, min(len(results), 10))

  req = datastore.LookupRequest()
  dist_dict = {}
  for result in results:
    result_uid = result[0]
    result_dist = result[1]
    dist_dict[result_uid] = result_dist
    user_key = datastore.Key()
    path = user_key.path_element.add()
    path.name = result_uid
    path.kind = 'User'
    req.key.extend([user_key])

  resp = datastore.lookup(req)
  user_dict = {}

  for entity_result in resp.found:
    found_user = entity_result.entity
    found_user_uid = found_user.key.path_element[0].name
    data_dict = {}
    for prop in found_user.property:
      if prop.name in ['emoji_1', 'emoji_2', 'emoji_3', 'generated_name', 'gender', 'gender_pref']:
        data_dict[prop.name] = prop.value.string_value
    data_dict['distance'] = dist_dict[found_user_uid]
    user_dict[found_user.key.path_element[0].name] = data_dict

  send({"to": get_token(user),
        "message_id": message_id,
        "data": {
          "response_type": "lookup_nearby",
          "body": user_dict,
          "timestamp": get_timestamp()
        }})

  logging.info('Nearby lookup response sent to user: ' + uid + ' message_id: ' + message_id)

def update_user(uid, user, data_dict, action):
  for prop in user.property:
    if data_dict.has_key(prop.name):
      prop.value.string_value = data_dict[prop.name]
    elif prop.name == 'date_modified':
      prop.value.timestamp_microseconds_value = long(time.time() * 1e6)

  req = datastore.CommitRequest()
  req.mode = datastore.CommitRequest.NON_TRANSACTIONAL
  req.mutation.update.extend([user])
  datastore.commit(req)

  if data_dict.has_key('radius'):
    pg_curs.execute('UPDATE ' + PG_TABLE +
                    ' SET radius = %s WHERE uid = %s;',
                    (int(data_dict['radius']), uid))
    pg_conn.commit()

  logging.info('Updated user: ' + uid + ' action: ' + action)

def update_location(uid, user, lat, lon):
  pg_curs.execute('UPDATE ' + PG_TABLE +
                  ' SET location = ST_SetSRID(ST_MakePoint(%s, %s),4326)::GEOGRAPHY WHERE uid = %s;',
                  (lon, lat, uid))
  pg_conn.commit()

  update_user(uid, user, {}, "update_location")

def send_message(from_uid, message_id, to_uid, message, timestamp):
  user = auth_user(to_uid, "", "lookup")

  if user == 404:
    logging.error('Failed to send message to user: ' + to_uid)
    return 404

  send({"to": get_token(user),
        "message_id": message_id,
        "data": {
          "from_uid": from_uid,
          "body": message,
          "timestamp": timestamp
        }})

  logging.info('Message sent to user: ' + to_uid + ' message_id: ' + message_id)

def send_match_notification(from_uid, message_id, from_user, to_uid):
  to_user = auth_user(to_uid, "", "lookup")

  if to_user == 404:
    logging.error('Failed to send match notification to user: ' + to_uid)
    return 404

  user_dict = {}
  data_dict = {}
  for prop in from_user.property:
    if prop.name in ['emoji_1', 'emoji_2', 'emoji_3', 'generated_name', 'gender']:
      data_dict[prop.name] = prop.value.string_value
  user_dict[from_uid] = data_dict

  send({"to": get_token(to_user),
        "message_id": message_id,
        "data": {
          "response_type": "match_notification",
          "body": user_dict,
          "timestamp": get_timestamp()
        }})

  logging.info('Match notification sent to user: ' + to_uid + ' message_id: ' + message_id)

def send_reupload_request(token, message_id):
  send({"to": token,
        "message_id": message_id,
        "data": {
          "response_type": "reupload_request"
        }})

  logging.info('Profile reupload request sent to: ' + token + ' message_id: ' + message_id)

def get_token(user):
  for prop in user.property:
    if prop.name == 'token':
      return prop.value.string_value

def get_timestamp():
  return str(long(time.time() * 1e3))

client = xmpp.Client(SERVER, debug=['socket'])
client.connect(server=(SERVER,PORT), secure=1, use_srv=False)
auth = client.auth(USERNAME, PASSWORD)
if not auth:
  logging.error('Authentication failed! Exiting...')
  sys.exit(1)
client.RegisterHandler('message', message_callback)

while True:
  if client.isConnected():
    client.Process(1)
  else:
    logging.error('Disconnected! Reconnecting...')
    client = xmpp.Client(SERVER, debug=['socket'])
    client.connect(server=(SERVER,PORT), secure=1, use_srv=False)
    auth = client.auth(USERNAME, PASSWORD)
    if not auth:
      logging.error('Authentication failed! Exiting...')
      sys.exit(1)
    client.RegisterHandler('message', message_callback)
