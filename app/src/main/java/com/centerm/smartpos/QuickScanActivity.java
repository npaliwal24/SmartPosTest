package com.centerm.smartpos;

import java.util.HashMap;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.centerm.scanActivity.NewScanActivity;
import com.centerm.smartpos.aidl.qrscan.AidlQuickScanZbar;
import com.centerm.smartpos.aidl.qrscan.AidlScanCallback;
import com.centerm.smartpos.aidl.qrscan.CameraBeanZbar;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
 
/**
 * @time   2018-07-24
 * @author linpeita@centerm.com
 * @func   快速扫描模块
 * @attention 该接口从CpaySDK3.1.3版本开始支持
 * @attention 为了演示方便，这里针对不同的设备进行了兼容，请各位使用者根据自己对应的机器类型进行参数设置即可
 */
public class QuickScanActivity extends BaseActivity {
	//硬件设备相关参数
	private Camera camera;
	private boolean cameraBack ; 
	private boolean lightOpen;
	private boolean isBigScreen = false;
	private boolean changeScreen = false;
	private int bigSize = 9604;//默认9604
	private int bestWidth = 640;
	private int bestHeight = 480;
	private int spinDegree = 90;
	//解析库选择
	private static final int bigScreenWidth = 1024;
	private static final int bigScreenHeight = 768;
	private static final int bigerScreenWidth = 1280;
	private static final int bigerScreenHeight = 800;
	//摄像头显示效果
	private int cameraDisplayEffect = 0;
	private static final int cameraDisplayEffect_Normal = 0;
	private static final int cameraDisplayEffect_Scan = 1;
	//demo中的UI控件
	private RadioGroup cameraRg;
	private RadioGroup lightRg;
	private RadioGroup cameraDisplayRg; //2018-03-05 增加摄像头显示效果切换按钮 linpeita@centerm.com
	private int currentApiVersion = Build.VERSION.SDK_INT;
	private int systemVersion ;//2017-03-30 增加systemVersion参数用于辅助进行大屏设备型号判断 wnagwenxun@centerm.com
    private AidlQuickScanZbar aidlQuickScanService = null;//2017-04-25 添加相关服务对象 wangwenxun@centerm.com
	
    public long quickScanStartTime4;	
    
