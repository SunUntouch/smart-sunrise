package com.zhun.sununtouch.smart_sunrise;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sunny
 * Starts and Cancels Alarms, manages Intenets and so on
 */

public /*abstract*/ class AlarmManage extends AppCompatActivity {

    private final Context context;
    private AlarmManager alarmManager;
    private final AlarmConfiguration config;

    AlarmManage(Context alarmContext, AlarmConfiguration alarmConfig){
        context = alarmContext;
        config  = alarmConfig;
        createAlarmManager();
    }
    private void createAlarmManager(){
        if(alarmManager == null)
            alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    }
    private AlarmConfiguration getConfig(){
        return config;
    }

    //Intents
    private Intent getAlarmIntent(){
        //Fill Intent
        Bundle bundle = new Bundle();
        bundle.putInt(AlarmConstants.ALARM_ID, getConfig().getAlarmID());
        return new Intent(context, AlarmReceiver.class).putExtras(bundle);
    }
    private PendingIntent getPendingIntent(){
        //Create new PendingIntent
        return PendingIntent.getBroadcast(context, getConfig().getAlarmID(), getAlarmIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
    }
    public boolean checkPendingIntent(){
        //Check if Intent exists
        return (PendingIntent.getBroadcast(context, getConfig().getAlarmID(), getAlarmIntent(), PendingIntent.FLAG_NO_CREATE) != null);
    }

    //Alarm Methods
    public boolean setAlarm(boolean snooze){

        //create Alarm, PendingIntent inherit the actual Alarm ID
        createAlarmManager();
        final PendingIntent pendingIntent = getPendingIntent();
        alarmManager.cancel(pendingIntent);

        AlarmConfiguration conf = getConfig();

        //Get Days to next Alarm and Check if AlarmTime is smaller then the actual time, if so then set it for 1 day to the future
        long alarmTime;
        final long currentTime = System.currentTimeMillis();
        if(!snooze)
        {
            //Add all necessary values to alarm time
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, conf.getHour());
            calendar.set(Calendar.MINUTE     , conf.getMinute());
            calendar.set(Calendar.SECOND     , 0);

            alarmTime = calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(conf.getTimeToNextDay());
            alarmTime = (alarmTime < currentTime) ? alarmTime + TimeUnit.DAYS.toMillis(1) : alarmTime;
        }
        else //Snooze
            alarmTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(conf.getSnooze()); //+ Snooze

        //Check if the complete start time is in the past, if so change light to better suiting values
        long timeBeforeMusic = (getBeforeScreenTime() >= getBeforeLEDTime()) ? getBeforeScreenTime() : getBeforeLEDTime();

        final long alarmTimeWithLight = alarmTime - timeBeforeMusic;
        boolean changed = false;
        if(alarmTimeWithLight < currentTime)
        {
            //Calculate Times
            final long screenTime = getBeforeScreenTime();
            final long ledTime    = getBeforeLEDTime();

            //Add 20 Seconds to Delta Time
            final long deltaTime  = currentTime - alarmTimeWithLight + TimeUnit.SECONDS.toMillis(20);
            timeBeforeMusic = (screenTime > ledTime)? screenTime - deltaTime : ledTime - deltaTime;

            //Safe newly set Values
            if(timeBeforeMusic < screenTime )
            {
                changed = true;
                conf.setTemporaryTimes(true);
                conf.setScreenStartTemp((!snooze) ? (int)TimeUnit.MILLISECONDS.toMinutes(timeBeforeMusic) : 0);
            }

            if(timeBeforeMusic < ledTime)
            {
                changed = true;
                conf.setTemporaryTimes(true);
                conf.setLEDStartTemp((!snooze) ? (int)TimeUnit.MILLISECONDS.toMinutes(timeBeforeMusic) : 0);
            }
        }

        if(!conf.isDaySet())
        {
            changed = true;
            conf.setAlarmOneShot(true);
        }

        if(changed)
            conf.commit();

        //Check for SDK Version and Use different AlarmManager Functions
        alarmTime = alarmTime - timeBeforeMusic;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            //Newer API Level provides a Symbol when Alarm is active
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(alarmTime, pendingIntent);
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        }
        else
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);

        //Show Toast when Set
        final boolean checked = checkPendingIntent();
        if(checked)
        {
            //TODO More elegant solution then a toast
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(alarmTime);
            Toast.makeText(context, SimpleDateFormat.getDateTimeInstance().format(cal.getTime()), Toast.LENGTH_SHORT).show();
        }
        return checked;
    }
    public boolean cancelAlarm(){

        //get Days till Next Alarm
        createAlarmManager();
        final PendingIntent intent = getPendingIntent();
        alarmManager.cancel(intent);
        intent.cancel();
        return checkPendingIntent();
    }
    public void refresh(){
        cancelAlarm();
        setAlarm(false);
    }

    //Getter
    private long getBeforeScreenTime(){
        return (getConfig().getScreen()) ? TimeUnit.MINUTES.toMillis(getConfig().getScreenStartTime()) : 0;
    }
    private long getBeforeLEDTime(){
        return (getConfig().getLED()) ? TimeUnit.MINUTES.toMillis(getConfig().getLEDStartTime()) : 0;
    }
}
