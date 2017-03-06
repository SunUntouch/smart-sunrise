package com.zhun.sununtouch.smart_sunrise.Configuration;

import android.content.Context;
import android.support.compat.BuildConfig;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by Sunny on 10.02.2017.
 * Helper Class for logging to File and Console together
 */

public class AlarmLogging {

    private final Context mContext;

    public AlarmLogging(Context context) {
        mContext = context;
    }

    public void i(String TAG, String message) {

        //Printing to LogCat
        Log.i(TAG, message);

        //Print to File if Activated
        AlarmSystemConfiguration config = new AlarmSystemConfiguration(mContext);
        if (config.loggingEnabled())
            appendLog("INFORMATION", TAG, message);
    }

    public void d(String TAG, String message) {

        //Printing to LogCat
        Log.d(TAG, message);

        //Print to File if Activated
        AlarmSystemConfiguration config = new AlarmSystemConfiguration(mContext);
        if (config.loggingEnabled() && BuildConfig.DEBUG)
            appendLog("DEBUG", TAG, message);
    }

    public void e(String TAG, String message) {

        //Printing to LogCat
        Log.e(TAG, message);

        //Print to File if Activated
        AlarmSystemConfiguration config = new AlarmSystemConfiguration(mContext);
        if (config.loggingEnabled())
            appendLog("ERROR", TAG, message);
    }

    public void v(String TAG, String message) {

        //Printing to LogCat
        Log.v(TAG, message);

        //Print to File if Activated
        AlarmSystemConfiguration config = new AlarmSystemConfiguration(mContext);
        if (config.loggingEnabled())
            appendLog("VERBOSE", TAG, message);
    }

    public void w(String TAG, String message) {

        //Printing to LogCat
        Log.w(TAG, message);

        //Print to File if Activated
        AlarmSystemConfiguration config = new AlarmSystemConfiguration(mContext);
        if (config.loggingEnabled())
            appendLog("WARNING", TAG, message);
    }

    public void wtf(String TAG, String message) {

        //Printing to LogCat
        Log.wtf(TAG, message);

        //Print to File if Activated
        AlarmSystemConfiguration config = new AlarmSystemConfiguration(mContext);
        if (config.loggingEnabled())
            appendLog("WHAT A TERRIBLE FAILURE", TAG, message);
    }

    private void appendLog(String Level, String TAG, String message) {

        //Create Directory
        File directory = new File("sdcard/SmartSunrise/");
        directory.mkdirs();

        final Calendar calendar = Calendar.getInstance();
        final String fileName =
                "sdcard/SmartSunrise/Logging" +
                        Integer.toString(calendar.get(Calendar.YEAR)) + "_" +
                        Integer.toString((calendar.get(Calendar.MONTH)) + 1) + "_" +
                        Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)) +
                        ".txt";

        //Create File
        File logFile = new File(fileName);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("IOException: ", e.getMessage());
            }
        }

        //Write to File
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(DateFormat.getTimeInstance().format(calendar.getTime()));
            buf.append(": ");
            buf.append(Level);
            buf.append(": ");
            buf.append(TAG);
            buf.append(": ");
            buf.append(message);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("IOException: ", e.getMessage());
        }
    }
}
