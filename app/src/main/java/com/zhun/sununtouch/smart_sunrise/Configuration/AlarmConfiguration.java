package com.zhun.sununtouch.smart_sunrise.Configuration;

import android.content.Context;
import android.content.SharedPreferences;

import com.zhun.sununtouch.smart_sunrise.Alarm.AlarmManage;
import com.zhun.sununtouch.smart_sunrise.Information.AlarmConstants;
import com.zhun.sununtouch.smart_sunrise.R;

import java.util.Calendar;

/**
 * Created by Sunny on 18.09.2016.
 * Model Class to represent a Alarm
 */
@SuppressWarnings("WeakerAccess")
public class AlarmConfiguration {

    private Context m_Context;
    private final AlarmLogging m_Log;
    private final String TAG = "AlarmConfiguration";

    //Actual Alarm Values
    private int actualAlarm    = 0;

    private String actualAlarmName = AlarmConstants.ALARM;

    //Actual Alarm Set
    private boolean tempTimes    = false;
    private boolean alarmOneShot = false;

    //Time
    private int m_Hour       = AlarmConstants.ACTUAL_TIME_HOUR;
    private int m_Minutes    = AlarmConstants.ACTUAL_TIME_MINUTE;
    private int m_SnoozeTime = AlarmConstants.ACTUAL_TIME_SNOOZE;

    //Days
    private boolean m_MondaySet    = AlarmConstants.ACTUAL_DAY_MONDAY;
    private boolean m_TuesdaySet   = AlarmConstants.ACTUAL_DAY_TUESDAY;
    private boolean m_WednesdaySet = AlarmConstants.ACTUAL_DAY_WEDNESDAY;
    private boolean m_ThursdaySet  = AlarmConstants.ACTUAL_DAY_THURSDAY;
    private boolean m_FridaySet    = AlarmConstants.ACTUAL_DAY_FRIDAY;
    private boolean m_SaturdaySet  = AlarmConstants.ACTUAL_DAY_SATURDAY;
    private boolean m_SundaySet    = AlarmConstants.ACTUAL_DAY_SUNDAY;

    //Music
    private String m_SongURI        = AlarmConstants.ACTUAL_MUSIC_SONG_URI;
    private int m_SongStart         = AlarmConstants.ACTUAL_MUSIC_START;
    private int m_SongLength        = AlarmConstants.ACTUAL_MUSIC_LENGTH;
    private int m_Volume            = AlarmConstants.ACTUAL_MUSIC_VOLUME;
    private boolean m_FadeInSet     = AlarmConstants.ACTUAL_MUSIC_FADE_IN;
    private int m_FadeInTime        = AlarmConstants.ACTUAL_MUSIC_FADE_IN_TIME;
    private boolean m_VibrationSet  = AlarmConstants.ACTUAL_MUSIC_VIBRATION;
    private int m_VibrationStrength = AlarmConstants.ACTUAL_MUSIC_VIBRATION_STRENGTH;

    //Light
    private boolean m_ScreenSet         = AlarmConstants.ACTUAL_SCREEN;
    private int m_ScreenBrightness      = AlarmConstants.ACTUAL_SCREEN_BRIGHTNESS;
    private int m_ScreenStartTime       = AlarmConstants.ACTUAL_SCREEN_START;
    private int m_ScreenStartTimeTemp   = AlarmConstants.ACTUAL_SCREEN_START;
    private int m_LightColor1           = AlarmConstants.ACTUAL_SCREEN_COLOR1;
    private int m_LightColor2           = AlarmConstants.ACTUAL_SCREEN_COLOR2;
    private boolean m_LightFade         = AlarmConstants.ACTUAL_SCREEN_COLOR_FADE;
    private boolean m_LightLEDSet       = AlarmConstants.ACTUAL_LED;
    private int m_LightLEDStartTime     = AlarmConstants.ACTUAL_LED_START;
    private int m_LightLEDStartTimeTemp = AlarmConstants.ACTUAL_LED_START;

    public AlarmConfiguration(Context context){
        m_Context = context;
        m_Log = new AlarmLogging(context);
    }
    public AlarmConfiguration(Context context, int ID){
        init(context,ID);
        m_Log = new AlarmLogging(context);
    }

