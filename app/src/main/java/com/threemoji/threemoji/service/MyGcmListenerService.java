package com.threemoji.threemoji.service;

import com.google.android.gms.gcm.GcmListenerService;

import com.threemoji.threemoji.ChatActivity;
import com.threemoji.threemoji.R;
import com.threemoji.threemoji.data.ChatContract;
import com.threemoji.threemoji.utility.EmojiVector;

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
import android.database.sqlite.SQLiteException;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MyGcmListenerService extends GcmListenerService {
    public static final String TAG = MyGcmListenerService.class.getSimpleName();
    public static final String[] PARTNER_PROJECTION_ALL = new String[]{
            ChatContract.PartnerEntry.COLUMN_EMOJI_1,
            ChatContract.PartnerEntry.COLUMN_EMOJI_2,
            ChatContract.PartnerEntry.COLUMN_EMOJI_3,
            ChatContract.PartnerEntry.COLUMN_GENDER,
            ChatContract.PartnerEntry.COLUMN_GENERATED_NAME};
    public static final String[] PARTNER_PROJECTION_GENERATED_NAME = new String[]{
            ChatContract.PartnerEntry.COLUMN_GENERATED_NAME};
    public static final String[] PARTNER_PROJECTION_NUM_NEW_MESSAGES = new String[]{
            ChatContract.PartnerEntry.COLUMN_NUM_NEW_MESSAGES};
    public static final String[] PARTNER_PROJECTION_IS_MUTED = new String[]{
            ChatContract.PartnerEntry.COLUMN_IS_MUTED};
    public static final int MINIMUM_MATCH_VALUE = 10;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String messageType = data.getString("message_type");
        String responseType = data.getString("response_type");

        if (responseType != null) {
            if (responseType.equals(getString(R.string.backend_response_lookup_profile_key))) {
                Log.d(TAG, "Profile lookup response: " + data.getString("body"));
                addPartnerToDb(data.getString("body"));

            } else if (responseType.equals(getString(R.string.backend_response_lookup_nearby_key))) {
                Log.d(TAG, "Nearby lookup response: " + data.getString("body"));
                updateLookupNearbyTimestamp();
                storePeopleNearbyData(data.getString("body"));

            } else if (responseType.equals(getString(R.string.backend_response_match_notification_key))) {
                Log.d(TAG, "Match notification received: " + data.getString("body"));
                matchWithPerson(data.getString("body"));
            }
        } else {
            String message = data.getString("body");
            String fromUid = data.getString("from_uid");
            String timestamp = data.getString("timestamp");

            String fromName = findNameFromUid(fromUid);

            Log.d(TAG, "From uid: " + fromUid);
            Log.d(TAG, "From name: " + fromName);
            Log.d(TAG, "Message: " + message);

            if (fromName.isEmpty()) {
                Intent intent = ChatIntentService.createIntent(this, ChatIntentService.Action.LOOKUP_UID, fromUid);
                this.startService(intent);
            }

            storeMessage(fromUid, timestamp, message);

            if (!isChatVisible(fromUid)) {
                updateNumNewMessages(fromUid);
                if (isNotificationsEnabled() && !isChatMuted(fromUid)) {
                    sendNotification(fromUid, fromName, message);
                }
            }
        }
    }

    private boolean isNotificationsEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                                .getBoolean(getString(R.string.pref_chat_notifications_key), Boolean.parseBoolean(getString(R.string.pref_chat_notifications_default)));
    }

    private boolean isChatMuted(String fromUid) {
        Cursor cursor = getPartnerCursor(fromUid, PARTNER_PROJECTION_IS_MUTED);
        try {
            cursor.moveToNext();
            boolean isMuted = cursor.getInt(0) > 0;
            cursor.close();
            return isMuted;
        } catch (NullPointerException | CursorIndexOutOfBoundsException e) {
            return false;
        }
    }

    private boolean isChatVisible(String fromUid) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> uidsOfOpenedChats = prefs.getStringSet(getString(R.string.uids_of_opened_chats),
                                                           new HashSet<String>());

        return !uidsOfOpenedChats.isEmpty() && uidsOfOpenedChats.contains(fromUid);
    }

    @Override
    public void onDeletedMessages() {
//        sendNotification("Deleted messages on server");
    }

    @Override
    public void onMessageSent(String msgId) {
//        sendNotification("Upstream message sent. Id=" + msgId);
    }

    @Override
    public void onSendError(String msgId, String error) {
//        sendNotification("Upstream message send error. Id=" + msgId + ", error" + error);
    }

    private void addPartnerToDb(String body) {
        try {
            JSONObject json = new JSONObject(body);

            Iterator<String> people = json.keys();
            while (people.hasNext()) {
                String uid = people.next();
                try {
                    JSONObject jsonPersonData = json.getJSONObject(uid);
                    String emoji1 = jsonPersonData.getString("emoji_1");
                    String emoji2 = jsonPersonData.getString("emoji_2");
                    String emoji3 = jsonPersonData.getString("emoji_3");
                    String gender = jsonPersonData.getString("gender");
                    String generatedName = jsonPersonData.getString("generated_name");

                    ContentValues values = new ContentValues();
                    values.put(ChatContract.PartnerEntry.COLUMN_UID, uid);
                    values.put(ChatContract.PartnerEntry.COLUMN_EMOJI_1, emoji1);
                    values.put(ChatContract.PartnerEntry.COLUMN_EMOJI_2, emoji2);
                    values.put(ChatContract.PartnerEntry.COLUMN_EMOJI_3, emoji3);
                    values.put(ChatContract.PartnerEntry.COLUMN_GENDER, gender);
                    values.put(ChatContract.PartnerEntry.COLUMN_GENERATED_NAME, generatedName);

                    Cursor cursor = getPartnerCursor(uid, PARTNER_PROJECTION_ALL);
                    if (cursor.getCount() > 0) { // if partner exists
                        cursor.moveToFirst();
                        if (!cursor.getString(0).equals(emoji1) || // if partner has updated profile
                            !cursor.getString(1).equals(emoji2) ||
                            !cursor.getString(2).equals(emoji3) ||
                            !cursor.getString(3).equals(gender) ||
                            !cursor.getString(4).equals(generatedName)) {

                            int rowsUpdated = updatePartnerEntry(uid, values);
                            Log.d(TAG, "Rows updated = " + rowsUpdated + ", " + generatedName);

                            addChangedProfileAlert(uid, cursor.getString(4), generatedName);
                        }
                    } else { // new partner
                        Uri uri = getContentResolver().insert(
                                ChatContract.PartnerEntry.CONTENT_URI,
                                values);
                        Log.d(TAG, "Added partner: " + uri.toString());

                        updateLastActivity(uid, String.valueOf(System.currentTimeMillis()));
                    }
                } catch (JSONException e) { // no such person exists on the server
                    if (json.getString(uid).equals("404")) {
                        Log.d(TAG, uid + " does not exist");

                        ContentValues values = new ContentValues();
                        values.put(ChatContract.PartnerEntry.COLUMN_IS_ALIVE, 0);
                        int rowsUpdated = updatePartnerEntry(uid, values);
                        Log.d(TAG, rowsUpdated + " rows have been updated");

                        addDeletedProfileAlert(uid);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private Cursor getPartnerCursor(String uid, String[] projection) {
        return getContentResolver().query(ChatContract.PartnerEntry.buildPartnerByUidUri(uid),
                projection,
                null,
                null,
                null);

    }

    private int updatePartnerEntry(String uid, ContentValues values) {
        return getContentResolver().update(
                ChatContract.PartnerEntry.CONTENT_URI, values,
                ChatContract.PartnerEntry.COLUMN_UID + " = ?",
                new String[]{uid});
    }

    private void addChangedProfileAlert(String partnerUid, String oldName, String newName) {
        addAlertMessage(partnerUid, oldName + " is now " + newName);
    }

    private void addDeletedProfileAlert(String partnerUid) {
        addAlertMessage(partnerUid, "Partner has left the chat. This chat will be archived.");
    }

    private void addMatchAlert(String partnerUid, String partnerGeneratedName) {
        addAlertMessage(partnerUid, "Say hello to your new match " + partnerGeneratedName + "!");
    }

    private void addAlertMessage(String partnerUid, String message) {
        ContentValues values = new ContentValues();
        values.put(ChatContract.MessageEntry.COLUMN_PARTNER_KEY, partnerUid);
        values.put(ChatContract.MessageEntry.COLUMN_DATETIME, System.currentTimeMillis());
        values.put(ChatContract.MessageEntry.COLUMN_MESSAGE_TYPE,
                   ChatContract.MessageEntry.MessageType.ALERT.name());
        values.put(ChatContract.MessageEntry.COLUMN_MESSAGE_DATA, message);

        Uri uri = getContentResolver().insert(
                ChatContract.MessageEntry.buildMessagesByUidUri(partnerUid), values);
        Log.d(TAG, "Added alert message: " + message + ", " + uri.toString());
    }

    private void updateLookupNearbyTimestamp() {
        long time = System.currentTimeMillis();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putLong(getString(R.string.prefs_lookup_nearby_time), time);
        editor.apply();
    }

    private void storePeopleNearbyData(String body) {
        try {
            JSONObject json = new JSONObject(body);
            Iterator<String> people = json.keys();

            // clear all existing data
            getContentResolver().delete(ChatContract.PeopleNearbyEntry.CONTENT_URI, null, null);

            EmojiVector emojiVector = getEmojiVector();
            int bestMatchValue = -1;
            String bestMatchUid = null, bestMatchGeneratedName = null;
            JSONObject bestMatchJson = null;

            while (people.hasNext()) {
                String uid = people.next();

                JSONObject jsonPersonData = json.getJSONObject(uid);

                String emoji1 = jsonPersonData.getString("emoji_1");
                String emoji2 = jsonPersonData.getString("emoji_2");
                String emoji3 = jsonPersonData.getString("emoji_3");
                String gender = jsonPersonData.getString("gender");
                String generatedName = jsonPersonData.getString("generated_name");
                Double distance = jsonPersonData.getDouble("distance")/1000;

                Cursor cursor = getPartnerCursor(uid, PARTNER_PROJECTION_GENERATED_NAME);
                if (cursor.getCount() == 0) { // if not already a chat partner
                    EmojiVector vector = new EmojiVector(emoji1, emoji2, emoji3);
                    int matchValue = vector.getMatchValue(emojiVector);
                    Log.d(TAG, "Match value for " + generatedName + ": " + String.valueOf(matchValue));
                    if (matchValue > bestMatchValue) {
                        bestMatchValue = matchValue;
                        bestMatchUid = uid;
                        bestMatchJson = new JSONObject("{}");
                        bestMatchJson.put(uid, jsonPersonData);
                    }
                }
                cursor.close();

                ContentValues values = new ContentValues();
                values.put(ChatContract.PeopleNearbyEntry.COLUMN_UID, uid);
                values.put(ChatContract.PeopleNearbyEntry.COLUMN_EMOJI_1, emoji1);
                values.put(ChatContract.PeopleNearbyEntry.COLUMN_EMOJI_2, emoji2);
                values.put(ChatContract.PeopleNearbyEntry.COLUMN_EMOJI_3, emoji3);
                values.put(ChatContract.PeopleNearbyEntry.COLUMN_GENDER, gender);
                values.put(ChatContract.PeopleNearbyEntry.COLUMN_GENERATED_NAME, generatedName);
                values.put(ChatContract.PeopleNearbyEntry.COLUMN_DISTANCE, distance);
                try {
                    Uri uri = getContentResolver().insert(
                            ChatContract.PeopleNearbyEntry.CONTENT_URI,
                            values);
                    Log.d(TAG, "Added person nearby: " + uri.toString());
                } catch (SQLiteException e) {
                    Log.e(TAG, "Failed to add new row to people nearby table");
                }
            }
            if (bestMatchValue > MINIMUM_MATCH_VALUE) {
                matchWithPerson(bestMatchJson.toString());
                Intent intent = ChatIntentService.createIntent(this, ChatIntentService.Action.SEND_MATCH_NOTIFICATION, bestMatchUid);
                this.startService(intent);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void matchWithPerson(String body) {
        try {
            JSONObject json = new JSONObject(body);
            String uid = json.keys().next();
            JSONObject jsonPersonData = json.getJSONObject(uid);
            String generatedName = jsonPersonData.getString("generated_name");
            addPartnerToDb(body);
            addMatchAlert(uid, generatedName);
            sendMatchNotification(uid, generatedName);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void storeMessage(String uid, String timestamp, String message) {
        ContentValues values = new ContentValues();
        values.put(ChatContract.MessageEntry.COLUMN_PARTNER_KEY, uid);
        values.put(ChatContract.MessageEntry.COLUMN_DATETIME, timestamp);
        values.put(ChatContract.MessageEntry.COLUMN_MESSAGE_TYPE,
                   ChatContract.MessageEntry.MessageType.RECEIVED.name());
        values.put(ChatContract.MessageEntry.COLUMN_MESSAGE_DATA, message);
        Uri uri = getContentResolver().insert(
                ChatContract.MessageEntry.buildMessagesByUidUri(uid), values);
        Log.d(TAG, "Added message: " + uri.toString());

        updateLastActivity(uid, timestamp);
    }

    private EmojiVector getEmojiVector() {
        EmojiVector emojiVector = null;
        try {
            File file = new File(getDir("data", MODE_PRIVATE), "emojiVectorMap");
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
            HashMap<String, Integer> emojiVectorMap = (HashMap<String, Integer>) inputStream.readObject();
            inputStream.close();
            emojiVector = new EmojiVector(emojiVectorMap);
        } catch (Exception e) {
            Log.e(TAG, "Failed to read emoji vector from file; attempting to write...");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String emoji1s = prefs.getString(getString(R.string.profile_emoji_one_key), "");
            String emoji2s = prefs.getString(getString(R.string.profile_emoji_two_key), "");
            String emoji3s = prefs.getString(getString(R.string.profile_emoji_three_key), "");
            emojiVector = new EmojiVector(emoji1s, emoji2s, emoji3s);
            try {
                File file = new File(getDir("data", MODE_PRIVATE), "emojiVectorMap");
                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                outputStream.writeObject(emojiVector.map);
                outputStream.flush();
                outputStream.close();
            } catch (IOException f) {
                Log.e(TAG, "Failed to write emoji vector to file" + f.toString());
            }
        }
        return emojiVector;
    }
    private String findNameFromUid(String uid) {
        Cursor cursor = getPartnerCursor(uid, PARTNER_PROJECTION_GENERATED_NAME);
        try {
            cursor.moveToNext();
            String name = cursor.getString(0);
            cursor.close();
            return name;
        } catch (NullPointerException | CursorIndexOutOfBoundsException e) {
            return "";
        }
    }

    private void updateLastActivity(String uid, String timestamp) {
        ContentValues values = new ContentValues();
        values.put(ChatContract.PartnerEntry.COLUMN_LAST_ACTIVITY, timestamp);
        values.put(ChatContract.PartnerEntry.COLUMN_IS_ARCHIVED, 0);
        getContentResolver().update(
                ChatContract.PartnerEntry.buildPartnerByUidUri(uid), values, null, null);
    }

    private void updateNumNewMessages(String fromUid) {
        Cursor cursor = getPartnerCursor(fromUid, PARTNER_PROJECTION_NUM_NEW_MESSAGES);
        int newMessages = 0;
        try {
            cursor.moveToNext();
            newMessages = cursor.getInt(0);
            cursor.close();
        } catch (NullPointerException | CursorIndexOutOfBoundsException e) {
            Log.e(TAG, "Could not get number of new messages for uid: " + fromUid);
        }

        ContentValues values = new ContentValues();
        values.put(ChatContract.PartnerEntry.COLUMN_NUM_NEW_MESSAGES, ++newMessages);
        getContentResolver().update(
                ChatContract.PartnerEntry.buildPartnerByUidUri(fromUid), values, null, null);
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String fromUid, String fromName, String message) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("uid", fromUid);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                                                                PendingIntent.FLAG_ONE_SHOT);
        if (fromName.isEmpty()) {
            fromName = "Message from a new partner!";
        }
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.small_icon)
                .setLargeIcon(
                        BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(fromName)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(getIntIDFromUid(fromUid) /* ID of notification */,
                notificationBuilder.build());
    }

    private void sendMatchNotification(String fromUid, String fromName) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("uid", fromUid);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.small_icon)
                .setLargeIcon(
                        BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle("You have a new match!")
                .setContentText("Start chatting with " + fromName)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(getIntIDFromUid(fromUid) /* ID of notification */,
                notificationBuilder.build());
    }

    private int getIntIDFromUid(String uid) {
        int result = 0;
        for (char c : uid.toCharArray()) {
            int num = Character.getNumericValue(c);
            if (num >= 0) {
                result += num;
            }
        }
        return result;
    }
}
