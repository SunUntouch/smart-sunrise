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

public class AlarmManage extends AppCompatActivity {

    private static final boolean DEBUG = false;

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
    private void setNewAlarm(boolean snooze){

        //create Alarm, PendingIntent inherit the actual Alarm ID
        createAlarmManager();
        PendingIntent pendingIntent = getPendingIntent();
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

            alarmTime = calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(conf.getTimeToNextDay());
            alarmTime = (alarmTime < currentTime) ? alarmTime + TimeUnit.DAYS.toMillis(1) : alarmTime;
        }
        else //Snooze
            alarmTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(conf.getSnooze()); //+ Snooze

        //Check if the complete start time is in the past, if so change light to better suiting values
        long lightStart = getTimeBeforeMusic();
        final long alarmTimeWithLight = alarmTime - lightStart;
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
                conf.setScreenStartTime((!snooze) ? (int)TimeUnit.MILLISECONDS.toMinutes(lightStart) : 0);

            if(lightStart < screenTime)
                conf.setLEDStartTime((!snooze) ? (int)TimeUnit.MILLISECONDS.toMinutes(lightStart) : 0);
            conf.commit();
        }
        else if(conf.hasAlarmmanagerValues()){
            conf.clearAlarmManagerFlag();
            conf.commit();
        }

        alarmTime -= lightStart ;

        //For Testing
        alarmTime = (DEBUG) ? System.currentTimeMillis() : alarmTime;

        //Check for SDK Version and Use different AlarmManager Functions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            //Newer API Level provides a Symbol when Alarm is active
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(alarmTime, pendingIntent);
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        }
        else
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);

        //Show Toast when Set
        if(checkPendingIntent())
        {
            //TODO More elegant solution
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(alarmTime);
            Toast.makeText(context, SimpleDateFormat.getDateTimeInstance().format(cal.getTime()), Toast.LENGTH_SHORT).show();
        }
    }
    public void setNewAlarm(){
        setNewAlarm(false);
    }
    public void snoozeAlarm(){
            setNewAlarm(true);
    }
    public void cancelAlarm(){

        //get Days till Next Alarm
        if(isAlarmRepeat())
            setNewAlarm();
        else
        {
            config.clearAlarmManagerFlag();
            config.setAlarm(false,false);
            config.commit();

            createAlarmManager();
            alarmManager.cancel(getPendingIntent());
            getPendingIntent().cancel();
        }
    }

    public void cancelAlarmwithButton(){

        createAlarmManager();
        if(checkPendingIntent())
        {
            alarmManager.cancel(getPendingIntent());
            getPendingIntent().cancel();
        }
    }

    //Getter
    private boolean isAlarmRepeat(){
        return getConfig().isDaySet();
    }
    private long getTimeAlarmStarts(int hours, int minutes){
        return TimeUnit.HOURS.toMinutes(hours) + minutes ;
    }

    private long getTimeBeforeMusic(){
        //Screen Max Minutes
        return (getBeforeScreenTime() >= getBeforeLEDTime()) ? getBeforeScreenTime() : getBeforeLEDTime();
    }
    private long getBeforeScreenTime(){
        return (getConfig().useScreen()) ? TimeUnit.MINUTES.toMillis(getConfig().getScreenStartTime()) : 0;
    }
    private long getBeforeLEDTime(){
        return (getConfig().useLED()) ? TimeUnit.MINUTES.toMillis(getConfig().getLEDStartTime()) : 0;
    }
}
