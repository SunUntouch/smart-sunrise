package com.zhun.sununtouch.smart_sunrise;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
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

    private Vibrator m_Vibrator;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setVibrationStop();
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
        int[] music = intentExtras.getIntArray(AlarmConstants.WAKEUP_MUSIC);// Song, StartTime, Volume, FadeIn, FadeInTime, Vibration Aktiv, Vibration Strength
        int[] light = intentExtras.getIntArray(AlarmConstants.WAKEUP_LIGHT);// UseScreen, ScreenBrightness, ScreenStrartTime, Color1, Color2, FadeColor, UseLed, LedStartTime

        //Boolean Values
        //Screen
        boolean useScreen      = (light[0] == 1) ? true : false;
        boolean useLED         = (light[6] == 1) ? true : false;
        boolean useColorFading = (light[5] == 1) ? true  : false;

        //Music
        boolean useFadeIn    = (music[3] == 1) ? true : false;
        boolean useVibration = (music[5] == 1) ? true : false;

        //SCREEN BRIGHTNESS/////////////////////////////////////////////////////////////////////////
        doBrightness(useScreen, useLED, light);

        //SCREEN VIEWS//////////////////////////////////////////////////////////////////////////////
        doViews();

        //SCREEN COLOR FADING///////////////////////////////////////////////////////////////////////
        doColorFading(useColorFading, light);

        //LED///////////////////////////////////////////////////////////////////////////////////////
        doLED(useLED, light);

        //MUSIC FADE IN/////////////////////////////////////////////////////////////////////////////

        //MUSIC VIBRATION///////////////////////////////////////////////////////////////////////////
        doVibrate(useVibration, useScreen, useLED, music, light);
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

    private void doBrightness(boolean _useScreen, boolean _useLED, final int[] _lightValues){

        if(_useScreen){

            if(_lightValues[2] > _lightValues[7])
                setBrightness(_lightValues);
            else {
                android.os.Handler screenLightHandler = new android.os.Handler();
                Runnable screenLightRunnable = new Runnable() {
                    @Override
                    public void run() {
                        setBrightness(_lightValues);
                    }
                };
                screenLightHandler.postDelayed(screenLightRunnable, TimeUnit.MINUTES.toMillis(_lightValues[7] - _lightValues[2]));
            }
        }
        else if(_useLED){

            android.os.Handler screenTimerHandler = new android.os.Handler();
            Runnable screenTimerRunnable = new Runnable() {
                @Override
                public void run() {
                    startScreen();
                }
            };
            screenTimerHandler.postDelayed(screenTimerRunnable, TimeUnit.MINUTES.toMillis(_lightValues[7]));
        }
        else{
            startScreen();
        }
    }
    private void doViews(){
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
    }
    private void doColorFading(boolean _colorFading, final int[] _lightValues){

        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.wakeup_wakescreen_layout);

        if(_colorFading){
            linearLayout.setBackgroundColor(Color.BLACK);
            ObjectAnimator colorFade1 = ObjectAnimator.ofObject(linearLayout,"backgroundColor", new ArgbEvaluator(), Color.BLACK, _lightValues[3]);
            final long duration = TimeUnit.MINUTES.toMillis(_lightValues[2]) / 4;
            colorFade1.setDuration(duration);
            colorFade1.start();

            Handler colorHandler = new Handler();
            Runnable colorRunnable = new Runnable() {
                @Override
                public void run() {

                    ObjectAnimator colorFade2 = ObjectAnimator.ofObject(linearLayout,"backgroundColor", new ArgbEvaluator(), _lightValues[3], _lightValues[4]);
                    colorFade2.setDuration(duration * 3);
                    colorFade2.start();

                }
            };
            colorHandler.postDelayed(colorRunnable, duration );
        }
        else{
            linearLayout.setBackgroundColor(_lightValues[3]);
        }
    }
    private void doLED(boolean _useLED, final int[] _lightValues){
        if(_useLED){
            if(_lightValues[2] > _lightValues[7]) { //If LED StartTime is bigger as LED Start Time

                //New Handler for Waiting Till Time to show LED
                final android.os.Handler handler = new android.os.Handler();

                Runnable runLED = new Runnable() {
                    @Override
                    public void run() {
                        startLED();
                    }
                };

                //Start Delayed
                handler.postDelayed(runLED, TimeUnit.MINUTES.toMillis(_lightValues[2] - _lightValues[7]));
            } else
                startLED();
        }
    }

    private void doFadeInMusic(){
    }
    private void doVibrate(boolean _vibrate, boolean _useScreen, boolean _useLED, final int[] _musicValues, int[] _lightValues){

        if(_useLED || _useScreen){

            if(_vibrate){
                Handler vibrationHandler = new Handler();
                Runnable vibrationRunnable = new Runnable() {
                    @Override
                    public void run() {
                        setVibrationStart(_musicValues[6], 100);
                    }
                };

                long millis = 0;
                if(_useLED && _useScreen)
                    millis = (_lightValues[2] > _lightValues[7]) ?
                            TimeUnit.MINUTES.toMillis(_lightValues[2]) : TimeUnit.MINUTES.toMillis(_lightValues[7]);
                else if(_useScreen)
                    millis = TimeUnit.MINUTES.toMillis(_lightValues[2]);
                else if(_useLED)
                    millis = TimeUnit.MINUTES.toMillis(_lightValues[7]);

                vibrationHandler.postDelayed(vibrationRunnable, millis);
            }
        }
        else
            setVibrationStart(_musicValues[6], 100);

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

    private void setVibrationStart(){
        long[] pattern = { 0, 100, 200, 300, 400 };
        setVibrationStart(pattern, 0);
    }
    private void setVibrationStart(int _intensity, long _duration){
        long[] pattern = generateVibratorpatter(_intensity, _duration);
        setVibrationStart(pattern, 0);
    }
    private void setVibrationStart(long[] _pattern, int _repeat){
        m_Vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        m_Vibrator.vibrate(_pattern, _repeat);
    }

    private long[] generateVibratorpatter(int _intensity, long duration){

        float intensity =  ((float) _intensity) / 100.0f;

        float dutyCycle = Math.abs( (intensity * 2.0f) - 1.0f );

        long hWidth = (long) (dutyCycle * ( duration - 1 )) + 1;
        long lWidth = (dutyCycle == 1.0f)? 0 : 1;

        int pulseCount = (int) (2.0f * ((float) duration / (float) (hWidth + lWidth)));
        long[] pattern = new long[pulseCount];

        for(int i = 0; i < pulseCount; ++i){
            pattern[i] = (intensity < 0.5f) ?
                    ((i % 2 == 0) ? hWidth : lWidth) : ( (i % 2 == 0) ? lWidth : hWidth );
        }

        return pattern;

    }

    private void setVibrationStop(){

        if(m_Vibrator != null){
            m_Vibrator.cancel();
        }
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
