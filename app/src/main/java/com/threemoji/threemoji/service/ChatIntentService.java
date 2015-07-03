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
        sendMessage(intent.getStringExtra("uuid"), intent.getStringExtra("message"));
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
