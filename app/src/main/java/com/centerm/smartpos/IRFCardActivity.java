package com.centerm.smartpos;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.centerm.smartpos.aidl.rfcard.AidlRFCard;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.util.HexUtil;

public class IRFCardActivity extends BaseActivity {

	private AidlRFCard rfcard = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.setContentView(R.layout.rfcard_activity);
		super.onCreate(savedInstanceState);
	}

	// 读取卡片类型
	public void readCardtype(View v) {
		int retVal = -1;
		try {
			retVal = this.rfcard.readCardType();
			showMessage(getResources().getString(R.string.rfcard_type)+":" + retVal, Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.rfcard_type_excp) + e.getLocalizedMessage(), Color.BLACK);
		}
	}

	// 打开
	public void open(View v) {
		try {
			this.rfcard.open();
			showMessage(getResources().getString(R.string.rfcard_open_succ), Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.rfcard_open_fail) + e.getLocalizedMessage(), Color.RED);
		}
	}

	// 查询状态
	public void status(View v) {
		byte statusVal = -1;
		try {
			statusVal = this.rfcard.status();
			showMessage(getResources().getString(R.string.rfcard_state)+":" + statusVal, Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.rfcard_state)+":" + statusVal, Color.RED);
		}
	}

	// 复位
	public void reset(View v) {
		byte[] retData = null;
		try {
			retData = this.rfcard.reset();
			if (null == retData) {
				showMessage(getResources().getString(R.string.rfcard_reset_result)+"null", Color.BLACK);
				return;
			}
			showMessage(getResources().getString(R.string.rfcard_reset_result) + HexUtil.bytesToHexString(retData),
					Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.rfcard_reset_fail) + e.getLocalizedMessage(), Color.RED);
		}
	}

	// 中断IC卡
	public void halt(View v) {
		try {
			this.rfcard.halt();
			showMessage(getResources().getString(R.string.rfcard_halt_succ), Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.rfcard_halt_fail), Color.RED);
		}
	}

	// 发送APDU
	public void sendAPDU(View v) {
		byte[] result = null;
		try {
			result = this.rfcard.send(HexUtil
					.hexStringToByte("00a404000e325041592e5359532e444446303100"));
			if (null == result) {
				showMessage(getResources().getString(R.string.rfcard_category_null), Color.RED);
				return;
			}
			showMessage(getResources().getString(R.string.rfcard_category) + HexUtil.bytesToHexString(result),
					Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.rfcard_category_excp) + e.getLocalizedMessage(), Color.RED);
		}
	}
	
	// 异步发送APDU
	public void sendAPDUasync(View v) {
		byte[] result = null;
		try {
			result = this.rfcard
					.sendAsync(HexUtil
							.hexStringToByte("00a404000e325041592e5359532e444446303100"));
			if (null == result) {
				showMessage(getResources().getString(R.string.rfcard_category_null), Color.RED);
				return;
			}
			showMessage(getResources().getString(R.string.rfcard_category) + HexUtil.bytesToHexString(result),
					Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.rfcard_category_excp) + e.getLocalizedMessage(), Color.RED);
		}
	}
	
	// 关闭
	public void close(View v) {
		try {
			this.rfcard.close();
			showMessage(getResources().getString(R.string.rfcard_close_succ), Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.rfcard_close_excp) + e.getLocalizedMessage(), Color.RED);
		}
	}

	@Override
	public void onDeviceConnected(AidlDeviceManager deviceManager) {
		try {
			rfcard = AidlRFCard.Stub.asInterface(deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_RFCARD));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
