package com.zhun.sununtouch.smart_sunrise;

/**
 * Created by Sunny on 27.12.2015.
 */
public abstract class AlarmConstants {

    //Shared Pref Settings
    public final static String WAKEUP_TIMER      = "com.zhun.sununtouch.smart_sunrise.WAKEUP_SETTINGS";
    public final static String WAKEUP_TIMER_INFO = "com.zhun.sununtouch.smart_sunrise.WAKEUP_SETTINGS_INFO";

    //Alarm
    public final static String ALARM                      = "Alarm";
    public final static String ALARM_NAME                 = "Alarm_Name";
    public final static String ALARM_VALUE                = "Alarm_Value";

    //Alarm Time
    public final static String ALARM_TIME_MINUTES         = "Alarm_Minutes";
    public final static String ALARM_TIME_HOUR            = "Alarm_Hour";
    public final static String ALARM_TIME_SNOOZE          = "Alarm_Snooze";

    //Alarm Days
    public final static String ALARM_DAY_MONDAY           = "Alarm_Monday";
    public final static String ALARM_DAY_TUESDAY          = "Alarm_Tuesday";
    public final static String ALARM_DAY_WEDNESDAY        = "Alarm_Wednesday";
    public final static String ALARM_DAY_THURSDAY         = "Alarm_Thursday";
    public final static String ALARM_DAY_FRIDAY           = "Alarm_Friday";
    public final static String ALARM_DAY_SATURDAY         = "Alarm_Saturday";
    public final static String ALARM_DAY_SUNDAY           = "Alarm_Sunday";

    //Alarm Music
    public final static String ALARM_MUSIC_SONGID         = "Alarm_SongID";
    public final static String ALARM_MUSIC_SONGSTART      = "Alarm_SongStart";
    public final static String ALARM_MUSIC_VOLUME         = "Alarm_Volume";
    public final static String ALARM_MUSIC_FADEIN         = "Alarm_FadeIn";
    public final static String ALARM_MUSIC_FADEINTIME     = "Alarm_FadeTime";

    public final static String ALARM_MUSIC_VIBRATION_ACTIV= "Alarm_Vibration_Activ";
    public final static String ALARM_MUSIC_VIBRATION_VALUE= "Alarm_Vibration_Value";

    //Alarm Light
    public final static String ALARM_LIGHT_SCREEN            = "Alarm_Screen";
    public final static String ALARM_LIGHT_SCREEN_BRIGTHNESS = "Alarm_ScreenBrigthness";
    public final static String ALARM_LIGHT_SCREEN_START_TIME = "Alarm_ScreenStartTime";
    public final static String ALARM_LIGHT_COLOR1            = "Alarm_ScreenColor1";
    public final static String ALARM_LIGHT_COLOR2            = "Alarm_ScreenColor2";
    public final static String ALARM_LIGHT_FADECOLOR         = "Alarm_FadeColor";
    public final static String ALARM_LIGHT_USELED            = "Alarm_UseLED";
    public final static String ALARM_LIGHT_LED_START_TIME    = "Alarm_LEDStartTime";

    //ChildItems
    public final static String WAKEUP_DAYS    = "Days";
    public final static String WAKEUP_TIME    = "Time";
    public final static String WAKEUP_MUSIC   = "Music";
    public final static String WAKEUP_LIGHT   = "Light";
    public final static String WAKEUP_DELETE  = "Delete";
}
