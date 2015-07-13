package com.threemoji.threemoji;

import com.google.android.gms.location.LocationResult;

import com.threemoji.threemoji.service.RegistrationIntentService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

public class LocationReceiver extends BroadcastReceiver {

    public static final String TAG = LocationReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (LocationResult.hasResult(intent)) {
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();
            Log.d(TAG, String.valueOf(location.getLatitude()) + ", " +
                       String.valueOf(location.getLongitude()));

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
                                                               .edit();
            editor.putString(context.getString(R.string.profile_location_latitude),
                             String.valueOf(location.getLatitude()));
            editor.putString(context.getString(R.string.profile_location_longitude),
                            String.valueOf(location.getLongitude()));
            editor.apply();

            Intent registrationIntent =
                    RegistrationIntentService
                            .createIntent(context,
                                          RegistrationIntentService.Action.UPDATE_LOCATION);
            context.startService(registrationIntent);
        }
    }
}
