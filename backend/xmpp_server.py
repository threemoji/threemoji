#/usr/bin/python
import sys, json, xmpp, os, logging, time
import googledatastore as datastore
from datetime import datetime

SERVER = 'gcm.googleapis.com'
PORT = 5236 # change to 5235 for production
USERNAME = os.environ.get('PROJECT_NUM')
PASSWORD = os.environ.get('GCM_API_KEY')

# Unique message id for downstream messages
sent_message_id = 0

def message_callback(session, message):
  global sent_message_id
  gcm = message.getTags('gcm')

  if gcm:
    print "Received at " + str(datetime.now().time())
    gcm_json = gcm[0].getData()
    msg = json.loads(gcm_json)
    msg_id = msg["message_id"]
    device_reg_id = msg["from"]

    # Ignore non-standard messages (e.g. acks/nacks).
    if not msg.has_key('message_type'):
      # Acknowledge the incoming message immediately.
      send({"to": device_reg_id,
            "message_id": msg_id,
            "message_type": "ack"})
      # Handle action
      if msg.has_key('data'):
        data = msg["data"]
        if data.has_key('action'):
          if data["action"] == "upload_profile" or data["action"] == "update_profile":
            uid = data["uid"]
            password = data["password"]
            token = data["token"]
            emoji_1 = data["emoji_1"]
            emoji_2 = data["emoji_2"]
            emoji_3 = data["emoji_3"]
            generated_name = data["generated_name"]
            gender = data["gender"]
            location = data["location"]
            radius = data["radius"]
            if data["action"] == "upload_profile":
              add_user(uid, password, token, emoji_1, emoji_2, emoji_3, generated_name, gender, location, radius)
            else:
              update_user(uid, password, token, emoji_1, emoji_2, emoji_3, generated_name, gender, location, radius)

      sent_message_id += 1

def send(json_dict):
  template = ("<message><gcm xmlns=\"google:mobile:data\">{0}</gcm></message>")
  message = xmpp.protocol.Message(node=template.format(json.dumps(json_dict, separators=(',', ':'))))
  client.send(message)

def add_user(uid, password, token, emoji_1, emoji_2, emoji_3, generated_name, gender, location, radius):
  datastore.set_options(dataset=os.environ.get('PROJECT_ID'))
  try:
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
    location_property = user.property.add()
    location_property.name = 'location'
    location_property.value.string_value = location
    radius_property = user.property.add()
    radius_property.name = 'radius'
    radius_property.value.string_value = radius
    date_created_property = user.property.add()
    date_created_property.name = 'date_created'
    date_created_property.value.timestamp_microseconds_value = current_time
    date_modified_property = user.property.add()
    date_modified_property.name = 'date_modified'
    date_modified_property.value.timestamp_microseconds_value = current_time

    resp = datastore.commit(req)
    logging.info('Added user: ' + generated_name + ' (' + uid + ')')
    # auto_id_key = resp.mutation_result.insert_auto_id_key[0].path_element[0].id
    # return auto_id_key
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

def update_user(uid, password, token, emoji_1, emoji_2, emoji_3, generated_name, gender, location, radius):
  datastore.set_options(dataset=os.environ.get('PROJECT_ID'))
  data_dict = {"token": token,
               "emoji_1": emoji_1, "emoji_2": emoji_2, "emoji_3": emoji_3,
               "generated_name": generated_name, "gender": gender,
               "location": location, "radius": radius}
  try:
    req = datastore.LookupRequest()

    user_key = datastore.Key()
    path = user_key.path_element.add()
    path.name = uid
    path.kind = 'User'
    fail_password = False

    req.key.extend([user_key])
    resp = datastore.lookup(req)
    user = resp.found[0].entity
    for prop in user.property:
      print "processing property " + prop.name
      if prop.name == 'password':
        if password != prop.value.string_value:
          print "expected " + prop.value.string_value
          print "got " + password
          fail_password = True
          break
        else:
          continue
      elif prop.name == 'date_created':
        continue
      elif prop.name == 'date_modified':
        prop.value.timestamp_microseconds_value = long(time.time() * 1e6)
      elif data_dict.has_key(prop.name):
        prop.value.string_value = data_dict[prop.name]
      else:
        continue

    if fail_password:
      logging.error('Access denied for user: ' + generated_name + ' (' + uid + ') action: update_profile')
    else:
      req = datastore.CommitRequest()
      req.mode = datastore.CommitRequest.NON_TRANSACTIONAL
      req.mutation.update.extend([user])
      datastore.commit(req)
      logging.info('Updated user: ' + generated_name + ' (' + uid + ')')
    # auto_id_key = resp.mutation_result.insert_auto_id_key[0].path_element[0].id
    # return auto_id_key
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

logging.basicConfig(level=logging.INFO)
client = xmpp.Client(SERVER, debug=['socket'])
client.connect(server=(SERVER,PORT), secure=1, use_srv=False)
auth = client.auth(USERNAME, PASSWORD)
if not auth:
  print 'Authentication failed!'
  sys.exit(1)

client.RegisterHandler('message', message_callback)

while True:
  client.Process(1)
