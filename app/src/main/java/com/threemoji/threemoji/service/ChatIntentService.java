package com.threemoji.threemoji.service;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import com.threemoji.threemoji.R;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

public class ChatIntentService extends IntentService {

    private static final String TAG = ChatIntentService.class.getSimpleName();
    private int timeToLive = 60 * 60; // one hour

    public ChatIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "intent string: " + intent.toString());
        if (intent.getStringExtra("message").startsWith("/lookup ")) {
            String targetUid = intent.getStringExtra("message").substring(8);
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            try {
                Bundle data = new Bundle();
                data.putString("action", getString(R.string.backend_action_lookup_profile));
                data.putString(getString(R.string.backend_uid_key), getPrefs().getString(getString(R.string.profile_uid_key), ""));
                data.putString(getString(R.string.backend_password_key), getPrefs().getString(getString(R.string.profile_password_key), ""));
                data.putString(getString(R.string.backend_profile_key), targetUid);
                String msgId = getNextMsgId(getPrefs().getString(getString(R.string.pref_token_key), ""));
                gcm.send(getString(R.string.gcm_project_num) + "@gcm.googleapis.com", msgId,
                        timeToLive, data);
                Log.v(TAG, "profile lookup request sent for target user: " + targetUid);
            } catch (IOException e) {
                Log.e(TAG, "IOException while sending request...", e);
            }
        } else if (intent.getStringExtra("message").startsWith("/delete ")) {
            String args[] = intent.getStringExtra("message").split("\\s+");
            if (args.length >= 3) {
                String targetUid = args[1], targetPass = args[2];
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
                try {
                    Bundle data = new Bundle();
                    data.putString("action", getString(R.string.backend_action_delete_profile));
                    data.putString(getString(R.string.backend_uid_key), targetUid);
                    data.putString(getString(R.string.backend_password_key), targetPass);
                    String msgId = getNextMsgId(getPrefs().getString(getString(R.string.pref_token_key), ""));
                    gcm.send(getString(R.string.gcm_project_num) + "@gcm.googleapis.com", msgId,
                            timeToLive, data);
                    Log.v(TAG, "delete profile request sent for target user: " + targetUid);
                } catch (IOException e) {
                    Log.e(TAG, "IOException while sending request...", e);
                }
            }
        } else {
            sendMessage(intent.getStringExtra("uuid"), intent.getStringExtra("message"));
        }
    }

    private void sendMessage(String to_uid, String message) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        try {
            Bundle data = new Bundle();
            data.putString("action", getString(R.string.backend_action_send_message));
            data.putString(getString(R.string.backend_uid_key), getPrefs().getString(getString(R.string.profile_uid_key), ""));
            data.putString(getString(R.string.backend_password_key), getPrefs().getString(getString(R.string.profile_password_key), ""));
            data.putString(getString(R.string.backend_to_key), to_uid);
            data.putString(getString(R.string.backend_message_key), message);
            String msgId = getNextMsgId(getPrefs().getString(getString(R.string.pref_token_key), ""));
            gcm.send(getString(R.string.gcm_project_num) + "@gcm.googleapis.com", msgId,
                    timeToLive, data);
            Log.v(TAG, "message sent to user: " + to_uid);
        } catch (IOException e) {
            Log.e(TAG, "IOException while sending message...", e);
        }
    }

    public String getNextMsgId(String token) {
        return token.substring(token.length() - 5).concat("" + System.currentTimeMillis());
    }

    private SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

}
