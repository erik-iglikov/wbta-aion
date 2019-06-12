package com.bulsy.wbtempest;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.serindlabs.pocketid.sdk.PocketIDSdk;
import com.serindlabs.pocketid.sdk.base.PocketIDListener;
import com.serindlabs.pocketid.sdk.constants.PocketIDEventType;
import com.serindlabs.pocketid.sdk.constants.PocketIDRequestCode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements PocketIDListener {
    static final String LOG_ID = "wbt";
    private static final float EXPECTED_DENSITY = 315.0f;  // original target density of runtime device
    private static final float EXPECTED_WIDTH_PIX = 720.0f;  // original target width of runtime device
    static final String STARTLEVEL_FILENAME = "wbt.lev";
    static final int LOW_LEVEL_THRESHOLD = 6; // once unlocked, start level will always be offered up to this level
    int TS_NORMAL; // normal text size
    int TS_BIG; // large text size
    Screen entryScreen;
    PlayScreen playScreen;
    Screen currentScreen;
    FullScreenView mainView;
    Typeface gamefont;
    float sizescalefactor; // scaling factor for current device screen, compared to expected/development screen
    public SoundPool soundpool = null;
    Map<Sound, Integer> soundMap = null;
    DisplayMetrics metrics;
    int maxStartLevel = 1;
    String lblSelectStartScr;

    /**
     * Initialize the activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            super.onCreate(savedInstanceState);
            metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            gamefont = Typeface.createFromAsset(getAssets(), "lt.ttf");
            sizescalefactor = (float)(metrics.widthPixels / EXPECTED_WIDTH_PIX);//*Math.pow(metrics.densityDpi / EXPECTED_DENSITY, 0.4));
            Log.d(LOG_ID, "dpi:"+metrics.densityDpi+" width:"+metrics.widthPixels+" scalefact:"+ sizescalefactor);
            if (sizescalefactor > 1.5f)
                sizescalefactor = 1.5f;
            else if (sizescalefactor < 0.5f)
                sizescalefactor = 0.5f;
            TS_NORMAL = (int)(38 * sizescalefactor);
            TS_BIG = (int)(80 * sizescalefactor);


//            getWindow().addContentView(entryScreen);
//
//            <com.serindlabs.pocketid.sdk.widget.PocketIDButton
//            android:id="@+id/btnLogin"
//            android:layout_width="wrap_content"
//            android:layout_height="wrap_content" />


            // get max start level
            BufferedReader f = null;
            try {
                f = new BufferedReader(new FileReader(getFilesDir() + STARTLEVEL_FILENAME));
                maxStartLevel = Integer.parseInt(f.readLine());
            } catch (Exception e) {
                Log.d(MainActivity.LOG_ID, "ReadMaxStartlevel", e);
            } finally {
                if (f != null)
                    f.close();
            }

            // create screens
            entryScreen = new EntryScreen(this);
            playScreen = new PlayScreen(this);

            mainView = new FullScreenView(this);
            setContentView(mainView);

            // POCKETID INTEGRATION
//            setContentView(mainView);
            PocketIDSdk.getInstance().initialize(this, "nh(DyBAlOlVWugK_ezmqN!qEHBiKYVF)");
            super.onCreate(savedInstanceState);
            PocketIDSdk.getInstance().registerListener(this);

            // set up sounds
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            soundpool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
            soundMap = new HashMap();
            AssetFileDescriptor descriptor = getAssets().openFd("fire.mp3");
            soundMap.put(Sound.FIRE, soundpool.load(descriptor, 1));
            descriptor = getAssets().openFd("enemyfire.mp3");
            soundMap.put(Sound.ENEMYFIRE, soundpool.load(descriptor, 1));
            descriptor = getAssets().openFd("crawlding.mp3");
            soundMap.put(Sound.CRAWLERMOVE, soundpool.load(descriptor, 1));
            descriptor = getAssets().openFd("crawldeath.mp3");
            soundMap.put(Sound.CRAWLERDEATH, soundpool.load(descriptor, 1));
            descriptor = getAssets().openFd("enemydeath.mp3");
            soundMap.put(Sound.ENEMYDEATH, soundpool.load(descriptor, 1));
            descriptor = getAssets().openFd("levchg1.mp3");
            soundMap.put(Sound.LEVCHG, soundpool.load(descriptor, 1));
            descriptor = getAssets().openFd("exlife.mp3");
            soundMap.put(Sound.EXLIFE, soundpool.load(descriptor, 1));






        } catch (Exception e) {
            // panic, crash, fine -- but let me know what happened.
            Log.d(LOG_ID, "onCreate", e);
        }
    }

    /**
     * play the inpassed sound
     * @param s
     * @param vol
     * @param rate
     * @return a streamID representing the sound being played
     */
    public int playSound(Sound s, float vol, float rate) {
        return soundpool.play(soundMap.get(s), vol, vol, 0, 0, rate);
    }
    public int playSound(Sound s) {
        return playSound(s, 0.9f, 1);
    }

    /**
     * stop the given sound stream, which was started by play()
     * @param streamID
     */
    public void stopSound(int streamID) {
        soundpool.stop(streamID);
    }
    /**
     * Handle resuming of the game,
     */
    @Override
    protected void onResume() {
        super.onResume();
        mainView.resume();
    }

    /**
     * Handle pausing of the game.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mainView.pause();
    }

    /**
     *
     * @return font to be used
     */
    public Typeface getGameFont() {
        return gamefont;
    }

    /**
     * @return displaymetrics of screen we are playing on.
     */
    public DisplayMetrics getDisplayMetrics(){
        return metrics;
    }

    // screen transitions

    /**
     * Start a new game.
     */
    public void startGame(int startlevel) {
        this.playScreen.startGame(startlevel);
        currentScreen = this.playScreen;
    }

    /**
     * Leave game and return to title screen.
     */
    public void leaveGame() {
        currentScreen = this.entryScreen;
    }

    /**
     * completely exit the game.
     */
    public void exit() {
        finish();
        System.exit(0);
    }


    /**
     * PocketID methods
     */

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        setContentView(this.entryScreen);
//        PocketIDSdk.getInstance().initialize(this, "nh(DyBAlOlVWugK_ezmqN!qEHBiKYVF)");
//        super.onCreate(savedInstanceState);
//        PocketIDSdk.getInstance().registerListener(this);
//    }

    @Override
    public void onEvent(String event, Bundle data) {
        switch (event)  {
            case PocketIDEventType.EVENT_LOGIN_SUCCESS:
                System.out.println("DDDDDDDD");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        PocketIDSdk.getInstance().unregisterListener(this);
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        System.out.println("1123123144224!");

        if (requestCode == PocketIDRequestCode.AUTHENTICATION && resultCode == RESULT_OK) {
            // code here
            System.out.println("AAAAAA!");


//            Snackbar.make(findViewById(R.id.pocketid), R.string.app_name,
//                    Snackbar.LENGTH_SHORT)
//                    .show();

            Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(startIntent);

        } else {
            super.onActivityResult(requestCode, resultCode, data);
            System.err.println("Wrong username or password!");
        }
    }





    /**
     * This inner class handles the main render loop, and delegates drawing and event handling to
     * the individual screens.
     */
    private class FullScreenView extends SurfaceView implements Runnable, View.OnTouchListener {
        private volatile boolean isRendering = false;
        Thread renderThread = null;
        SurfaceHolder holder;

        public FullScreenView(Context context) {
            super(context);
            holder = getHolder();
            currentScreen = entryScreen;
            setOnTouchListener(this);
        }

        public void resume() {
            isRendering = true;
            renderThread = new Thread(this);
            renderThread.start();
        }

        @Override
        public void run() {
            try {
                while(isRendering){
                    while(!holder.getSurface().isValid()) {
                        try {
                            Thread.sleep(5);
                        } catch (Exception e) { /* we don't care */  }
                    }

                    Screen cs = currentScreen; // we want to update and draw same screen, even if currentscreen changes

                    // update screen's context
                    cs.update(this);

                    // draw screen
                    Canvas c = holder.lockCanvas();
                    cs.draw(c, this);
                    holder.unlockCanvasAndPost(c);
                }
            } catch (Exception e) {
                // arguably overzealous to grab all exceptions here...but i want to know.
                Log.d(LOG_ID, "THREAD STOPPING", e);
            }
        }

        public void pause() {
            isRendering = false;
            while(true) {
                try {
                    renderThread.join();
                    return;
                } catch (InterruptedException e) {
                    // retry
                }
            }
        }

        public boolean onTouch(View v, MotionEvent event) {
            try {
                return currentScreen.onTouch(event);
            }
            catch (Exception e) {
                // arguably overzealous to grab all exceptions here...but i want to know.
                Log.d(LOG_ID, "onTouch", e);
            }
            return false;
        }
    }
}
