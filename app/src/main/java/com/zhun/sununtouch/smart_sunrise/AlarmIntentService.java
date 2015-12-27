package com.zhun.sununtouch.smart_sunrise;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Sunny on 27.12.2015.
 */
public class AlarmIntentService extends IntentService {

    public AlarmIntentService(){
        super("AlarmIntentService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {

        Intent alarmIntent = new Intent(this, AlarmActivity.class);
        alarmIntent.putExtras(intent);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(alarmIntent);

        AlarmReceiver.completeWakefulIntent(intent);
    }
}
