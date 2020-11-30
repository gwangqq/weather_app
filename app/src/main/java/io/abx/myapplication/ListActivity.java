package io.abx.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.igaworks.v2.core.AdBrixRm;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import io.abx.myapplication.utilities.GGLogger;
import io.abx.myapplication.utilities.NetworkUtils;
import io.abx.myapplication.utilities.OpenWeatherJsonUtils;

public class ListActivity extends AppCompatActivity implements
        ForecastAdapter.ForecastAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<JSONObject>, SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {

    // static variables
//    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int FORECAST_LOADER_ID = 0;
    private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;

    // UI View
    private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;
    // 검색한 도시 이름 보여주는 TextView
    private TextView mCityName;
    private JSONObject cityInfoJson;
    private Button btnOpenGoogleMap;


    String cityName;
    // View Life Cycle - ListActivity가 처음으로 만들어질 때 최초 1번 호출된다!!!!!!!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // deferred deep link listener 등록
//         뒤로 가기 버튼 생성
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        // deferred deep-link

        // R.lyaout 폴더에 있는 내 뷰를 가져와서 바인딩해준다
        setContentView(R.layout.activity_forecast);
        // adbrix 연동 과제.
        // 1. Activity 진입시 MAIN_LIST Event 호출!
        // 2. 어떤 도시를 클릭했는지 custom event 에 등록!

        // list에 사용할 RecyclerView를 xml에서 키 값을 가져와서 변수에 넣어줌.
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        // 날씨 정보를 서버로부터 받아오는 동안 보여줄 indicator
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        // city name 보여주는 TextView --> 버튼이나 뭐 이용해서 위도, 경도로 구글 맵 킬거임
        mCityName = (TextView) findViewById(R.id.tv_city_name);
        mCityName.setOnClickListener(this);
        mCityName.setVisibility(View.INVISIBLE);

        // RecyclerView에 layoutManager 선언(LinearLayoutManager--> vertical or horizontal 가능)
        // list 형식으로 세로로 쭉 보여주기 위해 vertical 로 설정
        boolean shouldReverseLayout = false;
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, shouldReverseLayout);

        // RecyclerView 를 사용하기 위해
        mForecastAdapter = new ForecastAdapter(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        // LoaderManager -> loader start
        // getSupportLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this); --> deprecated
        LoaderManager.getInstance(this).initLoader(FORECAST_LOADER_ID, null, this);
        // onCreate 확인!
        GGLogger.getInstance().D("onCreate에서 SharedPreference Change Lister 등록 성공!");
        // PreferenceManager -> Preference에서 변경된 내용이 있다면 확인해서 MainActivity에 반영!!!
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        btnOpenGoogleMap = (Button) findViewById(R.id.btn_open_map);
        btnOpenGoogleMap.setOnClickListener(this);
        btnOpenGoogleMap.setVisibility(View.INVISIBLE);
// deep-link open


        Log.d("JAKE","call onNewIntent on onCreate()");
        //onNewIntent(ListActivity.this.getIntent());
        AdBrixRm.setEventUploadCountInterval(AdBrixRm.AdBrixEventUploadCountInterval.MIN);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (PREFERENCES_HAVE_BEEN_UPDATED) {
            // LoaderManager -> background 에서 새로운 로더를 최초로 등록!!
            LoaderManager.getInstance(this).initLoader(FORECAST_LOADER_ID, null, this);
            PREFERENCES_HAVE_BEEN_UPDATED = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // LifeCycle 끝날 때 -> SharedPreferenceChangeListener unregister!
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }


    // Android SDK 파편화 때문에 이렇게 노란줄 뜬다..
    // 추천 되는 방법은 로더를 사용할 액티비티안에 AsyncTaskLoader 클래스를 하나 선언하여 사용하는 것!
    @Override
    public Loader<JSONObject> onCreateLoader(int id, final Bundle loaderArgs) {
        // AsyncTaskLoader 순서
        return new AsyncTaskLoader<JSONObject>(this) {
            JSONObject mWeatherData = null;

            // Loader 가 처음 시작할 때
            // 1. 맨 처음 MainActivity에 진입할 때
            // 2. MainActivity에서 새로고침(Refresh) 버튼을 클릭하여 새로운 데이터 받아올 때!
            @Override
            protected void onStartLoading() {
                GGLogger.getInstance().D(">>> onStartLoading()");

                if (mWeatherData != null) {
                    deliverResult(mWeatherData);
                } else {
                    Handler delayHandler = new Handler();
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    delayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            forceLoad();
                        }
                    }, 1500);
                }
            }

            @Override
            public JSONObject loadInBackground() {


                GGLogger.getInstance().D(">>> loadInBackground()");


//              searchNameActivity에서 editText에 입력한 cityName intent로 전달 받기
                Intent intent = getIntent();
                String action = intent.getAction();




                if ( null != action) {
                    Uri data = intent.getData();
                    cityName = data.getQueryParameter("city_name");
                } else {
                    cityName = intent.getExtras().getString("city_name");
                }
                GGLogger.getInstance().D("ListActivity test : " + cityName + " action : " + action);

//              SearchCityActivity에서 받아온 cityName을 parameter로 활용하여 URL을 생성해 내기!!
                URL weatherRequestUrl = NetworkUtils.buildUrl(cityName);
                GGLogger.getInstance().D("cityname 여기까지 잘 넘어옴!");

                try {
                    // 위에서 cityName parameter를 통해 생성한 URL을 통해 String 형태의 JSON 값 받아오기
                    String jsonWeatherResponse = NetworkUtils
                            .getResponseFromHttpUrl(weatherRequestUrl);
                    GGLogger.getInstance().D("Http와 통신 후 jsonWeatherResponse 확인 = " + jsonWeatherResponse);
                    // 위에서 생성한 JSONObject에 JSONArray 형태로 저장된 값을
                    // String[] 형태로 받아오기!
                    // MainActivity list 형식으로 weather data를 보여줄 때, 각 weather item에 대한 정보!
                    JSONObject simpleJsonWeatherData = OpenWeatherJsonUtils
                            .getSimpleCityWeatherStringsFromJson(jsonWeatherResponse);
                    GGLogger.getInstance().D("Json 값을 String 배열에 넣기 성공!!");
                    // JSONArray -> String[]으로 정보를 잘 받아왔는지 확인하기 위한 Log
//                    for (int i = 0; i < simpleJsonWeatherData.length; i++) {
//                        GGLogger.getInstance().D("parsed JSON Data 확인 = " + simpleJsonWeatherData[i]);
//                    }
                    // 값이 제대로 들어왔다면 JSON 형식으로 받아온 객체를 반환


                    return simpleJsonWeatherData;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }


            }

            // doInBackground 에서 받아온 정보를 전달해주는 method

            public void deliverResult(JSONObject data) {
                GGLogger.getInstance().D(">>> deliverResult(JSONObject data)");
                mWeatherData = data;
                //GGLogger.getInstance().D(">>> check what is in data ::: " + data.toString());

                super.deliverResult(data);
            }

        };
    }

    // Loader의 doInBackground를 통해 전달된 String[] 형태의 data가 제대로 넘어왔다면
    // 1. 정상 작동 -> weather data 보여주기, 2. 비정상 작동 -> 에러 메시지 보여주기
    @Override
    public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setAdapter(mForecastAdapter);
        // 로더를 항상 종료 될 때 호출하면 -> data를 불러온 뒤
        // UI 제대로 받아오기 위해
        // report에 empty 값 나오는 이유 -> UI 작업이 완성 됐을 때, background 작업이 완성되지 않음
        // background 작업이 완성 된 후 RecyclerView 그릴 수 있도록 수정
        // 어림짐작해서 adpater를 호출 한다 -> UI delay를 예상 할 수 밖에 없음
        // data JSONObject 형식으로 바꿨기 때문에 adapter method 에서 Json 가져가서 처리해야함! JSON가져와서 하나 꺼내가는 method 만들것


        if (null == data) {
            showErrorMessage();
            mCityName.setVisibility(View.GONE);
            btnOpenGoogleMap.setVisibility(View.GONE);
        } else {
            try {
                mForecastAdapter.setWeatherData(data.getJSONArray("listInfo"));
                cityInfoJson = data.getJSONObject("cityInfo");
                String parsedCityName = cityInfoJson.getString("name");
                mCityName.setText(parsedCityName);
                btnOpenGoogleMap.setVisibility(View.VISIBLE);
                btnOpenGoogleMap.setText("map of " + parsedCityName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            showWeatherDataView();
            mCityName.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<JSONObject> loader) {

    }

    // Menu를 생성하기 위해 xml 화면을 바인딩해주는 method
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.forecast, menu);
        return true;
    }


    // RecyclerView로 만든 weather list data 하나를 클릭했을 때 호출하는 method
    @Override
    public void onClick(String weatherForDay) {
        // MainActivity(weather list)의 정보를 클릭했을 때,
        // 각 list item에 저장된 data를 DetailActivity로 전달하기 위한 Intent 선언과 활용
        Intent intentToStartDetailActivity = new Intent(this, DetailActivity.class);
        // OpenApi를 통해 받아온 JSON을 String 상태로 Intent에 담기
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, weatherForDay);
        // Intent를 전달하여 새로운 Activity를 활용하기!
        startActivity(intentToStartDetailActivity);
    }

    // 날씨 앱이 정상 작동했을 때, 날씨 보여주는 TextView -> Visible, ErrorMessage -> invisible
    private void showWeatherDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    // 날씨 앱이 비정상적으로 작동했을 때, 날씨 보여주는 TextView를 숨기고 ErrorMessage TextView를 보여주기
    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    // 메인 화면에서 menu를 클릭했을 때
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // menu를 클릭했을 때 item의 id를 Integer 형식으로 가져오기
        int id = item.getItemId();
        // id가 새로고침 일 때!
        // 1. 새로고침 -> data를 초기화 하여 새롭게 받아와야 한다.
        if (id == R.id.action_refresh) {
            GGLogger.getInstance().D("refresh button check!! refresh 버튼 눌림!");
            LoaderManager.getInstance(this).restartLoader(FORECAST_LOADER_ID, null, this);
            GGLogger.getInstance().D("refresh 클릭!");
            // 3. LoaderManager를 새롭게 호출하기 위해 다시 한번 LoaderManager 불러오기!
            AdBrixRm.event("refresh_list_event");

            return true;
        }

        // Setting 메뉴 눌렀을 때 이동!
        if (id == R.id.action_settings) {
            // MainActivity -> SettingActivity 하기 위해 context정보를 Intent로 전달!!
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            // Adbrix sdk 연동 test
            AdBrixRm.event("setting_click_event");
            GGLogger.getInstance().D("adbrix sdk 연동 test");
            return true;
        }
        // 뒤로 가기 버튼 눌렀을 때 동작!
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    // onSharedPreference 바뀌었을 때,
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // 변화했을 때 true가 되므로 updated 되었음을 알려줌
        PREFERENCES_HAVE_BEEN_UPDATED = true;
    }

    // cityName click -> lat, lon 정보 -> googleMap
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_open_map: {
                try {
                    String lat = cityInfoJson.getString("lat");
                    String lon = cityInfoJson.getString("lon");
                    String name = cityInfoJson.getString("name");
                    // Toast.makeText(this, "lat:" + lat + " long:" + lon, Toast.LENGTH_SHORT).show();
                    // lat, lon -> 구글맵 열기
                    openGoogleMap(lat, lon, name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    // lat, lon 정보 가져와서 구글맵 열기 -> cityName으로 가져옴
    public void openGoogleMap(String lat, String lon, String name) {
        // 구글 맵 열었을 때 event 추가
        AdBrixRm.event("city_map_open");
        // 구글맵 열기 위한 package 설정 및 intent로 구글맵 열기 줌, 도시 중앙 설정
        Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lon + "?z=1&q=" + Uri.encode("center of " + name));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
        GGLogger.getInstance().D("geoInfo : lat=" + lat + " lon=" + lon);
    }

}