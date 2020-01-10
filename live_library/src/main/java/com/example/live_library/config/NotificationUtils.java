package com.example.live_library.config;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.live_library.R;

/**
 * 通知工具类
 */
public class NotificationUtils extends ContextWrapper {

    private String id;
    private String name;
    private Context mContext;
    private NotificationManager manager;
    private NotificationChannel channel;

    public NotificationUtils(Context context) {
        super(context);
        this.mContext = context;
        id = mContext.getPackageName();
        name = mContext.getPackageName();
    }

    /**
     * 创建通知 android O 以上创建channel
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotificationChannel() {
        if (channel == null) {
            channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
            // 设置channel的以下属性
            channel.enableVibration(false);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);
            getManager().createNotificationChannel(channel);
        }
    }

    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotificationChannel(String title, String content, int icon, Intent intent) {
        //PendingIntent.FLAG_UPDATE_CURRENT 这个类型才能传值
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (TextUtils.isEmpty(title)) {
            title = mContext.getApplicationInfo().name;
        }
        if (TextUtils.isEmpty(content)) {
            content = mContext.getApplicationInfo().name;
        }
        if (icon == 0) {
            icon = R.drawable.ic_launcher;
        }

        return new Notification.Builder(mContext, id)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
    }

    /**
     * android O以下的通知设置设置
     * @param title
     * @param content
     * @param icon
     * @param intent
     * @return
     */
    public NotificationCompat.Builder getNotification_25(String title, String content, int icon, Intent intent) {
        //PendingIntent.FLAG_UPDATE_CURRENT 这个类型才能传值
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(mContext, id)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setVibrate(new long[]{0})
                .setContentIntent(pendingIntent);
    }

    public static void sendNotification(@NonNull Context context, @NonNull String title, @NonNull String content, @NonNull int icon, @NonNull Intent intent) {
        NotificationUtils notificationUtils = new NotificationUtils(context);
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationUtils.createNotificationChannel();
            notification = notificationUtils.getNotificationChannel(title, content, icon, intent).build();
        } else {
            notification = notificationUtils.getNotification_25(title, content, icon, intent).build();
        }
        notificationUtils.getManager().notify(new java.util.Random().nextInt(10000), notification);
    }

    /**
     * 创建通知
     * @param context
     * @param title
     * @param content
     * @param icon
     * @param intent
     * @return
     */
    public static Notification createNotification(@NonNull Context context, @NonNull String title, @NonNull String content, @NonNull int icon, @NonNull Intent intent) {
        NotificationUtils notificationUtils = new NotificationUtils(context);
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationUtils.createNotificationChannel();
            notification = notificationUtils.getNotificationChannel(title, content, icon, intent).build();
        } else {
            notification = notificationUtils.getNotification_25(title, content, icon, intent).build();
        }
        return notification;
    }





}
