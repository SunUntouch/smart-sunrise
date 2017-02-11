package com.zhun.sununtouch.smart_sunrise.Information;

import android.graphics.Color;
import android.provider.Settings;

import java.text.DateFormatSymbols;
import java.util.Calendar;

/**
 * Created by Sunny
 * Model Class to provide Constant Values
 */

public abstract class AlarmConstants {

    //Shared Pref Settings
    @SuppressWarnings("unused")
    public final static String WAKEUP_TIMER                   = "com.zhun.sununtouch.smart_sunrise.WAKEUP_SETTINGS";
    public final static String WAKEUP_OPTIONS                 = "com.zhun.sununtouch.smart_sunrise.WAKEUP_OPTIONS";
    public final static String WAKEUP_OPTIONS_BRIGHTNESSSTEPS = "WAKEUP_OPTIONS_BRIGHTNESSSTEPS";
    public final static String WAKEUP_OPTIONS_THEME           = "WAKEUP_OPTIONS_THEME";
    public final static String WAKEUP_OPTIONS_LOGGING         = "WAKEUP_OPTIONS_LOGGING";

    //Threads
    public final static String ACTIVITY_DATE_THREAD      = "com.zhun.sununtouch.smart_sunrise.AlarmActivity.DATE_THREAD";
    public final static String ACTIVITY_MUSIC_THREAD     = "com.zhun.sununtouch.smart_sunrise.AlarmActivity.MUSIC_THREAD";
    public final static String ACTIVITY_VIBRATION_THREAD = "com.zhun.sununtouch.smart_sunrise.AlarmActivity.VIBRATION_THREAD";

    //Alarm
    public final static String ALARM                      = "Alarm";
    public final static String ALARM_ID                   = "Alarm_ID";
    public final static String ALARM_NAME                 = "Alarm_Name";
    public final static String ALARM_VALUE                = "Alarm_Value";

    public final static String ALARM_ONESHOT              = "Alarm_One_Shot";
    public final static String ALARM_TEMPORARY            = "Alarm_Temporary";

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
    public final static String ALARM_MUSIC_SONG_ID        = "Alarm_SongID";
    public static final String ALARM_MUSIC_SONG_LENGTH    = "Alarm_SongLength";
    public final static String ALARM_MUSIC_SONG_START     = "Alarm_SongStart";
    public final static String ALARM_MUSIC_VOLUME         = "Alarm_Volume";
    public final static String ALARM_MUSIC_FADE_IN        = "Alarm_FadeIn";
    public final static String ALARM_MUSIC_FADE_IN_TIME   = "Alarm_FadeTime";

    public final static String ALARM_MUSIC_VIBRATION_ACTIVE = "Alarm_Vibration_Active";
    public final static String ALARM_MUSIC_VIBRATION_VALUE  = "Alarm_Vibration_Value";

    //Alarm Light
    public final static String ALARM_LIGHT_SCREEN            = "Alarm_Screen";
    public final static String ALARM_LIGHT_SCREEN_BRIGHTNESS = "Alarm_ScreenBrightness";
    public final static String ALARM_LIGHT_SCREEN_START_TIME = "Alarm_ScreenStartTime";
    public final static String ALARM_LIGHT_SCREEN_START_TEMP = "Alarm_ScreenStartTemp";
    public final static String ALARM_LIGHT_COLOR1            = "Alarm_ScreenColor1";
    public final static String ALARM_LIGHT_COLOR2            = "Alarm_ScreenColor2";
    public final static String ALARM_LIGHT_FADE_COLOR        = "Alarm_FadeColor";
    public final static String ALARM_LIGHT_USE_LED           = "Alarm_UseLED";
    public final static String ALARM_LIGHT_LED_START_TIME    = "Alarm_LEDStartTime";
    public final static String ALARM_LIGHT_LED_START_TEMP    = "Alarm_LEDStartTemp";

    //StartValues
    public final static String[] SHORT_DAYS = DateFormatSymbols.getInstance().getShortWeekdays();
    public final static String[] LONG_DAYS  = DateFormatSymbols.getInstance().getWeekdays();

    //AlarmSet
    public final static boolean ACTUAL_ONESHOT   = false;
    public final static boolean ACTUAL_TEMPORARY = false;

    //Time
    private final static Calendar calendar = Calendar.getInstance();
    public final static int ACTUAL_TIME_HOUR   = calendar.get(Calendar.HOUR_OF_DAY);
    public final static int ACTUAL_TIME_MINUTE = calendar.get(Calendar.MINUTE);
    public final static int ACTUAL_TIME_SNOOZE = 10;

    //Days
    public final static boolean ACTUAL_DAY_MONDAY    = false;
    public final static boolean ACTUAL_DAY_TUESDAY   = false;
    public final static boolean ACTUAL_DAY_WEDNESDAY = false;
    public final static boolean ACTUAL_DAY_THURSDAY  = false;
    public final static boolean ACTUAL_DAY_FRIDAY    = false;
    public final static boolean ACTUAL_DAY_SATURDAY  = false;
    public final static boolean ACTUAL_DAY_SUNDAY    = false;

    //Music
    public final static String ACTUAL_MUSIC_SONG_URI = Settings.System.DEFAULT_ALARM_ALERT_URI.getPath();
    public final static int ACTUAL_MUSIC_START              = 0;
    public final static int ACTUAL_MUSIC_LENGTH             = 1;
    public final static int ACTUAL_MUSIC_VOLUME             = 100;

    public final static boolean ACTUAL_MUSIC_FADE_IN        = false;
    public final static int ACTUAL_MUSIC_FADE_IN_TIME       = 0;

    public final static boolean ACTUAL_MUSIC_VIBRATION      = false;
    public final static int ACTUAL_MUSIC_VIBRATION_STRENGTH = 100;

    //Light
    public final static boolean  ACTUAL_SCREEN           = true;
    public final static boolean ACTUAL_SCREEN_COLOR_FADE = true;

    public final static int ACTUAL_SCREEN_BRIGHTNESS = 99;
    public final static int ACTUAL_SCREEN_START      = 30;
    public final static int ACTUAL_SCREEN_COLOR1     = Color.RED;
    public final static int ACTUAL_SCREEN_COLOR2     = Color.BLUE;

    public final static boolean ACTUAL_LED           = false;
    public final static int ACTUAL_LED_START         = 0;

    public final static int ALARM_PERMISSION_MUSIC = 0;

    //Options
    public  final static int BRIGHTNESS_STEPS_MINIMUM = 5;
    public  final static int BRIGHTNESS_STEPS  = 100;
    public  final static boolean ALARM_LOGGING = false;
}
