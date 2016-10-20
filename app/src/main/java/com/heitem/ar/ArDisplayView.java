package com.heitem.ar;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class ArDisplayView extends SurfaceView implements SurfaceHolder.Callback {
	public static final String DEBUG_TAG = "ArDisplayView Log";
	Camera mCamera;
	SurfaceHolder mHolder;
	Activity mActivity;

	public ArDisplayView(Context context, Activity activity) {
		super(context);

		mActivity = activity;
		mHolder = getHolder();

		// This value is supposedly deprecated and set "automatically" when
		// needed.
		// Without this, the application crashes.
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// callbacks implemented by ArDisplayView
		mHolder.addCallback(this);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(DEBUG_TAG, "surfaceCreated");

		// Grab the camera
		mCamera = Camera.open();

		// Set Display orientation
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(CameraInfo.CAMERA_FACING_BACK, info);

		int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
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

		mCamera.setDisplayOrientation((info.orientation - degrees + 360) % 360);

		try {
			mCamera.setPreviewDisplay(mHolder);
		} catch (IOException e) {
			Log.e(DEBUG_TAG, "surfaceCreated exception: ", e);
		}
		//Code ajoute
		//set camera to continually auto-focus
		Camera.Parameters params = mCamera.getParameters();
		//*EDIT*//params.setFocusMode("continuous-picture");
		//It is better to use defined constraints as opposed to String, thanks to AbdelHady
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		mCamera.setParameters(params);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(DEBUG_TAG, "surfaceChanged");

		Camera.Parameters params = mCamera.getParameters();

		// Find an appropriate preview size that fits the surface
		List<Size> prevSizes = params.getSupportedPreviewSizes();
		for (Size s : prevSizes) {
			if ((s.height <= height) && (s.width <= width)) {
				params.setPreviewSize(s.width, s.height);
				break;
			}

		}

		// Set the preview format
		//params.setPreviewFormat(ImageFormat.JPEG);

		// Consider adjusting frame rate to appropriate rate for AR

		// Confirm the parameters
		mCamera.setParameters(params);

		// Begin previewing
		mCamera.startPreview();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(DEBUG_TAG, "surfaceDestroyed");

		// Shut down camera preview
		mCamera.stopPreview();
		mCamera.release();
	}
}
