package com.centerm.smartpos;

import com.centerm.smartpos.aidl.magcard.AidlMagCard;
import com.centerm.smartpos.aidl.magcard.AidlMagCardListener;
import com.centerm.smartpos.aidl.magcard.TrackData;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.util.LogUtil;

import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;

/**
 * 刷卡设备测试操作
 * 
 * @author Tianxiaobo
 * 
 */
public class SwipeCardActivity extends BaseActivity {

	private AidlMagCard magCard = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.setContentView(R.layout.swipe_card_activity);
			super.onCreate(savedInstanceState);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		try {
			if (null != this.magCard) {
				this.magCard.stopSwipeCard();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	// 打开设备
	public void open(View v) {
		try {
			this.magCard.open();
			showMessage(getString(R.string.magcard_open_success), Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.magcard_open_exception) + e.getLocalizedMessage(), Color.RED);
		}
	}

	// 关闭设备
	public void close(View v) {
		try {
			this.magCard.close();
			showMessage(getString(R.string.magcard_close_success), Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.magcard_close_exception) + e.getLocalizedMessage(), Color.RED);
		}
	}

	// 开始刷卡
	public void start(View v) {
		showMessage(getString(R.string.magcard_swipe_please));
		try {
			this.magCard.swipeCard(60000, new AidlMagCardListener.Stub() {

				@Override
				public void onSwipeCardTimeout() throws RemoteException {
					showMessage("", getString(R.string.magcard_swipe_timeout), Color.RED);
				}

				@Override
				public void onSwipeCardSuccess(TrackData arg0)
						throws RemoteException {
					showMessage(getString(R.string.magcard_swipe_success), "", Color.GREEN);
					showMessage(getString(R.string.magcard_cno), arg0.getCardno(), Color.GREEN);
					showMessage(getString(R.string.magcard_encrypt_cno), arg0.getEncryptCardNo(), Color.GREEN);
					LogUtil.print(getString(R.string.magcard_first_len)+arg0.getFirstTrackData().length());
					LogUtil.print(getString(R.string.magcard_second_len)+arg0.getSecondTrackData().length());
					LogUtil.print(getString(R.string.magcard_third_len)+arg0.getThirdTrackData().length());
					showMessage(getString(R.string.magcard_first_data), arg0.getFirstTrackData(), Color.GREEN);
					showMessage(getString(R.string.magcard_second_data), arg0.getSecondTrackData(),
							Color.GREEN);
					showMessage(getString(R.string.magcard_third_data), arg0.getThirdTrackData(), Color.GREEN);
					showMessage(getString(R.string.magcard_encrypt_data), arg0.getEncryptTrackData(),
							Color.GREEN);
					showMessage(getString(R.string.magcard_expiry_date), arg0.getExpiryDate(), Color.GREEN);
					showMessage(getString(R.string.magcard_service_code), arg0.getServiceCode(), Color.GREEN);

				}

				@Override
				public void onSwipeCardFail() throws RemoteException {
					showMessage(getString(R.string.magcard_swipe_failed));
				}

				@Override
				public void onSwipeCardException(int arg0)
						throws RemoteException {
					showMessage("", getString(R.string.magcard_swipe_exception), Color.RED);
				}

				@Override
				public void onCancelSwipeCard() throws RemoteException {
					showMessage(getString(R.string.magcard_swipe_cancel));
				}
			});
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	// 中断刷卡
	public void stopSwipe(View v) {
		try {
			this.magCard.stopSwipeCard();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 打开提示音
	public void openTone(View v) {
		try {
			magCard.setPromptTone(0x01);
			showMessage(getString(R.string.magcard_opentone_success), Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.magcard_opemtone_exception) + e.getLocalizedMessage(), Color.RED);
		}
	}

	// 关闭提示音
	public void closeTone(View v) {
		try {
			magCard.setPromptTone(0x00);
			showMessage(getString(R.string.magcard_close_tone_success), Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.magcard_close_tone_exception) + e.getLocalizedMessage(), Color.RED);
		}
	}


	@Override
	public void onDeviceConnected(AidlDeviceManager deviceManager) {
		try {
			magCard = AidlMagCard.Stub.asInterface(deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_MAGCARD));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
