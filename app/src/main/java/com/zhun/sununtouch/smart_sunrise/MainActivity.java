package com.zhun.sununtouch.smart_sunrise;

import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    LinkedHashMap<String, List<String>> expListDataChild;

    LinkedHashMap<String, LinkedHashMap<String, List<LinkedHashMap<String, Integer>>>> expListDataChild2;

    //Actual Alarm Values
    private int actualAlarm    = -1;
    private int actualHour     = 0;
    private int actualMin      = 0;

    private int actualButtonID = 0;

    private void changeListData(String _name, int _id){
        //Load sharedPrefereces
        String settingName = WAKEUP_TIMER + _id;
        SharedPreferences settings      = getApplicationContext().getSharedPreferences(settingName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        if(!expListDataAlarm.contains(settingName));
            expListDataAlarm.add(settingName);

        //Set new String Set
        //String name = _name;

        //add more

        //put StringSet back
        editor.putString(ALARM_NAME, _name);
        editor.putInt(ALARM_TIME_MINUTES, actualMin);
        editor.putInt(ALARM_TIME_HOUR, actualHour);
        editor.apply();
    }
    private void prepareListDataValues(String _alarmName, int _id, int[] _time, int[] _days, int[] _music, int[] _light){

        //Load sharedPrefereces
        String settingName = ALARM + _id;
        if(!expListDataAlarm.contains(settingName));
        expListDataAlarm.add(settingName);

        expListDataHeader.add(_alarmName);
        //Adding Child Data
        LinkedHashMap<String, List<LinkedHashMap<String, Integer>>> newAlarm = new LinkedHashMap<String, List<LinkedHashMap<String, Integer>>>();
        List<LinkedHashMap<String, Integer>> newAlarmValues = new ArrayList<LinkedHashMap<String, Integer>>();
        LinkedHashMap<String, Integer> alarmvalue = new LinkedHashMap<String, Integer>();

        //Putting Value for each child
        //Time
        alarmvalue.clear();
        alarmvalue.put("Hour", _time[0]);
        alarmvalue.put("Minute", _time[1]);
        newAlarmValues.add(alarmvalue);
        newAlarm.put(WAKEUP_TIME, newAlarmValues);

        //Day
        alarmvalue.clear();
        alarmvalue.put("Monday"   , _days[0]);
        alarmvalue.put("Tuesday"  , _days[1]);
        alarmvalue.put("Wednesday", _days[2]);
        alarmvalue.put("Thursday" , _days[3]);
        alarmvalue.put("Friday"   , _days[4]);
        alarmvalue.put("Saturday" , _days[5]);
        alarmvalue.put("Sunday"   , _days[6]);

        newAlarmValues.add(alarmvalue);
        newAlarm.put(WAKEUP_DAYS, newAlarmValues);

        //Music
        alarmvalue.clear();
        alarmvalue.put("Song"      , _music[0]); //Maybe ID?
        alarmvalue.put("StartTime" , _music[1]);
        alarmvalue.put("Volume"    , _music[2]);
        alarmvalue.put("FadIn"     , _music[3]);
        alarmvalue.put("FadeInTime", _music[4]);

        newAlarmValues.add(alarmvalue);
        newAlarm.put(WAKEUP_MUSIC, newAlarmValues);

        //Light
        alarmvalue.clear();
        alarmvalue.put("UseScreen"   , _light[0]);
        alarmvalue.put("ScreenColor1", _light[1]);
        alarmvalue.put("ScreenColor2", _light[2]);
        alarmvalue.put("FadeColor"   , _light[3]);
        alarmvalue.put("FadeTime"    , _light[4]);
        alarmvalue.put("UseLED"      , _light[5]);

        newAlarmValues.add(alarmvalue);
        newAlarm.put(WAKEUP_LIGHT, newAlarmValues);

        expListDataChild2.put(expListDataAlarm.get(_id), newAlarm);
    }
    private void prepareLisData(){
        //List
        expListDataHeader = new ArrayList<String>();
        expListDataAlarm  = new ArrayList<String>();
        expListDataChild  = new LinkedHashMap<String, List<String>>();
        expListDataChild2 = new LinkedHashMap<String, LinkedHashMap<String, List<LinkedHashMap<String, Integer>>>>();

        SharedPreferences information = getApplicationContext().getSharedPreferences(WAKEUP_TIMER_INFO, Context.MODE_PRIVATE);
        int amount = information.getInt(ALARM_VALUE, 0);

        if(amount == 0) {
            //No Alarm
            //Putting Value for each child
            int[] time  = {00, 00};         // hour, minute
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
                String name = settings.getString(ALARM_NAME, "Alarm");

                //Putting Value for each child
                int[] time  = {settings.getInt(ALARM_TIME_HOUR, 00), settings.getInt(ALARM_TIME_MINUTES, 00)};         // hour, minute

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
            if(_id <= amount)
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
        expListAdapter.notifyDataSetChanged();
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
        expListAdapter = new ExpandableListAdapter(this, expListDataAlarm, expListDataHeader, expListDataChild2);

        //setting list adapter
        expListView.setAdapter(expListAdapter);

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
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
                actualAlarm = groupPosition;
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

                ExpandableListAdapter expListAdapterNew = new ExpandableListAdapter(expListView.getContext(), expListDataAlarm, expListDataHeader, expListDataChild2);
                expListAdapter = expListAdapterNew;
                expListView.setAdapter(expListAdapter);
            }
        });
    }

    public void showTimeSettingsDialog(View v){

        actualButtonID = v.getId();

        DialogFragment newFragment = new SettingTimeFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute){
        //Do Something with the Time

        actualHour = hourOfDay;
        actualMin  = minute;

        Button bTime = (Button) findViewById(actualButtonID);
        String timeText = hourOfDay + ":" + minute;
        bTime.setText(timeText);
        String settingsName = WAKEUP_TIMER + actualAlarm;

        SharedPreferences settings = getApplicationContext().getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        saveListDataChild(settings.getString(ALARM_NAME, ALARM), actualAlarm);
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
