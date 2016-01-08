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
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
                    implements TimePickerDialog.OnTimeSetListener{
    //ExpendableList
    ExpandableListAdapter expListAdapter;
    ExpandableListView    expListView;

    List<String>          expListDataHeader;
    List<String>          expListDataAlarm;
    List<String>          expListDataMusicURI;

    LinkedHashMap<String,
            LinkedHashMap<String,
                    LinkedHashMap<String, Integer>>> expListDataChild;

    //Mediaplayer
    private MediaPlayer mediaPlayer;

    //Actual Alarm Values
    private int actualAlarm    =-1;
    private int actualAlarmSet = 0;

    //Time
    private int actualHour     = AlarmConstants.ACTUAL_TIME_HOUR;
    private int actualMin      = AlarmConstants.ACTUAL_TIME_MINUTE;
    private int actualSnooze   = AlarmConstants.ACTUAL_TIME_SNOOZE;

    //Days
    private int isMonday    = AlarmConstants.ACTUAL_DAY_MONDAY;
    private int isTuesday   = AlarmConstants.ACTUAL_DAY_TUESDAY;
    private int isWednesday = AlarmConstants.ACTUAL_DAY_WEDNESDAY;
    private int isThursday  = AlarmConstants.ACTUAL_DAY_THURSDAY;
    private int isFriday    = AlarmConstants.ACTUAL_DAY_FRIDAY;
    private int isSaturday  = AlarmConstants.ACTUAL_DAY_SATURDAY;
    private int isSunday    = AlarmConstants.ACTUAL_DAY_SUNDAY;

    //Music
    private String actualSongURI = AlarmConstants.ACTUAL_MUSIC_SONG_URI;
    private int actualSongStart  = AlarmConstants.ACTUAL_MUSIC_START;
    private int actualSongLength = AlarmConstants.ACTUAL_MUSIC_LENGTH;
    private int actualVolume     = AlarmConstants.ACTUAL_MUSIC_VOLUME;
    private int actualFadeIn     = AlarmConstants.ACTUAL_MUSIC_FADE_IN;
    private int actualFadeInTime = AlarmConstants.ACTUAL_MUSIC_FADE_IN_TIME;
    private int actualVibra      = AlarmConstants.ACTUAL_MUSIC_VIBRATION;
    private int actualVibraStr   = AlarmConstants.ACTUAL_MUSIC_VIBRATION_STRENGTH;

    //Light
    private int actualScreen           = AlarmConstants.ACTUAL_SCREEN;
    private int actualScreenBrightness = AlarmConstants.ACTUAL_SCREEN_BRIGHTNESS;
    private int actualScreenStartTime  = AlarmConstants.ACTUAL_SCREEN_START;
    private int actualLightColor1      = AlarmConstants.ACTUAL_SCREEN_COLOR1;
    private int actualLightColor2      = AlarmConstants.ACTUAL_SCREEN_COLOR2;
    private int actualLightFade        = AlarmConstants.ACTUAL_SCREEN_COLOR_FADE;
    private int actualLightLED         = AlarmConstants.ACTUAL_LED;
    private int actualLightLEDStartTime= AlarmConstants.ACTUAL_LED_START;

    //Last Clicked Button
    private int actualButtonID    = 0;
    private int actualAlarmNameID = 0;

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
        expListView = (ExpandableListView) findViewById(R.id.wakeup_timer_expendbleList);

        //prepare list data
        prepareLisData();
        expListAdapter = new ExpandableListAdapter(this, expListDataAlarm, expListDataMusicURI, expListDataHeader, expListDataChild);

        //setting list adapter
        expListView.setAdapter(expListAdapter);
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return false;
            }
        });
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {

                loadValuesNew(groupPosition);
                actualAlarm = groupPosition;

                if (groupPosition != previousGroup)
                    expListView.collapseGroup(previousGroup);

                previousGroup = groupPosition;
                expListView.invalidateViews();
            }
        });
        //Floating ActionButton/////////////////////////////////////////////////////////////////////
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Add New Alarm
                saveListDataChild(AlarmConstants.ALARM);
                expListAdapter = new ExpandableListAdapter(
                        expListView.getContext(),
                        expListDataAlarm,
                        expListDataMusicURI,
                        expListDataHeader,
                        expListDataChild);
                expListView.setAdapter(expListAdapter);
            }
        });
    }

    /***********************************************************************************************
     * DATA VALUES
     **********************************************************************************************/
    private void prepareListDataValues(String _alarmName,String _musicURI, int _id, int[] _time, int[] _days, int[] _music, int[] _light, int _alarmSet){

        //Load sharedPrefereces
        String settingName = AlarmConstants.ALARM + _id;
        if(!expListDataAlarm.contains(settingName)){

            expListDataAlarm.add(settingName);
            expListDataMusicURI.add(_musicURI);
            expListDataHeader.add(_alarmName);
        }

        //Adding Child Data
        LinkedHashMap<String,
                LinkedHashMap<String, Integer>> newAlarm = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> alarmvalueTime    = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> alarmvalueDay     = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> alarmvalueMusic   = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> alarmvalueLight   = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> alarmvalueDelete  = new LinkedHashMap<>();

        //Putting Value for each child
        //Time
        alarmvalueTime.clear();
        alarmvalueTime.put(AlarmConstants.ALARM_TIME_HOUR   , _time[0]);
        alarmvalueTime.put(AlarmConstants.ALARM_TIME_MINUTES, _time[1]);
        alarmvalueTime.put(AlarmConstants.ALARM_TIME_SNOOZE , _time[2]);
        newAlarm.put(AlarmConstants.WAKEUP_TIME, alarmvalueTime);

        //Day
        alarmvalueDay.clear();
        alarmvalueDay.put(AlarmConstants.ALARM_DAY_MONDAY   , _days[0]);
        alarmvalueDay.put(AlarmConstants.ALARM_DAY_TUESDAY  , _days[1]);
        alarmvalueDay.put(AlarmConstants.ALARM_DAY_WEDNESDAY, _days[2]);
        alarmvalueDay.put(AlarmConstants.ALARM_DAY_THURSDAY , _days[3]);
        alarmvalueDay.put(AlarmConstants.ALARM_DAY_FRIDAY   , _days[4]);
        alarmvalueDay.put(AlarmConstants.ALARM_DAY_SATURDAY , _days[5]);
        alarmvalueDay.put(AlarmConstants.ALARM_DAY_SUNDAY   , _days[6]);

        newAlarm.put(AlarmConstants.WAKEUP_DAYS, alarmvalueDay);

        //Music
        alarmvalueMusic.clear();
        alarmvalueMusic.put(AlarmConstants.ALARM_MUSIC_SONGSTART       , _music[0]);
        alarmvalueMusic.put(AlarmConstants.ALARM_MUSIC_VOLUME          , _music[1]);
        alarmvalueMusic.put(AlarmConstants.ALARM_MUSIC_FADEIN          , _music[2]);
        alarmvalueMusic.put(AlarmConstants.ALARM_MUSIC_FADEINTIME      , _music[3]);
        alarmvalueMusic.put(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV , _music[4]);
        alarmvalueMusic.put(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE , _music[5]);

        newAlarm.put(AlarmConstants.WAKEUP_MUSIC, alarmvalueMusic);

        //Light
        alarmvalueLight.clear();
        alarmvalueLight.put(AlarmConstants.ALARM_LIGHT_SCREEN            , _light[0]);
        alarmvalueLight.put(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS , _light[1]);
        alarmvalueLight.put(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME , _light[2]);
        alarmvalueLight.put(AlarmConstants.ALARM_LIGHT_COLOR1            , _light[3]);
        alarmvalueLight.put(AlarmConstants.ALARM_LIGHT_COLOR2            , _light[4]);
        alarmvalueLight.put(AlarmConstants.ALARM_LIGHT_FADECOLOR         , _light[5]);
        alarmvalueLight.put(AlarmConstants.ALARM_LIGHT_USELED            , _light[6]);
        alarmvalueLight.put(AlarmConstants.ALARM_LIGHT_LED_START_TIME    , _light[7]);

        newAlarm.put(AlarmConstants.WAKEUP_LIGHT, alarmvalueLight);

        //Delete
        alarmvalueDelete.clear();
        alarmvalueDelete.put(AlarmConstants.ALARM_SET, _alarmSet);
        newAlarm.put(AlarmConstants.WAKEUP_DELETE, alarmvalueDelete);

        expListDataChild.put(expListDataAlarm.get(_id), newAlarm);

        if(expListAdapter != null)
            expListAdapter.notifyDataSetChanged(expListDataAlarm, expListDataMusicURI, expListDataHeader, expListDataChild);
    }
    private void prepareLisData(){
        //List
        expListDataHeader = new ArrayList<>();
        expListDataAlarm  = new ArrayList<>();
        expListDataMusicURI= new ArrayList<>();
        expListDataChild  = new LinkedHashMap<>();

        SharedPreferences information = getSharedPreference(AlarmConstants.WAKEUP_TIMER_INFO);
        int amount = information.getInt(AlarmConstants.ALARM_VALUE, 0);

        if(amount == 0) {
            //No Alarm
            //Putting Value for each child
            int[] time  = {0, 0, 0};         // hour, minute
            int[] days  = {0,0,0,0,0,0,0};  // Monday - Sunday
            int[] music = {0,0,0,0,0,0};      // Song, StartTime, Volume, FadIn, FadeInTime, Vibration, Vibration Strength
            int[] light = {0,0,0,0,0,0,0,0};    // UseScreen, ScreenColor1, ScreenColor2, Fadecolor, FadeTime, UseLED

            prepareListDataValues(this.getString(R.string.wakeup_no_alarm), Settings.System.DEFAULT_ALARM_ALERT_URI.getPath(), 0, time, days, music, light, 0);
        }
        else {

            for (int id = 0; id < amount; ++id) {

                //sharedPrefereces
                SharedPreferences settings = getSharedPreference(AlarmConstants.WAKEUP_TIMER, id);

                //GetData
                String name = settings.getString(AlarmConstants.ALARM_NAME, this.getString(R.string.wakeup_no_alarm));

                String musicURI = settings.getString(AlarmConstants.ALARM_MUSIC_SONGID, Settings.System.DEFAULT_ALARM_ALERT_URI.getPath());

                //Putting Value for each child
                int[] time  = {
                        settings.getInt(AlarmConstants.ALARM_TIME_HOUR   , AlarmConstants.ACTUAL_TIME_HOUR),
                        settings.getInt(AlarmConstants.ALARM_TIME_MINUTES, AlarmConstants.ACTUAL_TIME_MINUTE),
                        settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE , AlarmConstants.ACTUAL_TIME_SNOOZE)};    // hour, minute, snooze

                int[] days  = {
                        settings.getInt(AlarmConstants.ALARM_DAY_MONDAY   , AlarmConstants.ACTUAL_DAY_MONDAY),
                        settings.getInt(AlarmConstants.ALARM_DAY_TUESDAY  , AlarmConstants.ACTUAL_DAY_TUESDAY),
                        settings.getInt(AlarmConstants.ALARM_DAY_WEDNESDAY, AlarmConstants.ACTUAL_DAY_WEDNESDAY),
                        settings.getInt(AlarmConstants.ALARM_DAY_THURSDAY , AlarmConstants.ACTUAL_DAY_THURSDAY),
                        settings.getInt(AlarmConstants.ALARM_DAY_FRIDAY   , AlarmConstants.ACTUAL_DAY_FRIDAY),
                        settings.getInt(AlarmConstants.ALARM_DAY_SATURDAY , AlarmConstants.ACTUAL_DAY_SATURDAY),
                        settings.getInt(AlarmConstants.ALARM_DAY_SUNDAY   , AlarmConstants.ACTUAL_DAY_SUNDAY)};  // Monday - Sunday

                int[] music = {
                        settings.getInt(AlarmConstants.ALARM_MUSIC_SONGSTART      , AlarmConstants.ACTUAL_MUSIC_START),
                        settings.getInt(AlarmConstants.ALARM_MUSIC_VOLUME         , AlarmConstants.ACTUAL_MUSIC_VOLUME),
                        settings.getInt(AlarmConstants.ALARM_MUSIC_FADEIN         , AlarmConstants.ACTUAL_MUSIC_FADE_IN),
                        settings.getInt(AlarmConstants.ALARM_MUSIC_FADEINTIME     , AlarmConstants.ACTUAL_MUSIC_FADE_IN_TIME),
                        settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV, AlarmConstants.ACTUAL_MUSIC_VIBRATION),
                        settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE, AlarmConstants.ACTUAL_MUSIC_VIBRATION_STRENGTH)};      // StartTime, Volume, FadIn, FadeInTime

                int[] light = {
                        settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN           , AlarmConstants.ACTUAL_SCREEN),
                        settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS, AlarmConstants.ACTUAL_SCREEN_BRIGHTNESS),
                        settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME, AlarmConstants.ACTUAL_SCREEN_START),
                        settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR1           , AlarmConstants.ACTUAL_SCREEN_COLOR1),
                        settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR2           , AlarmConstants.ACTUAL_SCREEN_COLOR2),
                        settings.getInt(AlarmConstants.ALARM_LIGHT_FADECOLOR        , AlarmConstants.ACTUAL_SCREEN_COLOR_FADE),
                        settings.getInt(AlarmConstants.ALARM_LIGHT_USELED           , AlarmConstants.ACTUAL_LED),
                        settings.getInt(AlarmConstants.ALARM_LIGHT_LED_START_TIME   , AlarmConstants.ACTUAL_LED_START)};// UseScreen, ScreenColor1, ScreenColor2, Fadecolor, FadeTime, UseLED

                int alarmSet = settings.getInt(AlarmConstants.ALARM_SET, 0);
                prepareListDataValues(name, musicURI, id, time, days, music, light, alarmSet);
            }
        }
    }

    private void changeListData(String _name, int _id){
        //Load sharedPrefereces
        SharedPreferences.Editor editor = getSharedPreferenceEditor(AlarmConstants.WAKEUP_TIMER, _id);

        String alarmName = AlarmConstants.ALARM + _id;

        if(!expListDataAlarm.contains(alarmName))
            expListDataAlarm.add(alarmName);

        //put StringSet back
        editor.putString(AlarmConstants.ALARM_NAME, _name);
        editor.putInt(AlarmConstants.ALARM_SET    , actualAlarmSet);

        //Time
        editor.putInt(AlarmConstants.ALARM_TIME_MINUTES  , actualMin);
        editor.putInt(AlarmConstants.ALARM_TIME_HOUR     , actualHour);
        editor.putInt(AlarmConstants.ALARM_TIME_SNOOZE   , actualSnooze);

        //Days
        editor.putInt(AlarmConstants.ALARM_DAY_MONDAY    , isMonday);
        editor.putInt(AlarmConstants.ALARM_DAY_TUESDAY   , isTuesday);
        editor.putInt(AlarmConstants.ALARM_DAY_WEDNESDAY , isWednesday);
        editor.putInt(AlarmConstants.ALARM_DAY_THURSDAY  , isThursday);
        editor.putInt(AlarmConstants.ALARM_DAY_FRIDAY    , isFriday);
        editor.putInt(AlarmConstants.ALARM_DAY_SATURDAY  , isSaturday);
        editor.putInt(AlarmConstants.ALARM_DAY_SUNDAY    , isSunday);

        //Music
        editor.putString(AlarmConstants.ALARM_MUSIC_SONGID      , actualSongURI);
        editor.putInt(AlarmConstants.ALARM_MUSIC_VOLUME         , actualVolume);
        editor.putInt(AlarmConstants.ALARM_MUSIC_SONGSTART      , actualSongStart);
        editor.putInt(AlarmConstants.ALARM_MUSIC_SONGLENGTH     , actualSongLength);
        editor.putInt(AlarmConstants.ALARM_MUSIC_FADEIN         , actualFadeIn);
        editor.putInt(AlarmConstants.ALARM_MUSIC_FADEINTIME     , actualFadeInTime);
        editor.putInt(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV, actualVibra);
        editor.putInt(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE, actualVibraStr);

        //Light
        editor.putInt(AlarmConstants.ALARM_LIGHT_SCREEN            , actualScreen);
        editor.putInt(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS , actualScreenBrightness);
        editor.putInt(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME , actualScreenStartTime);
        editor.putInt(AlarmConstants.ALARM_LIGHT_COLOR1            , actualLightColor1);
        editor.putInt(AlarmConstants.ALARM_LIGHT_COLOR2            , actualLightColor2);
        editor.putInt(AlarmConstants.ALARM_LIGHT_FADECOLOR         , actualLightFade);
        editor.putInt(AlarmConstants.ALARM_LIGHT_USELED            , actualLightLED);
        editor.putInt(AlarmConstants.ALARM_LIGHT_LED_START_TIME    , actualLightLEDStartTime);

        //apply Values to settings
        editor.apply();
    }
    private void saveListDataChild(String _name){

        SharedPreferences information = getSharedPreference(AlarmConstants.WAKEUP_TIMER_INFO);
        int amount = information.getInt(AlarmConstants.ALARM_VALUE, 0);

        saveListDataChild(_name, amount);
    }
    private void saveListDataChild(String _name, int _id){

        SharedPreferences information   = getSharedPreference(AlarmConstants.WAKEUP_TIMER_INFO);
        int amount = information.getInt(AlarmConstants.ALARM_VALUE, 0);

        //changelistData
        if(amount == 0){
            changeListData(_name, 0);
            ++amount;
        }
        else
        if(_id < amount)
            changeListData(_name, _id);
        else{
            loadValuesNew(amount+1);
            changeListData(_name, amount++);
        }

        SharedPreferences.Editor editor = information.edit();
        editor.putInt(AlarmConstants.ALARM_VALUE, amount);
        editor.apply();
        //prepare new List Data
        prepareLisData();
    }

    public void deleteChild(View v){

        Button deleteButton = (Button) v;

        deleteButton.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View v) {
                deleteListDataChild();
                return false;
            }
        });
    }
    private void deleteListDataChild(){

        int _id = actualAlarm;

        expListView.collapseGroup(_id);

        //Load SharedPreferences
        SharedPreferences information = getApplicationContext().getSharedPreferences(AlarmConstants.WAKEUP_TIMER_INFO, Context.MODE_PRIVATE);

        int amount = information.getInt(AlarmConstants.ALARM_VALUE, 0);

        if(amount > 0) {
            for (int id = _id; id < amount - 1; ++id) {
                SharedPreferences.Editor editorNew = getSharedPreferenceEditor(AlarmConstants.WAKEUP_TIMER, id);
                SharedPreferences settingsOld      = getSharedPreference(AlarmConstants.WAKEUP_TIMER, ++id);
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
            }
            SharedPreferences.Editor editor = getSharedPreferenceEditor(AlarmConstants.WAKEUP_TIMER, amount);

            editor.clear();
            editor.apply();

            SharedPreferences.Editor editorInf = information.edit();
            editorInf.putInt(AlarmConstants.ALARM_VALUE, --amount);
            editorInf.apply();

            //prepare new List Data
            prepareLisData();
        }
    }

    private void loadValuesNew(int _alarmID){

        actualAlarm = _alarmID;
        //save Settings
        SharedPreferences settings = getSharedPreference(AlarmConstants.WAKEUP_TIMER, actualAlarm);

        //AlarmManage alarmManager = new AlarmManage(this);
        //boolean checked = alarmManager.checkForPendingIntent(actualAlarm);
        //actualAlarmSet = (checked) ? 1 : 0;
        actualAlarmSet = settings.getInt(AlarmConstants.ALARM_SET, 0);

        //Putting Value for each child
        Calendar calendar = Calendar.getInstance();
        //Time
        actualHour   = settings.getInt(AlarmConstants.ALARM_TIME_HOUR   , calendar.get(Calendar.HOUR_OF_DAY));
        actualMin    = settings.getInt(AlarmConstants.ALARM_TIME_MINUTES, calendar.get(Calendar.MINUTE));
        actualSnooze = settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE , AlarmConstants.ACTUAL_TIME_SNOOZE);    // hour, minute, snooze

        //Days
        isMonday    = settings.getInt(AlarmConstants.ALARM_DAY_MONDAY   , AlarmConstants.ACTUAL_DAY_MONDAY);
        isTuesday   = settings.getInt(AlarmConstants.ALARM_DAY_TUESDAY  , AlarmConstants.ACTUAL_DAY_TUESDAY);
        isWednesday = settings.getInt(AlarmConstants.ALARM_DAY_WEDNESDAY, AlarmConstants.ACTUAL_DAY_WEDNESDAY);
        isThursday  = settings.getInt(AlarmConstants.ALARM_DAY_THURSDAY , AlarmConstants.ACTUAL_DAY_THURSDAY);
        isFriday    = settings.getInt(AlarmConstants.ALARM_DAY_FRIDAY   , AlarmConstants.ACTUAL_DAY_FRIDAY);
        isSaturday  = settings.getInt(AlarmConstants.ALARM_DAY_SATURDAY , AlarmConstants.ACTUAL_DAY_SATURDAY);
        isSunday    = settings.getInt(AlarmConstants.ALARM_DAY_SUNDAY   , AlarmConstants.ACTUAL_DAY_SUNDAY); // Monday - Sunday

        //Load Music
        actualSongURI   = settings.getString(AlarmConstants.ALARM_MUSIC_SONGID      , AlarmConstants.ACTUAL_MUSIC_SONG_URI);
        actualSongStart = settings.getInt(AlarmConstants.ALARM_MUSIC_SONGSTART      , AlarmConstants.ACTUAL_MUSIC_START);
        actualSongLength= settings.getInt(AlarmConstants.ALARM_MUSIC_SONGLENGTH     , AlarmConstants.ACTUAL_MUSIC_LENGTH);
        actualVolume    = settings.getInt(AlarmConstants.ALARM_MUSIC_VOLUME         , AlarmConstants.ACTUAL_MUSIC_VOLUME);
        actualFadeIn    = settings.getInt(AlarmConstants.ALARM_MUSIC_FADEIN         , AlarmConstants.ACTUAL_MUSIC_FADE_IN);
        actualFadeInTime= settings.getInt(AlarmConstants.ALARM_MUSIC_FADEINTIME     , AlarmConstants.ACTUAL_MUSIC_FADE_IN_TIME);
        actualVibra     = settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV, AlarmConstants.ACTUAL_MUSIC_VIBRATION);
        actualVibraStr  = settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE, AlarmConstants.ACTUAL_MUSIC_VIBRATION_STRENGTH);// Song, StartTime, Volume, FadIn, FadeInTime

        //Load Light
        actualScreen           = settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN            , AlarmConstants.ACTUAL_SCREEN);
        actualScreenBrightness = settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS , AlarmConstants.ACTUAL_SCREEN_BRIGHTNESS);
        actualScreenStartTime  = settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME , AlarmConstants.ACTUAL_SCREEN_START);
        actualLightColor1      = settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR1            , AlarmConstants.ACTUAL_SCREEN_COLOR1);
        actualLightColor2      = settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR2            , AlarmConstants.ACTUAL_SCREEN_COLOR2);
        actualLightFade        = settings.getInt(AlarmConstants.ALARM_LIGHT_FADECOLOR         , AlarmConstants.ACTUAL_SCREEN_COLOR_FADE);
        actualLightLED         = settings.getInt(AlarmConstants.ALARM_LIGHT_USELED            , AlarmConstants.ACTUAL_LED);
        actualLightLEDStartTime= settings.getInt(AlarmConstants.ALARM_LIGHT_LED_START_TIME    , AlarmConstants.ACTUAL_LED_START);// UseScreen, ScreenColor1, ScreenColor2, Fadecolor, FadeTime, UseLED
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private SharedPreferences getSharedPreference(String _settingName){
        return getApplicationContext().getSharedPreferences(_settingName, Context.MODE_PRIVATE);
    }
    private SharedPreferences getSharedPreference(String _settingName, int _actualAlarm){
        return getApplicationContext().getSharedPreferences(_settingName + Integer.toString(_actualAlarm), Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getSharedPreferenceEditor(String _settingsName){
        return getSharedPreference(_settingsName).edit();
    }

    private SharedPreferences.Editor getSharedPreferenceEditor(String _settingsName, int _actualAlarm){
        return getSharedPreference(_settingsName, _actualAlarm).edit();
    }

    private void saveSettings(String _settingName, int _actualAlarm, String _alarmName){
        //save Settings
        saveSettings(_settingName, _actualAlarm, _alarmName, _alarmName);
    }
    private void saveSettings(String _settingName, int _actualAlarm, String _alarmIdentifier, String _alarmName){
        //save Settings
        SharedPreferences settings = getSharedPreference(_settingName, _actualAlarm);
        saveListDataChild(settings.getString(_alarmIdentifier, _alarmName), _actualAlarm);
        prepareLisData();
    }

    private LinearLayout createAlertLinearLayout(View v, TextView textView, SeekBar seekBar, int _max, int _increment, int _progress){

        //LinearLayout
        LinearLayout linearLayout = new LinearLayout(v.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //TextView to show Value of SeekBar
        textView.setVisibility(TextView.INVISIBLE);
        linearLayout.addView(textView);

        //Seekbar
        seekBar.setMax(_max);
        seekBar.setKeyProgressIncrement(_increment);
        seekBar.setProgress(_progress);
        linearLayout.addView(seekBar);

        return linearLayout;
    }
    /***********************************************************************************************
     * Set New Alarm
     **********************************************************************************************/

    public void setNewAlarm(View v){

        actualButtonID = v.getId();

        ToggleButton activeAlarmToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_setAlarmButton);

        boolean checked = activeAlarmToggle.isChecked();

        actualAlarmSet = (checked) ? 1 : 0;
        activateAlarm(checked);

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);
    }

    private void activateAlarm(boolean active){

        AlarmManage newAlarm = new AlarmManage(getApplicationContext());

        if(active)
            newAlarm.setNewAlarm(actualAlarm, false, 0);
        else
            newAlarm.cancelAlarm(actualAlarm);
    }

    /***********************************************************************************************
     * AlarmConstants.ALARM NAME SETTING DIALOG
     **********************************************************************************************/
    public void showNameSettingDialog(View v){

        //save Text ID
        actualAlarmNameID = v.getId();

        //Create new Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_name));

        //EditText
        final EditText newName = new EditText(this);
        newName.setInputType(InputType.TYPE_CLASS_TEXT);

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

        builder.show();
    }

    private void onAlarmNameSet(String _newName){

        //Set new Alarm name
        TextView newNameView = (TextView) findViewById(actualAlarmNameID);
        newNameView.setText(_newName);

        //save Settings
        saveListDataChild(_newName, actualAlarm);
        prepareLisData();
    }

    /***********************************************************************************************
     * DAY SETTING DIALOG
     **********************************************************************************************/
    public void onDaysSet(View v){

        //save Button Id
        actualButtonID = v.getId();

        //The Day View has only Toggle Buttons which call this method
        ToggleButton toggle = (ToggleButton) v;

        //when togglebutton is checked set Alarm for this day
        switch(actualButtonID){
            case R.id.wakeup_monday   : isMonday    = (toggle.isChecked()) ? 1 : 0; break;
            case R.id.wakeup_tuesday  : isTuesday   = (toggle.isChecked()) ? 1 : 0; break;
            case R.id.wakeup_wednesday: isWednesday = (toggle.isChecked()) ? 1 : 0; break;
            case R.id.wakeup_thursday : isThursday  = (toggle.isChecked()) ? 1 : 0; break;
            case R.id.wakeup_friday   : isFriday    = (toggle.isChecked()) ? 1 : 0; break;
            case R.id.wakeup_saturday : isSaturday  = (toggle.isChecked()) ? 1 : 0; break;
            case R.id.wakeup_sunday   : isSunday    = (toggle.isChecked()) ? 1 : 0; break;
            default: break; //TODO Logging Error
        }

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);
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
        //save times
        actualHour = hourOfDay;
        actualMin  = minute;

        //Set Button Text
        String timeText = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute);

        Button bTime = (Button) findViewById(actualButtonID);
        bTime.setText(timeText);

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);
    }

    /***********************************************************************************************
     * MINUTE SETTING DIALOG
     **********************************************************************************************/
    public void showMinuteSettingDialog(View v) {

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
        LinearLayout linearLayout = createAlertLinearLayout(v, textView, seekBar, 99, 1, actualSnooze - 1);

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
        //Save Snooze Minutes
        actualSnooze = _minutes + 1;  //we Start with 1 minute

        //Set Button Text
        String timeText = this.getString(R.string.wakeup_time_snooze) + " " + actualSnooze + " " + this.getString(R.string.wakeup_time_minutes);

        Button bSnooze = (Button) findViewById(actualButtonID);
        bSnooze.setText(timeText);

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);
    }
    /***********************************************************************************************
     * MUSIC SET DIALOG
     **********************************************************************************************/
    public void showMusicSettingDialog(View v){

        actualButtonID = v.getId();

        //Create new Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_song_menu));

        String[] ringtoneMode = { this.getString(R.string.wakeup_music_ringtone), this.getString(R.string.wakeup_music_music)};
        builder.setItems(ringtoneMode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                onMusicSet(which);
                stopMusic();
                dialog.dismiss();
            }
        });

        //Show Builder
        builder.show();
    }
    
    private void onMusicSet(int _modeID){
        //Get All Song Values from the Android Media Content URI
        //Default for Uri is the internal Memory, because it is everytime available
        Uri allSongUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        //If the User Chooses the second entry switch to external Files
        if(_modeID == 1)
            allSongUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        //Set Values for the Resolver
        String[] STAR = { "*" };

        //Check if SD Card is Present
        boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if(isSDPresent || _modeID == 0){

            //Resolve ContentURI
            ContentResolver musicResolver = expListView.getContext().getContentResolver();
            Cursor cursor = musicResolver.query(allSongUri, STAR, null, null, null);

            //ArrayList for MusicEntrys
            ArrayList<SongInformation> songList = new ArrayList<>();

            //Search Cursor for Values
            if(cursor != null){

                if(cursor.moveToFirst()){
                    do{
                        String song_name = cursor
                                .getString(cursor
                                        .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));

                        int song_id = cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Audio.Media._ID));

                        String fullpath = cursor.getString(cursor
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
                            songList.add(new SongInformation(song_id,  song_name, artist_name, album_name, fullpath));
                        else
                            songList.add(new SongInformation(song_id,  song_name, artist_name, album_name, fullpath));
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

                //Get Values from Choosen Song
                actualSongURI = _Songs.get(which).getPath();

                //Get Values for Button
                String newMusicURI = _Songs.get(which).getPath();
                String newMusicText = newMusicURI.substring(newMusicURI.lastIndexOf('/') + 1);
                newMusicText = newMusicText.substring(0, newMusicText.lastIndexOf('.'));

                Button musicButton = (Button) findViewById(actualButtonID);
                musicButton.setText(newMusicText);

                //Get Song Lengh
                MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
                metaRetriever.setDataSource(newMusicURI);
                String durationStr = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long duration = Long.parseLong(durationStr);
                actualSongLength = (int) (duration / 1000);

                //save Settings
                saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);

                dialog.dismiss();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            public void onShow(DialogInterface dialog) {

                ListView songsView = alertDialog.getListView();
                songsView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                        Uri fileUri = Uri.parse(_Songs.get(position).getPath());

                        try {
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
                stopMusic();
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

        //Set Mediaplayer Values
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(getApplicationContext(), _SongUri);
        mediaPlayer.prepare();
    }

    private void stopMusic(){

        //Check for Mediaplayer
        if(mediaPlayer!=null){

            //Stop if Playing
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();

            //Release and Set null
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /***********************************************************************************************
     * MUSIC VOLUME DIALOG
     **********************************************************************************************/
    public void showMusicVolumeSettingDialog(View v){

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
                String message = String.format("%d", progress);
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
        LinearLayout linearLayout = createAlertLinearLayout(v, textView, seekBar, 100, 1, actualVolume);

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

        //Set ActualVolume
        actualVolume = _volume;

        //Find Button and set Text
        String volumeText = _volume + "%";

        Button bVolume = (Button) findViewById(actualButtonID);
        bVolume.setText(volumeText);

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);
    }

    /***********************************************************************************************
     * MUSIC START TIME DIALOG
     **********************************************************************************************/
    public void showMusicStartSettingDialog(View v){

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
                //Find Button and set Text
                String startTimeText = String.format(
                        "%02d:%02d",
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
        LinearLayout linearLayout = createAlertLinearLayout(v, textView, seekBar, actualSongLength, 1, actualSongStart);

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

        //Set ActualStart
        actualSongStart = _seconds;

        //actualFadeInTime = (actualFadeInTime > actualSongLength - actualSongStart)? actualFadeInTime = 0 : actualFadeInTime;

        //Find Button and set Text
        String startTimeText = String.format(
                "%02d:%02d",
                TimeUnit.SECONDS.toMinutes(_seconds),
                _seconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(_seconds)));

        Button bStartTime = (Button) findViewById(actualButtonID);
        bStartTime.setText(startTimeText);

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);
    }

    /***********************************************************************************************
     * MUSIC FADEIN TIME DIALOG
     **********************************************************************************************/
    public void showFadeInSettingsDialog(View v){

        //GEt ToggleButton
        final ToggleButton fadeInToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_music_toggleFadeIn);
        //Set Vibration Checked
        actualFadeIn = (fadeInToggle.isChecked())? 1 : 0;

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
                                "%02d:%02d",
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
            LinearLayout linearLayout = createAlertLinearLayout(v, textView, seekBar, actualSongLength, 1, actualFadeInTime); //TODO Length - current Startime > 0

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
        //Save Snooze Minutes
        actualFadeInTime = _seconds;
        actualFadeIn     = 1; //true

        //Set Button Text
        ToggleButton bFadeIn = (ToggleButton) findViewById(actualButtonID);

        String fadeTimeText = String.format(
                "%02d:%02d",
                TimeUnit.SECONDS.toMinutes(_seconds),
                _seconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(_seconds)));

        //Set Toggle Text
        if(bFadeIn.isChecked())
            bFadeIn.setText(fadeTimeText);
        else
            bFadeIn.setText(this.getString(R.string.wakeup_music_fadeOff));

        bFadeIn.setTextOn(fadeTimeText);
        bFadeIn.setTextOff(this.getString(R.string.wakeup_music_fadeOff));

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);
    }

    /***********************************************************************************************
     * MUSIC VIBRATION DIALOG
     **********************************************************************************************/
    public void showVibrationSettingDialog(View v){

        //GEt ToggleButton
        final ToggleButton vibrationToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_music_toggleVibration);

        //Set Vibration Checked
        actualVibra = (vibrationToggle.isChecked())? 1 : 0;

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
                LinearLayout linearLayout = createAlertLinearLayout(v, textView, seekBar, 100, 1, actualVibraStr);

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
        //Save Vibration Values
        actualVibraStr = _strength;
        actualVibra    = 1; //true

        //Set Button Text
        String vibraOn =  actualVibraStr + "%";
        String vibraOff = this.getString(R.string.wakeup_music_vibraOff);

        ToggleButton bVibraStrength = (ToggleButton) findViewById(actualButtonID);
        if(bVibraStrength.isChecked())
            bVibraStrength.setText(vibraOn);
        else
            bVibraStrength.setText(vibraOff);

        bVibraStrength.setTextOn(vibraOn);
        bVibraStrength.setTextOff(vibraOff);

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);
    }

    /***********************************************************************************************
     * SCEEN LIGHT SETTING DIALOG
     **********************************************************************************************/
    public void showScreenLightSettingDialog(View v){

        //GEt ToggleButton
        final ToggleButton screenToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_light_buttonLight);

        //Set Screen Checked
        actualScreen = (screenToggle.isChecked())? 1 : 0;

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
                        String message = Integer.toString(++progress)+ "%";
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
                LinearLayout linearLayout = createAlertLinearLayout(v, textView, seekBar, 99, 1, --actualScreenBrightness); //We must -1 because we dont want to have zero brightness

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

    private void onScreenBrightnessSet(int _brigthness){

        actualScreenBrightness = _brigthness;
        actualScreen           = 1; //true

        //Set Button Text
        String screenOn =  this.getString(R.string.wakeup_light_screen_brightness) + " " + actualScreenBrightness + "%";
        String screenOff = this.getString(R.string.wakeup_light_screen_brightness_off);

        ToggleButton bScreenBrightness = (ToggleButton) findViewById(actualButtonID);
        if(bScreenBrightness.isChecked())
            bScreenBrightness.setText(screenOn);
        else
            bScreenBrightness.setText(screenOff);

        bScreenBrightness.setTextOn(screenOn);
        bScreenBrightness.setTextOff(screenOff);

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);
    }

    public void showScreenLightStartSettingDialog(View v){

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
        LinearLayout linearLayout = createAlertLinearLayout(v, textView, seekBar, 99, 1, actualScreenStartTime - 1);

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
        //Save ScreenStart Minutes
        actualScreenStartTime = _minutes + 1;  //we Start with 1 minute

        //Set Button Text
        String timeText = actualScreenStartTime + " " + this.getString(R.string.wakeup_time_minutes);

        Button bStartTime = (Button) findViewById(actualButtonID);
        bStartTime.setText(timeText);

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);
    }

    /***********************************************************************************************
     * SCREEN COLOR SETTING DIALOG
     **********************************************************************************************/
    public void showScreenColor1SettingDialog(View v){

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

        final String color1 = this.getString(R.string.wakeup_light_screen_color1);
        final String color2 = this.getString(R.string.wakeup_light_screen_color2);

        if(color1.equals(_buttonView.getText().toString()))
            actualLightColor1 = _color;
        else if(color2.equals(_buttonView.getText().toString()))
            actualLightColor2 = _color;
        //TODO else with Logging

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);
    }

    /***********************************************************************************************
     * SCREEN COLOR FADE SETTING DIALOG
     **********************************************************************************************/
    public void showScreenColorFadeSettingDialog(View v){

        //GEt ToggleButton
        final ToggleButton screenFadeToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_light_buttonScreenFade);

        //Set Vibration Checked
        actualLightFade = (screenFadeToggle.isChecked())? 1 : 0;

        //Save ID From Button
        actualButtonID = v.getId();

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);
    }

    /***********************************************************************************************
     * LED LIGHT SETTING DIALOG
     **********************************************************************************************/
    public void showLEDLightSettingDialog(View v){
        //GEt ToggleButton
        final ToggleButton LEDToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_light_buttonLED);

        //Set LED Checked
        actualLightLED = (LEDToggle.isChecked())? 1 : 0;

        //Save ID From Button
        actualButtonID = v.getId();

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);
    }

    public void showLEDLightStartSettingDialog(View v){

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
        LinearLayout linearLayout = createAlertLinearLayout(v, textView, seekBar, 99, 1, actualLightLEDStartTime - 1);

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
        //Save LEDSTartTime Minutes
        actualLightLEDStartTime = _minutes + 1;  //we Start with 1 minute

        //Set Button Text
        String timeText = actualLightLEDStartTime + " " + this.getString(R.string.wakeup_time_minutes);

        Button bStartTime = (Button) findViewById(actualButtonID);
        bStartTime.setText(timeText);

        //save Settings
        saveSettings(AlarmConstants.WAKEUP_TIMER, actualAlarm, AlarmConstants.ALARM_NAME);
    }

    /***********************************************************************************************
     * OPTIONSMENU
     **********************************************************************************************/
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
