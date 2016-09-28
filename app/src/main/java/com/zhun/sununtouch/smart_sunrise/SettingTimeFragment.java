package com.zhun.sununtouch.smart_sunrise;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;

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
            throw new ClassCastException(activity.toString() + "must implement OnTimeSetListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Use Current Time as Default Value for Picker
        final Calendar calendar = Calendar.getInstance();
        return new TimePickerDialog(mActivity, mListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true); //TODO: Settings for 12 and 24 Hours
    }
}
