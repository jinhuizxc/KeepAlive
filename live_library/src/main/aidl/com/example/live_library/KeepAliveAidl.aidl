// KeepAliveAidl.aidl
package com.example.live_library;

// Declare any non-default types here with import statements

interface KeepAliveAidl {
    //相互唤醒服务
    void wakeup(String title, String description, int iconRes);
}
