package com.heitem.ui.widget;

 

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.heitem.ui.CameraActivity;

import java.io.IOException;

/**
 *
 * @author paul.blundell
 *
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

	private Camera camera;
	private SurfaceHolder holder;
	Activity mActivity;
	public static final String DEBUG_TAG = "ArDisplayView Log";

	public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CameraPreview(Context context) {
		super(context);
	}


	public void init(Camera camera) {
		this.camera = camera;
		initSurfaceHolder();
	}

	@SuppressWarnings("deprecation") // needed for < 3.0
	private void initSurfaceHolder() {
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		initCamera(holder);
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);

		int rotation = getDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
		}

		camera.setDisplayOrientation((info.orientation - degrees + 360) % 360);

		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			Log.e(DEBUG_TAG, "surfaceCreated exception: ", e);
		}
		//Code ajoute
		//set camera to continually auto-focus
		Camera.Parameters params = camera.getParameters();
		//*EDIT*//params.setFocusMode("continuous-picture");
		//It is better to use defined constraints as opposed to String, thanks to AbdelHady
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		camera.setParameters(params);
	}

	private void initCamera(SurfaceHolder holder) {
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (Exception e) {
			//Log.d("Error setting camera preview", e);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}
}