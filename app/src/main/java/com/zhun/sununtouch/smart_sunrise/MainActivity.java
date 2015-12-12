package com.zhun.sununtouch.smart_sunrise;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.zhun.sununtouch.smart_sunrise.MESSAGE";
    //Shared Pref Settings
    public final static String WAKEUP_TIMER = "com.zhun.sununtouch.smart_sunrise.WAKEUP_SETTINGS";

    //Alarm
    public final static String ALARM        = "Alarm";
    public final static String ALARM_VALUE  = "Alarm_Value";
    public final static String ALARM_NAME   = "com.zhun.sununtouch.smart_sunrise.WAKEUP_SETTINGS_NAME";
    public final static String ALARM_DAYS   = "com.zhun.sununtouch.smart_sunrise.WAKEUP_SETTINGS_DAYS";
    public final static String ALARM_LIGHT  = "com.zhun.sununtouch.smart_sunrise.WAKEUP_SETTINGS_LIGHT";
    public final static String ALARM_MUSIC   = "com.zhun.sununtouch.smart_sunrise.WAKEUP_SETTINGS_MUSIC";

    //ExpendableList
    ExpandableListAdapter         expListAdapter;
    ExpandableListView            expListView;
    List<String>                  expListDataHeader;
    HashMap<String, List<String>> expListDataChild;

    public void sendMessage(View view){

//        Intent intent = new Intent(this, DisplayMessageActivity.class);
//
//        EditText editText = (EditText) findViewById(R.id.edit_message);
//        String message = editText.getText().toString();
//
//        intent.putExtra(EXTRA_MESSAGE, message);
//        startActivity(intent);
    }

    private void prepareLisData(){

        //sharedPrefereces
        SharedPreferences        settings = getApplicationContext().getSharedPreferences(WAKEUP_TIMER, 0);
        //List
        expListDataHeader = new ArrayList<String>();
        expListDataChild  = new HashMap<String, List<String>>();

        //Check if Alarm is Set
        int Alarms = settings.getInt(ALARM_VALUE, 0);
        if(Alarms > 0) {

                for(int _alarms = 0; _alarms < Alarms; ++_alarms) {

                String name = ALARM + Integer.toString(_alarms);

                //Adding Child
                String value = name + "_name";
                expListDataHeader.add(settings.getString(value, ALARM));

                //Adding Child Data
                List<String> newAlarm = new ArrayList<String>();
                newAlarm.add("Time");
                newAlarm.add("Days");
                newAlarm.add("Music");
                newAlarm.add("Light");

                //Header Child Put
                expListDataChild.put(expListDataHeader.get(_alarms), newAlarm);
            }
        }
        else {
            //No Alarm
            expListDataHeader.add("No Alarm Set");
            //Adding Child Data
            List<String> newAlarm = new ArrayList<String>();
            newAlarm.add("New Alarm?");

            //Header Child Put
            expListDataChild.put(expListDataHeader.get(0), newAlarm);
        }
    }

    private void saveListDataChild(String _name){

        //Load SharedPreferences
        SharedPreferences  settings     = getApplicationContext().getSharedPreferences(WAKEUP_TIMER, 0);
        SharedPreferences.Editor editor = settings.edit();

        //Get Values
        int id = settings.getInt(ALARM_VALUE, 0);
        String name = ALARM + Integer.toString(id);

        //Set New Values
        editor.putString(name, _name);
        editor.putInt(ALARM_VALUE, ++id);

        //prepare new List Data
        prepareLisData();
        expListAdapter.notifyDataSetChanged();
    }

    private void editListDataChild(String _name, int _id){

        //Load SharedPreferences
        SharedPreferences  settings     = getApplicationContext().getSharedPreferences(WAKEUP_TIMER, 0);
        SharedPreferences.Editor editor = settings.edit();

        //Get Values
        String name = ALARM + Integer.toString(_id);

        //Set New Values
        editor.putString(name, _name);

        //prepare new List Data
        prepareLisData();
        expListAdapter.notifyDataSetChanged();
    }

    private void deleteListDataChild(int _id){
        //Load SharedPreferences
        SharedPreferences  settings     = getApplicationContext().getSharedPreferences(WAKEUP_TIMER, 0);
        SharedPreferences.Editor editor = settings.edit();

        //Get Values
        String name = ALARM + Integer.toString(_id);

        //remove Item
        editor.remove(name);

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

        //Floating ActionButton/////////////////////////////////////////////////////////////////////
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
