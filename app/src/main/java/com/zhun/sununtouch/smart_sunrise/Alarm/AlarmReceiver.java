package com.zhun.sununtouch.smart_sunrise.Alarm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.zhun.sununtouch.smart_sunrise.Configuration.AlarmLogging;
import com.zhun.sununtouch.smart_sunrise.R;

/**
 * Created by Sunny on 26.12.2015.
 * Receives Alarm Intent when Alarm Time is Current Time
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        startWakefulService(
                context,
                new Intent(context, AlarmIntentService.class)
                        .putExtras(intent)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );
        AlarmLogging log = new AlarmLogging(context);
        log.i("AlarmReceiver", context.getString(R.string.logging_alarm_receiver));
    }
}
