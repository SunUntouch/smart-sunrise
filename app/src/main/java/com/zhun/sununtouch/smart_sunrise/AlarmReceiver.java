package com.zhun.sununtouch.smart_sunrise;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by Sunny on 26.12.2015.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent alarmIntent = new Intent(context, AlarmIntentService.class);
        alarmIntent.putExtras(intent);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startWakefulService(context, alarmIntent);
    }
}