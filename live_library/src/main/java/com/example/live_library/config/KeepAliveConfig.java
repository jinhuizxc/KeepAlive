package com.example.live_library.config;

import com.example.live_library.R;

/**
 * 进程保活需要配置的属性
 */
public class KeepAliveConfig {

    /**
     * Job 的时间
     */
    public static final int JOB_TIME = 1000;
    // 前台通知
    public static final int FOREGROUND_NOTIFICATION_ID = 8888;
    /**
     * 运行模式
     */
    public static final String RUN_MODE = "RUN_MODE";
    /**
     * 进程开启的广播
     */
    public static final String PROCESS_ALIVE_ACTION = "PROCESS_ALIVE_ACTION";
    public static final String PROCESS_STOP_ACTION = "PROCESS_STOP_ACTION";
    public static ForegroundNotification foregroundNotification = null;
    public static KeepLiveService keepLiveService = null;
    public static int runMode = RunMode.getShape();

    /**
     * 广播通知的 action
     */
    public static String NOTIFICATION_ACTION = "NOTIFICATION_ACTION";

    public static String TITLE = "TITLE";
    public static String CONTENT = "CONTENT";
    public static String RES_ICON = "RES_ICON";
    public static int DEF_ICONS = R.drawable.ic_launcher;
    public static String SP_NAME = "KeepAliveConfig";

    // 停止、播放音乐
    public static final String SCREEN_ON = "SCREEN_ON";
    public static final String SCREEN_OFF = "SCREEN_OFF";


}
