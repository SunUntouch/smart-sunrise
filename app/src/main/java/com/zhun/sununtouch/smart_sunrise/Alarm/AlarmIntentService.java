package com.zhun.sununtouch.smart_sunrise.Alarm;

import android.app.IntentService;
import android.content.Intent;

import com.zhun.sununtouch.smart_sunrise.AlarmActivity;
import com.zhun.sununtouch.smart_sunrise.Configuration.AlarmLogging;
import com.zhun.sununtouch.smart_sunrise.R;

/**
 * Created by Sunny
 * Receives Alarm Intent and Starts Alarm Activity
 */

public class AlarmIntentService extends IntentService {

    public AlarmIntentService() {
        super("AlarmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //this is the alarm Intent
        startActivity(new Intent(this, AlarmActivity.class)
                .putExtras(intent)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
        AlarmReceiver.completeWakefulIntent(intent);

        AlarmLogging log = new AlarmLogging(this);
        log.i("AlarmIntentService", getString(R.string.logging_alarm_intent));
    }
}
