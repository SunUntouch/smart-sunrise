package com.zhun.sununtouch.smart_sunrise;

import android.app.IntentService;
import android.content.Intent;

public class AlarmIntentService extends IntentService {

    public AlarmIntentService(){
        super("AlarmIntentService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {

        if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
        {
            //This is the ON BOOT Intent
            AlarmConfiguration alarm = new AlarmConfiguration(getApplicationContext(), intent.getExtras().getInt(AlarmConstants.ALARM_ID));
            if(alarm.isDaySet() || alarm.getAlarmOneShot())
                alarm.activateAlarm();
        }
        else if(intent.getAction() == null)
        {
            //this is the alarm Intent
            Intent alarmIntent = new Intent(this, AlarmActivity.class);
            alarmIntent.putExtras(intent);
            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(alarmIntent);
            AlarmReceiver.completeWakefulIntent(intent);
        }
    }
}
