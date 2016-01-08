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
import java.util.concurrent.TimeUnit;

public class AlarmActivity extends AppCompatActivity {

    //Private camera Values
    private android.hardware.Camera m_Cam;
    private MediaPlayer mediaPlayer;
    private Vibrator m_Vibrator;

    //API Level 23
    private int actualAlarm = -1;
    private Handler alarmHandler;

    private boolean snoozed = false;
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(!snoozed){
            AlarmManage newAlarm = new AlarmManage(this);
            newAlarm.cancelAlarm(actualAlarm);
        }

        alarmHandler.removeCallbacksAndMessages(null);
        setVibrationStop();
        stopMusic();
        stopLED();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        alarmHandler = new Handler();

        Bundle intentExtras = getIntent().getExtras();
        int[] time  = intentExtras.getIntArray(AlarmConstants.WAKEUP_TIME); //Time, Minutes, Snooze
        int[] days  = intentExtras.getIntArray(AlarmConstants.WAKEUP_DAYS); //Monday - Sunday
        int[] music = intentExtras.getIntArray(AlarmConstants.WAKEUP_MUSIC);// StartTime, Volume, FadeIn, FadeInTime, Vibration Aktiv, Vibration Strength
        int[] light = intentExtras.getIntArray(AlarmConstants.WAKEUP_LIGHT);// UseScreen, ScreenBrightness, ScreenStrartTime, Color1, Color2, FadeColor, UseLed, LedStartTime

        String musicURI = intentExtras.getString(AlarmConstants.ALARM_MUSIC_SONGID);

        actualAlarm = intentExtras.getInt(AlarmConstants.ALARM_ID);

        //Boolean Values
        //Screen
        boolean useScreen      = light[0] == 1;
        boolean useLED         = light[6] == 1;
        boolean useColorFading = light[5] == 1;

        //Music
        boolean useFadeIn    = music[2] == 1;
        boolean useVibration = music[4] == 1;

        int minuteScreen = (useScreen) ? light[2] : 0;
        int minuteLED    = (useLED) ? light[7] : 0;

        int minutesMax   = (minuteScreen >= minuteLED) ? minuteScreen : minuteLED;

        int minuteScreenStart = minutesMax - minuteScreen;
        int minutesLEDSTart   = minutesMax - minuteLED;

        //SCREEN BRIGHTNESS/////////////////////////////////////////////////////////////////////////
        if(useScreen)
            doBrightness(minuteScreenStart, light[2], light[1]);

        //SCREEN VIEWS//////////////////////////////////////////////////////////////////////////////
        doViews();

        //SCREEN COLOR FADING///////////////////////////////////////////////////////////////////////
        doColorFading(useColorFading, minuteScreenStart, minuteScreen, light[3], light[4]);

        //LED///////////////////////////////////////////////////////////////////////////////////////
        if(useLED)
            doLED(minutesLEDSTart);

        //MUSIC FADE IN/////////////////////////////////////////////////////////////////////////////
        doPlayMusic(music[0], useFadeIn, music[1], minutesMax, music[3], musicURI );

        //MUSIC VIBRATION///////////////////////////////////////////////////////////////////////////
        if(useVibration)
            doVibrate(minutesMax, music[5]);
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
        String dayName = getDayName(calendar);

        String message = dayName + ", " +
                         Integer.toString(currentDay)   + "." +
                         Integer.toString(currentMonth) + "." +
                         Integer.toString(currentYear);
        dateText.setText(message);

        int currentHour   = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        final TextView timeText = (TextView) findViewById(R.id.wakeup_timer_wakescreen_textview);
        timeText.setText(String.format("%02d:%02d", currentHour, currentMinute));

