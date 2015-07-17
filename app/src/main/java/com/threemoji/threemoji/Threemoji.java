package com.threemoji.threemoji;

import android.app.Application;
import android.content.Context;

// http://stackoverflow.com/questions/4391720/how-can-i-get-a-resource-content-from-a-static-context
public class Threemoji extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }
}