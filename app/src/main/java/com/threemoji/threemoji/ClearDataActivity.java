package com.threemoji.threemoji;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.threemoji.threemoji.service.RegistrationIntentService;

import java.io.File;
import java.io.IOException;


public class ClearDataActivity extends AppCompatActivity {

    private static final String TAG = ClearDataActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_data);
    }

    public void clearAppData(View view) {
        Log.v(TAG, "Deleting profile on server");
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String token = prefs.getString(getString(R.string.pref_token_key), "");
        try {
            Bundle data = new Bundle();
            data.putString("action", getString(R.string.backend_action_delete_profile_key));
            data.putString(getString(R.string.backend_uid_key), prefs.getString(getString(R.string.profile_uid_key), ""));
            data.putString(getString(R.string.backend_password_key), prefs.getString(getString(R.string.profile_password_key), ""));
            String msgId = getNextMsgId(token);
            gcm.send(getString(R.string.gcm_project_num) + "@gcm.googleapis.com", msgId, data);
            Log.v(TAG, "delete profile request sent");
        } catch (IOException e) {
            Log.e(TAG, "IOException while sending request...", e);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ((ActivityManager) this.getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
        } else {
            File cache = getCacheDir();
            File appDir = new File(cache.getParent());
            if (appDir.exists()) {
                String[] children = appDir.list();
                for (String s : children) {
                    if (!s.equals("lib")) {
                        deleteDir(new File(appDir, s));
                        Log.i("ClearDataActivity", "**************** File .../" + s +
                                     " DELETED *******************");
                    }
                }
            }
            finish();
        }
    }

    public boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public String getNextMsgId(String token) {
        if (token.length() < 5) { // No token yet
            token = "ABCDE";
        }
        return token.substring(token.length() - 5).concat("" + System.currentTimeMillis());
    }
}
