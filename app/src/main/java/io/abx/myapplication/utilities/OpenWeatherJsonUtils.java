package io.abx.myapplication.utilities;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// OPEN API -> JOSNString -> 필요한 데이터 String[] 형태로 반환해주기 위한 class
public final class OpenWeatherJsonUtils {

    private static final String TAG = OpenWeatherJsonUtils.class.getSimpleName();

    // 주간 날씨
    // OPEN API에서 받아온 JSON에서 weather(key)는 JSONArray 형식으로 데이터 저장
    public static JSONObject getSimpleCityWeatherStringsFromJson(String forecastJsonStr)
            throws JSONException {
        JSONObject parsedJSONObject = new JSONObject();
        // API 통해서 받아온 String -> JSONObject 사용하기 위해 변환
        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        // 시간대별 날씨 정보 담아놓은 JSONArray
        JSONArray listArray = forecastJson.getJSONArray("list");

        // city name, lat, lon 담아놓을 JSONObject
        JSONObject cityInfo = new JSONObject();
        // city name, lat, lon forecastJson에서 꺼내오기
        String cityName = forecastJson.getJSONObject("city").getString("name");
        String lat = Integer.valueOf(forecastJson.getJSONObject("city").getJSONObject("coord").getInt("lat")).toString();
        String lon = Integer.valueOf(forecastJson.getJSONObject("city").getJSONObject("coord").getInt("lon")).toString();
        cityInfo.put("name", cityName);
        cityInfo.put("lat", lat);
        cityInfo.put("lon", lon);

        // 시간대별 날씨와 city information을 하나의 JSONObject에 담아서 로더에서 처리!
        parsedJSONObject.put("listInfo", listArray);
        parsedJSONObject.put("cityInfo", cityInfo);
        GGLogger.getInstance().D("Json에서 key:list로 저장 된 array 값 가져와서 출력!");

        return parsedJSONObject;
    }

    public static String[] listDataParsing(JSONArray listData) throws JSONException {
        // forecast adapter에서 바인딩을 위해 JSONArray -> String[] 변환!
        int len = listData.length();
        String[] list = new String[len];
        for (int i = 0; i < len; i++) {
            list[i] = listData.getString(i);
        }
        return list;
    }
}