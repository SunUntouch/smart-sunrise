package com.zhun.sununtouch.smart_sunrise;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity
                    implements TimePickerDialog.OnTimeSetListener{
    //ExpendableList
    private ExpandableListAdapter AlarmViewAdapter;
    private ExpandableListView    AlarmGroupView;

    //Actual Alarm Values
    private LinkedHashMap<Integer, AlarmConfiguration> alarmConfigurations = new LinkedHashMap<>();

    //Last Clicked AlarmGroup
    private int actualAlarm    =-1;

    //Media Player
    private MediaPlayer mediaPlayer;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /***********************************************************************************************
     * ONCREATE
     **********************************************************************************************/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set MainView//////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_main);

        //Set Toolbar///////////////////////////////////////////////////////////////////////////////
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Floating ActionButton/////////////////////////////////////////////////////////////////////
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveListDataChild(AlarmConstants.ALARM); //Add New Alarm
            }
        });

        // New Configuration and List View//////////////////////////////////////////////////////////
        AlarmViewAdapter = new ExpandableListAdapter(this, alarmConfigurations);
        AlarmGroupView = (ExpandableListView) findViewById(R.id.wakeup_timer_expendbleList);
        prepareConfiguration();

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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case AlarmConstants.ALARM_PERMISSION_MUSIC:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    searchMusic(1); //External Mode
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    Toast.makeText(MainActivity.this, R.string.wakeup_music_no_sd_card, Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    /***********************************************************************************************
     * DATA VALUES
     **********************************************************************************************/
    private void switchAlarmView(boolean visible, boolean invalidateView){

        LinearLayout AlarmNoLayout  = (LinearLayout) findViewById(R.id.wakeup_timer_no_Alarm_set_View);
        if(visible)
        {
            AlarmNoLayout.setVisibility(LinearLayout.GONE);
            AlarmGroupView.setVisibility(ExpandableListView.VISIBLE);
        }
        else
        {
            AlarmNoLayout.setVisibility(LinearLayout.VISIBLE);
            AlarmGroupView.setVisibility(ExpandableListView.GONE);
        }

        if(invalidateView)
            AlarmGroupView.invalidateViews();
    }

    private void prepareConfiguration(){
        prepareConfiguration(getPreferenceInfo().getInt(AlarmConstants.ALARM_VALUE, 0), true);
    }
    private void prepareConfiguration(int id, boolean loadAll){

        //Check if we have a Alarm
        boolean alarmExists = getPreferenceInfo().getInt(AlarmConstants.ALARM_VALUE, 0) != 0;
        if(alarmExists)
        {
            if(loadAll)
                loadConfig();
            else
                loadConfig(id, true);
        }
        switchAlarmView(alarmExists, true);
    }

    private void saveListDataChild(String name){
        //Get Shared Preferences
        saveListDataChild(name, getPreferenceInfo().getInt(AlarmConstants.ALARM_VALUE, 0)); //Load Amount from Config
    }
    private void saveListDataChild(String name, int id){
        //Get Shared Preferences
        int amount = getPreferenceInfo().getInt(AlarmConstants.ALARM_VALUE, 0);

        //changelistData
        if(amount == 0)
            saveConfigurationData(name, amount++);
        else if(id < amount)
            saveConfigurationData(name, id);
        else
            saveConfigurationData(name, amount++);

        //Add AlarmValue
        SharedPreferences.Editor editor = getPreferenceInfoEditor();
        editor.putInt(AlarmConstants.ALARM_VALUE, amount);
        editor.apply();

        //prepare new List Data
        prepareConfiguration(id, false);
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

        //Collapse Group
        if(AlarmGroupView.isGroupExpanded(ID))
            AlarmGroupView.collapseGroup(ID);

        //Copy Data to fill AlarmCount Gap
        int amount = getPreferenceInfo().getInt(AlarmConstants.ALARM_VALUE, 0);
        if( amount > 0)
        {
            --amount;
            for (int id = ID; id < amount; ++id)
            {
                SharedPreferences.Editor editorNew = getPreferenceSettingsEditor(id++);
                SharedPreferences settingsOld      = getPreferenceSettings(id);
                Map<String, ?> settingOld = settingsOld.getAll();

                for (Map.Entry<String, ?> value : settingOld.entrySet())
                {
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

            //Clear Old Entry
            SharedPreferences.Editor editor = getPreferenceSettingsEditor(amount);
            editor.clear();
            editor.apply();

            //Set new Amount Information
            editor = getPreferenceInfoEditor();
            editor.putInt(AlarmConstants.ALARM_VALUE, amount);
            editor.apply();

            //prepare new List Data
            alarmConfigurations.remove(amount);
            prepareConfiguration();
        }
    }

    private void saveConfigurationData(String name, int id){

        AlarmConfiguration config = new AlarmConfiguration();

        if(!alarmConfigurations.containsKey(id))
        {
            //put Alarm in map
            alarmConfigurations.put(id, config);
            switchAlarmView(false, false);
        }
        else
            config = alarmConfigurations.get(id);

        //Load sharedPreferences
        SharedPreferences.Editor editor = getPreferenceSettingsEditor(id);

        //Check if Alarm Exists
        //String alarmName = AlarmConstants.ALARM + id;

        //put StringSet back
        editor.putString(AlarmConstants.ALARM_NAME, name);

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
        addConfig(config, getPreferenceInfo().getInt(AlarmConstants.ALARM_VALUE, 0));
    }
    private void addConfig(AlarmConfiguration config, int id){
        alarmConfigurations.put(id, config);
    }
    private void loadConfig(){

        for(int ID = 0; ID < getPreferenceInfo().getInt(AlarmConstants.ALARM_VALUE, 0); ++ID)
            loadConfig(ID, false);

        if(AlarmViewAdapter != null)
            AlarmViewAdapter.notifyDataSetChanged(alarmConfigurations);
    }
    private void loadConfig(int ID, boolean notify){

        //save Settings
        SharedPreferences settings = getPreferenceSettings(ID);
        AlarmConfiguration newAlarm = new AlarmConfiguration();

        //ID, Name, AlarmSet
        newAlarm.setAlarmID(ID);
        newAlarm.setAlarmName(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM + Integer.toString(ID)));
        newAlarm.setAlarm(settings.getBoolean(AlarmConstants.ALARM_TIME_SET, false));

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

        //Time
        Calendar calendar = Calendar.getInstance();
        newAlarm.setHour  (settings.getInt(AlarmConstants.ALARM_TIME_HOUR   , calendar.get(Calendar.HOUR_OF_DAY)));
        newAlarm.setMinute(settings.getInt(AlarmConstants.ALARM_TIME_MINUTES, calendar.get(Calendar.MINUTE)));
        newAlarm.setSnooze(settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE , AlarmConstants.ACTUAL_TIME_SNOOZE));    // hour, minute, snooze

        alarmConfigurations.put(ID, newAlarm);

        if(AlarmViewAdapter != null && notify)
            AlarmViewAdapter.notifyDataSetChanged(alarmConfigurations);
    }
    private void loadValuesNew(int ID){

        debug_assertion(!alarmConfigurations.containsKey(ID));

        //Set actual Alarm
        actualAlarm = ID;

        //save Settings
        SharedPreferences settings = getPreferenceSettings(ID);

        //ID, name, AlarmSet
        alarmConfigurations.get(ID).setAlarmID(ID);
        alarmConfigurations.get(ID).setAlarmName(settings.getString(AlarmConstants.ALARM_NAME, AlarmConstants.ALARM + Integer.toString(ID)));
        alarmConfigurations.get(ID).setAlarm(settings.getBoolean(AlarmConstants.ALARM_TIME_SET, false));

        //Days
        alarmConfigurations.get(ID).setMonday   (settings.getInt(AlarmConstants.ALARM_DAY_MONDAY   , AlarmConstants.ACTUAL_DAY_MONDAY));
        alarmConfigurations.get(ID).setTuesday  (settings.getInt(AlarmConstants.ALARM_DAY_TUESDAY  , AlarmConstants.ACTUAL_DAY_TUESDAY));
        alarmConfigurations.get(ID).setWednesday(settings.getInt(AlarmConstants.ALARM_DAY_WEDNESDAY, AlarmConstants.ACTUAL_DAY_WEDNESDAY));
        alarmConfigurations.get(ID).setThursday (settings.getInt(AlarmConstants.ALARM_DAY_THURSDAY , AlarmConstants.ACTUAL_DAY_THURSDAY));
        alarmConfigurations.get(ID).setFriday   (settings.getInt(AlarmConstants.ALARM_DAY_FRIDAY   , AlarmConstants.ACTUAL_DAY_FRIDAY));
        alarmConfigurations.get(ID).setSaturday (settings.getInt(AlarmConstants.ALARM_DAY_SATURDAY , AlarmConstants.ACTUAL_DAY_SATURDAY));
        alarmConfigurations.get(ID).setSunday   (settings.getInt(AlarmConstants.ALARM_DAY_SUNDAY   , AlarmConstants.ACTUAL_DAY_SUNDAY)); // Monday - Sunday

        //Load Music
        alarmConfigurations.get(ID).setSongURI          (settings.getString(AlarmConstants.ALARM_MUSIC_SONGID      , AlarmConstants.ACTUAL_MUSIC_SONG_URI));
        alarmConfigurations.get(ID).setSongStart        (settings.getInt(AlarmConstants.ALARM_MUSIC_SONGSTART      , AlarmConstants.ACTUAL_MUSIC_START));
        alarmConfigurations.get(ID).setSongLength       (settings.getInt(AlarmConstants.ALARM_MUSIC_SONGLENGTH     , AlarmConstants.ACTUAL_MUSIC_LENGTH));
        alarmConfigurations.get(ID).setVolume           (settings.getInt(AlarmConstants.ALARM_MUSIC_VOLUME         , AlarmConstants.ACTUAL_MUSIC_VOLUME));
        alarmConfigurations.get(ID).setFadeIn           (settings.getInt(AlarmConstants.ALARM_MUSIC_FADEIN         , AlarmConstants.ACTUAL_MUSIC_FADE_IN));
        alarmConfigurations.get(ID).setFadeInTime       (settings.getInt(AlarmConstants.ALARM_MUSIC_FADEINTIME     , AlarmConstants.ACTUAL_MUSIC_FADE_IN_TIME));
        alarmConfigurations.get(ID).setVibration        (settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV, AlarmConstants.ACTUAL_MUSIC_VIBRATION));
        alarmConfigurations.get(ID).setVibrationStrength(settings.getInt(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE, AlarmConstants.ACTUAL_MUSIC_VIBRATION_STRENGTH));// Song, StartTime, Volume, FadIn, FadeInTime

        //Load Light
        alarmConfigurations.get(ID).setScreen          (settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN           , AlarmConstants.ACTUAL_SCREEN));
        alarmConfigurations.get(ID).setScreenBrightness(settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS, AlarmConstants.ACTUAL_SCREEN_BRIGHTNESS));
        alarmConfigurations.get(ID).setScreenStartTime (settings.getInt(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME, AlarmConstants.ACTUAL_SCREEN_START));

        alarmConfigurations.get(ID).setLightColor1(settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR1   , AlarmConstants.ACTUAL_SCREEN_COLOR1));
        alarmConfigurations.get(ID).setLightColor2(settings.getInt(AlarmConstants.ALARM_LIGHT_COLOR2   , AlarmConstants.ACTUAL_SCREEN_COLOR2));
        alarmConfigurations.get(ID).setLightFade  (settings.getInt(AlarmConstants.ALARM_LIGHT_FADECOLOR, AlarmConstants.ACTUAL_SCREEN_COLOR_FADE));

        alarmConfigurations.get(ID).setLED         (settings.getInt(AlarmConstants.ALARM_LIGHT_USELED        , AlarmConstants.ACTUAL_LED));
        alarmConfigurations.get(ID).setLEDStartTime(settings.getInt(AlarmConstants.ALARM_LIGHT_LED_START_TIME, AlarmConstants.ACTUAL_LED_START));// UseScreen, ScreenColor1, ScreenColor2, Fadecolor, FadeTime, UseLED

        //Time
        Calendar calendar = Calendar.getInstance();
        alarmConfigurations.get(ID).setHour  (settings.getInt(AlarmConstants.ALARM_TIME_HOUR   , calendar.get(Calendar.HOUR_OF_DAY)));
        alarmConfigurations.get(ID).setMinute(settings.getInt(AlarmConstants.ALARM_TIME_MINUTES, calendar.get(Calendar.MINUTE)));
        alarmConfigurations.get(ID).setSnooze(settings.getInt(AlarmConstants.ALARM_TIME_SNOOZE , AlarmConstants.ACTUAL_TIME_SNOOZE));    // hour, minute, snooze

        if(AlarmViewAdapter != null)
            AlarmViewAdapter.notifyDataSetChanged(alarmConfigurations);
    }

    private AlarmConfiguration getConfig(int ID){
        return alarmConfigurations.get(ID);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void saveSettings(int actualAlarm, String alarmName){
        saveSettings(actualAlarm, alarmName, alarmName);
    }
    private void saveSettings(int ID, String alarmName, String alarmIdentifier){
        //save Settings
        SharedPreferences settings = getPreferenceSettings(ID);
        saveListDataChild(settings.getString(alarmName, alarmIdentifier), ID);
    }

    private LinearLayout createAlertLinearLayout(View v, TextView textView, SeekBar seekBar, int max, int increment, int progress){

        //LinearLayout
        LinearLayout linearLayout = new LinearLayout(v.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //TextView to show Value of SeekBar
        textView.setVisibility(TextView.INVISIBLE);
        linearLayout.addView(textView);

        //Seek Bar
        seekBar.setMax(max);
        seekBar.setKeyProgressIncrement(increment);
        seekBar.setProgress(progress);
        linearLayout.addView(seekBar);
        return linearLayout;
    }

    private SharedPreferences.Editor getPreferenceSettingsEditor(int id){
        return AlarmSharedPreferences.getSharedPreference(getApplicationContext(), AlarmConstants.WAKEUP_TIMER, id).edit();
    }
    private SharedPreferences getPreferenceSettings(int id){
        return AlarmSharedPreferences.getSharedPreference(getApplicationContext(), AlarmConstants.WAKEUP_TIMER, id);
    }
    private SharedPreferences.Editor getPreferenceInfoEditor(){
        return AlarmSharedPreferences.getSharedPreference(getApplicationContext()).edit();
    }
    private SharedPreferences getPreferenceInfo(){
        return AlarmSharedPreferences.getSharedPreference(getApplicationContext());
    }

    private int getSeekBarPosition(int progress, int right, int left, int width, int offset, int max){
        //Get Position of Text
        return (progress * (width - (right + left) * offset)) / max;
    }
    private int getSeekBarPosition(int progress, int right, int width, int offset, int max){
        return getSeekBarPosition(progress, right, 0, width, offset, max);
    }
    private void debug_assertion(boolean check){

        if(BuildConfig.DEBUG && check)
            throw new AssertionError();
    }
    /***********************************************************************************************
     * Set New Alarm
     **********************************************************************************************/
    public     void setNewAlarm(View v){

        //Get Toggle Button
        ToggleButton activeAlarmToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_setAlarmButton);

        //Set Alarm
        boolean toggle = activateAlarm(activeAlarmToggle.isChecked());
        activeAlarmToggle.setChecked(toggle);
        alarmConfigurations.get(actualAlarm).setAlarm(toggle);

        //save Settings
        saveSettings(actualAlarm, AlarmConstants.ALARM_NAME);

        //show Toast
        String alarmText = String.format(getString(R.string.toast_positive_alarm),
                                String.format(Locale.US, "%02d:%02d", getConfig(actualAlarm).getHour(), getConfig(actualAlarm).getMinute()));
        AlarmToast.showToastShort(getApplicationContext(), getConfig(actualAlarm).isAlarmSet(), alarmText, getString(R.string.toast_negative_alarm));
    }
    private boolean activateAlarm(boolean active){

        //Get new Alarm and Set
        AlarmManage newAlarm = new AlarmManage(getApplicationContext(), getConfig(actualAlarm) );
        if(active)
            newAlarm.setNewAlarm(actualAlarm, false);
        else
            newAlarm.cancelAlarmwithButton(actualAlarm);

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
        try
        {
            AlarmNameAlert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); //TODO
        }
        catch (NullPointerException e)
        {
            AlarmToast.showToastShort(getApplicationContext(), getConfig(actualAlarm).isAlarmSet(), "Error: " + e.getMessage(), getString(R.string.toast_negative_alarm));
        }

        AlarmNameAlert.show();
    }
    private void onAlarmNameSet(String name){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //save Settings and reactivate Alarm
        saveListDataChild(name, actualAlarm);
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    /***********************************************************************************************
     * DAY SETTING DIALOG
     **********************************************************************************************/
    public void onDaysSet(View v){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //The Day View has only Toggle Buttons which call this method
        ToggleButton toggle = (ToggleButton) v;
        switch(v.getId()){
            case R.id.wakeup_monday   : alarmConfigurations.get(actualAlarm).setMonday   ((toggle.isChecked()) ? 1 : 0); break;
            case R.id.wakeup_tuesday  : alarmConfigurations.get(actualAlarm).setTuesday  ((toggle.isChecked()) ? 1 : 0); break;
            case R.id.wakeup_wednesday: alarmConfigurations.get(actualAlarm).setWednesday((toggle.isChecked()) ? 1 : 0); break;
            case R.id.wakeup_thursday : alarmConfigurations.get(actualAlarm).setThursday ((toggle.isChecked()) ? 1 : 0); break;
            case R.id.wakeup_friday   : alarmConfigurations.get(actualAlarm).setFriday   ((toggle.isChecked()) ? 1 : 0); break;
            case R.id.wakeup_saturday : alarmConfigurations.get(actualAlarm).setSaturday ((toggle.isChecked()) ? 1 : 0); break;
            case R.id.wakeup_sunday   : alarmConfigurations.get(actualAlarm).setSunday   ((toggle.isChecked()) ? 1 : 0); break;
            default: debug_assertion(true); break;
        }
        //save Settings and reactivate Alarm
        saveSettings(actualAlarm, AlarmConstants.ALARM_NAME);
        activateAlarm(getConfig(actualAlarm).isAlarmSet());
        AlarmGroupView.invalidateViews();
    }

    /***********************************************************************************************
     * TIME SETTING DIALOG
     **********************************************************************************************/
    public void showTimeSettingsDialog(View v){

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
        saveSettings(actualAlarm, AlarmConstants.ALARM_NAME);

        //reactivate Alarm
        activateAlarm(getConfig(actualAlarm).isAlarmSet());
    }

    /***********************************************************************************************
     * MINUTE SETTING DIALOG
     **********************************************************************************************/
    public  void showMinuteSettingDialog(View v) {

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //TextView to show Value of SeekBar
        final TextView textView = new TextView(v.getContext());
        //SeekBar
        final SeekBar seekBar = new SeekBar(v.getContext());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Set Position of Text
                int val = getSeekBarPosition(progress, 6, seekBar.getWidth(), seekBar.getThumbOffset(), seekBar.getMax());
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

        //Create new Builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_minutes));
        builder.setView(createAlertLinearLayout(v, textView, seekBar, 99, 1, alarmConfigurations.get(actualAlarm).getSnooze() - 1));
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
    private void onSnoozeMinutesSet(int minutes){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Save Snooze Minutes
        alarmConfigurations.get(actualAlarm).setSnooze(minutes + 1); //we Start with 1 minute

        //save Settings
        saveSettings(actualAlarm, AlarmConstants.ALARM_NAME);

        //reactivate Alarm
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    /***********************************************************************************************
     * MUSIC SET DIALOG
     **********************************************************************************************/
    public  void showMusicSettingDialog(View v){

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
    private void onMusicSet(int modeID){

        if(modeID == 0)
            searchMusic(modeID);
        else
        {
            //Check if SD Card is Present
            boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
            if(isSDPresent )
                checkMusicPermission();
            else
                Toast.makeText(MainActivity.this, R.string.wakeup_music_no_sd_card, Toast.LENGTH_SHORT).show();
        }
    }
    private void chooseAlarmSongDialog(final ArrayList<SongInformation> songs){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Create new Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_song));

        //Get SongNames from SongInformationArray
        ArrayList<String> songNames = new ArrayList<>();
        for(SongInformation song : songs)
        {
            //Get Name with Extension and remove it
            String songName  = song.getTitle();
            if(songName != null)
            {
                if(songName.contains("."))
                    songName = songName.substring(0, songName.lastIndexOf('.'));
                songNames.add(songName);
            }
        }
        //Get Song Name Array and set it for Alarm Dialog Builder
        builder.setItems(songNames.toArray(new String[songNames.size()]), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                //Get Song Lengh
                saveSongLength(songs.get(which).getPath());

                //save Settings reactivate Alarm
                saveSettings(actualAlarm, AlarmConstants.ALARM_NAME);
                activateAlarm(getConfig(actualAlarm).isAlarmSet());
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
                        try
                        {
                            prepareMusic(Uri.parse(songs.get(position).getPath()));
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
        alertDialog.show();
    }
    private void prepareMusic(Uri uri) throws IOException{

        //Check for MediaPlayer
        if(mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        else
            mediaPlayer.reset();

        //Set MediaPlayer Values
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(getApplicationContext(), uri);
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

    private void saveSongLength(String uri){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Get Song Length
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(uri);

        //Get Values from chosen Song
        String durationStr = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        alarmConfigurations.get(actualAlarm).setSongLength((int) (Long.parseLong(durationStr) / 1000));
        alarmConfigurations.get(actualAlarm).setSongURI(uri);
    }

    private void searchMusic(int modeID){
        //Get All Song Values from the Android Media Content URI
        //Default for Uri is the internal Memory, because it is every time available
        //If the User Chooses the second entry switch to external Files
        Uri allSongUri = (modeID == 1)? MediaStore.Audio.Media.EXTERNAL_CONTENT_URI : MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

        //Set Values for the Resolver
        String[] STAR = { "*" };

        //Resolve ContentURI
        ContentResolver musicResolver = getApplicationContext().getContentResolver();
        Cursor cursor = musicResolver.query(allSongUri, STAR, null, null, null);

        //Search Cursor for Values
        if(cursor != null)
        {
            //ArrayList for Music Entries
            ArrayList<SongInformation> songList = new ArrayList<>();
            if(cursor.moveToFirst())
            {
                do{
                    int song_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String song_name   = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String fullPath    = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String album_name  = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String artist_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    songList.add(new SongInformation(song_id,  song_name, artist_name, album_name, fullPath));

                    //int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
                    //int album_id   = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    //int artist_id  = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
                    //int isAlarm    = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_ALARM));
                    //int isRingtone = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_RINGTONE));
                }
                while(cursor.moveToNext());

                //Choose an Alarm
                chooseAlarmSongDialog(songList);
                cursor.close();
            }
        }
        else
            Toast.makeText(MainActivity.this, R.string.wakeup_music_no_music, Toast.LENGTH_SHORT).show();
    }
    private void checkMusicPermission(){

        if(ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE))
            {
                //we will jump to the Handler if the user accepts or declines th permission and start there our Dialog
            }
            else
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, AlarmConstants.ALARM_PERMISSION_MUSIC);
        }
    }

    /***********************************************************************************************
     * MUSIC VOLUME DIALOG
     **********************************************************************************************/
    public  void showMusicVolumeSettingDialog(View v){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //TextView to show Value of SeekBar
        final TextView textView = new TextView(v.getContext());
        //SeekBar
        final SeekBar seekBar = new SeekBar(v.getContext());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Set Position of Text
                int val = getSeekBarPosition(progress, 4, seekBar.getWidth(), seekBar.getThumbOffset(), seekBar.getMax());
                textView.setText(String.format(Locale.US, "%d", progress));
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

        //Create new Builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_song_Volume));
        builder.setView(createAlertLinearLayout(v, textView, seekBar, 100, 1, alarmConfigurations.get(actualAlarm).getVolume()));
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
        builder.show();
    }
    private void onMusicVolumeSet(int volume){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Set ActualVolume
        alarmConfigurations.get(actualAlarm).setVolume(volume);

        //save Settings reactivate Alarm
        saveSettings(actualAlarm, AlarmConstants.ALARM_NAME);
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    /***********************************************************************************************
     * MUSIC START TIME DIALOG
     **********************************************************************************************/
    public  void showMusicStartSettingDialog(View v){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //TextView to show Value of SeekBar
        final TextView textView = new TextView(v.getContext());
        //SeekBar
        final SeekBar seekBar = new SeekBar(v.getContext());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Set Position of Text
                int val = getSeekBarPosition(progress, 5, seekBar.getWidth(), seekBar.getThumbOffset(), seekBar.getMax());
                textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
                textView.setText(String.format(Locale.US, "%02d:%02d", TimeUnit.SECONDS.toMinutes(progress),
                                                            progress - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(progress))));
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

        //Create new Builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_song_Start));
        builder.setView(createAlertLinearLayout( v, textView, seekBar, alarmConfigurations.get(actualAlarm).getSongLength(), 1, alarmConfigurations.get(actualAlarm).getSongStart()));
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
        builder.show();
    }
    private void onMusicStartSet(int seconds){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Set ActualStart
        alarmConfigurations.get(actualAlarm).setSongStart(seconds);

        //save Settings reactivate Alarm
        saveSettings(actualAlarm, AlarmConstants.ALARM_NAME);
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

        //Set On LongClickListener
        fadeInToggle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                //TextView to show Value of SeekBar
                final TextView textView = new TextView(v.getContext());
                //SeekBar
                final SeekBar seekBar = new SeekBar(v.getContext());
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                        //Set Position of Text
                        int val = getSeekBarPosition(progress, 5, seekBar.getWidth(), seekBar.getThumbOffset(), seekBar.getMax());
                        textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
                        textView.setText(String.format(Locale.US, "%02d:%02d", TimeUnit.SECONDS.toMinutes(progress),
                                                                    progress - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(progress))));
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
            //Create new Builder
            final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle(v.getContext().getString(R.string.wakeup_set_alarm_song_fadeIn));

            //Set AlertDialog View
            builder.setView(
                    createAlertLinearLayout(
                        v,
                        textView,
                        seekBar,
                        alarmConfigurations.get(actualAlarm).getSongLength(),
                        1,
                        alarmConfigurations.get(actualAlarm).getSongStart()));
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
    private void onFadeInTimeSet(int seconds){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Save Snooze Minutes
        alarmConfigurations.get(actualAlarm).setFadeInTime(seconds);
        alarmConfigurations.get(actualAlarm).setFadeIn(1); //true

        //save Settings reactivate Alarm
        saveSettings(actualAlarm, AlarmConstants.ALARM_NAME);
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

        //Set On LongClickListener
        vibrationToggle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                //TextView to show Value of SeekBar
                final TextView textView = new TextView(v.getContext());
                //SeekBar
                final SeekBar seekBar = new SeekBar(v.getContext());
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                        //Set Position of Text
                        int val = getSeekBarPosition(progress, 4, seekBar.getWidth(), seekBar.getThumbOffset(), seekBar.getMax());
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

                //Create new Builder
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(v.getContext().getString(R.string.wakeup_set_alarm_song_vibration));
                builder.setView(createAlertLinearLayout(v, textView, seekBar, 100, 1, alarmConfigurations.get(actualAlarm).getVibrationStrength()));
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
                builder.show();
                return false;
            }
        });
    }
    private void onVibrationStrengthSet(int strength){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Save Vibration Values
        alarmConfigurations.get(actualAlarm).setVibrationStrength(strength);
        alarmConfigurations.get(actualAlarm).setVibration(1); //true

        //save Settings reactivate Alarm
        saveSettings(actualAlarm, AlarmConstants.ALARM_NAME);
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

        //Set On LongClickListener
        screenToggle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                //TextView to show Value of SeekBar
                final TextView textView = new TextView(v.getContext());
                //Seekbar
                final SeekBar seekBar = new SeekBar(v.getContext());
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        //Set Position of Text
                        int val = getSeekBarPosition(progress, 4, seekBar.getWidth(), seekBar.getThumbOffset(), seekBar.getMax());
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

                //Create new Builder
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(v.getContext().getString(R.string.wakeup_set_alarm_light_brightness));

                //Set Alertdialog View
                builder.setView(createAlertLinearLayout(v, textView, seekBar, 99, 1, alarmConfigurations.get(actualAlarm).getScreenBrightness() - 1)); //We must -1 because we dont want to have zero brightness
                builder.setPositiveButton(v.getContext().getString(R.string.wakeup_OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Set and Save Vibration Strength
                        onScreenBrightnessSet(seekBar.getProgress() + 1);  //+1 because we don't want to have zero brightness set
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
                builder.show();
                return false;
            }
        });
    }
    private void onScreenBrightnessSet(int brightness){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        alarmConfigurations.get(actualAlarm).setScreenBrightness(brightness);
        alarmConfigurations.get(actualAlarm).setScreen(1); //true

        //save Settings reactivate Alarm
        saveSettings(actualAlarm, AlarmConstants.ALARM_NAME);
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }

    public  void showScreenLightStartSettingDialog(View v){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //TextView to show Value of SeekBar
        final TextView textView = new TextView(v.getContext());

        //Seek Bar
        final SeekBar seekBar = new SeekBar(v.getContext());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Set Position of Text
                int val = getSeekBarPosition(progress, 6, seekBar.getWidth(), seekBar.getThumbOffset(), seekBar.getMax());
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

        //Create new Builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_light_minutes));
        builder.setView(createAlertLinearLayout(v, textView, seekBar, 100, 1, alarmConfigurations.get(actualAlarm).getScreenStartTime()));
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
        builder.show();
    }
    private void onScreenStartTimeSet(int minutes){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Save ScreenStart Minutes
        alarmConfigurations.get(actualAlarm).setScreenStartTime(minutes);  //we Start with 1 minute

        //save Settings reactivate Alarm
        saveSettings(actualAlarm, AlarmConstants.ALARM_NAME);
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
    private void onColorSet(Button bView, int color){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        final String color1 = this.getString(R.string.wakeup_light_screen_color1);
        final String color2 = this.getString(R.string.wakeup_light_screen_color2);

        if(color1.equals(bView.getText().toString()))
            alarmConfigurations.get(actualAlarm).setLightColor1(color);
        else if(color2.equals(bView.getText().toString()))
            alarmConfigurations.get(actualAlarm).setLightColor2(color);
        else
            debug_assertion(true);

        //save Settings
        saveSettings(actualAlarm, AlarmConstants.ALARM_NAME);

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

        //save Settings reactivate Alarm
        saveSettings(actualAlarm, AlarmConstants.ALARM_NAME);
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

        //save Settings reactivate Alarm
        saveSettings(actualAlarm, AlarmConstants.ALARM_NAME);
        activateAlarm(alarmConfigurations.get(actualAlarm).isAlarmSet());
    }
    public  void showLEDLightStartSettingDialog(View v){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //TextView to show Value of SeekBar
        final TextView textView = new TextView(v.getContext());

        //Seek Bar
        final SeekBar seekBar = new SeekBar(v.getContext());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Set Position of Text
                int val = getSeekBarPosition(progress, 6, seekBar.getWidth(), seekBar.getThumbOffset(), seekBar.getMax());
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

        //Create new Builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_LED_time));
        builder.setView(createAlertLinearLayout(v, textView, seekBar, 100, 1, alarmConfigurations.get(actualAlarm).getLEDStartTime()));
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
        builder.show();
    }
    private void onLEDStartTimeSet(int minutes){

        debug_assertion(!alarmConfigurations.containsKey(actualAlarm));

        //Save LEDSTartTime Minutes
        alarmConfigurations.get(actualAlarm).setLEDStartTime(minutes);

        //save Settings reactivate Alarm
        saveSettings(actualAlarm, AlarmConstants.ALARM_NAME);
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
