package com.zhun.sununtouch.smart_sunrise;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Sunny on 12.12.2015.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context      context;
    private List<String> wakeup_header;
    private HashMap<String, List<String>> wakeup_child;

    public ExpandableListAdapter(Context _context, List<String> _wakeup_header, HashMap<String, List<String>> _wakeup_child){
        this.context       = _context;
        this.wakeup_header = _wakeup_header;
        this.wakeup_child  = _wakeup_child;
    }

    //Childs/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.wakeup_child.get(this.wakeup_header.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.wakeup_timer_listitem, null);
        }

        TextView txtListChild = (TextView) convertView.findViewById(R.id.wakeup_timer_listItem);
        txtListChild.setText(childText);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.wakeup_child.get(this.wakeup_header.get(groupPosition)).size();
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
