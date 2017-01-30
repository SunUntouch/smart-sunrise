package com.zhun.sununtouch.smart_sunrise;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sunny
 * Helper Class for Setting time Fragment in Main Activity
 */

public /*static*/ class SettingTimeFragment extends DialogFragment{ //no need of static because all top level classes in java are static

    private Activity mActivity;
    private TimePickerDialog.OnTimeSetListener mListener;
    private int screenTime, ledTime;
    private void initialize(){

        mActivity = getActivity();
        if(mActivity != null)
        {
            //Error to implement onClickListener
            try{
                mListener = (TimePickerDialog.OnTimeSetListener) mActivity;
            } catch (ClassCastException e) {
                throw new ClassCastException(mActivity.toString() + "must implement OnTimeSetListener");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        screenTime = getArguments().getInt(AlarmConstants.ALARM_LIGHT_SCREEN_START_TIME, 0);
        ledTime = getArguments().getInt(AlarmConstants.ALARM_LIGHT_LED_START_TIME, 0);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        initialize();
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Use Current Time as Default Value for Picker
        final int currentTime = (ledTime < screenTime) ? screenTime : ledTime;
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calendar.getTimeInMillis() + TimeUnit.MINUTES.toMillis(currentTime));
        return new TimePickerDialog(
                mActivity,
                mListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                android.text.format.DateFormat.is24HourFormat(mActivity));
    }
}
