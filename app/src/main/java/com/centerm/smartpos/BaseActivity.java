package com.centerm.smartpos;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.util.LogUtil;

public abstract class BaseActivity extends Activity {
	public static final int SHOW_MSG = 0;

	private int showLineNum = 0;

	public LinearLayout linearLayout;
	private ScrollView scrollView;
	private TextView textView1;
	private TextView textView2;

	public LinearLayout rightButArea = null;

	public AidlDeviceManager manager = null;


	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			String msg1 = bundle.getString("msg1");
			String msg2 = bundle.getString("msg2");
			int color = bundle.getInt("color");
			updateView(msg1, msg2, color);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// super.setContentView(R.layout.base_activity);
		linearLayout = (LinearLayout) this.findViewById(R.id.tipLinearLayout);
		scrollView = (ScrollView) this.findViewById(R.id.tipScrollView);
		rightButArea = (LinearLayout) this.findViewById(R.id.main_linearlayout);
		
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		if(!this.getClass().getName().equals(MainActivity.class)){
			bindService();
		}
		
		
	}
	
 class MyBroadCastReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("base", "action:" +intent.getAction());
		
	}
	 
 }
 
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
				onDeviceConnected(manager);
			}
		}
	};
	
	//清屏
	public void clear(){
		linearLayout.removeAllViews();
	}
	
	/**
	 * 显示信息
	 * 
	 * @param msg
	 * @param color
	 * @createtor：Administrator
	 * @date:2014-9-15 下午9:45:18
	 */
	public void updateView(final String msg1, final String msg2, final int color) {
		if (showLineNum % 50 == 0) { // 显示够20行的时候重新开始
			linearLayout.removeAllViews();
			showLineNum = 0;
		}
		showLineNum++;
		LayoutInflater inflater = getLayoutInflater();
		View v = inflater.inflate(R.layout.show_item, null);
		textView1 = (TextView) v.findViewById(R.id.tip1);
		textView2 = (TextView) v.findViewById(R.id.tip2);
		textView1.setText(msg1);
		textView2.setText(msg2);
		textView1.setTextColor(Color.BLACK);
		textView2.setTextColor(color);
		linearLayout.addView(v);
		scrollView.post(new Runnable() {
			public void run() {
				scrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});

	}

	/**
	 * 更新UI
	 * 
	 * @param msg1
	 * @param msg2
	 * @param color
	 * @createtor：Administrator
	 * @date:2014-11-29 下午7:01:16
	 */
	public void showMessage(final String msg1, final String msg2,
			final int color) {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("msg1", msg1);
		bundle.putString("msg2", msg2);
		bundle.putInt("color", color);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	// 显示单条信息
	public void showMessage(final String msg1, final int color) {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("msg1", msg1);
		bundle.putString("msg2", "");
		bundle.putInt("color", color);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	public void showMessage(String str) {
		this.showMessage(str, Color.BLACK);
	}

	@Override
	protected void onPause() {
		super.onPause();
//		if(!this.getClass().getName().equals(MainActivity.class)){	//首页不执行解除绑定服务操作
//			unbindService(conn);
//		}
	}

	/**
	 * 设备服务连接成功时回调
	 * @param deviceManager
	 * @createtor：Administrator
	 * @date:2015-5-4 下午1:52:13
	 */
	public abstract void onDeviceConnected(AidlDeviceManager deviceManager);
}
