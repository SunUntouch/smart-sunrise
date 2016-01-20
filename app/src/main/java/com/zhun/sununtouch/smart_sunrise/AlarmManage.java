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
import java.util.concurrent.TimeUnit;

public class AlarmManage extends AppCompatActivity {

    private AlarmManager  alarmManager;
    private final Context context;

    AlarmManage(Context _context){

        context = _context;
        createAlarmManager();
    }

    private void createAlarmManager(){
        if(alarmManager == null)
            alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    }

    private PendingIntent getPendingIntent(int _alarmId){
        //Create new PendingIntent
        return PendingIntent.getBroadcast(context, _alarmId, getIntent(_alarmId), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getSnoozePendingIntent(int _alarmId){
        //Create new PendingIntent
        return PendingIntent.getBroadcast(context, _alarmId, getSnoozeIntent(_alarmId), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public boolean checkForPendingIntent(int _alarmId){
        //Check if Intent exists
        return (PendingIntent.getBroadcast(context, _alarmId, getIntent(_alarmId), PendingIntent.FLAG_NO_CREATE) != null);
    }

    private Intent getIntent(int _alarmId){
        //sharedPrefereces
        String settingsName = AlarmConstants.WAKEUP_TIMER + _alarmId;
        SharedPreferences settings = context.getSharedPreferences(settingsName, Context.MODE_PRIVATE);

        Bundle bundle = new Bundle();
        //Putting Value for each child
        int[] time  = {
                settings.getInt(AlarmConstants.ALARM_TIME_HOUR   , AlarmConstants.ACTUAL_TIME_HOUR),
                settings.getInt(AlarmConstants.ALARM_TIME_MINUTES, AlarmConstants.ACTUAL_TIME_MINUTE),
                settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE , AlarmConstants.ACTUAL_TIME_SNOOZE)};    // hour, minute, snooze

        bundle.putIntArray(AlarmConstants.WAKEUP_TIME, time);

        int[] days  = {
                settings.getInt(AlarmConstants.ALARM_DAY_MONDAY   , AlarmConstants.ACTUAL_DAY_MONDAY),
                settings.getInt(AlarmConstants.ALARM_DAY_TUESDAY  , AlarmConstants.ACTUAL_DAY_TUESDAY),
                settings.getInt(AlarmConstants.ALARM_DAY_WEDNESDAY, AlarmConstants.ACTUAL_DAY_WEDNESDAY),
                settings.getInt(AlarmConstants.ALARM_DAY_THURSDAY , AlarmConstants.ACTUAL_DAY_THURSDAY),
                settings.getInt(AlarmConstants.ALARM_DAY_FRIDAY   , AlarmConstants.ACTUAL_DAY_FRIDAY),
                settings.getInt(AlarmConstants.ALARM_DAY_SATURDAY , AlarmConstants.ACTUAL_DAY_SATURDAY),
                settings.getInt(AlarmConstants.ALARM_DAY_SUNDAY   , AlarmConstants.ACTUAL_DAY_SUNDAY)};  // Monday - Sunday

        bundle.putIntArray(AlarmConstants.WAKEUP_DAYS, days);

        int[] music = {
                settings.getInt(AlarmConstants.ALARM_MUSIC_SONGSTART      , AlarmConstants.ACTUAL_MUSIC_START),
                settings.getInt(AlarmConstants.ALARM_MUSIC_VOLUME         , AlarmConstants.ACTUAL_MUSIC_VOLUME),
                settings.getInt(AlarmConstants.ALARM_MUSIC_FADEIN         , AlarmConstants.ACTUAL_MUSIC_FADE_IN),
                settings.getInt(AlarmConstants.ALARM_MUSIC_FADEINTIME     , AlarmConstants.ACTUAL_MUSIC_FADE_IN_TIME),
                settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV, AlarmConstants.ACTUAL_MUSIC_VIBRATION),
                settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE, AlarmConstants.ACTUAL_MUSIC_VIBRATION_STRENGTH)};      // StartTime, Volume, FadIn, FadeInTime

        bundle.putIntArray(AlarmConstants.WAKEUP_MUSIC, music);

        int[] light = {
                settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN           , AlarmConstants.ACTUAL_SCREEN),
                settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS, AlarmConstants.ACTUAL_SCREEN_BRIGHTNESS),
                settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME, AlarmConstants.ACTUAL_SCREEN_START),
                settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR1           , AlarmConstants.ACTUAL_SCREEN_COLOR1),
                settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR2           , AlarmConstants.ACTUAL_SCREEN_COLOR2),
                settings.getInt(AlarmConstants.ALARM_LIGHT_FADECOLOR        , AlarmConstants.ACTUAL_SCREEN_COLOR_FADE),
                settings.getInt(AlarmConstants.ALARM_LIGHT_USELED           , AlarmConstants.ACTUAL_LED),
                settings.getInt(AlarmConstants.ALARM_LIGHT_LED_START_TIME   , AlarmConstants.ACTUAL_LED_START)};// UseScreen, ScreenColor1, ScreenColor2, Fadecolor, FadeTime, UseLED

        bundle.putIntArray(AlarmConstants.WAKEUP_LIGHT, light);

        //Uri to Music Song
        String musicURI = settings.getString(AlarmConstants.ALARM_MUSIC_SONGID, Settings.System.DEFAULT_ALARM_ALERT_URI.getPath());
        bundle.putString(AlarmConstants.ALARM_MUSIC_SONGID, musicURI);

        //Alarm ID
        bundle.putInt(AlarmConstants.ALARM_ID, _alarmId);

        //Fill Intent
        return new Intent(context, AlarmReceiver.class).putExtras(bundle);
    }

    private Intent getSnoozeIntent(int _alarmId){
        //sharedPrefereces
        String settingsName = AlarmConstants.WAKEUP_TIMER + _alarmId;
        SharedPreferences settings = context.getSharedPreferences(settingsName, Context.MODE_PRIVATE);

        Bundle bundle = new Bundle();
        //Putting Value for each child
        int[] time  = {
                settings.getInt(AlarmConstants.ALARM_TIME_HOUR   , AlarmConstants.ACTUAL_TIME_HOUR),
                settings.getInt(AlarmConstants.ALARM_TIME_MINUTES, AlarmConstants.ACTUAL_TIME_MINUTE),
                settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE , AlarmConstants.ACTUAL_TIME_SNOOZE)};    // hour, minute, snooze

        bundle.putIntArray(AlarmConstants.WAKEUP_TIME, time);

        int[] days  = {
                settings.getInt(AlarmConstants.ALARM_DAY_MONDAY   , AlarmConstants.ACTUAL_DAY_MONDAY),
                settings.getInt(AlarmConstants.ALARM_DAY_TUESDAY  , AlarmConstants.ACTUAL_DAY_TUESDAY),
                settings.getInt(AlarmConstants.ALARM_DAY_WEDNESDAY, AlarmConstants.ACTUAL_DAY_WEDNESDAY),
                settings.getInt(AlarmConstants.ALARM_DAY_THURSDAY , AlarmConstants.ACTUAL_DAY_THURSDAY),
                settings.getInt(AlarmConstants.ALARM_DAY_FRIDAY   , AlarmConstants.ACTUAL_DAY_FRIDAY),
                settings.getInt(AlarmConstants.ALARM_DAY_SATURDAY , AlarmConstants.ACTUAL_DAY_SATURDAY),
                settings.getInt(AlarmConstants.ALARM_DAY_SUNDAY   , AlarmConstants.ACTUAL_DAY_SUNDAY)};  // Monday - Sunday

        bundle.putIntArray(AlarmConstants.WAKEUP_DAYS, days);

        int[] music = {
                settings.getInt(AlarmConstants.ALARM_MUSIC_SONGSTART      , AlarmConstants.ACTUAL_MUSIC_START),
                settings.getInt(AlarmConstants.ALARM_MUSIC_VOLUME         , AlarmConstants.ACTUAL_MUSIC_VOLUME),
                settings.getInt(AlarmConstants.ALARM_MUSIC_FADEIN         , AlarmConstants.ACTUAL_MUSIC_FADE_IN),
                settings.getInt(AlarmConstants.ALARM_MUSIC_FADEINTIME     , AlarmConstants.ACTUAL_MUSIC_FADE_IN_TIME),
                settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV, AlarmConstants.ACTUAL_MUSIC_VIBRATION),
                settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE, AlarmConstants.ACTUAL_MUSIC_VIBRATION_STRENGTH)};      // StartTime, Volume, FadIn, FadeInTime

        bundle.putIntArray(AlarmConstants.WAKEUP_MUSIC, music);

        int[] light = {
                settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN           , AlarmConstants.ACTUAL_SCREEN),
                settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS, AlarmConstants.ACTUAL_SCREEN_BRIGHTNESS),
                0,
                settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR1           , AlarmConstants.ACTUAL_SCREEN_COLOR1),
                settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR2           , AlarmConstants.ACTUAL_SCREEN_COLOR2),
                settings.getInt(AlarmConstants.ALARM_LIGHT_FADECOLOR        , AlarmConstants.ACTUAL_SCREEN_COLOR_FADE),
                settings.getInt(AlarmConstants.ALARM_LIGHT_USELED           , AlarmConstants.ACTUAL_LED),
                0};// UseScreen, ScreenColor1, ScreenColor2, Fadecolor, FadeTime, UseLED


        bundle.putIntArray(AlarmConstants.WAKEUP_LIGHT, light);

        //Uri to Music Song
        String musicURI = settings.getString(AlarmConstants.ALARM_MUSIC_SONGID, Settings.System.DEFAULT_ALARM_ALERT_URI.getPath());
        bundle.putString(AlarmConstants.ALARM_MUSIC_SONGID, musicURI);

        //Alarm ID
        bundle.putInt(AlarmConstants.ALARM_ID, _alarmId);

        //Fill Intent
        return new Intent(context, AlarmReceiver.class).putExtras(bundle);
    }

    public void setNewAlarm(int _id, boolean snooze){

        //sharedPreferences
        String settingName = AlarmConstants.WAKEUP_TIMER + _id;
        SharedPreferences settings = context.getSharedPreferences(settingName, Context.MODE_PRIVATE);

        //GetData
        //Putting Value for each child
        int[] time  = {
                settings.getInt(AlarmConstants.ALARM_TIME_HOUR, AlarmConstants.ACTUAL_TIME_HOUR),
                settings.getInt(AlarmConstants.ALARM_TIME_MINUTES, AlarmConstants.ACTUAL_TIME_MINUTE),
                settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE, 10)};    // hour, minute, snooze

        //create Alarm
        createAlarmManager();
        PendingIntent pendingIntent = getPendingIntent(_id);
        alarmManager.cancel(pendingIntent);

        Calendar calendar = Calendar.getInstance();
        //Get Days to next Alarm
        long alarmTime;
        if(!snooze){
            calendar.set(Calendar.HOUR_OF_DAY, time[0]);
            calendar.set(Calendar.MINUTE, time[1]);

            alarmTime = calendar.getTimeInMillis();
            long systemTime = System.currentTimeMillis();
            long timeToNextDay = TimeUnit.DAYS.toMillis(getDaysToNextAlarm(settings) * 24); //24 Hours are One Day for TimeUnit
            alarmTime = (alarmTime < systemTime) ? alarmTime +  timeToNextDay : alarmTime;
        }
        else{//Snooze
            pendingIntent = getSnoozePendingIntent(_id);
            alarmTime = calendar.getTimeInMillis() + TimeUnit.MINUTES.toMillis(time[2]); //+ Snooze
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

    public void snoozeAlarm(int _id){
        if(_id!= -1)
            setNewAlarm(_id, true);
    }

    public  void cancelAlarm(int _id){
        //get Days till Next Alarm
        int daysToNextAlarm = getDaysToNextAlarm(_id);
        createAlarmManager();

        //If there are Days between next Alarm multiply else Cancel Alarm
        if(daysToNextAlarm > 0)
            setNewAlarm(_id, false);
        else{
            alarmManager.cancel(getPendingIntent(_id));
            getPendingIntent(_id).cancel();
        }
    }

    public void cancelAlarmwithButton(int _id){
        createAlarmManager();
        if(checkForPendingIntent(_id)){
            alarmManager.cancel(getPendingIntent(_id));
            getPendingIntent(_id).cancel();
        }
    }

    private int getDaysToNextAlarm(int _id){

        //sharedPrefereces
        String settingName = AlarmConstants.WAKEUP_TIMER + _id;
        SharedPreferences settings = context.getSharedPreferences(settingName, Context.MODE_PRIVATE);

        //Load Days for Repeat
        int[] days  = {
                settings.getInt(AlarmConstants.ALARM_DAY_MONDAY   , 0),
                settings.getInt(AlarmConstants.ALARM_DAY_TUESDAY  , 0),
                settings.getInt(AlarmConstants.ALARM_DAY_WEDNESDAY, 0),
                settings.getInt(AlarmConstants.ALARM_DAY_THURSDAY , 0),
                settings.getInt(AlarmConstants.ALARM_DAY_FRIDAY   , 0),
                settings.getInt(AlarmConstants.ALARM_DAY_SATURDAY , 0),
                settings.getInt(AlarmConstants.ALARM_DAY_SUNDAY   , 0)};

        //Load Calendar Instance and get Actual Day of the Week
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        //get Days till Next Alarm
        int daysToNextAlarm = 0;

        if(days[0] == 1 || days[1] == 1 || days[2] == 1 || days[3] == 1 || days[4] == 1 || days[5] == 1 || days[6] == 1)
            for(int day = 0; day < 7; ++day){

                //Get Current Day
                int nextDay = currentDay + day ;
                //If Sunday is Arrived switch to Monday index
                nextDay = (nextDay > 6) ? nextDay - 7 : nextDay;

                //If Next Day don't has a Alarm add 1 to daysNextAlarm else break loop
                if(days[nextDay] == 0)
                    ++daysToNextAlarm;
                else{
                    ++daysToNextAlarm;
                    break;
                }
            }

        return daysToNextAlarm;
    }

    private int getDaysToNextAlarm(SharedPreferences settings){

        //Load Days for Repeat
        int[] days  = {
                settings.getInt(AlarmConstants.ALARM_DAY_MONDAY   , 0),
                settings.getInt(AlarmConstants.ALARM_DAY_TUESDAY  , 0),
                settings.getInt(AlarmConstants.ALARM_DAY_WEDNESDAY, 0),
                settings.getInt(AlarmConstants.ALARM_DAY_THURSDAY , 0),
                settings.getInt(AlarmConstants.ALARM_DAY_FRIDAY   , 0),
                settings.getInt(AlarmConstants.ALARM_DAY_SATURDAY , 0),
                settings.getInt(AlarmConstants.ALARM_DAY_SUNDAY   , 0)};

        //Load Calendar Instance and get Actual Day of the Week
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        //get Days till Next Alarm
        int daysToNextAlarm = 0;

        if(days[0] == 1 || days[1] == 1 || days[2] == 1 || days[3] == 1 || days[4] == 1 || days[5] == 1 || days[6] == 1)
            for(int day = 0; day < 7; ++day){

                //Get Current Day
                int nextDay = currentDay + day ;
                //If Sunday is Arrived switch to Monday index
                nextDay = (nextDay > 6) ? nextDay - 7 : nextDay;

                //If Next Day don't has a Alarm add 1 to daysNextAlarm else break loop
                if(days[nextDay] == 0)
                    ++daysToNextAlarm;
                else{
                    ++daysToNextAlarm;
                    break;
                }
            }

        return daysToNextAlarm;
    }
}