        //Set new Handler for Updating Date and Time
        final Runnable timeRunnable = new Runnable() {
            @Override
            public void run() {
                //Update Time
                Calendar calendarNew = Calendar.getInstance();
                timeText.setText(String.format("%02d:%02d", calendarNew.get(Calendar.HOUR_OF_DAY), calendarNew.get(Calendar.MINUTE)));

                String message = getDayName(calendarNew) + ", " +
                                 Integer.toString(calendarNew.get(Calendar.DAY_OF_MONTH)) + "." +
                                 Integer.toString(calendarNew.get(Calendar.MONTH) + 1)    + "." +
                                 Integer.toString(calendarNew.get(Calendar.YEAR));
                //Update Date
                dateText.setText(message);

                alarmHandler.postDelayed(this, 100); //1 second check for new Time
            }
        };
        alarmHandler.postDelayed(timeRunnable, 100); //1 second check for new Time
    }

    /***********************************************************************************************
     * COLOR FADING
     **********************************************************************************************/
    private void doColorFading(final boolean _colorFading, int minutes, final int _minutesScreen, final int _color1, final int _color2){

        //Open LinearLayout to Change Color
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.wakeup_wakescreen_layout);
        linearLayout.setBackgroundColor(Color.BLACK);

        Runnable initialColorFadeRunnable = new Runnable() {
            @Override
            public void run() {

                //duration for StartScreen Till WakeUp
                final long duration = TimeUnit.MINUTES.toMillis(_minutesScreen ) / 4;

                //ObjectAnimator
                ObjectAnimator colorFade1 = ObjectAnimator.ofObject(linearLayout,"backgroundColor", new ArgbEvaluator(), Color.BLACK, _color1);
                colorFade1.setDuration(duration);
                colorFade1.start();

                //Check if Fading is true
                if(_colorFading){

                    //Second ColorHandler
                    Runnable colorRunnable = new Runnable() {
                        @Override
                        public void run() {
                            //Change Color a Second Time
                            ObjectAnimator colorFade2 = ObjectAnimator.ofObject(linearLayout,"backgroundColor", new ArgbEvaluator(), _color1, _color2);
                            colorFade2.setDuration(duration * 3);
                            colorFade2.start();
                        }
                    };
                    alarmHandler.postDelayed(colorRunnable, duration + 1 );
                }
            }
        };
        alarmHandler.postDelayed(initialColorFadeRunnable, TimeUnit.MINUTES.toMillis(minutes));
    }

    /***********************************************************************************************
     * BRIGHTNESS
     **********************************************************************************************/

    private void doBrightness(int minutes, final int _screenStartTime, final int _screenBrightness ){
        Runnable screenTimerRunnable = new Runnable() {
            @Override
            public void run() {
                setBrightness(_screenStartTime, _screenBrightness);
            }
        };
        alarmHandler.postDelayed(screenTimerRunnable, TimeUnit.MINUTES.toMillis(minutes));
    }
    private void setBrightness(int _screenStartTime, int _screenBrightness){

        //Start with Basic parameters
        startScreen();

        //GetCurrent Layout and Set new Brightness
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 0.0F;
        getWindow().setAttributes(layout);

        //time for each step ti illuminate
        final long  millis     =  ( TimeUnit.MINUTES.toMillis(_screenStartTime) == 0) ? 0 : TimeUnit.MINUTES.toMillis(_screenStartTime) / 100;  //divide milliseconds with 100 because we have 100 steps till full illumination
        final float brightness = (float) _screenBrightness / 100;

        //New Time Handler
        Runnable screenLightRunnable = new Runnable() {
            @Override
            public void run() {

                //get Layout and Update LightValue till Max
                WindowManager.LayoutParams layoutNew = getWindow().getAttributes();
                if(layoutNew.screenBrightness < brightness){

                    layoutNew.screenBrightness += 0.01F;
                    getWindow().setAttributes(layoutNew);

                    alarmHandler.postDelayed(this, millis);
                }
            }
        };
        alarmHandler.postDelayed(screenLightRunnable, millis); //All 10 Seconds more Light
    }

    /***********************************************************************************************
     * MUSIC
     **********************************************************************************************/
    private int currentVolume;
    private void doPlayMusic(int _startSeconds, boolean _FadeIn, final int _musicVolume, int minutes, final int _fadingSeconds, final String _musicURI){ // StartTime, Volume, FadeIn, FadeInTime, Vibration Aktiv, Vibration Strength

        try { prepareMusic(_musicURI); }
        catch (IOException e) { Log.e("Exception: ", e.getMessage()); }

        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        //Get MaxVolume of Music
        final int maxVolumeAndroid = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolumeAndroid, 0);

        mediaPlayer.seekTo((int) TimeUnit.SECONDS.toMillis(_startSeconds));
        if(_FadeIn){

            currentVolume = 0;
            mediaPlayer.setVolume(currentVolume, currentVolume);
            Runnable musicFadeInRunnable = new Runnable() {
                @Override
                public void run() {

                    if(!mediaPlayer.isPlaying())
                        mediaPlayer.start();

                    if( currentVolume <= _musicVolume){
                        //Get Time For Fading and Sequenze to 15 Steps
                        long millis =  TimeUnit.SECONDS.toMillis(_fadingSeconds);
                        long millis_steps = millis / 100;

                        //Set AudioManager
                        float volume= 1 - (float)(Math.log(_musicVolume-currentVolume)/Math.log(_musicVolume));
                        mediaPlayer.setVolume(volume, volume);
                        currentVolume +=1;
                        alarmHandler.postDelayed(this, millis_steps);
                    }
                }
            };
            alarmHandler.postDelayed(musicFadeInRunnable, TimeUnit.MINUTES.toMillis(minutes) + 1);
        }
        else{

            Runnable musicPlayRunnable = new Runnable() {
                @Override
                public void run() {

                    float volume= 1 - (float)(Math.log(0)/Math.log(_musicVolume));
                    mediaPlayer.setVolume(volume, volume);
                    mediaPlayer.start();
                }
            };
            alarmHandler.postDelayed(musicPlayRunnable, TimeUnit.MINUTES.toMillis(minutes));
        }
    }
    private void prepareMusic(String _SongUri) throws IOException {

        //Prepare Music Stream
        if(mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        else
            mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Uri newSongUri = Uri.parse(_SongUri);
        mediaPlayer.setDataSource(getApplicationContext(), newSongUri);
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
    private void doVibrate(int minutes, final int _vibrationStrength){
        //Set new Handler
        Runnable vibrationRunnable = new Runnable() {
            @Override
            public void run() {
                setVibrationStart(_vibrationStrength, 1000);
            }
        };

        //Get Time Value till Vibration Starts
        alarmHandler.postDelayed(vibrationRunnable, TimeUnit.MINUTES.toMillis(minutes));
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
        if(m_Vibrator != null){
            m_Vibrator.cancel();
            m_Vibrator = null;
        }
    }

    /***********************************************************************************************
     * SCREEN AND LED
     **********************************************************************************************/
    private void startScreen(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private void doLED(int minutes){

        //New Handler for Waiting Till Time to show LED
        Runnable runLED = new Runnable() {
            @Override
            public void run() {
                startLED();
            }
        };
        //Start Delayed
        alarmHandler.postDelayed(runLED, TimeUnit.MINUTES.toMillis(minutes));
    }
    private void startLED(){

        if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){  //TODO Test with new Version when available
                CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

                try {
                    String[] cameraIds = cameraManager.getCameraIdList();

                    for(String cameraID : cameraIds){

                        CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);
                        if(cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)){
                            cameraManager.setTorchMode(cameraID, true);
                        }
                    }
                } catch (Exception e){
                    //TODO Catch Exception
                }
            }
            else{
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
        if(m_Cam != null){
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

        AlarmManage newAlarm = new AlarmManage(this);
        snoozed = true;
        newAlarm.snoozeAlarm(actualAlarm);
        this.finish();
    }
}
