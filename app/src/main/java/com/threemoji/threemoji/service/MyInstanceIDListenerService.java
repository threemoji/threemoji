package com.threemoji.threemoji.service;

import com.google.android.gms.iid.InstanceIDListenerService;

// https://developers.google.com/instance-id/guides/android-implementation
public class MyInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = MyInstanceIDListenerService.class.getSimpleName();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        startService(RegistrationIntentService.createIntent(this,
                                                            RegistrationIntentService.Action.UPDATE_TOKEN));
    }
    // [END refresh_token]

}
