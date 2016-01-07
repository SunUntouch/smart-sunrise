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


/**
 * Created by Sunny on 06.01.2016.
 */
public class AlarmManage extends AppCompatActivity {

    private AlarmManager  alarmManager;
    private PendingIntent pendingIntent;

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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, _alarmId, getIntent(_alarmId), PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    private PendingIntent getOnTapPendingIntent(){

        Intent intent = new Intent("WAKEUP_TIMER_ON_TAP_INTENT");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent;
    }

    public boolean checkForPendingIntent(int _alarmId){

        //Check if Intent exists
        boolean alarmUp = (PendingIntent.getBroadcast(context, _alarmId, getIntent(_alarmId), PendingIntent.FLAG_NO_CREATE) != null);
        return alarmUp;
    }

    private Intent getIntent(int _alarmId){
        Intent intent = new Intent(context, AlarmReceiver.class);

        //sharedPrefereces
        String settingsName = AlarmConstants.WAKEUP_TIMER + _alarmId;
        SharedPreferences settings = context.getSharedPreferences(settingsName, Context.MODE_PRIVATE);

        Bundle bundle = new Bundle();
        //Time
        int[] timevalues = {
                settings.getInt(AlarmConstants.ALARM_TIME_HOUR   , 0),
                settings.getInt(AlarmConstants.ALARM_TIME_MINUTES, 0),
                settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE , 0)};

        bundle.putIntArray( AlarmConstants.WAKEUP_TIME,timevalues);

        //Days
        int[] daysvalues = {
                settings.getInt(AlarmConstants.ALARM_DAY_MONDAY   , 0),
                settings.getInt(AlarmConstants.ALARM_DAY_TUESDAY  , 0),
                settings.getInt(AlarmConstants.ALARM_DAY_WEDNESDAY, 0),
                settings.getInt(AlarmConstants.ALARM_DAY_THURSDAY , 0),
                settings.getInt(AlarmConstants.ALARM_DAY_FRIDAY   , 0),
                settings.getInt(AlarmConstants.ALARM_DAY_SATURDAY , 0),
                settings.getInt(AlarmConstants.ALARM_DAY_SUNDAY   , 0)};

        bundle.putIntArray( AlarmConstants.WAKEUP_DAYS,daysvalues);

        //music
        int[] musicvalues = {
                settings.getInt(AlarmConstants.ALARM_MUSIC_SONGSTART      , 0),
                settings.getInt(AlarmConstants.ALARM_MUSIC_VOLUME         , 0),
                settings.getInt(AlarmConstants.ALARM_MUSIC_FADEIN         , 0),
                settings.getInt(AlarmConstants.ALARM_MUSIC_FADEINTIME     , 0),
                settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV, 0),
                settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE, 0)};      // Song, StartTime, Volume, FadIn, FadeInTime, Vibration Aktiv, Vibration Strength

        bundle.putIntArray(AlarmConstants.WAKEUP_MUSIC, musicvalues);

        //light
        int[] lightvalues = {
                settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN           , 0),
                settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS, 0),
                settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME, 0),
                settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR1           , 0),
                settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR2           , 0),
                settings.getInt(AlarmConstants.ALARM_LIGHT_FADECOLOR        , 0),
                settings.getInt(AlarmConstants.ALARM_LIGHT_USELED           , 0),
                settings.getInt(AlarmConstants.ALARM_LIGHT_LED_START_TIME   , 0)}; // UseScreen, ScreenBrightness, ScreenStrartTime, Color1, Color2, FadeColor, UseLed, LedStartTime

        bundle.putIntArray(AlarmConstants.WAKEUP_LIGHT, lightvalues);

        //Uri to Music Song
        String musicURI = settings.getString(AlarmConstants.ALARM_MUSIC_SONGID, Settings.System.DEFAULT_ALARM_ALERT_URI.getPath());
        bundle.putString(AlarmConstants.ALARM_MUSIC_SONGID, musicURI);

        //Alarm ID
        bundle.putInt(AlarmConstants.ALARM_ID, _alarmId);

        //Fill Intent
        intent.putExtras(bundle);

        return intent;
    }

    public void setNewAlarm(int _id, boolean snooze, long repeatMillis){

        //sharedPrefereces
        String settingName = AlarmConstants.WAKEUP_TIMER + _id;
        SharedPreferences settings = context.getSharedPreferences(settingName, Context.MODE_PRIVATE);

        //GetData
        String name = settings.getString(AlarmConstants.ALARM_NAME, context.getString(R.string.wakeup_no_alarm));

        //Putting Value for each child
        int[] time  = {
                settings.getInt(AlarmConstants.ALARM_TIME_HOUR, 00),
                settings.getInt(AlarmConstants.ALARM_TIME_MINUTES, 00),
                settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE, 10)};    // hour, minute, snooze

        Calendar calendar = Calendar.getInstance();
        long snoozeTime = 0;

        if(!snooze){
            calendar.set(Calendar.HOUR_OF_DAY, time[0]);
            calendar.set(Calendar.MINUTE, time[1]);

        }
        else //Snooze
            snoozeTime = TimeUnit.MINUTES.toMillis(time[2]);

        pendingIntent = getPendingIntent(_id);

        createAlarmManager();
        alarmManager.cancel(pendingIntent);

        //Check for SDK Version and Use different AlarmManager Functions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){

            //Newer API Level provides a Symbol when Alarm is active
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis() + snoozeTime + repeatMillis, pendingIntent);
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        }
        else
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + snoozeTime + repeatMillis, pendingIntent);
    }

    public void snoozeAlarm(int _id){
        if(_id!= -1)
            setNewAlarm(_id, true, 0); //TODO Only Musik
    }

    public  void cancelAlarm(int _id){

        pendingIntent = getPendingIntent(_id);

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

        createAlarmManager();
        //If there are Days between next Alarm multiply else Cancel Alarm
        if(daysToNextAlarm > 0)
            setNewAlarm(_id, false, AlarmManager.INTERVAL_DAY * daysToNextAlarm);
        else
            alarmManager.cancel(pendingIntent);
    }
}