    private void init(Context context,int ID){
        m_Context = context;

        //save Settings
        SharedPreferences settings = AlarmSharedPreferences.getSharedPreference(m_Context, AlarmConstants.ALARM, ID);

        //ID, name, AlarmSet
        this.setAlarmID(ID);
        this.setAlarmName(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM + Integer.toString(ID)));
        this.setTemporaryTimes(settings.getBoolean(AlarmConstants.ALARM_TEMPORARY , AlarmConstants.ACTUAL_TEMPORARY));
        this.setAlarmOneShot  (settings.getBoolean(AlarmConstants.ALARM_ONESHOT   , AlarmConstants.ACTUAL_ONESHOT));

        //Days
        this.setMonday   (settings.getBoolean(AlarmConstants.ALARM_DAY_MONDAY    , AlarmConstants.ACTUAL_DAY_MONDAY));
        this.setTuesday  (settings.getBoolean(AlarmConstants.ALARM_DAY_TUESDAY   , AlarmConstants.ACTUAL_DAY_TUESDAY));
        this.setWednesday(settings.getBoolean(AlarmConstants.ALARM_DAY_WEDNESDAY , AlarmConstants.ACTUAL_DAY_WEDNESDAY));
        this.setThursday (settings.getBoolean(AlarmConstants.ALARM_DAY_THURSDAY  , AlarmConstants.ACTUAL_DAY_THURSDAY));
        this.setFriday   (settings.getBoolean(AlarmConstants.ALARM_DAY_FRIDAY    , AlarmConstants.ACTUAL_DAY_FRIDAY));
        this.setSaturday (settings.getBoolean(AlarmConstants.ALARM_DAY_SATURDAY  , AlarmConstants.ACTUAL_DAY_SATURDAY));
        this.setSunday   (settings.getBoolean(AlarmConstants.ALARM_DAY_SUNDAY    , AlarmConstants.ACTUAL_DAY_SUNDAY));

        //Load Music
        this.setSongURI          (settings.getString (AlarmConstants.ALARM_MUSIC_SONG_ID, AlarmConstants.ACTUAL_MUSIC_SONG_URI));
        this.setSongStart        (settings.getInt    (AlarmConstants.ALARM_MUSIC_SONG_START, AlarmConstants.ACTUAL_MUSIC_START));
        this.setSongLength       (settings.getInt    (AlarmConstants.ALARM_MUSIC_SONG_LENGTH, AlarmConstants.ACTUAL_MUSIC_LENGTH));
        this.setVolume           (settings.getInt    (AlarmConstants.ALARM_MUSIC_VOLUME          , AlarmConstants.ACTUAL_MUSIC_VOLUME));
        this.setFadeIn           (settings.getBoolean(AlarmConstants.ALARM_MUSIC_FADE_IN, AlarmConstants.ACTUAL_MUSIC_FADE_IN));
        this.setFadeInTime       (settings.getInt    (AlarmConstants.ALARM_MUSIC_FADE_IN_TIME, AlarmConstants.ACTUAL_MUSIC_FADE_IN_TIME));
        this.setVibration        (settings.getBoolean(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIVE, AlarmConstants.ACTUAL_MUSIC_VIBRATION));
        this.setVibrationStrength(settings.getInt    (AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE , AlarmConstants.ACTUAL_MUSIC_VIBRATION_STRENGTH));

