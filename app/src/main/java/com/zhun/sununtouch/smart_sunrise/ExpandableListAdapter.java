package com.zhun.sununtouch.smart_sunrise;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sunny
 * Expandable Lis6 View to Set Up Alarm values
 */

class ExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private AlarmConfigurationList configuration;

    /***********************************************************************************************
     * HELPER
     **********************************************************************************************/
    ExpandableListAdapter(Context context, AlarmConfigurationList alarms){
        this.context       = context;
        this.configuration = alarms;
    }
    void notifyDataSetChanged(AlarmConfigurationList config) {
        this.configuration = config;
        super.notifyDataSetChanged();
    }

    /***********************************************************************************************
     * VIEWS
     **********************************************************************************************/
    private View createTimeView(ViewGroup v, int ID){

        final AlarmConfiguration config = configuration.getAlarm(ID);

        //Create new Layout Inflater
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.wakeup_timer_listitem_time, v, false);

        //Get TextChild from View
        //TextView txtListChild = (TextView) view.findViewById(R.id.wakeup_timer_time_text_view);

        Button setTimeButton = (Button) view.findViewById(R.id.wakeup_timer_time_buttonTime);
        setTimeButton.setText(String.format(Locale.US, "%02d:%02d",config.getHour(), config.getMinute()));

        Button setSnoozeButton = (Button) view.findViewById(R.id.wakeup_timer_time_buttonSnooze);
        setSnoozeButton.setText(view.getContext().getResources().getQuantityString(R.plurals.wakeup_time_snooze, config.getSnooze(), config.getSnooze()));

        return view;
    }
    private View createDayView(ViewGroup v,int ID){

        final AlarmConfiguration config = configuration.getAlarm(ID);

        //Create new Layout Inflater
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.wakeup_timer_listitem_days, v, false);

        //Get TextChild from View
        TextView txtListChild = (TextView) view.findViewById(R.id.wakeup_timer_days_text_view);
        txtListChild.setText(view.getContext().getString(R.string.wakeup_day));
        createButton(view, R.id.wakeup_monday   , config.getDayName(Calendar.MONDAY   , false), config.isMonday());
        createButton(view, R.id.wakeup_tuesday  , config.getDayName(Calendar.TUESDAY  , false), config.isTuesday());
        createButton(view, R.id.wakeup_wednesday, config.getDayName(Calendar.WEDNESDAY, false), config.isWednesday());
        createButton(view, R.id.wakeup_thursday , config.getDayName(Calendar.THURSDAY , false), config.isThursday());
        createButton(view, R.id.wakeup_friday   , config.getDayName(Calendar.FRIDAY   , false), config.isFriday());
        createButton(view, R.id.wakeup_saturday , config.getDayName(Calendar.SATURDAY , false), config.isSaturday());
        createButton(view, R.id.wakeup_sunday   , config.getDayName(Calendar.SUNDAY   , false), config.isSunday());
        return view;
    }
    @SuppressWarnings("UnusedReturnValue")
    private ToggleButton createButton(View view, int id, String text, boolean checked){
        ToggleButton button = (ToggleButton) view.findViewById(id);
        button.setTextOn(text);
        button.setTextOff(text);
        button.setChecked(checked);
        return button;
    }

    private View createMusicView(ViewGroup v, int ID){

        final AlarmConfiguration config = configuration.getAlarm(ID);

        //Create new Layout Inflater
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.wakeup_timer_listitem_music, v, false);

        //Get TextChild from View
        //TextView txtListChild = (TextView) view.findViewById(R.id.wakeup_timer_music_text_view);

        //Choosing Music Button
        final String newMusicText = (config.getSongName().indexOf('.') == -1) ?  config.getSongName() : config.getSongName().substring(0, config.getSongName().lastIndexOf('.'));
        Button setMusicButton = (Button) view.findViewById(R.id.wakeup_timer_music_buttonMusic);
        setMusicButton.setText(newMusicText);

        //Set Volume Button
        Button setMusicVolumeButton = (Button) view.findViewById(R.id.wakeup_timer_music_buttonMusicVolume);
        setMusicVolumeButton.setText(String.format(Locale.US, "%d%s", config.getVolume(), "%"));

        //Set Start Time Button
        long seconds  = config.getSongStart();
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        seconds -= TimeUnit.MINUTES.toSeconds(minutes);
        Button setMusicStartTime = (Button) view.findViewById(R.id.wakeup_timer_music_SongStart);
        setMusicStartTime.setText(String.format(Locale.US,"%02d:%02d", minutes, seconds));

        //Set FadeIn ToggleButton
        seconds  = config.getFadeInTime();
        minutes  = TimeUnit.SECONDS.toMinutes(seconds);
        seconds -= TimeUnit.MINUTES.toSeconds(minutes);
        ToggleButton setFadeIn = (ToggleButton) view.findViewById(R.id.wakeup_timer_music_toggleFadeIn);
        setFadeIn.setTextOn(String.format(Locale.US, "%02d:%02d", minutes, seconds));
        setFadeIn.setChecked(config.getFadeIn());

        //Set Vibration ToggleButton
        ToggleButton setVibrationButton = (ToggleButton) view.findViewById(R.id.wakeup_timer_music_toggleVibration);
        setVibrationButton.setTextOn(config.getVibrationStrength() + "%");
        setVibrationButton.setChecked(config.getVibration());
        return view;
    }
    private View createLightView(ViewGroup v, int ID){

        final AlarmConfiguration config = configuration.getAlarm(ID);

        //Create new Layout Inflater
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.wakeup_timer_listitem_light, v, false);

        //Get TextChild from View
        //TextView txtListChild = (TextView) view.findViewById(R.id.wakeup_timer_light_text_view);

        //Toggle Screen light
        ToggleButton setLightButton = (ToggleButton) view.findViewById(R.id.wakeup_timer_light_buttonLight);
        setLightButton.setTextOn(view.getContext().getString( R.string.wakeup_light_screen_brightness) + " " +  config.getScreenBrightness() + "%");
        setLightButton.setChecked(config.getScreen());

        //Set Start Time Button
        Button setStartTime = (Button) view.findViewById(R.id.wakeup_timer_light_buttonStart);
        setStartTime.setText(view.getContext().getResources().getQuantityString(R.plurals.wakeup_time_minutes, config.getScreenStartTime(), config.getScreenStartTime()));

        //Toggle Fading
        ToggleButton setFade = (ToggleButton) view.findViewById(R.id.wakeup_timer_light_buttonScreenFade);
        setFade.setChecked(config.getLightFade());

        //First Color
        Button setColorButton1 = (Button) view.findViewById(R.id.wakeup_timer_light_buttonColor1);
        setColorButton1.getBackground().setColorFilter(config.getLightColor1(), PorterDuff.Mode.MULTIPLY);

        //Second Color
        Button setColorButton2 = (Button) view.findViewById(R.id.wakeup_timer_light_buttonColor2);
        setColorButton2.setEnabled(config.getLightFade());

        //Gradient View
        View gradient = view.findViewById(R.id.wakeup_timer_light_gradient);
        if(config.getLightFade())
        {
            //Set Color Filter to second Button
            setColorButton2.getBackground().setColorFilter(config.getLightColor2(), PorterDuff.Mode.MULTIPLY);

            //Get ViewGradient
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[] {config.getLightColor1(),config.getLightColor2()});
            gd.setCornerRadius(0f);
            gradient.setBackground(gd);
        }
        else{
            gradient.setBackgroundColor(Color.WHITE);
            gradient.getBackground().setColorFilter(config.getLightColor1(), PorterDuff.Mode.MULTIPLY);
        }

        //Toggle LED
        ToggleButton setLEDButton = (ToggleButton) view.findViewById(R.id.wakeup_timer_light_buttonLED);
        setLEDButton.setChecked(config.getLED());

        //Set LED Start Time
        Button setLEDStartTime = (Button) view.findViewById(R.id.wakeup_timer_light_buttonLEDStart);
        setLEDStartTime.setText(view.getContext().getResources().getQuantityString(R.plurals.wakeup_time_minutes, config.getLEDStartTime(), config.getLEDStartTime()));

        return view;
    }
    private View createDeleteView(ViewGroup v, int ID){

        final AlarmConfiguration config = configuration.getAlarm(ID);

        //Create new Layout Inflater
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.wakeup_timer_listitem_delete, v, false);

        //Get TextChild from View
        TextView txtListChild = (TextView) view.findViewById(R.id.wakeup_timer_delete_text_view);
        txtListChild.setVisibility(TextView.GONE);

        //Button deleteAlarm = (Button) view.findViewById(R.id.wakeup_timer_deleteButton);

        //Check for Alarm and Set Button to boolean value
        ToggleButton setNewAlarm = (ToggleButton) view.findViewById(R.id.wakeup_timer_setAlarmButton);
        setNewAlarm.setChecked(config.isAlarmSet());

        return view;
    }

    /***********************************************************************************************
     * GROUPS
     **********************************************************************************************/
    @Override
    public int getGroupCount() {
        return this.configuration.size();
    }
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
    @Override
    public boolean hasStableIds() {
        return false;
    }
    @Override
    public Object getGroup(int groupPosition) {
        return groupPosition;
    }
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.wakeup_timer_listgroup, parent, false);
        }

        final AlarmConfiguration config = configuration.getAlarm(groupPosition);

        //Set Group Title
        TextView txtListHeader = (TextView) convertView.findViewById(R.id.wakeup_timer_groupItem);
        txtListHeader.setText(config.getAlarmName());
        txtListHeader.setClickable(isExpanded);

        //time Text
        TextView txtListTime = (TextView) convertView.findViewById(R.id.wakeup_group_time);
        txtListTime.setText(String.format(Locale.US, "%02d:%02d",config.getHour(), config.getMinute()));

        //Day Text
        TextView txtListDays = (TextView) convertView.findViewById(R.id.wakeup_group_days);
        txtListDays.setText(String.format("%s %s %s %s %s %s %s",
                (config.isMonday())    ? config.getDayName(Calendar.MONDAY   , false) : "",
                (config.isTuesday())   ? config.getDayName(Calendar.TUESDAY  , false) : "",
                (config.isWednesday()) ? config.getDayName(Calendar.WEDNESDAY, false) : "",
                (config.isThursday())  ? config.getDayName(Calendar.THURSDAY , false) : "",
                (config.isFriday())    ? config.getDayName(Calendar.FRIDAY   , false) : "",
                (config.isSaturday())  ? config.getDayName(Calendar.SATURDAY , false) : "",
                (config.isSunday())    ? config.getDayName(Calendar.SUNDAY   , false) : ""));
        return convertView;
    }
    /***********************************************************************************************
     * CHILD
     **********************************************************************************************/
    @Override
    public int getChildrenCount(int groupPosition) {
        return this.configuration.getAlarm(groupPosition).getChildItemSize();
    }
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return  AlarmConfiguration.childItem.values()[childPosition];
    }
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        switch ((AlarmConfiguration.childItem) getChild(groupPosition, childPosition))
        {
            case WAKEUP_TIME  : return createTimeView  (parent, groupPosition);
            case WAKEUP_DAYS  : return createDayView   (parent, groupPosition);
            case WAKEUP_MUSIC : return createMusicView (parent, groupPosition);
            case WAKEUP_LIGHT : return createLightView (parent, groupPosition);
            case WAKEUP_DELETE: return createDeleteView(parent, groupPosition);
            default: return null;
        }
    }
}
