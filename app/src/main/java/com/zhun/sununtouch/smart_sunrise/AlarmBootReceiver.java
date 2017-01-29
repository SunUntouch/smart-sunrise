package com.zhun.sununtouch.smart_sunrise;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Sunny on 02.01.2017.
 * Receives On Boot Intent to set Alarm New
 */

public class AlarmBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent != null && intent.getAction() != null && "android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
        {
            //This is the ON BOOT Intent
            AlarmConfiguration alarm = new AlarmConfiguration(context, intent.getExtras().getInt(AlarmConstants.ALARM_ID));
            if(alarm.isDaySet() || alarm.getAlarmOneShot())
                alarm.activateAlarm();
        }
    }
}
