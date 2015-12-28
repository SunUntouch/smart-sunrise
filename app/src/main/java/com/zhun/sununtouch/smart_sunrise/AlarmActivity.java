package com.zhun.sununtouch.smart_sunrise;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class AlarmActivity extends AppCompatActivity {

    //Private camera Values
    private CameraCaptureSession   mSession;
    private CaptureRequest.Builder mBuilder;
    private CameraDevice           mCameraDevice;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private android.os.Handler dateHandler = new android.os.Handler();
    private android.os.Handler timeHandler = new android.os.Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        Bundle intentExtras = getIntent().getExtras();
        int[] time  = intentExtras.getIntArray(AlarmConstants.WAKEUP_TIME); //Time, Minutes, Snooze
        int[] days  = intentExtras.getIntArray(AlarmConstants.WAKEUP_DAYS); //Monday - Sunday
        int[] music = intentExtras.getIntArray(AlarmConstants.WAKEUP_MUSIC);// Song, StartTime, Volume, FadIn, FadeInTime, Vibration Aktiv, Vibration Strength
        final int[] light = intentExtras.getIntArray(AlarmConstants.WAKEUP_LIGHT);// UseScreen, ScreenBrightness, ScreenStrartTime, Color1, Color2, FadeColor, UseLed, LedStartTime


        //SCREEN BRIGHTNESS/////////////////////////////////////////////////////////////////////////
        final boolean useScreen = (light[0] == 1) ? true : false;
        boolean useLED = (light[6] == 1) ? true : false;
        if(useScreen){

            if(light[2] > light[7])
                setBrightness(light);
            else {
                android.os.Handler screenLightHandler = new android.os.Handler();
                Runnable screenLightRunnable = new Runnable() {
                    @Override
                    public void run() {
                        setBrightness(light);
                    }
                };
                screenLightHandler.postDelayed(screenLightRunnable, TimeUnit.MINUTES.toMillis(light[7] - light[2]));
            }
        }
        else if(useLED){

            android.os.Handler screenTimerHandler = new android.os.Handler();
            Runnable screenTimerRunnable = new Runnable() {
                @Override
                public void run() {
                    startScreen();
                }
            };
            screenTimerHandler.postDelayed(screenTimerRunnable, TimeUnit.MINUTES.toMillis(light[7]));
        }
        else{
            startScreen();
        }

        //SCREEN////////////////////////////////////////////////////////////////////////////////////
        //SetViews
        //Date
        Calendar calendar = Calendar.getInstance();
        int currentDay   = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH) + 1; //Is Zero based January is 0
        int currentYear  = calendar.get(Calendar.YEAR);

        final TextView dateText = (TextView) findViewById(R.id.wakeup_wakescreen_date);
        String dayName = getDayName(calendar);

        dateText.setText(dayName + ", " + currentDay + "." + currentMonth + "." + currentYear);

        int currentHour   = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        final TextView timeText = (TextView) findViewById(R.id.wakeup_timer_wakescreen_textview);
        timeText.setText(String.format("%02d:%02d", currentHour, currentMinute));

        //Set new Handler for Updating Date and Time
        timeHandler = new android.os.Handler();
        final Runnable timeRunnable = new Runnable() {
            @Override
            public void run() {
                //Update Time
                Calendar calendarNew = Calendar.getInstance();
                timeText.setText(String.format("%02d:%02d", calendarNew.get(Calendar.HOUR_OF_DAY), calendarNew.get(Calendar.MINUTE)));

                //Update Date
                dateText.setText(getDayName(calendarNew) + ", " +
                        calendarNew.get(Calendar.DAY_OF_MONTH) + "." +
                        (calendarNew.get(Calendar.MONTH) + 1) + "." +
                        calendarNew.get(Calendar.YEAR));

                timeHandler.postDelayed(this, 100); //1 second check for new Time
            }
        };
        timeHandler.postDelayed(timeRunnable, 100); //1 second check for new Time


        //COLOR FADING//////////////////////////////////////////////////////////////////////////////
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.wakeup_wakescreen_layout);


        boolean colorFading = (light[5] == 1) ? true  : false;
        if(colorFading){
            linearLayout.setBackgroundColor(Color.BLACK);
            ObjectAnimator colorFade1 = ObjectAnimator.ofObject(linearLayout,"backgroundColor", new ArgbEvaluator(), Color.BLACK, light[3]);
            final long duration = TimeUnit.MINUTES.toMillis(light[2]) / 4;
            colorFade1.setDuration(duration);
            colorFade1.start();

            Handler colorHandler = new Handler();
            Runnable colorRunnable = new Runnable() {
                @Override
                public void run() {

                    ObjectAnimator colorFade2 = ObjectAnimator.ofObject(linearLayout,"backgroundColor", new ArgbEvaluator(), light[3], light[4]);
                    colorFade2.setDuration(duration * 3);
                    colorFade2.start();

                }
            };
            colorHandler.postDelayed(colorRunnable, duration );
        }
        else{
            linearLayout.setBackgroundColor(light[3]);
        }

        //LED///////////////////////////////////////////////////////////////////////////////////////
        if(useLED){
            if(light[2] > light[7]) { //If LED StartTime is bigger as LED Start Time

                //New Handler for Waiting Till Time to show LED
                final android.os.Handler handler = new android.os.Handler();

                Runnable runLED = new Runnable() {
                    @Override
                    public void run() {
                        startLED();
                    }
                };

                //Start Delayed
                handler.postDelayed(runLED, TimeUnit.MINUTES.toMillis(light[2] - light[7]));
            } else
                startLED();
        }
    }

    private String getDayName(Calendar _calendar){
        String dayName = "";
        switch(_calendar.get(Calendar.DAY_OF_WEEK)){
            case Calendar.MONDAY:
                dayName = this.getString(R.string.wakeup_day_monday);
                break;
            case Calendar.TUESDAY:
                dayName = this.getString(R.string.wakeup_day_tuesday);
                break;
            case Calendar.WEDNESDAY:
                dayName = this.getString(R.string.wakeup_day_wednesday);
                break;
            case Calendar.THURSDAY:
                dayName = this.getString(R.string.wakeup_day_thursday);
                break;
            case Calendar.FRIDAY:
                dayName = this.getString(R.string.wakeup_day_friday);
                break;
            case Calendar.SATURDAY:
                dayName = this.getString(R.string.wakeup_day_saturday);
                break;
            case Calendar.SUNDAY:
                dayName = this.getString(R.string.wakeup_day_sunday);
                break;
        }
        return dayName;
    }

    private void setBrightness(final int[] _lightValues){

        startScreen();

        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 0.0F;
        getWindow().setAttributes(layout);

        final long  millis =  TimeUnit.MINUTES.toMillis(_lightValues[2]) / 100;  //divide milliseconds with 100 because we have 100 steps till full illumination

        final android.os.Handler screenLightHandler = new android.os.Handler();
        Runnable screenLightRunnable = new Runnable() {
            @Override
            public void run() {

                WindowManager.LayoutParams layoutNew = getWindow().getAttributes();

                if(layoutNew.screenBrightness < (float) _lightValues[1] / 100){

                    layoutNew.screenBrightness += 0.01F;
                    getWindow().setAttributes(layoutNew);

                    screenLightHandler.postDelayed(this, millis);
                }
            }
        };
        screenLightHandler.postDelayed(screenLightRunnable, millis); //All 10 Seconds more Light
    }

    private void startScreen(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }
    private void startLED(){
        android.hardware.Camera cam = android.hardware.Camera.open();
        android.hardware.Camera.Parameters p = cam.getParameters();
        p.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
        cam.setParameters(p);
        cam.startPreview(); //TODO Listener to Release Cam, Change to not deprecated version
    }
}
