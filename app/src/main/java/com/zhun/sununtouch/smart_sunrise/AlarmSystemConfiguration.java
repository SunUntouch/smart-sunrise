package com.zhun.sununtouch.smart_sunrise;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Sunny on 10.02.2017.
 */

public class AlarmSystemConfiguration {

    private final Context m_Context;

    private int brightness_steps = 100;
    private String currentTheme;
    private boolean enableLogging = false;

    AlarmSystemConfiguration(final Context context){
        m_Context = context;
        init();
    }

    private void init(){
        //Load Settings
        SharedPreferences settings = AlarmSharedPreferences.getSharedPreference(m_Context, AlarmConstants.WAKEUP_OPTIONS);
        this.brightness_steps = settings.getInt(AlarmConstants.WAKEUP_OPTIONS_BRIGHTNESSSTEPS , AlarmConstants.BRIGHTNESS_STEPS);
        this.currentTheme     = settings.getString(AlarmConstants.WAKEUP_OPTIONS_THEME        , m_Context.getString(R.string.options_theme_default));
        this.enableLogging    = settings.getBoolean(AlarmConstants.WAKEUP_OPTIONS_LOGGING     , AlarmConstants.ALARM_LOGGING);
    }
    private void commit(){
        //Save sharedPreferences
        SharedPreferences.Editor editor = AlarmSharedPreferences.getSharedPreferenceEditor(m_Context, AlarmConstants.WAKEUP_OPTIONS).clear();
        editor.putInt(AlarmConstants.WAKEUP_OPTIONS_BRIGHTNESSSTEPS, getBrightnessSteps());
        editor.putString(AlarmConstants.WAKEUP_OPTIONS_THEME       , getAlarmTheme());
        editor.putBoolean(AlarmConstants.WAKEUP_OPTIONS_LOGGING    , loggingEnabled());
        editor.apply();
    }

    //Getter and Setter
    void setBrightnessSteps(final int steps){
        brightness_steps = steps;
        commit();
    }
    int getBrightnessSteps(){
        return brightness_steps;
    }
    void setAlarmTheme(final String theme){
        currentTheme = theme;
        commit();
    }
    String getAlarmTheme(){
        return currentTheme;
    }
    void enableLogging(boolean enable){
        enableLogging = enable;
        commit();
    }
    boolean loggingEnabled(){
        return enableLogging;
    }
}
