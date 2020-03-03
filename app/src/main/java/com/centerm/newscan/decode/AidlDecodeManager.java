package com.centerm.newscan.decode;

import com.centerm.smartpos.aidl.qrscan.AidlQuickScanZbar;

public class AidlDecodeManager {
	private static AidlDecodeManager mAidlDecodeManager;
	private AidlQuickScanZbar aidlQuickScanService;
	
	private AidlDecodeManager() {
		
	}
	
	public static AidlDecodeManager getInstance() {
		if(mAidlDecodeManager == null) {
			mAidlDecodeManager = new AidlDecodeManager();
		}
		return mAidlDecodeManager;
	}

	public AidlDecodeManager getmAidlDecodeManager() {
		return mAidlDecodeManager;
	}

	public void setmAidlDecodeManager(AidlDecodeManager mAidlDecodeManager) {
		this.mAidlDecodeManager = mAidlDecodeManager;
	}

	public AidlQuickScanZbar getAidlQuickScanService() {
		return aidlQuickScanService;
	}

	public void setAidlQuickScanService(AidlQuickScanZbar aidlQuickScanService) {
		this.aidlQuickScanService = aidlQuickScanService;
	}
}
