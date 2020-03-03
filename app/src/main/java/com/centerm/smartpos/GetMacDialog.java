package com.centerm.smartpos;

import com.centerm.smartpos.aidl.pinpad.PinPadBuilder;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class GetMacDialog {

	private Context mContext;
	private EditText wIndexEt, dataEt;
	private RadioGroup mode_rg;
	private RadioGroup encryp_rg;
	private Button btn_neg;
	private Button btn_pos;
	private AlertDialog mAlertDialog;

	private byte mode = PinPadBuilder.MAC_MODE.DEFAULT;
	private byte method = PinPadBuilder.MACMETHOD_TYPE.TYPE_CUP_ECB;

	public interface GetMacListener {
		void getMac(String data, byte method, byte mode, byte wIndex);
	}

	public GetMacDialog(Context context, final GetMacListener listener) {
		mContext = context;
		AlertDialog.Builder builder = new Builder(context);
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.dialog_calc_mac, null);

		mode_rg = (RadioGroup) view.findViewById(R.id.mode_rg);
		encryp_rg = (RadioGroup) view.findViewById(R.id.mac_encrypt_rg);
		wIndexEt = (EditText) view.findViewById(R.id.input_windex);
		dataEt = (EditText) view.findViewById(R.id.input_data);
		btn_neg = (Button) view.findViewById(R.id.btn_neg);
		btn_pos = (Button) view.findViewById(R.id.btn_pos);
		mode_rg.setOnCheckedChangeListener(modeCheckedListener);
		mode_rg.check(R.id.mac_wkey);
		encryp_rg.setOnCheckedChangeListener(encrypCheckedListener);
		encryp_rg.check(R.id.mac_encrypt_2);
		btn_pos.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String workInx = wIndexEt.getText().toString();
				String data = dataEt.getText().toString();
				if (TextUtils.isEmpty(data) || TextUtils.isEmpty(workInx)) {
					Toast.makeText(mContext, R.string.Incomplete_data_entry, Toast.LENGTH_SHORT)
							.show();  //2017/02/27 15:43 英文适配 wengtao@centerm.com 
					return;
				}

				listener.getMac(data, method, mode,
						(byte) Integer.parseInt(workInx));
				dismiss();
			}
		});
		btn_neg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		builder.setView(view);
		mAlertDialog = builder.create();
		mAlertDialog.setCancelable(true);
		mAlertDialog.setCanceledOnTouchOutside(true);
	}

	public void show() {
		mAlertDialog.show();
	}

	public void dismiss() {
		mAlertDialog.dismiss();
	}

	private OnCheckedChangeListener modeCheckedListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.mac_wkey:
				mode = PinPadBuilder.MAC_MODE.DEFAULT;
				break;
			case R.id.mac_pkey:
				mode = PinPadBuilder.MAC_MODE.PROCESS;
				break;
			case R.id.mac_pkey_sr:
				mode = PinPadBuilder.MAC_MODE.PROCESS_SR;
				break;
			case R.id.mac_sm4_wkey:
				mode = PinPadBuilder.MAC_MODE.SM4_DEFAULT;
				break;
			}
		}
	};

	private OnCheckedChangeListener encrypCheckedListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.mac_encrypt_0:
				method = PinPadBuilder.MACMETHOD_TYPE.TYPE_X9_9;
				break;
			case R.id.mac_encrypt_1:
				method = PinPadBuilder.MACMETHOD_TYPE.TYPE_BOC_EXTENED;
				break;
			case R.id.mac_encrypt_2:
				method = PinPadBuilder.MACMETHOD_TYPE.TYPE_CUP_ECB;
				break;
			case R.id.mac_encrypt_3:
				method = PinPadBuilder.MACMETHOD_TYPE.TYPE_X919;
				break;
			case R.id.mac_encrypt_4:
				method = PinPadBuilder.MACMETHOD_TYPE.TYPE_X919_00;
				break;
			case R.id.mac_encrypt_5:
				method = PinPadBuilder.MACMETHOD_TYPE.TYPE_X919_MP;
				break;
			case R.id.mac_encrypt_6:
				method = PinPadBuilder.MACMETHOD_TYPE.TYPE_XOR_3DES;
				break;
			case R.id.mac_encrypt_7:
				method = PinPadBuilder.MACMETHOD_TYPE.TYPE_CUP_SM4;
				int session = mode_rg.getCheckedRadioButtonId();
				switch (session) {
				case R.id.mac_wkey:
				case R.id.mac_pkey:
				case R.id.mac_pkey_sr:
					mode_rg.check(R.id.mac_sm4_wkey);
					break;
				}
				break;
			}
		}
	};

}
