package com.centerm.newscan.decode;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.centerm.scanActivity.NewScanActivity;
import com.centerm.smartpos.aidl.qrscan.AidlQuickScanZbar;
import com.centerm.smartpos.aidl.qrscan.CTDecoder;
import com.centerm.smartpos.aidl.qrscan.DecodeResult;

public class DecodeHandler extends Handler {

	NewScanActivity activity = null;
	public static final int decode=0x7f070003;
	public static final int quit=0x7f070007;
	public static final int decode_succeeded=0x7f070005;
	public static final int decode_failed=0x7f070004;
	public static final int auto_focus=0x7f070002;
	public static final int restart_preview=0x7f070006;
	public int libChoose = 0;
	public static final int useLibc = 0;
	public static final int useZbar = 1;
	
	DecodeHandler(NewScanActivity activity) {
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
		case  decode:
			decode((byte[]) message.obj, message.arg1, message.arg2);
			break;
		case  quit:
			Looper.myLooper().quit();
			break;
		}
	}
	
	private void decode(byte[] data, int width, int height) {
		byte[] rotatedData = new byte[data.length];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++)
				rotatedData[x * height + height - y - 1] = data[x + y * width];
		}
		int tmp = width;// Here we are swapping, that's the difference to #11
		width = height;
		height = tmp;

	    String result = null;
	    if(AidlDecodeManager.getInstance().getAidlQuickScanService() == null) {
	    	Log.d("QuickScan", "aidlQuickScanService is null");
	    }else {
	    	DecodeResult mDecodeResult;
			try {
				mDecodeResult = AidlDecodeManager.getInstance().getAidlQuickScanService().decodeBarcode(width, height, rotatedData);
				if(mDecodeResult != null && mDecodeResult.getRet() > 0) {
			    	if(mDecodeResult.getDecodeData() != null) {
			    		result = new String(mDecodeResult.getDecodeData());
					}
			    }	
			} catch (RemoteException e) {
				e.printStackTrace();
			}
	    }
	    
		if (result != null) {
			if (null != activity.getHandler()) {
				Message msg = new Message();
				msg.obj = result;
				msg.what =  decode_succeeded;
				activity.getHandler().sendMessage(msg);
			}
		} else {
			if (null != activity.getHandler()) {
				activity.getHandler().sendEmptyMessage(decode_failed);
			}
		}
	}

}
