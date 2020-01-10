package com.example.live_library.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.live_library.KeepAliveAidl;
import com.example.live_library.KeepAliveRunning;
import com.example.live_library.R;
import com.example.live_library.config.KeepAliveConfig;
import com.example.live_library.config.NotificationUtils;
import com.example.live_library.config.RunMode;
import com.example.live_library.receiver.NotificationClickReceiver;
import com.example.live_library.receiver.OnePixelReceiver;
import com.example.live_library.utils.SPUtils;

import static com.example.live_library.KeepAliveManager.TAG;
import static com.example.live_library.config.KeepAliveConfig.RUN_MODE;
import static com.example.live_library.config.KeepAliveConfig.SP_NAME;

/**
 * 运行的本地服务
 */
public class LocalService extends Service {

    private OnePixelReceiver mOnePixelReceiver;
    private ScreenStateReceiver screenStateReceiver;
    private boolean isPause = true;//控制暂停
    private MediaPlayer mediaPlayer;
    private LocalBinder mBinder;
    private Handler handler;
    private KeepAliveRunning mKeepAliveRunning;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * 定义本地binder
     */
    private class LocalBinder extends KeepAliveAidl.Stub{

        @Override
        public void wakeup(String title, String description, int iconRes) throws RemoteException {
            shouldDefNotify();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: 本地服务启动成功");
        if (mBinder == null){
            mBinder = new LocalBinder();
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        isPause = pm.isScreenOn();
        if (handler == null) {
            handler = new Handler();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 播放无声音乐
        KeepAliveConfig.runMode = SPUtils.getInstance(getApplicationContext(), SP_NAME)
                .getInt(KeepAliveConfig.RUN_MODE);
        Log.d(TAG, "运行模式：" + KeepAliveConfig.runMode);

        // 设置音乐播放
        if (mediaPlayer == null && KeepAliveConfig.runMode == RunMode.HIGH_POWER_CONSUMPTION){
            mediaPlayer = MediaPlayer.create(this, R.raw.novioce);
            mediaPlayer.setVolume(0f, 0f);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.i(TAG, "循环播放音乐");
                    play();
                }
            });
            play();
        }

        // 像素保活
        if (mOnePixelReceiver == null) {
            mOnePixelReceiver = new OnePixelReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mOnePixelReceiver, intentFilter);

        // 屏幕点亮状态监听，用于单独控制音乐播放
        if (screenStateReceiver == null) {
            screenStateReceiver = new ScreenStateReceiver();
        }
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(KeepAliveConfig.SCREEN_ON);
        intentFilter2.addAction(KeepAliveConfig.SCREEN_OFF);
        registerReceiver(screenStateReceiver, intentFilter2);

        // 开启一个前台通知，用于提升服务进程优先级
        shouldDefNotify();
        // 绑定守护进程
        try {
            Intent intent3 = new Intent(this, RemoteService.class);
            this.bindService(intent3, connection, Context.BIND_ABOVE_CLIENT);
        } catch (Exception e) {
            Log.i("RemoteService--", e.getMessage());
        }
        // 隐藏服务通知
        try {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                startService(new Intent(this, HideForegroundService.class));
            }
        } catch (Exception e) {
            Log.i("HideForegroundService--", e.getMessage());
        }

        // 初始化,保活程序启动
        if (mKeepAliveRunning == null)
            mKeepAliveRunning = new KeepAliveRunning();
        mKeepAliveRunning.onRunning();
        return START_STICKY;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                if (mBinder != null && KeepAliveConfig.foregroundNotification != null) {
                    KeepAliveAidl guardAidl = KeepAliveAidl.Stub.asInterface(service);
                    guardAidl.wakeup(SPUtils.getInstance(getApplicationContext(), SP_NAME).getString(KeepAliveConfig.TITLE),
                            SPUtils.getInstance(getApplicationContext(), SP_NAME).getString(KeepAliveConfig.CONTENT),
                            SPUtils.getInstance(getApplicationContext(), SP_NAME).getInt(KeepAliveConfig.RES_ICON));
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Intent remoteService = new Intent(LocalService.this,
                    RemoteService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalService.this.startForegroundService(remoteService);
            } else {
                LocalService.this.startService(remoteService);
            }
            Intent intent = new Intent(LocalService.this, RemoteService.class);
            LocalService.this.bindService(intent, connection,
                    Context.BIND_ABOVE_CLIENT);
            PowerManager pm = (PowerManager) LocalService.this.getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            if (isScreenOn) {
                sendBroadcast(new Intent(KeepAliveConfig.SCREEN_ON));
            } else {
                sendBroadcast(new Intent(KeepAliveConfig.SCREEN_OFF));
            }
        }
    };

    /**
     * 播放音乐
     */
    private void play() {
        Log.i(TAG, "播放音乐");
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    /**
     * 停止音乐
     */
    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }


    private void shouldDefNotify() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            KeepAliveConfig.CONTENT = SPUtils.getInstance(getApplicationContext(), SP_NAME).getString(KeepAliveConfig.CONTENT);
            KeepAliveConfig.DEF_ICONS = SPUtils.getInstance(getApplicationContext(), SP_NAME).getInt(KeepAliveConfig.RES_ICON, R.drawable.ic_launcher);
            KeepAliveConfig.TITLE = SPUtils.getInstance(getApplicationContext(), SP_NAME).getString(KeepAliveConfig.TITLE);
            String title = SPUtils.getInstance(getApplicationContext(), SP_NAME).getString(KeepAliveConfig.TITLE);
            Log.d("JOB-->" + TAG, "KeepAliveConfig.CONTENT_" + KeepAliveConfig.CONTENT + "    " + KeepAliveConfig.TITLE + "  " + title);
            if (!TextUtils.isEmpty(KeepAliveConfig.TITLE) && !TextUtils.isEmpty(KeepAliveConfig.CONTENT)) {
                // 启用前台服务，提升优先级
                Intent intent2 = new Intent(getApplicationContext(), NotificationClickReceiver.class);
                intent2.setAction(NotificationClickReceiver.CLICK_NOTIFICATION);
                Notification notification = NotificationUtils.createNotification(LocalService.this, KeepAliveConfig.TITLE, KeepAliveConfig.CONTENT, KeepAliveConfig.DEF_ICONS, intent2);
                startForeground(KeepAliveConfig.FOREGROUND_NOTIFICATION_ID, notification);
                Log.d("JOB-->", TAG + "显示通知栏");
            }
        }
    }

    /**
     * ScreenStateReceiver
     * 屏幕状态监听广播
     */
    private class ScreenStateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(KeepAliveConfig.SCREEN_ON)){
                isPause = true;
                pause();
            }else if (intent.getAction().equals(KeepAliveConfig.SCREEN_OFF)){
                isPause = false;
                play();
            }
        }
    }

    @Override
    public void onDestroy() {
        unbindService(connection);
        unregisterReceiver(mOnePixelReceiver);
        unregisterReceiver(screenStateReceiver);

        if (mKeepAliveRunning != null){
            mKeepAliveRunning.onStop();
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