        //Load Light
        this.setScreen          (settings.getBoolean(AlarmConstants.ALARM_LIGHT_SCREEN            , AlarmConstants.ACTUAL_SCREEN));
        this.setScreenBrightness(settings.getInt    (AlarmConstants.ALARM_LIGHT_SCREEN_BRIGHTNESS, AlarmConstants.ACTUAL_SCREEN_BRIGHTNESS));
        this.setScreenStartTime (settings.getInt    (AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME , AlarmConstants.ACTUAL_SCREEN_START));
        this.setScreenStartTemp (settings.getInt    (AlarmConstants.ALARM_LIGHT_SCREEN_START_TEMP , AlarmConstants.ACTUAL_SCREEN_START));
        this.setLightColor1     (settings.getInt    (AlarmConstants.ALARM_LIGHT_COLOR1            , AlarmConstants.ACTUAL_SCREEN_COLOR1));
        this.setLightColor2     (settings.getInt    (AlarmConstants.ALARM_LIGHT_COLOR2            , AlarmConstants.ACTUAL_SCREEN_COLOR2));
        this.setLightFade       (settings.getBoolean(AlarmConstants.ALARM_LIGHT_FADE_COLOR, AlarmConstants.ACTUAL_SCREEN_COLOR_FADE));
        this.setLED             (settings.getBoolean(AlarmConstants.ALARM_LIGHT_USE_LED, AlarmConstants.ACTUAL_LED));
        this.setLEDStartTime    (settings.getInt    (AlarmConstants.ALARM_LIGHT_LED_START_TIME    , AlarmConstants.ACTUAL_LED_START));
        this.setLEDStartTemp    (settings.getInt    (AlarmConstants.ALARM_LIGHT_LED_START_TEMP    , AlarmConstants.ACTUAL_LED_START));

