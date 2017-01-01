package com.zhun.sununtouch.smart_sunrise;

import android.animation.Animator;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class AlarmActivity extends AppCompatActivity {

    private AlarmConfiguration config;
    //Private camera Values
    private android.hardware.Camera m_Cam;
    private MediaPlayer mediaPlayer;
    private Vibrator m_Vibrator;

    //API Level 23
    private Handler alarmHandler;

    private AlarmWorkerThread ledThread;
    private AlarmWorkerThread musicThread;

    private boolean snoozed = false;

    private static final int BRIGHTNESS_STEPS = 100;
    private static final int VIBRATION_LENGTH = 10000;

    /***********************************************************************************************
     * ONCREATE AND HELPER
     **********************************************************************************************/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm);
        alarmHandler = new Handler();
        musicThread  = new AlarmWorkerThread("SmartSunrise_Music");
        ledThread    = new AlarmWorkerThread("SmartSunrise_LED");

        //Load Values
        final int actualAlarm = getIntent().getExtras().getInt(AlarmConstants.ALARM_ID);
        config = new AlarmConfiguration(getApplicationContext(), actualAlarm, false);

        //Boolean Values
        final int minuteScreen = (getConfig().useScreen())? getConfig().getScreenStartTime() : 0;
        final int minuteLED    = (getConfig().useLED())   ? getConfig().getLEDStartTime()    : 0;
        final int minutesMax   = (minuteScreen >= minuteLED) ? minuteScreen : minuteLED;

        //SCREEN VIEWS//////////////////////////////////////////////////////////////////////////////
        doViews();

        //Start with Basic parameters
        doPlayMusic(minutesMax);

        //Vibration
