package io.abx.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.igaworks.v2.core.AdBrixRm;
import io.abx.myapplication.utilities.GGLogger;

public class DeeplinkActivity extends AppCompatActivity
//        implements AdBrixRm.DeferredDeeplinkListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onNewIntent(DeeplinkActivity.this.getIntent());
        GGLogger.getInstance().D("onCreate() in DeeplinkActivity  =========  city_name : " );

//        AdBrixRm.setDeferredDeeplinkListener(this);
    }

    // do Something when deep-link is called
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        GGLogger.getInstance().D("onNewIntent(Intent intent)");
        setIntent(intent);
        AdBrixRm.deeplinkEvent(DeeplinkActivity.this);
        GGLogger.getInstance().D("=====================");

        // get name of parameter
        Uri uri = Uri.parse(intent.getDataString());
        String str = uri.getQueryParameterNames().toString().replace("[", "").replace("]", "");


        GGLogger.getInstance().D("onNewIntent what is query? : " + str);
        GGLogger.getInstance().D("onNewIntent Uri : " + uri);

        switch (str) {
            case "city_name": {
                String cityName = uri.getQueryParameter(str);
                Intent startListActivityIntent = new Intent(getApplicationContext(), ListActivity.class);
                startListActivityIntent.putExtra("city_name", cityName);
                GGLogger.getInstance().D("onNewIntent city name : " + cityName);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startListActivityIntent);
                DeeplinkActivity.this.finish();
                break;
            }
            case "page": {
                Intent startSettingsActivity = new Intent(getApplicationContext(), SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startSettingsActivity);
                DeeplinkActivity.this.finish();
                break;
            }
        }


    }

//  deferred deeplink listener
/*    @Override
    public void onReceiveDeferredDeeplink(String s) {
        GGLogger.getInstance().D("===============");
        GGLogger.getInstance().D("DeeplinkActivity Listener : " + s);
    }*/
}