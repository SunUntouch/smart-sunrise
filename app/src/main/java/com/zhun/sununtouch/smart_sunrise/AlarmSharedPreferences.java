package com.zhun.sununtouch.smart_sunrise;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Sunny on 22.01.2016.
 */
public class AlarmSharedPreferences {

    public static SharedPreferences getSharedPreference(Context context, String _settingName){
        return context.getSharedPreferences(_settingName, Context.MODE_PRIVATE);
    }
    public static SharedPreferences getSharedPreference(Context context, String _settingName, int _actualAlarm){
        return context.getSharedPreferences(_settingName + Integer.toString(_actualAlarm), Context.MODE_PRIVATE);
    }
    public static SharedPreferences getSharedPreference(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences.Editor getSharedPreferenceEditor(Context context, String _settingsName){
        return getSharedPreference(context, _settingsName).edit();
    }
    public static SharedPreferences.Editor getSharedPreferenceEditor(Context context, String _settingsName, int _actualAlarm){
        return getSharedPreference(context, _settingsName, _actualAlarm).edit();
    }
    public static SharedPreferences.Editor getSharedPreferenceEditor(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).edit();
    }


}
