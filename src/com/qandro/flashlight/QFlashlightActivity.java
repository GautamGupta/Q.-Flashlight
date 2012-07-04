package com.qandro.flashlight;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class QFlashlightActivity extends Activity {
	
	Integer oriBrightnessValue;
	Boolean flashlightStatus = false; // false = off, true = on
	Camera mCamera = null;
	Parameters parameters;
	LinearLayout flashControl;
	SurfaceView preview;
	SurfaceHolder mHolder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// Retrieve the brightness value for future use
		try {
			oriBrightnessValue = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		
		setContentView(R.layout.main);
		
		flashControl = (LinearLayout) findViewById(R.id.flashcontrol);
		preview = (SurfaceView) findViewById(R.id.preview);
		mHolder = preview.getHolder();

		flashControl.setOnClickListener(new LinearLayout.OnClickListener(){
			
			@Override
			public void onClick(View arg0) {
				toggleFlashLight();
			}
		});
	}
	
	/**
	 * Revert to original brightness
	 * Also turn off the flashlight if api level < 14
	 * And turn off the cam if we're not using it
	 */
	@Override
	public void onStop() {
		super.onStop();
		
		// Revert to original brightness
		setBrightness(oriBrightnessValue);
		
		// Turn off the flashlight if api level < 14 as leaving it on would result in a FC
		if (Integer.valueOf(android.os.Build.VERSION.SDK) < 14 || flashlightStatus == false) {
			turnOffFlashLight();
			
			// Turn off the cam if it is on
			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}
		}
	}
	
	/**
	 * Check if the device has a flashlight
	 * @return True if the device has a flashlight, false if not
	 */
	public Boolean deviceHasFlashlight() {
		Context context = this;
		PackageManager packageManager = context.getPackageManager();
		
		if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Set brightness to a desired value
	 * @param brightness
	 */
	private void setBrightness(int brightness) {
	    WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
	    layoutParams.screenBrightness = brightness/100.0f;
	    getWindow().setAttributes(layoutParams);
	}
	
	/**
	 * Toggle the flashlight on/off status
	 */
	public void toggleFlashLight() {
		if (flashlightStatus == false) { // Off, turn it on
			turnOnFlashLight();
		} else { // On, turn it off
			turnOffFlashLight();
		}
	}
	
	/**
	 * Turn on the flashlight if the device has one.
	 * Also set the background colour to white and brightness to max.
	 */
	public void turnOnFlashLight() {
		// Safety measure if it's already on
		turnOffFlashLight();
		
		// Turn on the flash if the device has one
		if (deviceHasFlashlight()) {
			
			// Switch on the cam for app's life
			if (mCamera == null) {
				// Turn on Cam
				mCamera = Camera.open();
				try {
					mCamera.setPreviewDisplay(mHolder);
				} catch (IOException e) {
					e.printStackTrace();
				}
				mCamera.startPreview();
			}
	
			// Turn on LED
			parameters = mCamera.getParameters();
			parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(parameters);
		}
		
		// Set background color
		flashControl.setBackgroundColor(Color.WHITE);
		
		// Set brightness to max
		setBrightness(100);
		
		// Self awareness
		flashlightStatus = true;
	}
	
	/**
	 * Turn off the flashlight if we find it to be on.
	 * Also set the background to black and revert to original brightness
	 */
	public void turnOffFlashLight() {
		// Turn off flashlight
		if (mCamera != null) {
			parameters = mCamera.getParameters();
			if (parameters.getFlashMode().equals(Parameters.FLASH_MODE_TORCH)) {
				parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
				mCamera.setParameters(parameters);
			}
		}
		
		// Set background color
		flashControl.setBackgroundColor(Color.BLACK);
		
		// Revert to original brightness
		setBrightness(oriBrightnessValue);
		
		// Self awareness
		flashlightStatus = false;
	}
}

