package com.centerm.smartpos;
 

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.centerm.smartpos.DownloadKeyDialog.DownloadKeyListener;
import com.centerm.smartpos.GetMacDialog.GetMacListener;
import com.centerm.smartpos.aidl.pinpad.AidlPinPad;
import com.centerm.smartpos.aidl.pinpad.MacInfo;
import com.centerm.smartpos.aidl.pinpad.OnSignListener;
import com.centerm.smartpos.aidl.pinpad.PinInfo;
import com.centerm.smartpos.aidl.pinpad.PinPadBuilder;
import com.centerm.smartpos.aidl.pinpad.PinPadDownloadKeyCallback;
import com.centerm.smartpos.aidl.pinpad.PinPadInputPinCallback;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.util.HexUtil;
import com.centerm.smartpos.util.LogUtil;

/**
 * 获取PIN输入
 * 
 * @author Tianxiaobo
 * 
 */
public class PinPadActivity extends BaseActivity {

	private AidlPinPad pinPad = null;// 内置密码键盘
	private AidlPinPad pinPadex = null;// 外置密码键盘
	private AidlPinPad pinPadmetal = null;// c1010

	private AidlPinPad curpinPad = null;// 当前选中的密码键盘
	private Spinner spBautate;

	private DownloadKeyDialog keyDialog;
	private GetMacDialog macDialog;
	private AidlDeviceManager deviceManager;

	// 定义所有的回调接口
	// 输入PIN接口回调
	private PinPadInputPinCallback pinCallback = new PinPadInputPinCallback.Stub() {

		@Override
		public void onError(int arg0, String arg1) throws RemoteException {
			showMessage("", getResources().getString(R.string.pin_input_error)
					+ arg0 + getResources().getString(R.string.error_info)
					+ arg1, Color.RED);
		}

		@Override
		public void onReadPinCancel() throws RemoteException {
			showMessage("",
					getResources().getString(R.string.pin_input_cancel),
					Color.BLACK);
		}

		@Override
		public void onReadPinException() throws RemoteException {
			showMessage("", getResources().getString(R.string.pin_get_excp),
					Color.RED);
		}

		@Override
		public void onReadPinSuccess(byte[] arg0) throws RemoteException {
			if (arg0.length == 0) {
				showMessage(
						getResources().getString(
								R.string.pin_get_succ_and_null_psw),
						HexUtil.bytesToHexString(arg0), Color.GREEN);
			} else {
				showMessage(getResources().getString(R.string.pin_input_succ),
						HexUtil.bytesToHexString(arg0), Color.GREEN);
			}

		}

		@Override
		public void onReadPinTimeout() throws RemoteException {
			showMessage(getResources().getString(R.string.pin_input_timeout),
					"", Color.RED);
		}

		@Override
		public void onReadingPin(int arg0, String arg1) throws RemoteException {
			if (arg0 == 0x00) { // 长度为0，清屏
				// clear();
				showMessage(getResources().getString(
						R.string.pin_input_in_pinpad));
				return;
			}
			if (arg1 != null) {
				showMessage("", arg1, Color.RED); // 用户正在输入，此时回显星号
			} else {
				String str = "";
				while (arg0-- > 0) {
					str += "*";
				}
				showMessage(getResources().getString(R.string.inputing), str,
						Color.GREEN); // 用户正在输入，此时回显星号
			}
		}

	};

