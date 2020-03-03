package com.centerm.newscan.decode;

import android.os.Handler;
import android.os.Message;

import com.centerm.newscan.camera.CameraManager;
import com.centerm.scanActivity.NewScanActivity;
import com.centerm.smartpos.aidl.qrscan.AidlQuickScanZbar;

public class CaptureActivityHandler extends Handler {

	DecodeThread decodeThread = null;
	NewScanActivity activity = null;
	private State state;

	private enum State {
		PREVIEW, SUCCESS, DONE
	}

	public CaptureActivityHandler(NewScanActivity activity) {
		this.activity = activity;
		decodeThread = new DecodeThread(activity);
		decodeThread.start();
		state = State.SUCCESS;
		CameraManager.get().startPreview();
		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {

		switch (message.what) {
		case DecodeHandler.auto_focus:
			if (state == State.PREVIEW) {
				CameraManager.get().requestAutoFocus(this, DecodeHandler.auto_focus);
			}
			break;
		case DecodeHandler.restart_preview:
			restartPreviewAndDecode();
			break;
		case DecodeHandler.decode_succeeded:
			state = State.SUCCESS;
			activity.handleDecode((String) message.obj);// 解析成功，回调
			break;

		case DecodeHandler.decode_failed:
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					DecodeHandler.decode);
			break;
		}

	}

	public void quitSynchronously() {
		state = State.DONE;
		CameraManager.get().stopPreview();
		removeMessages(DecodeHandler.decode_succeeded);
		removeMessages(DecodeHandler.decode_failed);
		removeMessages(DecodeHandler.decode);
		removeMessages(DecodeHandler.auto_focus);
		decodeThread.getHandler().sendEmptyMessage(DecodeHandler.quit);
	}

	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					DecodeHandler.decode);
			CameraManager.get().requestAutoFocus(this, DecodeHandler.auto_focus);
		}
	}

}
