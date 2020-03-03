package com.centerm.smartpos;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.centerm.smartpos.aidl.iccard.AidlICCard;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.util.HexUtil;

/**
 * 接触式IC卡设备
 * 
 * @author Tianxiaobo
 * 
 */
public class ICCardActivity extends BaseActivity {

	private AidlICCard iccard = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.setContentView(R.layout.iccard_activity);
		super.onCreate(savedInstanceState);
	}

	// 打开
	public void open(View v) {
		try {
			this.iccard.open();
			showMessage(getResources().getString(R.string.iccard_open_succ), Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.iccard_open_fail) + e.getLocalizedMessage(), Color.RED);
		}
	}

	// 查询状态
	public void status(View v) {
		byte statusVal = -1;
		try {
			statusVal = this.iccard.status();
			showMessage(getResources().getString(R.string.iccard_state_result) + statusVal, Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.iccard_state_result) + statusVal, Color.RED);
		}
	}

	// 复位
	public void reset(View v) {
		byte[] retData = null;
		try {
			retData = this.iccard.reset();
			showMessage(getResources().getString(R.string.iccard_reset_result) + (retData == null ? "null" :HexUtil.bytesToHexString(retData)),
					Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.iccard_reset_fail) + e.getLocalizedMessage(), Color.RED);
		}
	}

	// 中断IC卡
	public void halt(View v) {
		try {
			this.iccard.halt();
			showMessage(getResources().getString(R.string.iccard_interrupt_succ), Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.iccard_interrupt_fail), Color.RED);
		}
	}

	// 发送APDU
	public void sendAPDU(View v) {
		byte[] result = null;
		try {
			result = this.iccard.send(HexUtil
					.hexStringToByte("00A404000E315041592E5359532E4444463031"));
			if (null != result) {
				showMessage(getResources().getString(R.string.iccard_choose_catelog_result) + HexUtil.bytesToHexString(result),
						Color.BLACK);
			} else {
				showMessage("", getResources().getString(R.string.iccard_APDU_fail_and_reset), Color.RED);
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.iccard_choose_catelog_excp) + e.getLocalizedMessage(), Color.RED);
		}
	}
	
	// 异步发送APDU指令
	public void sendAPDUasync(View v) {
		byte[] result = null;
		try {
			result = this.iccard.sendAsync(HexUtil
					.hexStringToByte("00A404000E315041592E5359532E4444463031"));
			if (null != result) {
				showMessage(getResources().getString(R.string.iccard_choose_catelog_result) + HexUtil.bytesToHexString(result),
						Color.BLACK);
			} else {
				showMessage("", getResources().getString(R.string.iccard_APDU_fail_and_reset), Color.RED);
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage("", getResources().getString(R.string.iccard_APDU_fail_and_reset), Color.RED);
		}
	}

	// 关闭
	public void close(View v) {
		try {
			this.iccard.close();
			showMessage(getResources().getString(R.string.iccard_close_succ), Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.iccard_close_excp) + e.getLocalizedMessage(), Color.RED);
		}
	}

	@Override
	public void onDeviceConnected(AidlDeviceManager deviceManager) {
		try {
			this.iccard = AidlICCard.Stub.asInterface(deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_ICCARD));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
