package com.example.live_library.service;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.live_library.KeepAliveAidl;
import com.example.live_library.R;
import com.example.live_library.config.KeepAliveConfig;
import com.example.live_library.config.NotificationUtils;
import com.example.live_library.receiver.NotificationClickReceiver;
import com.example.live_library.utils.SPUtils;

import static com.example.live_library.KeepAliveManager.TAG;
import static com.example.live_library.config.KeepAliveConfig.SP_NAME;

/**
 * 守护进程
 */
public class RemoteService extends Service {

    private RemoteBinder mBinder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "RemoteService onCreate");
        if (mBinder == null) {
            mBinder = new RemoteBinder();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 绑定服务
        try {
            this.bindService(new Intent(RemoteService.this, LocalService.class),
                    connection, Context.BIND_ABOVE_CLIENT);
            shouDefNotify();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return START_STICKY;
    }

    /**
     * 创建前台通知服务
     */
    private void shouDefNotify() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            KeepAliveConfig.CONTENT = SPUtils.getInstance(getApplicationContext(), SP_NAME).getString(KeepAliveConfig.CONTENT);
            KeepAliveConfig.DEF_ICONS = SPUtils.getInstance(getApplicationContext(), SP_NAME).getInt(KeepAliveConfig.RES_ICON, R.drawable.ic_launcher);
            KeepAliveConfig.TITLE = SPUtils.getInstance(getApplicationContext(), SP_NAME).getString(KeepAliveConfig.TITLE);
            String title = SPUtils.getInstance(getApplicationContext(), SP_NAME).getString(KeepAliveConfig.TITLE);
            Log.d("JOB-->" + TAG, "KeepAliveConfig.CONTENT_" + KeepAliveConfig.CONTENT + "    " + KeepAliveConfig.TITLE + "  " + title);
            if (!TextUtils.isEmpty(KeepAliveConfig.TITLE) && !TextUtils.isEmpty(KeepAliveConfig.CONTENT)) {
                //启用前台服务，提升优先级
                Intent intent2 = new Intent(getApplicationContext(), NotificationClickReceiver.class);
                intent2.setAction(NotificationClickReceiver.CLICK_NOTIFICATION);
                Notification notification = NotificationUtils.createNotification(RemoteService.this, KeepAliveConfig.TITLE, KeepAliveConfig.CONTENT, KeepAliveConfig.DEF_ICONS, intent2);
                startForeground(KeepAliveConfig.FOREGROUND_NOTIFICATION_ID, notification);
                Log.d("JOB-->", TAG + "显示通知栏");
            }
        }
    }

    @Override
    public void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            shouDefNotify();
        }

        // 断开连接，启动服务, 绑定服务
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Intent remoteService = new Intent(RemoteService.this,
                    LocalService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                RemoteService.this.startForegroundService(remoteService);
            } else {
                RemoteService.this.startService(remoteService);
            }
            RemoteService.this.bindService(new Intent(RemoteService.this,
                    LocalService.class), connection, Context.BIND_ABOVE_CLIENT);
            PowerManager pm = (PowerManager) RemoteService.this.getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            if (isScreenOn) {
                sendBroadcast(new Intent("_ACTION_SCREEN_ON"));
            } else {
                sendBroadcast(new Intent("_ACTION_SCREEN_OFF"));
            }
        }
    };


    private class RemoteBinder extends KeepAliveAidl.Stub {
        @Override
        public void wakeup(String title, String description, int iconRes) throws RemoteException {
            Log.i(TAG, " wakeUp");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (title != null || description != null) {
                    KeepAliveConfig.CONTENT = title;
                    KeepAliveConfig.DEF_ICONS = iconRes;
                    KeepAliveConfig.TITLE = description;
                } else {
                    KeepAliveConfig.CONTENT = SPUtils.getInstance(getApplicationContext(), SP_NAME)
                            .getString(KeepAliveConfig.CONTENT);
                    KeepAliveConfig.DEF_ICONS = SPUtils.getInstance(getApplicationContext(), SP_NAME)
                            .getInt(KeepAliveConfig.RES_ICON, R.drawable.ic_launcher);
                    KeepAliveConfig.TITLE = SPUtils.getInstance(getApplicationContext(), SP_NAME)
                            .getString(KeepAliveConfig.TITLE);

                }
                if (KeepAliveConfig.TITLE != null && KeepAliveConfig.CONTENT != null) {
                    //启用前台服务，提升优先级
                    Intent intent2 = new Intent(getApplicationContext(), NotificationClickReceiver.class);
                    intent2.setAction(NotificationClickReceiver.CLICK_NOTIFICATION);
                    Notification notification = NotificationUtils.createNotification(RemoteService.this, KeepAliveConfig.TITLE, KeepAliveConfig.CONTENT, KeepAliveConfig.DEF_ICONS, intent2);
                    startForeground(KeepAliveConfig.FOREGROUND_NOTIFICATION_ID, notification);
                    Log.d("JOB-->", TAG + "2 显示通知栏");
                }
            }
        }
    }
}
