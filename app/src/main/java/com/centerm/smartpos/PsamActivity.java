package com.centerm.smartpos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.centerm.smartpos.aidl.psam.AidlPsam;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.util.HexUtil;

/**
 * psam卡测试操作
 * 
 * @author Tianxiaobo
 * 
 */
public class PsamActivity extends BaseActivity {

	private AidlPsam psam1 = null;
	private AidlPsam psam2 = null;
	private AidlPsam psam3 = null;

	private AidlPsam curPsam = null;
	private String sendCmd = "00A4040005D26800000100";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.setContentView(R.layout.psam_activity);
		super.onCreate(savedInstanceState);
	}

	public void psam1Selected(View v) {
		curPsam = psam1;
		showMessage(getString(R.string.psam_chose_type));
	}

	public void psam2Selected(View v) {
		curPsam = psam2;
		showMessage(getString(R.string.psam_chose_type));
	}

	public void psam3Selected(View v) {
		curPsam = psam3;
		showMessage(getString(R.string.psam_chose_type));
	}

	// 打开
	public void open(View v) {
		try {
			boolean flag = curPsam.open();
			if (flag) {
				showMessage(getString(R.string.psam_open_success));
			} else {
				showMessage(getString(R.string.psam_open_failed));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 复位
	public void reset(View v) {
		try {
			byte[] ret = curPsam.reset();
			if (null != ret) {
				showMessage(getString(R.string.psam_reset_success)
						+ HexUtil.bytesToHexString(ret));
			} else {
				showMessage(getString(R.string.psam_reset_null));
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.psam_reset_exception)
					+ e.getLocalizedMessage());
		}
	}

	// 发送APDU
	public void sendAPDU(View v) {
		try {
			inputCmd();
			
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.psam_send_apdu_exception));
		}
	}

	private void inputCmd() {
		final EditText et = new EditText(this);
		et.setHint("00A4040005D26800000100");
		
		new AlertDialog.Builder(this).setTitle("apdu")
				.setIcon(android.R.drawable.ic_dialog_info).setView(et)
				.setPositiveButton("确定", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String input = et.getText().toString().trim();
						if (input.equals("")) {
							sendCmd = "00A4040005D26800000100";
						} else {
							sendCmd = input.toUpperCase();
						}
						
						try {
							
							byte[] ret = curPsam.sendApdu(HexUtil
									.hexStringToByte(sendCmd));

							if (null != ret) {
								showMessage(getString(R.string.psam_send_apdu)
										+ HexUtil.bytesToHexString(ret));
							} else {
								showMessage(getString(R.string.psam_send_apdu_null));
							}
						} catch (Exception e) {
							e.printStackTrace();
							showMessage(getString(R.string.psam_send_apdu_exception));
						}
						
						return;
					}
				}).setNegativeButton("取消", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
				}).setCancelable(false).show();

	}

	// 设置ETU时间,取值为0、1、2
	public void setEtu(View v) {
		try {
			boolean flag = curPsam.setETU((byte) 2);
			showMessage(getString(R.string.psam_set_etu_result) + flag);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.psam_set_etu_exception));
		}
	}

	// 关闭
	public void close(View v) {
		boolean flag;
		try {
			flag = curPsam.close();
			if (flag) {
				showMessage(getString(R.string.psam_close_success));
			} else {
				showMessage(getString(R.string.psam_close_failed));
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.psam_close_exception));
		}
	}

	@Override
	public void onDeviceConnected(AidlDeviceManager deviceManager) {
		try {
			this.psam1 = AidlPsam.Stub.asInterface(deviceManager
					.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_PSAM1));
			this.psam2 = AidlPsam.Stub.asInterface(deviceManager
					.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_PSAM2));
			this.psam3 = AidlPsam.Stub.asInterface(deviceManager
					.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_PSAM3));
			curPsam = this.psam1;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
