package com.zhun.sununtouch.smart_sunrise;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.os.Vibrator;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhun.sununtouch.smart_sunrise.Alarm.AlarmIntentService;
import com.zhun.sununtouch.smart_sunrise.Alarm.AlarmWorkerThread;
import com.zhun.sununtouch.smart_sunrise.Configuration.AlarmConfiguration;
import com.zhun.sununtouch.smart_sunrise.Configuration.AlarmLogging;
import com.zhun.sununtouch.smart_sunrise.Configuration.AlarmSystemConfiguration;
import com.zhun.sununtouch.smart_sunrise.Information.AlarmConstants;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class AlarmActivity extends AppCompatActivity {

    private AlarmConfiguration config;
    private AlarmSystemConfiguration systemConfig;
    private AlarmLogging m_Log;

    //Private camera Values, suppress Warnings because we handle the deprecation
    @SuppressWarnings("deprecation")
    private android.hardware.Camera m_Cam; //For old Android
    private MediaPlayer mediaPlayer;
    private Vibrator m_Vibrator;

    //API Level 23
    private Handler alarmHandler;
    private PowerManager.WakeLock lock;

    private boolean snoozed = false;
    private String TAG = "AlarmActivity";

    /***********************************************************************************************
     * ON_CREATE AND HELPER
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm);
        alarmHandler = new Handler();

        //Load Values
        final int actualAlarm = getIntent().getExtras().getInt(AlarmConstants.ALARM_ID);
        config = new AlarmConfiguration(getApplicationContext(), actualAlarm);
        systemConfig = new AlarmSystemConfiguration(getApplicationContext());

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmActivity_WakeLock");
        lock.acquire();

        //Get Logging Helper
        m_Log = new AlarmLogging(getApplicationContext());
        m_Log.i(TAG, getString(R.string.logging_activity_creating));

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
        m_Log.i(TAG, getString(R.string.logging_activity_created));
    }
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        m_Log.i(TAG, getString(R.string.logging_activity_hint));
        this.finish();
    }
    @Override
    protected void onStop() {
        m_Log.i(TAG, getString(R.string.logging_activity_stopped));
        super.onStop();
    }
    @Override
    protected void onDestroy() {

        //Cancel or Snooze Alarm
        if(snoozed)
            config.snoozeAlarm();
        else if(getConfig().isDaySet())
            config.refreshAlarm();
        else
            config.cancelAlarm();

        //Stop Stuff
        enableLED(false);
        setVibrationStop();
        stopMusic();
        stopAnimation();

        //Kill Threads
        if(dateThread != null)
        {
            dateThread.removeCallBacks(null);
            dateThread.interrupt();
            dateThread.quit();
            dateThread = null;
        }
        if(musicThread != null)
        {
            musicThread.removeCallBacks(null);
            musicThread.interrupt();
            musicThread.quit();
            musicThread = null;
        }
        if(vibrationThread != null)
        {
            vibrationThread.removeCallBacks(null);
            vibrationThread.interrupt();
            vibrationThread.quit();
            vibrationThread = null;
        }

        if(brightnessTask != null)
            brightnessTask.cancel(true);

        //Remove Callbacks
        alarmHandler.removeCallbacksAndMessages(null);

        m_Log.i(TAG, getString(R.string.logging_activity_destroyed));

        //release Activity
        if(lock.isHeld())
            lock.release();

        stopService(new Intent(getApplicationContext(), AlarmIntentService.class));
        super.onDestroy();
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
        float currentBrightness =  0.0f;
        final Handler brightnessHandler = new Handler();
        final String taskTag = "Brightness";

        BrightnessAsyncTask(Integer screenBrightness, Long screenFade) {
            super();
            screenFadeTime = screenFade;
            brightness = (screenBrightness >= 100) ? 1f : (float)screenBrightness / 100f;

            m_Log.i(TAG, getString(R.string.logging_asyncTask_created, taskTag));
        }
        @Override
        protected Void doInBackground(Void... params) {

            if(screenFadeTime > 0)
            {
                //time for each step ti illuminate
                final long  millis = screenFadeTime / systemConfig.getBrightnessSteps();  //divide milliseconds with 100 because we have 100 steps till full illumination
                final float brightnessSteps = brightness / systemConfig.getBrightnessSteps();
                //get Layout and Update LightValue till Max
                currentBrightness = 0f;
                publishProgress(currentBrightness);
                setRunnable(brightnessHandler, new Runnable() {
                    @Override
                    public void run() {
                        currentBrightness += brightnessSteps;
                        publishProgress((currentBrightness > brightness)? brightness : currentBrightness);
                        if(currentBrightness < brightness)
                            setRunnable(brightnessHandler, this, millis);
                    }
                }, millis);
            }
            else
                publishProgress(brightness);
            return null;
        }
        @Override
        protected void onProgressUpdate(Float... values) {
            super.onProgressUpdate(values);
            setBrightness(values[0]);
        }
        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);

            if(brightnessHandler != null)
                brightnessHandler.removeCallbacksAndMessages(null);
            m_Log.i(TAG, getString(R.string.logging_asyncTask_destroyed, taskTag));
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            m_Log.i(TAG, getString(R.string.logging_asyncTask_finished, taskTag));
            super.onPostExecute(aVoid);
        }
    }
    private void startAsyncTask(BrightnessAsyncTask task, boolean parallel){
        if(parallel)
            AsyncTaskCompat.executeParallel(task); //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
        m_Log.i(TAG, getString(R.string.logging_asyncTask_started, task.taskTag));
    }

    private AlarmWorkerThread dateThread;
    private AlarmWorkerThread musicThread;
    private AlarmWorkerThread vibrationThread;
    private void setRunnable(final AlarmWorkerThread thread, Runnable runnable, long millis){

        if(thread == null || runnable == null)
            return;

        if(!thread.isAlive())
        {
            if(thread.isInterrupted())
                thread.quit();
            thread.start();
            thread.prepareHandler();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_Log.i(TAG, getString(R.string.logging_runnable_set, thread.getName()));
                }
            });
        }

        if(millis == 0)
            thread.postTask(runnable);
        else
            thread.postDelayedTask(runnable, millis);
    }

    private void setRunnable(Handler handler, Runnable runnable, long millis){
        setRunnable(handler, runnable, millis, false);
    }
    private void setRunnable(Handler handler, Runnable runnable, long millis, boolean exact){

        if(handler == null || runnable == null)
            return;

        if(millis == 0)
            handler.post(runnable);
        else if(exact)
            handler.postAtTime(runnable, millis);
        else
            handler.postDelayed(runnable, millis);
    }
    /***********************************************************************************************
     * WAKEUP AND SNOOZE BUTTON
     **********************************************************************************************/
    @SuppressWarnings("UnusedParameters")
    public void onWakeUpClick(View v){
        m_Log.i(TAG, getString(R.string.logging_user_stop));
        this.finish();
    }
    @SuppressWarnings("UnusedParameters")
    public void onSnoozeClick(View v){
        snoozed = true;
        m_Log.i(TAG, getString(R.string.logging_user_snooze));
        this.finish();
    }
    /***********************************************************************************************
     * CONSTRUCT VIEWS
     **********************************************************************************************/
    private void doViews(){
        //Date
        dateThread  = new AlarmWorkerThread(AlarmConstants.ACTIVITY_DATE_THREAD);
        setRunnable(dateThread, new Runnable() {
            @Override
            public void run() {
                final Calendar calendar = Calendar.getInstance();
                AlarmActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView)findViewById(R.id.wakeup_wakescreen_date)).setText(
                                getConfig().getDayName(calendar.get(Calendar.DAY_OF_WEEK), true) + ", " +
                                Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)) + "." +
                                Integer.toString(calendar.get(Calendar.MONTH) + 1)    + "." +
                                Integer.toString(calendar.get(Calendar.YEAR)));
                    }
                });
                setRunnable(dateThread, this, 1000);
            }
        }, 0);

        //TextClock
        //final TextClock txtClock = (TextClock) findViewById(R.id.wakeup_timer_wakescreen_clock);

        m_Log.i(TAG, getString(R.string.logging_view_time_build));
    }
    /***********************************************************************************************
     * COLOR FADING AND BRIGHTNESS
     **********************************************************************************************/
    private final AnimatorSet fadeAnimationSet = new AnimatorSet();
    private void startAlarmProcedure(final int minutes, final int fadeTime, final int ledTime){
        //Add Animations to new Set
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.wakeup_wakescreen_layout);
        linearLayout.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.DARKEN);
        ObjectAnimator fadeObject = ObjectAnimator.ofObject(
                            linearLayout,
                            "backgroundColor",
                            new ArgbEvaluator(),
                            Color.BLACK,
                            getConfig().getLightColor1());

        if(getConfig().getLightFade())
            fadeAnimationSet.playSequentially(fadeObject, ObjectAnimator.ofObject(
                                                                            linearLayout,
                                                                            "backgroundColor",
                                                                            new ArgbEvaluator(),
                                                                            getConfig().getLightColor1(),
                                                                            getConfig().getLightColor2()));
        else
            fadeAnimationSet.playSequentially(fadeObject);

        //Set all Values, FadeObject starting Music and Vibration at the End of the Animation
        final long fadingMillis = TimeUnit.MINUTES.toMillis(fadeTime);
        final long fadingDelay = TimeUnit.MINUTES.toMillis(minutes - fadeTime);
        fadeAnimationSet.setTarget(linearLayout);
        fadeAnimationSet.setStartDelay(fadingDelay); //screenStartTime = minutes - fadeTime
        fadeAnimationSet.setDuration((getConfig().getLightFade()) ?  fadingMillis / 2 : fadingMillis);
        fadeAnimationSet.addListener(new Animator.AnimatorListener() {

            boolean cancelled = false;

            @Override
            public void onAnimationStart(Animator animation) {

                startScreen(false);
                setBrightness(0.0f);
                setRunnable(alarmHandler, new Runnable(){
                    @Override
                    public void run() {
                        //Only Called once the AnimatorSet is started
                        if(getConfig().getScreen())
                            doBrightness(fadingMillis);
                    }
                }, fadingDelay);
                m_Log.i(TAG, getString(R.string.logging_view_animation_start, fadingDelay));
            }
            @Override
            public void onAnimationEnd(Animator animation) {

                m_Log.i(TAG, getString(R.string.logging_view_animation_end));

                //Called after every Animation in the AnimatorSet
                //Check for More Animations
                if(cancelled || fadeAnimationSet.getChildAnimations().size() == 1 && getConfig().getFadeIn())
                    return;

                startAction();
                animation.removeAllListeners();
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
                animation.removeAllListeners();
                m_Log.i(TAG, getString(R.string.logging_view_animation_stopped));
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        fadeAnimationSet.start();

        //Start LED Timer
        if(getConfig().getLED())
            doLED(minutes - ledTime);

        m_Log.i(TAG, getString(R.string.logging_view_procedure_build));
    }
    private void startAction(){
        doPlayMusic(0);
        doVibrate(0);
    }
    private void stopAnimation(){
        fadeAnimationSet.cancel();
    }
    /***********************************************************************************************
     * BRIGHTNESS
     **********************************************************************************************/
    private void startScreen(boolean initial){
        if(!initial)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        else
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        m_Log.i(TAG, getString(R.string.logging_view_screen_start, initial));
    }
    private void startScreen(Window win, boolean initial){
        if(!initial)
            win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        else
            win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        m_Log.i(TAG, getString(R.string.logging_view_screen_start, initial));
    }
    private void setBrightness(final float brightness){
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = brightness;
        getWindow().setAttributes(layout);

        m_Log.i(TAG, getString(R.string.logging_brightness_set, brightness));
    }

    private BrightnessAsyncTask brightnessTask;
    private void doBrightness(final long screenStart){

        //New Handler for Waiting Till Time to Screen and Brightness
        brightnessTask = new BrightnessAsyncTask(
                                getConfig().getScreenBrightness(),
                                screenStart);
        startAsyncTask(brightnessTask, true);
    }
    /***********************************************************************************************
     * MUSIC
     **********************************************************************************************/
    private int currentVolume = 0;
    private void doPlayMusic(int minutes){

        m_Log.i(TAG, getString(R.string.logging_start_music, getConfig().getSongURI()));

        //Prepare Music player
        try { prepareMusic(getConfig().getSongURI()); }
        catch (IOException e) { m_Log.e(TAG, getString(R.string.logging_exception_io, "prepareMusic",e.getMessage())); }

        //Start Runnable and Thread
        musicThread  = new AlarmWorkerThread(AlarmConstants.ACTIVITY_MUSIC_THREAD);
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


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            m_Log.i(TAG, getString(R.string.logging_music_playing));
                        }
                    });
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
        if(mediaPlayer==null)
            return;

        if(mediaPlayer.isPlaying())
            mediaPlayer.stop();

        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;

        m_Log.i(TAG, getString(R.string.logging_music_stopped));
    }
    /***********************************************************************************************
     * VIBRATION
     **********************************************************************************************/
    private void doVibrate(int minutes){
        if(!getConfig().getVibration())
            return;

        //Set new Handler
        vibrationThread = new AlarmWorkerThread(AlarmConstants.ACTIVITY_VIBRATION_THREAD);
        setRunnable(vibrationThread, new Runnable() {
            @Override
            public void run() {
                setVibrationStart(0);
            }
        }, TimeUnit.MINUTES.toMillis(minutes));
    }
    private void setVibrationStart(int repeat){
        //Start without delay,
        //Vibrate fpr milliseconds
        if(m_Vibrator == null)
            m_Vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        m_Vibrator.vibrate(new long[]{0, 8 * getConfig().getVibrationStrength() + 350, (4000 / (long) Math.sqrt(getConfig().getVibrationStrength() + 1))}, repeat);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_Log.i(TAG, getString(R.string.logging_vibration_started));
            }
        });
    }
    private void setVibrationStop(){
        //Cancel and Release Vibrator
        if(m_Vibrator == null)
            return;
        m_Vibrator.cancel();
        m_Vibrator = null;

        m_Log.i(TAG, getString(R.string.logging_vibration_stopped));
    }
    /***********************************************************************************************
     * LED
     **********************************************************************************************/
    private void doLED(int minutes){
        setRunnable(alarmHandler, new Runnable() {
            @Override
            public void run() {
                enableLED(true);
            }
        }, (minutes != 0) ?  TimeUnit.MINUTES.toMillis(minutes) : 1000);
    }
    @SuppressWarnings("deprecation")
    private void enableLED(final boolean enable){
        //Check if Device has a LED
        if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
        {
            //Check for Build Version
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                try{
                    CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                    final String[] cameraIds = cameraManager.getCameraIdList();
                    for(String cameraID : cameraIds)
                    {
                        final CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);
                        final Boolean hasTorch = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                        if(hasTorch != null && hasTorch)
                            cameraManager.setTorchMode(cameraID, enable);
                    }
                }
                catch (final Exception e){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            m_Log.e(TAG, getString(R.string.logging_exception, "LED Starting",e.getMessage()));
                        }});
                }
            }
            else if(enable) { //Start new Cam
                m_Cam = android.hardware.Camera.open();
                //Load Parameters and Set Parameters
                android.hardware.Camera.Parameters p = m_Cam.getParameters();
                p.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
                m_Cam.setParameters(p);
                //Start LED
                m_Cam.startPreview();
            }
            else if(m_Cam != null) { //Stop and Release LED
                m_Cam.stopPreview();
                m_Cam.release();
                m_Cam = null;
            }

            m_Log.i(TAG, (enable) ? getString(R.string.logging_led_start) : getString(R.string.logging_led_stop));
        }
    }
}
