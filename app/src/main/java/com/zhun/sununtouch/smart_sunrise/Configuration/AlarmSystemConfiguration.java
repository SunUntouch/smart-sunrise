package com.zhun.sununtouch.smart_sunrise.Configuration;

import android.content.Context;
import android.content.SharedPreferences;

import com.zhun.sununtouch.smart_sunrise.Information.AlarmConstants;
import com.zhun.sununtouch.smart_sunrise.R;

/**
 * Created by Sunny on 10.02.2017.
 * Helper Class to Load and Save global options
 */

public class AlarmSystemConfiguration {

    private final Context m_Context;

    private int brightness_steps = 100;
    private String currentTheme;
    private boolean enableLogging = false;

    public AlarmSystemConfiguration(final Context context){
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
    public void setBrightnessSteps(final int steps){
        brightness_steps = steps;
        commit();
    }
    public int getBrightnessSteps(){
        return brightness_steps;
    }
    public void setAlarmTheme(final String theme){
        currentTheme = theme;
        commit();
    }
    public String getAlarmTheme(){
        return currentTheme;
    }
    public void enableLogging(boolean enable){
        enableLogging = enable;
        commit();
    }
    public boolean loggingEnabled(){
        return enableLogging;
    }
}
