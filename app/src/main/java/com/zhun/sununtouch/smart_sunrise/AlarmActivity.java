package com.zhun.sununtouch.smart_sunrise;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AlarmActivity extends AppCompatActivity {

    private AlarmConfiguration config;
    //Private camera Values
    private android.hardware.Camera m_Cam;
    private MediaPlayer mediaPlayer;
    private Vibrator m_Vibrator;

    //API Level 23
    private int actualAlarm = -1;

    private boolean mUseThread = true;
    private Handler alarmHandler;

    private AlarmWorkerThread alarmThread;

    private boolean snoozed = false;
    @Override
    protected void onDestroy() {
        alarmThread.quit();
        super.onDestroy();
    }

    @Override
    protected void onStop() {

        if(!snoozed){
            AlarmManage newAlarm = new AlarmManage(this, getConfig());
            newAlarm.cancelAlarm(actualAlarm);
        }

        alarmHandler.removeCallbacksAndMessages(null);

        if(mUseThread)
            alarmThread.removeCallBacks(null);

        stopLED();
        setVibrationStop();
        stopMusic();

        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm);
        alarmHandler = new Handler();
        alarmThread = new AlarmWorkerThread("AlarmThread");

        //Load Values
        actualAlarm = getIntent().getExtras().getInt(AlarmConstants.ALARM_ID);
        config = new AlarmConfiguration(getApplicationContext(), actualAlarm);

        //Boolean Values
        int minuteScreen = (getConfig().useScreen())? getConfig().getScreenStartTime() : 0;
        int minuteLED    = (getConfig().useLED())   ? getConfig().getLEDStartTime()    : 0;

        int minutesMax   = (minuteScreen >= minuteLED) ? minuteScreen : minuteLED;

        //SCREEN VIEWS//////////////////////////////////////////////////////////////////////////////
        doViews();
        doPlayMusic(minutesMax);
        if(getConfig().useVibration())
            doVibrate(minutesMax);

        if(getConfig().useScreen())
        {
            doBrightness (minutesMax - minuteScreen);
            doColorFading(minutesMax - minuteScreen, minuteScreen);

            doLED(minutesMax - minuteLED);
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

    private AlarmConfiguration getConfig(){
        return config;
    }

    private void setRunnable(Runnable runnable, long millis){
        setRunnable(false, runnable, millis);
    }
    private void setRunnable(boolean useThread, Runnable runnable, long millis){

        if(!useThread)
        {
            if(millis == 0)
                alarmHandler.post(runnable);
            else
                alarmHandler.postDelayed(runnable, millis);
        }
        else
            setRunnable(alarmThread, runnable, millis);
    }
    private void setRunnable(AlarmWorkerThread thread, Runnable runnable, long millis){

    if(!thread.isAlive())
    {
        thread.start();
        thread.prepareHandler();
    }

    if(millis == 0)
        thread.postTask(runnable);
    else
        thread.postDelayedTask(runnable, millis);
}
    /***********************************************************************************************
     * CONSTRUCT VIEWS
     **********************************************************************************************/
    private void doViews(){
        //SetViews
        //Date
        Calendar calendar = Calendar.getInstance();
        int currentDay   = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH) + 1; //Is Zero based January is 0
        int currentYear  = calendar.get(Calendar.YEAR);

        final TextView dateText = (TextView) findViewById(R.id.wakeup_wakescreen_date);
        dateText.setText(
                getDayName(calendar) + ", " +
                Integer.toString(currentDay)   + "." +
                Integer.toString(currentMonth) + "." +
                Integer.toString(currentYear));

        int currentHour   = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        final TextView timeText = (TextView) findViewById(R.id.wakeup_timer_wakescreen_textview);
        timeText.setText(String.format(Locale.US, "%02d:%02d", currentHour, currentMinute));

        //Set new Handler for Updating Date and Time
        final Runnable timeRunnable = new Runnable() {
            @Override
            public void run() {

                setRunnable(this, 100); //1 second check for new Time

                //Update Time
                Calendar calendarNew = Calendar.getInstance();
                timeText.setText(String.format(Locale.US, "%02d:%02d", calendarNew.get(Calendar.HOUR_OF_DAY), calendarNew.get(Calendar.MINUTE)));
                //Update Date
                dateText.setText(
                        getDayName(calendarNew) + ", " +
                        Integer.toString(calendarNew.get(Calendar.DAY_OF_MONTH)) + "." +
                        Integer.toString(calendarNew.get(Calendar.MONTH) + 1)    + "." +
                        Integer.toString(calendarNew.get(Calendar.YEAR)));
            }
        };
        setRunnable(timeRunnable, 100);//1 second check for new Time
    }

    /***********************************************************************************************
     * COLOR FADING
     **********************************************************************************************/
    private void doColorFading( int minutes, final int minutesScreen){

        Runnable initialColorFadeRunnable = new Runnable() {
            @Override
            public void run() {
                changeColors(getConfig().getLightFade() == 1, minutesScreen);
            }
        };
        setRunnable( initialColorFadeRunnable, TimeUnit.MINUTES.toMillis(minutes));
    }
    private void changeColors(boolean _colorFading, int _minutesScreen){

        //Open LinearLayout to Change Color
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.wakeup_wakescreen_layout);
        linearLayout.setBackgroundColor(Color.BLACK);

        //duration for StartScreen Till WakeUp
        long duration = TimeUnit.MINUTES.toMillis(_minutesScreen ) / 4;

        //ObjectAnimator
        ObjectAnimator colorFade1 = ObjectAnimator.ofObject(linearLayout,"backgroundColor", new ArgbEvaluator(), Color.BLACK, getConfig().getLightColor1());
        colorFade1.setDuration(duration);
        colorFade1.start();

        //Check if Fading is true
        if(_colorFading)
        {
            //Change Color a Second Time
            final ObjectAnimator colorFade2 = ObjectAnimator.ofObject(linearLayout,"backgroundColor", new ArgbEvaluator(), getConfig().getLightColor1(), getConfig().getLightColor2());
            colorFade2.setDuration(duration * 3);
            //Second ColorHandler
            Runnable colorRunnable = new Runnable() {
                @Override
                public void run() {
                    colorFade2.start();
                }
            };

            setRunnable(colorRunnable, duration + 1);
        }
    }
    /***********************************************************************************************
     * BRIGHTNESS
     **********************************************************************************************/
    private void startScreen(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }
    private void doBrightness(int minuteScreenStart){

        Runnable screenTimerRunnable = new Runnable() {
            @Override
            public void run() {
                setBrightness(getConfig().getScreenStartTime(), getConfig().getScreenBrightness());
            }
        };
        setRunnable(screenTimerRunnable, TimeUnit.MINUTES.toMillis(minuteScreenStart));
    }
    private void setBrightness(int _screenStartTime, int _screenBrightness){

        //Start with Basic parameters
        startScreen();

        //GetCurrent Layout and Set new Brightness
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 0.0F;
        getWindow().setAttributes(layout);

        //time for each step ti illuminate
        final float brightness = (float) _screenBrightness / 100;
        if(_screenStartTime > 0){
            final long  millis = ( TimeUnit.MINUTES.toMillis(_screenStartTime) == 0) ? 0 : TimeUnit.MINUTES.toMillis(_screenStartTime) / 100;  //divide milliseconds with 100 because we have 100 steps till full illumination
            //New Time Handler
            Runnable screenLightRunnable = new Runnable() {
                @Override
                public void run() {

                //get Layout and Update LightValue till Max
                WindowManager.LayoutParams layoutNew = getWindow().getAttributes();
                if(layoutNew.screenBrightness < brightness){

                    layoutNew.screenBrightness += 0.01F;
                    getWindow().setAttributes(layoutNew);
                    setRunnable(this, millis);
                }
                }
            };
            setRunnable(screenLightRunnable, millis);  //All 10 Seconds more Light
        }
        else{
            //get Layout and Update LightValue to Max
            WindowManager.LayoutParams layoutNew = getWindow().getAttributes();
            layoutNew.screenBrightness = brightness;
            getWindow().setAttributes(layoutNew);
        }
    }

    /***********************************************************************************************
     * MUSIC
     **********************************************************************************************/
    private int currentVolume;
    private void doPlayMusic(int minutes){ // StartTime, Volume, FadeIn, FadeInTime, Vibration Aktiv, Vibration Strength

        try { prepareMusic(getConfig().getSongURI()); }
        catch (IOException e) { Log.e("Exception: ", e.getMessage()); }

        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        //Get MaxVolume of Music
        final int maxVolumeAndroid = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolumeAndroid, 0);

        mediaPlayer.seekTo((int) TimeUnit.SECONDS.toMillis(getConfig().getSongStart()));
        if(getConfig().useFadeIn())
        {
            currentVolume = 0;
            mediaPlayer.setVolume(currentVolume, currentVolume);
            Runnable musicFadeInRunnable = new Runnable() {
                @Override
                public void run() {

                    if(!mediaPlayer.isPlaying())
                        mediaPlayer.start();

                    int musicVolume = config.getVolume();
                    if( currentVolume <= musicVolume){
                        //Get Time For Fading and Sequenze to 15 Steps
                        long millis =  TimeUnit.SECONDS.toMillis(getConfig().getFadeInTime());
                        long millis_steps = millis / 100;

                        //Set AudioManager
                        float volume= 1 - (float)(Math.log(musicVolume-currentVolume)/Math.log(musicVolume));
                        mediaPlayer.setVolume(volume, volume);
                        currentVolume +=1;

                        setRunnable(mUseThread, this, millis_steps);
                    }
                }
            };

            setRunnable(mUseThread, musicFadeInRunnable, TimeUnit.MINUTES.toMillis(minutes) + 1);
        }
        else
        {
            Runnable musicPlayRunnable = new Runnable() {
                @Override
                public void run() {

                    int musicVolume = config.getVolume();
                    float volume= 1 - (float)(Math.log(0)/Math.log(musicVolume));
                    mediaPlayer.setVolume(volume, volume);
                    mediaPlayer.start();
                }
            };
            setRunnable(mUseThread, musicPlayRunnable, TimeUnit.MINUTES.toMillis(minutes));
        }
    }
    private void prepareMusic(String _SongUri) throws IOException {

        //Prepare Music Stream
        if(mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        else
            mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(_SongUri));
        mediaPlayer.setLooping(true);
        mediaPlayer.prepare();
    }

    private void stopMusic(){

        //Stop and Release Music
        if(mediaPlayer!=null){

            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();

            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /***********************************************************************************************
     * VIBRATION
     **********************************************************************************************/
    private void doVibrate(int minutes){

        //Set new Handler
        Runnable vibrationRunnable = new Runnable() {
            @Override
            public void run() {
                setVibrationStart(getConfig().getVibrationStrength(), 1000);
            }
        };
        setRunnable(mUseThread, vibrationRunnable, TimeUnit.MINUTES.toMillis(minutes));
    }
    private void setVibrationStart(){
        long[] pattern = { 0, 100, 200, 300, 400 };
        setVibrationStart(pattern, 0);
    }
    private void setVibrationStart(int _intensity, long _duration){
        long[] pattern = generateVibratorPattern(_intensity, _duration);
        setVibrationStart(pattern, 0);
    }
    private void setVibrationStart(long[] _pattern, int _repeat){
        m_Vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        m_Vibrator.vibrate(_pattern, _repeat);
    }
    private long[] generateVibratorPattern(int _intensity, long duration){

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

        //Cancel and Release Vibrator
        if(m_Vibrator != null)
        {
            m_Vibrator.cancel();
            m_Vibrator = null;
        }
    }

    /***********************************************************************************************
     * LED
     **********************************************************************************************/
    private void doLED(int minutes){

        //New Handler for Waiting Till Time to show LED
        Runnable runLED = new Runnable() {
            @Override
            public void run() {
                startLED();
            }
        };
        //Start Delayed
        setRunnable(mUseThread, runLED, TimeUnit.MINUTES.toMillis(minutes));
    }
    private void startLED(){

        if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {  //TODO Test with new Version when available
                CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

                try
                {
                    String[] cameraIds = cameraManager.getCameraIdList();

                    for(String cameraID : cameraIds)
                    {
                        CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);
                        if(cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE))
                            cameraManager.setTorchMode(cameraID, true);
                    }
                } catch (Exception e){/*TODO Catch Exception*/ }
            }
            else
            {
                //Start new Cam
                m_Cam = android.hardware.Camera.open();

                //Load Parameters and Set Parameters
                android.hardware.Camera.Parameters p = m_Cam.getParameters();
                p.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
                m_Cam.setParameters(p);

                //Start LED
                m_Cam.startPreview();
            }
        }
    }
    private void stopLED(){

        //Stop and Release LED
        if(m_Cam != null)
        {
            m_Cam.stopPreview();
            m_Cam.release();
            m_Cam = null;
        }
    }

    /***********************************************************************************************
     * WAKEUP AND SNOOZE BUTTON
     **********************************************************************************************/
    public void onWakeUpClick(View v){
        this.finish();
    }

    public void onSnoozeClick(View v){

        AlarmManage newAlarm = new AlarmManage(this, config);
        snoozed = true;
        newAlarm.snoozeAlarm(actualAlarm);
        this.finish();
    }
}
