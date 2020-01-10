package com.example.live_library.onepx;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.example.live_library.receiver.OnePixelReceiver;

/**
 * 一像素保活页面
 *
 * 是否可以使用一像素，默认可以使用，只有在android p以下可以使用
 *
 */
public class OnePixelActivity extends Activity {

    public static final String FINISH_ACTIVITY = "finish_activity";
    //    public static final String SCREEN_ON = "screen_on";
//    public static final String SCREEN_OFF = "screen_Off";
    // 注册广播接受者   当屏幕开启结果成功结束一像素的activity
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设定一像素的activity
        Window window = getWindow();
        window.setGravity(Gravity.START| Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.width = 1;
        params.height = 1;
        window.setAttributes(params);

        // 动态注册广播
        // 在一像素activity里注册广播接受者
        // 接受到广播结束掉一像素
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(OnePixelActivity.FINISH_ACTIVITY)){
                    finish();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OnePixelActivity.FINISH_ACTIVITY);  // 结束activity
//        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
//        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(broadcastReceiver, intentFilter);

        checkScreenOn();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 屏幕亮屏息屏，如果应用进程还在的话,
        checkScreenOn();
    }

    @Override
    protected void onDestroy() {
        // 注销广播
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    /**
     * 检查屏幕是否息屏，亮屏
     */
    private void checkScreenOn() {
        PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        assert powerManager != null;
        boolean isScreenOn = powerManager.isScreenOn();
        // 如果亮屏，就销毁activity
        if (isScreenOn){
            finish();
        }
    }
}
