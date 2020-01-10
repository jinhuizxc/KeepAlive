package com.example.live_library.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.example.live_library.config.KeepAliveConfig;
import com.example.live_library.onepx.OnePixelActivity;
import com.example.live_library.utils.AppUtils;

public class OnePixelReceiver extends BroadcastReceiver {

    Handler mHandler;
    boolean isAppForeground = false;

    public OnePixelReceiver() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {   // 屏幕打开的时候发送广播  结束一像素
            // 销毁页面
            context.sendBroadcast(new Intent(OnePixelActivity.FINISH_ACTIVITY));
            if (!isAppForeground){
                isAppForeground = false;
            }
            // 启动应用home页面？
            try {
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                home.addCategory(Intent.CATEGORY_HOME);
                context.getApplicationContext().startActivity(home);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //通知屏幕已点亮，停止播放无声音乐
            context.sendBroadcast(new Intent(KeepAliveConfig.SCREEN_ON));
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {   // 屏幕关闭的时候接受到广播
            isAppForeground = AppUtils.IsForeground(context);  // false
            // 启动一像素activity
            try {
                Intent intent1 = new Intent(context, OnePixelActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 通知屏幕已关闭，开始播放无声音乐
            context.sendBroadcast(new Intent(KeepAliveConfig.SCREEN_OFF));

        }
    }
}
