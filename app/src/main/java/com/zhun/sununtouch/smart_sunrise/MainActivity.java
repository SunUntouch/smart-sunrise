package com.zhun.sununtouch.smart_sunrise;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
                    implements TimePickerDialog.OnTimeSetListener{
    //ExpendableList
    private ExpandableListAdapter AlarmViewAdapter;
    private ExpandableListView AlarmGroupView;
    private LinearLayout          noAlarmLayout;

    private LinkedHashMap<String,
            LinkedHashMap<String,
                    LinkedHashMap<String, Integer>>> expListDataChild;

    //Media Player
    private MediaPlayer mediaPlayer;

    //Actual Alarm Values
    private LinkedHashMap<Integer, AlarmConfiguration> alarmConfigurations = new LinkedHashMap<>();

    private int actualAlarm    =-1;

    //Last Clicked Button
    private int actualButtonID    = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /***********************************************************************************************
     * ONCREATE
     **********************************************************************************************/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Expendable Listview///////////////////////////////////////////////////////////////////////
        //get the listView
        AlarmGroupView = (ExpandableListView) findViewById(R.id.wakeup_timer_expendbleList);
        noAlarmLayout = (LinearLayout) findViewById(R.id.wakeup_timer_no_Alarm_set_View);

        //Configuration/////////////////////////////////////////////////////////////////////////////
        prepareConfiguration();
        AlarmViewAdapter = new ExpandableListAdapter(this, alarmConfigurations);

        //setting list adapter
        AlarmGroupView.setAdapter(AlarmViewAdapter);
        AlarmGroupView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {

                loadValuesNew(groupPosition);
                actualAlarm = groupPosition;

                if (groupPosition != previousGroup)
                    AlarmGroupView.collapseGroup(previousGroup);

                previousGroup = groupPosition;
                AlarmGroupView.invalidateViews();
            }
        });

        //Floating ActionButton/////////////////////////////////////////////////////////////////////
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Add New Alarm
                saveListDataChild(AlarmConstants.ALARM);
            }
        });
    }

    /***********************************************************************************************
     * DATA VALUES
     **********************************************************************************************/
    private void prepareConfiguration(){

        //Get Shared Preferences
        SharedPreferences information = AlarmSharedPreferences.getSharedPreference(getApplicationContext(), AlarmConstants.WAKEUP_TIMER_INFO);
        int amount = information.getInt(AlarmConstants.ALARM_VALUE, 0);

        if(amount == 0)
        {
            noAlarmLayout.setVisibility(LinearLayout.VISIBLE);
            AlarmGroupView.setVisibility(ExpandableListView.GONE);
        }
        else
        {
            loadConfig();

            //We have a Alarm
            noAlarmLayout.setVisibility(LinearLayout.GONE);
            AlarmGroupView.setVisibility(ExpandableListView.VISIBLE);
        }

        //Change View
        AlarmGroupView.invalidateViews();
    }

    private Vector<Integer> fillAlarmVector(int alarm_type, SharedPreferences settings){
        //Choose AlarmType
        switch (alarm_type)
        {
            case AlarmConstants.TYPE_TIME:
            {
                // hour, minute, snooze
                Vector<Integer> time = new Vector<>(3);
                if(settings != null)
                {
                    time.addElement(settings.getInt(AlarmConstants.ALARM_TIME_HOUR   , AlarmConstants.ACTUAL_TIME_HOUR));
                    time.addElement(settings.getInt(AlarmConstants.ALARM_TIME_MINUTES, AlarmConstants.ACTUAL_TIME_MINUTE));
                    time.addElement(settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE , AlarmConstants.ACTUAL_TIME_SNOOZE));
                }
                else
                    for(int element = 0; element < time.capacity(); ++element)
                        time.addElement(0);

                return time;
            }
            case AlarmConstants.TYPE_DAYS:
            {
                // Monday - Sunday
                Vector<Integer> days = new Vector<>(7);
                if(settings != null)
                {
                    days.addElement(settings.getInt(AlarmConstants.ALARM_DAY_MONDAY   , AlarmConstants.ACTUAL_DAY_MONDAY));
                    days.addElement(settings.getInt(AlarmConstants.ALARM_DAY_TUESDAY  , AlarmConstants.ACTUAL_DAY_TUESDAY));
                    days.addElement(settings.getInt(AlarmConstants.ALARM_DAY_WEDNESDAY, AlarmConstants.ACTUAL_DAY_WEDNESDAY));
                    days.addElement(settings.getInt(AlarmConstants.ALARM_DAY_THURSDAY , AlarmConstants.ACTUAL_DAY_THURSDAY));
                    days.addElement(settings.getInt(AlarmConstants.ALARM_DAY_FRIDAY   , AlarmConstants.ACTUAL_DAY_FRIDAY));
                    days.addElement(settings.getInt(AlarmConstants.ALARM_DAY_SATURDAY , AlarmConstants.ACTUAL_DAY_SATURDAY));
                    days.addElement(settings.getInt(AlarmConstants.ALARM_DAY_SUNDAY   , AlarmConstants.ACTUAL_DAY_SUNDAY));
                }
                else
                    for(int element = 0; element < days.capacity(); ++element)
                        days.addElement(0);

                return  days;
            }
            case AlarmConstants.TYPE_MUSIC:
            {
                // StartTime, Volume, FadIn, FadeInTime, Vibration, Vibration Strength
                Vector<Integer> music = new Vector<>(6);
                if(settings != null)
                {
                    music.addElement(settings.getInt(AlarmConstants.ALARM_MUSIC_SONGSTART      , AlarmConstants.ACTUAL_MUSIC_START));
                    music.addElement(settings.getInt(AlarmConstants.ALARM_MUSIC_VOLUME         , AlarmConstants.ACTUAL_MUSIC_VOLUME));
                    music.addElement(settings.getInt(AlarmConstants.ALARM_MUSIC_FADEIN         , AlarmConstants.ACTUAL_MUSIC_FADE_IN));
                    music.addElement(settings.getInt(AlarmConstants.ALARM_MUSIC_FADEINTIME     , AlarmConstants.ACTUAL_MUSIC_FADE_IN_TIME));
                    music.addElement(settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV, AlarmConstants.ACTUAL_MUSIC_VIBRATION));
                    music.addElement(settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE, AlarmConstants.ACTUAL_MUSIC_VIBRATION_STRENGTH));
                }
                else
                    for(int element = 0; element < music.capacity(); ++element)
                        music.addElement(0);

                return music;
            }
            case AlarmConstants.TYPE_LIGHT:
            {
                // UseScreen, Brightness, Screen Start Time, ScreenColor1, ScreenColor2, FadeColor, FadeTime, UseLED, LED Start Time
                Vector<Integer> light = new Vector<>(8);
                if(settings != null)
                {
                    light.addElement(settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN           , AlarmConstants.ACTUAL_SCREEN));
                    light.addElement(settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS, AlarmConstants.ACTUAL_SCREEN_BRIGHTNESS));
                    light.addElement(settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME, AlarmConstants.ACTUAL_SCREEN_START));
                    light.addElement(settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR1           , AlarmConstants.ACTUAL_SCREEN_COLOR1));
                    light.addElement(settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR2           , AlarmConstants.ACTUAL_SCREEN_COLOR2));
                    light.addElement(settings.getInt(AlarmConstants.ALARM_LIGHT_FADECOLOR        , AlarmConstants.ACTUAL_SCREEN_COLOR_FADE));
                    light.addElement(settings.getInt(AlarmConstants.ALARM_LIGHT_USELED           , AlarmConstants.ACTUAL_LED));
                    light.addElement(settings.getInt(AlarmConstants.ALARM_LIGHT_LED_START_TIME   , AlarmConstants.ACTUAL_LED_START));
                }
                else
                    for(int element = 0; element < light.capacity(); ++element)
                        light.addElement(0);

                return  light;
            }
            default:
                debug_assertion(true);
                return new Vector<>();
        }
    }


    private void saveListDataChild(String _name){

        //Get Shared Preferences
        SharedPreferences information = AlarmSharedPreferences.getSharedPreference(getApplicationContext(), AlarmConstants.WAKEUP_TIMER_INFO);

        int amount = information.getInt(AlarmConstants.ALARM_VALUE, 0);
        saveListDataChild(_name, amount);
    }
    private void saveListDataChild(String _name, int _id){

        //Get Shared Preferences
        SharedPreferences information   = AlarmSharedPreferences.getSharedPreference(getApplicationContext(), AlarmConstants.WAKEUP_TIMER_INFO);
        int amount = information.getInt(AlarmConstants.ALARM_VALUE, 0);

        //changelistData
        if(amount == 0)
            saveConfigurationData(_name, amount++);
        else if(_id < amount)
            saveConfigurationData(_name, _id);
        else
            saveConfigurationData(_name, ++amount);

        //Add AlarmValue
        SharedPreferences.Editor editor = information.edit();
        editor.putInt(AlarmConstants.ALARM_VALUE, amount);
        editor.apply();

        //prepare new List Data
        prepareConfiguration();
    }

    public  void deleteChild(View v){

        //Delete Child
        Button deleteButton = (Button) v;
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, getString(R.string.delete_warning), Toast.LENGTH_SHORT).show();
            }
        });
        deleteButton.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View v) {
                deleteListDataChild();
                return false;
            }
        });
    }
    private void deleteListDataChild(){

        //Get Current Alarm
        int ID = actualAlarm;

        //Delete Map
        alarmConfigurations.remove(ID);

        //Collapse Group
        AlarmGroupView.collapseGroup(ID);

        //Load SharedPreferences
        SharedPreferences information = getApplicationContext().getSharedPreferences(AlarmConstants.WAKEUP_TIMER_INFO, Context.MODE_PRIVATE);

        //Copy Data to fill AlarmCount Gap
        int amount = information.getInt(AlarmConstants.ALARM_VALUE, 0);
        if( amount > 0)
        {
            for (int id = ID; id < amount; ++id)
            {
                if(id < amount -1)
                {
                    SharedPreferences.Editor editorNew = AlarmSharedPreferences.getSharedPreferenceEditor(getApplicationContext(), AlarmConstants.WAKEUP_TIMER, id);
                    SharedPreferences settingsOld      = AlarmSharedPreferences.getSharedPreference(getApplicationContext(), AlarmConstants.WAKEUP_TIMER, id + 1);
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
                }
            }

            //Set New Amount
            int newAmount = --amount;
            SharedPreferences.Editor editor = AlarmSharedPreferences.getSharedPreferenceEditor(getApplicationContext(), AlarmConstants.WAKEUP_TIMER, newAmount);
            editor.clear();
            editor.apply();

            //Set new Amount Information
            SharedPreferences.Editor editorInf = information.edit();
            editorInf.putInt(AlarmConstants.ALARM_VALUE, newAmount);
            editorInf.apply();

            //prepare new List Data
            prepareConfiguration();
        }
    }

    private void saveConfigurationData(String _name, int _id){

        AlarmConfiguration config = new AlarmConfiguration();

        if(!alarmConfigurations.containsKey(_id))
        {
            //put Alarm in map
            alarmConfigurations.put(_id, config);

            //activate view
            noAlarmLayout.setVisibility(LinearLayout.VISIBLE);
            AlarmGroupView.setVisibility(ExpandableListView.GONE);
        }
        else
            config = alarmConfigurations.get(_id);

        //Load sharedPreferences
        SharedPreferences.Editor editor = AlarmSharedPreferences.getSharedPreferenceEditor(getApplicationContext(), AlarmConstants.WAKEUP_TIMER, _id);

        //Check if Alarm Exists
        String alarmName = AlarmConstants.ALARM + _id;

        //put StringSet back
        editor.putString(AlarmConstants.ALARM_NAME, _name);

        //Alarm is Set
        editor.putBoolean(AlarmConstants.ALARM_TIME_SET, config.isAlarmSet());

        //Time
        editor.putInt(AlarmConstants.ALARM_TIME_MINUTES  , config.getMinute());
        editor.putInt(AlarmConstants.ALARM_TIME_HOUR     , config.getHour());
        editor.putInt(AlarmConstants.ALARM_TIME_SNOOZE   , config.getSnooze());

        //Days
        editor.putInt(AlarmConstants.ALARM_DAY_MONDAY    , config.isMonday());
        editor.putInt(AlarmConstants.ALARM_DAY_TUESDAY   , config.isTuesday());
        editor.putInt(AlarmConstants.ALARM_DAY_WEDNESDAY , config.isWednesday());
        editor.putInt(AlarmConstants.ALARM_DAY_THURSDAY  , config.isThursday());
        editor.putInt(AlarmConstants.ALARM_DAY_FRIDAY    , config.isFriday());
        editor.putInt(AlarmConstants.ALARM_DAY_SATURDAY  , config.isSaturday());
        editor.putInt(AlarmConstants.ALARM_DAY_SUNDAY    , config.isSunday());

        //Music
        editor.putString(AlarmConstants.ALARM_MUSIC_SONGID       , config.getSongURI());
        editor.putInt(AlarmConstants.ALARM_MUSIC_VOLUME          , config.getVolume());
        editor.putInt(AlarmConstants.ALARM_MUSIC_SONGSTART       , config.getSongStart());
        editor.putInt(AlarmConstants.ALARM_MUSIC_SONGLENGTH      , config.getSongLength());
        editor.putInt(AlarmConstants.ALARM_MUSIC_FADEIN          , config.getFadeIn());
        editor.putInt(AlarmConstants.ALARM_MUSIC_FADEINTIME      , config.getFadeInTime());
        editor.putInt(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV , config.getVibration());
        editor.putInt(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE , config.getVibrationStrength());

        //Light
        editor.putInt(AlarmConstants.ALARM_LIGHT_SCREEN            , config.getScreen());
        editor.putInt(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS , config.getScreenBrightness());
        editor.putInt(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME , config.getScreenStartTime());
        editor.putInt(AlarmConstants.ALARM_LIGHT_COLOR1            , config.getLightColor1());
        editor.putInt(AlarmConstants.ALARM_LIGHT_COLOR2            , config.getLightColor2());
        editor.putInt(AlarmConstants.ALARM_LIGHT_FADECOLOR         , config.getLightFade());
        editor.putInt(AlarmConstants.ALARM_LIGHT_USELED            , config.getLED());
        editor.putInt(AlarmConstants.ALARM_LIGHT_LED_START_TIME    , config.getLEDStartTime());

        //apply Values to settings
        editor.apply();
    }
    private void addConfig(AlarmConfiguration config){
        //Get Shared Preferences
        SharedPreferences information = AlarmSharedPreferences.getSharedPreference(getApplicationContext(), AlarmConstants.WAKEUP_TIMER_INFO);

        int amount = information.getInt(AlarmConstants.ALARM_VALUE, 0);
        addConfig(config, amount);
    }
    private void addConfig(AlarmConfiguration config, int id){
        alarmConfigurations.put(id, config);
    }
    private void loadConfig(){

        //Get Shared Preferences
        SharedPreferences information = AlarmSharedPreferences.getSharedPreference(getApplicationContext(), AlarmConstants.WAKEUP_TIMER_INFO);
        int amount = information.getInt(AlarmConstants.ALARM_VALUE, 0);

        for(int _alarmID = 0; _alarmID < amount; ++_alarmID)
        {
            //save Settings
            SharedPreferences settings = AlarmSharedPreferences.getSharedPreference(getApplicationContext(), AlarmConstants.WAKEUP_TIMER, _alarmID);

            //Putting Value for each child
            Calendar calendar = Calendar.getInstance();

            AlarmConfiguration newAlarm = new AlarmConfiguration();

            //ID
            newAlarm.setAlarmID(_alarmID);

            //Name
            newAlarm.setAlarmName(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM + Integer.toString(_alarmID)));

            //AlarmSet
            newAlarm.setAlarm(settings.getBoolean(AlarmConstants.ALARM_TIME_SET, false));

            //Time
            newAlarm.setHour  (settings.getInt(AlarmConstants.ALARM_TIME_HOUR   , calendar.get(Calendar.HOUR_OF_DAY)));
            newAlarm.setMinute(settings.getInt(AlarmConstants.ALARM_TIME_MINUTES, calendar.get(Calendar.MINUTE)));
            newAlarm.setSnooze(settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE , AlarmConstants.ACTUAL_TIME_SNOOZE));    // hour, minute, snooze

            //Days
            newAlarm.setMonday   (settings.getInt(AlarmConstants.ALARM_DAY_MONDAY   , AlarmConstants.ACTUAL_DAY_MONDAY));
            newAlarm.setTuesday  (settings.getInt(AlarmConstants.ALARM_DAY_TUESDAY  , AlarmConstants.ACTUAL_DAY_TUESDAY));
            newAlarm.setWednesday(settings.getInt(AlarmConstants.ALARM_DAY_WEDNESDAY, AlarmConstants.ACTUAL_DAY_WEDNESDAY));
            newAlarm.setThursday (settings.getInt(AlarmConstants.ALARM_DAY_THURSDAY , AlarmConstants.ACTUAL_DAY_THURSDAY));
            newAlarm.setFriday   (settings.getInt(AlarmConstants.ALARM_DAY_FRIDAY   , AlarmConstants.ACTUAL_DAY_FRIDAY));
            newAlarm.setSaturday (settings.getInt(AlarmConstants.ALARM_DAY_SATURDAY , AlarmConstants.ACTUAL_DAY_SATURDAY));
            newAlarm.setSunday   (settings.getInt(AlarmConstants.ALARM_DAY_SUNDAY   , AlarmConstants.ACTUAL_DAY_SUNDAY)); // Monday - Sunday

            //Load Music
            newAlarm.setSongURI          (settings.getString(AlarmConstants.ALARM_MUSIC_SONGID      , AlarmConstants.ACTUAL_MUSIC_SONG_URI));
            newAlarm.setSongStart        (settings.getInt(AlarmConstants.ALARM_MUSIC_SONGSTART      , AlarmConstants.ACTUAL_MUSIC_START));
            newAlarm.setSongLength       (settings.getInt(AlarmConstants.ALARM_MUSIC_SONGLENGTH     , AlarmConstants.ACTUAL_MUSIC_LENGTH));
            newAlarm.setVolume           (settings.getInt(AlarmConstants.ALARM_MUSIC_VOLUME         , AlarmConstants.ACTUAL_MUSIC_VOLUME));
            newAlarm.setFadeIn           (settings.getInt(AlarmConstants.ALARM_MUSIC_FADEIN         , AlarmConstants.ACTUAL_MUSIC_FADE_IN));
            newAlarm.setFadeInTime       (settings.getInt(AlarmConstants.ALARM_MUSIC_FADEINTIME     , AlarmConstants.ACTUAL_MUSIC_FADE_IN_TIME));
            newAlarm.setVibration        (settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV, AlarmConstants.ACTUAL_MUSIC_VIBRATION));
            newAlarm.setVibrationStrength(settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE, AlarmConstants.ACTUAL_MUSIC_VIBRATION_STRENGTH));// Song, StartTime, Volume, FadIn, FadeInTime

            //Load Light
            newAlarm.setScreen          (settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN           , AlarmConstants.ACTUAL_SCREEN));
            newAlarm.setScreenBrightness(settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS, AlarmConstants.ACTUAL_SCREEN_BRIGHTNESS));
            newAlarm.setScreenStartTime (settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME, AlarmConstants.ACTUAL_SCREEN_START));

            newAlarm.setLightColor1(settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR1   , AlarmConstants.ACTUAL_SCREEN_COLOR1));
            newAlarm.setLightColor2(settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR2   , AlarmConstants.ACTUAL_SCREEN_COLOR2));
            newAlarm.setLightFade  (settings.getInt(AlarmConstants.ALARM_LIGHT_FADECOLOR, AlarmConstants.ACTUAL_SCREEN_COLOR_FADE));

            newAlarm.setLED         (settings.getInt(AlarmConstants.ALARM_LIGHT_USELED        , AlarmConstants.ACTUAL_LED));
            newAlarm.setLEDStartTime(settings.getInt(AlarmConstants.ALARM_LIGHT_LED_START_TIME, AlarmConstants.ACTUAL_LED_START));// UseScreen, ScreenColor1, ScreenColor2, Fadecolor, FadeTime, UseLED

            //Put in map
            alarmConfigurations.put(_alarmID, newAlarm);
        }

        if(AlarmViewAdapter != null)
            AlarmViewAdapter.notifyDataSetChanged(alarmConfigurations);
    }
    private void loadValuesNew(int _alarmID){

        debug_assertion(!alarmConfigurations.containsKey(_alarmID));

        //Set actual Alarm
        actualAlarm = _alarmID;

        //save Settings
        SharedPreferences settings = AlarmSharedPreferences.getSharedPreference(getApplicationContext(), AlarmConstants.WAKEUP_TIMER, actualAlarm);

        //Putting Value for each child
        Calendar calendar = Calendar.getInstance();

        //ID
        alarmConfigurations.get(_alarmID).setAlarmID(_alarmID);

        //name
        alarmConfigurations.get(_alarmID).setAlarmName(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM + Integer.toString(_alarmID)));

        //AlarmSet
        alarmConfigurations.get(_alarmID).setAlarm(settings.getBoolean(AlarmConstants.ALARM_TIME_SET, false));

        //Time
        alarmConfigurations.get(_alarmID).setHour  (settings.getInt(AlarmConstants.ALARM_TIME_HOUR   , calendar.get(Calendar.HOUR_OF_DAY)));
        alarmConfigurations.get(_alarmID).setMinute(settings.getInt(AlarmConstants.ALARM_TIME_MINUTES, calendar.get(Calendar.MINUTE)));
        alarmConfigurations.get(_alarmID).setSnooze(settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE , AlarmConstants.ACTUAL_TIME_SNOOZE));    // hour, minute, snooze

        //Days
        alarmConfigurations.get(_alarmID).setMonday   (settings.getInt(AlarmConstants.ALARM_DAY_MONDAY   , AlarmConstants.ACTUAL_DAY_MONDAY));
        alarmConfigurations.get(_alarmID).setTuesday  (settings.getInt(AlarmConstants.ALARM_DAY_TUESDAY  , AlarmConstants.ACTUAL_DAY_TUESDAY));
        alarmConfigurations.get(_alarmID).setWednesday(settings.getInt(AlarmConstants.ALARM_DAY_WEDNESDAY, AlarmConstants.ACTUAL_DAY_WEDNESDAY));
        alarmConfigurations.get(_alarmID).setThursday (settings.getInt(AlarmConstants.ALARM_DAY_THURSDAY , AlarmConstants.ACTUAL_DAY_THURSDAY));
        alarmConfigurations.get(_alarmID).setFriday   (settings.getInt(AlarmConstants.ALARM_DAY_FRIDAY   , AlarmConstants.ACTUAL_DAY_FRIDAY));
        alarmConfigurations.get(_alarmID).setSaturday (settings.getInt(AlarmConstants.ALARM_DAY_SATURDAY , AlarmConstants.ACTUAL_DAY_SATURDAY));
        alarmConfigurations.get(_alarmID).setSunday   (settings.getInt(AlarmConstants.ALARM_DAY_SUNDAY   , AlarmConstants.ACTUAL_DAY_SUNDAY)); // Monday - Sunday

        //Load Music
        alarmConfigurations.get(_alarmID).setSongURI          (settings.getString(AlarmConstants.ALARM_MUSIC_SONGID      , AlarmConstants.ACTUAL_MUSIC_SONG_URI));
        alarmConfigurations.get(_alarmID).setSongStart        (settings.getInt(AlarmConstants.ALARM_MUSIC_SONGSTART      , AlarmConstants.ACTUAL_MUSIC_START));
        alarmConfigurations.get(_alarmID).setSongLength       (settings.getInt(AlarmConstants.ALARM_MUSIC_SONGLENGTH     , AlarmConstants.ACTUAL_MUSIC_LENGTH));
        alarmConfigurations.get(_alarmID).setVolume           (settings.getInt(AlarmConstants.ALARM_MUSIC_VOLUME         , AlarmConstants.ACTUAL_MUSIC_VOLUME));
        alarmConfigurations.get(_alarmID).setFadeIn           (settings.getInt(AlarmConstants.ALARM_MUSIC_FADEIN         , AlarmConstants.ACTUAL_MUSIC_FADE_IN));
        alarmConfigurations.get(_alarmID).setFadeInTime       (settings.getInt(AlarmConstants.ALARM_MUSIC_FADEINTIME     , AlarmConstants.ACTUAL_MUSIC_FADE_IN_TIME));
        alarmConfigurations.get(_alarmID).setVibration        (settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV, AlarmConstants.ACTUAL_MUSIC_VIBRATION));
        alarmConfigurations.get(_alarmID).setVibrationStrength(settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE, AlarmConstants.ACTUAL_MUSIC_VIBRATION_STRENGTH));// Song, StartTime, Volume, FadIn, FadeInTime

        //Load Light
        alarmConfigurations.get(_alarmID).setScreen          (settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN           , AlarmConstants.ACTUAL_SCREEN));
        alarmConfigurations.get(_alarmID).setScreenBrightness(settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS, AlarmConstants.ACTUAL_SCREEN_BRIGHTNESS));
        alarmConfigurations.get(_alarmID).setScreenStartTime (settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME, AlarmConstants.ACTUAL_SCREEN_START));

        alarmConfigurations.get(_alarmID).setLightColor1(settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR1   , AlarmConstants.ACTUAL_SCREEN_COLOR1));
        alarmConfigurations.get(_alarmID).setLightColor2(settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR2   , AlarmConstants.ACTUAL_SCREEN_COLOR2));
        alarmConfigurations.get(_alarmID).setLightFade  (settings.getInt(AlarmConstants.ALARM_LIGHT_FADECOLOR, AlarmConstants.ACTUAL_SCREEN_COLOR_FADE));

        alarmConfigurations.get(_alarmID).setLED         (settings.getInt(AlarmConstants.ALARM_LIGHT_USELED        , AlarmConstants.ACTUAL_LED));
        alarmConfigurations.get(_alarmID).setLEDStartTime(settings.getInt(AlarmConstants.ALARM_LIGHT_LED_START_TIME, AlarmConstants.ACTUAL_LED_START));// UseScreen, ScreenColor1, ScreenColor2, Fadecolor, FadeTime, UseLED

        if(AlarmViewAdapter != null)
            AlarmViewAdapter.notifyDataSetChanged(alarmConfigurations);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void saveSettings(String _settingName, int _actualAlarm, String _alarmName){

        //save Settings
        saveSettings(_settingName, _actualAlarm, _alarmName, _alarmName);
    }
    private void saveSettings(String _settingName, int _actualAlarm, String _alarmIdentifier, String _alarmName){

        //save Settings
        SharedPreferences settings = AlarmSharedPreferences.getSharedPreference(getApplicationContext(), _settingName, _actualAlarm);
        saveListDataChild(settings.getString(_alarmIdentifier, _alarmName), _actualAlarm);
    }

    private LinearLayout createAlertLinearLayout(View v, TextView textView, SeekBar seekBar, int _max, int _increment, int _progress){

        //LinearLayout
        LinearLayout linearLayout = new LinearLayout(v.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //TextView to show Value of SeekBar
        textView.setVisibility(TextView.INVISIBLE);
        linearLayout.addView(textView);

        //Seek Bar
        seekBar.setMax(_max);
        seekBar.setKeyProgressIncrement(_increment);
        seekBar.setProgress(_progress);
        linearLayout.addView(seekBar);

        return linearLayout;
    }

    private void debug_assertion(boolean check){

        if(BuildConfig.DEBUG && check)
            throw new AssertionError();
    }
    /***********************************************************************************************
     * Set New Alarm
     **********************************************************************************************/
    public     void setNewAlarm(View v){

        //Set Button ID
        actualButtonID = v.getId();

        //Get Toggle Button
        ToggleButton activeAlarmToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_setAlarmButton);

        //Set Alarm
        boolean toggle = activateAlarm(activeAlarmToggle.isChecked());
        activeAlarmToggle.setChecked(toggle);

        alarmConfigurations.get(actualAlarm).setAlarm(toggle);

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);

        //show Toast
        String alarmText = String.format(getString(
                R.string.toast_positive_alarm),
                String.format("%02d:%02d".toUpperCase(Locale.US),
                        alarmConfigurations.get(actualAlarm).getHour(),
                        alarmConfigurations.get(actualAlarm).getMinute()));

        AlarmToast.showToastShort(getApplicationContext(), alarmConfigurations.get(actualAlarm).isAlarmSet(), alarmText, getString(R.string.toast_negative_alarm));
    }
    private boolean activateAlarm(boolean active){

        //Get new Alarm Manager
        AlarmManage newAlarm = new AlarmManage(getApplicationContext());

        //Set Alarm
        if(active)
            newAlarm.setNewAlarm(actualAlarm, false);
        else
            newAlarm.cancelAlarmwithButton(actualAlarm);

        //Return if Alarm is Set in the System
        return newAlarm.checkForPendingIntent(actualAlarm);
    }

    /***********************************************************************************************
     * AlarmConstants.ALARM NAME SETTING DIALOG
     **********************************************************************************************/
    public  void showNameSettingDialog(View v){

        //Create new Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_name));

        //get Text View
        TextView currentName = (TextView) v;

        //EditText
        final EditText newName = new EditText(this);
        newName.setText(currentName.getText());
        newName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        newName.setSelection(currentName.getText().length());

        //Set Builder
        builder.setView(newName);
        builder.setPositiveButton(this.getString(R.string.wakeup_OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Use the new Name
                onAlarmNameSet(newName.getText().toString());
            }
        });
        builder.setNegativeButton(this.getString(R.string.wakeup_Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        //Set Visible Keyboard to AlarmAlertDialog and show Dialog
        AlertDialog AlarmNameAlert = builder.create();
        AlarmNameAlert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        AlarmNameAlert.show();
    }
    private void onAlarmNameSet(String _newName){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //save Settings
        saveListDataChild(_newName, actualAlarm);

        //reactivate Alarm
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    /***********************************************************************************************
     * DAY SETTING DIALOG
     **********************************************************************************************/
    public void onDaysSet(View v){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //save Button Id
        actualButtonID = v.getId();

        //The Day View has only Toggle Buttons which call this method
        ToggleButton toggle = (ToggleButton) v;

        //when togglebutton is checked set Alarm for this day
        switch(actualButtonID){
            case R.id.wakeup_monday   : alarmConfigurations.get(actualAlarm).setMonday   ((toggle.isChecked()) ? 1 : 0); break;
            case R.id.wakeup_tuesday  : alarmConfigurations.get(actualAlarm).setTuesday  ((toggle.isChecked()) ? 1 : 0); break;
            case R.id.wakeup_wednesday: alarmConfigurations.get(actualAlarm).setWednesday((toggle.isChecked()) ? 1 : 0); break;
            case R.id.wakeup_thursday : alarmConfigurations.get(actualAlarm).setThursday ((toggle.isChecked()) ? 1 : 0); break;
            case R.id.wakeup_friday   : alarmConfigurations.get(actualAlarm).setFriday   ((toggle.isChecked()) ? 1 : 0); break;
            case R.id.wakeup_saturday : alarmConfigurations.get(actualAlarm).setSaturday ((toggle.isChecked()) ? 1 : 0); break;
            case R.id.wakeup_sunday   : alarmConfigurations.get(actualAlarm).setSunday   ((toggle.isChecked()) ? 1 : 0); break;
            default: debug_assertion(true); break;
        }

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);
        AlarmGroupView.invalidateViews();

        //reactivate Alarm
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    /***********************************************************************************************
     * TIME SETTING DIALOG
     **********************************************************************************************/
    public void showTimeSettingsDialog(View v){

        //save Button ID
        actualButtonID = v.getId();

        //Open TimePicker Dialog
        DialogFragment newFragment = new SettingTimeFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }
    public void onTimeSet(TimePicker view, int hourOfDay, int minute){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //save times
        alarmConfigurations.get(actualAlarm).setHour(hourOfDay);
        alarmConfigurations.get(actualAlarm).setMinute(minute);
        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);

        //reactivate Alarm
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    /***********************************************************************************************
     * MINUTE SETTING DIALOG
     **********************************************************************************************/
    public  void showMinuteSettingDialog(View v) {

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Save ID From Button
        actualButtonID = v.getId();

        //TextView to show Value of SeekBar
        final TextView textView = new TextView(v.getContext());
        //SeekBar
        final SeekBar seekBar = new SeekBar(v.getContext());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Set Position of Text
                int val = (progress * (seekBar.getWidth() - 6 * seekBar.getThumbOffset())) / seekBar.getMax();
                String message = Integer.toString(++progress) + "min";
                textView.setText(message);
                textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                textView.setVisibility(TextView.VISIBLE);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //textView.setVisibility(TextView.GONE);
            }
        });
        //LinearLayout
        LinearLayout linearLayout = createAlertLinearLayout(v, textView, seekBar, 99, 1, alarmConfigurations.get(actualAlarm).getSnooze() - 1);

        //Create new Builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_minutes));
        builder.setView(linearLayout);
        builder.setPositiveButton(this.getString(R.string.wakeup_OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Set and Save Vibration Strength
                onSnoozeMinutesSet(seekBar.getProgress());
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(this.getString(R.string.wakeup_Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //Show Builder
        builder.show();
    }
    private void onSnoozeMinutesSet(int _minutes){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Save Snooze Minutes
        alarmConfigurations.get(actualAlarm).setSnooze(_minutes + 1); //we Start with 1 minute

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);

        //reactivate Alarm
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    /***********************************************************************************************
     * MUSIC SET DIALOG
     **********************************************************************************************/
    public  void showMusicSettingDialog(View v){

        actualButtonID = v.getId();

        //Create new Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_song_menu));

        String[] ringtoneMode = { this.getString(R.string.wakeup_music_ringtone), this.getString(R.string.wakeup_music_music)};
        builder.setItems(ringtoneMode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                onMusicSet(which);
                stopMusic(true);
                dialog.dismiss();
            }
        });

        //Show Builder
        builder.show();
    }
    private void onMusicSet(int _modeID){

        //Get All Song Values from the Android Media Content URI
        //Default for Uri is the internal Memory, because it is every time available
        Uri allSongUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        //If the User Chooses the second entry switch to external Files
        if(_modeID == 1)
            allSongUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        //Set Values for the Resolver
        String[] STAR = { "*" };

        //Check if SD Card is Present
        boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if(isSDPresent || _modeID == 0)
        {
            //Resolve ContentURI
            ContentResolver musicResolver = AlarmGroupView.getContext().getContentResolver();
            Cursor cursor = musicResolver.query(allSongUri, STAR, null, null, null);

            //ArrayList for Music Entries
            ArrayList<SongInformation> songList = new ArrayList<>();

            //Search Cursor for Values
            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do{
                        String song_name = cursor
                                .getString(cursor
                                        .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));

                        int song_id = cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Audio.Media._ID));

                        String fullPath = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.DATA));

                        String album_name = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.ALBUM));

                        String artist_name = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.ARTIST));

                        int isMusic = cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));

                        //int album_id = cursor.getInt(cursor
                        //        .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                        //int artist_id = cursor.getInt(cursor
                        //        .getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
                        //int isAlarm = cursor.getInt(cursor
                        //        .getColumnIndex(MediaStore.Audio.Media.IS_ALARM));
                        //int isRingtone = cursor.getInt(cursor
                        //        .getColumnIndex(MediaStore.Audio.Media.IS_RINGTONE));

                        if(isMusic == 1 && _modeID == 1)
                            songList.add(new SongInformation(song_id,  song_name, artist_name, album_name, fullPath));
                        else
                            songList.add(new SongInformation(song_id,  song_name, artist_name, album_name, fullPath));
                    }
                    while(cursor.moveToNext());
                }
                //Choose an Alarm
                chooseAlarmSongDialog(songList);

                //Close Cursor
                cursor.close();
            }
            else
                Toast.makeText(MainActivity.this, R.string.wakeup_music_no_music, Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(MainActivity.this, R.string.wakeup_music_no_sd_card, Toast.LENGTH_SHORT).show();
    }
    private void chooseAlarmSongDialog(final ArrayList<SongInformation> _Songs){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Create new Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_song));

        //Get SongNames from SongInformationArray
        ArrayList<String> songNameArrayList = new ArrayList<>();

        for(SongInformation songs : _Songs){

            //Get Name with Extension and remove it
            String nameWithoutExtension  = songs.getTitle();
            if(nameWithoutExtension != null && nameWithoutExtension.contains("."))
                nameWithoutExtension = nameWithoutExtension.substring(0, nameWithoutExtension.lastIndexOf('.'));

            //Add SongName to list
            songNameArrayList.add(nameWithoutExtension);
        }
        //Get Song Name Array and set it for Alarm Dialog
        final String[] songNameArray = songNameArrayList.toArray(new String[songNameArrayList.size()]);

        //Set Builder
        builder.setItems(songNameArray, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                //Get Song Lengh
                saveSongLength(_Songs.get(which).getPath());

                //save Settings
                saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);

                //reactivate Alarm
                activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());

                dialog.dismiss();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            public void onShow(DialogInterface dialog) {

                ListView songsView = alertDialog.getListView();
                songsView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                        stopMusic(false);

                        Uri fileUri = Uri.parse(_Songs.get(position).getPath());
                        try
                        {
                            prepareMusic(fileUri);
                        } catch (IOException e) {
                            Log.e("Exception: ", e.getMessage());
                        }

                        mediaPlayer.start();
                        return true;
                    }
                });
            }
        });

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                stopMusic(true);
            }
        });

        //Show Builder
        alertDialog.show();
    }
    private void prepareMusic(Uri _SongUri) throws IOException{

        //Check for MediaPlayer
        if(mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        else
            mediaPlayer.reset();

        //Set MediaPlayer Values
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(getApplicationContext(), _SongUri);
        mediaPlayer.prepare();
    }
    private void stopMusic(boolean release){

        //Check for MediaPlayer
        if(mediaPlayer==null)
            return;

        //Stop if Playing
        if(mediaPlayer.isPlaying())
            mediaPlayer.stop();

        //Release and Set null
        if(!release)
            return;

        mediaPlayer.release();
        mediaPlayer = null;
    }

    private void saveSongLength(String _uri){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Get Song Lengh
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(_uri);

        //Get Values from chosen Song
        String durationStr = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long duration = Long.parseLong(durationStr);

        alarmConfigurations.get(actualAlarm).setSongLength((int) (duration / 1000));
        alarmConfigurations.get(actualAlarm).setSongURI(_uri);
    }
    /***********************************************************************************************
     * MUSIC VOLUME DIALOG
     **********************************************************************************************/
    public  void showMusicVolumeSettingDialog(View v){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Save ID From Button
        actualButtonID = v.getId();

        //TextView to show Value of SeekBar
        final TextView textView = new TextView(v.getContext());
        //SeekBar
        final SeekBar seekBar = new SeekBar(v.getContext());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Set Position of Text
                int val = (progress * (seekBar.getWidth() - 4 * seekBar.getThumbOffset())) / seekBar.getMax();
                String message = String.format("%d".toUpperCase(Locale.US), progress);
                textView.setText(message);
                textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                textView.setVisibility(TextView.VISIBLE);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //textView.setVisibility(TextView.GONE);
            }
        });
        //LinearLayout
        LinearLayout linearLayout = createAlertLinearLayout(v, textView, seekBar, 100, 1, alarmConfigurations.get(actualAlarm).getVolume());

        //Create new Builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_song_Volume));
        builder.setView(linearLayout);
        builder.setPositiveButton(this.getString(R.string.wakeup_OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Set and Save Vibration Strength
                onMusicVolumeSet(seekBar.getProgress());
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(this.getString(R.string.wakeup_Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //Show Builder
        builder.show();
    }
    private void onMusicVolumeSet(int _volume){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Set ActualVolume
        alarmConfigurations.get(actualAlarm).setVolume(_volume);

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);

        //reactivate Alarm
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    /***********************************************************************************************
     * MUSIC START TIME DIALOG
     **********************************************************************************************/
    public  void showMusicStartSettingDialog(View v){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Save ID From Button
        actualButtonID = v.getId();

        //TextView to show Value of SeekBar
        final TextView textView = new TextView(v.getContext());
        //SeekBar
        final SeekBar seekBar = new SeekBar(v.getContext());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Set Position of Text
                int val = (progress * (seekBar.getWidth() - 5 * seekBar.getThumbOffset())) / seekBar.getMax();
                //Find Button and set Text
                String startTimeText = String.format(
                        "%02d:%02d".toUpperCase(Locale.US),
                        TimeUnit.SECONDS.toMinutes(progress),
                        progress - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(progress)));
                textView.setText(startTimeText);
                textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                textView.setVisibility(TextView.VISIBLE);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //textView.setVisibility(TextView.GONE);
            }
        });
        //LinearLayout
        LinearLayout linearLayout = createAlertLinearLayout(
                v,
                textView,
                seekBar,
                alarmConfigurations.get(actualAlarm).getSongLength(),
                1,
                alarmConfigurations.get(actualAlarm).getSongStart());

        //Create new Builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_song_Start));
        builder.setView(linearLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Set and Save Vibration Strength
                onMusicStartSet(seekBar.getProgress());
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //Show Builder
        builder.show();
    }
    private void onMusicStartSet(int _seconds){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Set ActualStart
        alarmConfigurations.get(actualAlarm).setSongStart(_seconds);

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);

        //reactivate Alarm
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    /***********************************************************************************************
     * MUSIC FADEIN TIME DIALOG
     **********************************************************************************************/
    public  void showFadeInSettingsDialog(View v){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //GEt ToggleButton
        final ToggleButton fadeInToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_music_toggleFadeIn);
        //Set Vibration Checked
        alarmConfigurations.get(actualAlarm).setFadeIn((fadeInToggle.isChecked())? 1 : 0);

        //Save ID From Button
        actualButtonID = v.getId();

        //Set On LongClickListener
        fadeInToggle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                //Save ID From Button
                actualButtonID = v.getId();

                //TextView to show Value of SeekBar
                final TextView textView = new TextView(v.getContext());
                //Seekbar
                final SeekBar seekBar = new SeekBar(v.getContext());
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                        //Set Position of Text
                        int val = (progress * (seekBar.getWidth() - 5 * seekBar.getThumbOffset())) / seekBar.getMax();
                        String startTimeText = String.format(
                                "%02d:%02d".toUpperCase(Locale.US),
                                TimeUnit.SECONDS.toMinutes(progress),
                                progress - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(progress)));
                        textView.setText(startTimeText);
                        textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        textView.setVisibility(TextView.VISIBLE);
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //textView.setVisibility(TextView.GONE);
                    }
                });
            //LinearLayout
            LinearLayout linearLayout = createAlertLinearLayout(
                    v,
                    textView,
                    seekBar,
                    alarmConfigurations.get(actualAlarm).getSongLength(),
                    1,
                    alarmConfigurations.get(actualAlarm).getSongStart());

            //Create new Builder
            final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle(v.getContext().getString(R.string.wakeup_set_alarm_song_fadeIn));

            //Set Alertdialog View
            builder.setView(linearLayout);
            builder.setPositiveButton(v.getContext().getString(R.string.wakeup_OK), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Set and Save Vibration Strength
                    onFadeInTimeSet(seekBar.getProgress());
                    fadeInToggle.setChecked(true);
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(v.getContext().getString(R.string.wakeup_Cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            //Show Builder
            builder.show();
            return false;
            }
        });
    }
    private void onFadeInTimeSet(int _seconds){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Save Snooze Minutes
        alarmConfigurations.get(actualAlarm).setFadeInTime(_seconds);
        alarmConfigurations.get(actualAlarm).setFadeIn(1); //true

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);

        //reactivate Alarm
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    /***********************************************************************************************
     * MUSIC VIBRATION DIALOG
     **********************************************************************************************/
    public  void showVibrationSettingDialog(View v){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Get ToggleButton
        final ToggleButton vibrationToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_music_toggleVibration);

        //Set Vibration Checked
        alarmConfigurations.get(actualAlarm).setVibration((vibrationToggle.isChecked())? 1 : 0);

        //Save ID From Button
        actualButtonID = v.getId();

        //Set On LongClickListener
        vibrationToggle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                //Save ID From Button
                actualButtonID = v.getId();

                //TextView to show Value of SeekBar
                final TextView textView = new TextView(v.getContext());
                //Seekbar
                final SeekBar seekBar = new SeekBar(v.getContext());
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                        //Set Position of Text
                        int val = (progress * (seekBar.getWidth() - 4 * seekBar.getThumbOffset())) / seekBar.getMax();
                        String message = Integer.toString(progress) + "%";
                        textView.setText(message);
                        textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        textView.setVisibility(TextView.VISIBLE);
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //textView.setVisibility(TextView.GONE);
                    }
                });

                //Linearlayout
                LinearLayout linearLayout = createAlertLinearLayout(v, textView, seekBar, 100, 1, alarmConfigurations.get(actualAlarm).getVibrationStrength());

                //Create new Builder
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(v.getContext().getString(R.string.wakeup_set_alarm_song_vibration));
                //Set Alertdialog View
                builder.setView(linearLayout);
                builder.setPositiveButton(v.getContext().getString(R.string.wakeup_OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Set and Save Vibration Strength
                        onVibrationStrengthSet(seekBar.getProgress());
                        vibrationToggle.setChecked(true);
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(v.getContext().getString(R.string.wakeup_Cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                //Show Builder
                builder.show();
                return false;
            }
        });
    }
    private void onVibrationStrengthSet(int _strength){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Save Vibration Values
        alarmConfigurations.get(actualAlarm).setVibrationStrength(_strength);
        alarmConfigurations.get(actualAlarm).setVibration(1); //true

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);

        //reactivate Alarm
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    /***********************************************************************************************
     * SCEEN LIGHT SETTING DIALOG
     **********************************************************************************************/
    public  void showScreenLightSettingDialog(View v){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //GEt ToggleButton
        final ToggleButton screenToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_light_buttonLight);

        //Set Screen Checked
        alarmConfigurations.get(actualAlarm).setScreen((screenToggle.isChecked())? 1 : 0);

        //Save ID From Button
        actualButtonID = v.getId();

        //Set On LongClickListener
        screenToggle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                //Save ID From Button
                actualButtonID = v.getId();

                //TextView to show Value of SeekBar
                final TextView textView = new TextView(v.getContext());
                //Seekbar
                final SeekBar seekBar = new SeekBar(v.getContext());
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        //Set Position of Text
                        int val = (progress * (seekBar.getWidth() - 4 * seekBar.getThumbOffset())) / seekBar.getMax();
                        String message = Integer.toString(progress + 1)+ "%";
                        textView.setText(message);
                        textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        textView.setVisibility(TextView.VISIBLE);
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //textView.setVisibility(TextView.GONE);
                    }
                });
                //LinearLayout
                LinearLayout linearLayout = createAlertLinearLayout(
                        v,
                        textView,
                        seekBar,
                        99,
                        1,
                        alarmConfigurations.get(actualAlarm).getScreenBrightness() - 1); //We must -1 because we dont want to have zero brightness

                //Create new Builder
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(v.getContext().getString(R.string.wakeup_set_alarm_light_brightness));

                //Set Alertdialog View
                builder.setView(linearLayout);
                builder.setPositiveButton(v.getContext().getString(R.string.wakeup_OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Set and Save Vibration Strength
                        int realprogess = seekBar.getProgress() + 1; //+1 because we don't want to have zero brightness set
                        onScreenBrightnessSet(realprogess);
                        screenToggle.setChecked(true);
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(v.getContext().getString(R.string.wakeup_Cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                //Show Builder
                builder.show();
                return false;
            }
        });
    }
    private void onScreenBrightnessSet(int _brightness){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        alarmConfigurations.get(actualAlarm).setScreenBrightness(_brightness);
        alarmConfigurations.get(actualAlarm).setScreen(1); //true

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);

        //reactivate Alarm
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    public  void showScreenLightStartSettingDialog(View v){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Save ID From Button
        actualButtonID = v.getId();

        //TextView to show Value of SeekBar
        final TextView textView = new TextView(v.getContext());

        //Seek Bar
        final SeekBar seekBar = new SeekBar(v.getContext());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Set Position of Text
                int val = (progress * (seekBar.getWidth() - 6 * seekBar.getThumbOffset())) / seekBar.getMax();
                String message = Integer.toString(progress) + "min";
                textView.setText(message);
                textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                textView.setVisibility(TextView.VISIBLE);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //textView.setVisibility(TextView.GONE);
            }
        });
        //LinearLayout
        LinearLayout linearLayout = createAlertLinearLayout(v, textView, seekBar, 100, 1, alarmConfigurations.get(actualAlarm).getScreenStartTime());

        //Create new Builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_light_minutes));
        builder.setView(linearLayout);
        builder.setPositiveButton(this.getString(R.string.wakeup_OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Set and Save Vibration Strength
                onScreenStartTimeSet(seekBar.getProgress());
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(this.getString(R.string.wakeup_Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //Show Builder
        builder.show();
    }
    private void onScreenStartTimeSet(int _minutes){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Save ScreenStart Minutes
        alarmConfigurations.get(actualAlarm).setScreenStartTime(_minutes);  //we Start with 1 minute

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);

        //reactivate Alarm
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    /***********************************************************************************************
     * SCREEN COLOR SETTING DIALOG
     **********************************************************************************************/
    public  void showScreenColor1SettingDialog(View v){

        final Button bColor = (Button) v;

        //TODO Need a better ColorPicker without 0xfffff bug
        ColorPickingDialog colorPicker = new ColorPickingDialog(v.getContext(), 0xffffff, new ColorPickingDialog.OnColorSelectedListener() {
            @Override
            public void colorSelected(Integer color) {
                bColor.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                onColorSet(bColor, color);
            }
        });
        colorPicker.setTitle(v.getContext().getString(R.string.wakeup_set_alarm_color));
        colorPicker.show();
    }
    private void onColorSet(Button _buttonView, int _color){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        final String color1 = this.getString(R.string.wakeup_light_screen_color1);
        final String color2 = this.getString(R.string.wakeup_light_screen_color2);

        if(color1.equals(_buttonView.getText().toString()))
            alarmConfigurations.get(actualAlarm).setLightColor1(_color);
        else if(color2.equals(_buttonView.getText().toString()))
            alarmConfigurations.get(actualAlarm).setLightColor2(_color);
        else
            debug_assertion(true);

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);

        //reactivate Alarm
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    /***********************************************************************************************
     * SCREEN COLOR FADE SETTING DIALOG
     **********************************************************************************************/
    public void showScreenColorFadeSettingDialog(View v){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //GEt ToggleButton
        final ToggleButton screenFadeToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_light_buttonScreenFade);

        //Set Vibration Checked
        alarmConfigurations.get(actualAlarm).setLightFade((screenFadeToggle.isChecked())? 1 : 0);

        //Save ID From Button
        actualButtonID = v.getId();

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);

        //reactivate Alarm
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    /***********************************************************************************************
     * LED LIGHT SETTING DIALOG
     **********************************************************************************************/
    public  void showLEDLightSettingDialog(View v){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //GEt ToggleButton
        final ToggleButton LEDToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_light_buttonLED);

        //Set LED Checked
        alarmConfigurations.get(actualAlarm).setLED((LEDToggle.isChecked())? 1 : 0);

        //Save ID From Button
        actualButtonID = v.getId();

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);

        //reactivate Alarm
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }
    public  void showLEDLightStartSettingDialog(View v){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Save ID From Button
        actualButtonID = v.getId();

        //TextView to show Value of SeekBar
        final TextView textView = new TextView(v.getContext());

        //Seek Bar
        final SeekBar seekBar = new SeekBar(v.getContext());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Set Position of Text
                int val = (progress * (seekBar.getWidth() - 6 * seekBar.getThumbOffset())) / seekBar.getMax();
                String message = Integer.toString(progress) + "min";
                textView.setText(message);
                textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                textView.setVisibility(TextView.VISIBLE);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //textView.setVisibility(TextView.GONE);
            }
        });
        //LinearLayout
        LinearLayout linearLayout = createAlertLinearLayout(
                v,
                textView,
                seekBar,
                100,
                1,
                alarmConfigurations.get(actualAlarm).getLEDStartTime());

        //Create new Builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_LED_time));
        builder.setView(linearLayout);
        builder.setPositiveButton(this.getString(R.string.wakeup_OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Set and Save Vibration Strength
                onLEDStartTimeSet(seekBar.getProgress());
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(this.getString(R.string.wakeup_Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //Show Builder
        builder.show();
    }
    private void onLEDStartTimeSet(int _minutes){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Save LEDSTartTime Minutes
        alarmConfigurations.get(actualAlarm).setLEDStartTime(_minutes);

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);

        //reactivate Alarm
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    /***********************************************************************************************
     * OPTIONSMENU
     **********************************************************************************************/
    // TODO OptionsMenu: Set Different Colors for All Elements
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
