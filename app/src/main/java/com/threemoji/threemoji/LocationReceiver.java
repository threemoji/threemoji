package com.threemoji.threemoji;

import com.google.android.gms.location.LocationResult;

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
        if (LocationResult.hasResult(intent)){
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();
            Log.d(TAG, String.valueOf(location.getLatitude()) + ", " +
                       String.valueOf(location.getLongitude()));

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putFloat(context.getString(R.string.profile_location_latitude),
                            Double.doubleToRawLongBits(location.getLatitude()));
            editor.putFloat(context.getString(R.string.profile_location_longitude),
                            Double.doubleToRawLongBits(location.getLongitude()));
            editor.apply();
        }
    }
}
