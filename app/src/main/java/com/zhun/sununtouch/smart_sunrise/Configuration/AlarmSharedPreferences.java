package com.zhun.sununtouch.smart_sunrise.Configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Sunny on 22.01.2016.
 * Helper Class to Read and Write to Shared Preferences
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AlarmSharedPreferences {
    public static SharedPreferences getSharedPreference(Context context, String settingName){
        return context.getSharedPreferences(settingName, Context.MODE_PRIVATE);
    }
    public static SharedPreferences getSharedPreference(Context context, String settingName, int actualAlarm){
        return context.getSharedPreferences(settingName + Integer.toString(actualAlarm), Context.MODE_PRIVATE);
    }
    public static SharedPreferences getSharedPreference(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences.Editor getSharedPreferenceEditor(Context context, String settingsName){
        return getSharedPreference(context, settingsName).edit();
    }
    public static SharedPreferences.Editor getSharedPreferenceEditor(Context context, String settingsName, int actualAlarm){
        return getSharedPreference(context, settingsName, actualAlarm).edit();
    }
    public static SharedPreferences.Editor getSharedPreferenceEditor(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).edit();
    }
}
