package spaceproblems.camerathingy;


import spaceproblems.camerathingy.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.ImageView;

import java.io.File;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class InteractWithImage extends Activity
{
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 1500;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private static BitmapDrawable image;

    private Bitmap myBitmap;

    private String path;

    private static final String TAG = "InteractWithImage";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        image = null;
        setContentView(R.layout.activity_interact_with_image);
        setupActionBar();

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        myBitmap = null;
        final ImageView imageView = (ImageView) findViewById(R.id.picture);
        path = getIntent().getExtras().getString("image");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;

        myBitmap = BitmapFactory.decodeFile(path, options);
        imageView.setImageBitmap(myBitmap);

        mSystemUiHider = SystemUiHider.getInstance(this, imageView,
                HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener()
                {

                    @Override
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    public void onVisibilityChange(boolean visible)
                    {

                        controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);

                        if (visible && AUTO_HIDE)
                        {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (TOGGLE_ON_CLICK)
                {
                    mSystemUiHider.toggle();
                } else
                {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.delete_image).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPause()
    {
        if (myBitmap != null)
        {
            myBitmap.recycle();
        }
        myBitmap = null;
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        if (myBitmap != null)
        {
            myBitmap.recycle();
        }
        myBitmap = null;
        super.onStop();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);

    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent)
        {
            if (AUTO_HIDE)
            {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis)
    {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            if (myBitmap != null)
            {
                myBitmap.recycle();
            }
            myBitmap = null;
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void deleteImage(View view)
    {

        File file = new File(path);

        if(file.exists())
        {
            file.delete();
            Log.d(TAG, "File being deleted " + file.getAbsolutePath());
        }

        finish();
    }
}
