package io.abx.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.igaworks.v2.core.AdBrixRm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.abx.myapplication.utilities.GGLogger;
import io.abx.myapplication.utilities.OpenWeatherJsonUtils;
import io.abx.myapplication.utilities.SunshineWeatherUtils;

// MainActivity WeatherList에 View를 재활용하기 위해 필요한 Class
// RecyclerView adapter 사용하기 위해 상속!!
public class ForecastAdapter extends
        RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    // MainActivity 에 필요한 데이터를 저장하기 위한 variable
    private String[] mWeatherData;

    // private static final String TAG = ForecastAdapter.class.getSimpleName();

    // list item을 클릭했을 때, 이를 처리하기 위한 clickHandler 선언
    // ForecastAdapter 사용하기 위해 반드시 생성자의 인자로 가져가야함!
    final private ForecastAdapterOnClickHandler mClickHandler;

    // interface로 ClickHandler 선언
    public interface ForecastAdapterOnClickHandler {
        void onClick(String weatherForDay);
    }

    // clickHandler 등록 -> Adapter는 반드시 clickHandler 가져야함!
    public ForecastAdapter(ForecastAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    // RecyclerView ViewHolder 재활용할 View 객체에 필요한 각각의 UI를 담아두기
    // 각 재활용된 View 클릭했을 때, 동작이 필요하므로 OnClickListener interface 사용!
    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        // view holder에 재활용할 UI
        public final TextView mDate;
        public final TextView mDescription;
        public final TextView mHighTemperature;
        public final TextView mLowTemperature;
        public final ImageView mWeatherImageView;

        // RecyclerViewAdapter 에 필요한 view를 res/layout 에서 xml파일로 불러와 바인딩!
        public ForecastAdapterViewHolder(View view) {
            super(view);
            mDate = (TextView) view.findViewById(R.id.date);
            mDescription = (TextView) view.findViewById(R.id.weather_description);
            mHighTemperature = (TextView) view.findViewById(R.id.high_temperature);
            mLowTemperature = (TextView) view.findViewById(R.id.low_temperature);
            mWeatherImageView = (ImageView) view.findViewById(R.id.weather_icon);
            // view 클릭 했을 때, 각 click에 필요한 동작에 필요하기 때문에 listener 등록!
            view.setOnClickListener(this);
        }

        // OnClickListener 사용하기 위해 overriding 해야하는 method
        // 클릭했을 때 실제 어떤 동작을 지시하는가
        @Override
        public void onClick(View v) {
            // RecyclerView 클릭 했을 시, 각 list 순서를 불러오기!
            int adapterPosition = getAdapterPosition();
            // 각 View 클릭 했을 때 필요한 정보를 가져오기
            String weatherForDay = mWeatherData[adapterPosition];
            GGLogger.getInstance().D("weatherForDay : " + weatherForDay);
            try {
                JSONObject jsonForDescription = new JSONObject(weatherForDay);
                // adbrix 기능 연동
                // 날씨 description을 property로 설정
                // 어떤 날씨에 사람들이 상세 날씨보기를 많이 보았는가 파악할 수 있는 event
                AdBrixRm.AttrModel weatherDescription = new AdBrixRm.AttrModel()
                        .setAttrs("weather_description", jsonForDescription.getJSONArray("weather")
                                .getJSONObject(0)
                                .getString("description"));
                GGLogger.getInstance().D("weather description 확인 : " + jsonForDescription.getJSONArray("weather")
                        .getJSONObject(0)
                        .getString("description"));
                AdBrixRm.event("weather_list_click_event", weatherDescription);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            GGLogger.getInstance().D("adbrix 연동 잘 되었는지 확인 : Weather_List_Click!!");
            // click handler에 클릭했던 view의 data 전달!
            mClickHandler.onClick(weatherForDay);
        }
    }

    // RecyclerView class 사용하기 위해 override 해야하는 method 1
    // ViewGroup -> 재활용할 view -> ViewGroup 담아서 전달
    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // View에 필요한 context를 통해 정보를 가져오기
        Context context = viewGroup.getContext();
        // RecyclerView 사용하기 위해 forecast.xml 파일 바인딩
        int layoutIdForListItem = R.layout.forecast_list_item;
        // Layout Inflater 활용하여 새로운
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        // 여러가지 뷰를
        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new ForecastAdapterViewHolder(view);
    }

    // 받아온 정보를 앞으로 재활용할 하나의 view 객체에 바인딩하는 과정
    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {
        String weatherForThisDay = mWeatherData[position];
        GGLogger.getInstance().D("ForecastAdapter에서 리스트 화면 뿌릴 개별 item data" + weatherForThisDay);
        // weather list 각 list에 들어갈 TextView, ImageView
        String date = null;
        String description = null;
        String highTemp = null;
        String lowTemp = null;

        // ImageView Icon을 가져오기 위해 weatherId 초기화
        int weatherId = 0;
        try {
            // 각 list에 들어갈 data를 JSONObject 형태로 받아냄
            JSONObject weatherDataForList = new JSONObject(weatherForThisDay);

            // JSONObject에서 key 값으로 필요한 정보를 받아오기!
            date = weatherDataForList.getString("dt_txt");

            // OPEN API에서 weather는 JSONObject 안에 JSONArray 형태로 저장되어 있음
            JSONArray array = weatherDataForList.getJSONArray("weather");
            description = array.getJSONObject(0).getString("description");

            // OPEN API에 Main이란 JSONObject에 저장된 값을 key를 활용해 최저, 최고 온도 값 가져오기
            highTemp = weatherDataForList.getJSONObject("main").getString("temp_max");
            lowTemp = weatherDataForList.getJSONObject("main").getString("temp_min");

            // OPENAPI에서 받아온 weatherid
            weatherId = array.getJSONObject(0).getInt("id");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // OPEN API를 통해 가져온 데이터를 각각의 TextView 화면에 보여주기
        forecastAdapterViewHolder.mDate.setText(date);
        forecastAdapterViewHolder.mDescription.setText(description);
        forecastAdapterViewHolder.mHighTemperature.setText(highTemp + "\u00B0" + "C");
        forecastAdapterViewHolder.mLowTemperature.setText(lowTemp + "\u00B0" + "C");

        // SunshineWeatherUtils -> weather id 활용하여 필요한 icon을 가져오기!
        forecastAdapterViewHolder.mWeatherImageView.setImageResource(SunshineWeatherUtils.getIconResourceForWeatherCondition(weatherId));
    }

    // RecyclerView 활용할 때, 반드시 필요한 method
    // 재활용할 view 몇개 만들어야 할지 data 개수를 반환하는 method
    @Override
    public int getItemCount() {
        if (null == mWeatherData) {
            return 0;
        }
        return mWeatherData.length;
    }

    // String[] 형태로 넘어온 weatherData -> 위에 선언한 mWeatherData
    public void setWeatherData(JSONArray weatherData) throws JSONException {
        // 로더에서 받아온 JSONArray -> 배열에 넣어서 처리해야함!
        GGLogger.getInstance().D("refresh button check ForecastAdapter 까지 넘어옴!! 그리고 넘어온 JSON 값 " + weatherData);
        mWeatherData = OpenWeatherJsonUtils.listDataParsing(weatherData);
    }
}