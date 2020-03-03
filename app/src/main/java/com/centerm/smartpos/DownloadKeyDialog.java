package com.centerm.smartpos;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.centerm.smartpos.util.HexUtil;

public class DownloadKeyDialog {

	private Context mContext;
	private EditText keyEt, mIndexEt, wIndexEt;
	private Button btn_neg;
	private Button btn_pos;
	private AlertDialog mAlertDialog;
	private DownloadKeyListener keyListener;

	public interface DownloadKeyListener {
		void downloadKey(byte[] key, byte mIndex, byte wIndex);
	}

	public DownloadKeyDialog(Context context, boolean isMkey,
			DownloadKeyListener listener) {
		mContext = context;
		keyListener = listener;
		AlertDialog.Builder builder = new Builder(context);
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.dialog_download_key, null);

		keyEt = (EditText) view.findViewById(R.id.input_key);
		mIndexEt = (EditText) view.findViewById(R.id.input_mindex);
		wIndexEt = (EditText) view.findViewById(R.id.input_windex);
		btn_neg = (Button) view.findViewById(R.id.btn_neg);
		btn_pos = (Button) view.findViewById(R.id.btn_pos);
		if (isMkey) {
			wIndexEt.setEnabled(false);
		} else {
			keyEt.setText("F40379AB9E0EC533F40379AB9E0EC533");
		}
		btn_pos.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String key = keyEt.getText().toString();
				String mainInx = mIndexEt.getText().toString();
				String workInx = wIndexEt.getText().toString();
				if (TextUtils.isEmpty(key) || TextUtils.isEmpty(mainInx)
						|| TextUtils.isEmpty(workInx)) {
					Toast.makeText(mContext, R.string.Incomplete_data_entry, Toast.LENGTH_SHORT)
							.show();  //2017/02/27 15:42 英文适配 wengtao@centerm.com 
					return;
				}

				keyListener.downloadKey(HexUtil.hexStringToByte(key),
						(byte) Integer.parseInt(mainInx),
						(byte) Integer.parseInt(workInx));
				mAlertDialog.dismiss();
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

}
