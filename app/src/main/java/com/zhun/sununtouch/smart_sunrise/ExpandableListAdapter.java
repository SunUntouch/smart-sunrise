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

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    public final static String WAKEUP_DAYS  = "Days";
    public final static String WAKEUP_TIME  = "Time";
    public final static String WAKEUP_MUSIC = "Music";
    public final static String WAKEUP_LIGHT = "Light";

    public final static String[] WAKEUP_CHILDS  = { WAKEUP_TIME, WAKEUP_DAYS, WAKEUP_MUSIC, WAKEUP_LIGHT};

    private Context      context;
    private List<String> wakeup_header;
    private List<String> wakeup_alarm;
    private LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>> wakeup_child;

    public ExpandableListAdapter(Context _context, List<String> _wakeup_alarm, List<String> _wakeup_header, LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>> _wakeup_child){
        this.context       = _context;
        this.wakeup_header = _wakeup_header;
        this.wakeup_alarm  = _wakeup_alarm;
        this.wakeup_child = _wakeup_child;
    }

    public void notifyDataSetChanged(List<String> _wakeup_alarm, List<String> _wakeup_header, LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>> _wakeup_child) {
        this.wakeup_header = _wakeup_header;
        this.wakeup_alarm  = _wakeup_alarm;
        this.wakeup_child = _wakeup_child;
        //super.notifyDataSetChanged();
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
                Button setMusicButton = (Button) _convertView.findViewById(_viewID[1]);
                String musicText = "Choose Music"; //TODO set Music text Button Name
                setMusicButton.setText(musicText);

                Button setVibrationButton = (Button) _convertView.findViewById(_viewID[2]);
                String vibrationText      = "Set Vibration "; //TODO set Music Vibration Text and Strength
                setVibrationButton.setText(vibrationText);
            }
            break;
            case WAKEUP_LIGHT:{
                Button setLightButton = (Button) _convertView.findViewById(_viewID[1]);
                String lightText = "Set Light"; //TODO set light Text
                setLightButton.setText(lightText);

                Button setColorButton = (Button) _convertView.findViewById(_viewID[2]);
                String colorText      = "Choose Color"; //TODO set Light Color Text
                setColorButton.setText(colorText);

                Button setLEDButton = (Button) _convertView.findViewById(_viewID[3]);
                String timeText = "Set LED"; //Todo set LED TExt maybe switch to toggle with time slider
                setLEDButton.setText(timeText);
            }
            break;
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
                                R.id.wakeup_timer_music_buttonVibration };
                        convertView = inflateLayout(convertView, R.layout.wakeup_timer_listitem_music, choosenChild, childValues, wakeup_Music_ID);
                    }
                    break;
                    case WAKEUP_LIGHT: {
                        int[] wakeup_Light_ID = {
                                R.id.wakeup_timer_light_textview,
                                R.id.wakeup_timer_light_buttonLight,
                                R.id.wakeup_timer_light_buttonColor,
                                R.id.wakeup_timer_light_buttonLED };
                        convertView = inflateLayout(convertView, R.layout.wakeup_timer_listitem_light, choosenChild, childValues, wakeup_Light_ID);
                    }
                    break;
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

        return convertView;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
