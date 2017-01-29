package com.zhun.sununtouch.smart_sunrise;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;

/**
 * Created by Sunny on 02.10.2016.
 * Helper Class for setting and removing HandlerThreads
 */

class AlarmWorkerThread extends HandlerThread{

    private android.os.Handler mHandler;
    AlarmWorkerThread(String name) {
        super(name);
    }

    void postTask(Runnable task){
        mHandler.post(task);
    }
    void postDelayedTask(Runnable task, long millis){
        mHandler.postDelayed(task, millis);
    }

    void prepareHandler(){
        mHandler = new Handler(getLooper());
    }
    void removeCallBacks(@Nullable Object token){
        mHandler.removeCallbacksAndMessages(token);
    }
}
