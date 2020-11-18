package io.abx.myapplication;

import android.app.Application;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.igaworks.v2.core.AdBrixRm;
import com.igaworks.v2.core.application.AbxActivityHelper;
import com.igaworks.v2.core.application.AbxActivityLifecycleCallbacks;


import io.abx.myapplication.utilities.GGLogger;

public class MyApplication extends Application implements AdBrixRm.DeferredDeeplinkListener {

    private MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();


        if (Build.VERSION.SDK_INT >= 14) {
            registerActivityLifecycleCallbacks(new AbxActivityLifecycleCallbacks());
        }
        AbxActivityHelper.initializeSdk(MyApplication.this, "A5rMExypwk6qKAmu3ezFDA", "5aCTd34GykyGFskV3zpTRw");

        AdBrixRm.setDeferredDeeplinkListener(this);

    }

    @Override
    public void onReceiveDeferredDeeplink(String urlStr) {
        GGLogger.getInstance().D("onReceiveDeferredDeeplink url : " + urlStr);
        Uri uri = Uri.parse(urlStr);
        String parameter = uri.getQueryParameter("city_name");
        GGLogger.getInstance().D("onReceiveDeferredDeeplink city_name : " + parameter);
        String cityName = parameter;
        myApplication = (MyApplication) getApplicationContext();
        Intent intent = new Intent(myApplication, ListActivity.class);
        intent.putExtra("city_name", cityName);
        startActivity(intent);
    }


}
