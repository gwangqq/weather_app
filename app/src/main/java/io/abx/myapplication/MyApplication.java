package io.abx.myapplication;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.igaworks.v2.core.AdBrixRm;
import com.igaworks.v2.core.application.AbxActivityHelper;
import com.igaworks.v2.core.application.AbxActivityLifecycleCallbacks;

import io.abx.myapplication.utilities.GGLogger;

public class MyApplication extends Application implements AdBrixRm.DeferredDeeplinkListener,AdBrixRm.onTouchRemotePushListener,AdBrixRm.onTouchLocalPushListener{

    @Override
    public void onCreate() {
        super.onCreate();
        AbxActivityHelper.initializeSdk(MyApplication.this, "Gzh8ovoHjkOJtN2NDZUIkw", "J3pKtrNT7ke7p9Xhr4RDsw");

        if (Build.VERSION.SDK_INT >= 14) {
            registerActivityLifecycleCallbacks(new AbxActivityLifecycleCallbacks());
        }
        AdBrixRm.setRemotePushMessageListener(this);  // Server Push
        AdBrixRm.setLocalPushMessageListener(this);   // Local Push
    }

    // Local Push Listener
    @Override
    public void onTouchLocalPush(String onTouchLocalPushString) {

        Log.d("jake", "onReceiveLocakPushmessage" + onTouchLocalPushString);
    }

    // Remote Push Listener
    @Override
    public void onTouchRemotePush(String onTouchRemotePushString) {

        Log.d("jake", "onReceiveRemotePushMessage" + onTouchRemotePushString);
    }

    @Override
    public void onReceiveDeferredDeeplink(String s) {
        GGLogger.getInstance().D("deferredDeeplink listener ::" + s);
    }
}