package com.zhun.sununtouch.smart_sunrise;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by Sunny on 22.01.2016.
 */
public class AlarmToast {

    public static void showToastShort(Context _context, String _toastShortText){
        Toast.makeText(_context, _toastShortText, Toast.LENGTH_SHORT).show();
    }

    public static void showToastLong(Context _context, String _toastLongText){
        Toast.makeText(_context, _toastLongText, Toast.LENGTH_LONG).show();
    }

    public static void showToast(Context _context, boolean _longToast, String _toastShortText, String _toastLongText){

        if(!_longToast)
            Toast.makeText(_context, _toastLongText, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(_context, _toastShortText, Toast.LENGTH_SHORT).show();
    }
    public static void showToastShort(Context _context, boolean _shortToast, String _toastTextPositive, String _toastTextNegative){

        if(_shortToast)
            Toast.makeText(_context, _toastTextPositive, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(_context, _toastTextNegative, Toast.LENGTH_SHORT).show();
    }

    public static void showToastLong(Context _context, boolean _longToast, String _toastTextPositive, String _toastTextNegative){

        if(_longToast)
            Toast.makeText(_context, _toastTextPositive, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(_context, _toastTextNegative, Toast.LENGTH_LONG).show();
    }
}
