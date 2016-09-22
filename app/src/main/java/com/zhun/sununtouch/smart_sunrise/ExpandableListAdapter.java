package com.zhun.sununtouch.smart_sunrise;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private final static String[] WAKEUP_CHILDS  = {AlarmConstants.WAKEUP_DELETE, AlarmConstants.WAKEUP_TIME, AlarmConstants.WAKEUP_DAYS, AlarmConstants.WAKEUP_MUSIC, AlarmConstants.WAKEUP_LIGHT};

    private Context      context;
    private LinkedHashMap<Integer, AlarmConfiguration> configuration = new LinkedHashMap<>();

    public ExpandableListAdapter(Context _context, LinkedHashMap<Integer, AlarmConfiguration> config){
        this.context       = _context;
        this.configuration = config;
    }

    public void notifyDataSetChanged(LinkedHashMap<Integer, AlarmConfiguration> config) {
        this.configuration = config;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    private View createTimeView(AlarmConfiguration config){

        //Create new Layout Inflater
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View _convertView = inflater.inflate(R.layout.wakeup_timer_listitem_time, null);

        //Get TextChild from View
        TextView txtListChild = (TextView) _convertView.findViewById(R.id.wakeup_timer_time_textview);
        txtListChild.setText(_convertView.getContext().getString(R.string.wakeup_time));

        Button setTimeButton = (Button) _convertView.findViewById(R.id.wakeup_timer_time_buttonTime);

        String timeText = String.format("%02d:%02d".toLowerCase(),config.getHour(), config.getMinute());
        setTimeButton.setText(timeText);

        Button setSnoozeButton = (Button) _convertView.findViewById(R.id.wakeup_timer_time_buttonSnooze);
        String snoozeText = _convertView.getContext().getString(R.string.wakeup_time_snooze) + " " +
                config.getSnooze() + " " +
                _convertView.getContext().getString(R.string.wakeup_time_minutes);

        setSnoozeButton.setText(snoozeText);

        return _convertView;
    }
    private View createDayView(AlarmConfiguration config){

        //Create new Layout Inflater
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View _convertView = inflater.inflate(R.layout.wakeup_timer_listitem_days, null);

        //Get TextChild from View
        TextView txtListChild = (TextView) _convertView.findViewById(R.id.wakeup_timer_days_textview);
        txtListChild.setText(_convertView.getContext().getString(R.string.wakeup_day));

        ToggleButton setMonday    = (ToggleButton) _convertView.findViewById(R.id.wakeup_monday);
        ToggleButton setTuesday   = (ToggleButton) _convertView.findViewById(R.id.wakeup_tuesday);
        ToggleButton setWednesday = (ToggleButton) _convertView.findViewById(R.id.wakeup_wednesday);
        ToggleButton setThursday  = (ToggleButton) _convertView.findViewById(R.id.wakeup_thursday);
        ToggleButton setFriday    = (ToggleButton) _convertView.findViewById(R.id.wakeup_friday);
        ToggleButton setSaturday  = (ToggleButton) _convertView.findViewById(R.id.wakeup_saturday);
        ToggleButton setSunday    = (ToggleButton) _convertView.findViewById(R.id.wakeup_sunday);

        setMonday   .setChecked(config.isMonday(true));
        setTuesday  .setChecked(config.isTuesday(true));
        setWednesday.setChecked(config.isWednesday(true));
        setThursday .setChecked(config.isThursday(true));
        setFriday   .setChecked(config.isFriday(true));
        setSaturday .setChecked(config.isSaturday(true));
        setSunday   .setChecked(config.isSunday(true));

        return _convertView;
    }
    private View createMusicView(AlarmConfiguration config){

        //Create new Layout Inflater
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View _convertView = inflater.inflate(R.layout.wakeup_timer_listitem_music, null);

        //Get TextChild from View
        TextView txtListChild = (TextView) _convertView.findViewById(R.id.wakeup_timer_music_textview);
        txtListChild.setText(_convertView.getContext().getString(R.string.wakeup_music));

        //Choosing Music Button
        String newMusicURI  = config.getSongURI();
        String newMusicText = newMusicURI.substring(newMusicURI.lastIndexOf('/') + 1);
        if(newMusicText.indexOf('.') != -1)
            newMusicText = newMusicText.substring(0, newMusicText.lastIndexOf('.'));

        Button setMusicButton = (Button) _convertView.findViewById(R.id.wakeup_timer_music_buttonMusic);
        setMusicButton.setText(newMusicText);

        //Set Volume Button
        Button setMusicVolumeButton = (Button) _convertView.findViewById(R.id.wakeup_timer_music_buttonMusicVolume);
        String musicVolumeText = config.getVolume() + "%";
        setMusicVolumeButton.setText(musicVolumeText);

        //Set Start Time Button
        Button setMusicStartTime = (Button) _convertView.findViewById(R.id.wakeup_timer_music_SongStart);
        int seconds = config.getSongStart();
        String startTimeText = String.format(
                "%02d:%02d".toLowerCase(),
                TimeUnit.SECONDS.toMinutes(seconds),
                seconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)));
        setMusicStartTime.setText(startTimeText);

        //Set FadeIn ToggleButton
        ToggleButton setFadeIn = (ToggleButton) _convertView.findViewById(R.id.wakeup_timer_music_toggleFadeIn);
        int fadeSeconds  = config.getFadeIn();
        String fadeTimeText = String.format(
                "%02d:%02d".toLowerCase(),
                TimeUnit.SECONDS.toMinutes(fadeSeconds),
                fadeSeconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(fadeSeconds)));

        setFadeIn.setTextOn(fadeTimeText);
        setFadeIn.setTextOff(_convertView.getContext().getString(R.string.wakeup_music_fadeOff));

        boolean fadeChecked = config.getFadeIn() == 1;
        setFadeIn.setChecked(fadeChecked);

        //Set Vibration ToggleButton
        ToggleButton setVibrationButton = (ToggleButton) _convertView.findViewById(R.id.wakeup_timer_music_toggleVibration);
        String vibrationTextOn       = config.getVibrationStrength() + "%";
        String vibrationTextOff      = _convertView.getContext().getString(R.string.wakeup_music_vibraOff);

        setVibrationButton.setTextOn(vibrationTextOn);
        setVibrationButton.setTextOff(vibrationTextOff);

        boolean vibraChecked = config.getVibration() == 1;
        setVibrationButton.setChecked(vibraChecked);

        return _convertView;
    }
    private View createLightView(AlarmConfiguration config){

        //Create new Layout Inflater
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View _convertView = inflater.inflate(R.layout.wakeup_timer_listitem_light, null);

        //Get TextChild from View
        TextView txtListChild = (TextView) _convertView.findViewById(R.id.wakeup_timer_light_textview);
        txtListChild.setText(_convertView.getContext().getString(R.string.wakeup_light));

        //Toggle Screen light
        ToggleButton setLightButton = (ToggleButton) _convertView.findViewById(R.id.wakeup_timer_light_buttonLight);
        String screenOn =  _convertView.getContext().getString(R.string.wakeup_light_screen_brightness) + " " +
                config.getScreenBrightness() + "%";
        String screenOff = _convertView.getContext().getString(R.string.wakeup_light_screen_brightness_off);

        setLightButton.setTextOn(screenOn);
        setLightButton.setTextOff(screenOff);

        boolean screenChecked = config.getScreen() == 1;
        setLightButton.setChecked(screenChecked);

        //Set Start Time Button
        Button setStartTime = (Button) _convertView.findViewById(R.id.wakeup_timer_light_buttonStart);
        String startTimeText= config.getScreenStartTime() + " " +
                _convertView.getContext().getString(R.string.wakeup_time_minutes);
        setStartTime.setText(startTimeText);

        //First Color
        Button setColorButton1 = (Button) _convertView.findViewById(R.id.wakeup_timer_light_buttonColor1);
        String colorText      = _convertView.getContext().getString(R.string.wakeup_light_screen_color1);
        setColorButton1.setText(colorText);
        setColorButton1.getBackground().setColorFilter(config.getLightColor1(), PorterDuff.Mode.MULTIPLY);

        //Second Color
        Button setColorButton2 = (Button) _convertView.findViewById(R.id.wakeup_timer_light_buttonColor2);
        String colorText2      = _convertView.getContext().getString(R.string.wakeup_light_screen_color2);
        setColorButton2.setText(colorText2);
        setColorButton2.getBackground().setColorFilter(config.getLightColor2(), PorterDuff.Mode.MULTIPLY);

        //Toggle Fading
        ToggleButton setFade = (ToggleButton) _convertView.findViewById(R.id.wakeup_timer_light_buttonScreenFade);
        String colorFadeTextOn = _convertView.getContext().getString(R.string.wakeup_light_screen_fadingOn);
        String colorFadeTextOff = _convertView.getContext().getString(R.string.wakeup_light_screen_fadingOff);
        setFade.setTextOn(colorFadeTextOn);
        setFade.setTextOff(colorFadeTextOff);

        boolean FadeChecked = config.getLightFade() == 1;
        setFade.setChecked(FadeChecked);

        //Toggle LED
        ToggleButton setLEDButton = (ToggleButton) _convertView.findViewById(R.id.wakeup_timer_light_buttonLED);
        String timeTextOn = _convertView.getContext().getString(R.string.wakeup_light_screen_LEDOn);
        String timeTextOff = _convertView.getContext().getString(R.string.wakeup_light_screen_LEDOff);
        setLEDButton.setTextOn(timeTextOn);
        setLEDButton.setTextOff(timeTextOff);

        boolean LEDChecked = config.getLED() == 1;
        setLEDButton.setChecked(LEDChecked);

        //Set LED Start Time
        Button setLEDStartTime = (Button) _convertView.findViewById(R.id.wakeup_timer_light_buttonLEDStart);
        String startTimeLEDText= config.getLEDStartTime() + " " +
                _convertView.getContext().getString(R.string.wakeup_time_minutes);
        setLEDStartTime.setText(startTimeLEDText);

        return _convertView;
    }
    private View createDeleteView(int _groupPosition){

        //Create new Layout Inflater
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View _convertView = inflater.inflate(R.layout.wakeup_timer_listitem_delete, null);

        //Get TextChild from View
        TextView txtListChild = (TextView) _convertView.findViewById(R.id.wakeup_timer_delete_textview);
        //txtListChild.setText(_childText);
        txtListChild.setVisibility(TextView.GONE);

        Button deleteAlarm = (Button) _convertView.findViewById(R.id.wakeup_timer_deleteButton);
        String deleteText = _convertView.getContext().getString(R.string.wakeup_delete);
        deleteAlarm.setText(deleteText);

        //Check for Alarm and Set Button to boolean value
        AlarmManage newAlarm = new AlarmManage(_convertView.getContext());
        boolean checked =newAlarm.checkForPendingIntent(_groupPosition);

        ToggleButton setNewAlarm = (ToggleButton) _convertView.findViewById(R.id.wakeup_timer_setAlarmButton);
        setNewAlarm.setChecked(checked);

        return _convertView;
    }

    //Childs/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return  this.configuration.get(groupPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        AlarmConfiguration config = (AlarmConfiguration)getGroup(groupPosition);
        switch (AlarmConfiguration.childItem.values()[childPosition])
        {
            case WAKEUP_TIME  : return createTimeView  (config);
            case WAKEUP_DAYS  : return createDayView   (config);
            case WAKEUP_MUSIC : return createMusicView (config);
            case WAKEUP_LIGHT : return createLightView (config);
            case WAKEUP_DELETE: return createDeleteView(groupPosition);
            default: return null;
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.configuration.get(groupPosition).getChildItemSize(); //TODO thats not right, we need a size of childrens... but for now we never use this... so...
    }

    //Groups/////////////////////////////////////////////////////////////////////////
    @Override
    public Object getGroup(int groupPosition) {
        return this.configuration.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.configuration.size();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        AlarmConfiguration config = (AlarmConfiguration) getGroup(groupPosition);

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.wakeup_timer_listgroup, null);
        }

        //Set Group Title
        TextView txtListHeader = (TextView) convertView.findViewById(R.id.wakeup_timer_groupItem);

        //txtListHeader.setTypeface(null, Typeface.BOLD);
        txtListHeader.setText(config.getAlarmName());

        if(isExpanded)
            txtListHeader.setClickable(true);
        else
            txtListHeader.setClickable(false);

        //time Text
        String timeText = String.format("%02d:%02d".toLowerCase(),config.getHour(), config.getMinute());

        TextView txtListTime = (TextView) convertView.findViewById(R.id.wakeup_group_time);
        txtListTime.setText(timeText);

        //Day Text
        String days = String.format("%s %s %s %s %s %s %s",
                (config.isMonday(true))    ? convertView.getContext().getString(R.string.wakeup_day_monday_short)    : "",
                (config.isTuesday(true))   ? convertView.getContext().getString(R.string.wakeup_day_tuesday_short)   : "",
                (config.isWednesday(true)) ? convertView.getContext().getString(R.string.wakeup_day_wednesday_short) : "",
                (config.isThursday(true))  ? convertView.getContext().getString(R.string.wakeup_day_thursday_short)  : "",
                (config.isFriday(true))    ? convertView.getContext().getString(R.string.wakeup_day_friday_short)    : "",
                (config.isSaturday(true))  ? convertView.getContext().getString(R.string.wakeup_day_saturday_short)  : "",
                (config.isSunday(true))    ? convertView.getContext().getString(R.string.wakeup_day_sunday_short)    : "");

        TextView txtListDays = (TextView) convertView.findViewById(R.id.wakeup_group_days);
        txtListDays.setText(days);
        return convertView;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
