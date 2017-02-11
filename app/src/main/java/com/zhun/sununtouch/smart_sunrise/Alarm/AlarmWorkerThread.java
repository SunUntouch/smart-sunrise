package com.zhun.sununtouch.smart_sunrise.Alarm;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;

/**
 * Created by Sunny on 02.10.2016.
 * Helper Class for setting and removing HandlerThreads
 */

public class AlarmWorkerThread extends HandlerThread{

    private android.os.Handler mHandler;
    public AlarmWorkerThread(String name) {
        super(name);
    }

    public void postTask(Runnable task){
        mHandler.post(task);
    }
    public void postDelayedTask(Runnable task, long millis){
        mHandler.postDelayed(task, millis);
    }

    public void prepareHandler(){
        mHandler = new Handler(getLooper());
    }
    public void removeCallBacks(@Nullable Object token){
        mHandler.removeCallbacksAndMessages(token);
    }
}
