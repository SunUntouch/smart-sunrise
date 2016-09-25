package com.zhun.sununtouch.smart_sunrise;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class AlarmManage extends AppCompatActivity {

    private  AlarmManager alarmManager;
    private AlarmConfiguration config;
    private final Context context;

    AlarmManage(Context alarmContext, AlarmConfiguration alarmConfig){

        context = alarmContext;
        config  = alarmConfig;
        createAlarmManager();
    }

    private void createAlarmManager(){
        if(alarmManager == null)
            alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    }

    private PendingIntent getPendingIntent(int alarmId){
        //Create new PendingIntent
        return PendingIntent.getBroadcast(context, alarmId, getIntent(alarmId), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getSnoozePendingIntent(int alarmId){
        //Create new PendingIntent
        return PendingIntent.getBroadcast(context, alarmId, getSnoozeIntent(alarmId), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public boolean checkForPendingIntent(int alarmId){
        //Check if Intent exists
        return (PendingIntent.getBroadcast(context, alarmId, getIntent(alarmId), PendingIntent.FLAG_NO_CREATE) != null);
    }

    private Intent getIntent(int ID){

        //Fill Intent
        Bundle bundle = new Bundle();
        bundle.putInt(AlarmConstants.ALARM_ID, ID);
        return new Intent(context, AlarmReceiver.class).putExtras(bundle);
    }

    private Intent getSnoozeIntent(int ID){

        //Fill Intent
        Bundle bundle = new Bundle();
        bundle.putInt(AlarmConstants.ALARM_ID, ID);
        return new Intent(context, AlarmReceiver.class).putExtras(bundle);
    }

    public void setNewAlarm(int ID, boolean snooze){

        //create Alarm
        createAlarmManager();
        PendingIntent pendingIntent = getPendingIntent(ID);
        alarmManager.cancel(pendingIntent);

        //Get Days to next Alarm
        long alarmTime;
        AlarmConfiguration conf = getConfig();
        Calendar calendar = Calendar.getInstance();
        if(!snooze)
        {
            calendar.set(Calendar.HOUR_OF_DAY, conf.getHour());
            calendar.set(Calendar.MINUTE     , conf.getMinute());

            long lightStart    = TimeUnit.MINUTES.toMillis(getTimeBeforeMusic(conf));
            long timeToNextDay = TimeUnit.DAYS.toMillis(getDaysToNextAlarm());

            //Add all necessary values to alarm time
            alarmTime = calendar.getTimeInMillis() + timeToNextDay - lightStart;

            //Check if AlarmTime is smaller then the actual time, if so then set it for 1 day to the future
            alarmTime = (alarmTime < System.currentTimeMillis()) ? alarmTime + TimeUnit.DAYS.toMillis(1) : alarmTime;
        }
        else //Snooze
        {
            pendingIntent = getSnoozePendingIntent(ID);
            alarmTime = calendar.getTimeInMillis() + TimeUnit.MINUTES.toMillis(conf.getSnooze()); //+ Snooze
        }

        //Check for SDK Version and Use different AlarmManager Functions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            //Newer API Level provides a Symbol when Alarm is active
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(alarmTime, pendingIntent);
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        }
        else
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
    }

    public void snoozeAlarm(int ID){
        if(ID > -1)
            setNewAlarm(ID, true);
    }

    public  void cancelAlarm(int ID){

        createAlarmManager();

        //get Days till Next Alarm
        if(getDaysToNextAlarm() > 0)
            setNewAlarm(ID, false);
        else
        {
            alarmManager.cancel(getPendingIntent(ID));
            getPendingIntent(ID).cancel();
        }
    }

    public void cancelAlarmwithButton(int ID){

        createAlarmManager();
        if(checkForPendingIntent(ID))
        {
            alarmManager.cancel(getPendingIntent(ID));
            getPendingIntent(ID).cancel();
        }
    }

    private boolean isAlarmRepeat(){
        return getConfig().isDaySet();
    }

    private AlarmConfiguration getConfig(){
        return config;
    }
    private int getDaysToNextAlarm(){

        AlarmConfiguration conf = getConfig();

        //Load Calendar Instance and get Actual Day of the Week
        Calendar calendar = Calendar.getInstance();
        int currentDay    =(calendar.get(Calendar.DAY_OF_WEEK) - 1 < 6) ? calendar.get(Calendar.DAY_OF_WEEK) : 0;
        if(!conf.isDaySet())
            return 0;

        //get Days till Next Alarm
        int daysToNextAlarm = 1;
        while(!conf.isDaySet(currentDay))
        {
            ++daysToNextAlarm;
            if(conf.isDaySet(currentDay))
                break;
            //If Sunday is Arrived switch to Monday index
            currentDay = (currentDay < 6) ? ++currentDay : 0;
        }
        return daysToNextAlarm;
    }

    public long getTimeAlarmStarts(int hours, int minutes){
        return TimeUnit.HOURS.toMinutes(hours) + minutes ;
    }

    public int getTimeBeforeMusic(AlarmConfiguration conf){

        //Screen
        int minuteScreen = conf.useScreen() ? conf.getScreenStartTime() : 0;
        int minuteLED    = conf.useLED()    ? conf.getLEDStartTime()    : 0;

        //Max Minutes
        return (minuteScreen >= minuteLED) ? minuteScreen : minuteLED;
    }
}
