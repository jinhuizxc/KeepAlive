package com.example.live_library;

import android.util.Log;

import static com.example.live_library.KeepAliveManager.TAG;

public class KeepAliveRunning implements IKeepLiveRunning {
    @Override
    public void onRunning() {
        Log.e(TAG, "KeepAliveRunning onRunning: true");
    }

    @Override
    public void onStop() {
        Log.e(TAG, "KeepAliveRunning onStop: false");
    }
}
