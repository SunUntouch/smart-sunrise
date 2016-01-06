package com.zhun.sununtouch.smart_sunrise;

import android.content.Context;
import android.database.Cursor;
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
import java.util.concurrent.TimeUnit;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private final static String[] WAKEUP_CHILDS  = {AlarmConstants.WAKEUP_DELETE, AlarmConstants.WAKEUP_TIME, AlarmConstants.WAKEUP_DAYS, AlarmConstants.WAKEUP_MUSIC, AlarmConstants.WAKEUP_LIGHT};

    private Context      context;
    private List<String> wakeup_header;
    private List<String> wakeup_alarm;
    private List <String>  wakeup_musicURIs;
    private LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>> wakeup_child;

    public ExpandableListAdapter(Context _context, List<String> _wakeup_alarm, List<String> _wake_up_musicURIs, List<String> _wakeup_header, LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>> _wakeup_child){
        this.context         = _context;
        this.wakeup_header   = _wakeup_header;
        this.wakeup_alarm    = _wakeup_alarm;
        this.wakeup_child    = _wakeup_child;
        this.wakeup_musicURIs = _wake_up_musicURIs;
    }

    public void notifyDataSetChanged(List<String> _wakeup_alarm, List<String> _wake_up_musicURIs, List<String> _wakeup_header, LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>> _wakeup_child) {
        this.wakeup_header   = _wakeup_header;
        this.wakeup_alarm    = _wakeup_alarm;
        this.wakeup_child    = _wakeup_child;
        this.wakeup_musicURIs = _wake_up_musicURIs;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    private View inflateLayout(View _convertView, int _groupPosition, int _layoutID, String _childText, LinkedHashMap<String, Integer> _childValues, int[] _viewID){

        //Create new Layout Inflater
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _convertView = inflater.inflate(_layoutID, null);

        //Switch Behaviour for every Setting View
        switch(_childText){
            case AlarmConstants.WAKEUP_TIME:{

                //Get TextChild from View
                TextView txtListChild = (TextView) _convertView.findViewById(_viewID[0]);
                txtListChild.setText(_convertView.getContext().getString(R.string.wakeup_time));

                Button setTimeButton = (Button) _convertView.findViewById(_viewID[1]);

                String timeText = String.format("%02d",_childValues.get(AlarmConstants.ALARM_TIME_HOUR)) +
                        ":" + String.format("%02d", _childValues.get(AlarmConstants.ALARM_TIME_MINUTES));
                setTimeButton.setText(timeText);

                Button setSnoozeButton = (Button) _convertView.findViewById(_viewID[2]);
                String snoozeText      = _convertView.getContext().getString(R.string.wakeup_time_snooze) + " " + _childValues.get(AlarmConstants.ALARM_TIME_SNOOZE) + " " + _convertView.getContext().getString(R.string.wakeup_time_minutes);
                setSnoozeButton.setText(snoozeText);
            }
            break;
            case AlarmConstants.WAKEUP_DAYS:{

                //Get TextChild from View
                TextView txtListChild = (TextView) _convertView.findViewById(_viewID[0]);
                txtListChild.setText(_convertView.getContext().getString(R.string.wakeup_day));

                ToggleButton setMonday = (ToggleButton) _convertView.findViewById(_viewID[1]);
                setMonday.setChecked((_childValues.get(AlarmConstants.ALARM_DAY_MONDAY) > 0) ? true : false);

                ToggleButton setTuesday = (ToggleButton) _convertView.findViewById(_viewID[2]);
                setTuesday.setChecked((_childValues.get(AlarmConstants.ALARM_DAY_TUESDAY) > 0) ? true : false);

                ToggleButton setWednesday = (ToggleButton) _convertView.findViewById(_viewID[3]);
                setWednesday.setChecked((_childValues.get(AlarmConstants.ALARM_DAY_WEDNESDAY) > 0) ? true : false);

                ToggleButton setThursday = (ToggleButton) _convertView.findViewById(_viewID[4]);
                setThursday.setChecked((_childValues.get(AlarmConstants.ALARM_DAY_THURSDAY) > 0) ? true : false);

                ToggleButton setFriday = (ToggleButton) _convertView.findViewById(_viewID[5]);
                setFriday.setChecked((_childValues.get(AlarmConstants.ALARM_DAY_FRIDAY) > 0) ? true : false);

                ToggleButton setSaturday = (ToggleButton) _convertView.findViewById(_viewID[6]);
                setSaturday.setChecked((_childValues.get(AlarmConstants.ALARM_DAY_SATURDAY) > 0) ? true : false);

                ToggleButton setSunday = (ToggleButton) _convertView.findViewById(_viewID[7]);
                setSunday.setChecked((_childValues.get(AlarmConstants.ALARM_DAY_SUNDAY) > 0) ? true : false);
            }
            break;
            case AlarmConstants.WAKEUP_MUSIC:{

                //Get TextChild from View
                TextView txtListChild = (TextView) _convertView.findViewById(_viewID[0]);
                txtListChild.setText(_convertView.getContext().getString(R.string.wakeup_music));

                //Choosing Music Button
                String newMusicURI  = wakeup_musicURIs.get(_groupPosition);
                String newMusicText = newMusicURI.substring(newMusicURI.lastIndexOf('/') + 1);
                if(newMusicText.indexOf('.') != -1)
                    newMusicText = newMusicText.substring(0, newMusicText.lastIndexOf('.'));

                Button setMusicButton = (Button) _convertView.findViewById(_viewID[1]);
                setMusicButton.setText(newMusicText);

                //Set Volume Button
                Button setMusicVolumeButton = (Button) _convertView.findViewById(_viewID[2]);
                String musicVolumeText = _childValues.get(AlarmConstants.ALARM_MUSIC_VOLUME) + "%";
                setMusicVolumeButton.setText(musicVolumeText);

                //Set Start Time Button
                Button setMusicStartTime = (Button) _convertView.findViewById(_viewID[3]);
                int seconds = _childValues.get(AlarmConstants.ALARM_MUSIC_SONGSTART);
                String startTimeText = String.format(
                        "%02d:%02d",
                        TimeUnit.SECONDS.toMinutes(seconds),
                        seconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)));
                setMusicStartTime.setText(startTimeText);

                //Set FadeIn ToggleButton
                ToggleButton setFadeIn = (ToggleButton) _convertView.findViewById(_viewID[4]);
                int fadeSeconds  = _childValues.get(AlarmConstants.ALARM_MUSIC_FADEINTIME);
                String FadeInOn  =  String.format("%02ds", fadeSeconds);
                String FadeInOff = _convertView.getContext().getString(R.string.wakeup_music_fadeOff);

                setFadeIn.setTextOn(FadeInOn);
                setFadeIn.setTextOff(FadeInOff);

                boolean fadeChecked = (_childValues.get(AlarmConstants.ALARM_MUSIC_FADEIN) == 1)? true : false;
                setFadeIn.setChecked(fadeChecked);

                //Set Vibration ToggleButton
                ToggleButton setVibrationButton = (ToggleButton) _convertView.findViewById(_viewID[5]);
                String vibrationTextOn       = _childValues.get(AlarmConstants.ALARM_MUSIC_VIBRATION_VALUE) + "%";
                String vibrationTextOff      = _convertView.getContext().getString(R.string.wakeup_music_vibraOff);

                setVibrationButton.setTextOn(vibrationTextOn);
                setVibrationButton.setTextOff(vibrationTextOff);

                boolean vibraChecked = (_childValues.get(AlarmConstants.ALARM_MUSIC_VIBRATION_ACTIV) == 1)? true : false;
                setVibrationButton.setChecked(vibraChecked);
            }
            break;
            case AlarmConstants.WAKEUP_LIGHT:{

                //Get TextChild from View
                TextView txtListChild = (TextView) _convertView.findViewById(_viewID[0]);
                txtListChild.setText(_convertView.getContext().getString(R.string.wakeup_light));

                //Toggle Screen light
                ToggleButton setLightButton = (ToggleButton) _convertView.findViewById(_viewID[1]);
                String screenOn =  _convertView.getContext().getString(R.string.wakeup_light_screen_brightness) + " " +
                        _childValues.get(AlarmConstants.ALARM_LIGHT_SCREEN_BRIGTHNESS) + "%";
                String screenOff = _convertView.getContext().getString(R.string.wakeup_light_screen_brightness_off);

                setLightButton.setTextOn(screenOn);
                setLightButton.setTextOff(screenOff);

                boolean screenChecked = (_childValues.get(AlarmConstants.ALARM_LIGHT_SCREEN) == 1)? true : false;
                setLightButton.setChecked(screenChecked);

                //Set Start Time Button
                Button setStartTime = (Button) _convertView.findViewById(_viewID[2]);
                String startTimeText= _childValues.get(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME) + " " +
                        _convertView.getContext().getString(R.string.wakeup_time_minutes);
                setStartTime.setText(startTimeText);

                //First Color
                Button setColorButton1 = (Button) _convertView.findViewById(_viewID[3]);
                String colorText      = _convertView.getContext().getString(R.string.wakeup_light_screen_color1); //TODO set Light Color Text
                setColorButton1.setText(colorText);
                setColorButton1.getBackground().setColorFilter(_childValues.get(AlarmConstants.ALARM_LIGHT_COLOR1), PorterDuff.Mode.MULTIPLY);

                //Second Color
                Button setColorButton2 = (Button) _convertView.findViewById(_viewID[4]);
                String colorText2      = _convertView.getContext().getString(R.string.wakeup_light_screen_color2); //TODO set Light Color Text
                setColorButton2.setText(colorText2);
                setColorButton2.getBackground().setColorFilter(_childValues.get(AlarmConstants.ALARM_LIGHT_COLOR2), PorterDuff.Mode.MULTIPLY);

                //Toggle Fading
                ToggleButton setFade = (ToggleButton) _convertView.findViewById(_viewID[5]);
                String colorFadeTextOn = _convertView.getContext().getString(R.string.wakeup_light_screen_fadingOn); //TODO set light Text
                String colorFadeTextOff = _convertView.getContext().getString(R.string.wakeup_light_screen_fadingOff);
                setFade.setTextOn(colorFadeTextOn);
                setFade.setTextOff(colorFadeTextOff);

                boolean FadeChecked = (_childValues.get(AlarmConstants.ALARM_LIGHT_FADECOLOR) == 1)? true : false;
                setFade.setChecked(FadeChecked);

                //Toggle LED
                ToggleButton setLEDButton = (ToggleButton) _convertView.findViewById(_viewID[6]);
                String timeTextOn = _convertView.getContext().getString(R.string.wakeup_light_screen_LEDOn); //Todo set LED TExt maybe switch to toggle with time slider
                String timeTextOff = _convertView.getContext().getString(R.string.wakeup_light_screen_LEDOff);
                setLEDButton.setTextOn(timeTextOn);
                setLEDButton.setTextOff(timeTextOff);

                boolean LEDChecked = (_childValues.get(AlarmConstants.ALARM_LIGHT_USELED) == 1)? true : false;
                setLEDButton.setChecked(LEDChecked);

                //Set LED Start Time
                Button setLEDStartTime = (Button) _convertView.findViewById(_viewID[7]);
                String startTimeLEDText= _childValues.get(AlarmConstants.ALARM_LIGHT_LED_START_TIME) + " " +
                        _convertView.getContext().getString(R.string.wakeup_time_minutes);
                setLEDStartTime.setText(startTimeLEDText);
            }
            break;
            case AlarmConstants.WAKEUP_DELETE:{
                //Get TextChild from View
                TextView txtListChild = (TextView) _convertView.findViewById(_viewID[0]);
                txtListChild.setText(_childText);

                Button deleteAlarm = (Button) _convertView.findViewById(R.id.wakeup_timer_deleteButton);
                String deleteText = _convertView.getContext().getString(R.string.wakeup_delete);
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
                    case AlarmConstants.WAKEUP_DAYS: {
                        int[] wakeupDay_ID = {
                                R.id.wakeup_timer_days_textview,
                                R.id.wakeup_monday,
                                R.id.wakeup_tuesday,
                                R.id.wakeup_wednesday,
                                R.id.wakeup_thursday,
                                R.id.wakeup_friday,
                                R.id.wakeup_saturday,
                                R.id.wakeup_sunday };

                        convertView = inflateLayout(
                                convertView,
                                groupPosition,
                                R.layout.wakeup_timer_listitem_days,
                                choosenChild,
                                childValues,
                                wakeupDay_ID);
                    }
                    break;
                    case AlarmConstants.WAKEUP_TIME: {
                        int[] wakeupTime_ID = {
                                R.id.wakeup_timer_time_textview,
                                R.id.wakeup_timer_time_buttonTime,
                                R.id.wakeup_timer_time_buttonSnooze };

                        convertView = inflateLayout(
                                convertView,
                                groupPosition,
                                R.layout.wakeup_timer_listitem_time,
                                choosenChild,
                                childValues,
                                wakeupTime_ID);
                    }
                    break;
                    case AlarmConstants.WAKEUP_MUSIC: {
                        int[] wakeup_Music_ID = {
                                R.id.wakeup_timer_music_textview,
                                R.id.wakeup_timer_music_buttonMusic,
                                R.id.wakeup_timer_music_buttonMusicVolume,
                                R.id.wakeup_timer_music_SongStart,
                                R.id.wakeup_timer_music_toggleFadeIn,
                                R.id.wakeup_timer_music_toggleVibration };

                        convertView = inflateLayout(
                                convertView,
                                groupPosition,
                                R.layout.wakeup_timer_listitem_music,
                                choosenChild,
                                childValues,
                                wakeup_Music_ID);
                    }
                    break;
                    case AlarmConstants.WAKEUP_LIGHT: {
                        int[] wakeup_Light_ID = {
                                R.id.wakeup_timer_light_textview,
                                R.id.wakeup_timer_light_buttonLight,
                                R.id.wakeup_timer_light_buttonStart,
                                R.id.wakeup_timer_light_buttonColor1,
                                R.id.wakeup_timer_light_buttonColor2,
                                R.id.wakeup_timer_light_buttonScreenFade,
                                R.id.wakeup_timer_light_buttonLED,
                                R.id.wakeup_timer_light_buttonLEDStart};

                        convertView = inflateLayout(
                                convertView,
                                groupPosition,
                                R.layout.wakeup_timer_listitem_light,
                                choosenChild,
                                childValues,
                                wakeup_Light_ID);
                    }
                    break;
                    case AlarmConstants.WAKEUP_DELETE: {
                        int[] wakeup_Delete_ID = {
                                R.id.wakeup_timer_delete_textview,
                                R.id.wakeup_timer_deleteButton };

                        convertView = inflateLayout(
                                convertView,
                                groupPosition,
                                R.layout.wakeup_timer_listitem_delete,
                                "",
                                childValues,
                                wakeup_Delete_ID);
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
