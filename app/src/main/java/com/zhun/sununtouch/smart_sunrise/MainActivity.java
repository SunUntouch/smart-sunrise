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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
                    implements TimePickerDialog.OnTimeSetListener{
    public final static String EXTRA_MESSAGE = "com.zhun.sununtouch.smart_sunrise.MESSAGE";
    //Shared Pref Settings
    public final static String WAKEUP_TIMER = "com.zhun.sununtouch.smart_sunrise.WAKEUP_SETTINGS";
    public final static String WAKEUP_TIMER_INFO = "com.zhun.sununtouch.smart_sunrise.WAKEUP_SETTINGS_INFO";

    //Alarm
    public final static String ALARM        = "Alarm";
    public final static String ALARM_NAME   = "Alarm_Name";
    public final static String ALARM_VALUE  = "Alarm_Value";
    public final static String ALARM_MINUTES= "Alarm_Minutes";
    public final static String ALARM_HOUR   = "Alarm_Hour";

    //ChildItems
    public final static String WAKEUP_DAYS  = "Days";
    public final static String WAKEUP_TIME  = "Time";
    public final static String WAKEUP_MUSIC = "Music";
    public final static String WAKEUP_LIGHT = "Light";

    //ExpendableList
    ExpandableListAdapter         expListAdapter;
    ExpandableListView            expListView;
    List<String>                  expListDataHeader;
    HashMap<String, List<String>> expListDataChild;

    //Actual Alarm Values
    int actualAlarm    = -1;
    int actualHour     = 0;
    int actualMin      = 0;

    private void changeListData(String _name, int _id){
        //Load sharedPrefereces
        String settingName = WAKEUP_TIMER + _id;
        SharedPreferences settings      = getApplicationContext().getSharedPreferences(settingName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        //Set new String Set
        //String name = _name;

        //add more

        //put StringSet back
        editor.putString(ALARM_NAME, _name);
        editor.putInt(ALARM_MINUTES, actualMin);
        editor.putInt(ALARM_HOUR, actualHour);
        editor.apply();
    }
    private void prepareLisData(){
            //List
        expListDataHeader = new ArrayList<String>();
        expListDataChild  = new HashMap<String, List<String>>();

        SharedPreferences information = getApplicationContext().getSharedPreferences(WAKEUP_TIMER_INFO, Context.MODE_PRIVATE);
        int amount = information.getInt(ALARM_VALUE, 0);

        if(amount == 0) {
            //No Alarm
            expListDataHeader.add("No Alarm Set");
            //Adding Child Data
            List<String> newAlarm = new ArrayList<String>();
            newAlarm.add(WAKEUP_TIME);
            newAlarm.add(WAKEUP_DAYS);
            newAlarm.add(WAKEUP_MUSIC);
            newAlarm.add(WAKEUP_LIGHT);

            //Header Child Put
            expListDataChild.put(expListDataHeader.get(0), newAlarm);
        }
        else {

            for (int _amount = 0; _amount < amount; ++_amount) {

                //sharedPrefereces
                String settingName = WAKEUP_TIMER + _amount;
                SharedPreferences settings = getApplicationContext().getSharedPreferences(settingName, Context.MODE_PRIVATE);

                //GetData
                String name = settings.getString(ALARM_NAME, "Alarm");

                //Set Data
                expListDataHeader.add(name);
                //Adding Child Data
                List<String> newAlarm = new ArrayList<String>();
                newAlarm.add(WAKEUP_TIME);
                newAlarm.add(WAKEUP_DAYS);
                newAlarm.add(WAKEUP_MUSIC);
                newAlarm.add(WAKEUP_LIGHT);

                //Header Child Put
                expListDataChild.put(expListDataHeader.get(_amount), newAlarm);
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

        //expListAdapter.notifyDataSetChanged();
        //expListView.invalidateViews();
        //expListView.setAdapter(expListAdapter);
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
        expListAdapter = new ExpandableListAdapter(this, expListDataHeader, expListDataChild);

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

                ExpandableListAdapter expListAdapterNew = new ExpandableListAdapter(expListView.getContext(), expListDataHeader, expListDataChild);
                expListAdapter = expListAdapterNew;
                expListView.setAdapter(expListAdapter);
            }
        });
    }

    public void showTimeSettingsDialog(View v){
        DialogFragment newFragment = new SettingTimeFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute){
        //Do Something with the Time

        actualHour = hourOfDay;
        actualMin  = minute;

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
