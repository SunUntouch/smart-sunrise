package com.zhun.sununtouch.smart_sunrise;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sunny on 12.12.2015.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {
    public final static String WAKEUP_DAYS  = "Days";
    public final static String WAKEUP_TIME  = "Time";
    public final static String WAKEUP_MUSIC = "Music";
    public final static String WAKEUP_LIGHT = "Light";

    public final static String[] WAKEUP_CHILDS  = { WAKEUP_TIME, WAKEUP_DAYS, WAKEUP_MUSIC, WAKEUP_LIGHT};

    private Context      context;
    private List<String> wakeup_header;
    private List<String> wakeup_alarm;
    private LinkedHashMap<String, List<String>> wakeup_child;

    private LinkedHashMap<String, LinkedHashMap<String, List<LinkedHashMap<String, Integer>>>> wakeup_child2;

    public ExpandableListAdapter(Context _context, List<String> _wakeup_alarm, List<String> _wakeup_header, LinkedHashMap<String, LinkedHashMap<String, List<LinkedHashMap<String, Integer>>>> _wakeup_child2){
        this.context       = _context;
        this.wakeup_header = _wakeup_header;
        this.wakeup_alarm  = _wakeup_alarm;
        this.wakeup_child2 = _wakeup_child2;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    private View inflateLayout(View _convertView, int _layoutID, String _childText, int _viewID){

        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _convertView = inflater.inflate(_layoutID, null);

        TextView txtListChild = (TextView) _convertView.findViewById(_viewID);
        txtListChild.setText(_childText);

        return _convertView;
    }
    //Childs/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return  this.wakeup_child2.get(wakeup_alarm.get(groupPosition)).get(WAKEUP_CHILDS[childPosition]);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final List<LinkedHashMap<String, Integer>> childValues = (List<LinkedHashMap<String, Integer>>)getChild(groupPosition, childPosition);

        String choosenChild = WAKEUP_CHILDS[childPosition];

                switch (choosenChild){
                    case WAKEUP_DAYS: {
                        convertView = inflateLayout(convertView, R.layout.wakeup_timer_listitem_days, choosenChild, R.id.wakeup_timer_days_textview);
                    }
                    break;
                    case WAKEUP_TIME: {
                        convertView = inflateLayout(convertView, R.layout.wakeup_timer_listitem_time, choosenChild, R.id.wakeup_timer_time_textview);
                    }
                    break;
                    case WAKEUP_MUSIC: {
                        convertView = inflateLayout(convertView, R.layout.wakeup_timer_listitem_music, choosenChild, R.id.wakeup_timer_music_textview);
                    }
                    break;
                    case WAKEUP_LIGHT: {
                        convertView = inflateLayout(convertView, R.layout.wakeup_timer_listitem_light, choosenChild, R.id.wakeup_timer_light_textview);
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
        return this.wakeup_child2.get(wakeup_alarm.get(groupPosition)).size();
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