//        if(getConfig().useVibration())
//            doVibrate(minutesMax);

        //LED
        if(getConfig().useLED())
            doLED(minutesMax - minuteLED); // ledStartTime

        //SCREEN
        if(getConfig().useScreen())
        {
            final int screenStartTime = minutesMax - minuteScreen;
            doBrightness (screenStartTime);
            doColorFading(screenStartTime);
        }
        else
            startScreen(); //else start the screen

        //Clear Flag
        config.clearAlarmManagerFlag();
        config.commit();
    }
    protected void onStop() {
        //Cancel or Snooze Alarm
        if(snoozed)
            config.snoozeAlarm();
        else
            config.cancelAlarm();

        //Remove Callbacks
        alarmHandler.removeCallbacksAndMessages(null);
        ledThread.removeCallBacks(null);
        musicThread.removeCallBacks(null);


        //Stop Stuff
        stopLED();
        setVibrationStop();
        stopMusic();
        super.onStop();
    }
    protected void onDestroy() {

        ledThread.quit();
        musicThread.quit();
        super.onDestroy();
    }

    private void setRunnable(Runnable runnable, long millis){

        if(millis == 0)
            alarmHandler.post(runnable);
        else
            alarmHandler.postDelayed(runnable, millis);
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

    private AlarmConfiguration getConfig(){
        return config;
    }
    /***********************************************************************************************
     * WAKEUP AND SNOOZE BUTTON
     **********************************************************************************************/
    public void onWakeUpClick(View v){
        this.finish();
    }
    public void onSnoozeClick(View v){
        snoozed = true;
        this.finish();
    }
    /***********************************************************************************************
     * CONSTRUCT VIEWS
     **********************************************************************************************/
    private void doViews(){
        //SetViews
        //Date
        //TODO Set Runnable to Update Date
        Calendar calendar = Calendar.getInstance();
        final TextView dateText = (TextView) findViewById(R.id.wakeup_wakescreen_date);
        dateText.setText(
                getDayName(calendar) + ", " +
                Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)) + "." +
                Integer.toString(calendar.get(Calendar.MONTH) + 1)    + "." +
                Integer.toString(calendar.get(Calendar.YEAR)));

        //TextClock
        final TextClock txtClock = (TextClock) findViewById(R.id.wakeup_timer_wakescreen_clock);
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
    /***********************************************************************************************
     * COLOR FADING
     **********************************************************************************************/
    private void doColorFading( int minutes){

        //Open LinearLayout to Change Color
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.wakeup_wakescreen_layout);
        linearLayout.setBackgroundColor(Color.BLACK);

        //duration for StartScreen Till WakeUp
        final long duration = TimeUnit.MINUTES.toMillis(getConfig().getScreenStartTime() ) / 2;

        //ObjectAnimator
        final ObjectAnimator colorFade1 = ObjectAnimator.ofObject(
                                                linearLayout,
                                                "backgroundColor",
                                                new ArgbEvaluator(),
                                                Color.BLACK,
                                                getConfig().getLightColor1());
        colorFade1.setDuration(duration);

        //Check if Fading is true
        if(getConfig().useLightFade())
        {
            final boolean isFaded = false;
            colorFade1.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }
                @Override
                public void onAnimationEnd(Animator animation) {

                    ObjectAnimator.ofObject
                            (
                                linearLayout,
                                "backgroundColor",
                                new ArgbEvaluator(),
                                getConfig().getLightColor1(),
                                getConfig().getLightColor2()
                            ).setDuration(duration).start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }
                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        colorFade1.setStartDelay(TimeUnit.MINUTES.toMillis(minutes));
        colorFade1.start();
    }

    /***********************************************************************************************
     * BRIGHTNESS
     **********************************************************************************************/
    private void startScreen(){
        //Show Window
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
    }
    private void doBrightness(final int minuteScreenStart){

        setRunnable(new Runnable() {
            @Override
            public void run() {
                setBrightness();
            }
        }, TimeUnit.MINUTES.toMillis(minuteScreenStart) + 1);
    }
    private void setBrightness(){

        //Start The Screen
        startScreen();

        //GetCurrent Layout and Set new Brightness
        final Window window = getWindow();
        window.getAttributes().screenBrightness = 0.0F;
        window.setAttributes(window.getAttributes());

        //time for each step ti illuminate
        final long  millis = (TimeUnit.MINUTES.toMillis(getConfig().getScreenStartTime()) / BRIGHTNESS_STEPS) + 100;  //divide milliseconds with 100 because we have 100 steps till full illumination

        //New Time Handler
        Runnable screenLightRunnable = new Runnable() {

            final float brightness = (float) getConfig().getScreenBrightness() / 100f;
            final float brightnessStep = brightness / BRIGHTNESS_STEPS;

            @Override
            public void run() {
                //get Layout and Update LightValue till Max
                if(window.getAttributes().screenBrightness < brightness){

                    window.getAttributes().screenBrightness += brightnessStep;
                    window.setAttributes(window.getAttributes());
                    setRunnable(this, millis);
                }
            }
        };
        setRunnable(screenLightRunnable, millis);  //All 10 Seconds more Light
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

                    final int musicVolume = config.getVolume();
                    if( currentVolume <= musicVolume){
                        //Set AudioManager
                        final float volume = 1 - (float)(Math.log(100 - currentVolume++)/Math.log(100));
                        mediaPlayer.setVolume(volume, volume);
                        //Set new Runnable
                        setRunnable(musicThread, this, TimeUnit.SECONDS.toMillis(getConfig().getFadeInTime()) / musicVolume);
                    }
                }
            };
            setRunnable(musicThread, musicFadeInRunnable, TimeUnit.MINUTES.toMillis(minutes) + 1);
        }
        else
        {
            setRunnable(musicThread, new Runnable() {
                @Override
                public void run() {
                    final int musicVolume = config.getVolume();
                    float volume= 1 - (float)(Math.log(0)/Math.log(musicVolume));
                    mediaPlayer.setVolume(volume, volume);
                    mediaPlayer.start();
                }
            }, TimeUnit.MINUTES.toMillis(minutes));
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

            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    /***********************************************************************************************
     * VIBRATION
     **********************************************************************************************/
    private void doVibrate(int minutes){

        //Set new Handler
        setRunnable(new Runnable() {
            @Override
            public void run() {
                setVibrationStart(VIBRATION_LENGTH);
            }
        }, TimeUnit.MINUTES.toMillis(minutes));
    }
    private void setVibrationStart(){
        setVibrationStart(new long[]{ 0, 100, 200, 300, 400 }, 0);
    }
    private void setVibrationStart(long _duration){
        setVibrationStart(generateVibratorPattern(getConfig().getVibrationStrength(), _duration), 0);
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
        setRunnable(ledThread, new Runnable() {
            @Override
            public void run() {
                startLED();
            }
        }, TimeUnit.MINUTES.toMillis(minutes));
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

        //Newer API
        if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  //TODO Test with new Version when available
                CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

                try {
                    String[] cameraIds = cameraManager.getCameraIdList();

                    for (String cameraID : cameraIds) {
                        CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);
                        if (cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE))
                            cameraManager.setTorchMode(cameraID, false);
                    }
                } catch (Exception e) {/*TODO Catch Exception*/ }
            }
        }

        //Stop and Release LED
        if(m_Cam != null)
        {
            m_Cam.stopPreview();
            m_Cam.release();
            m_Cam = null;
        }
    }
}
