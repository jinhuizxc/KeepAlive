package com.example.live_library;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.live_library.config.ForegroundNotification;
import com.example.live_library.config.KeepAliveConfig;
import com.example.live_library.config.RunMode;
import com.example.live_library.service.JobHandlerService;
import com.example.live_library.service.LocalService;
import com.example.live_library.service.RemoteService;
import com.example.live_library.utils.AppUtils;
import com.example.live_library.utils.SPUtils;

import static com.example.live_library.config.KeepAliveConfig.SP_NAME;

/**
 * 进程保活管理
 */
public class KeepAliveManager {

    public static final String TAG = "KeepAlive";


    /**
     * 启动保活
     *
     * @param application            your application
     * @param runMode
     * @param foregroundNotification 前台服务
     */
    public static void toKeepAlive(@NonNull Application application, @NonNull int runMode, String title,
                                   String content, int res_icon, ForegroundNotification foregroundNotification) {
        if (AppUtils.isRunning(application)) {
            KeepAliveConfig.foregroundNotification = foregroundNotification;
            SPUtils.getInstance(application, SP_NAME).putString(KeepAliveConfig.TITLE, title);
            SPUtils.getInstance(application, SP_NAME).putString(KeepAliveConfig.CONTENT, content);
            SPUtils.getInstance(application, SP_NAME).putInt(KeepAliveConfig.RES_ICON, res_icon);
            SPUtils.getInstance(application, SP_NAME).putInt(KeepAliveConfig.RUN_MODE, runMode);
            // 优化后的枚举
            RunMode.setShape(runMode);
            KeepAliveConfig.runMode = RunMode.getShape();
            // api 21
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //启动定时器，在定时器中启动本地服务和守护进程
                JobHandlerService.startJob(application);
            } else {
                Intent localIntent = new Intent(application, LocalService.class);
                //启动守护进程
                Intent guardIntent = new Intent(application, RemoteService.class);
                if (Build.VERSION.SDK_INT >= 26) {
                    application.startForegroundService(localIntent);
                    application.startForegroundService(guardIntent);
                } else {
                    application.startService(localIntent);
                    application.startService(guardIntent);
                }
            }
        }
    }

    public static void stopWork(Application application) {
        try {
            KeepAliveConfig.foregroundNotification = null;
            KeepAliveConfig.keepLiveService = null;
            KeepAliveConfig.runMode = RunMode.getShape();
            JobHandlerService.stopJob();
            //启动本地服务
            Intent localIntent = new Intent(application, LocalService.class);
            //启动守护进程
            Intent guardIntent = new Intent(application, RemoteService.class);
            application.stopService(localIntent);
            application.stopService(guardIntent);
            application.stopService(new Intent(application, JobHandlerService.class));
        } catch (Exception e) {
            Log.e(TAG, "stopWork-->" + e.getMessage());
        }
    }

}