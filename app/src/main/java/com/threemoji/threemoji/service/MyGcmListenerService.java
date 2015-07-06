package com.threemoji.threemoji.service;

import com.google.android.gms.gcm.GcmListenerService;

import com.threemoji.threemoji.MainActivity;
import com.threemoji.threemoji.MyLifecycleHandler;
import com.threemoji.threemoji.R;
import com.threemoji.threemoji.data.ChatContract;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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

            }
        } else {
            String message = data.getString("body");
            String fromUuid = data.getString("from_uid");
            String timestamp = data.getString("timestamp");

            Log.v(TAG, "From uuid: " + fromUuid);
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

    private void storeMessage(String uuid, String timestamp, String message) {
        Uri uri;
        ContentValues values = new ContentValues();
        values.put(ChatContract.MessageEntry.COLUMN_PARTNER_KEY, uuid);
        values.put(ChatContract.MessageEntry.COLUMN_DATETIME, timestamp);
        values.put(ChatContract.MessageEntry.COLUMN_MESSAGE_TYPE,
                   ChatContract.MessageEntry.MessageType.RECEIVED.name());
        values.put(ChatContract.MessageEntry.COLUMN_MESSAGE_DATA, message);
        uri = getContentResolver().insert(
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
