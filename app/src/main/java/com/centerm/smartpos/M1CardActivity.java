package com.centerm.smartpos;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.centerm.smartpos.aidl.mifare.AidlMifare;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.util.HexUtil;

public class M1CardActivity extends BaseActivity {

	private AidlMifare m1card = null;
	private EditText addrEt;
	private EditText dataEt;

	private byte[] resetData = null;
	private byte addid = (byte) 6;// 数据块

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.setContentView(R.layout.m1card_activity);
		super.onCreate(savedInstanceState);
		addrEt = (EditText) findViewById(R.id.m1_addr);
		dataEt = (EditText) findViewById(R.id.m1_data);
	}

	@Override
	public void onDeviceConnected(AidlDeviceManager deviceManager) {
		try {
			this.m1card = AidlMifare.Stub.asInterface(deviceManager
					.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_MIFARE));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 打开
	public void open(View v) {
		try {
			this.m1card.open();
			showMessage(getResources().getString(R.string.rfcard_open_succ),
					Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(
					getResources().getString(R.string.rfcard_open_fail)
							+ e.getLocalizedMessage(), Color.RED);
		}
	}

	// 关闭
	public void close(View v) {
		try {
			this.m1card.close();
			showMessage(getResources().getString(R.string.rfcard_close_succ),
					Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.rfcard_close_excp)
					+ e.getLocalizedMessage(), Color.RED);
		}
	}

	// 状态
	public void status(View v) {
		getId();

		byte statusVal = -1;
		try {
			statusVal = this.m1card.status();
			showMessage(getResources().getString(R.string.m1card_state) + ":"
					+ statusVal, Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.m1card_state) + ":"
					+ statusVal, Color.RED);
		}
	}

	// 非接卡复位
	public void reset(View v) {
		getId();

		resetData = null;
		try {
			resetData = this.m1card.reset();
			if (null == resetData) {
				showMessage(
						getResources().getString(R.string.rfcard_reset_result)
								+ ":" + "null", Color.BLACK);
				return;
			}
			showMessage(getResources().getString(R.string.rfcard_reset_result)
					+ HexUtil.bytesToHexString(resetData), Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.rfcard_reset_fail)
					+ e.getLocalizedMessage(), Color.RED);
		}
	}

	// 认证
	public void auth(View v) {
		getId();

		try {
			showMessage(getResources().getString(R.string.m1card_authenticate));
			byte[] keyA = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
					(byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
			// int ret = m1card.auth(0x00, (byte) 9, keyA, data);
			int ret = m1card.auth(0x00, addid, keyA, resetData);
			showMessage(getResources().getString(R.string.returns) + ret);
		} catch (Exception e) {
			showMessage(getResources().getString(R.string.excute_fail));
		}
	}

	// 读数据
	public void readBlock(View v) {
		getId();

		// 用于接收返回的块数据
		try {
			showMessage(getResources().getString(
					R.string.m1card_block_date_read));
			byte[] wData = new byte[24];
			int ret;
			ret = m1card.readBlock(addid, wData);
			if (ret == 0) {
				showMessage(getResources().getString(
						R.string.m1card_recent_data)
						+ HexUtil.bcd2str(wData));
			} else {
				showMessage(getResources().getString(
						R.string.m1card_block_data_read_fail)
						+ ","
						+ getResources().getString(R.string.returns)
						+ ret);
			}
		} catch (Exception e) {
			showMessage(getResources().getString(R.string.excute_fail));
		}

	}

	// begin 2017/02/27 15:46 英文适配 wengtao@centerm.com
	// 写数据
	public void writeBlock(View v) {
		getId();

		byte[] wData = dataEt.getText().toString().getBytes();
		byte[] d = new byte[16];

		if (wData != null && wData.length>=4) {
			d[0] = wData[0];
			d[1] = wData[1];
			d[2] = wData[2];
			d[3] = wData[3];
		}
		
		try {
			int ret = m1card.writeBlock(addid, d);
			if (ret == 0) {
				showMessage(getString(R.string.m1card_block_data_write_succ));
			} else {
				showMessage(getResources().getString(
						R.string.m1card_block_data_write_fail)
						+ ","
						+ getResources().getString(R.string.returns)
						+ ret);
			}

		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.excute_fail));
		}
	}

	// 加值
	public void addValue(View v) {
		getId();

		try {
			showMessage(getString(R.string.m1card_add_value));
			byte[] redData = new byte[] { 0x01, 0x02, 0x03, 0x04 };

			int ret = m1card.addValue(addid, null, redData);
			if (ret == 0) {
				showMessage(getString(R.string.m1card_add_value_succ));
			} else {
				showMessage(getResources().getString(
						R.string.m1card_add_value_fail)
						+ ","
						+ getResources().getString(R.string.returns)
						+ ret);
			}

		} catch (Exception e) {
			showMessage(getResources().getString(R.string.excute_fail));
		}
	}

	// 减值
	public void reduceValue(View v) {
		getId();

		try {
			showMessage(getString(R.string.m1card_sub_value));
			byte[] redData = new byte[] { 0x01, 0x02, 0x03, 0x04 };

			int ret = m1card.reduceValue(addid, null, redData);
			if (ret == 0) {
				showMessage(getString(R.string.m1card_sub_value_succ));
			} else {
				showMessage(getResources().getString(
						R.string.m1card_sub_value_fail)
						+ ","
						+ getResources().getString(R.string.returns)
						+ ret);
			}
		} catch (Exception e) {
			showMessage(getResources().getString(R.string.excute_fail));
		}
	}

	// end 2017/02/27 15:46

	private void getId() {

		String aet = addrEt.getText().toString();
		if (!TextUtils.isEmpty(aet)) {
			addid = (byte) (int) (Integer.valueOf(aet));
		}

	}

}
