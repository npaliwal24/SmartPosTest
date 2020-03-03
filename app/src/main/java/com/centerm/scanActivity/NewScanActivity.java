package com.centerm.scanActivity;

import com.centerm.newscan.camera.CameraManager;
import com.centerm.newscan.decode.AidlDecodeManager;
import com.centerm.newscan.decode.CaptureActivityHandler;
import com.centerm.newscan.decode.InactivityTimer;
import com.centerm.smartpos.R;
import com.centerm.smartpos.aidl.qrscan.AidlQuickScanZbar;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.util.LogUtil;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class NewScanActivity extends Activity /*implements Callback*/{ 
	private CaptureActivityHandler handler;
	private boolean hasSurface;
	private InactivityTimer inactivityTimer;
	private RelativeLayout mContainer = null;
	private RelativeLayout mCropLayout = null;
	private ImageView topMask = null;
	private DisplayMetrics dm;// 屏幕分辨率容器

	public AidlDeviceManager manager = null;
    private AidlQuickScanZbar aidlQuickScanService = null;
	private Boolean cameraBack = true;
	private Boolean lightOpen = true;
	
	private SurfaceTexture surfaceCache;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		bindService();
		setContentView(R.layout.activity_qr_scan);
		CameraManager.init(getApplication());
		 
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);

		mContainer = (RelativeLayout) findViewById(R.id.capture_containter);
		mCropLayout = (RelativeLayout) findViewById(R.id.capture_crop_layout);
		topMask = (ImageView) findViewById(R.id.top_mask);
		
		dm = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(dm);
		@SuppressWarnings("unused")
		int width = dm.widthPixels;
		int height = dm.heightPixels;

		RelativeLayout.LayoutParams topMaskParams = (RelativeLayout.LayoutParams) topMask
				.getLayoutParams(); // 取控件mGrid当前的布局参数
		topMaskParams.height = (int) (height * 217 / 852);
		topMask.setLayoutParams(topMaskParams); // 使设置好的布局参数应用到上方阴影图片

		RelativeLayout.LayoutParams cropLayoutParams = (RelativeLayout.LayoutParams) mCropLayout
				.getLayoutParams();
		cropLayoutParams.height = (int) (height * 318 / 852);
		cropLayoutParams.width = (int) (height * 318 / 852);
		mCropLayout.setLayoutParams(cropLayoutParams);
		
		ImageView mQrLineView = (ImageView) findViewById(R.id.capture_scan_line);
		TranslateAnimation mAnimation = new TranslateAnimation(
				TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE,
				0f, TranslateAnimation.RELATIVE_TO_PARENT, 0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 0.9f);
		mAnimation.setDuration(1500);
		mAnimation.setRepeatCount(-1);
		mAnimation.setRepeatMode(Animation.REVERSE);
		mAnimation.setInterpolator(new LinearInterpolator());
		mQrLineView.setAnimation(mAnimation);
		
		Intent intent = getIntent();
		if (intent != null) {
			cameraBack = intent.getBooleanExtra("cameraBack", true);
			lightOpen = intent.getBooleanExtra("lightOpen", false);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		TextureView textureView = (TextureView) findViewById(R.id.capture_preview);
		if (hasSurface) {
	        initCamera(surfaceCache, cameraBack);
		}else {
			textureView.setSurfaceTextureListener(surfaceTextureListener);
		}
	}

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        	if (!hasSurface) {
        		surfaceCache = surface;
                initCamera(surfaceCache, cameraBack);
                if(lightOpen) {
                	CameraManager.get().openLight();
                }else {
                	CameraManager.get().offLight();
                }
        	}
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        	hasSurface = false;
        	return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
	
	public void bindService() {
		Intent intent = new Intent();
		intent.setPackage("com.centerm.smartposservice");
		intent.setAction("com.centerm.smartpos.service.MANAGER_SERVICE");
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}

	/**
	 * 服务连接桥
	 */
	public ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			manager = null;
			LogUtil.print(getResources().getString(R.string.bind_service_fail));
			LogUtil.print("manager = " + manager);
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			manager = AidlDeviceManager.Stub.asInterface(service);
			LogUtil.print(getResources().getString(R.string.bind_service_success));
			LogUtil.print("manager = " + manager);
			if (null != manager) {
				try {
					aidlQuickScanService = AidlQuickScanZbar.Stub.asInterface(manager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_QUICKSCAN));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				AidlDecodeManager.getInstance().setAidlQuickScanService(aidlQuickScanService);
			}
		}
	};
	
	private void initCamera(SurfaceTexture surfaceTexture,boolean cameraBack) {
		try {
			CameraManager.get().openDriver(surfaceTexture, cameraBack);
		} catch (Exception e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this);
		}
	}

	public Handler getHandler() {
		return handler;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		unbindService(conn);
		super.onDestroy();
	}

	public void handleDecode(String result) {
		inactivityTimer.onActivity();

		Intent intent = new Intent();
		intent.putExtra("result", true);
		intent.putExtra("txtResult", result);
		setResult(12, intent);
		CameraManager.get().stopPreview();
		CameraManager.get().closeDriver();
		this.finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == 211
				|| event.getKeyCode() == KeyEvent.KEYCODE_MENU)
			return true;

		return super.onKeyDown(keyCode, event);
	}

}
