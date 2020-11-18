package io.abx.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.igaworks.v2.core.AdBrixRm;

import io.abx.myapplication.utilities.GGLogger;


// 도시명 입력 -> intent -> MainActivity -> Network(OPEN API)->MainActivity -> DetailActivity
// 맨 처음 도시명을 입력하여 넘기는 간단한 Activity
public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener{
    // UI View
    EditText searchCityNameEditText;
    ProgressBar loadingIndicator;
    Button searchBtn;
    TextView mWelcome;
//    private static final String TAG = MainActivity.class.getSimpleName();

    String cityName;


    // 이 activity가 처음 create 될 때, 각 layout xml을 바인딩 해주. 맨 처음 한번!!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_city_name);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // UI xml file binding
        searchCityNameEditText = (EditText) findViewById(R.id.search_box);
        loadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        searchBtn = (Button) findViewById(R.id.search_button);
        mWelcome = (TextView) findViewById(R.id.tv_welcome);
        searchBtn.setOnClickListener(this);
        // enter 처리
        searchCityNameEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    giveCityName();
                    return true;
                }
                return false;
            }
        });
        // SharedPreference 활용 하기
        // 맨 처음 -> preference 기본 default 값 호출 -> 화면에 보여주기
        mWelcome.setText(sharedPreferences.getString(getString(R.string.pref_user_name_key).trim(),
                getResources().getString(R.string.pref_user_name_default)) +
                "님 반갑습니다. 원하는 도시 날씨를 검색하세요!!!!");
        // onSharedPreferenceChangeListener 등록
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        // 디퍼트 딥링크 연동


        // 로컬 푸시 설정
//        connectPush("NOTIFICATION TEST", "This is for a test. Just ignore it");
        AdBrixRm.setPushEnable(true);

        AdBrixRm.setEventUploadCountInterval(AdBrixRm.AdBrixEventUploadCountInterval.MIN);
    }


    // 간단한 메뉴 (search) 만들기
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // res/menu 의 main.xml 불러와서 search 메뉴 바인딩
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // EditTextView에 입력한 cityName -> MainActivity로 전달하기 위한 method
    public void giveCityName() {
        String cityName = searchCityNameEditText.getText().toString().trim();
        if (isEmpty(cityName)){
            return;
        } else {
            // EditText에 입력한 cityName 가져오기
            // cityName 전달하기 위한 Intent
            Intent giveCityNameIntent = new Intent(getApplicationContext(), ListActivity.class);
            giveCityNameIntent.putExtra("city_name", cityName);
            // adbrix custom event data 상세 데이터 등록!!
            // city name 검색 시 호출 되야 하므로, EditText에 있는 String -> args 받아와 EVENT 등록 및 실행!
            AdBrixRm.AttrModel searchCityNameEvent =
                    new AdBrixRm.AttrModel().setAttrs("city_name", cityName);
            AdBrixRm.event("search_city_event", searchCityNameEvent);
            GGLogger.getInstance().D("search_city_event : " + cityName);
            startActivity(giveCityNameIntent);
            // cityName 검색 후, 빈칸으로 만들어 주기
            searchCityNameEditText.setText("");
        }
    }

    // search 메뉴 클릭 했을 때, 작동!
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 클릭한 item id 가져오기
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.action_settings) {
            // MainActivity -> SettingActivity 하기 위해 context정보를 Intent로 전달!!
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            // Adbrix sdk 연동 test
            AdBrixRm.event("setting_click_event");
            GGLogger.getInstance().D("adbrix sdk 연동 test");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // adbrix SDK 연동 test button!!
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_button: {
                GGLogger.getInstance().D("search button clicked!!!!");
                // intent를 통해 cityName 전달해줘라!
                giveCityName();

                AdBrixRm.BigTextPushProperties bigTextPushMessageProperties = new AdBrixRm.BigTextPushProperties()
                        .setTitle("PushTest")
                        .setContentText("This is ContentText")
                        .setBigContentTitle("CHECK WEATHER")
                        .setSummaryText("check today's weather")
                        .setBigText("Welcome. Do you want to check the weather?")
                        .setSecond(5)
                        .setEventId(12345)
                        .setDeepLinkUri("jake://settings");
                AdBrixRm.setBigTextClientPushEvent(this,bigTextPushMessageProperties, true);
                break;
            }
        }
    }

    // 사용자 이름 바뀌었을 때, listener -> 호출 되는 method
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!(sharedPreferences instanceof ListActivity)) {
            String changedName = sharedPreferences.getString(key, getString(R.string.pref_user_name_default)).trim();
            mWelcome.setText(changedName + "님 반갑습니다. 원하는 도시 날씨를 검색하세요!!!!");
        }
    }

    public boolean isEmpty(String cityName){
        if (cityName.equals("") || cityName.equals(null)) {
            // 빈칸일 때 Alert 띄어줘서 넘어가지 않도록 하기 !!!! -> adbrix console city_name_search_event 의 empty 값을 제거!!
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.alert_message_title)).setMessage(getString(R.string.alert_message_value));
            builder.setPositiveButton(getString(R.string.alert_ok_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // alert dialog ok button click -> no event need
                    searchCityNameEditText.setText("");
                }
            });
            // 무의미한 빈간, 띄어쓰기, 빈 값이 전달 됐을 때
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return true;
        } else {
            return false;
        }
    }

}