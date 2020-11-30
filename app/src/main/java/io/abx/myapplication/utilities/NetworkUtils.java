package io.abx.myapplication.utilities;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

// 1. MainActivity Activity -> 전달 받은 cityName -> OPEN API 호출을 위한 URL 생성
// 2. URL을 통해 Http 통신하기
public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();
    // OPEN API domain
    private static final String STATIC_WEATHER_URL =
            "http://api.openweathermap.org/data/2.5/forecast?";
    private static final String FORECAST_BASE_URL = STATIC_WEATHER_URL;

    // OPEN API APP key
    private static final String authority = "938c1fa87cfd21620b41e35eb87a5277";
    // 섭씨, 화씨
    private static final String units = "metric";
    // 언어 설정 -> 한국어
    private static final String language = "kr";
    // 10개의 데이터 가져오기
    private static final int cnt = 20;

    // 각 파라미터 constance
    final static String QUERY_PARAM = "q";
    final static String UNITS_PARAM = "units";
    final static String CNT_PARAM = "cnt";
    final static String LANG_PARAM = "lang";
    final static String APP_ID = "appid";

    // 선언한 URL, parameter constance를 활용하여 필요한 URL 생성하기!
    public static URL buildUrl(String cityName) {
        Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, cityName)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(LANG_PARAM, language)
                .appendQueryParameter(CNT_PARAM, Integer.valueOf(cnt).toString())
                .appendQueryParameter(APP_ID, authority)
                .build();
        GGLogger.getInstance().D("cityName으로 만든 uri 확인  = " + builtUri);
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        return url;
    }

    // Http 통신하기 위한 method
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            GGLogger.getInstance().D(">>>> NetworkUtils  ::: " + url.toString());
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}