	// 密钥下载回调
	private PinPadDownloadKeyCallback downloadKeyCallback = new PinPadDownloadKeyCallback.Stub() {

		@Override
		public void onKeyDownloadSuccess() throws RemoteException {
			showMessage("", getResources().getString(R.string.key_insert_succ),
					Color.GREEN);
		}

		@Override
		public void onError(int arg0, String arg1) throws RemoteException {
			showMessage(getResources().getString(R.string.key_dl_error) + arg0
					+ getResources().getString(R.string.error_info) + arg1,
					Color.RED);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.setContentView(R.layout.keyboard_activity);
		super.onCreate(savedInstanceState);

		spBautate = (Spinner) findViewById(R.id.baudrate);
	}

	public void expinpadSelected(View v) {
		if (pinPadex == null) {
			try {
				pinPadex = AidlPinPad.Stub.asInterface(deviceManager
						.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_EXPINPAD));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		curpinPad = pinPadex;
		LogUtil.print("curpinPad: pinPadex");
	}

	public void pinpadSelected(View v) {
		curpinPad = pinPad;
		LogUtil.print("curpinPad: pinPad");
	}

	public void metalpinpadSelected(View v) {
		curpinPad = pinPadmetal;
		LogUtil.print("curpinPad: pinPadmetal");
	}

	// 获取随机数
	public void getRandom(View v) {
		if (!isAvailable()) {
			return;
		}

		LogUtil.print(getResources().getString(R.string.ran_num_get));
		try {
			byte[] random = curpinPad.getRandom();
			showMessage(getResources().getString(R.string.ran_num_get_succ)
					+ HexUtil.bytesToHexString(random), Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.ran_num_get_excp),
					Color.RED);
		}
	}

	// 分散MAK
	public void disperseMak(View v) {
		if (!isAvailable()) {
			return;
		}

		LogUtil.print(getResources().getString(R.string.disp_mak));
		keyDialog = new DownloadKeyDialog(PinPadActivity.this, false,
				new DownloadKeyListener() {

					@Override
					public void downloadKey(byte[] key, byte mIndex, byte wIndex) {
						try {
							showMessage(
					getResources().getString(R.string.disp_mak_work_key_begin),
					Color.BLACK);
							boolean isDisperseWkey = curpinPad.disperseWkey(
									PinPadBuilder.WORKKEYTYPE.MAK, key, mIndex,
									wIndex);

							if (isDisperseWkey) {
								showMessage(getResources().getString(
						R.string.disp_mak_work_key_succ));
							} else {
								showMessage(getResources().getString(
						R.string.disp_mak_work_key_fail));
							}
						} catch (Exception e) {
							e.printStackTrace();
							showMessage("",
					getResources().getString(R.string.disp_mak_work_key_excp)
							+ e.getLocalizedMessage(), Color.RED);
						}
					}
				});
		keyDialog.show();
	}

	// 分散TDK
	public void disperseTdk(View v) {
		if (!isAvailable()) {
			return;
		}

		LogUtil.print(getResources().getString(R.string.disp_tdk));
		keyDialog = new DownloadKeyDialog(PinPadActivity.this, false,
				new DownloadKeyListener() {

					@Override
					public void downloadKey(byte[] key, byte mIndex, byte wIndex) {
						try {
							showMessage(
					getResources().getString(R.string.disp_tdk_work_key_begin),
					Color.BLACK);
							boolean isDisperseWkey = curpinPad.disperseWkey(
									PinPadBuilder.WORKKEYTYPE.TDK, key, mIndex,
									wIndex);

							if (isDisperseWkey) {
				showMessage(getResources().getString(
						R.string.disp_tdk_work_key_succ));
			} else {
				showMessage(getResources().getString(
						R.string.disp_tdk_work_key_fail));
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage("",
					getResources().getString(R.string.disp_tdk_work_key_excp)
							+ e.getLocalizedMessage(), Color.RED);
		}
					}
				});
		keyDialog.show();
	}

	// 分散PIK
	public void dispersePik(View v) {
		if (!isAvailable()) {
			return;
		}

		LogUtil.print(getResources().getString(R.string.disp_pik));
		keyDialog = new DownloadKeyDialog(PinPadActivity.this, false,
				new DownloadKeyListener() {

					@Override
					public void downloadKey(byte[] key, byte mIndex, byte wIndex) {
						try {
							showMessage("开始进行PIK工作密钥分散", Color.BLACK);
							boolean isDisperseWkey = curpinPad.disperseWkey(
									PinPadBuilder.WORKKEYTYPE.PIK, key, mIndex,
									wIndex);

							if (isDisperseWkey) {
				showMessage(getResources().getString(
						R.string.disp_pik_work_key_succ));
			} else {
				showMessage(getResources().getString(
						R.string.disp_pik_work_key_fail));
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage("",
					getResources().getString(R.string.disp_pik_work_key_excp)
							+ e.getLocalizedMessage(), Color.RED);
		}
					}
				});
		keyDialog.show();
	}

	// 分散SMTDK
	public void disperseSMTdk(View v) {
		if (!isAvailable()) {
			return;
		}

		LogUtil.print(getResources().getString(R.string.disp_tdk));
		keyDialog = new DownloadKeyDialog(PinPadActivity.this, false,
				new DownloadKeyListener() {

					@Override
					public void downloadKey(byte[] key, byte mIndex, byte wIndex) {
						try {
							showMessage(
					getResources()
							.getString(R.string.disp_smtdk_work_key_begin),
					Color.BLACK);
							boolean isDisperseWkey = curpinPad.disperseWkey(
									PinPadBuilder.WORKKEYTYPE.SMTDK, key,
									mIndex, wIndex);

							if (isDisperseWkey) {
				showMessage(getResources().getString(
						R.string.disp_smtdk_work_key_succ));
			} else {
				showMessage(getResources().getString(
						R.string.disp_smtdk_work_key_fail));
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage("",
					getResources().getString(R.string.disp_smtdk_work_key_excp)
							+ e.getLocalizedMessage(), Color.RED);
		}
					}
				});
		keyDialog.show();
	}

	// 分散SMPIK
	public void disperseSMPik(View v) {
		if (!isAvailable()) {
			return;
		}

		LogUtil.print(getResources().getString(R.string.disp_smpik_work_key));
		keyDialog = new DownloadKeyDialog(PinPadActivity.this, false,
				new DownloadKeyListener() {

					@Override
					public void downloadKey(byte[] key, byte mIndex, byte wIndex) {
						try {
							showMessage(
					getResources()
							.getString(R.string.disp_smpik_work_key_begin),
					Color.BLACK);
							boolean isDisperseWkey = curpinPad.disperseWkey(
									PinPadBuilder.WORKKEYTYPE.SMPIK, key,
									mIndex, wIndex);

							if (isDisperseWkey) {
				showMessage(getResources().getString(
						R.string.disp_smpik_work_key_succ));
			} else {
				showMessage(getResources().getString(
						R.string.disp_smpik_work_key_fail));
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage("",
					getResources().getString(R.string.disp_smpik_work_key_excp)
							+ e.getLocalizedMessage(), Color.RED);
		}
					}
				});
		keyDialog.show();
	}

	// 分散SMMAK
	public void disperseSMMak(View v) {
		if (!isAvailable()) {
			return;
		}

		LogUtil.print(getResources().getString(R.string.disp_smmak));
		keyDialog = new DownloadKeyDialog(PinPadActivity.this, false,
				new DownloadKeyListener() {

					@Override
					public void downloadKey(byte[] key, byte mIndex, byte wIndex) {
						try {
							showMessage(
					getResources()
							.getString(R.string.disp_smmak_work_key_begin),
					Color.BLACK);
							boolean isDisperseWkey = curpinPad.disperseWkey(
									PinPadBuilder.WORKKEYTYPE.SMMAK, key,
									mIndex, wIndex);

							if (isDisperseWkey) {
				showMessage(getResources().getString(
						R.string.disp_smmak_work_key_succ));
			} else {
				showMessage(getResources().getString(
						R.string.disp_smmak_work_key_fail));
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage("",
					getResources().getString(R.string.disp_smmak_work_key_excp)
							+ e.getLocalizedMessage(), Color.RED);
		}
					}
				});
		keyDialog.show();
	}

	public void getMac(View v) {
		if (!isAvailable()) {
			return;
		}

		LogUtil.print(getResources().getString(R.string.mac_caculate));
		macDialog = new GetMacDialog(PinPadActivity.this, new GetMacListener() {

			@Override
			public void getMac(String data, byte method, byte mode, byte wIndex) {
				try {
					showMessage("计算MAC,结果如下", Color.BLACK);
					MacInfo macInfo = new MacInfo(wIndex, method, mode, data,
							"0001020304050607");
					byte[] mac = curpinPad.getMac(macInfo);
					if (mac != null) {
						showMessage("mac : " + HexUtil.bcd2str(mac),
								Color.GREEN);
					} else {
						showMessage(getResources()
						.getString(R.string.mac_caculate_fail));
					}

				} catch (Exception e) {
					e.printStackTrace();
					showMessage(
					"",
					getResources().getString(R.string.mac_caculate_fail)
							+ e.getLocalizedMessage(), Color.RED);
				}
			}
		});
		macDialog.show();
	}

	// 磁道数据加密
	public void encryptData(View v) {
		if (!isAvailable()) {
			return;
		}

		LogUtil.print(getResources().getString(
				R.string.data_encryp_with_wkey_ecb_mackey));
		try {
            //3.0.9_20160216
			// PinPadBuilder.DATAENCRYPT_MODE.DEFAULT| PinPadBuilder.DATAENCRYPT_MODE.BIT1_ECB|PinPadBuilder.DATAENCRYPT_MODE.BIT6_MACKEY
			byte mode = PinPadBuilder.DATAENCRYPT_MODE.DEFAULT;
			byte[] encryptData = curpinPad
					.enCryptData(
							mode,
							(byte) 0,
							null,
							HexUtil.hexStringToByte("F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533"));

			if (encryptData != null) {
				showMessage(getResources().getString(R.string.data_encryp_succ)
						+ HexUtil.bytesToHexString(encryptData), Color.GREEN);
				Log.i("", HexUtil.bytesToHexString(encryptData));
			} else {
				showMessage(getResources().getString(R.string.data_encryp_fail));
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.print(getResources().getString(
					R.string.track_data_encryp_excp));
			showMessage(
					getResources().getString(R.string.track_data_encryp_excp),
					Color.RED);
		}
	}
	
	public void smEncryptData(View v) {
		if (!isAvailable()) {
			return;
		}

		LogUtil.print(getResources().getString(R.string.track_data_encryp));
		try {
			byte[] encryptData = curpinPad
					.enCryptData(
							PinPadBuilder.DATAENCRYPT_MODE.SM4_DEFAULT,
							(byte) 0,
							null,
							HexUtil.hexStringToByte("F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533"));
			
			if (encryptData != null) {
				showMessage(
						getResources().getString(
								R.string.track_data_encryp_succ)
								+ HexUtil.bytesToHexString(encryptData),
						Color.GREEN);
			} else {
				showMessage(getResources().getString(
						R.string.track_data_encryp_fail));
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.print(getResources().getString(
					R.string.track_data_encryp_excp));
			showMessage(
					getResources().getString(R.string.track_data_encryp_excp),
					Color.RED);
		}
	}

	/**
	 * 国密获取PIN
	 * 
	 * @param v
	 * @createtor：Administrator
	 * @date:2016-03-18 下午01:46:40
	 */
	public void getPin(View v) {
		if (!isAvailable()) {
			return;
		}
		LogUtil.print(getResources().getString(R.string.sm4_pin_input));
		try {
			PinInfo pinInfo = null;
			/* 工作密钥加密，随机数不参与计算 */
			showMessage(getResources().getString(R.string.sm4_pin_input_psw),
					Color.BLACK);
			pinInfo = new PinInfo(
					(byte) 0,
					PinPadBuilder.DATAENCRYPT_MODE.DEFAULT,
					"0000111111111111", // 注意此处传入的为pan值，取pan银联标准：从卡号的倒数第二位开始往前输12位，前补4个0
					"23.45", 6, 6, "0001020304050607",
					PinPadBuilder.PIN_ENCRYPT_MODE.MODE_ZERO,
					PinPadBuilder.PIN_CARDCAL.NEED_CARD,
					PinPadBuilder.PIN_INPUT_TIMES.TIMES_ONCE);
			//设置国密标识，1代表国密
			pinInfo.setwMode((byte) 1);
 			curpinPad.getPinWithTime(pinInfo, pinCallback, 60);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取PIN
	 * 
	 * @param v
	 * @createtor：Administrator
	 * @date:2014-12-15 下午2:03:40
	 */
	public void getPin1(View v) {
		if (!isAvailable()) {
			return;
		}
		LogUtil.print(getResources().getString(R.string.pin_get));
		try {
			/* 过程密钥加密，随机数参与计算 */
			PinInfo pinInfo = null;
			showMessage(getResources().getString(R.string.pin_get_with_psw),
					Color.BLACK);
            //Lql 3.0.9_20160216
			pinInfo = new PinInfo((byte) 0,
					PinPadBuilder.DATAENCRYPT_MODE.DEFAULT, "1111134455678998",
					"23.45", 6, 6, "0001020304050607",
					PinPadBuilder.PIN_ENCRYPT_MODE.MODE_ZERO,
					PinPadBuilder.PIN_CARDCAL.NEED_CARD,
					PinPadBuilder.PIN_INPUT_TIMES.TIMES_ONCE);
			
			/*用于设置卡号的处理方法，1表示银联的处理方式，0表示拉卡拉的处理方式*/
//			pinInfo.setCardNoDealType(1);  
			//如果希望内置密码键盘按键音不响，按照这样设置
			curpinPad.setInnerPinpadBeep(false);
			curpinPad.getPinWithTime(pinInfo, pinCallback, 60);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.pin_input_excp),
					Color.RED);
		}
	}

	// 外接密码键盘显示
	public void display(View v) {
		if (!isAvailable()) {
			return;
		}

		LogUtil.print(getResources().getString(R.string.pinpad_data_show));
		try {
			boolean isDisplay = curpinPad.display("BALANCE:", "12.23");
			if (isDisplay)
				showMessage(getResources().getString(
						R.string.pinpad_data_show_succ));
			else
				showMessage(getResources().getString(
						R.string.pinpad_data_show_fail));

		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources()
					.getString(R.string.pinpad_data_show_excp), Color.RED);
		}
	}

	// 取消PIn输入
	public void cancelGetPin(View v) {
		if (!isAvailable()) {
			return;
		}

		LogUtil.print(getResources().getString(R.string.pin_cancel));
		try {
			curpinPad.stopGetPin();
			showMessage(getResources().getString(R.string.pin_cancel_succ),
					Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.pin_cancel_fail),
					Color.RED);
		}
	}

	// 确认PIN输入
	public void confirmGetPin(View v) {
		if (!isAvailable()) {
			return;
		}

		LogUtil.print(getResources().getString(R.string.confirmgetpin_is_use));
		try {
			curpinPad.confirmGetPin();
			showMessage(getResources().getString(R.string.pin_confirm_succ),
					Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.pin_confirm_fail),
					Color.RED);
		}
	}

	public void open(View view) {
		if (!isAvailable()) {
			return;
		}

		try {
			boolean ret = curpinPad.open(Integer.parseInt(spBautate
					.getSelectedItem().toString()));
			if (ret) {
				showMessage(
						getResources().getString(R.string.pinpad_open_succ),
						Color.BLUE);
			} else {
				showMessage(
						getResources().getString(R.string.pinpad_open_fail),
						Color.BLUE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.pinpad_open_excp),
					Color.RED);
		}
	}

	public void close(View view) {
		if (!isAvailable()) {
			return;
		}

		try {
			boolean ret = curpinPad.close();
			if (ret) {
				showMessage(getResources()
						.getString(R.string.pinpad_close_succ), Color.BLUE);
			} else {
				showMessage(getResources()
						.getString(R.string.pinpad_close_fail), Color.BLUE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.pinpad_close_excp),
					Color.RED);
		}

	}

	public void rfcardStatus(View view) {
		if (!isAvailable()) {
			return;
		}

		try {
			byte ret = curpinPad.rfCardStatus();
			if (ret == 0) {
				showMessage(
						getResources().getString(
								R.string.external_rfcard_not_in_place),
						Color.BLUE);
			} else {
				showMessage(
						getResources().getString(
								R.string.external_rfcard_in_place), Color.BLUE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(
					getResources().getString(
							R.string.external_rfcard_state_excp), Color.RED);
		}
	}

	public void rfcardReset(View view) {
		if (!isAvailable()) {
			return;
		}

		try {
			byte[] ret = curpinPad.rfCardReset();
			if (ret == null) {
				showMessage(getResources().getString(R.string.reset_fail),
						Color.BLUE);
			} else {
				showMessage(getResources().getString(R.string.data_reset)
						+ HexUtil.bcd2str(ret), Color.BLUE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(
					getResources().getString(
							R.string.external_rfcard_reset_excp), Color.RED);
		}
	}

	public void rfcardsendCmd(View view) {
		if (!isAvailable()) {
			return;
		}

		try {
			byte[] apdu = { 0x00, (byte) 0xa4, 0x04, 0x00, 0x0E, 0x31, 0x50,
					0x41, 0x59, 0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46,
					0x30, 0x31 };
			byte[] ret = curpinPad.rfCardApdu(apdu);

			if (ret != null) {
				showMessage(getResources().getString(R.string.response)
						+ HexUtil.bcd2str(ret), Color.BLACK);
			} else {
				showMessage(getResources().getString(R.string.send_cmd_fail));
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(
					getResources().getString(
							R.string.external_rfcard_cmd_send_excp), Color.RED);
		}

	}

	public void rfcardHalt(View view) {
		if (!isAvailable()) {
			return;
		}
		try {
			boolean ret = curpinPad.rfCardHalt();
			if (ret) {
				showMessage(
						getResources().getString(
								R.string.external_rfcard_interrupt_succ),
						Color.BLACK);
			} else {
				showMessage(
						getResources().getString(
								R.string.external_rfcard_interrupt_fail), Color.BLACK);
			}

		} catch (Exception e) {
			e.printStackTrace();
			showMessage(
					getResources().getString(R.string.external_rfcard_send_cmd_excp),
					Color.RED);
		}

	}
	
	public void downloadKekEncryptedMKey(View view) {
		if (!isAvailable()) {
			return;
		}
		try {
			boolean ret = curpinPad
					.loadTekEncryptedMainkey(
							(byte) 0,
							(byte) 0,
							(byte) 0,
							HexUtil.hexStringToByte("F40379AB9E0EC533F40379AB9E0EC533"),
							null);
			if (ret) {
				showMessage(
						getResources().getString(
								R.string.down_mkey_level3_success), Color.BLACK);
			} else {
				showMessage(
						getResources().getString(
								R.string.down_mkey_level3_failed), Color.BLACK);
			}

		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.down_mkey_level3_exception), Color.RED);
		}

	}

	public void downloadKekEncryptedWKey(View view) {
		if (!isAvailable()) {
			return;
		}
		try {
			boolean ret = curpinPad
					.loadTekEncryptedWorkKey(
							(byte) 0,
							(byte) 0,
							(byte) 0,
							HexUtil.hexStringToByte("F40379AB9E0EC533F40379AB9E0EC533"),
							null);
			if (ret) {
				showMessage(
						getResources().getString(
								R.string.down_wkey_level3_success), Color.BLACK);
			} else {
				showMessage(
						getResources().getString(
								R.string.down_wkey_level3_failed), Color.BLACK);
			}

		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.down_wkey_level3_exception), Color.RED);
		}

	}

	public void startSign(View view) {
		if (!isAvailable()) {
			return;
		}
		try {
			curpinPad.startSign("1122334455667788", new OnSignListener.Stub() {

				@Override
				public void onSignSucceed(String arg0, Bitmap arg1)
						throws RemoteException {
					showMessage(getResources().getString(R.string.sign_success), Color.BLACK);
				}

				@Override
				public void onSignFailed(int arg0) throws RemoteException {
					showMessage(getResources().getString(R.string.sign_failed), Color.BLACK);
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.sign_exception), Color.RED);
		}

	}

	public void stopSign(View view) {
		if (!isAvailable()) {
			return;
		}
		try {
			boolean ret = curpinPad.stopSign();
			if (ret) {
				showMessage(getResources().getString(R.string.stop_sign_success), Color.BLACK);
			} else {
				showMessage(getResources().getString(R.string.stop_sign_failed), Color.BLACK);
			}

		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.stop_sign_exception), Color.RED);
		}

	}

	@Override
	public void onDeviceConnected(AidlDeviceManager deviceManager) {
		try {
			this.deviceManager = deviceManager;
			pinPad = AidlPinPad.Stub.asInterface(deviceManager
					.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_PINPAD));
			pinPadmetal = AidlPinPad.Stub.asInterface(deviceManager
					.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_METAL));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isAvailable() {
		if (curpinPad == null) {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.pinpad_type_choose),
					Toast.LENGTH_SHORT).show();
			return false;
		}

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_0:
			showMessage(getResources().getString(R.string.button) + "0");
			break;
		case KeyEvent.KEYCODE_1:
			showMessage(getResources().getString(R.string.button) + "1");
			break;
		case KeyEvent.KEYCODE_2:
			showMessage(getResources().getString(R.string.button) + "2");
			break;
		case KeyEvent.KEYCODE_3:
			showMessage(getResources().getString(R.string.button) + "3");
			break;
		case KeyEvent.KEYCODE_4:
			showMessage(getResources().getString(R.string.button) + "4");
			break;
		case KeyEvent.KEYCODE_5:
			showMessage(getResources().getString(R.string.button) + "5");
			break;
		case KeyEvent.KEYCODE_6:
			showMessage(getResources().getString(R.string.button) + "6");
			break;
		case KeyEvent.KEYCODE_7:
			showMessage(getResources().getString(R.string.button) + "7");
			break;
		case KeyEvent.KEYCODE_8:
			showMessage(getResources().getString(R.string.button) + "8");
			break;
		case KeyEvent.KEYCODE_9:
			showMessage(getResources().getString(R.string.button) + "9");
			break;
		default:
			showMessage(getResources().getString(R.string.other));
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	
	public void getHardSn(View view){
		if (!isAvailable()) {
			return;
		}

		LogUtil.print(getResources().getString(R.string.hardsn_get));
		try {
			byte[] hardSn = curpinPad.getHardWareSN();
			if(hardSn != null){
				showMessage(getResources().getString(R.string.hardsn_get_succ)
						+ HexUtil.bytesToHexString(hardSn), Color.BLACK);
			}else{
				showMessage(getResources().getString(R.string.hardsn_get_failed), Color.RED);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getResources().getString(R.string.hardsn_get_excp),
					Color.RED);
		}
	
	}
	
	public void getMacForSNK(View view) {
		if (!isAvailable()) {
			return;
		}
		byte[] mac;
		try {
			byte[] hardSn = curpinPad.getHardWareSN();
			if (hardSn != null) {
				mac = curpinPad.getMacForSNK(HexUtil.bytesToHexString(hardSn),
						null);
			} else {
				mac = curpinPad
						.getMacForSNK(
								HexUtil.bytesToHexString("11111111111111112222222222222222"
										.getBytes()), null);
			}

			if (mac != null) {
				showMessage("mac : " + HexUtil.bcd2str(mac), Color.GREEN);
			} else {
				showMessage(getResources()
						.getString(R.string.mac_caculate_fail));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void getCheckValue(View v) {
		byte[] cv = null;
		try {
			cv = curpinPad.getCheckValue((byte) 0, (byte) 0, (byte) 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (cv != null) {
			showMessage("checkvalue : " + HexUtil.bcd2str(cv));
		} else {
			showMessage("获取checkvalue失败");
		}
	}
}
