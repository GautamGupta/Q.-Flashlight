package com.qandro.flashlight;

import java.io.IOException;

import com.qandro.flashlight.R;

import android.app.Activity;
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
	
	int oriBrightnessValue;
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
	 * 
	 * Also turn off the flashlight if api level < 14
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// Revert to original brightness
		setBrightness(oriBrightnessValue);
		
		// Turn off the flashlight if api level < 14 as leaving it on would result in a FC
		if ( Integer.valueOf(android.os.Build.VERSION.SDK) < 14 ) {
			turnOffFlashLight();
		}
	}
	
	/**
	 * Toggle the flashlight on/off status
	 */
	public void toggleFlashLight() {
		if (mCamera == null) { // Off, turn it on
			turnOnFlashLight();
		} else { // On, turn it off
			turnOffFlashLight();
		}
	}
	
	/**
	 * Turn on the flashlight.
	 * 
	 * Also set the background colour to white and brightness to max.
	 */
	public void turnOnFlashLight() {
		// Safety measure if it's already on
		turnOffFlashLight();
		
		// Turn on Cam
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(mHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Turn on LED  
		parameters = mCamera.getParameters();
		parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
		mCamera.setParameters(parameters);      
		mCamera.startPreview();
		
		// Set background color
		flashControl.setBackgroundColor(Color.WHITE);
		
		// Set brightness to max
		setBrightness(100);
	}
	
	/**
	 * Turn off the flashlight if we find it to be on.
	 * 
	 * Also set the background to black and revert to original brightness
	 */
	public void turnOffFlashLight() {
		
		// Turn off cam
		if (mCamera != null) {
			mCamera.release();
	        mCamera = null;
		}
		
		// Set background color
		flashControl.setBackgroundColor(Color.BLACK);
		
		// Revert to original brightness
		setBrightness(oriBrightnessValue);
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
}