    @SuppressWarnings({ "deprecation", "static-access" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.setContentView(R.layout.quickscan_activity);
		super.onCreate(savedInstanceState);
//		avaliableBtn   = (Button)findViewById(R.id.quickscan_centerm_lib_avaliable_btn);
		//控制初始闪光灯亮灭
		lightRg = (RadioGroup) findViewById(R.id.light_rg);
		lightRg.setOnCheckedChangeListener(lightCheckedListsner);
		lightRg.check(R.id.light_off);
		//控制摄像头显示效果
		cameraDisplayRg = (RadioGroup) findViewById(R.id.camera_display_rg);
		cameraDisplayRg.setOnCheckedChangeListener(cameraDisplayCheckedListsner);
		
		//进行不同屏幕的适配
		 WindowManager windowManager = getWindowManager();    
	     Display display = windowManager.getDefaultDisplay();    
	     int screenWidth = display.getWidth();    
	     int screenHeight = display.getHeight();
	     if((screenWidth>=bigScreenWidth)&&(screenHeight>=bigScreenHeight)){
	    	 //如果碰到大屏幕有两种解决方案，一种是修改分辨率参数，一种是修改旋转角度参数
	    	 isBigScreen = true;
	    	 if(changeScreen){
	    	 bestWidth  = screenWidth; 
	    	 bestHeight = screenHeight;}
	    	 else{
	    		 spinDegree = 0;
	    	 }
	     }
	     //begin 2017-03-30   大屏设备进行机器型号判断 wangwenxun@centerm.com
	     //9605 C960F Android5.1
	     //9604 C960F Android4.2
	     //9505 C960E 5.0
	     //9506 C960E 6.0
	     if(isBigScreen){
	    	  
	    	 if((screenWidth==bigerScreenWidth)&&(screenHeight==bigerScreenHeight)){
	    		 if(currentApiVersion==17){
	    			 bigSize = 9605;	 
	    		 }else{
	    			 bigSize = 9604;
	    		 } 
	    	 }else if((screenWidth==bigScreenWidth)&&(screenHeight==bigScreenHeight)){
	    		 systemVersion = Integer.parseInt((String) android.os.Build.BRAND.subSequence(0, 1));
                 if(systemVersion==5){
                	 bigSize = 9505;
                 }else{
                	 bigSize = 9506;
                 }
	    	 }
	     }
	     //end 2017-03-30
	    
	     //begin 2017-03-03 根据硬件设备具体情况来决定摄像头前后置，如果硬件设备没有摄像头，那么该功能不可使用
	     //wangwenxun@centerm.com
	     //控制摄像头前后置
			cameraRg = (RadioGroup) findViewById(R.id.camera_rg);
			cameraRg.setOnCheckedChangeListener(checkedListsner);
			cameraRg.check(R.id.camera_back);
			if(camera.getNumberOfCameras()<2){
				cameraRg.setVisibility(View.GONE);
				if((FindBackCamera()==-1)&&(FindFrontCamera()==-1)){
					showMessage("未检测到摄像头，扫码功能不可使用，按键将不可点击，请返回");
				}else if(FindBackCamera()==-1){
					//如果没有后置摄像头而有前置
					if((bigSize==9605)||(bigSize==9505)){
						cameraBack = true;
					}else{
						cameraBack = false;
					}
				}else if(FindFrontCamera()==-1){
					//如果没有前置摄像头而有后置
					if((bigSize==9605)||(bigSize==9505)){
						cameraBack = false;
					}else{
						cameraBack = true;
					}
				}
			}
			//end 2018-03-03
		 
	}
	
	private OnCheckedChangeListener checkedListsner = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.camera_back:
				cameraBack = true;
				break;
			case R.id.camera_front:
				cameraBack = false;
				break;
			}
		}
	};
	
	private OnCheckedChangeListener lightCheckedListsner = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.light_up:
				lightOpen = true;
				break;
			case R.id.light_off:
				lightOpen = false;
				break;
			}
		}
	};
	
	private OnCheckedChangeListener cameraDisplayCheckedListsner = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.camera_display_normal:
				cameraDisplayEffect = cameraDisplayEffect_Normal;
				break;
			case R.id.camera_display_scan:
				cameraDisplayEffect = cameraDisplayEffect_Scan;
				break;
			}
		}
	};
	
	@Override
	public void onResume(){
		super.onResume();
	}

    //清空屏幕显示
	public void clearDisplay(View view) {
		clear();
	}

	/**
	 * 29017-03-09 wangwenxun@centerm.com
	 * 所有的接口都是通过onActivityResult接收结果
	 * 如果使用的库是useLibc，那么resultCode等于9，如果使用的库是useZbar，那么resultCode等于8
	 * resultCode智能等于9或者8，如果等于其他的则无法接收结果
	 */

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (data != null) {
			boolean libload = data.getBooleanExtra("libload", true);
			if (libload) {
				String txt = data.getStringExtra("txtResult");
				if (!TextUtils.isEmpty(txt)) {
					if(resultCode == 12){
						 long endTime4 = data.getLongExtra("ScanCostTime", System.currentTimeMillis()); 
						 long quickScanCostTime3 = endTime4 -quickScanStartTime4 ;
						    showMessage("基础服务解码接口耗时:"+quickScanCostTime3);
					}
					showMessage(getString(R.string.scan_success) + "\n" + getString(R.string.scan_info) + "\n"+ txt);
				} else {
					showMessage(getString(R.string.scan_fail));
				}
			} else {
				showMessage(getString(R.string.centerm_lib_not_avaliable));
			}
		} else {
			showMessage(getString(R.string.scan_fail));
		}
	}


	@Override
	public void onDeviceConnected(AidlDeviceManager deviceManager) {
		// 每次调用接口前必须先初始化这两个类
		try {
		//2017-04-25  每次调用前必须先绑定对应的服务，并在服务中进行相应的初始化操作 wangwenxun@centerm.com
			aidlQuickScanService = AidlQuickScanZbar.Stub.asInterface(deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_QUICKSCAN));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
   /** time:2017-03-03 
     * func:摄像头判断函数,这两个参数可以用于判断是前置摄像头还是后置摄像头是否存在
     * Two means used to judge whether the front Camera or the Back Camera exits
	 * author:linpeita@centerm.com
	 * adder: wangwenxun@centerm.com 
	 */
	
	private int FindFrontCamera() {
		int cameraCount = 0;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras(); // get cameras number

		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				// 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
				return camIdx;
			}
		}
		return -1;
	}

	private int FindBackCamera() {
		int cameraCount = 0;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras(); // get cameras number

		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				// 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
				return camIdx;
			}
		}
		return -1;
	}
	
	public void stsartFastScan(View view) {
		final long startTime = System.currentTimeMillis();
		try {
			//cameraID           int     摄像头ID
			//width              int     预览宽
			//height             int     预览高
			//lightMode          int     闪光灯模式
			//time               int     扫描时间（ms）
			//spindegree         int     旋转角度（ScanQRCode接口无效）
			//beep               int     蜂鸣次数（ScanQRCode接口无效）
			//ExternalMap Key：
			//Persist            boolean 是否需要持续扫码
			//ShowPreview        boolean 是否需要前置预览
			//ScanEffect         boolean 是否需要扫码模式
			CameraBeanZbar cameraBean = new com.centerm.smartpos.aidl.qrscan.CameraBeanZbar(0, bestWidth, bestHeight, 4, Integer.MAX_VALUE, spinDegree, 1);
			if(cameraBack){
				cameraBean.setCameraId(0);
			}else{
				cameraBean.setCameraId(1);
			}
			if (lightOpen) {
				cameraBean.setLightMode(2);
			}
			HashMap<String,Object> externalMap = new HashMap<String,Object>();
		    externalMap.put("ShowPreview", true);
		    externalMap.put("ScanEffect", cameraDisplayEffect == 1);
			cameraBean.setExternalMap(externalMap);
//			switchCameraDisplayEffect();//2018-03-06 增加切换摄像头显示效果 linpeita@centerm.com
		    aidlQuickScanService.scanQRCode(cameraBean, new AidlScanCallback.Stub() {
				@Override
				public void onFailed(int arg0) throws RemoteException {
					showMessage(getString(R.string.scan_fail));
				    
				}
				
				@Override
				public void onCaptured(String arg0, int arg1) throws RemoteException {
					showMessage(getString(R.string.scan_success) + "\n" + getString(R.string.scan_info) + "\n"+ arg0);
				    long SuccessEndTime = System.currentTimeMillis();
				    long SuccessCostTime = SuccessEndTime - startTime;
				    showMessage("基础服务接口扫码耗时 = "+SuccessCostTime);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** 
	 * time:2018-03-06
     * func:摄像头显示效果切换
     * 
	 * cameraType：1，前置摄像头显示效果；2，后置摄像头显示效果。
	 * mode：1，扫码效果；0，正常效果。
	 * 
	 * 扫码效果：  专门用于屏幕码快速移动扫码；（显示效果较暗，不适合纸质码扫码需求）
	 * 正常效果：  可用于正常拍照、预览和扫码（包括纸质码和屏幕码），不支持屏幕码快速移动扫码功能。
	 * 
	 * author:linpeita@centerm.com
	 */
	private void switchCameraDisplayEffect() {
		try {
			aidlQuickScanService.switchCameraDisplayEffect(cameraBack?0:1, cameraDisplayEffect);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stsartFastDecode(View v) {
		quickScanStartTime4 = System.currentTimeMillis();
		Intent intent = new Intent();
		intent.setClass(QuickScanActivity.this, NewScanActivity.class);
		intent.putExtra("cameraBack", cameraBack);
		intent.putExtra("lightOpen", lightOpen);
		startActivityForResult(intent, 12);
	}
}
	
	 
	 	 