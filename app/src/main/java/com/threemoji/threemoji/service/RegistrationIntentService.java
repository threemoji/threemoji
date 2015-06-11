package com.threemoji.threemoji.service;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import com.threemoji.threemoji.QuickstartPreferences;
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
    private long timeToLive = 60 * 60 * 1000; // one hour

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = getPrefs();
        boolean isRegistrationComplete = sharedPreferences.getBoolean(
                QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);

        if (!isRegistrationComplete) {
            try {
                // In the (unlikely) event that multiple refresh operations occur simultaneously,
                // ensure that they are processed sequentially.
                synchronized (TAG) {
                    // [START register_for_gcm]
                    // Initially this call goes out to the network to retrieve the token, subsequent calls
                    // are local.
                    // [START get_token]
                    InstanceID instanceID = InstanceID.getInstance(this);
                    String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                                                       GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                                                       null);
                    // [END get_token]
                    Log.i(TAG, "GCM Registration Token: " + token);

                    // TODO: Implement this method to send any registration to your app's servers.
                    sendTokenToServer(token);

                    // Persist the token - no need to register again.
                    storeToken(token);

                    // You should store a boolean that indicates whether the generated token has been
                    // sent to your server. If the boolean is false, send the token to your server,
                    // otherwise your server should have already received the token.
                    sharedPreferences.edit()
                                     .putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true)
                                     .apply();
                    // [END register_for_gcm]
                }
            } catch (Exception e) {
                Log.d(TAG, "Failed to complete token refresh", e);
                // If an exception happens while fetching the new token or updating our registration data
                // on a third-party server, this ensures that we'll attempt the update at a later time.
                sharedPreferences.edit()
                                 .putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false)
                                 .apply();
            }
        }
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side
     * account
     * maintained by your application.
     *
     * @param token The new token.
     */
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
        editor.putString("keyToken", token);
        editor.putInt("keyState", 1);
        editor.apply();
    }

    private int getNextMsgId() {
        int id = getPrefs().getInt("keyMsgId", 0);
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putInt("keyMsgId", ++id);
        editor.apply();
        return id;
    }

    private SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

}
