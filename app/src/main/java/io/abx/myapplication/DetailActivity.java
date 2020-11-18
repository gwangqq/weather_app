package io.abx.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.igaworks.v2.core.AdBrixRm;

import org.json.JSONException;
import org.json.JSONObject;

import io.abx.myapplication.utilities.GGLogger;
import io.abx.myapplication.utilities.SunshineWeatherUtils;

public class DetailActivity extends AppCompatActivity {

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private String mForecast;
    //  Primary information
    private TextView mWeatherDescription;
    private TextView mHighTemperature;
    private TextView mLowTemperature;
    private TextView mDate;
    private ImageView mImageView;

    //  Extra information
    private TextView mHumidity;
    private TextView mPressure;
    private TextView mWind;

    // 강수량
    private TextView mRainFall;
    private ImageView mRain;
    //    private TextView mTest;
    public final String TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_detail);
//        mTest = (TextView) findViewById(R.id.tv_test);

//      주요 날씨 정보 보여주기
        mWeatherDescription = (TextView) findViewById(R.id.weather_description);
        mHighTemperature = (TextView) findViewById(R.id.high_temperature);
        mLowTemperature = (TextView) findViewById(R.id.low_temperature);
        mDate = (TextView) findViewById(R.id.date);
        mImageView = (ImageView) findViewById(R.id.weather_icon);

//      강수량 보여주기(weather id에 따라 보여야 하므로, 처음에는 안보이는 것으로 지정할 것임)
        mRainFall = (TextView) findViewById(R.id.rain_fall);
        mRain = (ImageView) findViewById(R.id.ic_rain);
//      자잘한 날씨 정보 보여주기
        mHumidity = (TextView) findViewById(R.id.humidity);
        mPressure = (TextView) findViewById(R.id.pressure);
        mWind = (TextView) findViewById(R.id.wind_measurement);

        // MainActivity를 통해 DetailActivity 시작하게 하는 intent
        Intent intentThatStartedThisActivity = getIntent();

        // 비정상적인 상태
        if (intentThatStartedThisActivity == null ||
                !intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
            return;
        }

        // MainActivity 각 recyclerView에 저장되있는 JSONObject String 값 받아오기
        mForecast = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT);
        GGLogger.getInstance().D("DetailActivity로 넘어온 String data 확인!! = " + mForecast);
//                mTest.setText(mForecast);
//                  weather list 에서 클릭했을 때 JSON을 String으로 받아와서 JSONObject로 받아오기


//      주요 날씨 정보 보여주기
        try {
            JSONObject detailWeatherData = new JSONObject(mForecast);

            // weather Id 비오는 날씨 -> 강수량 표시
            //            비오지 않는 날씨 -> 강수량 표시 X
            int weatherId = detailWeatherData.getJSONArray("weather")
                    .getJSONObject(0)
                    .getInt("id");
            if ((weatherId >= 300 && weatherId <= 321) || (weatherId >= 500 && weatherId <= 504)
                    || (weatherId >= 520 && weatherId <= 531)) {
                showDetailWeather(weatherId, detailWeatherData);

                // 비올때 강수량 보여주기!
                mRainFall.setText(detailWeatherData.getJSONObject("rain").getString("3h") + "cm");
                mRainFall.setVisibility(View.VISIBLE);
                mRain.setImageResource(R.drawable.rain);
                mRain.setVisibility(View.VISIBLE);
            } else {
                showDetailWeather(weatherId, detailWeatherData);
                mRainFall.setVisibility(View.INVISIBLE);
                mRain.setVisibility(View.INVISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // SharedPreference 활용 하기
        // 사용자 이름이 바뀌면 같이 바꿔주기
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String name = sharedPreferences.getString("user", getString(R.string.pref_user_name_default));
        GGLogger.getInstance().D("DetailActivity에서 SharedPreference 쓰기 : " + name);
        AdBrixRm.setEventUploadCountInterval(AdBrixRm.AdBrixEventUploadCountInterval.MIN);
    }

    // 상세 날씨 정보 화면에 출력해주는 method
    public void showDetailWeather(int weatherId, JSONObject weatherDetail) throws JSONException {
        // weatherDetail information
        // API 정보 날짜
        mDate.setText(weatherDetail.getString("dt_txt"));
        // lang=kr 설정으로, 한글로 된 날씨 출력
        mWeatherDescription
                .setText(weatherDetail.getJSONArray("weather")
                        .getJSONObject(0)
                        .getString("description"));
        // 최고 온도 -> 섭씨
        mHighTemperature
                .setText(weatherDetail.getJSONObject("main").getString("temp_max") + getString(R.string.format_temperature) + "C");
        // 최저 온도 -> 섭씨
        mLowTemperature
                .setText(weatherDetail.getJSONObject("main").getString("temp_min") + getString(R.string.format_temperature) + "C");
        // weather id -> SunshineWeatherUtils -> image file 불러오는 method 사용
        // weather id 는 어느 Weather Open API 나 같다!
        mImageView.setImageResource(SunshineWeatherUtils
                .getIconResourceForWeatherCondition(
                        weatherId));
//      자잘한 날씨  정보 보여주기
        // 습도
        mHumidity
                .setText(weatherDetail.getJSONObject("main").getString("humidity") + "%");
        // 기압
        mPressure
                .setText(weatherDetail.getJSONObject("main").getString("pressure") + "hPa");
        // 풍속!
        mWind
                .setText(weatherDetail.getJSONObject("wind").getString("speed") + "m/s");
    }

    //  상세 날씨 보기 페이지에서 메뉴 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail, menu);
        return true;
    }

    //  상세 날씨 보기 페이지에서 메뉴 클릭 했을 때
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // DetailActivity -> Setting 메뉴 클릭 했을 때 -> setting 이동하기 위한 intent 생성 -> intent로 setting activity 실행!
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            // adbrix sdk 연동 test
            AdBrixRm.event("setting_click_event");
            return true;
        }

        if (id == android.R.id.home) {
            GGLogger.getInstance().D("Detail Activity Home Button click");
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}