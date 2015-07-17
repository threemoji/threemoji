package com.threemoji.threemoji.service;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.threemoji.threemoji.LocationReceiver;
import com.threemoji.threemoji.R;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

public class BackgroundLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = BackgroundLocationService.class.getSimpleName();

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000 * 60 * 60;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1000 * 60 * 30;
    public static final int BROADCAST_REQUEST_CODE = 1;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private PendingIntent mLocationIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getBooleanExtra(getString(R.string.location_service_lookup_nearby), false) && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            startLocationUpdates();
            lookupNearbyUsingLastLocation();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connection successful");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " +
                   connectionResult.getErrorCode());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private void startLocationUpdates() {
        Log.v(TAG, "Starting location updates");
        Intent intent = new Intent(this, LocationReceiver.class);
        mLocationIntent = PendingIntent.getBroadcast(this, BROADCAST_REQUEST_CODE, intent,
                                                     PendingIntent.FLAG_UPDATE_CURRENT);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                                                                 mLocationIntent);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationIntent);
    }

    private void lookupNearbyUsingLastLocation() {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
                                                           .edit();
        if (lastLocation != null) {
            editor.putString(this.getString(R.string.profile_location_latitude),
                             String.valueOf(lastLocation.getLatitude()));
            editor.putString(this.getString(R.string.profile_location_longitude),
                             String.valueOf(lastLocation.getLongitude()));
            editor.apply();
            Log.v(TAG, "Starting lookup nearby intent");
            startService(ChatIntentService.createIntent(this, ChatIntentService.Action.LOOKUP_ALL));
        } else {
            editor.putLong(getString(R.string.prefs_lookup_nearby_time), System.currentTimeMillis()).apply();
        }
    }
}
