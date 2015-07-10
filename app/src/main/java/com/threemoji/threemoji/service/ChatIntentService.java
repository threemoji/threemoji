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

    public static final String TAG = ChatIntentService.class.getSimpleName();
    public static final int TIME_TO_LIVE_SECONDS = 60 * 60; // one hour

    private GoogleCloudMessaging mGcm;

    public enum Action {
        LOOKUP_ALL, LOOKUP_UID
    }

    public ChatIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mGcm = GoogleCloudMessaging.getInstance(this);
        Log.d(TAG, "Intent string: " + intent.toString());
        String action = intent.getStringExtra("action");

        if (action != null && Action.valueOf(action) == Action.LOOKUP_ALL) {
            try {
                Bundle data = new Bundle();
                data.putString("action", getString(R.string.backend_action_lookup_nearby_key));
                data.putString(getString(R.string.backend_uid_key), getPrefs().getString(getString(R.string.profile_uid_key), ""));
                data.putString(getString(R.string.backend_password_key), getPrefs().getString(getString(R.string.profile_password_key), ""));

                data.putString(getString(R.string.backend_radius_key), "123");

                String msgId = getNextMsgId(getPrefs().getString(getString(R.string.pref_token_key), ""));
                sendData(data, msgId);
                Log.d(TAG, "Nearby lookup request sent");
            } catch (IOException e) {
                Log.e(TAG, "IOException while sending request...", e);
            }
        } else if (action != null && Action.valueOf(action) == Action.LOOKUP_UID) {
            String targetUid = intent.getStringExtra("uuid");
            try {
                Bundle data = new Bundle();
                data.putString("action", getString(R.string.backend_action_lookup_profile_key));
                data.putString(getString(R.string.backend_uid_key), getPrefs().getString(getString(R.string.profile_uid_key), ""));
                data.putString(getString(R.string.backend_password_key), getPrefs().getString(getString(R.string.profile_password_key), ""));

                data.putString(getString(R.string.backend_profile_key), targetUid);

                String msgId = getNextMsgId(getPrefs().getString(getString(R.string.pref_token_key), ""));
                sendData(data, msgId);
                Log.d(TAG, "Profile lookup request sent for target user: " + targetUid);
            } catch (IOException e) {
                Log.e(TAG, "IOException while sending request...", e);
            }
        } else {
            sendMessage(intent.getStringExtra("uid"), intent.getStringExtra("message"));
        }
    }

    private void sendMessage(String toUid, String message) {
        try {
            Bundle data = new Bundle();
            data.putString("action", getString(R.string.backend_action_send_message_key));
            data.putString(getString(R.string.backend_uid_key), getPrefs().getString(getString(R.string.profile_uid_key), ""));
            data.putString(getString(R.string.backend_password_key), getPrefs().getString(getString(R.string.profile_password_key), ""));

            data.putString(getString(R.string.backend_to_key), toUid);
            data.putString(getString(R.string.backend_message_key), message);

            String msgId = getNextMsgId(getPrefs().getString(getString(R.string.pref_token_key), ""));
            sendData(data, msgId);
            Log.d(TAG, "Message sent to user: " + toUid);
        } catch (IOException e) {
            Log.e(TAG, "IOException while sending message...", e);
        }
    }

    private void sendData(Bundle data, String msgId) throws IOException {
        mGcm.send(getString(R.string.gcm_project_num) + "@gcm.googleapis.com", msgId,
                  TIME_TO_LIVE_SECONDS, data);
    }

    public String getNextMsgId(String token) {
        if (token.length() < 5) { // No token yet
            token = "ABCDE";
        }
        return token.substring(token.length() - 5).concat("" + System.currentTimeMillis());
    }

    private SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

}
