package com.centerm.newscan.decode;

import android.app.Activity;
import android.content.DialogInterface;
/**
 * @time   2017-03-20
 * @author wangwenxun@centerm.com
 * @func   【配合快速扫描快速解析接口】活动结束监听者。当扫描出结果或者超时之后，该监听者调用接口结束扫描活动返回结果
 */
public final class FinishListener implements DialogInterface.OnClickListener,
		DialogInterface.OnCancelListener, Runnable {

	private final Activity activityToFinish;

	public FinishListener(Activity activityToFinish) {
		this.activityToFinish = activityToFinish;
	}

	public void onCancel(DialogInterface dialogInterface) {
		run();
	}

	public void onClick(DialogInterface dialogInterface, int i) {
		run();
	}

	public void run() {
		activityToFinish.finish();
	}

}
