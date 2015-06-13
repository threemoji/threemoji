package com.threemoji.threemoji.service;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import com.threemoji.threemoji.R;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = RegistrationIntentService.class.getSimpleName();
    private int timeToLive = 60 * 60; // one hour

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "intent string: " + intent.toString());
        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                unregisterFromServer();
                String token = getGcmToken();
                sendTokenToServer(token);
                storeToken(token);
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        } finally {
            stopSelf();
        }
    }

    private String getGcmToken() throws IOException {
        InstanceID instanceID = InstanceID.getInstance(this);
        String token = instanceID.getToken(getString(R.string.gcm_project_id),
                                           GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                                           null);
        Log.i(TAG, "GCM Registration Token: " + token);
        return token;
    }

    private void unregisterFromServer() throws IOException {
        InstanceID instanceID = InstanceID.getInstance(this);
        instanceID.deleteToken(getString(R.string.gcm_project_id),
                               GoogleCloudMessaging.INSTANCE_ID_SCOPE);
    }

    private void sendTokenToServer(String token) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        try {
            Bundle data = new Bundle();
            data.putString("payload", "test payload");
            String msgId = Integer.toString(getNextMsgId());
            gcm.send(getString(R.string.gcm_project_id) + "@gcm.googleapis.com", msgId,
                     timeToLive, data);
            Log.v(TAG, "token sent: " + token);
        } catch (IOException e) {
            Log.e(TAG,
                  "IOException while sending token to backend...", e);
        }
    }

    private void storeToken(String token) {
        Log.i(TAG, "Saving token to prefs: " + token);
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString("gcmToken", token).apply();
    }

    private int getNextMsgId() {
        int id = getPrefs().getInt("keyMsgId", 0);
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putInt("keyMsgId", ++id).apply();
        return id;
    }

    private SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

}
