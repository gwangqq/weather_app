package io.abx.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.igaworks.v2.core.AdBrixRm;

import io.abx.myapplication.utilities.GGLogger;


//  setting
public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    static String userName;

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // deep-link test
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        if (data==null){
            userName = null;
        } else{
            userName  = data.getQueryParameter("name");
        }

        GGLogger.getInstance().D("data 수신 test = " + userName);
        super.onCreate(savedInstanceState);
        // activity_settings.xml -> binding
        this.setContentView(R.layout.activity_settings);
        // setting -> 이전 화면으로 돌아가는 action bar 생성
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        GGLogger.getInstance().D("SettingsActivity test : " + userName);
        AdBrixRm.setEventUploadCountInterval(AdBrixRm.AdBrixEventUploadCountInterval.MIN);
    }

    // menu 및 action bar에 있는 widget 눌렀을 때 동작하는 method
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // 뒤로가기 버튼 눌렀을 때
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!(sharedPreferences instanceof ListActivity)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.pref_user_name_key), userName);
            editor.commit();
        }
    }
}