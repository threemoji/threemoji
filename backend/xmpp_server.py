#/usr/bin/python
import sys, json, xmpp, os
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

      sent_message_id += 1

def send(json_dict):
  template = ("<message><gcm xmlns=\"google:mobile:data\">{0}</gcm></message>")
  message = xmpp.protocol.Message(node=template.format(json.dumps(json_dict, separators=(',', ':'))))
  client.send(message)

client = xmpp.Client(SERVER, debug=['socket'])
client.connect(server=(SERVER,PORT), secure=1, use_srv=False)
auth = client.auth(USERNAME, PASSWORD)
if not auth:
  print 'Authentication failed!'
  sys.exit(1)

client.RegisterHandler('message', message_callback)

while True:
  client.Process(1)
