package com.zhun.sununtouch.smart_sunrise;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * Created by Sunny on 18.09.2016.
 */
class AlarmConfiguration {

    private Context m_Context;

    //Actual Alarm Values
    private int actualAlarm    = 0;

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
    private String m_SongURI    = AlarmConstants.ACTUAL_MUSIC_SONG_URI;
    private int m_SongStart     = AlarmConstants.ACTUAL_MUSIC_START;
    private int m_SongLength    = AlarmConstants.ACTUAL_MUSIC_LENGTH;
    private int m_Volume        = AlarmConstants.ACTUAL_MUSIC_VOLUME;
    private boolean m_FadeInSet = AlarmConstants.ACTUAL_MUSIC_FADE_IN;
    private int m_FadeInTime    = AlarmConstants.ACTUAL_MUSIC_FADE_IN_TIME;
    private boolean m_VibraSet  = AlarmConstants.ACTUAL_MUSIC_VIBRATION;
    private int m_VibraStrength = AlarmConstants.ACTUAL_MUSIC_VIBRATION_STRENGTH;

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

    AlarmConfiguration(Context context){
        m_Context = context;
    }
    AlarmConfiguration(Context context, int ID){
        init(context,ID);
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
        this.setSongURI          (settings.getString (AlarmConstants.ALARM_MUSIC_SONGID          , AlarmConstants.ACTUAL_MUSIC_SONG_URI));
        this.setSongStart        (settings.getInt    (AlarmConstants.ALARM_MUSIC_SONGSTART       , AlarmConstants.ACTUAL_MUSIC_START));
        this.setSongLength       (settings.getInt    (AlarmConstants.ALARM_MUSIC_SONGLENGTH      , AlarmConstants.ACTUAL_MUSIC_LENGTH));
        this.setVolume           (settings.getInt    (AlarmConstants.ALARM_MUSIC_VOLUME          , AlarmConstants.ACTUAL_MUSIC_VOLUME));
        this.setFadeIn           (settings.getBoolean(AlarmConstants.ALARM_MUSIC_FADEIN          , AlarmConstants.ACTUAL_MUSIC_FADE_IN));
        this.setFadeInTime       (settings.getInt    (AlarmConstants.ALARM_MUSIC_FADEINTIME      , AlarmConstants.ACTUAL_MUSIC_FADE_IN_TIME));
        this.setVibration        (settings.getBoolean(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV , AlarmConstants.ACTUAL_MUSIC_VIBRATION));
        this.setVibrationStrength(settings.getInt    (AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE , AlarmConstants.ACTUAL_MUSIC_VIBRATION_STRENGTH));

        //Load Light
        this.setScreen          (settings.getBoolean(AlarmConstants.ALARM_LIGHT_SCREEN            , AlarmConstants.ACTUAL_SCREEN));
        this.setScreenBrightness(settings.getInt    (AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS , AlarmConstants.ACTUAL_SCREEN_BRIGHTNESS));
        this.setScreenStartTime (settings.getInt    (AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME , AlarmConstants.ACTUAL_SCREEN_START));
        this.setScreenStartTemp (settings.getInt    (AlarmConstants.ALARM_LIGHT_SCREEN_START_TEMP , AlarmConstants.ACTUAL_SCREEN_START));
        this.setLightColor1     (settings.getInt    (AlarmConstants.ALARM_LIGHT_COLOR1            , AlarmConstants.ACTUAL_SCREEN_COLOR1));
        this.setLightColor2     (settings.getInt    (AlarmConstants.ALARM_LIGHT_COLOR2            , AlarmConstants.ACTUAL_SCREEN_COLOR2));
        this.setLightFade       (settings.getBoolean(AlarmConstants.ALARM_LIGHT_FADECOLOR         , AlarmConstants.ACTUAL_SCREEN_COLOR_FADE));
        this.setLED             (settings.getBoolean(AlarmConstants.ALARM_LIGHT_USELED            , AlarmConstants.ACTUAL_LED));
        this.setLEDStartTime    (settings.getInt    (AlarmConstants.ALARM_LIGHT_LED_START_TIME    , AlarmConstants.ACTUAL_LED_START));
        this.setLEDStartTemp    (settings.getInt    (AlarmConstants.ALARM_LIGHT_LED_START_TEMP    , AlarmConstants.ACTUAL_LED_START));

        //Time
        Calendar calendar = Calendar.getInstance();
        this.setHour  (settings.getInt(AlarmConstants.ALARM_TIME_HOUR    , calendar.get(Calendar.HOUR_OF_DAY)));
        this.setMinute(settings.getInt(AlarmConstants.ALARM_TIME_MINUTES , calendar.get(Calendar.MINUTE)));
        this.setSnooze(settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE  , AlarmConstants.ACTUAL_TIME_SNOOZE));
    }
    void commit(){

        //Load sharedPreferences
        SharedPreferences.Editor editor = AlarmSharedPreferences.getSharedPreferenceEditor(m_Context, AlarmConstants.ALARM, getAlarmID()).clear();

        //put StringSet back
        editor.putString(AlarmConstants.ALARM_NAME, getAlarmName());

        //Alarm is Set
        editor.putBoolean(AlarmConstants.ALARM_ONESHOT, getTemporaryTimes());
        editor.putBoolean(AlarmConstants.ALARM_TEMPORARY, getAlarmOneShot());

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
        editor.putString (AlarmConstants.ALARM_MUSIC_SONGID          , getSongURI());
        editor.putInt    (AlarmConstants.ALARM_MUSIC_VOLUME          , getVolume());
        editor.putInt    (AlarmConstants.ALARM_MUSIC_SONGSTART       , getSongStart());
        editor.putInt    (AlarmConstants.ALARM_MUSIC_SONGLENGTH      , getSongLength());
        editor.putBoolean(AlarmConstants.ALARM_MUSIC_FADEIN          , getFadeIn());
        editor.putInt    (AlarmConstants.ALARM_MUSIC_FADEINTIME      , getFadeInTime());
        editor.putBoolean(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV , getVibration());
        editor.putInt    (AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE , getVibrationStrength());

        //Light
        editor.putBoolean(AlarmConstants.ALARM_LIGHT_SCREEN            , getScreen());
        editor.putInt    (AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS , getScreenBrightness());
        editor.putInt    (AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME , getScreenStartTime());
        editor.putInt    (AlarmConstants.ALARM_LIGHT_SCREEN_START_TEMP , getScreenStartTemp());
        editor.putInt    (AlarmConstants.ALARM_LIGHT_COLOR1            , getLightColor1());
        editor.putInt    (AlarmConstants.ALARM_LIGHT_COLOR2            , getLightColor2());
        editor.putBoolean(AlarmConstants.ALARM_LIGHT_FADECOLOR         , getLightFade());
        editor.putBoolean(AlarmConstants.ALARM_LIGHT_USELED            , getLED());
        editor.putInt    (AlarmConstants.ALARM_LIGHT_LED_START_TIME    , getLEDStartTime());
        editor.putInt    (AlarmConstants.ALARM_LIGHT_LED_START_TEMP    , getLEDStartTemp());

        //apply Values to settings
        editor.apply();
    }

    private AlarmManage createAlarmManager(){
            return new AlarmManage(m_Context, this);
    }
    boolean cancelAlarm(){
        return createAlarmManager().cancelAlarm();
    }
    boolean snoozeAlarm(){
        return createAlarmManager().setAlarm(true);
    }
    boolean activateAlarm(){
        return createAlarmManager().setAlarm(false);
    }
    boolean refreshAlarm(){
        return createAlarmManager().refresh();
    }

    //Enums
    enum childItem{
        WAKEUP_DELETE,
        WAKEUP_TIME,
        WAKEUP_DAYS,
        WAKEUP_MUSIC,
        WAKEUP_LIGHT
    }
    private final int childItems = childItem.values().length;
    int getChildItemSize(){
        return childItems;
    }

    //Name
    private String actualAlarmname = AlarmConstants.ALARM;
    String getAlarmName(){
        return actualAlarmname;
    }
    void setAlarmName(final String name){
        actualAlarmname = name;
    }

    //Actual Alarm Values
    int getAlarmID(){
        return actualAlarm;
    }
    void setAlarmID(final int id){
        actualAlarm = id;
    }

    //Actual Alarm Set
    boolean isAlarmSet(){
        return createAlarmManager().checkPendingIntent();
    }

    //Time
    int getHour(){
        return m_Hour;
    }
    int getMinute(){
        return m_Minutes;
    }
    int getSnooze(){
        return m_SnoozeTime;
    }

    void setHour(int hour){
        m_Hour = hour;
    }
    void setMinute(int minute){
        m_Minutes    = minute;
    }
    void setSnooze(int snooze){
        m_SnoozeTime = snooze;
    }

    //Days
    boolean isDaySet(){
        return (isMonday()|| isTuesday() || isWednesday() || isThursday() || isFriday() || isSaturday() || isSunday());
    }
    boolean isDaySet(int day){
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

    int getTimeToNextDay(){

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
    boolean isMonday(){
        return m_MondaySet;
    }
    boolean isTuesday(){
        return m_TuesdaySet;
    }
    boolean isWednesday(){
        return m_WednesdaySet;
    }
    boolean isThursday(){
        return m_ThursdaySet;
    }
    boolean isFriday(){
        return m_FridaySet;
    }
    boolean isSaturday(){
        return m_SaturdaySet;
    }
    boolean isSunday(){
        return m_SundaySet;
    }

    void setMonday(boolean monday){
        m_MondaySet = monday;
    }
    void setTuesday(boolean tuesday){
        m_TuesdaySet = tuesday;
    }
    void setWednesday(boolean wednesday){
        m_WednesdaySet = wednesday;
    }
    void setThursday(boolean thursday){
        m_ThursdaySet = thursday;
    }
    void setFriday(boolean friday){
        m_FridaySet = friday;
    }
    void setSaturday(boolean saturday){
        m_SaturdaySet = saturday;
    }
    void setSunday(boolean sunday){
        m_SundaySet = sunday;
    }

    //Music
    String getSongName(){
        return m_SongURI.substring(m_SongURI.lastIndexOf('/') + 1);
    }
    String getSongURI(){
        return m_SongURI;
    }
    void setSongURI(String uri){
        m_SongURI = uri;
    }

    int getSongStart(){
        return m_SongStart;
    }
    void setSongStart(int start){
        m_SongStart = start;
    }

    int getSongLength(){
        return m_SongLength;
    }
    void setSongLength(int length){
        m_SongLength = length;
    }

    int getVolume(){
        return m_Volume;
    }
    void setVolume(int volume){
        m_Volume = volume;
    }

    boolean getFadeIn(){
        return m_FadeInSet;
    }
    void setFadeIn(boolean fadein){
        m_FadeInSet = fadein;
    }

    int getFadeInTime(){
        return m_FadeInTime;
    }
    void setFadeInTime(int time){
        m_FadeInTime = time;
    }

    boolean getVibration(){
        return m_VibraSet;
    }
    void setVibration(boolean vibration){
        m_VibraSet = vibration;
    }

    int getVibrationStrength(){
        return m_VibraStrength;
    }
    void setVibrationStrength(int strength){
        m_VibraStrength = strength;
    }

    //Screen
    boolean getScreen(){
        return m_ScreenSet;
    }
    void setScreen(boolean screen){
        m_ScreenSet = screen;
    }

    int getScreenBrightness(){
        return m_ScreenBrightness;
    }
    void setScreenBrightness(int brightness){
        m_ScreenBrightness = brightness;
    }

    int getScreenStartTime(){
        return m_ScreenStartTime;
    }
    void setScreenStartTime(int time){
        m_ScreenStartTime = time;
    }
    int getScreenStartTemp(){
        return m_ScreenStartTimeTemp;
    }
    void setScreenStartTemp(int time){
        m_ScreenStartTimeTemp = time;
    }

    //Light
    int getLightColor1(){
        return m_LightColor1;
    }
    void setLightColor1(int color){
        m_LightColor1 = color;
    }

    int getLightColor2(){
        return m_LightColor2;
    }
    void setLightColor2(int color){
        m_LightColor2 = color;
    }

    boolean getLightFade(){
        return m_LightFade ;
    }
    void setLightFade(boolean fade){
        m_LightFade = fade;
    }

    //LED
    boolean getLED(){
        return m_LightLEDSet;
    }
    void setLED(boolean led){
        m_LightLEDSet = led;
    }

    int getLEDStartTime(){
        return m_LightLEDStartTime;
    }
    void setLEDStartTime(int time){
        m_LightLEDStartTime = time;
    }
    int getLEDStartTemp(){
        return m_LightLEDStartTimeTemp;
    }
    void setLEDStartTemp(int time){
        m_LightLEDStartTimeTemp = time;
    }

    void setTemporaryTimes(boolean temp){
        tempTimes = temp;
    }
    boolean getTemporaryTimes(){
        return tempTimes;
    }

    void setAlarmOneShot(boolean shot){
        alarmOneShot = shot;
    }
    boolean getAlarmOneShot(){
        return alarmOneShot;
    }
}