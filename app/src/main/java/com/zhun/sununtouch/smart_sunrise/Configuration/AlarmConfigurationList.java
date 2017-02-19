package com.zhun.sununtouch.smart_sunrise.Configuration;

import android.content.Context;
import android.content.SharedPreferences;

import com.zhun.sununtouch.smart_sunrise.Information.AlarmConstants;
import com.zhun.sununtouch.smart_sunrise.Information.AlarmToast;

import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Sunny on 28.12.2016.
 * Model Class to represent a Array of Alarm Configurations
 */

@SuppressWarnings("unused")
public class AlarmConfigurationList {

    private final List<AlarmConfiguration> m_Alarms;
    private final Context m_Context;
    private int m_Amount;
    private boolean m_AlarmSet;
    private AlarmConfiguration m_nextAlarm;


    //Constructor
    public AlarmConfigurationList(Context context) {

        //Get Amount and initialize List
        m_Context = context;
        m_Alarms = new Vector<>();
        m_Amount = AlarmSharedPreferences.getSharedPreference(context).getInt(AlarmConstants.ALARM_VALUE, 0);
        m_AlarmSet = false;
        m_nextAlarm = new AlarmConfiguration(context);

        //If no Alarms are set, we Add a Default Alarm
        if (m_Amount == 0)
            return;

        //Fill Alarm List
        for (int alarmID = 0; alarmID < m_Amount; ++alarmID)
        {
            AlarmConfiguration alarm = new AlarmConfiguration(context, alarmID);
            m_Alarms.add(alarm);

            if(alarm.isAlarmSet()){
                m_AlarmSet = true;
                if(m_nextAlarm.getTimeInMillis() > alarm.getTimeInMillis() )
                    m_nextAlarm = alarm;
            }
        }
    }

    //Getter and Setter
    public void addAlarm(AlarmConfiguration alarm) {
        alarm.setAlarmID(m_Amount);
        alarm.commit();

        m_Alarms.add(alarm);
        m_Amount = m_Alarms.size();

        AlarmSharedPreferences.getSharedPreferenceEditor(m_Context).putInt(AlarmConstants.ALARM_VALUE, m_Amount).apply();
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean removeAlarm(int alarmID) {

        try {
            m_Alarms.remove(alarmID);
        } catch (Exception e) {
            AlarmToast.showToastShort(m_Context, "Error: " + e.getMessage());
            return false;
        }
        removeSharedPreference(alarmID);
        return true;
    }

    private void removeSharedPreference(int alarmID) {

        //Copy Data to fill AlarmCount Gap
        if (m_Amount > 0) {
            m_Amount = m_Alarms.size();
            for (int id = alarmID; id < m_Amount; ++id) {
                SharedPreferences sharedPrefs = AlarmSharedPreferences.getSharedPreference(m_Context, AlarmConstants.ALARM, id++);
                SharedPreferences.Editor editorNew = sharedPrefs.edit();
                SharedPreferences settingsOld = AlarmSharedPreferences.getSharedPreference(m_Context, AlarmConstants.ALARM, id);

                Map<String, ?> settingOld = settingsOld.getAll();
                for (Map.Entry<String, ?> value : settingOld.entrySet()) {
                    if (value.getValue().getClass().equals(Boolean.class))
                        editorNew.putBoolean(value.getKey(), (Boolean) value.getValue());
                    else if (value.getValue().getClass().equals(Float.class))
                        editorNew.putFloat(value.getKey(), (Float) value.getValue());
                    else if (value.getValue().getClass().equals(Integer.class))
                        editorNew.putInt(value.getKey(), (Integer) value.getValue());
                    else if (value.getValue().getClass().equals(Long.class))
                        editorNew.putLong(value.getKey(), (Long) value.getValue());
                    else if (value.getValue().getClass().equals(String.class))
                        editorNew.putString(value.getKey(), (String) value.getValue());
                }
                editorNew.apply();
                settingsOld.edit().clear().apply();
            }
        }

        //Refresh Amount
        AlarmSharedPreferences.getSharedPreferenceEditor(m_Context).putInt(AlarmConstants.ALARM_VALUE, m_Amount).apply();
    }

    public AlarmConfiguration getAlarm(int alarmID) {
        return m_Alarms.get(alarmID);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean setAlarm(AlarmConfiguration alarm) {
        try {
            m_Alarms.set(alarm.getAlarmID(), alarm);
        } catch (Exception e) {
            AlarmToast.showToastShort(m_Context, "Error: " + e.getMessage());
            return false;
        }
        alarm.commit();
        return true;
    }

    public int size() {
        return m_Amount;
    }

    public boolean contains(int alarmID) {
        return m_Alarms.size() > alarmID;
    }

    public boolean isEmpty() {
        return m_Alarms.isEmpty();
    }

    public boolean isAlarmSet(){
        return m_AlarmSet;
    }

    public AlarmConfiguration getNextSetAlarm(){
        return m_nextAlarm;
    }
}
