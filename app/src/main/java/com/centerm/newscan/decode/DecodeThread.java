package com.centerm.newscan.decode;

import java.util.concurrent.CountDownLatch;

import android.os.Handler;
import android.os.Looper;

import com.centerm.scanActivity.NewScanActivity;

public class DecodeThread extends Thread {

	NewScanActivity activity;
	private Handler handler;
	private final CountDownLatch handlerInitLatch;
	
	DecodeThread(NewScanActivity activity) {
		this.activity = activity;
		handlerInitLatch = new CountDownLatch(1);
	}

	Handler getHandler() {
		try {
			handlerInitLatch.await();
		} catch (InterruptedException ie) {
		}
		return handler;
	}

	@Override
	public void run() {
		Looper.prepare();
		handler = new DecodeHandler(activity);
		handlerInitLatch.countDown();
		Looper.loop();
	}

}
