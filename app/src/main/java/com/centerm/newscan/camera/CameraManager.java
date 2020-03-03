package com.centerm.newscan.camera;

import java.io.IOException;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.view.SurfaceHolder;
import com.centerm.zbar.camera.open.OpenCameraInterface;

public class CameraManager {

	private static CameraManager cameraManager;

	static final int SDK_INT;
	static {
		int sdkInt;
		try {
			sdkInt = android.os.Build.VERSION.SDK_INT;
		} catch (NumberFormatException nfe) {
			sdkInt = 10000;
		}
		SDK_INT = sdkInt;
	}

	private final CameraConfigurationManager configManager;
	private Camera camera;
	private boolean initialized;
	private boolean previewing;
	private final boolean useOneShotPreviewCallback;
	private final PreviewCallback previewCallback;
	private final AutoFocusCallback autoFocusCallback;
	private Parameters parameter;

	public static void init(Context context) {
		if (cameraManager == null) {
			cameraManager = new CameraManager(context);
		}
	}

	public static CameraManager get() {
		return cameraManager;
	}

	private CameraManager(Context context) {
		this.configManager = new CameraConfigurationManager(context);

		useOneShotPreviewCallback = SDK_INT > 3;
		previewCallback = new PreviewCallback(configManager,
				useOneShotPreviewCallback);
		autoFocusCallback = new AutoFocusCallback();
	}

	private int requestedCameraId = -1;
	private int requestedFramingRectWidth;
	private int requestedFramingRectHeight;

	public void openDriver(SurfaceTexture surfaceTexture, boolean cameraBack) throws IOException {
		Camera theCamera = camera;
		if(cameraBack){
			requestedCameraId = FindBackCamera();
		}else{
			requestedCameraId = FindFrontCamera();
		}
		if (theCamera == null) {
			if (requestedCameraId >= 0) {
				theCamera = OpenCameraInterface.open(requestedCameraId);
			} else {
				theCamera = OpenCameraInterface.open();
			}

			if (theCamera == null) {
				throw new IOException();
			}
			camera = theCamera;
		}
//		theCamera.setPreviewDisplay(holder);
		theCamera.setPreviewTexture(surfaceTexture);

		if (!initialized) {
			initialized = true;
			configManager.initFromCameraParameters(theCamera);
			if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
			
				setManualFramingRect(requestedFramingRectWidth,
						requestedFramingRectHeight);
						
				
				requestedFramingRectWidth = 0;
				requestedFramingRectHeight = 0;
			}
		}

		Camera.Parameters parameters = theCamera.getParameters();
		String parametersFlattened = parameters == null ? null : parameters
				.flatten(); // Save these, temporarily
		try {
			configManager.setDesiredCameraParameters(theCamera, false);
		} catch (RuntimeException re) {
			// Driver failed
			// Log.w(TAG,
			// "Camera rejected parameters. Setting only minimal safe-mode parameters");
			// Log.i(TAG, "Resetting to saved camera params: " +
			// parametersFlattened);
			// Reset:
			if (parametersFlattened != null) {
				parameters = theCamera.getParameters();
				parameters.unflatten(parametersFlattened);
				try {
					theCamera.setParameters(parameters);
					configManager.setDesiredCameraParameters(theCamera, true);
				} catch (RuntimeException re2) {
					// Well, darn. Give up
					// Log.w(TAG,
					// "Camera rejected even safe-mode parameters! No configuration");
				}
			}
		}

