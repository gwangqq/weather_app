package io.abx.myapplication.utilities;

import android.util.Log;

// 전체 Log를 확인하고 싶어 만든 Class
// Singleton 적용, static ->
public class GGLogger {

    // My debugging Log TAG
    private final String GG_LOG_TAG = "JAKE";

    // Singleton instance
    private static GGLogger _instance;
    public static GGLogger getInstance() {
        if (_instance != null) {
            return _instance;
        }
        _instance = new GGLogger();
        return _instance;
    }
    // 기본 각 Log method
    public void V(String message) {
        Log.v(GG_LOG_TAG, message);
    }

    public void D(String message) {
        Log.d(GG_LOG_TAG, message);
    }

    public void I(String message) {
        Log.i(GG_LOG_TAG, message);
    }

    public void W(String message) {
        Log.w(GG_LOG_TAG, message);
    }

    public void E(String message) {
        Log.e(GG_LOG_TAG, message);
    }
}
