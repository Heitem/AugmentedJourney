package com.heitem.ar;

import android.app.Activity;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.heitem.augmentedjourney.R;
import com.heitem.data_localization.Place;

public class ArActivity extends Activity {

    private OverlayView arContent;
    LayoutInflater controlInflater = null;
    public static Place p;
    public static ImageView target;

    //Code ajoute
    protected PowerManager.WakeLock mWakeLock;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        p = getIntent().getParcelableExtra("Place");

        //Full Screen Activity
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE);

        setContentView(R.layout.activity_ar);
        
        FrameLayout arViewPane = (FrameLayout) findViewById(R.id.ar_view_pane);
        
        ArDisplayView arDisplay = new ArDisplayView(getApplicationContext(), this);
        arViewPane.addView(arDisplay);

        arContent = new OverlayView(getApplicationContext());
        arViewPane.addView(arContent);

        controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.target_layout, null);
        ViewGroup.LayoutParams layoutParamsControl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.addContentView(viewControl, layoutParamsControl);

        //target = (ImageView)findViewById(R.id.target);
   }

	@Override
	protected void onPause() {
		arContent.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		arContent.onResume();
	}
}