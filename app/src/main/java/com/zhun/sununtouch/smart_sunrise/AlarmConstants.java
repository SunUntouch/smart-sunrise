package com.zhun.sununtouch.smart_sunrise;

import android.graphics.Color;
import android.provider.Settings;

import java.util.Calendar;

abstract class AlarmConstants {

    //Shared Pref Settings
    final static String WAKEUP_TIMER            = "com.zhun.sununtouch.smart_sunrise.WAKEUP_SETTINGS";

    //Alarm
    final static String ALARM                      = "Alarm";
    final static String ALARM_ID                   = "Alarm_ID";
    final static String ALARM_NAME                 = "Alarm_Name";
    final static String ALARM_VALUE                = "Alarm_Value";

    final static String ALARM_TIME_SET             = "Alarm_isSet";
    final static String ALARM_MANAGER              = "Alarm_Manager";

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
    final static String ALARM_MUSIC_SONGID         = "Alarm_SongID";
    static final String ALARM_MUSIC_SONGLENGTH     = "Alarm_SongLength";
    final static String ALARM_MUSIC_SONGSTART      = "Alarm_SongStart";
    final static String ALARM_MUSIC_VOLUME         = "Alarm_Volume";
    final static String ALARM_MUSIC_FADEIN         = "Alarm_FadeIn";
    final static String ALARM_MUSIC_FADEINTIME     = "Alarm_FadeTime";

    final static String ALARM_MUSIC_VIBRATION_ACTIV= "Alarm_Vibration_Activ";
    final static String ALARM_MUSIC_VIBRATION_VALUE= "Alarm_Vibration_Value";

    //Alarm Light
    final static String ALARM_LIGHT_SCREEN            = "Alarm_Screen";
    final static String ALARM_LIGHT_SCREEN_BRIGTHNESS = "Alarm_ScreenBrigthness";
    final static String ALARM_LIGHT_SCREEN_START_TIME = "Alarm_ScreenStartTime";
    final static String ALARM_LIGHT_COLOR1            = "Alarm_ScreenColor1";
    final static String ALARM_LIGHT_COLOR2            = "Alarm_ScreenColor2";
    final static String ALARM_LIGHT_FADECOLOR         = "Alarm_FadeColor";
    final static String ALARM_LIGHT_USELED            = "Alarm_UseLED";
    final static String ALARM_LIGHT_LED_START_TIME    = "Alarm_LEDStartTime";

    //StartValues
    private static Calendar calendar = Calendar.getInstance();

    //AlarmSet
    final static boolean ALARM_IS_SET   = false;
    //Time
    final static int ACTUAL_TIME_HOUR   = calendar.get(Calendar.HOUR_OF_DAY);
    final static int ACTUAL_TIME_MINUTE = calendar.get(Calendar.MINUTE);
    final static int ACTUAL_TIME_SNOOZE = 10;

    //Days
    final static int ACTUAL_DAY_MONDAY    = 0;
    final static int ACTUAL_DAY_TUESDAY   = 0;
    final static int ACTUAL_DAY_WEDNESDAY = 0;
    final static int ACTUAL_DAY_THURSDAY  = 0;
    final static int ACTUAL_DAY_FRIDAY    = 0;
    final static int ACTUAL_DAY_SATURDAY  = 0;
    final static int ACTUAL_DAY_SUNDAY    = 0;

    //Music
    final static String ACTUAL_MUSIC_SONG_URI = Settings.System.DEFAULT_ALARM_ALERT_URI.getPath();
    final static int ACTUAL_MUSIC_START              = 0;
    final static int ACTUAL_MUSIC_LENGTH             = 1;
    final static int ACTUAL_MUSIC_VOLUME             = 100;
    final static int ACTUAL_MUSIC_FADE_IN            = 0;
    final static int ACTUAL_MUSIC_FADE_IN_TIME       = 0;
    final static int ACTUAL_MUSIC_VIBRATION          = 0;
    final static int ACTUAL_MUSIC_VIBRATION_STRENGTH = 100;

    //Light
    final static int ACTUAL_SCREEN            = 1;
    final static int ACTUAL_SCREEN_BRIGHTNESS = 99;
    final static int ACTUAL_SCREEN_START      = 30;
    final static int ACTUAL_SCREEN_COLOR1     = Color.RED;
    final static int ACTUAL_SCREEN_COLOR2     = Color.BLUE;
    final static int ACTUAL_SCREEN_COLOR_FADE = 1;
    final static int ACTUAL_LED               = 0;
    final static int ACTUAL_LED_START         = 10;

    final static int ALARM_PERMISSION_MUSIC = 0;
}
