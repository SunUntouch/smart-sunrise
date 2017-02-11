package com.zhun.sununtouch.smart_sunrise.Information;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Sunny on 22.01.2016.
 * Helper Class to show a Toast
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AlarmToast {

    public static void showToast(Context context, boolean longToast, String toastShortText, String toastLongText){

        if(longToast)
            showToastLong(context, toastLongText);
        else
            showToastShort(context, toastShortText);
    }

    public static void showToastShort(Context context, String toastShortText){
        Toast.makeText(context, toastShortText, Toast.LENGTH_SHORT).show();
    }
    public static void showToastShort(Context context, boolean shortToast, String toastTextPositive, String toastTextNegative){

        if(shortToast)
            showToastShort(context, toastTextPositive);
        else
            showToastShort(context, toastTextNegative);
    }

    public static void showToastLong(Context context, String toastLongText){
        Toast.makeText(context, toastLongText, Toast.LENGTH_LONG).show();
    }
    public static void showToastLong(Context context, boolean longToast, String toastTextPositive, String toastTextNegative){

        if(longToast)
            showToastLong(context, toastTextPositive);
        else
            showToastLong(context, toastTextNegative);
    }
}
