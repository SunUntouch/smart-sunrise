package com.zhun.sununtouch.smart_sunrise;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Sunny
 * Receives Alarm Intent and Starts Alarm Activity
 */

public class AlarmIntentService extends IntentService {

    public AlarmIntentService(){
        super("AlarmIntentService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        //this is the alarm Intent
        startActivity(new Intent(this, AlarmActivity.class)
                                .putExtras(intent)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        AlarmReceiver.completeWakefulIntent(intent);
    }
}
