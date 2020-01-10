package com.example.live_library;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.live_library.config.ForegroundNotification;
import com.example.live_library.config.ForegroundNotificationClickListener;

import static com.example.live_library.config.RunMode.HIGH_POWER_CONSUMPTION;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startKeepAlive(View view) {
        start();
    }

    private void start() {
        //启动保活服务
        KeepAliveManager.toKeepAlive(
                getApplication()
                , HIGH_POWER_CONSUMPTION,
                "进程保活",
                "Process: System(哥们儿) 我不想被杀死",
                R.mipmap.ic_launcher,
                new ForegroundNotification(
                        //定义前台服务的通知点击事件
                        new ForegroundNotificationClickListener() {
                            @Override
                            public void foregroundNotificationClick(Context context, Intent intent) {
                                Log.d("JOB-->", " foregroundNotificationClick");
                            }
                        })
        );

    }

    public void stopKeepAlive(View view) {
        KeepAliveManager.stopWork(getApplication());
    }
}
