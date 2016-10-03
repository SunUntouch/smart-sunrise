package com.zhun.sununtouch.smart_sunrise;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Vector;

/**
 * Created by Sunny on 18.09.2016.
 */
class AlarmConfiguration {

    //Actual Alarm Values
    private int actualAlarm    =-1;

    //Actual Alarm Set
    private boolean alarmSet   = AlarmConstants.ALARM_IS_SET;

    //Time
    private int actualHour     = AlarmConstants.ACTUAL_TIME_HOUR;
    private int actualMin      = AlarmConstants.ACTUAL_TIME_MINUTE;
    private int actualSnooze   = AlarmConstants.ACTUAL_TIME_SNOOZE;

    //Days
    private int isMonday    = AlarmConstants.ACTUAL_DAY_MONDAY;
    private int isTuesday   = AlarmConstants.ACTUAL_DAY_TUESDAY;
    private int isWednesday = AlarmConstants.ACTUAL_DAY_WEDNESDAY;
    private int isThursday  = AlarmConstants.ACTUAL_DAY_THURSDAY;
    private int isFriday    = AlarmConstants.ACTUAL_DAY_FRIDAY;
    private int isSaturday  = AlarmConstants.ACTUAL_DAY_SATURDAY;
    private int isSunday    = AlarmConstants.ACTUAL_DAY_SUNDAY;

    //Music
    private String actualSongURI = AlarmConstants.ACTUAL_MUSIC_SONG_URI;
    private int actualSongStart  = AlarmConstants.ACTUAL_MUSIC_START;
    private int actualSongLength = AlarmConstants.ACTUAL_MUSIC_LENGTH;
    private int actualVolume     = AlarmConstants.ACTUAL_MUSIC_VOLUME;
    private int actualFadeIn     = AlarmConstants.ACTUAL_MUSIC_FADE_IN;
    private int actualFadeInTime = AlarmConstants.ACTUAL_MUSIC_FADE_IN_TIME;
    private int actualVibra      = AlarmConstants.ACTUAL_MUSIC_VIBRATION;
    private int actualVibraStr   = AlarmConstants.ACTUAL_MUSIC_VIBRATION_STRENGTH;

    //Light
    private int actualScreen           = AlarmConstants.ACTUAL_SCREEN;
    private int actualScreenBrightness = AlarmConstants.ACTUAL_SCREEN_BRIGHTNESS;
    private int actualScreenStartTime  = AlarmConstants.ACTUAL_SCREEN_START;
    private int actualLightColor1      = AlarmConstants.ACTUAL_SCREEN_COLOR1;
    private int actualLightColor2      = AlarmConstants.ACTUAL_SCREEN_COLOR2;
    private int actualLightFade        = AlarmConstants.ACTUAL_SCREEN_COLOR_FADE;
    private int actualLightLED         = AlarmConstants.ACTUAL_LED;
    private int actualLightLEDStartTime= AlarmConstants.ACTUAL_LED_START;

    AlarmConfiguration(){
    }
    AlarmConfiguration(Context context, int ID){

        //save Settings
        SharedPreferences settings = AlarmSharedPreferences.getSharedPreference(context, AlarmConstants.WAKEUP_TIMER, ID);

        //ID, name, AlarmSet
        this.setAlarmID(ID);
        this.setAlarmName(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM + Integer.toString(ID)));
        this.setAlarm(settings.getBoolean(AlarmConstants.ALARM_TIME_SET, false));

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
    }
    AlarmConfiguration(SharedPreferences settings, int ID){

        //ID, name, AlarmSet
        this.setAlarmID(ID);
        this.setAlarmName(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM + Integer.toString(ID)));
        this.setAlarm(settings.getBoolean(AlarmConstants.ALARM_TIME_SET, false));

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
        return actualHour;
    }
    int getMinute(){
        return actualMin;
    }
    int getSnooze(){
        return actualSnooze;
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
        actualHour   = hour;
    }
    void setMinute(int minute){
        actualMin    = minute;
    }
    void setSnooze(int snooze){
        actualSnooze = snooze;
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
        return isMonday;
    }
    int isTuesday(){
        return isTuesday;
    }
    int isWednesday(){
        return isWednesday;
    }
    int isThursday(){
        return isThursday;
    }
    int isFriday(){
        return isFriday;
    }
    int isSaturday(){
        return isSaturday;
    }
    int isSunday(){
        return isSunday;
    }

    boolean Monday(){
        return isMonday == 1;
    }
    boolean Tuesday(){
        return isTuesday == 1;
    }
    boolean Wednesday(){
        return isWednesday == 1;
    }
    boolean Thursday(){
        return isThursday == 1;
    }
    boolean Friday(){
        return isFriday == 1;
    }
    boolean Saturday(){
        return isSaturday == 1;
    }
    boolean Sunday(){ return isSunday == 1;
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
        isMonday = monday;
    }
    void setTuesday(int tuesday){
        isTuesday = tuesday;
    }
    void setWednesday(int wednesday){
        isWednesday = wednesday;
    }
    void setThursday(int thursday){
        isThursday = thursday;
    }
    void setFriday(int friday){
        isFriday = friday;
    }
    void setSaturday(int saturday){
        isSaturday = saturday;
    }
    void setSunday(int sunday){
        isSunday = sunday;
    }

    //Music
    String getSongName(){
        return actualSongURI.substring(actualSongURI.lastIndexOf('/') + 1);
    }
    String getSongURI(){
        return actualSongURI;
    }
    void setSongURI(String uri){
        actualSongURI = uri;
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
        return actualSongStart;
    }
    void setSongStart(int start){
        actualSongStart = start;
    }

    int getSongLength(){
        return actualSongLength;
    }
    void setSongLength(int length){
        actualSongLength = length;
    }

    int getVolume(){
        return actualVolume;
    }
    void setVolume(int volume){
        actualVolume = volume;
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
        return actualFadeIn == 1;
    }
    int getFadeIn(){
        return actualFadeIn;
    }
    void setFadeIn(int fadein){
        actualFadeIn = fadein;
    }

    int getFadeInTime(){
        return actualFadeInTime;
    }
    void setFadeInTime(int time){
        actualFadeInTime = time;
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
        return actualVibra == 1;
    }
    int getVibration(){
        return actualVibra;
    }
    void setVibration(int vibration){
        actualVibra = vibration;
    }

    int getVibrationStrength(){
        return actualVibraStr;
    }
    void setVibrationStrength(int strength){
        actualVibraStr = strength;
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
        return actualScreen;
    }
    boolean useScreen(){
        return actualScreen == 1;
    }
    void setScreen(int screen){
        actualScreen = screen;
    }

    int getScreenBrightness(){
        return actualScreenBrightness;
    }
    void setScreenBrightness(int brightness){
        actualScreenBrightness = brightness;
    }

    int getScreenStartTime(){
        return actualScreenStartTime;
    }
    void setScreenStartTime(int time){
        actualScreenStartTime = time;
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
        return actualLightColor1;
    }
    void setLightColor1(int color){
        actualLightColor1 = color;
    }

    int getLightColor2(){
        return actualLightColor2;
    }
    void setLightColor2(int color){
        actualLightColor2 = color;
    }

    int getLightFade(){
        return actualLightFade;
    }
    void setLightFade(int fade){
        actualLightFade = fade;
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
        return actualLightLED == 1;
    }
    int getLED(){
        return actualLightLED;
    }
    void setLED(int led){
        actualLightLED = led;
    }

    int getLEDStartTime(){
        return actualLightLEDStartTime;
    }
    void setLEDStartTime(int time){
        actualLightLEDStartTime = time;
    }
}
