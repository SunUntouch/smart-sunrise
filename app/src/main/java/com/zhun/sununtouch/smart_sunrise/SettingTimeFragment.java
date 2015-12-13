package com.zhun.sununtouch.smart_sunrise;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.util.Calendar;


public /*static*/ class SettingTimeFragment extends DialogFragment{ //no need of static because all top level classes in java are static

    private Activity mActivity;
    private TimePickerDialog.OnTimeSetListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = activity;

        //Error to implement onClickListener
        try{
            mListener = (TimePickerDialog.OnTimeSetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement OnTimeSetlistener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Use Current Time as Default Value for Picker
        final Calendar calendar = Calendar.getInstance();
        int hour   = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        //Create new Instance of TimePicker and return it
        return new TimePickerDialog(mActivity, mListener, hour, minute, true); //TODO: Settings for 12 and 24 Hours
    }
}
