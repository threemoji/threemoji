package com.threemoji.threemoji.service;

import com.google.android.gms.gcm.GcmListenerService;

import com.threemoji.threemoji.MainActivity;
import com.threemoji.threemoji.MyLifecycleHandler;
import com.threemoji.threemoji.R;
import com.threemoji.threemoji.data.ChatContract;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Iterator;
import java.util.Random;

public class MyGcmListenerService extends GcmListenerService {
    private static final String TAG = MyGcmListenerService.class.getSimpleName();

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, data.toString());
        Log.d(TAG, "From: " + from);
        String messageType = data.getString("message_type");
        String responseType = data.getString("response_type");
        if (messageType != null && messageType.equals("ack")) {
            Log.d(TAG, "ACK");
        } else if (responseType != null) {
            if (responseType.equals("lookup_profile")) {
                Log.d(TAG, "Profile lookup response: " + data.getString("body"));
            } else if (responseType.equals("lookup_nearby")) {
                Log.d(TAG, "Nearby lookup response: " + data.getString("body"));
                storePeopleNearbyData(data.getString("body"));
            }
        } else {
            String message = data.getString("body");
            String fromUuid = data.getString("from_uid");
            String timestamp = data.getString("timestamp");

            Log.v(TAG, "From uuid: " + fromUuid);
//            addPartnerIfNeeded(fromUuid);

            storeMessage(fromUuid, timestamp, message);
            String fromName = findNameFromUuid(fromUuid);
            Log.v(TAG, "From name: " + fromName);
            if (!MyLifecycleHandler.isApplicationVisible()) {
                sendNotification(fromName, message);
            }
            Log.d(TAG, "Message: " + message);
        }

        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

    }
    // [END receive_message]

    private void addPartnerIfNeeded(String fromUuid) {
        if (findNameFromUuid(fromUuid).equals("")) {
            Intent intent = new Intent(this, ChatIntentService.class);
            intent.putExtra("action", ChatIntentService.Action.LOOKUP_UUID.name());
            intent.putExtra("uuid", fromUuid);
            this.startService(intent);

            //
        }
    }

    private String findNameFromUuid(String fromUuid) {
        Cursor cursor =
                getContentResolver().query(ChatContract.PartnerEntry.CONTENT_URI,
                                           new String[]{
                                                   ChatContract.PartnerEntry.COLUMN_GENERATED_NAME},
                                           ChatContract.PartnerEntry.COLUMN_UUID + " = ?",
                                           new String[]{fromUuid},
                                           null);

        try {
            cursor.moveToNext();
            String uuid = cursor.getString(0);
            cursor.close();
            return uuid;
        } catch (NullPointerException | CursorIndexOutOfBoundsException e) {
            Log.e(TAG, e.getMessage());
            return "";
        }
    }

    private void storePeopleNearbyData(String body) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userUuid = prefs.getString(getString(R.string.profile_uid_key), "");
        try {
            JSONObject json = new JSONObject(body);
//            Log.v(TAG, json.toString());

            Iterator<String> people = json.keys();
            while (people.hasNext()) {
                String uuid = people.next();

                JSONObject jsonPersonData = json.getJSONObject(uuid);

                String emoji1 = (String) jsonPersonData.get("emoji_1");
                String emoji2 = (String) jsonPersonData.get("emoji_2");
                String emoji3 = (String) jsonPersonData.get("emoji_3");
                String gender = (String) jsonPersonData.get("gender");
                String generatedName = (String) jsonPersonData.get("generated_name");

                if (uuid.equals(userUuid)) {
                    continue;
                }
                ContentValues values = new ContentValues();
                values.put(ChatContract.PeopleNearbyEntry.COLUMN_UUID, uuid);
                values.put(ChatContract.PeopleNearbyEntry.COLUMN_EMOJI_1, emoji1);
                values.put(ChatContract.PeopleNearbyEntry.COLUMN_EMOJI_2, emoji2);
                values.put(ChatContract.PeopleNearbyEntry.COLUMN_EMOJI_3, emoji3);
                values.put(ChatContract.PeopleNearbyEntry.COLUMN_GENDER, gender);
                values.put(ChatContract.PeopleNearbyEntry.COLUMN_GENERATED_NAME, generatedName);
                values.put(ChatContract.PeopleNearbyEntry.COLUMN_DISTANCE, "10");

                if (personAlreadyInClientDatabase(uuid, generatedName)) {
                    int rowsUpdated = getContentResolver().update(
                            ChatContract.PeopleNearbyEntry.CONTENT_URI, values,
                            ChatContract.PeopleNearbyEntry.COLUMN_UUID + " = ?",
                            new String[]{uuid});
                    Log.v(TAG, "Rows updated = " + rowsUpdated);
                } else {
                    Uri uri = getContentResolver().insert(
                            ChatContract.PeopleNearbyEntry.CONTENT_URI,
                            values);
                    Log.v(TAG, uri.toString());
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private boolean personAlreadyInClientDatabase(String uuid, String generatedName) {
        Cursor cursor =
                getContentResolver().query(ChatContract.PeopleNearbyEntry.CONTENT_URI,
                                           new String[]{
                                                   ChatContract.PeopleNearbyEntry.COLUMN_GENERATED_NAME},
                                           ChatContract.PeopleNearbyEntry.COLUMN_UUID + " = ?",
                                           new String[]{uuid},
                                           null);

        return cursor.getCount() > 0;
    }

    private void storeMessage(String uuid, String timestamp, String message) {
        ContentValues values = new ContentValues();
        values.put(ChatContract.MessageEntry.COLUMN_PARTNER_KEY, uuid);
        values.put(ChatContract.MessageEntry.COLUMN_DATETIME, timestamp);
        values.put(ChatContract.MessageEntry.COLUMN_MESSAGE_TYPE,
                   ChatContract.MessageEntry.MessageType.RECEIVED.name());
        values.put(ChatContract.MessageEntry.COLUMN_MESSAGE_DATA, message);
        Uri uri = getContentResolver().insert(
                ChatContract.MessageEntry.buildMessagesWithPartnerUri(uuid), values);
    }

    @Override
    public void onDeletedMessages() {
//        sendNotification("Deleted messages on server");
    }

    @Override
    public void onMessageSent(String msgId) {
        //sendNotification("Upstream message sent. Id=" + msgId);
    }

    @Override
    public void onSendError(String msgId, String error) {
//        sendNotification("Upstream message send error. Id=" + msgId + ", error" + error);
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String from, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                                                                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(from)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Random rand = new Random();
        notificationManager.notify(rand.nextInt() /* ID of notification */,
                                   notificationBuilder.build());
    }
}
