package com.centerm.newscan.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
/**
 * @time   2017-03-20
 * @author wangwenxun@centerm.com
 * @func   【配合快速扫描快速解析接口】自动对焦回调类
 */
public class AutoFocusCallback implements Camera.AutoFocusCallback {

	private static final String TAG = AutoFocusCallback.class.getSimpleName();

	private static final long AUTOFOCUS_INTERVAL_MS = 500L;

	private Handler autoFocusHandler;
	private int autoFocusMessage;

	void setHandler(Handler autoFocusHandler, int autoFocusMessage) {
		this.autoFocusHandler = autoFocusHandler;
		this.autoFocusMessage = autoFocusMessage;
	}

	public void onAutoFocus(boolean success, Camera camera) {
		if (autoFocusHandler != null) {
			Message message = autoFocusHandler.obtainMessage(autoFocusMessage,
					success);
			autoFocusHandler.sendMessageDelayed(message, AUTOFOCUS_INTERVAL_MS);
			autoFocusHandler = null;
		} else {
			Log.d(TAG, "Got auto-focus callback, but no handler for it");
		}
	}

}
