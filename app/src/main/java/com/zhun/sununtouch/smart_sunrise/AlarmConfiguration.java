package com.zhun.sununtouch.smart_sunrise;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Sunny on 18.09.2016.
 */
class AlarmConfiguration {

    Context m_Context;

    //Actual Alarm Values
    private int actualAlarm    = 0;

    //Actual Alarm Set
    private boolean alarmSet   = AlarmConstants.ALARM_IS_SET;

    //Time
    private int m_Hour       = AlarmConstants.ACTUAL_TIME_HOUR;
    private int m_Minutes    = AlarmConstants.ACTUAL_TIME_MINUTE;
    private int m_SnoozeTime = AlarmConstants.ACTUAL_TIME_SNOOZE;

    //Days
    private int m_MondaySet    = AlarmConstants.ACTUAL_DAY_MONDAY;
    private int m_TuesdaySet   = AlarmConstants.ACTUAL_DAY_TUESDAY;
    private int m_WednesdaySet = AlarmConstants.ACTUAL_DAY_WEDNESDAY;
    private int m_ThursdaySet  = AlarmConstants.ACTUAL_DAY_THURSDAY;
    private int m_FridaySet    = AlarmConstants.ACTUAL_DAY_FRIDAY;
    private int m_SaturdaySet  = AlarmConstants.ACTUAL_DAY_SATURDAY;
    private int m_SundaySet    = AlarmConstants.ACTUAL_DAY_SUNDAY;

    //Music
    private String m_SongURI    = AlarmConstants.ACTUAL_MUSIC_SONG_URI;
    private int m_SongStart     = AlarmConstants.ACTUAL_MUSIC_START;
    private int m_SongLength    = AlarmConstants.ACTUAL_MUSIC_LENGTH;
    private int m_Volume        = AlarmConstants.ACTUAL_MUSIC_VOLUME;
    private int m_FadeInSet     = AlarmConstants.ACTUAL_MUSIC_FADE_IN;
    private int m_FadeInTime    = AlarmConstants.ACTUAL_MUSIC_FADE_IN_TIME;
    private int m_VibraSet      = AlarmConstants.ACTUAL_MUSIC_VIBRATION;
    private int m_VibraStrength = AlarmConstants.ACTUAL_MUSIC_VIBRATION_STRENGTH;

    //Light
    private int m_ScreenSet             = AlarmConstants.ACTUAL_SCREEN;
    private int m_ScreenBrightness      = AlarmConstants.ACTUAL_SCREEN_BRIGHTNESS;
    private int m_ScreenStartTime       = AlarmConstants.ACTUAL_SCREEN_START;
    private int m_ScreenStartTimeTemp   = AlarmConstants.ACTUAL_SCREEN_START;
    private int m_LightColor1           = AlarmConstants.ACTUAL_SCREEN_COLOR1;
    private int m_LightColor2           = AlarmConstants.ACTUAL_SCREEN_COLOR2;
    private int m_LightFade             = AlarmConstants.ACTUAL_SCREEN_COLOR_FADE;
    private int m_LightLEDSet           = AlarmConstants.ACTUAL_LED;
    private int m_LightLEDStartTime     = AlarmConstants.ACTUAL_LED_START;
    private int m_LightLEDStartTimeTemp = AlarmConstants.ACTUAL_LED_START;

    //dirty flag
    private boolean m_Dirty = true;

    private boolean m_AlarmManager = false;

    AlarmConfiguration(Context context){
        m_Context = context;
    }
    AlarmConfiguration(Context context, int ID){

        m_Context = context;

        //save Settings
        SharedPreferences settings = AlarmSharedPreferences.getSharedPreference(context, AlarmConstants.ALARM, ID);

        //ID, name, AlarmSet
        this.setAlarmID(ID);
        this.setAlarmName(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM + Integer.toString(ID)));
        this.setAlarm(settings.getBoolean(AlarmConstants.ALARM_TIME_SET, false));

        this.m_AlarmManager = settings.getBoolean(AlarmConstants.ALARM_MANAGER, false);

