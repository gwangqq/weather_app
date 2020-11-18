package io.abx.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;


import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import io.abx.myapplication.utilities.GGLogger;


public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {


    // preference 생성!!
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // pref_general.xml -> preference 화면으로 binding
        addPreferencesFromResource(R.xml.pref_general);

        // SharePreferences 생성
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();

        // PreferenceScreen 생성!
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        // 설정 화면이 하나밖에 없으므로 index = 0

        Preference preference = preferenceScreen.getPreference(0);
        // sharedPreferences 에서 key 값으로 value 가져오기!
//        String value = sharedPreferences.getString(preference.getKey(), "");
        String value;
        value = SettingsActivity.userName;
        if (value==null){
            value=sharedPreferences.getString(preference.getKey(), "");
        }
        // summary 가져온 값으로 설정하기!
        setPreferenceSummary(preference, value);
        GGLogger.getInstance().D("settings fragment test1 : " + value);

    }




    // OnSharedPreferenceChangeListener Preference 시작할 때 등록!!
    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    // OnSharedPreferenceChangeListener Preference 종료할 때 등록 취소(해지?)!!
    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    // SharedPreference 변경됐을 때 호출 되는 method
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // 받아온 key로 sharedpreferences 에서 preference 꺼내오기!
        Preference preference = findPreference(key);
        if (null != preference) {
            setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
        }
    }

    // preference 값을 Summary로 보여주기
    private void setPreferenceSummary(Preference preference, Object value) {
        // preference 이름 String으로 받아오기
        String preferenceValue = value.toString();
        // preference에 Summary 설정하기
        preference.setSummary(preferenceValue);
    }


}