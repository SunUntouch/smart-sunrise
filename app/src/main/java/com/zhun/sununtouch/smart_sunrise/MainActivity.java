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
    public final static String EXTRA_MESSAGE     = "com.zhun.sununtouch.smart_sunrise.MESSAGE";

    //ExpendableList
    ExpandableListAdapter expListAdapter;
    ExpandableListView    expListView;

    List<String>          expListDataHeader;
    List<String>          expListDataAlarm;
    List<String>          expListDataMusicURI;


    LinkedHashMap<String,
            LinkedHashMap<String,
                    LinkedHashMap<String, Integer>>> expListDataChild;

    //Actual Alarm Values
    private int actualAlarm    = -1;

    //name
    private String actualName  = "Alarm";
    //Time
    private int actualHour     = 0;
    private int actualMin      = 0;
    private int actualSnooze   =10;

    //Days
    private int isMonday    = 0;
    private int isTuesday   = 0;
    private int isWednesday = 0;
    private int isThursday  = 0;
    private int isFriday    = 0;
    private int isSaturday  = 0;
    private int isSunday    = 0;

    //Music
    private String actualSongURI = Settings.System.DEFAULT_ALARM_ALERT_URI.getPath();
    private int actualSongStart  = 0;
    private int actualVolume     =10;
    private int actualFadeIn     = 0;
    private int actualFadeInTime = 0;
    private int actualVibra      = 0;
    private int actualVibraStr   =10;

    //Light
    private int actualScreen           = 0;
    private int actualScreenBrightness =99;
    private int actualScreenStartTime  =30;
    private int actualLightColor1      = 0;
    private int actualLightColor2      = 0;
    private int actualLightFade        = 0;
    private int actualLightLED         = 0;
    private int actualLightLEDStartTime= 0;

    //Last Clicked Button
    private int actualButtonID    = 0;
    private int actualAlarmNameID = 0;


    private static int timeHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    private static int timeMinute = Calendar.getInstance().get(Calendar.MINUTE);
    private MediaPlayer mediaPlayer;

    /***********************************************************************************************
     * DATA VALUES
     **********************************************************************************************/
    private void prepareListDataValues(String _alarmName,String _musicURI, int _id, int[] _time, int[] _days, int[] _music, int[] _light){

        //Load sharedPrefereces
        String settingName = AlarmConstants.ALARM + _id;
        if(!expListDataAlarm.contains(settingName)) //TODO add Alarm URi and Values correct
        expListDataAlarm.add(settingName);

        expListDataMusicURI.add(_musicURI);
        expListDataHeader.add(_alarmName);
        //Adding Child Data
        LinkedHashMap<String,
                LinkedHashMap<String, Integer>> newAlarm = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
        LinkedHashMap<String, Integer> alarmvalueTime    = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, Integer> alarmvalueDay     = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, Integer> alarmvalueMusic   = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, Integer> alarmvalueLight   = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, Integer> alarmvalueDelete  = new LinkedHashMap<String, Integer>();

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
        newAlarm.put(AlarmConstants.WAKEUP_DELETE, alarmvalueDelete);

        expListDataChild.put(expListDataAlarm.get(_id), newAlarm);

        if(expListAdapter != null)
            expListAdapter.notifyDataSetChanged(expListDataAlarm, expListDataMusicURI, expListDataHeader, expListDataChild);
    }
    private void prepareLisData(){
        //List
        expListDataHeader = new ArrayList<String>();
        expListDataAlarm  = new ArrayList<String>();
        expListDataMusicURI= new ArrayList<String>();
        expListDataChild  = new LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>>();

        SharedPreferences information = getApplicationContext().getSharedPreferences(AlarmConstants.WAKEUP_TIMER_INFO, Context.MODE_PRIVATE);
        int amount = information.getInt(AlarmConstants.ALARM_VALUE, 0);

        if(amount == 0) {
            //No Alarm
            //Putting Value for each child
            int[] time  = {00, 00, 00};         // hour, minute
            int[] days  = {0,0,0,0,0,0,0};  // Monday - Sunday
            int[] music = {0,0,0,0,0,0};      // Song, StartTime, Volume, FadIn, FadeInTime, Vibration, Vibration Strength
            int[] light = {0,0,0,0,0,0,0,0};    // UseScreen, ScreenColor1, ScreenColor2, Fadecolor, FadeTime, UseLED

            prepareListDataValues(this.getString(R.string.wakeup_no_alarm), "", 0, time, days, music, light); //TODO Set SongID to Standard URI
        }
        else {

            for (int _amount = 0; _amount < amount; ++_amount) {

                //sharedPrefereces
                String settingName = AlarmConstants.WAKEUP_TIMER + _amount;
                SharedPreferences settings = getApplicationContext().getSharedPreferences(settingName, Context.MODE_PRIVATE);

                //GetData
                String name = settings.getString(AlarmConstants.ALARM_NAME, this.getString(R.string.wakeup_no_alarm));

                String musicURI = settings.getString(AlarmConstants.ALARM_MUSIC_SONGID, Settings.System.DEFAULT_ALARM_ALERT_URI.getPath());

                //Putting Value for each child
                int[] time  = {
                        settings.getInt(AlarmConstants.ALARM_TIME_HOUR   , 00),
                        settings.getInt(AlarmConstants.ALARM_TIME_MINUTES, 00),
                        settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE , 10)};    // hour, minute, snooze

                int[] days  = {
                        settings.getInt(AlarmConstants.ALARM_DAY_MONDAY   , 0),
                        settings.getInt(AlarmConstants.ALARM_DAY_TUESDAY  , 0),
                        settings.getInt(AlarmConstants.ALARM_DAY_WEDNESDAY, 0),
                        settings.getInt(AlarmConstants.ALARM_DAY_THURSDAY , 0),
                        settings.getInt(AlarmConstants.ALARM_DAY_FRIDAY   , 0),
                        settings.getInt(AlarmConstants.ALARM_DAY_SATURDAY , 0),
                        settings.getInt(AlarmConstants.ALARM_DAY_SUNDAY   , 0)};  // Monday - Sunday

                int[] music = {
                        settings.getInt(AlarmConstants.ALARM_MUSIC_SONGSTART      , 0),
                        settings.getInt(AlarmConstants.ALARM_MUSIC_VOLUME         , 0),
                        settings.getInt(AlarmConstants.ALARM_MUSIC_FADEIN         , 0),
                        settings.getInt(AlarmConstants.ALARM_MUSIC_FADEINTIME     , 0),
                        settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV, 0),
                        settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE, 0)};      // StartTime, Volume, FadIn, FadeInTime

                int[] light = {
                        settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN           , 0),
                        settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS, 0),
                        settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME, 0),
                        settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR1           , 0),
                        settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR2           , 0),
                        settings.getInt(AlarmConstants.ALARM_LIGHT_FADECOLOR        , 0),
                        settings.getInt(AlarmConstants.ALARM_LIGHT_USELED           , 0),
                        settings.getInt(AlarmConstants.ALARM_LIGHT_LED_START_TIME   , 0)};// UseScreen, ScreenColor1, ScreenColor2, Fadecolor, FadeTime, UseLED

                prepareListDataValues(name, musicURI, _amount, time, days, music, light);
            }
        }
    }

    private void changeListData(String _name, int _id){
        //Load sharedPrefereces
        String settingName = AlarmConstants.WAKEUP_TIMER + _id;
        SharedPreferences settings      = getApplicationContext().getSharedPreferences(settingName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        String alarmName = AlarmConstants.ALARM + _id;

        if(!expListDataAlarm.contains(alarmName))
            expListDataAlarm.add(alarmName);

        //put StringSet back
        editor.putString(AlarmConstants.ALARM_NAME, _name);

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

        SharedPreferences information = getApplicationContext().getSharedPreferences(AlarmConstants.WAKEUP_TIMER_INFO, Context.MODE_PRIVATE);
        int amount = information.getInt(AlarmConstants.ALARM_VALUE, 0);

        saveListDataChild(_name, amount);
    }
    private void saveListDataChild(String _name, int _id){

        SharedPreferences information   = getApplicationContext().getSharedPreferences(AlarmConstants.WAKEUP_TIMER_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = information.edit();

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

        editor.putInt(AlarmConstants.ALARM_VALUE, amount);
        editor.apply();
        //prepare new List Data
        prepareLisData();
    }

    public void deleteChild(View v){

        Button deleteButton = (Button) v;

        deleteButton.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View v) {
                deleteListDataChild(v);
                return false;
            }
        });
    }
    private void deleteListDataChild(View v){

        int _id = actualAlarm;

        expListView.collapseGroup(_id);

        //Load SharedPreferences
        SharedPreferences information      = getApplicationContext().getSharedPreferences(AlarmConstants.WAKEUP_TIMER_INFO, Context.MODE_PRIVATE);

        int amount = information.getInt(AlarmConstants.ALARM_VALUE, 0);

        if(amount > 0) {
            for (int id = _id; id < amount - 1; ++id) {
                String settingNameNew = AlarmConstants.WAKEUP_TIMER + id;
                SharedPreferences settingsNew = getApplicationContext().getSharedPreferences(settingNameNew, Context.MODE_PRIVATE);
                SharedPreferences.Editor editorNew = settingsNew.edit();

                String settingNameOld = AlarmConstants.WAKEUP_TIMER + ++id;
                SharedPreferences settingsOld = getApplicationContext().getSharedPreferences(settingNameOld, Context.MODE_PRIVATE);
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
            String name = AlarmConstants.WAKEUP_TIMER + amount;
            SharedPreferences settings = getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();

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
        String settingsName = AlarmConstants.WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);

        //Putting Value for each child
        //Time
        actualHour   = settings.getInt(AlarmConstants.ALARM_TIME_HOUR, 00);
        actualMin    = settings.getInt(AlarmConstants.ALARM_TIME_MINUTES, 00);
        actualSnooze = settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE, 10);    // hour, minute, snooze

        //Days
        isMonday    = settings.getInt(AlarmConstants.ALARM_DAY_MONDAY   , 0);
        isTuesday   = settings.getInt(AlarmConstants.ALARM_DAY_TUESDAY  , 0);
        isWednesday = settings.getInt(AlarmConstants.ALARM_DAY_WEDNESDAY, 0);
        isThursday  = settings.getInt(AlarmConstants.ALARM_DAY_THURSDAY , 0);
        isFriday    = settings.getInt(AlarmConstants.ALARM_DAY_FRIDAY   , 0);
        isSaturday  = settings.getInt(AlarmConstants.ALARM_DAY_SATURDAY , 0);
        isSunday    = settings.getInt(AlarmConstants.ALARM_DAY_SUNDAY, 0); // Monday - Sunday

        //Load Music
        actualSongURI   = settings.getString(AlarmConstants.ALARM_MUSIC_SONGID      , Settings.System.DEFAULT_ALARM_ALERT_URI.getPath());
        actualSongStart = settings.getInt(AlarmConstants.ALARM_MUSIC_SONGSTART      , 0);
        actualVolume    = settings.getInt(AlarmConstants.ALARM_MUSIC_VOLUME         , 0);
        actualFadeIn    = settings.getInt(AlarmConstants.ALARM_MUSIC_FADEIN         , 0);
        actualFadeInTime= settings.getInt(AlarmConstants.ALARM_MUSIC_FADEINTIME     , 0);
        actualVibra     = settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV, 0);
        actualVibraStr  = settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE, 0);// Song, StartTime, Volume, FadIn, FadeInTime

        //Load Light
        actualScreen           = settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN            , 0);
        actualScreenBrightness = settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS , 0);
        actualScreenStartTime  = settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME , 0);
        actualLightColor1      = settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR1            , 0);
        actualLightColor2      = settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR2            , 0);
        actualLightFade        = settings.getInt(AlarmConstants.ALARM_LIGHT_FADECOLOR         , 0);
        actualLightLED         = settings.getInt(AlarmConstants.ALARM_LIGHT_USELED            , 0);
        actualLightLEDStartTime= settings.getInt(AlarmConstants.ALARM_LIGHT_LED_START_TIME    , 0);// UseScreen, ScreenColor1, ScreenColor2, Fadecolor, FadeTime, UseLED
    }

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
                loadValuesNew(groupPosition); //TODO Dont know if needed

                actualAlarm = groupPosition;

                if (groupPosition != previousGroup)
                    expListView.collapseGroup(previousGroup);

                previousGroup = groupPosition;
                expListView.invalidateViews();
            }
        });

        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            public void onGroupCollapse(int groupPosition) {
            }
        });

        //Floating ActionButton/////////////////////////////////////////////////////////////////////
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                saveListDataChild(AlarmConstants.ALARM);
                ExpandableListAdapter expListAdapterNew = new ExpandableListAdapter(expListView.getContext(), expListDataAlarm, expListDataMusicURI, expListDataHeader, expListDataChild);
                expListAdapter = expListAdapterNew;
                expListView.setAdapter(expListAdapter);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /***********************************************************************************************
     * AlarmConstants.ALARM NAME SETTING DIALOG
     **********************************************************************************************/
    public void showNameSettingDialog(View v){

        //save Text ID
        actualAlarmNameID = v.getId();

        //Create new Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_name));

        final EditText newName = new EditText(this);

        newName.setInputType(InputType.TYPE_CLASS_TEXT);

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

        actualName = _newName;

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

        switch(actualButtonID){
            case R.id.wakeup_monday:{
                isMonday = (toggle.isChecked()) ? 1 : 0;  //when togglebutton is checked set Alarm for this day
            }
            break;
            case R.id.wakeup_tuesday:{
                isTuesday = (toggle.isChecked()) ? 1 : 0;  //when togglebutton is checked set Alarm for this day
            }
            break;
            case R.id.wakeup_wednesday:{
                isWednesday = (toggle.isChecked()) ? 1 : 0;  //when togglebutton is checked set Alarm for this day
            }
            break;
            case R.id.wakeup_thursday:{
                isThursday = (toggle.isChecked()) ? 1 : 0;  //when togglebutton is checked set Alarm for this day
            }
            break;
            case R.id.wakeup_friday:{
                isFriday = (toggle.isChecked()) ? 1 : 0;  //when togglebutton is checked set Alarm for this day
            }
            break;
            case R.id.wakeup_saturday:{
                isSaturday = (toggle.isChecked()) ? 1 : 0;  //when togglebutton is checked set Alarm for this day
            }
            break;
            case R.id.wakeup_sunday:{
                isSunday = (toggle.isChecked()) ? 1 : 0;  //when togglebutton is checked set Alarm for this day
            }
            break;
            default:
                //TODO Logging Error
                break;
        }

        //save Settings
        String settingsName = AlarmConstants.WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM), actualAlarm);

        prepareLisData();
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
        //Do Something with the Time

        //save times
        actualHour = hourOfDay;
        actualMin  = minute;

        //Set Button Text
        Button bTime = (Button) findViewById(actualButtonID);
        String timeText = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute);
        bTime.setText(timeText);

        //save Settings
        String settingsName = AlarmConstants.WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM), actualAlarm);

        prepareLisData();

        AlarmManage newAlarm = new AlarmManage(this);
        newAlarm.setNewAlarm(actualAlarm, false, 0);
        //setNewAlarm(actualAlarm, false, 0);
    }

    /***********************************************************************************************
     * MINUTE SETTING DIALOG
     **********************************************************************************************/
    public void showMinuteSettingDialog(View v) {

        //save Button Id
        actualButtonID = v.getId();

        //Create new Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_minutes));


        //Fill Values for the Minute Choosing Dialog
        ArrayList<String> Minutes = new ArrayList<>();
        int MaxMinutes = 100; int currentMinute = 1;
        while(currentMinute <= MaxMinutes){

            Minutes.add(Integer.toString(currentMinute));
            ++currentMinute;
        }

        //Cast to Array
        String[] minuteArray = new String[Minutes.size()];
        minuteArray = Minutes.toArray(minuteArray);

        //Set Builder Settings and Onclikclistener
        builder.setItems(minuteArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                onSnoozeMinutesSet(which);
                dialog.dismiss();
            }
        });

        //Show Builder
        builder.show();
    }

    private void onSnoozeMinutesSet(int _minutes){
        //Do Something with the Minutes

        //Save Snooze Minutes
        actualSnooze = _minutes + 1;  //we Start with 1 minute

        //Set Button Text
        Button bSnooze = (Button) findViewById(actualButtonID);
        String timeText = this.getString(R.string.wakeup_time_snooze) + " " + actualSnooze + " " + this.getString(R.string.wakeup_time_minutes);
        bSnooze.setText(timeText);

        //save Settings
        String settingsName = AlarmConstants.WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM), actualAlarm);

        prepareLisData();
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

        boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

        if(isSDPresent || _modeID == 0){
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
                        int album_id = cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                        String artist_name = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        int artist_id = cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));

                        int isAlarm = cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Audio.Media.IS_ALARM));

                        int isMusic = cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));

                        int isRingtone = cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Audio.Media.IS_RINGTONE));

                        if(isMusic == 1 && _modeID == 1)
                            songList.add(new SongInformation(song_id,  song_name, artist_name, album_name, fullpath));
                        else
                            songList.add(new SongInformation(song_id,  song_name, artist_name, album_name, fullpath));
                    }
                    while(cursor.moveToNext());
                }
                //Choose an Alarm
                chooseAlarmSongDialog(songList);
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
        final ArrayList<SongInformation> Songs = _Songs;
        ArrayList<String> songNameArrayList = new ArrayList<>();
        for(SongInformation songs : Songs){
            String nameWithoutExtension  = songs.getTitle();

            if(nameWithoutExtension != null && nameWithoutExtension.contains("."))
                nameWithoutExtension = nameWithoutExtension.substring(0, nameWithoutExtension.lastIndexOf('.'));

            songNameArrayList.add(nameWithoutExtension);
        }
        //Get Song Name Array and set it for Alarm Dialog
        final String[] songNameArray = songNameArrayList.toArray(new String[0]);

        builder.setItems(songNameArray, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                actualSongURI = _Songs.get(which).getPath();

                String newMusicURI = _Songs.get(which).getPath();
                String newMusicText = newMusicURI.substring(newMusicURI.lastIndexOf('/') + 1);
                newMusicText = newMusicText.substring(0, newMusicText.lastIndexOf('.'));

                Button musicButton = (Button) findViewById(actualButtonID);
                musicButton.setText(newMusicText);

                //save Settings
                String settingsName = AlarmConstants.WAKEUP_TIMER + actualAlarm;
                SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
                saveListDataChild(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM), actualAlarm);

                prepareLisData();

                dialog.dismiss();
                //return;
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            public void onShow(DialogInterface dialog) {

                ListView songsView = alertDialog.getListView(); //TODO Not Working onLongClick
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

        if(mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        else
            mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(getApplicationContext(), _SongUri);
        mediaPlayer.prepare();
    }

    private void stopMusic(){

        if(mediaPlayer!=null){

            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();

            //mediaPlayer.reset();
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

        //Create new Builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_song_Volume));

        //TextView to show Value of SeekBar
        final TextView textView = new TextView(v.getContext());
        //textView.setVisibility(View.INVISIBLE);

        //Seekbar
        final SeekBar seekBar = new SeekBar(v.getContext());
        seekBar.setMax(100);
        seekBar.setKeyProgressIncrement(1);
        seekBar.setProgress(actualVolume);

        //LinearLayout
        LinearLayout linearLayout = new LinearLayout(v.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(textView);
        linearLayout.addView(seekBar);

        //Set Alertdialog View
        builder.setView(linearLayout);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Set Position of Text
                int val = (progress * (seekBar.getWidth() - 4 * seekBar.getThumbOffset())) / seekBar.getMax();
                textView.setText("" + progress);
                textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //textView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //textView.setVisibility(View.GONE);
            }
        });

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
        Button bVolume = (Button) findViewById(actualButtonID);
        String volumeText = _volume + "%";
        bVolume.setText(volumeText);

        //save Settings
        String settingsName = AlarmConstants.WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM), actualAlarm);

        prepareLisData();
    }

    /***********************************************************************************************
     * MUSIC START TIME DIALOG
     **********************************************************************************************/
    public void showMusicStartSettingDialog(View v){

        //Save ID From Button
        actualButtonID = v.getId();

        //Create new Builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_song_Start));

        //TextView to show Value of SeekBar
        final TextView textView = new TextView(v.getContext());
        //textView.setVisibility(View.INVISIBLE);

        //Seekbar
        final SeekBar seekBar = new SeekBar(v.getContext());
        seekBar.setMax(100); //TODO get Seconds from choosen Song and Fill Into
        seekBar.setKeyProgressIncrement(1);
        seekBar.setProgress(actualSongStart);

        //LinearLayout
        LinearLayout linearLayout = new LinearLayout(v.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(textView);
        linearLayout.addView(seekBar);

        //Set Alertdialog View
        builder.setView(linearLayout);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Set Position of Text
                int val = (progress * (seekBar.getWidth() - 4 * seekBar.getThumbOffset())) / seekBar.getMax();
                textView.setText(progress + "s");
                textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //textView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //textView.setVisibility(View.GONE);
            }
        });

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

        //Find Button and set Text
        Button bStartTime = (Button) findViewById(actualButtonID);
        String startTimeText = String.format(
                "%02d:%02d",
                TimeUnit.SECONDS.toMinutes(_seconds),
                _seconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(_seconds)));
        bStartTime.setText(startTimeText);

        //save Settings
        String settingsName = AlarmConstants.WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM), actualAlarm);

        prepareLisData();
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

                //Create new Builder
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(v.getContext().getString(R.string.wakeup_set_alarm_song_fadeIn));

                //TextView to show Value of SeekBar
                final TextView textView = new TextView(v.getContext());
                //textView.setVisibility(View.INVISIBLE);

                //Seekbar
                final SeekBar seekBar = new SeekBar(v.getContext());
                seekBar.setMax(100); //TODO Songlength - StartTime = max FadeIn
                seekBar.setKeyProgressIncrement(1);
                seekBar.setProgress(actualFadeInTime);

                //LinearLayout
                LinearLayout linearLayout = new LinearLayout(v.getContext());
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.addView(textView);
                linearLayout.addView(seekBar);

                //Set Alertdialog View
                builder.setView(linearLayout);

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                        //Set Position of Text
                        int val = (progress * (seekBar.getWidth() - 4 * seekBar.getThumbOffset())) / seekBar.getMax();
                        textView.setText(progress + "s");
                        textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        //textView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //textView.setVisibility(View.GONE);
                    }
                });

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
        //Do Something with the Vibration Strength

        //Save Snooze Minutes
        actualFadeInTime = _seconds;
        actualFadeIn     = 1; //true

        //Set Button Text
        ToggleButton bFadeIn = (ToggleButton) findViewById(actualButtonID);

        String fadeInOn =  String.format("%02ds", actualFadeInTime);
        String fadeInOff = this.getString(R.string.wakeup_music_fadeOff);

        if(bFadeIn.isChecked())
            bFadeIn.setText(fadeInOn);
        else
            bFadeIn.setText(fadeInOff);


        bFadeIn.setTextOn(fadeInOn);
        bFadeIn.setTextOff(fadeInOff);

        //save Settings
        String settingsName = AlarmConstants.WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM), actualAlarm);

        prepareLisData();
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

                //Create new Builder
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(v.getContext().getString(R.string.wakeup_set_alarm_song_vibration));

                //TextView to show Value of SeekBar
                final TextView textView = new TextView(v.getContext());
                //textView.setVisibility(View.INVISIBLE);

                //Seekbar
                final SeekBar seekBar = new SeekBar(v.getContext());
                seekBar.setMax(100);
                seekBar.setKeyProgressIncrement(1);
                seekBar.setProgress(actualVibraStr);

                //LinearLayout
                LinearLayout linearLayout = new LinearLayout(v.getContext());
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.addView(textView);
                linearLayout.addView(seekBar);

                //Set Alertdialog View
                builder.setView(linearLayout);

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                        //Set Position of Text
                        int val = (progress * (seekBar.getWidth() - 4 * seekBar.getThumbOffset())) / seekBar.getMax();
                        textView.setText(progress + "%");
                        textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        //textView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //textView.setVisibility(View.GONE);
                    }
                });

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
        //Do Something with the Vibration Strength

        //Save Snooze Minutes
        actualVibraStr = _strength;
        actualVibra    = 1; //true

        //Set Button Text
        ToggleButton bVibraStrength = (ToggleButton) findViewById(actualButtonID);

        String vibraOn =  actualVibraStr + "%";
        String vibraOff = this.getString(R.string.wakeup_music_vibraOff);

        if(bVibraStrength.isChecked())
            bVibraStrength.setText(vibraOn);
        else
            bVibraStrength.setText(vibraOff);

        bVibraStrength.setTextOn(vibraOn);
        bVibraStrength.setTextOff(vibraOff);

        //save Settings
        String settingsName = AlarmConstants.WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM), actualAlarm);

        prepareLisData();
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

                //Create new Builder
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(v.getContext().getString(R.string.wakeup_set_alarm_light_brightness));

                //TextView to show Value of SeekBar
                final TextView textView = new TextView(v.getContext());
                //textView.setVisibility(View.INVISIBLE);

                //Seekbar
                final SeekBar seekBar = new SeekBar(v.getContext());
                seekBar.setMax(99);
                seekBar.setKeyProgressIncrement(1);
                seekBar.setProgress(--actualScreenBrightness); //We must -1 because we dont want to have zero brightness

                //LinearLayout
                LinearLayout linearLayout = new LinearLayout(v.getContext());
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.addView(textView);
                linearLayout.addView(seekBar);

                //Set Alertdialog View
                builder.setView(linearLayout);

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                        //Set Position of Text
                        int val = (progress * (seekBar.getWidth() - 4 * seekBar.getThumbOffset())) / seekBar.getMax();
                        textView.setText(++progress + "%");
                        textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        //textView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //textView.setVisibility(View.GONE);
                    }
                });

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
        ToggleButton bScreenBrightness = (ToggleButton) findViewById(actualButtonID);

        String screenOn =  this.getString(R.string.wakeup_light_screen_brightness) + " " + actualScreenBrightness + "%";
        String screenOff = this.getString(R.string.wakeup_light_screen_brightness_off);

        if(bScreenBrightness.isChecked())
            bScreenBrightness.setText(screenOn);
        else
            bScreenBrightness.setText(screenOff);

        bScreenBrightness.setTextOn(screenOn);
        bScreenBrightness.setTextOff(screenOff);

        //save Settings
        String settingsName = AlarmConstants.WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM), actualAlarm);

        prepareLisData();
    }

    public void showScreenLightStartSettingDialog(View v){

        //save Button Id
        actualButtonID = v.getId();

        //Create new Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(v.getContext().getString(R.string.wakeup_set_alarm_light_minutes));


        //Fill Values for the Minute Choosing Dialog
        ArrayList<String> Minutes = new ArrayList<>();
        int MaxMinutes = 100; int currentMinute = 1;
        while(currentMinute <= MaxMinutes){

            Minutes.add(Integer.toString(currentMinute));
            ++currentMinute;
        }

        //Cast to Array
        String[] minuteArray = new String[Minutes.size()];
        minuteArray = Minutes.toArray(minuteArray);

        //Set Builder Settings and onClickListener
        builder.setItems(minuteArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                onScreenStartTimeSet(which);
                dialog.dismiss();
            }
        });

        //Show Builder
        builder.show();
    }

    private void onScreenStartTimeSet(int _minutes){
        //Do Something with the Minutes

        //Save Snooze Minutes
        actualScreenStartTime = _minutes + 1;  //we Start with 1 minute

        //Set Button Text
        Button bStartTime = (Button) findViewById(actualButtonID);
        String timeText = actualScreenStartTime + " " + this.getString(R.string.wakeup_time_minutes);
        bStartTime.setText(timeText);

        //save Settings
        String settingsName = AlarmConstants.WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM), actualAlarm);

        prepareLisData();
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

        if(_buttonView.getText().toString() == color1)
            actualLightColor1 = _color;
        else if(_buttonView.getText().toString() == color2)
            actualLightColor2 = _color;
        //TODO else with Logging

        //save Settings
        String settingsName = AlarmConstants.WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM), actualAlarm);

        prepareLisData();
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
        String settingsName = AlarmConstants.WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM), actualAlarm);

        prepareLisData();
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
        String settingsName = AlarmConstants.WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM), actualAlarm);

        prepareLisData();
    }

    public void showLEDLightStartSettingDialog(View v){

        //save Button Id
        actualButtonID = v.getId();

        //Create new Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(v.getContext().getString(R.string.wakeup_set_alarm_LED_time));


        //Fill Values for the Minute Choosing Dialog
        ArrayList<String> Minutes = new ArrayList<>();
        int MaxMinutes = 100; int currentMinute = 1;
        while(currentMinute <= MaxMinutes){

            Minutes.add(Integer.toString(currentMinute));
            ++currentMinute;
        }

        //Cast to Array
        String[] minuteArray = new String[Minutes.size()];
        minuteArray = Minutes.toArray(minuteArray);

        //Set Builder Settings and Onclikclistener
        builder.setItems(minuteArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                onLEDStartTimeSet(which);
                dialog.dismiss();
            }
        });

        //Show Builder
        builder.show();
    }

    private void onLEDStartTimeSet(int _minutes){
        //Do Something with the Minutes

        //Save Snooze Minutes
        actualLightLEDStartTime = _minutes + 1;  //we Start with 1 minute

        //Set Button Text
        Button bStartTime = (Button) findViewById(actualButtonID);
        String timeText = actualLightLEDStartTime + " " + this.getString(R.string.wakeup_time_minutes);
        bStartTime.setText(timeText);

        //save Settings
        String settingsName = AlarmConstants.WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM), actualAlarm);

        prepareLisData();
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
