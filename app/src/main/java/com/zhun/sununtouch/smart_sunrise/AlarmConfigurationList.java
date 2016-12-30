package com.zhun.sununtouch.smart_sunrise;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Sunny on 28.12.2016.
 */

public class AlarmConfigurationList {

    int m_Amount;
    List<AlarmConfiguration> m_Alarms;

    Context m_Context;

    //Constructor
    AlarmConfigurationList(Context context){

        //Get Amount and initialize List
        m_Context = context;
        m_Alarms  = new Vector<>();
        m_Amount  = AlarmSharedPreferences.getSharedPreference(context).getInt(AlarmConstants.ALARM_VALUE, 0);

        //If no Alarms are set, we Add a Default Alarm
        if(m_Amount == 0)
            return;

        //Fill Alarm List
        for(int alarmID = 0; alarmID < m_Amount; ++alarmID)
            m_Alarms.add(new AlarmConfiguration(context, alarmID));
    }

    //Getter and Setter
    public void addAlarm(AlarmConfiguration alarm)
    {
        alarm.setAlarmID(m_Amount);
        alarm.commit();

        m_Alarms.add(alarm);
        m_Amount = m_Alarms.size();

        AlarmSharedPreferences.getSharedPreferenceEditor(m_Context).putInt(AlarmConstants.ALARM_VALUE, m_Amount).apply();
    }
    public boolean removeAlarm(int alarmID){

        try {
            m_Alarms.remove(alarmID);

        }catch (IndexOutOfBoundsException e){
            //TODO: Logging
            return false;
        }catch(UnsupportedOperationException e){
            return false;
        }

        removeSharedPreference(alarmID);
        return true;
    }

    private void removeSharedPreference(int alarmID){

        //Copy Data to fill AlarmCount Gap
        if( m_Amount > 0) {
            m_Amount = m_Alarms.size();
            for (int id = alarmID; id < m_Amount; ++id) {
                SharedPreferences sharedPrefs = AlarmSharedPreferences.getSharedPreference(m_Context, AlarmConstants.ALARM, id++); //TODO check if thats right whith double ++id
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

    public AlarmConfiguration getAlarm(int alarmID){
        return m_Alarms.get(alarmID);
    }
    public boolean setAlarm(AlarmConfiguration alarm){

        try {
            m_Alarms.set(alarm.getAlarmID(), alarm);
        }catch (IndexOutOfBoundsException e){
            //TODO: Logging and Catch the other possible Cases
            return false;
        }

        alarm.commit();
        return true;
    }

    public void refresh(){

        //Refresh List
        for(int alarmID = 0; alarmID < m_Amount; ++alarmID)
            m_Alarms.set(alarmID, new AlarmConfiguration(m_Context, alarmID));
    }

    public int size(){
        return m_Amount;
    }

    public boolean contains(int alarmID){
        return m_Alarms.size() > alarmID;
    }

    public boolean isEmpty(){
        return m_Alarms.isEmpty();
    }
}
