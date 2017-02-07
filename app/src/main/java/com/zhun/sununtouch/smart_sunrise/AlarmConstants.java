package com.zhun.sununtouch.smart_sunrise;

import android.graphics.Color;
import android.provider.Settings;

import java.text.DateFormatSymbols;
import java.util.Calendar;

/**
 * Created by Sunny
 * Model Class to provide Constant Values
 */

abstract class AlarmConstants {

    //Shared Pref Settings
    @SuppressWarnings("unused")
    final static String WAKEUP_TIMER            = "com.zhun.sununtouch.smart_sunrise.WAKEUP_SETTINGS";

    //Threads
    final static String ACTIVITY_DATE_THREAD      = "com.zhun.sununtouch.smart_sunrise.AlarmActivity.DATE_THREAD";
    final static String ACTIVITY_MUSIC_THREAD     = "com.zhun.sununtouch.smart_sunrise.AlarmActivity.MUSIC_THREAD";
    final static String ACTIVITY_VIBRATION_THREAD = "com.zhun.sununtouch.smart_sunrise.AlarmActivity.VIBRATION_THREAD";

    //Alarm
    final static String ALARM                      = "Alarm";
    final static String ALARM_ID                   = "Alarm_ID";
    final static String ALARM_NAME                 = "Alarm_Name";
    final static String ALARM_VALUE                = "Alarm_Value";

    final static String ALARM_ONESHOT              = "Alarm_One_Shot";
    final static String ALARM_TEMPORARY            = "Alarm_Temporary";

    //Alarm Time
    final static String ALARM_TIME_MINUTES         = "Alarm_Minutes";
    final static String ALARM_TIME_HOUR            = "Alarm_Hour";
    final static String ALARM_TIME_SNOOZE          = "Alarm_Snooze";

    //Alarm Days
    final static String ALARM_DAY_MONDAY           = "Alarm_Monday";
    final static String ALARM_DAY_TUESDAY          = "Alarm_Tuesday";
    final static String ALARM_DAY_WEDNESDAY        = "Alarm_Wednesday";
    final static String ALARM_DAY_THURSDAY         = "Alarm_Thursday";
    final static String ALARM_DAY_FRIDAY           = "Alarm_Friday";
    final static String ALARM_DAY_SATURDAY         = "Alarm_Saturday";
    final static String ALARM_DAY_SUNDAY           = "Alarm_Sunday";

    //Alarm Music
    final static String ALARM_MUSIC_SONG_ID        = "Alarm_SongID";
    static final String ALARM_MUSIC_SONG_LENGTH    = "Alarm_SongLength";
    final static String ALARM_MUSIC_SONG_START     = "Alarm_SongStart";
    final static String ALARM_MUSIC_VOLUME         = "Alarm_Volume";
    final static String ALARM_MUSIC_FADE_IN        = "Alarm_FadeIn";
    final static String ALARM_MUSIC_FADE_IN_TIME   = "Alarm_FadeTime";

    final static String ALARM_MUSIC_VIBRATION_ACTIVE = "Alarm_Vibration_Active";
    final static String ALARM_MUSIC_VIBRATION_VALUE  = "Alarm_Vibration_Value";

    //Alarm Light
    final static String ALARM_LIGHT_SCREEN            = "Alarm_Screen";
    final static String ALARM_LIGHT_SCREEN_BRIGHTNESS = "Alarm_ScreenBrightness";
    final static String ALARM_LIGHT_SCREEN_START_TIME = "Alarm_ScreenStartTime";
    final static String ALARM_LIGHT_SCREEN_START_TEMP = "Alarm_ScreenStartTemp";
    final static String ALARM_LIGHT_COLOR1            = "Alarm_ScreenColor1";
    final static String ALARM_LIGHT_COLOR2            = "Alarm_ScreenColor2";
    final static String ALARM_LIGHT_FADE_COLOR        = "Alarm_FadeColor";
    final static String ALARM_LIGHT_USE_LED           = "Alarm_UseLED";
    final static String ALARM_LIGHT_LED_START_TIME    = "Alarm_LEDStartTime";
    final static String ALARM_LIGHT_LED_START_TEMP    = "Alarm_LEDStartTemp";

    //StartValues
    final static String[] SHORT_DAYS = DateFormatSymbols.getInstance().getShortWeekdays();
    final static String[] LONG_DAYS  = DateFormatSymbols.getInstance().getWeekdays();

    //AlarmSet
    final static boolean ACTUAL_ONESHOT   = false;
    final static boolean ACTUAL_TEMPORARY = false;

    //Time
    private final static Calendar calendar = Calendar.getInstance();
    final static int ACTUAL_TIME_HOUR   = calendar.get(Calendar.HOUR_OF_DAY);
    final static int ACTUAL_TIME_MINUTE = calendar.get(Calendar.MINUTE);
    final static int ACTUAL_TIME_SNOOZE = 10;

    //Days
    final static boolean ACTUAL_DAY_MONDAY    = false;
    final static boolean ACTUAL_DAY_TUESDAY   = false;
    final static boolean ACTUAL_DAY_WEDNESDAY = false;
    final static boolean ACTUAL_DAY_THURSDAY  = false;
    final static boolean ACTUAL_DAY_FRIDAY    = false;
    final static boolean ACTUAL_DAY_SATURDAY  = false;
    final static boolean ACTUAL_DAY_SUNDAY    = false;

    //Music
    final static String ACTUAL_MUSIC_SONG_URI = Settings.System.DEFAULT_ALARM_ALERT_URI.getPath();
    final static int ACTUAL_MUSIC_START              = 0;
    final static int ACTUAL_MUSIC_LENGTH             = 1;
    final static int ACTUAL_MUSIC_VOLUME             = 100;

    final static boolean ACTUAL_MUSIC_FADE_IN        = false;
    final static int ACTUAL_MUSIC_FADE_IN_TIME       = 0;

    final static boolean ACTUAL_MUSIC_VIBRATION      = false;
    final static int ACTUAL_MUSIC_VIBRATION_STRENGTH = 100;

    //Light
    final static boolean  ACTUAL_SCREEN           = true;
    final static boolean ACTUAL_SCREEN_COLOR_FADE = true;

    final static int ACTUAL_SCREEN_BRIGHTNESS = 99;
    final static int ACTUAL_SCREEN_START      = 30;
    final static int ACTUAL_SCREEN_COLOR1     = Color.RED;
    final static int ACTUAL_SCREEN_COLOR2     = Color.BLUE;

    final static boolean ACTUAL_LED           = false;
    final static int ACTUAL_LED_START         = 0;

    final static int ALARM_PERMISSION_MUSIC = 0;
}