		/*
		 * if (camera == null) { camera = Camera.open(); if (camera == null) {
		 * throw new IOException(); } camera.setPreviewDisplay(holder);
		 * 
		 * if (!initialized) { initialized = true;
		 * configManager.initFromCameraParameters(camera); }
		 * configManager.setDesiredCameraParameters(camera);
		 * FlashlightManager.enableFlashlight(); }
		 */
	}

	/**
	 * Allows third party apps to specify the scanning rectangle dimensions,
	 * rather than determine them automatically based on screen resolution.
	 * 
	 * @param width
	 *            The width in pixels to scan.
	 * @param height
	 *            The height in pixels to scan.
	 */
	public synchronized void setManualFramingRect(int width, int height) {
		if (initialized) {
			Point screenResolution = configManager.getScreenResolution();
			if (width > screenResolution.x) {
				width = screenResolution.x;
			}
			if (height > screenResolution.y) {
				height = screenResolution.y;
			}
			// Log.d(TAG, "Calculated manual framing rect: " + framingRect);
		} else {
			requestedFramingRectWidth = width;
			requestedFramingRectHeight = height;
		}
	}

	public Point getCameraResolution() {
		return configManager.getCameraResolution();
	}

	public void closeDriver() {
		if (camera != null) {
			FlashlightManager.disableFlashlight();
			camera.release();
			camera = null;
		}
	}

	public void startPreview() {
		if (camera != null && !previewing) {
			 
			camera.startPreview();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			previewing = true;
		}
	}

	public void stopPreview() {
		if (camera != null && previewing) {
			if (!useOneShotPreviewCallback) {
				camera.setPreviewCallback(null);
			}
			camera.stopPreview();
			Camera.Parameters param = camera.getParameters();
			param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			camera.setParameters(param);

			previewCallback.setHandler(null, 0);
			autoFocusCallback.setHandler(null, 0);
			previewing = false;
		}
	}

	public void requestPreviewFrame(Handler handler, int message) {
		if (camera != null && previewing) {
			previewCallback.setHandler(handler, message);
			if (useOneShotPreviewCallback) {
				camera.setOneShotPreviewCallback(previewCallback);
			} else {
				camera.setPreviewCallback(previewCallback);
			}
		}
	}

	public void requestAutoFocus(Handler handler, int message) {
		if (camera != null && previewing) {
			autoFocusCallback.setHandler(handler, message);
			//2017-03-28  开启自动对焦  wangwenxun@centerm.com
			camera.autoFocus(autoFocusCallback);
		}
	}

	public void openLight() {
		if (camera != null) {
			parameter = camera.getParameters();
			parameter.setFlashMode(Parameters.FLASH_MODE_TORCH);
			camera.setParameters(parameter);
		}
	}

	public void offLight() {
		if (camera != null) {
			parameter = camera.getParameters();
			parameter.setFlashMode(Parameters.FLASH_MODE_OFF);
			camera.setParameters(parameter);
		}
	}
	//begin 2017-03-30 为9505型号进行特别处理 wangwenxun@centerm.com
	public void openLight2(){
		if(camera != null){
			parameter = camera.getParameters();
			parameter.setFlashMode(Parameters.FLASH_MODE_ON);
			camera.setParameters(parameter);
		}
	}
	public void offLight2(){
		if(camera != null){
			parameter = camera.getParameters();
			parameter.setFlashMode(Parameters.FLASH_MODE_TORCH);
			camera.setParameters(parameter);
		}
		
	}
	//end 2017-03-30
	
	//begin 2018-11-01 获取前置摄像头或者后置摄像头ID linpeita@centerm.com
	private int FindBackCamera(){  
        int cameraCount = 0;  
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();  
        cameraCount = Camera.getNumberOfCameras(); // get cameras number  
                
        for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {  
            Camera.getCameraInfo( camIdx, cameraInfo ); // get camerainfo  
            if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_BACK ) {   
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置  
               return camIdx;  
            }  
        }  
        return -1;  
    }
	
    private int FindFrontCamera(){  
        int cameraCount = 0;  
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();  
        cameraCount = Camera.getNumberOfCameras(); // get cameras number  
                
        for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {  
            Camera.getCameraInfo( camIdx, cameraInfo ); // get camerainfo  
            if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_FRONT ) {   
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置  
               return camIdx;  
            }  
        }  
        return -1;  
    }
	//end 2018-11-01 linpeita@centerm.com
}
