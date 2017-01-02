package com.zhun.sununtouch.smart_sunrise;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
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
import android.widget.ArrayAdapter;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity
                    implements TimePickerDialog.OnTimeSetListener{
    //ExpendableList
    private ExpandableListAdapter AlarmViewAdapter;
    private ExpandableListView    AlarmGroupView;

    //Actual Alarm Values
    private AlarmConfigurationList m_AlarmConfigurations;

    //Last Clicked AlarmGroup
    private int actualAlarm    =-1;

    //Media Player
    private MediaPlayer mediaPlayer;

    //Vibrator
    private Vibrator m_Vibrator;

    //Thread
    private AlarmWorkerThread mThread;

    private boolean m_isVisible = false;

    /***********************************************************************************************
     * ONCREATE AND HELPER
     **********************************************************************************************/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mThread = new AlarmWorkerThread("Smart_Sunrise_Main_Worker");
        m_AlarmConfigurations = new AlarmConfigurationList(getApplicationContext());

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

                switchAlarmView(true);

                m_AlarmConfigurations.addAlarm(new AlarmConfiguration(getApplicationContext()));
                AlarmViewAdapter.notifyDataSetChanged(m_AlarmConfigurations);

                if(AlarmGroupView.isGroupExpanded(actualAlarm))
                    AlarmGroupView.collapseGroup(actualAlarm);
            }
        });

        // New com.zhun.sununtouch.smart_sunrise.Configuration and List View//////////////////////////////////////////////////////////
        AlarmViewAdapter = new ExpandableListAdapter(this, m_AlarmConfigurations);
        AlarmGroupView = (ExpandableListView) findViewById(R.id.wakeup_timer_expendbleList);

        AlarmGroupView.setAdapter(AlarmViewAdapter);
        AlarmGroupView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            int previousGroup = -1;
            @Override
            public void onGroupExpand(int groupPosition) {

                if (groupPosition != previousGroup)
                {
                    if(previousGroup != -1)
                        AlarmGroupView.collapseGroup(previousGroup);

                    previousGroup = groupPosition;
                    actualAlarm = groupPosition;
                }
            }
        });
        AlarmGroupView.requestFocus();

        //Set View
        switchAlarmView(!m_AlarmConfigurations.isEmpty());
    }
    protected void onDestroy() {
        mThread.quit();
        super.onDestroy();
    }
    private void debug_assertion(boolean check){

        if(BuildConfig.DEBUG && check)
            throw new AssertionError();
    }

    private void switchAlarmView(boolean visible){

        LinearLayout AlarmNoLayout  = (LinearLayout) findViewById(R.id.wakeup_timer_no_Alarm_set_View);
        if(visible && !m_isVisible){

            AlarmNoLayout.setVisibility(LinearLayout.GONE);
            AlarmGroupView.setVisibility(ExpandableListView.VISIBLE);
            AlarmGroupView.invalidateViews();
            m_isVisible = true;
        }else if(!visible && m_isVisible){

            AlarmNoLayout.setVisibility(LinearLayout.VISIBLE);
            AlarmGroupView.setVisibility(ExpandableListView.GONE);
            AlarmGroupView.invalidateViews();
            m_isVisible = false;
        }
    }

    private LinearLayout createAlertLinearLayout(View v, TextView textView, SeekBar seekBar, int max, int increment, int progress){
        //LinearLayout
        LinearLayout linearLayout = new LinearLayout(v.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //TextView to show Value of SeekBar
        textView.setVisibility(TextView.INVISIBLE);
        linearLayout.addView(textView);

        ///SeekBar
        seekBar.setMax(max);
        seekBar.setKeyProgressIncrement(increment);
        seekBar.setProgress(progress);
        linearLayout.addView(seekBar);
        return linearLayout;
    }
    private int getSeekBarPosition(int progress, int right, int left, int width, int offset, int max){
        //Get Position of Text
        return (progress * (width - (right + left) * offset)) / max;
    }
    private int getSeekBarPosition(int progress, int right, int width, int offset, int max){
        return getSeekBarPosition(progress, right, 0, width, offset, max);
    }

    private void setRunnable(Runnable runnable){
        setRunnable(mThread, runnable, 0);
    }
    private void setRunnable(AlarmWorkerThread thread, Runnable runnable, long millis){

        if(!thread.isAlive())
        {
            thread.start();
            thread.prepareHandler();
        }

        if(millis == 0)
            thread.postTask(runnable);
        else
            thread.postDelayedTask(runnable, millis);
    }

    /***********************************************************************************************
     * DATA VALUES
     **********************************************************************************************/
    public  void deleteChild(View v){

        //Show Message One Time, when the OnClickListeners are set then they will always be invoked
        Toast.makeText(MainActivity.this, getString(R.string.delete_warning), Toast.LENGTH_SHORT).show();

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

                if(AlarmGroupView.isGroupExpanded(actualAlarm))
                    AlarmGroupView.collapseGroup(actualAlarm);

                if(!m_AlarmConfigurations.isEmpty())
                {
                    m_AlarmConfigurations.removeAlarm(actualAlarm);
                    AlarmViewAdapter.notifyDataSetChanged(m_AlarmConfigurations);
                }

                if(m_AlarmConfigurations.isEmpty())
                    switchAlarmView(false);

                return false;
            }
        });
    }

    private AlarmConfiguration getAlarm(int ID){
        return m_AlarmConfigurations.getAlarm(ID);
    }

    private void updateChanges(AlarmConfiguration alarm){
        updateChanges(alarm, true);
    }
    private void updateChanges(AlarmConfiguration alarm, boolean notify){
        updateChanges(alarm, notify, false);
    }
    private void updateChanges(AlarmConfiguration alarm, boolean notify, boolean invalidate){

        m_AlarmConfigurations.setAlarm(alarm);

        //Don't need both
        if(invalidate)
            AlarmGroupView.invalidateViews();
        else if(notify)
            AlarmViewAdapter.notifyDataSetChanged(m_AlarmConfigurations);
    }

    /***********************************************************************************************
     * Set New Alarm
     **********************************************************************************************/
    public void setNewAlarm(View v){

        //Get Toggle Button
        ToggleButton activeAlarmToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_setAlarmButton);

        AlarmConfiguration alarm = getAlarm(actualAlarm);
        boolean alarmSet = (activeAlarmToggle.isChecked()) ? alarm.activateAlarm() : alarm.cancelAlarm();
        activeAlarmToggle.setChecked(alarmSet);
        updateChanges(alarm);
    }

    /***********************************************************************************************
     * AlarmConstants.ALARM NAME SETTING DIALOG
     **********************************************************************************************/
    public void showNameSettingDialog(View v){

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
            AlarmToast.showToastShort(getApplicationContext(), getAlarm(actualAlarm).isAlarmSet(), "Error: " + e.getMessage(), getString(R.string.toast_negative_alarm));
        }

        AlarmNameAlert.show();
    }
    private void onAlarmNameSet(String name){

        //save Settings and reactivate Alarm
        AlarmConfiguration alarm = getAlarm(actualAlarm);
        alarm.setAlarmName(name);
        updateChanges(alarm);
    }
    /***********************************************************************************************
     * DAY SETTING DIALOG
     **********************************************************************************************/
    public void onDaysSet(View v){

        //The Day View has only Toggle Buttons which call this method
        AlarmConfiguration alarm = getAlarm(actualAlarm);
        ToggleButton toggle = (ToggleButton) v;
        switch(v.getId()){
            case R.id.wakeup_monday   : alarm.setMonday   (toggle.isChecked()); break;
            case R.id.wakeup_tuesday  : alarm.setTuesday  (toggle.isChecked()); break;
            case R.id.wakeup_wednesday: alarm.setWednesday(toggle.isChecked()); break;
            case R.id.wakeup_thursday : alarm.setThursday (toggle.isChecked()); break;
            case R.id.wakeup_friday   : alarm.setFriday   (toggle.isChecked()); break;
            case R.id.wakeup_saturday : alarm.setSaturday (toggle.isChecked()); break;
            case R.id.wakeup_sunday   : alarm.setSunday   (toggle.isChecked()); break;
            default: debug_assertion(true); break;
        }
        //save Settings and reactivate Alarm
        updateChanges(alarm);

        //refresh Alarm separate only for some Key Values
        if(alarm.isAlarmSet())
            alarm.refreshAlarm();
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

        AlarmConfiguration alarm = getAlarm(actualAlarm);
        //save times
        alarm.setHour(hourOfDay);
        alarm.setMinute(minute);
        updateChanges(alarm);

        //refresh Alarm separate only for some Key Values
        if(alarm.isAlarmSet())
            alarm.refreshAlarm();
    }
    /***********************************************************************************************
     * MINUTE SETTING DIALOG
     **********************************************************************************************/
    public void showMinuteSettingDialog(View v) {

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
        builder.setView(createAlertLinearLayout(v, textView, seekBar, 99, 1, getAlarm(actualAlarm).getSnooze() - 1));
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

        //Save Snooze Minutes
        AlarmConfiguration alarm = getAlarm(actualAlarm);
        alarm.setSnooze(minutes + 1); //we Start with 1 minute
        updateChanges(alarm);
    }
    /***********************************************************************************************
     * MUSIC SET DIALOG
     **********************************************************************************************/
    private final Vector<AlertDialog> mDialogs = new Vector<>();
    public void showMusicSettingDialog(View v){
        //Builder List view
        ListView listView = new ListView(this);
        listView.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.song_artist_listgroup,
                R.id.song_artist_groupItem,
                new String[]{this.getString(R.string.wakeup_music_ringtone), this.getString(R.string.wakeup_music_music)}));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchMusic(position);
            }
        });

        //Create and show new Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_song_menu));
        builder.setView(listView);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                stopMusic(true);
            }
        });
        mDialogs.addElement(builder.show());
    }
    private void chooseAlarmArtistDialog( final SongDatabase songs){

        //Get SongNames from SongInformationArray
        final ArrayList<String> artists = new ArrayList<>(Arrays.asList(songs.getArtistStrings()));
        Collections.sort(artists);

        ListView listView = new ListView(this);
        listView.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.song_artist_listgroup,
                R.id.song_artist_groupItem,
                artists));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chooseAlarmAlbumDialog(artists.get(position), songs);
            }
        });

        //Create and show new Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_artist));
        builder.setView(listView);
        mDialogs.addElement(builder.show());
    }
    private void chooseAlarmAlbumDialog( final String artist, final SongDatabase songs){

        //Get SongNames from SongInformationArray
        final ArrayList<String> album = new ArrayList<>(Arrays.asList(songs.getAlbumStrings(artist)));
        Collections.sort(album);

        ListView listView = new ListView(this);
        listView.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.song_artist_listgroup,
                R.id.song_artist_groupItem,
                album));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chooseAlarmSongDialog(songs.getSongs(artist, album.get(position)));
            }
        });

        //Create and show new Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_album));
        builder.setView(listView);
        mDialogs.addElement(builder.show());
    }
    private void chooseAlarmSongDialog(SongInformation[] songs){

        final ArrayList<SongInformation> sortedSongs = new ArrayList<>(Arrays.asList(songs));
        Collections.sort(sortedSongs, new Comparator<SongInformation>() {
            @Override
            public int compare(SongInformation lhs, SongInformation rhs) {
                return (int) (lhs.getID() - rhs.getID());
            }
        });

        ArrayList<String> namedSongs = new ArrayList<>();
        for(SongInformation song : sortedSongs)
            namedSongs.add(song.getTitle());

        //Create new Builder and Get Song Name Array and set it for Alarm Dialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_song)); //TODO
        builder.setItems(namedSongs.toArray(new String[namedSongs.size()]), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                //Get Song Lengh
                saveSongLength(sortedSongs.get(which).getPath());
                for(AlertDialog dia : mDialogs)
                    dia.dismiss();
                dialog.dismiss();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            public void onShow(DialogInterface dialog) {
                
                ListView songsView = alertDialog.getListView();
                songsView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                        AlarmConfiguration alarm = getAlarm(actualAlarm);
                        startMusic(Uri.parse(sortedSongs.get(position).getPath()), true, true, false, alarm.getVolume(), 0);
                        return true;
                    }
                });
            }
        });
        alertDialog.show();
    }
    private void saveSongLength(String uri){

        //Get Song Length
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(uri);

        //Get Values from chosen Song
        AlarmConfiguration alarm = getAlarm(actualAlarm);
        alarm.setSongLength((int) (Long.parseLong(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000));
        alarm.setSongURI(uri);
        updateChanges(alarm);
    }

    private void searchMusic(int modeID){
        searchMusic(modeID, true, true);
    }
    private void searchMusic(int modeID, boolean showToast, boolean startDialog){

        if(modeID != 0)
        {
            //Check if External Media is Present, send Toast if not and return
            if(!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            {
                Toast.makeText(MainActivity.this, R.string.wakeup_music_no_sd_card, Toast.LENGTH_SHORT).show();
                return;
            }

            //Return if we wait for Handler
            if(!checkMusicPermission())
                return;
        }

        //Get All Song Values from the Android Media Content URI
        //Default for Uri is the internal Memory, because it is every time available
        //If the User Chooses the second entry switch to external Files
        ContentResolver musicResolver = getApplicationContext().getContentResolver();
        Cursor cursor = musicResolver.query(
                (modeID == 1)? MediaStore.Audio.Media.EXTERNAL_CONTENT_URI : MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                new String[]{"*"},
                null,
                null,
                null);

        //Search Cursor for Values
        if(cursor != null)
        {
            //ArrayList for Music Entries
            SongDatabase songData = new SongDatabase();
            try {
                if(cursor.moveToFirst())
                {
                    do{
                        int song_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                        String song_name   = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                        String fullPath    = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        String album_name  = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                        String artist_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        songData.addSong(new SongInformation(song_id,  song_name, artist_name, album_name, fullPath));
                        //int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
                        //int album_id   = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                        //int artist_id  = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
                        //int isAlarm    = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_ALARM));
                        //int isRingtone = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_RINGTONE));
                    }
                    while(cursor.moveToNext());
                }
            }
            finally{
                cursor.close();
            }

            //Choose an Alarm
            if(startDialog)
                chooseAlarmArtistDialog(songData);
        }
        else if(showToast)
            Toast.makeText(MainActivity.this, R.string.wakeup_music_no_music, Toast.LENGTH_SHORT).show();
    }

    private void startMusic(final Uri uri, final boolean start, final boolean stop, final boolean looping, final int volume, final int startTime){
        //Set Runnable to play Music
        setRunnable(
                new Runnable()
                {
                    @Override
                    public void run() {

                        if(stop)
                            stopMusic(false);

                        //Check for MediaPlayer
                        if(mediaPlayer == null)
                            mediaPlayer = new MediaPlayer();
                        else
                            mediaPlayer.reset();

                        try
                        {
                            //Set MediaPlayer Values
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(getApplicationContext(), uri);
                            mediaPlayer.prepare();
                        } catch (IOException e) {
                            Log.e("Exception: ", e.getMessage());
                        }

                        mediaPlayer.seekTo((int) TimeUnit.SECONDS.toMillis(startTime));

                        if(start)
                            mediaPlayer.start();

                        if(volume > -1)
                        {
                            float vol = 1 - (float)(Math.log(100-volume)/Math.log(100));
                            mediaPlayer.setVolume(vol, vol);
                        }
                        mediaPlayer.setLooping(looping);
                    }
                }
        );
    }

    private void stopMusic(boolean release){

        //Check for MediaPlayer
        if(mediaPlayer==null)
            return;

        //Stop if Playing
        if(mediaPlayer.isPlaying())
            mediaPlayer.stop();

        //Release and Set null
        if(release)
        {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    private boolean checkMusicPermission(){
        return  checkMusicPermission(true);
    }
    private boolean checkMusicPermission(boolean askPermission){

        if(ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return true; //Granted

        if(askPermission && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE))
        {/*we will jump to the Handler if the user accepts or declines the permission and start there our Dialog*/}
        else if(askPermission)
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, AlarmConstants.ALARM_PERMISSION_MUSIC);

        //We must wait for granting
        return false;
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
     * MUSIC VOLUME DIALOG
     **********************************************************************************************/
    public void showMusicVolumeSettingDialog(View v){

        AlarmConfiguration alarm = getAlarm(actualAlarm);
        startMusic(Uri.parse(alarm.getSongURI()), true, true, true, alarm.getVolume(), alarm.getSongStart());

        //TextView to show Value of SeekBar
        final TextView textView = new TextView(v.getContext());
        //SeekBar
        final SeekBar seekBar = new SeekBar(v.getContext());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Set Position of Text
                final int val = getSeekBarPosition(progress, 4, seekBar.getWidth(), seekBar.getThumbOffset(), seekBar.getMax());
                textView.setText(String.format(Locale.US, "%d", progress));
                textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);

                if(mediaPlayer != null && mediaPlayer.isPlaying())
                {
                    final float volume = 1 - (float)(Math.log(100-progress)/Math.log(100));
                    mediaPlayer.setVolume(volume, volume);
                }
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
        builder.setView(createAlertLinearLayout(v, textView, seekBar, 100, 1, alarm.getVolume()));
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
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                stopMusic(true);
            }
        });
        builder.show();
    }
    private void onMusicVolumeSet(int volume){

        //Set ActualVolume and save Settings
        AlarmConfiguration alarm = getAlarm(actualAlarm);
        alarm.setVolume(volume);
        updateChanges(alarm);
    }
    /***********************************************************************************************
     * MUSIC START TIME DIALOG
     **********************************************************************************************/
    public void showMusicStartSettingDialog(View v){

        final AlarmConfiguration alarm = getAlarm(actualAlarm);
        startMusic(Uri.parse(alarm.getSongURI()), true, true, true, alarm.getVolume(), alarm.getSongStart());

        //TextView to show Value of SeekBar
        final TextView textView = new TextView(v.getContext());
        //SeekBar
        final SeekBar seekBar = new SeekBar(v.getContext());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Set Position of Text
                final int val = getSeekBarPosition(progress, 5, seekBar.getWidth(), seekBar.getThumbOffset(), seekBar.getMax());
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
                if(mediaPlayer != null && mediaPlayer.isPlaying())
                    mediaPlayer.seekTo((int) TimeUnit.SECONDS.toMillis(seekBar.getProgress()));
            }
        });

        //Create new Builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle(this.getString(R.string.wakeup_set_alarm_song_Start));
        builder.setView(createAlertLinearLayout( v, textView, seekBar, alarm.getSongLength(), 1, alarm.getSongStart()));
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
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                stopMusic(true);
            }
        });
        builder.show();
    }
    private void onMusicStartSet(int seconds){

        //Set ActualStart save Settings
        AlarmConfiguration alarm = getAlarm(actualAlarm);
        alarm.setSongStart(seconds);
        updateChanges(alarm);
    }
    /***********************************************************************************************
     * MUSIC FADEIN TIME DIALOG
     **********************************************************************************************/
    public void showFadeInSettingsDialog(View v){

        //GEt ToggleButton and Set On LongClickListener
        final ToggleButton fadeInToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_music_toggleFadeIn);
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
                                getAlarm(actualAlarm).getSongLength(),
                            1,
                                getAlarm(actualAlarm).getSongStart()));
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
                builder.show();
                return false;
            }
        });

        //Set Vibration Checked
        AlarmConfiguration alarm = getAlarm(actualAlarm);
        alarm.setFadeIn(fadeInToggle.isChecked());
        updateChanges(alarm,false);
    }
    private void onFadeInTimeSet(int seconds){

        //Save Snooze Minutes
        AlarmConfiguration alarm = getAlarm(actualAlarm);
        alarm.setFadeInTime(seconds);
        alarm.setFadeIn(true);
        updateChanges(alarm);
    }
    /***********************************************************************************************
     * MUSIC VIBRATION DIALOG
     **********************************************************************************************/
    public void showVibrationSettingDialog(View v){

        //Get ToggleButton and Set On LongClickListener
        final ToggleButton vibrationToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_music_toggleVibration);
        vibrationToggle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //TODO Let it vibrate
                //TextView to show Value of SeekBar
                final TextView textView = new TextView(v.getContext());
                //SeekBar
                final SeekBar seekBar = new SeekBar(v.getContext());
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar Bar, int progress, boolean fromUser) {

                        //Set Position of Text
                        int val = getSeekBarPosition(progress, 4, Bar.getWidth(), Bar.getThumbOffset(), Bar.getMax());
                        String message = Integer.toString(progress) + "%";
                        textView.setText(message);
                        textView.setX(Bar.getX() + val + Bar.getThumbOffset() / 2);
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        textView.setVisibility(TextView.VISIBLE);
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //textView.setVisibility(TextView.GONE);
                        setVibrationStart(seekBar.getProgress());
                    }
                });


                //Create new Builder
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(v.getContext().getString(R.string.wakeup_set_alarm_song_vibration));
                builder.setView(createAlertLinearLayout(v, textView, seekBar, 100, 1, getAlarm(actualAlarm).getVibrationStrength()));
                builder.setPositiveButton(v.getContext().getString(R.string.wakeup_OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Set and Save Vibration Strength
                        onVibrationStrengthSet(seekBar.getProgress());
                        vibrationToggle.setChecked(true);
                        setVibrationStop();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(v.getContext().getString(R.string.wakeup_Cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setVibrationStop();
                        dialog.dismiss();
                    }
                });

                builder.show();
                setVibrationStart(getAlarm(actualAlarm).getVibrationStrength());
                return false;
            }
        });

        //Set Vibration Checked
        AlarmConfiguration alarm = getAlarm(actualAlarm);
        alarm.setVibration(vibrationToggle.isChecked());
        updateChanges(alarm, false);
    }
    private void onVibrationStrengthSet(int strength){

        //Save Vibration Values and save Settings
        AlarmConfiguration alarm = getAlarm(actualAlarm);
        alarm.setVibrationStrength(strength);
        alarm.setVibration(true);
        updateChanges(alarm);
    }

    private void setVibrationStart(final int _intensity){

        setRunnable(new Runnable() {
            @Override
            public void run() {
                //Start without delay,
                //Vibrate fpr milliseconds
                //Sleep for milliseconds
                if(m_Vibrator == null)
                    m_Vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                m_Vibrator.vibrate(new long[]{0, 8 * _intensity + 350, (4000 / (long) Math.sqrt(_intensity + 1))}, 0);
            }
        });
    }
    private void setVibrationStop(){

        //Cancel and Release Vibrator
        if(m_Vibrator != null)
        {
            m_Vibrator.cancel();
            m_Vibrator = null;
        }
    }
    /***********************************************************************************************
     * SCEEN LIGHT SETTING DIALOG
     **********************************************************************************************/
    public void showScreenLightSettingDialog(View v){
        //GEt ToggleButton and Set On LongClickListener
        final ToggleButton screenToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_light_buttonLight);
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
                        final int val = getSeekBarPosition(progress, 4, seekBar.getWidth(), seekBar.getThumbOffset(), seekBar.getMax());
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
                builder.setView(createAlertLinearLayout(v, textView, seekBar, 99, 1, getAlarm(actualAlarm).getScreenBrightness() - 1)); //We must -1 because we dont want to have zero brightness
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

        //Set Screen Checked
        AlarmConfiguration alarm = getAlarm(actualAlarm);
        alarm.setScreen(screenToggle.isChecked());
        updateChanges(alarm, false);
    }
    private void onScreenBrightnessSet(int brightness){

        //set Brightness and save Settings
        AlarmConfiguration alarm = getAlarm(actualAlarm);
        alarm.setScreenBrightness(brightness);
        updateChanges(alarm);
    }

    public void showScreenLightStartSettingDialog(View v){

        //TextView to show Value of SeekBar
        final TextView textView = new TextView(v.getContext());

        //Seek Bar
        final SeekBar seekBar = new SeekBar(v.getContext());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Set Position of Text
                final int val = getSeekBarPosition(progress, 6, seekBar.getWidth(), seekBar.getThumbOffset(), seekBar.getMax());
                final String message = Integer.toString(progress) + "min";
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
        builder.setView(createAlertLinearLayout(v, textView, seekBar, 100, 1, getAlarm(actualAlarm).getScreenStartTime()));
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

        //Save ScreenStart Minutes and save Settings
        AlarmConfiguration alarm = getAlarm(actualAlarm);
        alarm.setScreenStartTime(minutes);  //we Start with 1 minute
        updateChanges(alarm);

        //refresh Alarm separate only for some Key Values
        if(alarm.isAlarmSet())
            alarm.refreshAlarm();
    }
    /***********************************************************************************************
     * SCREEN COLOR SETTING DIALOG
     **********************************************************************************************/
    public void showScreenColor1SettingDialog(View v){

        //TODO Need a better ColorPicker without 0xfffff bug
        final Button bColor = (Button) v;
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

        final String color1 = this.getString(R.string.wakeup_light_screen_color1);
        final String color2 = this.getString(R.string.wakeup_light_screen_color2);

        AlarmConfiguration alarm = getAlarm(actualAlarm);
        if(color1.equals(bView.getText().toString()))
            alarm.setLightColor1(color);
        else if(color2.equals(bView.getText().toString()))
            alarm.setLightColor2(color);
        else debug_assertion(true);

        //save Settings
        updateChanges(alarm);
    }
    /***********************************************************************************************
     * SCREEN COLOR FADE SETTING DIALOG
     **********************************************************************************************/
    public void showScreenColorFadeSettingDialog(View v){

        //GEt ToggleButton
        final ToggleButton screenFadeToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_light_buttonScreenFade);

        //Set Vibration Checked
        AlarmConfiguration alarm = getAlarm(actualAlarm);
        alarm.setLightFade(screenFadeToggle.isChecked());
        updateChanges(alarm);
    }
    /***********************************************************************************************
     * LED LIGHT SETTING DIALOG
     **********************************************************************************************/
    public void showLEDLightSettingDialog(View v){

        //GEt ToggleButton
        final ToggleButton LEDToggle = (ToggleButton) v.findViewById(R.id.wakeup_timer_light_buttonLED);

        //Set LED Checked and save Settings
        AlarmConfiguration alarm = getAlarm(actualAlarm);
        alarm.setLED(LEDToggle.isChecked());
        updateChanges(alarm);
    }
    public void showLEDLightStartSettingDialog(View v){

        //TextView to show Value of SeekBar
        final TextView textView = new TextView(v.getContext());

        //Seek Bar
        final SeekBar seekBar = new SeekBar(v.getContext());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Set Position of Text
                final int val = getSeekBarPosition(progress, 6, seekBar.getWidth(), seekBar.getThumbOffset(), seekBar.getMax());
                final String message = Integer.toString(progress) + "min";
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
        builder.setView(createAlertLinearLayout(v, textView, seekBar, 100, 1, getAlarm(actualAlarm).getLEDStartTime()));
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
        //Save LEDSTartTime Minutes save Settings
        AlarmConfiguration alarm = getAlarm(actualAlarm);
        alarm.setLEDStartTime(minutes);
        updateChanges(alarm);

        //refresh Alarm separate only for some Key Values
        if(alarm.isAlarmSet())
            alarm.refreshAlarm();
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
