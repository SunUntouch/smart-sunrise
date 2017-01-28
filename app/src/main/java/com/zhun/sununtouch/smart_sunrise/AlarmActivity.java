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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
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
    PowerManager.WakeLock lock;

    private boolean snoozed = false;
    private static final int BRIGHTNESS_STEPS = 100;  //TODO add to Options

    /***********************************************************************************************
     * ONCREATE AND HELPER
     **********************************************************************************************/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm);
        alarmHandler = new Handler();

        //Load Values
        final int actualAlarm = getIntent().getExtras().getInt(AlarmConstants.ALARM_ID);
        config = new AlarmConfiguration(getApplicationContext(), actualAlarm);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        lock.acquire();

        //Open Screen
        startScreen(true);

        //Boolean Values
        boolean changed = false;

        //Get current Times
        int minuteScreen, minuteLED, minutesMax;
        if(getConfig().getTemporaryTimes())
        {
            changed = true;
            minuteScreen = (getConfig().getScreen())? getConfig().getScreenStartTemp() : 0;
            minuteLED    = (getConfig().getLED())   ? getConfig().getLEDStartTemp()    : 0;

            //Delete Temporary Flag if Set
            config.setTemporaryTimes(false);
        }
        else
        {
            minuteScreen = (getConfig().getScreen())? getConfig().getScreenStartTime() : 0;
            minuteLED    = (getConfig().getLED())   ? getConfig().getLEDStartTime()    : 0;
        }

        minutesMax   = (minuteScreen >= minuteLED) ? minuteScreen : minuteLED;

        //Check if this is a OneShot Alarm
        if(getConfig().getAlarmOneShot())
        {
            changed = true;
            config.setAlarmOneShot(false);
        }
        //Commit Changes
        if(changed)
            config.commit();

        //SCREEN VIEWS//////////////////////////////////////////////////////////////////////////////
        doViews();

        //Do the Other Stuff
        startAlarmProcedure(minutesMax, minuteScreen, minuteLED);
    }
    protected void onStop() {
        //Cancel or Snooze Alarm
        if(snoozed)
            config.snoozeAlarm();
        else if(getConfig().isDaySet())
            config.refreshAlarm();
        else
            config.cancelAlarm();

        //Stop Stuff
        stopLED();
        setVibrationStop();
        stopMusic();

        //Kill Threads
        if(musicThread != null)
        {
            musicThread.removeCallBacks(null);
            musicThread.quit();
        }
        if(vibrationThread != null)
        {
            vibrationThread.removeCallBacks(null);
            vibrationThread.quit();
        }

        //Remove Callbacks
        alarmHandler.removeCallbacksAndMessages(null);

        //release Activity
        if(lock.isHeld())
            lock.release();

        super.onStop();
    }

    private AlarmConfiguration getConfig(){
        return config;
    }

    /***********************************************************************************************
     * AsyncTasks and Threads
     **********************************************************************************************/
    private class BrightnessAsyncTask extends AsyncTask<Void, Float, Void>{

        final long screenFadeTime;
        final float brightness;

        BrightnessAsyncTask(Integer screenBrightness, Long screenfade) {
            super();
            screenFadeTime = screenfade;
            brightness = (float)screenBrightness / 100f;
        }
        @Override
        protected Void doInBackground(Void... params) {

            float currentBrightness =  brightness;
            if(screenFadeTime > 0)
            {
                //time for each step ti illuminate
                final long  millis = screenFadeTime / BRIGHTNESS_STEPS;  //divide milliseconds with 100 because we have 100 steps till full illumination

                //get Layout and Update LightValue till Max
                currentBrightness = 0f;
                while(currentBrightness <= brightness)
                {
                    publishProgress(currentBrightness);
                    currentBrightness += brightness / BRIGHTNESS_STEPS;
                    SystemClock.sleep(millis);
                }
            }
            else
                publishProgress(currentBrightness);
            return null;
        }
        @Override
        protected void onProgressUpdate(Float... values) {
            super.onProgressUpdate(values);
            setBrightness(values[0]);
        }
    }
    private class LEDAsyncTask extends AsyncTask<Void, Void, Void>{

        final long ledDelay;
        LEDAsyncTask(Long delay) {
            super();
            ledDelay = delay;
        }
        @Override
        protected Void doInBackground(Void... params) {

            if(ledDelay > 0)
                SystemClock.sleep(ledDelay);
            startLED();
            return null;
        }
    }

    private void startAsyncTask(BrightnessAsyncTask task, boolean parallel){
        if(parallel)
            AsyncTaskCompat.executeParallel(task); //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }
    private void startAsyncTask(LEDAsyncTask task, boolean parallel){
        if(parallel)
            AsyncTaskCompat.executeParallel(task); //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }

    BrightnessAsyncTask brightnessAsyncTask;
    LEDAsyncTask ledAsyncTask;

    private AlarmWorkerThread musicThread;
    private AlarmWorkerThread vibrationThread;
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
        //final TextClock txtClock = (TextClock) findViewById(R.id.wakeup_timer_wakescreen_clock);
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
     * COLOR FADING AND BRIGHTNESS
     **********************************************************************************************/
    private void startAlarmProcedure(final int minutes, final int fadeTime, final int ledTime){

        //duration for StartScreen Till WakeUp
        final long fadingMillis = TimeUnit.MINUTES.toMillis(fadeTime);
        final long duration = (getConfig().getLightFade()) ?  fadingMillis / 2 : fadingMillis;

        //Open LinearLayout to Change Color assign to ObjectAnimator
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.wakeup_wakescreen_layout);
        linearLayout.setBackgroundColor(Color.BLACK);

        //FadeObject starting Music and Vibration at the End of the Animation
        ObjectAnimator colorFade1 = ObjectAnimator.ofObject(
                                                linearLayout,
                                                "backgroundColor",
                                                new ArgbEvaluator(),
                                                Color.BLACK,
                                                getConfig().getLightColor1());
        colorFade1.setDuration(duration);
        colorFade1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                startScreen(false);
                if(getConfig().getScreen())
                    doBrightness(fadingMillis);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                //Check if Fading is true
                if(getConfig().getLightFade())
                {
                    ObjectAnimator fadeObject  = ObjectAnimator.ofObject(
                                        linearLayout,
                                        "backgroundColor",
                                        new ArgbEvaluator(),
                                        getConfig().getLightColor1(),
                                        getConfig().getLightColor2());
                    fadeObject.setDuration(duration);
                    fadeObject.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            startAction();
                        }
                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }
                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    });
                    fadeObject.start();
                }
                else
                    startAction();
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        final int screenStartTime = minutes - fadeTime;
        if(screenStartTime > 0)
            colorFade1.setStartDelay(TimeUnit.MINUTES.toMillis(screenStartTime) + 1);
        colorFade1.start();

        //LED
        if(getConfig().getLED())
            doLED(minutes - ledTime);
    }
    void startAction(){
        doPlayMusic(0);
        doVibrate(0);
    }

    /***********************************************************************************************
     * BRIGHTNESS
     **********************************************************************************************/
    private void startScreen(boolean initial){
        if(!initial)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }

        else
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                 WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                 WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                                 WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                                 WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
    }
    private void setBrightness(final float brightness){
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = brightness;
        getWindow().setAttributes(layout);
    }
    private void doBrightness(final long screenStart){

        brightnessAsyncTask = new BrightnessAsyncTask(
                getConfig().getScreenBrightness(),
                screenStart);
        startAsyncTask(brightnessAsyncTask, true);
    }

    /***********************************************************************************************
     * MUSIC
     **********************************************************************************************/
    private int currentVolume = 0;
    private void doPlayMusic(int minutes){ // StartTime, Volume, FadeIn, FadeInTime, Vibration Aktiv, Vibration Strength

        try { prepareMusic(getConfig().getSongURI()); }
        catch (IOException e) { Log.e("Exception: ", e.getMessage()); }

        //Start Runnable and Thread
        musicThread  = new AlarmWorkerThread("SmartSunrise_Music");
        setRunnable(musicThread, new Runnable() {
            @Override
            public void run() {
                if(!mediaPlayer.isPlaying()){

                    //Get MaxVolume of Music
                    final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

                    mediaPlayer.seekTo((int) TimeUnit.SECONDS.toMillis(getConfig().getSongStart()));
                    mediaPlayer.setVolume(currentVolume, currentVolume);
                    mediaPlayer.start();
                }

                final int musicVolume = getConfig().getVolume();
                final boolean fadeIn  = getConfig().getFadeIn();
                if( currentVolume <= musicVolume || !fadeIn){
                    //Set AudioManager
                    float volume = (fadeIn) ?
                            1 - (float)(Math.log(100 - currentVolume++)/Math.log(100)) :
                            1 - (float)(Math.log(100 - musicVolume    )/Math.log(100));
                    mediaPlayer.setVolume(volume, volume);
                    //Set new Runnable
                    if(fadeIn)
                        setRunnable(musicThread, this, TimeUnit.SECONDS.toMillis(getConfig().getFadeInTime()) / musicVolume);
                }
            }
        }, TimeUnit.MINUTES.toMillis(minutes) + 1);
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

        if(getConfig().getVibration())
        {
            vibrationThread = new AlarmWorkerThread("SmartSunrise_Vibration");
            //Set new Handler
            setRunnable(vibrationThread, new Runnable() {
                @Override
                public void run() {
                    setVibrationStart(0);
                }
            }, TimeUnit.MINUTES.toMillis(minutes));
        }
    }
    private void setVibrationStart(int repeat){
        //Start without delay,
        //Vibrate fpr milliseconds
        //Sleep for milliseconds
        if(m_Vibrator == null)
            m_Vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        m_Vibrator.vibrate(new long[]{0, 8 * getConfig().getVibrationStrength() + 350, (4000 / (long) Math.sqrt(getConfig().getVibrationStrength() + 1))}, repeat);
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
        ledAsyncTask = new LEDAsyncTask(TimeUnit.MINUTES.toMillis(minutes));
        startAsyncTask(ledAsyncTask, true);
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
                }
                catch (Exception e){/*TODO Catch Exception*/ }
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
