package com.zhun.sununtouch.smart_sunrise;

import android.content.Context;
import android.database.DataSetObserver;
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
import java.util.concurrent.TimeUnit;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    public final static String WAKEUP_DAYS  = "Days";
    public final static String WAKEUP_TIME  = "Time";
    public final static String WAKEUP_MUSIC = "Music";
    public final static String WAKEUP_LIGHT = "Light";
    public final static String WAKEUP_DELETE= "Delete";

    public final static String[] WAKEUP_CHILDS  = {WAKEUP_DELETE, WAKEUP_TIME, WAKEUP_DAYS, WAKEUP_MUSIC, WAKEUP_LIGHT};

    private Context      context;
    private List<String> wakeup_header;
    private List<String> wakeup_alarm;
    private LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>> wakeup_child;

    public ExpandableListAdapter(Context _context, List<String> _wakeup_alarm, List<String> _wakeup_header, LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>> _wakeup_child){
        this.context       = _context;
        this.wakeup_header = _wakeup_header;
        this.wakeup_alarm  = _wakeup_alarm;
        this.wakeup_child  = _wakeup_child;
    }

    public void notifyDataSetChanged(List<String> _wakeup_alarm, List<String> _wakeup_header, LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>> _wakeup_child) {
        this.wakeup_header = _wakeup_header;
        this.wakeup_alarm  = _wakeup_alarm;
        this.wakeup_child  = _wakeup_child;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    private View inflateLayout(View _convertView, int _layoutID, String _childText, LinkedHashMap<String, Integer> _childValues, int[] _viewID){

        //Create new Layout Inflater
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _convertView = inflater.inflate(_layoutID, null);

        //Get TextChild from View
        TextView txtListChild = (TextView) _convertView.findViewById(_viewID[0]);
        txtListChild.setText(_childText);

        //Switch Behaviour for every Setting View
        switch(_childText){
            case WAKEUP_TIME:{

                Button setTimeButton = (Button) _convertView.findViewById(_viewID[1]);
                String timeText = String.format("%02d",_childValues.get("Hour")) + ":" + String.format("%02d", _childValues.get("Minute"));
                setTimeButton.setText(timeText);

                Button setSnoozeButton = (Button) _convertView.findViewById(_viewID[2]);
                String snoozeText      = "SNOOZE: " + _childValues.get("Snooze") + " Minutes";
                setSnoozeButton.setText(snoozeText);
            }
            break;
            case WAKEUP_DAYS:{
                ToggleButton setMonday = (ToggleButton) _convertView.findViewById(_viewID[1]);
                setMonday.setChecked((_childValues.get("Monday") > 0) ? true : false);

                ToggleButton setTuesday = (ToggleButton) _convertView.findViewById(_viewID[2]);
                setTuesday.setChecked((_childValues.get("Tuesday") > 0) ? true : false);

                ToggleButton setWednesday = (ToggleButton) _convertView.findViewById(_viewID[3]);
                setWednesday.setChecked((_childValues.get("Wednesday") > 0) ? true : false);

                ToggleButton setThursday = (ToggleButton) _convertView.findViewById(_viewID[4]);
                setThursday.setChecked((_childValues.get("Thursday") > 0) ? true : false);

                ToggleButton setFriday = (ToggleButton) _convertView.findViewById(_viewID[5]);
                setFriday.setChecked((_childValues.get("Friday") > 0) ? true : false);

                ToggleButton setSaturday = (ToggleButton) _convertView.findViewById(_viewID[6]);
                setSaturday.setChecked((_childValues.get("Saturday") > 0) ? true : false);

                ToggleButton setSunday = (ToggleButton) _convertView.findViewById(_viewID[7]);
                setSunday.setChecked((_childValues.get("Sunday") > 0) ? true : false);
            }
            break;
            case WAKEUP_MUSIC:{
                //Choosing Music Button
                Button setMusicButton = (Button) _convertView.findViewById(_viewID[1]);
                String musicText = "Choose Music"; //TODO set Music text Button Name
                setMusicButton.setText(musicText);

                //Set Volume Button
                Button setMusicVolumeButton = (Button) _convertView.findViewById(_viewID[2]);
                String musicVolumeText = _childValues.get("Volume") + "%";
                setMusicVolumeButton.setText(musicVolumeText);

                //Set Start Time Button
                Button setMusicStartTime = (Button) _convertView.findViewById(_viewID[3]);
                int seconds = _childValues.get("StartTime");
                String startTimeText = String.format(
                        "%02d:%02d",
                        TimeUnit.SECONDS.toMinutes(seconds),
                        seconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)));
                setMusicStartTime.setText(startTimeText);

                //Set FadeIn ToggleButton
                ToggleButton setFadeIn = (ToggleButton) _convertView.findViewById(_viewID[4]);
                int fadeSeconds  = _childValues.get("FadeInTime");
                String FadeInOn  =  String.format("%02ds", fadeSeconds);
                String FadeInOff = "OFF";

                setFadeIn.setTextOn(FadeInOn);
                setFadeIn.setTextOff(FadeInOff);

                boolean fadeChecked = (_childValues.get("FadeIn") == 1)? true : false;
                setFadeIn.setChecked(fadeChecked);

                //Set Vibration ToggleButton
                ToggleButton setVibrationButton = (ToggleButton) _convertView.findViewById(_viewID[5]);
                String vibrationTextOn      = _childValues.get("VibrationValue") + "%";
                String vibrationTextOff      = "OFF";

                setVibrationButton.setTextOn(vibrationTextOn);
                setVibrationButton.setTextOff(vibrationTextOff);

                boolean vibraChecked = (_childValues.get("Vibration") == 1)? true : false;
                setVibrationButton.setChecked(vibraChecked);
            }
            break;
            case WAKEUP_LIGHT:{
                //Toggle Screen light
                ToggleButton setLightButton = (ToggleButton) _convertView.findViewById(_viewID[1]);
                String lightTextOn = "ON"; //TODO set light Text
                String lightTextOff = "OFF";
                setLightButton.setTextOn(lightTextOn);
                setLightButton.setTextOff(lightTextOff);

                //First Color
                Button setColorButton1 = (Button) _convertView.findViewById(_viewID[2]);
                String colorText      = "Color1"; //TODO set Light Color Text
                setColorButton1.setText(colorText);

                //Second Color
                Button setColorButton2 = (Button) _convertView.findViewById(_viewID[3]);
                String colorText2      = "Color2"; //TODO set Light Color Text
                setColorButton2.setText(colorText2);

                //Toggle Fading
                ToggleButton setFadeColor = (ToggleButton) _convertView.findViewById(_viewID[4]);
                String colorFadeTextOn = "ON"; //TODO set light Text
                String colorFadeTextOff = "OFF";
                setFadeColor.setTextOn(colorFadeTextOn);
                setFadeColor.setTextOff(colorFadeTextOff);

                //Toggle LED
                ToggleButton setLEDButton = (ToggleButton) _convertView.findViewById(_viewID[5]);
                String timeTextOn = "LED ON"; //Todo set LED TExt maybe switch to toggle with time slider
                String timeTextOff = "LED OFF";
                setLEDButton.setTextOn(timeTextOn);
                setLEDButton.setTextOff(timeTextOff);
            }
            break;
            case WAKEUP_DELETE:{
                Button deleteAlarm = (Button) _convertView.findViewById(R.id.wakeup_timer_deleteButton);
                String deleteText = "Delete";
                deleteAlarm.setText(deleteText);
            }
            default:
                break;
        }
        return _convertView;
    }
    //Childs/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return  this.wakeup_child.get(wakeup_alarm.get(groupPosition)).get(WAKEUP_CHILDS[childPosition]);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

       //TextView alarmName = (TextView) convertView.findViewById(R.id.wakeup_timer_groupItem);
        //alarmName.setClickable(true);

        final LinkedHashMap<String, Integer> childValues = (LinkedHashMap<String, Integer>)getChild(groupPosition, childPosition);

        String choosenChild = WAKEUP_CHILDS[childPosition];

                switch (choosenChild){
                    case WAKEUP_DAYS: {
                        int[] wakeupDay_ID = {
                                R.id.wakeup_timer_days_textview,
                                R.id.wakeup_monday,
                                R.id.wakeup_tuesday,
                                R.id.wakeup_wednesday,
                                R.id.wakeup_thursday,
                                R.id.wakeup_friday,
                                R.id.wakeup_saturday,
                                R.id.wakeup_sunday };

                        convertView = inflateLayout(convertView, R.layout.wakeup_timer_listitem_days, choosenChild,  childValues, wakeupDay_ID);
                    }
                    break;
                    case WAKEUP_TIME: {
                        int[] wakeupTime_ID = {
                                R.id.wakeup_timer_time_textview,
                                R.id.wakeup_timer_time_buttonTime,
                                R.id.wakeup_timer_time_buttonSnooze };
                        convertView = inflateLayout(convertView, R.layout.wakeup_timer_listitem_time, choosenChild, childValues, wakeupTime_ID);
                    }
                    break;
                    case WAKEUP_MUSIC: {
                        int[] wakeup_Music_ID = {
                                R.id.wakeup_timer_music_textview,
                                R.id.wakeup_timer_music_buttonMusic,
                                R.id.wakeup_timer_music_buttonMusicVolume,
                                R.id.wakeup_timer_music_SongStart,
                                R.id.wakeup_timer_music_toggleFadeIn,
                                R.id.wakeup_timer_music_toggleVibration };
                        convertView = inflateLayout(convertView, R.layout.wakeup_timer_listitem_music, choosenChild, childValues, wakeup_Music_ID);
                    }
                    break;
                    case WAKEUP_LIGHT: {
                        int[] wakeup_Light_ID = {
                                R.id.wakeup_timer_light_textview,
                                R.id.wakeup_timer_light_buttonLight,
                                R.id.wakeup_timer_light_buttonColor1,
                                R.id.wakeup_timer_light_buttonColor2,
                                R.id.wakeup_timer_light_buttonScreenFade,
                                R.id.wakeup_timer_light_buttonLED };
                        convertView = inflateLayout(convertView, R.layout.wakeup_timer_listitem_light, choosenChild, childValues, wakeup_Light_ID);
                    }
                    break;
                    case WAKEUP_DELETE: {
                        int[] wakeup_Delete_ID = {
                                R.id.wakeup_timer_delete_textview,
                                R.id.wakeup_timer_deleteButton };
                        convertView = inflateLayout(convertView, R.layout.wakeup_timer_listitem_delete, "", childValues, wakeup_Delete_ID);
                    }
                    default:
                        break;
                }
             //else Log.e("Invalid Data:", "pairValueName: not in Range",null ); //TODO implement Error Handling!
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.wakeup_child.get(wakeup_alarm.get(groupPosition)).size();
    }

    //Groups/////////////////////////////////////////////////////////////////////////
    @Override
    public Object getGroup(int groupPosition) {
        return this.wakeup_header.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.wakeup_header.size();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        //Set Group Title
        String headerTitle = (String) getGroup(groupPosition);

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.wakeup_timer_listgroup, null);
        }

        TextView txtListHeader = (TextView) convertView.findViewById(R.id.wakeup_timer_groupItem);
        txtListHeader.setTypeface(null, Typeface.BOLD);
        txtListHeader.setText(headerTitle);

        if(isExpanded)
            txtListHeader.setClickable(true);
        else
            txtListHeader.setClickable(false);


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
