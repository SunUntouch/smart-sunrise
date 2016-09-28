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
            //Add all necessary values to alarm time
            long lightStart    = TimeUnit.MINUTES.toMillis(getTimeBeforeMusic());
            long timeToNextDay = TimeUnit.DAYS.toMillis(getDaysToNextAlarm());

            calendar.set(Calendar.HOUR_OF_DAY, conf.getHour());
            calendar.set(Calendar.MINUTE     , conf.getMinute());
            alarmTime = calendar.getTimeInMillis() + timeToNextDay - lightStart; //TODO What if the user sets a Alarm and the light time is lower then the current time?

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

    public long getTimeAlarmStarts(int hours, int minutes){
        return TimeUnit.HOURS.toMinutes(hours) + minutes ;
    }

    public int getTimeBeforeMusic(){
        //Screen
        int minuteScreen = getConfig().useScreen() ? getConfig().getScreenStartTime() : 0;
        int minuteLED    = getConfig().useLED()    ? getConfig().getLEDStartTime()    : 0;

        //Max Minutes
        return (minuteScreen >= minuteLED) ? minuteScreen : minuteLED;
    }
}
