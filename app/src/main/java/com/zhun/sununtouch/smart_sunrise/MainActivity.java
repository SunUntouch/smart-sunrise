package com.zhun.sununtouch.smart_sunrise;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
                    implements TimePickerDialog.OnTimeSetListener{
    public final static String EXTRA_MESSAGE = "com.zhun.sununtouch.smart_sunrise.MESSAGE";
    //Shared Pref Settings
    public final static String WAKEUP_TIMER      = "com.zhun.sununtouch.smart_sunrise.WAKEUP_SETTINGS";
    public final static String WAKEUP_TIMER_INFO = "com.zhun.sununtouch.smart_sunrise.WAKEUP_SETTINGS_INFO";

    //Alarm
    public final static String ALARM                      = "Alarm";
    public final static String ALARM_NAME                 = "Alarm_Name";
    public final static String ALARM_VALUE                = "Alarm_Value";

    public final static String ALARM_TIME_MINUTES         = "Alarm_Minutes";
    public final static String ALARM_TIME_HOUR            = "Alarm_Hour";
    public final static String ALARM_TIME_SNOOZE          = "Alarm_Snooze";

    public final static String ALARM_DAY_MONDAY           = "Alarm_Monday";
    public final static String ALARM_DAY_TUESDAY          = "Alarm_Tuesday";
    public final static String ALARM_DAY_WEDNESDAY        = "Alarm_Wednesday";
    public final static String ALARM_DAY_THURSDAY         = "Alarm_Thursday";
    public final static String ALARM_DAY_FRIDAY           = "Alarm_Friday";
    public final static String ALARM_DAY_SATURDAY         = "Alarm_Saturday";
    public final static String ALARM_DAY_SUNDAY           = "Alarm_Sunday";

    public final static String ALARM_MUSIC_SONGID         = "Alarm_SongID";
    public final static String ALARM_MUSIC_SONGSTART      = "Alarm_SongStart";
    public final static String ALARM_MUSIC_VOLUME         = "Alarm_Volume";
    public final static String ALARM_MUSIC_FADEIN         = "Alarm_FadeIn";
    public final static String ALARM_MUSIC_FADEINTIME     = "Alarm_FadeTime";

    public final static String ALARM_LIGHT_SCREEN         = "Alarm_Screen";
    public final static String ALARM_LIGHT_COLOR1         = "Alarm_ScreenColor1";
    public final static String ALARM_LIGHT_COLOR2         = "Alarm_ScreenColor2";
    public final static String ALARM_LIGHT_FADECOLOR      = "Alarm_FadeColor";
    public final static String ALARM_LIGHT_FADECOLORTIME  = "Alarm_FadeColorTime";
    public final static String ALARM_LIGHT_USELED         = "Alarm_UseLED";

//ChildItems
    public final static String WAKEUP_DAYS    = "Days";
    public final static String WAKEUP_TIME    = "Time";
    public final static String WAKEUP_MUSIC   = "Music";
    public final static String WAKEUP_LIGHT   = "Light";

    //ExpendableList
    ExpandableListAdapter         expListAdapter;
    ExpandableListView            expListView;
    List<String>                  expListDataHeader;
    List<String>                  expListDataAlarm;
    LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>> expListDataChild;

    //Actual Alarm Values
    private int actualAlarm    = -1;

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

    //Last Clicked Button
    private int actualButtonID = 0;

    private void changeListData(String _name, int _id){
        //Load sharedPrefereces
        String settingName = WAKEUP_TIMER + _id;
        SharedPreferences settings      = getApplicationContext().getSharedPreferences(settingName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        String alarmName = ALARM + _id;

        boolean contains = expListDataAlarm.contains(alarmName);
        if(!expListDataAlarm.contains(alarmName))
        expListDataAlarm.add(alarmName);

        //put StringSet back
        editor.putString(ALARM_NAME, _name);

        //Time
        editor.putInt(ALARM_TIME_MINUTES  , actualMin);
        editor.putInt(ALARM_TIME_HOUR     , actualHour);
        editor.putInt(ALARM_TIME_SNOOZE   , actualSnooze);

        //Days
        editor.putInt(ALARM_DAY_MONDAY    , isMonday);
        editor.putInt(ALARM_DAY_TUESDAY   , isTuesday);
        editor.putInt(ALARM_DAY_WEDNESDAY , isWednesday);
        editor.putInt(ALARM_DAY_THURSDAY  , isThursday);
        editor.putInt(ALARM_DAY_FRIDAY    , isFriday);
        editor.putInt(ALARM_DAY_SATURDAY  , isSaturday);
        editor.putInt(ALARM_DAY_SUNDAY    , isSunday);

        //Music

        //Light

        //apply Values to settings
        editor.apply();
    }
    private void prepareListDataValues(String _alarmName, int _id, int[] _time, int[] _days, int[] _music, int[] _light){

        //Load sharedPrefereces
        String settingName = ALARM + _id;
        if(!expListDataAlarm.contains(settingName))
        expListDataAlarm.add(settingName);

        expListDataHeader.add(_alarmName);
        //Adding Child Data
        LinkedHashMap<String, LinkedHashMap<String, Integer>> newAlarm = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
        LinkedHashMap<String, Integer> alarmvalueTime = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, Integer> alarmvalueDay = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, Integer> alarmvalueMusic = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, Integer> alarmvalueLight = new LinkedHashMap<String, Integer>();

        //Putting Value for each child
        //Time
        alarmvalueTime.clear();
        alarmvalueTime.put("Hour"  , _time[0]);
        alarmvalueTime.put("Minute", _time[1]);
        alarmvalueTime.put("Snooze", _time[2]);
        newAlarm.put(WAKEUP_TIME, alarmvalueTime);

        //Day
        alarmvalueDay.clear();
        alarmvalueDay.put("Monday"   , _days[0]);
        alarmvalueDay.put("Tuesday"  , _days[1]);
        alarmvalueDay.put("Wednesday", _days[2]);
        alarmvalueDay.put("Thursday" , _days[3]);
        alarmvalueDay.put("Friday"   , _days[4]);
        alarmvalueDay.put("Saturday" , _days[5]);
        alarmvalueDay.put("Sunday"   , _days[6]);

        newAlarm.put(WAKEUP_DAYS, alarmvalueDay);

        //Music
        alarmvalueMusic.clear();
        alarmvalueMusic.put("Song"      , _music[0]); //Maybe ID?
        alarmvalueMusic.put("StartTime" , _music[1]);
        alarmvalueMusic.put("Volume"    , _music[2]);
        alarmvalueMusic.put("FadIn"     , _music[3]);
        alarmvalueMusic.put("FadeInTime", _music[4]);

        newAlarm.put(WAKEUP_MUSIC, alarmvalueMusic);

        //Light
        alarmvalueLight.clear();
        alarmvalueLight.put("UseScreen"   , _light[0]);
        alarmvalueLight.put("ScreenColor1", _light[1]);
        alarmvalueLight.put("ScreenColor2", _light[2]);
        alarmvalueLight.put("FadeColor"   , _light[3]);
        alarmvalueLight.put("FadeTime"    , _light[4]);
        alarmvalueLight.put("UseLED"      , _light[5]);

        newAlarm.put(WAKEUP_LIGHT, alarmvalueTime);

        expListDataChild.put(expListDataAlarm.get(_id), newAlarm);

        if(expListAdapter != null)
            expListAdapter.notifyDataSetChanged(expListDataAlarm, expListDataHeader, expListDataChild);
    }
    private void prepareLisData(){
        //List
        expListDataHeader = new ArrayList<String>();
        expListDataAlarm  = new ArrayList<String>();
        expListDataChild = new LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>>();

        SharedPreferences information = getApplicationContext().getSharedPreferences(WAKEUP_TIMER_INFO, Context.MODE_PRIVATE);
        int amount = information.getInt(ALARM_VALUE, 0);

        if(amount == 0) {
            //No Alarm
            //Putting Value for each child
            int[] time  = {00, 00, 00};         // hour, minute
            int[] days  = {0,0,0,0,0,0,0};  // Monday - Sunday
            int[] music = {0,0,0,0,0};      // Song, StartTime, Volume, FadIn, FadeInTime
            int[] light = {0,0,0,0,0,0};    // UseScreen, ScreenColor1, ScreenColor2, Fadecolor, FadeTime, UseLED

            prepareListDataValues("No Alarm Set",0, time, days, music, light);
        }
        else {

            for (int _amount = 0; _amount < amount; ++_amount) {

                //sharedPrefereces
                String settingName = WAKEUP_TIMER + _amount;
                SharedPreferences settings = getApplicationContext().getSharedPreferences(settingName, Context.MODE_PRIVATE);

                //GetData
                String name = settings.getString(ALARM_NAME, "No Alarm Set");

                //Putting Value for each child
                int[] time  = {
                        settings.getInt(ALARM_TIME_HOUR, 00),
                        settings.getInt(ALARM_TIME_MINUTES, 00),
                        settings.getInt(ALARM_TIME_SNOOZE, 10)};    // hour, minute, snooze

                int[] days  = {
                        settings.getInt(ALARM_DAY_MONDAY   , 0),
                        settings.getInt(ALARM_DAY_TUESDAY  , 0),
                        settings.getInt(ALARM_DAY_WEDNESDAY, 0),
                        settings.getInt(ALARM_DAY_THURSDAY , 0),
                        settings.getInt(ALARM_DAY_FRIDAY   , 0),
                        settings.getInt(ALARM_DAY_SATURDAY , 0),
                        settings.getInt(ALARM_DAY_SUNDAY   , 0)};  // Monday - Sunday

                int[] music = {
                        settings.getInt(ALARM_MUSIC_SONGID    , 0),
                        settings.getInt(ALARM_MUSIC_SONGSTART , 0),
                        settings.getInt(ALARM_MUSIC_VOLUME    , 0),
                        settings.getInt(ALARM_MUSIC_FADEIN    , 0),
                        settings.getInt(ALARM_MUSIC_FADEINTIME, 0)};      // Song, StartTime, Volume, FadIn, FadeInTime

                int[] light = {
                        settings.getInt(ALARM_LIGHT_SCREEN, 0),
                        settings.getInt(ALARM_LIGHT_COLOR1,0),
                        settings.getInt(ALARM_LIGHT_COLOR2,0),
                        settings.getInt(ALARM_LIGHT_FADECOLOR,0),
                        settings.getInt(ALARM_LIGHT_FADECOLORTIME,0),
                        settings.getInt(ALARM_LIGHT_USELED, 0)};    // UseScreen, ScreenColor1, ScreenColor2, Fadecolor, FadeTime, UseLED

                prepareListDataValues(name, _amount, time, days, music, light);
            }
        }
    }

    private void saveListDataChild(String _name){

        SharedPreferences information = getApplicationContext().getSharedPreferences(WAKEUP_TIMER_INFO, Context.MODE_PRIVATE);
        int amount = information.getInt(ALARM_VALUE, 0);

        saveListDataChild(_name, amount);
    }
    private void saveListDataChild(String _name, int _id){

        SharedPreferences information   = getApplicationContext().getSharedPreferences(WAKEUP_TIMER_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = information.edit();

        int amount = information.getInt(ALARM_VALUE, 0);

        //changelistData
        if(amount == 0){
            changeListData(_name, 0);
            ++amount;
        }
        else
            if(_id < amount)
                changeListData(_name, _id);
            else
                changeListData(_name, amount++);

        editor.putInt(ALARM_VALUE, amount);
        editor.apply();
        //prepare new List Data
        prepareLisData();
    }

    private void deleteListDataChild(int _id){

        //Load SharedPreferences
        SharedPreferences information      = getApplicationContext().getSharedPreferences(WAKEUP_TIMER_INFO, Context.MODE_PRIVATE);

        int amount = information.getInt(ALARM_VALUE, 0);

        for(int id = _id; id < amount; ++id)
        {
            String settingNameNew = WAKEUP_TIMER_INFO + id;
            SharedPreferences settingsNew = getApplicationContext().getSharedPreferences(settingNameNew, Context.MODE_PRIVATE);
            SharedPreferences.Editor editorNew = settingsNew.edit();

            String settingNameOld = WAKEUP_TIMER_INFO + ++id;
            SharedPreferences settingsOld = getApplicationContext().getSharedPreferences(settingNameOld, Context.MODE_PRIVATE);
            Map<String, ?> settingOld = settingsOld.getAll();

            for(Map.Entry<String, ?> value : settingOld.entrySet()){
                if      (value.getValue().getClass().equals(Boolean.class)) editorNew.putBoolean(value.getKey(), (Boolean)value.getValue());
                else if (value.getValue().getClass().equals(Float.class))   editorNew.putFloat(value.getKey(),   (Float)value.getValue());
                else if (value.getValue().getClass().equals(Integer.class)) editorNew.putInt(value.getKey(),     (Integer)value.getValue());
                else if (value.getValue().getClass().equals(Long.class))    editorNew.putLong(value.getKey(),    (Long)value.getValue());
                else if (value.getValue().getClass().equals(String.class))  editorNew.putString(value.getKey(),  (String)value.getValue());
            }
        }
        String name = WAKEUP_TIMER + amount;
        SharedPreferences  settings     = getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();

        SharedPreferences.Editor editorInf = information.edit();
        editorInf.putInt(ALARM_VALUE, --amount);
        editorInf.apply();

        //prepare new List Data
        prepareLisData();
        //expListAdapter.notifyDataSetChanged();

    }

    private void loadValuesNew(int _alarmID){

        actualAlarm = _alarmID;
        //save Settings
        String settingsName = WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);

        //Putting Value for each child
        //Time
        actualHour   = settings.getInt(ALARM_TIME_HOUR, 00);
        actualMin    = settings.getInt(ALARM_TIME_MINUTES, 00);
        actualSnooze = settings.getInt(ALARM_TIME_SNOOZE, 10);    // hour, minute, snooze

        //Days
        isMonday    = settings.getInt(ALARM_DAY_MONDAY   , 0);
        isTuesday   = settings.getInt(ALARM_DAY_TUESDAY  , 0);
        isWednesday = settings.getInt(ALARM_DAY_WEDNESDAY, 0);
        isThursday  = settings.getInt(ALARM_DAY_THURSDAY , 0);
        isFriday    = settings.getInt(ALARM_DAY_FRIDAY   , 0);
        isSaturday  = settings.getInt(ALARM_DAY_SATURDAY , 0);
        isSunday    = settings.getInt(ALARM_DAY_SUNDAY   , 0); // Monday - Sunday

        //TODO Load Music
        settings.getInt(ALARM_MUSIC_SONGID    , 0);
        settings.getInt(ALARM_MUSIC_SONGSTART , 0);
        settings.getInt(ALARM_MUSIC_VOLUME    , 0);
        settings.getInt(ALARM_MUSIC_FADEIN    , 0);
        settings.getInt(ALARM_MUSIC_FADEINTIME, 0);      // Song, StartTime, Volume, FadIn, FadeInTime

        //TODO Load Light
        settings.getInt(ALARM_LIGHT_SCREEN, 0);
        settings.getInt(ALARM_LIGHT_COLOR1,0);
        settings.getInt(ALARM_LIGHT_COLOR2,0);
        settings.getInt(ALARM_LIGHT_FADECOLOR,0);
        settings.getInt(ALARM_LIGHT_FADECOLORTIME,0);
        settings.getInt(ALARM_LIGHT_USELED, 0);    // UseScreen, ScreenColor1, ScreenColor2, Fadecolor, FadeTime, UseLED

    }

    @Override
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
        expListAdapter = new ExpandableListAdapter(this, expListDataAlarm, expListDataHeader, expListDataChild);

        //setting list adapter
        expListView.setAdapter(expListAdapter);

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                loadValuesNew(groupPosition); //TODO Dont know if needed

                if (groupPosition != previousGroup)
                    expListView.collapseGroup(previousGroup);
                previousGroup = groupPosition;
                expListView.invalidateViews();
            }
        });

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return false;
            }
        });

        expListView.setOnGroupClickListener((new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return false;
            }
        }));

        //Floating ActionButton/////////////////////////////////////////////////////////////////////
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                saveListDataChild(ALARM);
                ExpandableListAdapter expListAdapterNew = new ExpandableListAdapter(expListView.getContext(), expListDataAlarm, expListDataHeader, expListDataChild);
                expListAdapter = expListAdapterNew;
                expListView.setAdapter(expListAdapter);
            }
        });
    }

    public void showTimeSettingsDialog(View v){

        //save Button ID
        actualButtonID = v.getId();

        //Open TimePicker Dialog
        DialogFragment newFragment = new SettingTimeFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void showMinuteSettingDialog(View v) {

        //save Button Id
        actualButtonID = v.getId();

        //Create new Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Minutes");


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
        String settingsName = WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(ALARM_NAME, ALARM), actualAlarm);

        prepareLisData();
    }

    private void onSnoozeMinutesSet(int _minutes){
        //Do Something with the Minutes

        //Save Snooze Minutes
        actualSnooze = _minutes + 1;  //we Start with 1 minute

        //Set Button Text
        Button bSnooze = (Button) findViewById(actualButtonID);
        String timeText = "Snooze " + actualSnooze + " Minutes";
        bSnooze.setText(timeText);

        //save Settings
        String settingsName = WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(ALARM_NAME, ALARM), actualAlarm);

        prepareLisData();
    }

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
        String settingsName = WAKEUP_TIMER + actualAlarm;
        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(ALARM_NAME, ALARM), actualAlarm);

        prepareLisData();
    }

    public void showMusicSettingDialog(View v){

        //Create new Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Alarm Tone");

        String[] ringtoneMode = { "Ringtones", "Music"};

        builder.setItems(ringtoneMode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                onMusicSet(which);
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

        String[] projection = new String[] {
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS };

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
        }

        //Choose an Alarm
        chooseAlarmSongDialog(songList);
    }

    private void chooseAlarmSongDialog(final ArrayList<SongInformation> _Songs){

        //Create new Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Alarm");

        //Get SongNames from SongInformationArray
        final ArrayList<SongInformation> Songs = _Songs;
        ArrayList<String> songNameArrayList = new ArrayList<>();
        for(SongInformation songs : Songs)
            songNameArrayList.add(songs.getTitle());

        //Get Song Name Array and set it for Alarm Dialog
        final String[] songNameArray = songNameArrayList.toArray(new String[0]);

        builder.setItems(songNameArray, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                return;
            }


        });

        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            public void onShow(DialogInterface dialog) {

                ListView songsView = alertDialog.getListView();
                songsView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                        Uri fileUri = Uri.parse(_Songs.get(position).getPath());
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                        try{
                            //playMusic(fileUri);  TODO Error Handling!!
                            mediaPlayer.setDataSource(getApplicationContext(), fileUri);
                            mediaPlayer.prepare();
                        } catch(IOException e){
                            Log.e("Exception: ", e.getMessage());
                        }

                        mediaPlayer.start();
                        return true;
                    }
                });
            }
        });

        //Show Builder
        builder.show();
    }

    public void playMusic(Uri _SongUri) throws IOException{

        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(getApplicationContext(), _SongUri);
        mediaPlayer.prepare();
        mediaPlayer.start();
    }
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