        //Time
        Calendar calendar = Calendar.getInstance();
        this.setHour  (settings.getInt(AlarmConstants.ALARM_TIME_HOUR    , calendar.get(Calendar.HOUR_OF_DAY)));
        this.setMinute(settings.getInt(AlarmConstants.ALARM_TIME_MINUTES , calendar.get(Calendar.MINUTE)));
        this.setSnooze(settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE  , AlarmConstants.ACTUAL_TIME_SNOOZE));
    }
    public void commit(){

        //Load sharedPreferences
        SharedPreferences.Editor editor = AlarmSharedPreferences.getSharedPreferenceEditor(m_Context, AlarmConstants.ALARM, getAlarmID()).clear();

        //put StringSet back
        editor.putString(AlarmConstants.ALARM_NAME, getAlarmName());

        //Alarm is Set
        editor.putBoolean(AlarmConstants.ALARM_ONESHOT, getAlarmOneShot());
        editor.putBoolean(AlarmConstants.ALARM_TEMPORARY,  getTemporaryTimes());

        //Time
        editor.putInt(AlarmConstants.ALARM_TIME_MINUTES  , getMinute());
        editor.putInt(AlarmConstants.ALARM_TIME_HOUR     , getHour());
        editor.putInt(AlarmConstants.ALARM_TIME_SNOOZE   , getSnooze());

        //Days
        editor.putBoolean(AlarmConstants.ALARM_DAY_MONDAY    , isMonday());
        editor.putBoolean(AlarmConstants.ALARM_DAY_TUESDAY   , isTuesday());
        editor.putBoolean(AlarmConstants.ALARM_DAY_WEDNESDAY , isWednesday());
        editor.putBoolean(AlarmConstants.ALARM_DAY_THURSDAY  , isThursday());
        editor.putBoolean(AlarmConstants.ALARM_DAY_FRIDAY    , isFriday());
        editor.putBoolean(AlarmConstants.ALARM_DAY_SATURDAY  , isSaturday());
        editor.putBoolean(AlarmConstants.ALARM_DAY_SUNDAY    , isSunday());

        //Music
        editor.putString (AlarmConstants.ALARM_MUSIC_SONG_ID, getSongURI());
        editor.putInt    (AlarmConstants.ALARM_MUSIC_VOLUME          , getVolume());
        editor.putInt    (AlarmConstants.ALARM_MUSIC_SONG_START, getSongStart());
        editor.putInt    (AlarmConstants.ALARM_MUSIC_SONG_LENGTH, getSongLength());
        editor.putBoolean(AlarmConstants.ALARM_MUSIC_FADE_IN, getFadeIn());
        editor.putInt    (AlarmConstants.ALARM_MUSIC_FADE_IN_TIME, getFadeInTime());
        editor.putBoolean(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIVE, getVibration());
        editor.putInt    (AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE , getVibrationStrength());

        //Light
        editor.putBoolean(AlarmConstants.ALARM_LIGHT_SCREEN            , getScreen());
        editor.putInt    (AlarmConstants.ALARM_LIGHT_SCREEN_BRIGHTNESS, getScreenBrightness());
        editor.putInt    (AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME , getScreenStartTime());
        editor.putInt    (AlarmConstants.ALARM_LIGHT_SCREEN_START_TEMP , getScreenStartTemp());
        editor.putInt    (AlarmConstants.ALARM_LIGHT_COLOR1            , getLightColor1());
        editor.putInt    (AlarmConstants.ALARM_LIGHT_COLOR2            , getLightColor2());
        editor.putBoolean(AlarmConstants.ALARM_LIGHT_FADE_COLOR, getLightFade());
        editor.putBoolean(AlarmConstants.ALARM_LIGHT_USE_LED, getLED());
        editor.putInt    (AlarmConstants.ALARM_LIGHT_LED_START_TIME    , getLEDStartTime());
        editor.putInt    (AlarmConstants.ALARM_LIGHT_LED_START_TEMP    , getLEDStartTemp());

        //apply Values to settings
        editor.apply();

        m_Log.i(TAG, m_Context.getString(R.string.logging_config_saved, AlarmConstants.ALARM + getAlarmID()));
    }

    private AlarmManage createAlarmManager(){
            return new AlarmManage(m_Context, this);
    }
    public boolean cancelAlarm(){
        return createAlarmManager().cancelAlarm();
    }
    public void snoozeAlarm(){
        createAlarmManager().setAlarm(true);
    }
    public boolean activateAlarm(){
        return createAlarmManager().setAlarm(false);
    }
    public void refreshAlarm(){
        createAlarmManager().refresh();
    }

    //Enums
    public enum childItem{
        WAKEUP_DELETE,
        WAKEUP_TIME,
        WAKEUP_DAYS,
        WAKEUP_MUSIC,
        WAKEUP_LIGHT
    }
    private static final int childItems = childItem.values().length;
    public int getChildItemSize(){
        return childItems;
    }

    //Name
    public String getAlarmName(){
        return actualAlarmName;
    }
    public void setAlarmName(final String name){
        actualAlarmName = name;
    }

    //Actual Alarm Values
    public int getAlarmID(){
        return actualAlarm;
    }
    public void setAlarmID(final int id){
        actualAlarm = id;
    }

    //Actual Alarm Set
    public boolean isAlarmSet(){
        return createAlarmManager().checkPendingIntent();
    }

    //Time
    public int getHour(){
        return m_Hour;
    }
    public int getMinute(){
        return m_Minutes;
    }
    public int getSnooze(){
        return m_SnoozeTime;
    }

    public void setHour(int hour){
        m_Hour = hour;
    }
    public void setMinute(int minute){
        m_Minutes    = minute;
    }
    public void setSnooze(int snooze){
        m_SnoozeTime = snooze;
    }

    //Days
    public String getDayName(int day, boolean longName){
        if(day < 0 && day > Calendar.SATURDAY)
            return "";
        return (longName) ? AlarmConstants.LONG_DAYS[day] : AlarmConstants.SHORT_DAYS[day];
    }
    public boolean isDaySet(){
        return (isMonday()|| isTuesday() || isWednesday() || isThursday() || isFriday() || isSaturday() || isSunday());
    }
    public boolean isDaySet(int day){
        switch(day)
        {
            case Calendar.MONDAY   : return isMonday();
            case Calendar.TUESDAY  : return isTuesday();
            case Calendar.WEDNESDAY: return isWednesday();
            case Calendar.THURSDAY : return isThursday();
            case Calendar.FRIDAY   : return isFriday();
            case Calendar.SATURDAY : return isSaturday();
            case Calendar.SUNDAY   : return isSunday();
            default: return false;
        }
    }

    public int getTimeToNextDay(){

        //No Repeat Days set, return zero
        if(!isDaySet())
            return 0;

        //Count Days
        int nextDay = 1;
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        currentDay = (currentDay++ == Calendar.SATURDAY) ? Calendar.SUNDAY : currentDay;
        while (!isDaySet(currentDay))
        {
            if(nextDay++ == 7)
                break;

            if(currentDay++ == Calendar.SATURDAY)
                currentDay = Calendar.SUNDAY;
        }
        return nextDay;
    }
    public boolean isMonday(){
        return m_MondaySet;
    }
    public boolean isTuesday(){
        return m_TuesdaySet;
    }
    public boolean isWednesday(){
        return m_WednesdaySet;
    }
    public boolean isThursday(){
        return m_ThursdaySet;
    }
    public boolean isFriday(){
        return m_FridaySet;
    }
    public boolean isSaturday(){
        return m_SaturdaySet;
    }
    public boolean isSunday(){
        return m_SundaySet;
    }

    public void setMonday(boolean monday){
        m_MondaySet = monday;
    }
    public void setTuesday(boolean tuesday){
        m_TuesdaySet = tuesday;
    }
    public void setWednesday(boolean wednesday){
        m_WednesdaySet = wednesday;
    }
    public void setThursday(boolean thursday){
        m_ThursdaySet = thursday;
    }
    public void setFriday(boolean friday){
        m_FridaySet = friday;
    }
    public void setSaturday(boolean saturday){
        m_SaturdaySet = saturday;
    }
    public void setSunday(boolean sunday){
        m_SundaySet = sunday;
    }

    //Music
    public String getSongName(){
        return m_SongURI.substring(m_SongURI.lastIndexOf('/') + 1);
    }
    public String getSongURI(){
        return m_SongURI;
    }
    public void setSongURI(String uri){
        m_SongURI = uri;
    }

    public int getSongStart(){
        return m_SongStart;
    }
    public void setSongStart(int start){
        m_SongStart = start;
    }

    public int getSongLength(){
        return m_SongLength;
    }
    public void setSongLength(int length){
        m_SongLength = length;
    }

    public int getVolume(){
        return m_Volume;
    }
    public void setVolume(int volume){
        m_Volume = volume;
    }

    public boolean getFadeIn(){
        return m_FadeInSet;
    }
    public void setFadeIn(boolean fadeIn){
        m_FadeInSet = fadeIn;
    }

    public int getFadeInTime(){
        return m_FadeInTime;
    }
    public void setFadeInTime(int time){
        m_FadeInTime = time;
    }

    public boolean getVibration(){
        return m_VibrationSet;
    }
    public void setVibration(boolean vibration){
        m_VibrationSet = vibration;
    }

    public int getVibrationStrength(){
        return m_VibrationStrength;
    }
    public void setVibrationStrength(int strength){
        m_VibrationStrength = strength;
    }

    //Screen
    public boolean getScreen(){
        return m_ScreenSet;
    }
    public void setScreen(boolean screen){
        m_ScreenSet = screen;
    }

    public int getScreenBrightness(){
        return m_ScreenBrightness;
    }
    public void setScreenBrightness(int brightness){
        m_ScreenBrightness = brightness;
    }

    public int getScreenStartTime(){
        return m_ScreenStartTime;
    }
    public void setScreenStartTime(int time){
        m_ScreenStartTime = time;
    }
    public int getScreenStartTemp(){
        return (m_ScreenStartTimeTemp < 0) ? 0 : m_ScreenStartTimeTemp ;
    }
    public void setScreenStartTemp(int time){
        m_ScreenStartTimeTemp = time;
    }

    //Light
    public int getLightColor1(){
        return m_LightColor1;
    }
    public void setLightColor1(int color){
        m_LightColor1 = color;
    }

    public int getLightColor2(){
        return m_LightColor2;
    }
    public void setLightColor2(int color){
        m_LightColor2 = color;
    }

    public boolean getLightFade(){
        return m_LightFade ;
    }
    public void setLightFade(boolean fade){
        m_LightFade = fade;
    }

    //LED
    public boolean getLED(){
        return m_LightLEDSet;
    }
    public void setLED(boolean led){
        m_LightLEDSet = led;
    }

    public int getLEDStartTime(){
        return m_LightLEDStartTime;
    }
    public void setLEDStartTime(int time){
        m_LightLEDStartTime = time;
    }
    public int getLEDStartTemp(){
        return (m_LightLEDStartTimeTemp < 0) ? 0 : m_LightLEDStartTimeTemp ;
    }
    public void setLEDStartTemp(int time){
        m_LightLEDStartTimeTemp = time;
    }

    public void setTemporaryTimes(boolean temp){
        tempTimes = temp;
    }
    public boolean getTemporaryTimes(){
        return tempTimes;
    }

    public void setAlarmOneShot(boolean shot){
        alarmOneShot = shot;
    }
    public boolean getAlarmOneShot(){
        return alarmOneShot;
    }
}