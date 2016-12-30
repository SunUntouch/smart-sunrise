package com.zhun.sununtouch.smart_sunrise;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class AlarmManage extends AppCompatActivity {

    private final Context context;
    private AlarmManager alarmManager;
    private AlarmConfiguration config;

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
    private PendingIntent getPendingIntent(int alarmId){
        //Create new PendingIntent
        return PendingIntent.getBroadcast(context, alarmId, getIntent(alarmId), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    //Alarm Methods
    private void setNewAlarm(int ID, boolean snooze){

        //create Alarm, PendingIntent inherit the actual Alarm ID
        createAlarmManager();
        PendingIntent pendingIntent = getPendingIntent(ID);
        alarmManager.cancel(pendingIntent);

        //Get Days to next Alarm
        long alarmTime;
        if(!snooze)
        {
            AlarmConfiguration conf = getConfig();
            Calendar calendar = Calendar.getInstance();

            //Add all necessary values to alarm time
            calendar.set(Calendar.HOUR_OF_DAY, conf.getHour());
            calendar.set(Calendar.MINUTE     , conf.getMinute());
            alarmTime = calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(getDaysToNextAlarm());

            //Check if AlarmTime is smaller then the actual time, if so then set it for 1 day to the future
            final long currentTime = System.currentTimeMillis();
            alarmTime = (alarmTime < currentTime) ? alarmTime + TimeUnit.DAYS.toMillis(1) : alarmTime;

            //Check if the complete start time is in the past, if so change light to better suiting values
            long lightStart = getTimeBeforeMusic();
            final long alarmTimeWithLight = alarmTime - getTimeBeforeMusic();
            if(alarmTimeWithLight < currentTime)
            {
                //Calculate Times
                final long screenTime = getBeforeScreenTime();
                final long ledTime    = getBeforeLEDTime();

                //Add 20 Seconds to Delta Time
                final long deltaTime  = currentTime - alarmTimeWithLight + TimeUnit.SECONDS.toMillis(20);
                lightStart = (screenTime > ledTime)? screenTime - deltaTime : ledTime - deltaTime;

                //Safe newly set Values
                conf.setFromAlarmManager();
                if(lightStart < ledTime )
                    conf.setScreenStartTime((int)TimeUnit.MILLISECONDS.toMinutes(lightStart));

                if(lightStart < screenTime)
                    conf.setLEDStartTime((int)TimeUnit.MILLISECONDS.toMinutes(lightStart));
                conf.commit();
            }
            alarmTime -= lightStart ;
        }
        else{ //Snooze

            AlarmConfiguration conf = getConfig();
            final int snoozeTime = conf.getSnooze();
            alarmTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(snoozeTime); //+ Snooze

            conf.setFromAlarmManager();
            final int screenTime = conf.getScreenStartTime();
            conf.setScreenStartTime((screenTime < snoozeTime ) ? screenTime : 0 );

            final int ledTime = conf.getLEDStartTime();
            conf.setLEDStartTime((ledTime < snoozeTime) ? ledTime : 0 );
            conf.commit();
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
    public void setNewAlarm(int ID){
        setNewAlarm(ID, false);
    }
    public void snoozeAlarm(int ID){
            setNewAlarm(ID, true);
    }
    public void cancelAlarm(int ID){

        //get Days till Next Alarm
        if(getDaysToNextAlarm() > 0)
            setNewAlarm(ID);
        else
        {
            createAlarmManager();
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

    //Getter
    private boolean isAlarmRepeat(){
        return getConfig().isDaySet();
    }
    private long getTimeAlarmStarts(int hours, int minutes){
        return TimeUnit.HOURS.toMinutes(hours) + minutes ;
    }
    private int getDaysToNextAlarm(){

        //Load Calendar Instance and get Actual Day of the Week
        Calendar calendar = Calendar.getInstance();
        switch (calendar.get(Calendar.DAY_OF_WEEK))
        {
            case Calendar.MONDAY   : return  getConfig().getTimeToNextDay(0);
            case Calendar.TUESDAY  : return  getConfig().getTimeToNextDay(1);
            case Calendar.WEDNESDAY: return  getConfig().getTimeToNextDay(2);
            case Calendar.THURSDAY : return  getConfig().getTimeToNextDay(3);
            case Calendar.FRIDAY   : return  getConfig().getTimeToNextDay(4);
            case Calendar.SATURDAY : return  getConfig().getTimeToNextDay(5);
            case Calendar.SUNDAY   : return  getConfig().getTimeToNextDay(6);
            default: return 0;
        }
    }
    private long getTimeBeforeMusic(){
        //Screen
        //Max Minutes
        long screen = getBeforeScreenTime();
        long led = getBeforeLEDTime();

        return (screen >= led) ? screen : led;
    }
    private long getBeforeScreenTime(){
        return (getConfig().useScreen()) ? TimeUnit.MINUTES.toMillis(getConfig().getScreenStartTime()) : 0;
    }
    private long getBeforeLEDTime(){
        return (getConfig().useLED()) ? TimeUnit.MINUTES.toMillis(getConfig().getLEDStartTime()) : 0;
    }
}