        //Days
        this.setMonday   (settings.getInt(AlarmConstants.ALARM_DAY_MONDAY   , AlarmConstants.ACTUAL_DAY_MONDAY));
        this.setTuesday  (settings.getInt(AlarmConstants.ALARM_DAY_TUESDAY  , AlarmConstants.ACTUAL_DAY_TUESDAY));
        this.setWednesday(settings.getInt(AlarmConstants.ALARM_DAY_WEDNESDAY, AlarmConstants.ACTUAL_DAY_WEDNESDAY));
        this.setThursday (settings.getInt(AlarmConstants.ALARM_DAY_THURSDAY , AlarmConstants.ACTUAL_DAY_THURSDAY));
        this.setFriday   (settings.getInt(AlarmConstants.ALARM_DAY_FRIDAY   , AlarmConstants.ACTUAL_DAY_FRIDAY));
        this.setSaturday (settings.getInt(AlarmConstants.ALARM_DAY_SATURDAY , AlarmConstants.ACTUAL_DAY_SATURDAY));
        this.setSunday   (settings.getInt(AlarmConstants.ALARM_DAY_SUNDAY   , AlarmConstants.ACTUAL_DAY_SUNDAY)); // Monday - Sunday

        //Load Music
        this.setSongURI          (settings.getString(AlarmConstants.ALARM_MUSIC_SONGID      , AlarmConstants.ACTUAL_MUSIC_SONG_URI));
        this.setSongStart        (settings.getInt(AlarmConstants.ALARM_MUSIC_SONGSTART      , AlarmConstants.ACTUAL_MUSIC_START));
        this.setSongLength       (settings.getInt(AlarmConstants.ALARM_MUSIC_SONGLENGTH     , AlarmConstants.ACTUAL_MUSIC_LENGTH));
        this.setVolume           (settings.getInt(AlarmConstants.ALARM_MUSIC_VOLUME         , AlarmConstants.ACTUAL_MUSIC_VOLUME));
        this.setFadeIn           (settings.getInt(AlarmConstants.ALARM_MUSIC_FADEIN         , AlarmConstants.ACTUAL_MUSIC_FADE_IN));
        this.setFadeInTime       (settings.getInt(AlarmConstants.ALARM_MUSIC_FADEINTIME     , AlarmConstants.ACTUAL_MUSIC_FADE_IN_TIME));
        this.setVibration        (settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV, AlarmConstants.ACTUAL_MUSIC_VIBRATION));
        this.setVibrationStrength(settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE, AlarmConstants.ACTUAL_MUSIC_VIBRATION_STRENGTH));// Song, StartTime, Volume, FadIn, FadeInTime

        //Load Light
        this.setScreen          (settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN           , AlarmConstants.ACTUAL_SCREEN));
        this.setScreenBrightness(settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS, AlarmConstants.ACTUAL_SCREEN_BRIGHTNESS));
        this.setScreenStartTime (settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME, AlarmConstants.ACTUAL_SCREEN_START));

        this.setLightColor1(settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR1   , AlarmConstants.ACTUAL_SCREEN_COLOR1));
        this.setLightColor2(settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR2   , AlarmConstants.ACTUAL_SCREEN_COLOR2));
        this.setLightFade  (settings.getInt(AlarmConstants.ALARM_LIGHT_FADECOLOR, AlarmConstants.ACTUAL_SCREEN_COLOR_FADE));

        this.setLED         (settings.getInt(AlarmConstants.ALARM_LIGHT_USELED        , AlarmConstants.ACTUAL_LED));
        this.setLEDStartTime(settings.getInt(AlarmConstants.ALARM_LIGHT_LED_START_TIME, AlarmConstants.ACTUAL_LED_START));// UseScreen, ScreenColor1, ScreenColor2, Fadecolor, FadeTime, UseLED

        //Time
        Calendar calendar = Calendar.getInstance();
        this.setHour  (settings.getInt(AlarmConstants.ALARM_TIME_HOUR   , calendar.get(Calendar.HOUR_OF_DAY)));
        this.setMinute(settings.getInt(AlarmConstants.ALARM_TIME_MINUTES, calendar.get(Calendar.MINUTE)));
        this.setSnooze(settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE , AlarmConstants.ACTUAL_TIME_SNOOZE));    // hour, minute, snooze

        //Dirty
        this.m_Dirty = false;
    }

    public void commit(){

        //Load sharedPreferences
        SharedPreferences.Editor editor = AlarmSharedPreferences.getSharedPreferenceEditor(m_Context, AlarmConstants.ALARM, getAlarmID()).clear();

        //put StringSet back
        editor.putString(AlarmConstants.ALARM_NAME, getAlarmName());

        //AlarmManager active
        editor.putBoolean(AlarmConstants.ALARM_MANAGER, m_AlarmManager);

        //Alarm is Set
        editor.putBoolean(AlarmConstants.ALARM_TIME_SET, isAlarmSet());

        //Time
        editor.putInt(AlarmConstants.ALARM_TIME_MINUTES  , getMinute());
        editor.putInt(AlarmConstants.ALARM_TIME_HOUR     , getHour());
        editor.putInt(AlarmConstants.ALARM_TIME_SNOOZE   , getSnooze());

        //Days
        editor.putInt(AlarmConstants.ALARM_DAY_MONDAY    , isMonday());
        editor.putInt(AlarmConstants.ALARM_DAY_TUESDAY   , isTuesday());
        editor.putInt(AlarmConstants.ALARM_DAY_WEDNESDAY , isWednesday());
        editor.putInt(AlarmConstants.ALARM_DAY_THURSDAY  , isThursday());
        editor.putInt(AlarmConstants.ALARM_DAY_FRIDAY    , isFriday());
        editor.putInt(AlarmConstants.ALARM_DAY_SATURDAY  , isSaturday());
        editor.putInt(AlarmConstants.ALARM_DAY_SUNDAY    , isSunday());

        //Music
        editor.putString(AlarmConstants.ALARM_MUSIC_SONGID       , getSongURI());
        editor.putInt(AlarmConstants.ALARM_MUSIC_VOLUME          , getVolume());
        editor.putInt(AlarmConstants.ALARM_MUSIC_SONGSTART       , getSongStart());
        editor.putInt(AlarmConstants.ALARM_MUSIC_SONGLENGTH      , getSongLength());
        editor.putInt(AlarmConstants.ALARM_MUSIC_FADEIN          , getFadeIn());
        editor.putInt(AlarmConstants.ALARM_MUSIC_FADEINTIME      , getFadeInTime());
        editor.putInt(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV , getVibration());
        editor.putInt(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE , getVibrationStrength());

        //Light
        editor.putInt(AlarmConstants.ALARM_LIGHT_SCREEN            , getScreen());
        editor.putInt(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS , getScreenBrightness());
        editor.putInt(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME , getScreenStartTime());
        editor.putInt(AlarmConstants.ALARM_LIGHT_COLOR1            , getLightColor1());
        editor.putInt(AlarmConstants.ALARM_LIGHT_COLOR2            , getLightColor2());
        editor.putInt(AlarmConstants.ALARM_LIGHT_FADECOLOR         , getLightFade());
        editor.putInt(AlarmConstants.ALARM_LIGHT_USELED            , getLED());
        editor.putInt(AlarmConstants.ALARM_LIGHT_LED_START_TIME    , getLEDStartTime());

        //apply Values to settings
        editor.apply();
    }

    public void removeSharedPreference(int amount){

        //Copy Data to fill AlarmCount Gap
        if( amount > 0) {
            --amount;
            for (int id = actualAlarm; id < amount; ++id) {
                SharedPreferences sharedPrefs = AlarmSharedPreferences.getSharedPreference(m_Context, AlarmConstants.WAKEUP_TIMER, id++); //TODO check if thats right whith double ++id
                SharedPreferences.Editor editorNew = sharedPrefs.edit();
                SharedPreferences settingsOld = AlarmSharedPreferences.getSharedPreference(m_Context, AlarmConstants.WAKEUP_TIMER, id);

                Map<String, ?> settingOld = settingsOld.getAll();
                for (Map.Entry<String, ?> value : settingOld.entrySet()) {
                    if (value.getValue().getClass().equals(Boolean.class))
                        editorNew.putBoolean(value.getKey(), (Boolean) value.getValue());
                    else if (value.getValue().getClass().equals(Float.class))
                        editorNew.putFloat(value.getKey(), (Float) value.getValue());
                    else if (value.getValue().getClass().equals(Integer.class))
                        editorNew.putInt(value.getKey(), (Integer) value.getValue());
                    else if (value.getValue().getClass().equals(Long.class))
                        editorNew.putLong(value.getKey(), (Long) value.getValue());
                    else if (value.getValue().getClass().equals(String.class))
                        editorNew.putString(value.getKey(), (String) value.getValue());
                }
                editorNew.apply();
            }
        }

        //Clear Old Entry
        SharedPreferences.Editor editor = AlarmSharedPreferences.getSharedPreference(m_Context, AlarmConstants.WAKEUP_TIMER, amount).edit();
        editor.clear();
        editor.apply();
    }

    //Get and Setter


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
    void setAlarmName(String name){
        actualAlarmname = name;
    }

    //Actual Alarm Values
    int getAlarmID(){
        return actualAlarm;
    }
    void setAlarmID(int id){
        actualAlarm = id;
    }

    //Actual Alarm Set
    boolean isAlarmSet(){
        return alarmSet;
    }
    void setAlarm(boolean alarm){
        alarmSet = alarm;
    }

    //Time
    Vector<Integer> getTime(){
        Vector<Integer> time = new Vector<>(3);
        time.addElement(getHour());
        time.addElement(getMinute());
        time.addElement(getSnooze());

        return time;
    }
    int getHour(){
        return m_Hour;
    }
    int getMinute(){
        return m_Minutes;
    }
    int getSnooze(){
        return m_SnoozeTime;
    }

    void setTime(int hour, int minute, int snooze){
        setHour(hour);
        setMinute(minute);
        setSnooze(snooze);
    }
    void setTime(int hour, int minute){
        setHour(hour);
        setMinute(minute);
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
        return (Monday()|| Tuesday() || Wednesday() || Thursday() || Friday() || Saturday() || Sunday());
    }
    boolean isDaySet(int day){

        switch(day)
        {
            case 0: return Monday();
            case 1: return Tuesday();
            case 2: return Wednesday();
            case 3: return Thursday();
            case 4: return Friday();
            case 5: return Saturday();
            case 6: return Sunday();
            default: return false;
        }
    }
    int getDaySet(){
        return isMonday() + isTuesday() + isWednesday() + isThursday() + isFriday() + isSaturday() + isSunday();
    }
    int getTimeToNextDay(int currentDay){

        if(!isDaySet())
            return 0;

        int nextDay = 1;
        if((currentDay == 6) ? isDaySet(0) : isDaySet(++currentDay))
            return nextDay;

        while (!isDaySet(currentDay++))
        {
            ++nextDay;
            if(currentDay == 7)
                currentDay = 0;
        }
        return nextDay;
    }
    Vector<Integer> getDays(){

        Vector<Integer> days = new Vector<>(7);
        days.addElement(isMonday());
        days.addElement(isTuesday());
        days.addElement(isWednesday());
        days.addElement(isThursday());
        days.addElement(isFriday());
        days.addElement(isSaturday());
        days.addElement(isSunday());
        return days;
    }
    int isMonday(){
        return m_MondaySet;
    }
    int isTuesday(){
        return m_TuesdaySet;
    }
    int isWednesday(){
        return m_WednesdaySet;
    }
    int isThursday(){
        return m_ThursdaySet;
    }
    int isFriday(){
        return m_FridaySet;
    }
    int isSaturday(){
        return m_SaturdaySet;
    }
    int isSunday(){
        return m_SundaySet;
    }

    boolean Monday(){
        return m_MondaySet == 1;
    }
    boolean Tuesday(){
        return m_TuesdaySet == 1;
    }
    boolean Wednesday(){
        return m_WednesdaySet == 1;
    }
    boolean Thursday(){
        return m_ThursdaySet == 1;
    }
    boolean Friday(){
        return m_FridaySet == 1;
    }
    boolean Saturday(){
        return m_SaturdaySet == 1;
    }
    boolean Sunday(){ return m_SundaySet == 1;
    }

    void setDays(int monday, int tuesday, int wednesday, int thursday, int friday, int saturday, int sunday){
        setMonday(monday);
        setTuesday(tuesday);
        setWednesday(wednesday);
        setThursday(thursday);
        setFriday(friday);
        setSaturday(saturday);
        setSunday(sunday);
    }
    void setMonday(int monday){
        m_MondaySet = monday;
    }
    void setTuesday(int tuesday){
        m_TuesdaySet = tuesday;
    }
    void setWednesday(int wednesday){
        m_WednesdaySet = wednesday;
    }
    void setThursday(int thursday){
        m_ThursdaySet = thursday;
    }
    void setFriday(int friday){
        m_FridaySet = friday;
    }
    void setSaturday(int saturday){
        m_SaturdaySet = saturday;
    }
    void setSunday(int sunday){
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


    Vector<Integer> getSongTimes(){
        Vector<Integer> time = new Vector<>(2);
        time.addElement(getSongLength());
        time.addElement(getSongStart());
        return time;
    }
    void setSongTimes(int length, int start){
        setSongLength(length);
        setSongStart(start);
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

    Vector<Integer> getFadeValues(){
        Vector<Integer> fadeIn = new Vector<>(2);
        fadeIn.addElement(getFadeIn());
        fadeIn.addElement(getFadeInTime());
        return fadeIn;
    }
    void setFadeValues(int fadein, int time){
        setFadeIn(fadein);
        setFadeInTime(time);
    }

    boolean useFadeIn(){
        return m_FadeInSet == 1;
    }
    int getFadeIn(){
        return m_FadeInSet;
    }
    void setFadeIn(int fadein){
        m_FadeInSet = fadein;
    }

    int getFadeInTime(){
        return m_FadeInTime;
    }
    void setFadeInTime(int time){
        m_FadeInTime = time;
    }

    Vector<Integer> getVibrationValues(){
        Vector<Integer> vibration = new Vector<>(2);
        vibration.addElement(getVibration());
        vibration.addElement(getVibrationStrength());
        return vibration;
    }
    void setVibrationValues(int vibration, int strength){
        setVibration(vibration);
        setVibrationStrength(strength);
    }

    boolean useVibration(){
        return m_VibraSet == 1;
    }
    int getVibration(){
        return m_VibraSet;
    }
    void setVibration(int vibration){
        m_VibraSet = vibration;
    }

    int getVibrationStrength(){
        return m_VibraStrength;
    }
    void setVibrationStrength(int strength){
        m_VibraStrength = strength;
    }

    //Screen
    Vector<Integer> getScreenValues(){
        Vector<Integer> screen = new Vector<>(3);
        screen.addElement(getScreen());
        screen.addElement(getScreenBrightness());
        screen.addElement(getScreenStartTime());
        return screen;
    }
    void setScreenValues(int screen, int brightness, int start){
        setScreen(screen);
        setScreenBrightness(brightness);
        setScreenStartTime(start);
    }

    int getScreen(){
        return m_ScreenSet;
    }
    boolean useScreen(){
        return m_ScreenSet == 1;
    }
    void setScreen(int screen){
        m_ScreenSet = screen;
    }

    int getScreenBrightness(){
        return m_ScreenBrightness;
    }
    void setScreenBrightness(int brightness){
        m_ScreenBrightness = brightness;
    }

    int getScreenStartTime(){
        return (m_AlarmManager) ? m_ScreenStartTimeTemp : m_ScreenStartTime;
    }
    void setScreenStartTime(int time){

        if(m_AlarmManager)
            m_ScreenStartTimeTemp = time;

        m_ScreenStartTime = time;
    }

    //Light
    Vector<Integer> getLightValues(){
        Vector<Integer> light = new Vector<>(3);
        light.addElement(getLightFade());
        light.addElement(getLightColor1());
        light.addElement(getLightColor2());
        return light;
    }
    void setLightValues(int fade, int color1, int color2){
        setLightFade(fade);
        setLightColor1(color1);
        setLightColor2(color2);
    }

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

    int getLightFade(){
        return m_LightFade;
    }
    void setLightFade(int fade){
        m_LightFade = fade;
    }

    //LED
    Vector<Integer> getLEDValues(){
        Vector<Integer> led = new Vector<>(2);
        led.addElement(getLED());
        led.addElement(getLEDStartTime());
        return led;
    }
    void setLEDValues(int led, int time){
        setLED(led);
        setLEDStartTime(time);
    }

    boolean useLED(){
        return m_LightLEDSet == 1;
    }
    int getLED(){
        return m_LightLEDSet;
    }
    void setLED(int led){
        m_LightLEDSet = led;
    }

    int getLEDStartTime(){
        return (!m_AlarmManager) ? m_LightLEDStartTime : m_LightLEDStartTimeTemp;
    }
    void setLEDStartTime(int time){

        if(m_AlarmManager)
            m_LightLEDStartTimeTemp  = time;

        m_LightLEDStartTime = time;
    }

    void setFromAlarmManager(){
        m_AlarmManager = true;
    }
    void clearAlarmManagerFlag(){
        m_AlarmManager = false;
    }
}
