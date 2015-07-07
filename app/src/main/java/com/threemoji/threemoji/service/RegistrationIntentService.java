package com.threemoji.threemoji.service;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import com.threemoji.threemoji.R;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = RegistrationIntentService.class.getSimpleName();
    private static final int TIME_TO_LIVE_SECONDS = 60 * 60; // one hour

    private GoogleCloudMessaging mGcm;
    private InstanceID mInstanceID;

    public enum Action {
        CREATE_PROFILE, UPDATE_PROFILE, CREATE_TOKEN, UPDATE_TOKEN
    }

    public RegistrationIntentService() {
        super(TAG);
    }

    public static Intent createIntent(Context context, Action action) {
        Intent intent = new Intent(context, RegistrationIntentService.class);
        intent.putExtra("action", action.name());
        return intent;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mGcm = GoogleCloudMessaging.getInstance(this);
        mInstanceID = InstanceID.getInstance(this);
        Log.v(TAG, "intent string: " + intent.toString());
        Action action = Action.valueOf(intent.getStringExtra("action"));
        try {
            switch (action) {

                case CREATE_PROFILE:
                    initUid();
                    initPassword();
                    uploadProfile(false);
                    break;

                case UPDATE_PROFILE:
                    uploadProfile(true);
                    break;

                case CREATE_TOKEN:
                    unregisterTokenFromServer();
                    getAndStoreGcmToken();
                    sendTokenToServer();
                    break;

                case UPDATE_TOKEN:
                    unregisterTokenFromServer();
                    getAndStoreGcmToken();
                    updateTokenOnServer();
                    break;
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete " + action, e);
        } finally {
            stopSelf();
        }
    }


    // ================================================================
    // Methods that communicate with GCM
    // ================================================================
    private void unregisterTokenFromServer() throws IOException {
        Log.v(TAG, "Unregistering from server");
        mInstanceID.deleteToken(getString(R.string.gcm_project_num),
                                GoogleCloudMessaging.INSTANCE_ID_SCOPE);
    }

    private void uploadProfile(boolean update) {
        Log.v(TAG, "Uploading profile");
        SharedPreferences prefs = getPrefs();
        String token = prefs.getString(getString(R.string.pref_token_key), "");
        try {
            Bundle data = new Bundle();
            data.putString("action", update ? getString(
                    R.string.backend_action_update_profile_key) : getString(
                    R.string.backend_action_upload_profile_key));
            data.putString(getString(R.string.backend_uid_key),
                           prefs.getString(getString(R.string.profile_uid_key), ""));
            data.putString(getString(R.string.backend_password_key),
                           prefs.getString(getString(R.string.profile_password_key), ""));
            data.putString(getString(R.string.backend_token_key), token);
            data.putString(getString(R.string.backend_emoji_one_key),
                           prefs.getString(getString(R.string.profile_emoji_one_key), ""));
            data.putString(getString(R.string.backend_emoji_two_key),
                           prefs.getString(getString(R.string.profile_emoji_two_key), ""));
            data.putString(getString(R.string.backend_emoji_three_key),
                           prefs.getString(getString(R.string.profile_emoji_three_key), ""));
            data.putString(getString(R.string.backend_generated_name_key),
                           prefs.getString(getString(R.string.profile_generated_name_key),
                                           ""));
            data.putString(getString(R.string.backend_gender_key),
                           prefs.getString(getString(R.string.profile_gender_key), ""));
            data.putString(getString(R.string.backend_location_key), "LOCATION");
            data.putString(getString(R.string.backend_radius_key),
                           prefs.getString(getString(R.string.pref_max_distance_key),
                                           getString(R.string.pref_max_distance_default)));
            sendData(token, data);
            Log.v(TAG, "profile uploaded");
        } catch (IOException e) {
            Log.e(TAG, "IOException while uploading profile to backend...", e);
        }
    }

    private void sendTokenToServer() {
        Log.v(TAG, "Sending token to server");
        String token = getPrefs().getString(getString(R.string.pref_token_key), "");
        try {
            Bundle data = new Bundle();
            data.putString("payload", token);
            sendData(token, data);
            Log.v(TAG, "token sent: " + token);
        } catch (IOException e) {
            Log.e(TAG, "IOException while sending token to backend...", e);
        }
    }

    private void updateTokenOnServer() {
        Log.v(TAG, "Updating token on server");
        SharedPreferences prefs = getPrefs();
        String token = prefs.getString(getString(R.string.pref_token_key), "");
        try {
            Bundle data = new Bundle();
            data.putString("action", getString(R.string.backend_action_update_token_key));
            data.putString(getString(R.string.backend_uid_key),
                           prefs.getString(getString(R.string.profile_uid_key), ""));
            data.putString(getString(R.string.backend_password_key),
                           prefs.getString(getString(R.string.profile_password_key), ""));
            data.putString(getString(R.string.backend_token_key), token);
            sendData(token, data);
            Log.v(TAG, "token updated: " + token);
        } catch (IOException e) {
            Log.e(TAG, "IOException while updating token on backend...", e);
        }
    }

    private void sendData(String token, Bundle data) throws IOException {
        mGcm.send(getString(R.string.gcm_project_num) + "@gcm.googleapis.com",
                  getNextMsgId(token),
                  TIME_TO_LIVE_SECONDS, data);
    }


    // ================================================================
    // Methods that modify local preferences
    // ================================================================
    private void initUid() {
        Log.v(TAG, "Initialising uuid");
        String uid = getPrefs().getString(getString(R.string.profile_uid_key), null);
        if (uid == null) {
            getPrefs().edit()
                      .putString(getString(R.string.profile_uid_key), UUID.randomUUID().toString())
                      .apply();
        }
    }

    private void initPassword() {
        Log.v(TAG, "Initialising password");
        String password = getPrefs().getString(getString(R.string.profile_password_key), null);
        if (password == null) {
            getPrefs().edit()
                      .putString(getString(R.string.profile_password_key),
                                 mInstanceID.getId())
                      .apply();
        }
    }

    private void getAndStoreGcmToken() throws IOException {
        String token = mInstanceID.getToken(getString(R.string.gcm_project_num),
                                            GoogleCloudMessaging.INSTANCE_ID_SCOPE);
        Log.i(TAG, "GCM Registration Token: " + token);
        storeToken(token);
    }

    private void storeToken(String token) {
        Log.i(TAG, "Saving token to prefs: " + token);
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString(getString(R.string.pref_token_key), token).apply();
    }


    // ================================================================
    // Utility methods
    // ================================================================
    public String getNextMsgId(String token) {
        return token.substring(token.length() - 5).concat("" + System.currentTimeMillis());
    }

    private SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }
}
