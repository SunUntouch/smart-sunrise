package com.zhun.sununtouch.smart_sunrise.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zhun.sununtouch.smart_sunrise.Configuration.AlarmConfigurationList;
import com.zhun.sununtouch.smart_sunrise.Configuration.AlarmLogging;
import com.zhun.sununtouch.smart_sunrise.R;

/**
 * Created by Sunny on 02.01.2017.
 * Receives On Boot Intent to set Alarm New
 */

public class AlarmBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null && intent.getAction() != null && "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            //This is the ON BOOT Intent
            AlarmConfigurationList alarms = new AlarmConfigurationList(context, true);
            if (alarms.isAlarmSet()) {
                alarms.restartAlarm();
            }

            AlarmLogging log = new AlarmLogging(context);
            log.i("AlarmBootReceiver", context.getString(R.string.logging_alarm_boot));
        }
    }
}
