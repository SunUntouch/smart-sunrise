package com.zhun.sununtouch.smart_sunrise;

import android.graphics.Color;
import android.provider.Settings;

import java.util.Calendar;

public abstract class AlarmConstants {

    //Shared Pref Settings
    public final static String WAKEUP_TIMER            = "com.zhun.sununtouch.smart_sunrise.WAKEUP_SETTINGS";
    public final static String WAKEUP_TIMER_INFO       = "com.zhun.sununtouch.smart_sunrise.WAKEUP_SETTINGS_INFO";

    //Alarm
    public final static String ALARM                      = "Alarm";
    public final static String ALARM_ID                   = "Alarm_ID";
    public final static String ALARM_NAME                 = "Alarm_Name";
    public final static String ALARM_VALUE                = "Alarm_Value";

    public final static String ALARM_TIME_SET             = "Alarm_isSet";

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
    public static final String ALARM_MUSIC_SONGLENGTH     = "Alarm_SongLength";
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


    //StartValues
    static Calendar calendar = Calendar.getInstance();

    //AlarmSet
    public final static boolean ALARM_IS_SET   = false;
    //Time
    public final static int ACTUAL_TIME_HOUR   = calendar.get(Calendar.HOUR_OF_DAY);
    public final static int ACTUAL_TIME_MINUTE = calendar.get(Calendar.MINUTE);
    public final static int ACTUAL_TIME_SNOOZE = 10;

    //Days
    public final static int ACTUAL_DAY_MONDAY    = 0;
    public final static int ACTUAL_DAY_TUESDAY   = 0;
    public final static int ACTUAL_DAY_WEDNESDAY = 0;
    public final static int ACTUAL_DAY_THURSDAY  = 0;
    public final static int ACTUAL_DAY_FRIDAY    = 0;
    public final static int ACTUAL_DAY_SATURDAY  = 0;
    public final static int ACTUAL_DAY_SUNDAY    = 0;

    //Music
    public final static String ACTUAL_MUSIC_SONG_URI = Settings.System.DEFAULT_ALARM_ALERT_URI.getPath();
    public final static int ACTUAL_MUSIC_START              = 0;
    public final static int ACTUAL_MUSIC_LENGTH             = 1;
    public final static int ACTUAL_MUSIC_VOLUME             = 100;
    public final static int ACTUAL_MUSIC_FADE_IN            = 0;
    public final static int ACTUAL_MUSIC_FADE_IN_TIME       = 0;
    public final static int ACTUAL_MUSIC_VIBRATION          = 0;
    public final static int ACTUAL_MUSIC_VIBRATION_STRENGTH = 100;

    //Light
    public final static int ACTUAL_SCREEN            = 1;
    public final static int ACTUAL_SCREEN_BRIGHTNESS = 99;
    public final static int ACTUAL_SCREEN_START      = 30;
    public final static int ACTUAL_SCREEN_COLOR1     = Color.RED;
    public final static int ACTUAL_SCREEN_COLOR2     = Color.BLUE;
    public final static int ACTUAL_SCREEN_COLOR_FADE = 1;
    public final static int ACTUAL_LED               = 0;
    public final static int ACTUAL_LED_START         = 10;

    //Alarm Types
    public final static int TYPE_TIME  = 0;
    public final static int TYPE_MUSIC = 1;
    public final static int TYPE_DAYS  = 2;
    public final static int TYPE_LIGHT = 3;
}
