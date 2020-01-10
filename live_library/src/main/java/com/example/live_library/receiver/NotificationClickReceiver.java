package com.example.live_library.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.live_library.config.KeepAliveConfig;

/**
 * 通知的点击监听广播
 */
public class NotificationClickReceiver extends BroadcastReceiver {

    public final static String CLICK_NOTIFICATION = "click_notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(NotificationClickReceiver.CLICK_NOTIFICATION)){
            if (KeepAliveConfig.foregroundNotification != null){
                if (KeepAliveConfig.foregroundNotification
                        .getForegroundNotificationClickListener() != null){
                    KeepAliveConfig.foregroundNotification.getForegroundNotificationClickListener()
                            .foregroundNotificationClick(context, intent);
                }
            }
        }
    }
}
