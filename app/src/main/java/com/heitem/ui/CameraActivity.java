package com.heitem.ui;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.heitem.ar.ArActivity;
import com.heitem.augmentedjourney.R;
import com.heitem.data_localization.Place;
import com.heitem.ui.widget.CameraPreview;

import static com.heitem.utils.CameraHelper.cameraAvailable;
import static com.heitem.utils.CameraHelper.getCameraInstance;

//mport com.blundell.tut.cameraoverlay.FromXML;

/**
 * Takes a photo saves it to the SD card and returns the path of this photo to
 * the calling Activity
 * 
 * @author paul.blundell
 * 
 */
public class CameraActivity extends Activity implements SensorEventListener, LocationListener {

	protected static final String EXTRA_IMAGE_PATH = "com.heitem.ui.CameraActivity.EXTRA_IMAGE_PATH";

	private Camera camera;
	private CameraPreview cameraPreview;

	//Code ajoute
	protected PowerManager.WakeLock mWakeLock;
	RelativeLayout screen;
	ImageView cible;

	public static final String DEBUG_TAG = "OverlayView Log";

	private Handler handler;
	Place p;

	// Mount Washington, NH: 44.27179, -71.3039, 6288 ft (highest peak)
	private final static Location location = new Location("manual");
	static {
		if(ArActivity.p != null) {
			location.setLatitude(ArActivity.p.getLatitude());
			location.setLongitude(ArActivity.p.getLongitude());
		}
		//mountWashington.setLatitude(43.998d);
		//mountWashington.setLongitude(-71.2d);
		location.setAltitude(500d);
	}

	private LocationManager locationManager = null;
	private SensorManager sensors = null;

	private Location lastLocation;
	private float[] lastAccelerometer;
	private float[] lastCompass;

	private float verticalFOV;
	private float horizontalFOV;

	private boolean isAccelAvailable;
	private boolean isCompassAvailable;
	private boolean isGyroAvailable;
	private Sensor accelSensor;
	private Sensor compassSensor;
	private Sensor gyroSensor;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Full screen - first part
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		fullScreen();

		this.handler = new Handler();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		sensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		gyroSensor = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


		startSensors();
		startGPS();

		setContentView(R.layout.activity_camera);
		setResult(RESULT_CANCELED);
		// Camera may be in use by another activity or the system or not
		// available at all
		camera = getCameraInstance();
		if (cameraAvailable(camera)) {
			initCameraPreview();
		} else {
			finish();
		}

		Camera.Parameters params = camera.getParameters();
		verticalFOV = params.getVerticalViewAngle();
		horizontalFOV = params.getHorizontalViewAngle();


		cible = (ImageView)findViewById(R.id.target);

		screen = (RelativeLayout) findViewById(R.id.screen);
		screen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fullScreen();
			}
		});

		float curBearingToMW = 0.0f;

		if (lastLocation != null) {
			curBearingToMW = lastLocation.bearingTo(location);
		}

		// compute rotation matrix
		float rotation[] = new float[9];
		float identity[] = new float[9];
		if (lastAccelerometer != null && lastCompass != null) {
			boolean gotRotation = SensorManager.getRotationMatrix(rotation, identity, lastAccelerometer, lastCompass);
			if (gotRotation) {
				float cameraRotation[] = new float[9];
				// remap such that the camera is pointing straight down the Y axis
				SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X, SensorManager.AXIS_Z, cameraRotation);

				// orientation vector
				float orientation[] = new float[3];
				SensorManager.getOrientation(cameraRotation, orientation);

				// use roll for screen rotation
				//canvas.rotate((float) (0.0f - Math.toDegrees(orientation[2])));

				// Translate, but normalize for the FOV of the camera -- basically, pixels per degree, times degrees == pixels
				float dx = (float) ((screen.getWidth() / horizontalFOV) * (Math.toDegrees(orientation[0]) - curBearingToMW));
				float dy = (float) ((screen.getHeight() / verticalFOV) * Math.toDegrees(orientation[1]));

				// wait to translate the dx so the horizon doesn't get pushed off
				//canvas.translate(0.0f, 0.0f - dy);

				// make our line big enough to draw regardless of rotation and translation
				//canvas.drawLine(0f - canvas.getHeight(), canvas.getHeight() / 2, canvas.getWidth() + canvas.getHeight(), canvas.getHeight() / 2, targetPaint);

				// now translate the dx
				cible.setTranslationX(0.0f - dx);
				cible.setTranslationY(0.0f);

				// draw our point -- we've rotated and translated this to the right spot already
				//canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, 8.0f, targetPaint);

				//canvas.restore();
			}
		}
	}

	// Show the camera view on the activity
	private void initCameraPreview() {
		cameraPreview = (CameraPreview) findViewById(R.id.camera_preview);
		cameraPreview.init(camera);
	}

	// ALWAYS remember to release the camera when you are finished
	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera();
		locationManager.removeUpdates(this);
		sensors.unregisterListener(this);
	}

	private void releaseCamera() {
		if (camera != null) {
			camera.release();
			camera = null;
		}
	}

	public void fullScreen(){
		//Full Screen Activity
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE);
	}

	private void startSensors() {
		isAccelAvailable = sensors.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
		isCompassAvailable = sensors.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_NORMAL);
		isGyroAvailable = sensors.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	private void startGPS() {
		Criteria criteria = new Criteria();
		// criteria.setAccuracy(Criteria.ACCURACY_FINE);
		// while we want fine accuracy, it's unlikely to work indoors where we
		// do our testing. :)
		criteria.setAccuracy(Criteria.NO_REQUIREMENT);
		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

		String best = locationManager.getBestProvider(criteria, true);

		android.util.Log.v(DEBUG_TAG, "Best provider: " + best);

		locationManager.requestLocationUpdates(best, 50, 0, this);
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		android.util.Log.d(DEBUG_TAG, "onAccuracyChanged");

	}

	public void onSensorChanged(SensorEvent event) {
		// Log.d(DEBUG_TAG, "onSensorChanged");

		StringBuilder msg = new StringBuilder(event.sensor.getName()).append(" ");
		for (float value : event.values) {
			msg.append("[").append(String.format("%.3f", value)).append("]");
		}

		switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				lastAccelerometer = event.values.clone();
				break;
			case Sensor.TYPE_GYROSCOPE:
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				lastCompass = event.values.clone();
				break;
		}

		//this.invalidate();
	}

	public void onLocationChanged(Location location) {
		// store it off for use when we need it
		lastLocation = location;
	}

	public void onProviderDisabled(String provider) {
		// ...
	}

	public void onProviderEnabled(String provider) {
		// ...
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// ...
	}

	@Override
	public void onResume() {
		super.onResume();
		startSensors();
		startGPS();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
