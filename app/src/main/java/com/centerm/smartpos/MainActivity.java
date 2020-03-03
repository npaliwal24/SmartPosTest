package com.centerm.smartpos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.util.CompactUtil;

public class MainActivity extends BaseActivity {
	private ListView operationList;
	private ArrayAdapter<String> adapter;

	private String[] listItem;

	private Class[] cls = new Class[] { 
			QuickScanActivity.class,
			SwipeCardActivity.class,
			PinPadActivity.class,
			PbocActivity2.class,
			ICCardActivity.class,
			IRFCardActivity.class,
			PrinterActivity.class, 
		    M1CardActivity.class,
			PsamActivity.class, SerialPortActivity.class,
			SoundPlayerActivity.class};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.main_activity);
		CompactUtil.instance(this.getApplicationContext());
		listItem = getResources().getStringArray(R.array.FUNCTION_LIST);
		operationList = (ListView) findViewById(R.id.main_list_view);
		/*
		 * adapter = new ArrayAdapter<String>(this,
		 * android.R.layout.simple_list_item_1, listItem);
		 */
		adapter = new ArrayAdapter<String>(this, R.layout.show_item2, listItem);
		operationList.setAdapter(adapter);
		operationList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, cls[position]);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onDeviceConnected(AidlDeviceManager deviceManager) {
		// TODO Auto-generated method stub
		
	}
}
