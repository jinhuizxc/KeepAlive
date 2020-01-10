package com.example.live_library.service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.live_library.config.KeepAliveConfig;
import com.example.live_library.utils.AppUtils;

import static com.example.live_library.KeepAliveManager.TAG;

/**
 * 定时器
 * 安卓5.0及以上
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobHandlerService extends JobService {

    private static int EXECUTE_COUNT = 0;

    private static JobScheduler jobScheduler;

    @Override
    public boolean onStartJob(JobParameters params) {
        try {
            ++EXECUTE_COUNT;
            Log.d("JOB-->", " Job 执行 " + EXECUTE_COUNT);
            //7.0以上轮询
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                startJob(this);
            }
            if (!AppUtils.isServiceRunning(getApplicationContext(), getPackageName() + ":local") ||
                    !AppUtils.isRunningTaskExist(getApplicationContext(), getPackageName() + ":remote")) {
                Log.d("JOB-->", " 重新开启了 服务 ");
                startService(this);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job onStopJob");
        if (!AppUtils.isServiceRunning(getApplicationContext(), getPackageName() + ":local")
                || !AppUtils.isRunningTaskExist(getApplicationContext(), getPackageName() + ":remote")) {
            startService(this);
        }
        return false;
    }

    private void startService(Context context) {
        try {
            Log.i(TAG, "---》启动双进程保活服务");
            //启动本地服务
            Intent localIntent = new Intent(context, LocalService.class);
            //启动守护进程
            Intent guardIntent = new Intent(context, RemoteService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(localIntent);
                startForegroundService(guardIntent);
            } else {
                startService(localIntent);
                startService(guardIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void startJob(Context context) {
        try {
            jobScheduler = (JobScheduler) context.getSystemService(
                    Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = new JobInfo.Builder(10,
                    new ComponentName(context.getPackageName(),
                            JobHandlerService.class.getName())).setPersisted(true);
            /**
             * I was having this problem and after review some blogs and the official documentation,
             * I realised that JobScheduler is having difference behavior on Android N(24 and 25).
             * JobScheduler works with a minimum periodic of 15 mins.
             *
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //7.0以上延迟1s执行
                builder.setMinimumLatency(KeepAliveConfig.JOB_TIME);
            } else {
                //每隔1s执行一次job
                builder.setPeriodic(KeepAliveConfig.JOB_TIME);
            }
            jobScheduler.schedule(builder.build());

        } catch (Exception e) {
            Log.e(TAG, "startJob: " + e.getMessage());
        }
    }


    /**
     * 停止job
     */
    public static void stopJob() {
        if (jobScheduler != null)
            jobScheduler.cancelAll();
    }

}
