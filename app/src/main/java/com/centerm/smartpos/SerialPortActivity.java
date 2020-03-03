package com.centerm.smartpos;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.centerm.smartpos.aidl.serialport.AidlSerialPort;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.util.HexUtil;

/**
 * 串口测试类
 * 
 * @author Tianxiaobo
 * 
 */
public class SerialPortActivity extends BaseActivity {
	private AidlSerialPort serialport1 = null;
	private AidlSerialPort serialprot2 = null;

	private AidlSerialPort serialport = null;

	private CheckBox hexDisplayCheckBox;
	private CheckBox hexWriteCheckBox;
	private CheckBox rateCheckBox;
	private EditText writeDataEditText;
	private TextWatcher writeDataTextWatcher;

	private boolean blHexDisplay = false;
	private boolean blHexWrite = false;
	private boolean isReading = false;
	private boolean is115200 = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.serialport_activity);
		super.onCreate(savedInstanceState);
		initViews();
	}

	private void initViews() {
		hexDisplayCheckBox = (CheckBox) this
				.findViewById(R.id.hexDisplayCheckBox);
		hexWriteCheckBox = (CheckBox) this.findViewById(R.id.hexWriteCheckBox);
		rateCheckBox = (CheckBox) this.findViewById(R.id.rateCheckBox);
		hexDisplayCheckBox.setOnCheckedChangeListener(new CheckBoxListener());
		hexWriteCheckBox.setOnCheckedChangeListener(new CheckBoxListener());
		rateCheckBox.setOnCheckedChangeListener(new CheckBoxListener());

		writeDataEditText = (EditText) this
				.findViewById(R.id.writeDataEditText);
		writeDataTextWatcher = new TextWatcher() {
			String strBefore = "";
			String strCur = "";
			String strNew = "";
			String digits = "0123456789abcdefABCDEF";
			private int lPreIndex;
			private boolean blInvalid = false;
			int index;

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				if (!blInvalid) {
					strBefore = s.toString();
					lPreIndex = writeDataEditText.getSelectionStart();
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (blHexWrite) {
					strCur = s.toString();
					if (blInvalid) {
						blInvalid = false;
						return;
					}
					StringBuffer strBuf = new StringBuffer();
					for (int i = 0; i < strCur.length(); i++) {
						if (digits.indexOf(strCur.charAt(i)) >= 0) {
							strBuf.append(strCur.charAt(i));
						} else {
							blInvalid = true;
							writeDataEditText.setText(strBefore);
							writeDataEditText.setSelection(lPreIndex);
							break;
						}
					}
				}
			}
		};
		writeDataEditText.addTextChangedListener(writeDataTextWatcher);
	}

	// 打开
	public void open(View v) {
		if (!isAvailable()) {
			return;
		}
		int ret = -1;
		try {
			if (is115200)
				ret = this.serialport.openSerial(115200);
			else
				ret = this.serialport.openSerial(9600);
			if(ret >= 0)
				showMessage(getString(R.string.serial_open_success));
			else
				showMessage(getString(R.string.serial_open_failed));
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.serial_open_exception));
		}
	}

	// 关闭
	public void close(View v) {
		if (!isAvailable()) {
			return;
		}
		isReading = false;
		int ret = -1;
		try {
			ret = this.serialport.closeSerial();
			if(ret >= 0)
				showMessage(getString(R.string.serial_close_success));
			else
				showMessage(getString(R.string.serial_close_failed));
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.serial_close_exception));
		}
	}

	// 读取数据
	public void readData(View v) {
		if (!isAvailable()) {
			return;
		}
		if (isReading)
			return;

		isReading = true;

		new Thread() {
			@Override
			public void run() {
				try {
					while (isReading) {
						byte[] data = serialport.receiveData(500);
						Log.e("serial", "data:" + HexUtil.bcd2str(data));

						if (data == null || data.length < 1) {
						} else {
							if (blHexDisplay) {
								showMessage(getString(R.string.serial_read)
										+ HexUtil.bytesToHexString(data));
							} else {
								showMessage(getString(R.string.serial_read) + new String(data));
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void stopReceive(View v) {
		if (!isAvailable()) {
			return;
		}
		isReading = false;
	}

	// 写数据
	public void writeData(View v) {
		if (!isAvailable()) {
			return;
		}
		String writeData = writeDataEditText.getText().toString();
		byte hexData[] = writeData.getBytes();
		if (blHexWrite) {
			if ((writeData.length() % 2) != 0) {
				showMessage("Input data is wrong!");
				return;
			}
			hexData = HexUtil.hexStringToByte(writeData);
			if (hexData == null) {
				return;
			}
		}
		try {
			this.serialport.sendData(hexData);
			showMessage(getString(R.string.serial_send_success));
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.serial_send_exception));
		}
	}

	@Override
	public void onDeviceConnected(AidlDeviceManager deviceManager) {
		try {
			serialport1 = AidlSerialPort.Stub.asInterface(deviceManager
					.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_SERIALPORT1));
			serialprot2 = AidlSerialPort.Stub.asInterface(deviceManager
					.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_SERIALPORT2));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void serialport1Selcet(View view) {
		serialport = serialport1;
	}

	public void serialport2Selcet(View view) {
		serialport = serialprot2;
	}

	private boolean isAvailable() {
		if (serialport == null) {
			Toast.makeText(getApplicationContext(), R.string.serial_chose_type,
					Toast.LENGTH_SHORT).show();
			return false;
		}

		return true;
	}

	class CheckBoxListener implements OnCheckedChangeListener {
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			switch (buttonView.getId()) {
			case R.id.hexDisplayCheckBox:
				blHexDisplay = isChecked;
				break;
			case R.id.hexWriteCheckBox:
				blHexWrite = isChecked;
				if (isChecked) {
					writeDataEditText.setText("");
				}
				break;
			case R.id.rateCheckBox:
				is115200 = isChecked;

				break;
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		isReading = false;
	}
}
