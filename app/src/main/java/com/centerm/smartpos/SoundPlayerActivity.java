package com.centerm.smartpos;

import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.HashMap;

import com.centerm.smartpos.aidl.soundplayer.AidlSoundPlayer;
import com.centerm.smartpos.aidl.soundplayer.AidlSoundStateChangeListener;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.constant.MapBean;

public class SoundPlayerActivity extends BaseActivity {

	private AidlSoundPlayer soundPlayer;
	private AidlSoundStateChangeListener aidlSoundStateListener = new SoundCallback();
    private EditText numberEdit ;
	private CheckBox yuanCheckBox; 
	private SeekBar seekBar ;
	private int readSpeed= 500;
	private boolean readUnit = false;
	private TextView speedView;
	
	private class SoundCallback extends AidlSoundStateChangeListener.Stub{

		@Override
		public void onSoundError(int arg0) throws RemoteException {
			showMessage("Sound Error");
		}

		@Override
		public void onSoundFinish() throws RemoteException {
			showMessage("Sound Finish");
		}

		@Override
		public void onSoundPause() throws RemoteException {
			showMessage("Sound pause");
		}

		@Override
		public void onSounding() throws RemoteException {
			showMessage("Sounding... ...");
		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.setContentView(R.layout.soundplayer_activity);
		super.onCreate(savedInstanceState);
		numberEdit = (EditText)findViewById(R.id.sound_input);
		numberEdit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		speedView = (TextView)findViewById(R.id.sound_speed);
		
		seekBar = (SeekBar)findViewById(R.id.seekbar);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					readSpeed = (130-progress)*10;
					speedView.setText(readSpeed+"");
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
		});
		seekBar.setProgress(70);//一开始默认间隔为500=(130-70)*10
		yuanCheckBox = (CheckBox)findViewById(R.id.yuan);
	    yuanCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					readUnit = true;
				}else{
					readUnit = false;
				}
			}
	    	
	    });
	}
	
	
	
	//20170518    智能数额朗读   wangwenxun@centerm.com 
	//传入的数据如果大于0，那么不能以0开头
	public void intelligenceAmountReader(View v){
		final String numberStr = numberEdit.getText().toString().trim();
		 
		Thread intellThread = new Thread(new Runnable(){
			@Override
			public void run() {
				if((numberStr!=null)&&(numberStr.length()!=0)){
					try {
						setReadParam();
						//如果不想进行监听，传null也是可以的
		            	//soundPlayer.intelligenceSound(numberStr, null);
					    soundPlayer.intelligenceSound(numberStr,aidlSoundStateListener);
		            	 
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}else{
					showMessage("Invalid Param");
				}
			}
		});
		intellThread.start();
	}
	
	// 20170518 普通数额朗读 wangwenxun@centerm.com
	public void commonAmountReader(View v) {
		final String numberStr = numberEdit.getText().toString().trim();
		Thread t  = new Thread(new Runnable(){
			@Override
			public void run() {
				if ((numberStr != null) && (numberStr.length() != 0)) {
		            try {
		            	setReadParam(); 
						//设置普通播报时间
		            	soundPlayer.commonSound(numberStr, aidlSoundStateListener);
		            	 
		            } catch (RemoteException e) {
						e.printStackTrace();
					}
				} else {
					showMessage("Invalid Param");
				}
			}
		});
		t.start();
	}
	

	public void swipeCard(View v) {
		play(Constant.VOICE_TYPE.SWIPE_CARD);
	}

	public void insertCard(View v) {
		play(Constant.VOICE_TYPE.INSERT_CARD);
	}

	public void waveCard(View v) {
		play(Constant.VOICE_TYPE.WAVE_CARD);
	}

	public void swipeWaveCard(View v) {
		play(Constant.VOICE_TYPE.SWIPE_WAVE_CARD);
	}

	public void swipeInsertCard(View v) {
		play(Constant.VOICE_TYPE.SWIPE_INSERT_CARD);
	}

	public void swipeInsertWaveCard(View v) {
		play(Constant.VOICE_TYPE.SWIPE_INSERT_WAVE_CARD);
	}

	public void transactionNoLeaveCard(View v) {
		play(Constant.VOICE_TYPE.TRANSACTION_DONT_LEAVE_CARD);
	}

	public void transactionOkLeaveCard(View v) {
		play(Constant.VOICE_TYPE.TRANSACTION_OK_LEAVE_CARD);
	}

	public void inputAmount(View v) {
		play(Constant.VOICE_TYPE.INPUT_AMOUNT);
	}

	public void inputPwd(View v) {
		play(Constant.VOICE_TYPE.INPUT_PASSWORD);
	}

	public void pwdLengthNotEnough(View v) {
		play(Constant.VOICE_TYPE.PASSWORD_LENGTH_NO_ENOUGH);
	}

	public void confirmCardNoAmount(View v) {
		play(Constant.VOICE_TYPE.CONFIRM_CARDNO_AMOUNT);
	}

	public void communicateWait(View v) {
		play(Constant.VOICE_TYPE.TRANSACTION_WAIT_PLEASE);
	}

	public void printWait(View v) {
		play(Constant.VOICE_TYPE.PRINTTING_WAIT_PLEASE);
	}

	public void pwdWrongReTransaction(View v) {
		play(Constant.VOICE_TYPE.PASSWORD_WRONG_RETRANSACTION);
	}

	public void printerLackPaper(View v) {
		play(Constant.VOICE_TYPE.PRINTER_LACK_PAPER);
	}

	public void lowPowerNoPrint(View v) {
		play(Constant.VOICE_TYPE.LOW_POWER_NO_PRINT);
	}

	public void transactionSuccess(View v) {
		play(Constant.VOICE_TYPE.TRANSACTION_SUCCESS);
	}

	public void communicateError(View v) {
		play(Constant.VOICE_TYPE.COMMUNICATION_BREAKDOWN);
	}

	public void sign(View v) {
		play(Constant.VOICE_TYPE.SIGN_PLEASE);
	}

	public void balance(View v) {
		play(Constant.VOICE_TYPE.BALANCE_PLEASE);
	}

	public void automaticSuccess(View v) {
		play(Constant.VOICE_TYPE.AUTOMATIC_SUCCESS);
	}

	public void activationSuccess(View v) {
		play(Constant.VOICE_TYPE.ACTIVATION_SUCCESS);
	}
	
	public void waveInsertCard(View v) {
		play(Constant.VOICE_TYPE.WAVE_INSERT_CARD);
	}
	
	public static void sleep(long time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	

	private void play(int type) {
		if (soundPlayer != null) {
			try {
				soundPlayer.play(type);
			} catch (Exception e) {
				e.printStackTrace();
				showMessage(e.getLocalizedMessage(), Color.RED);
			}
		}
	}
	
	private void setReadParam(){
		MapBean mapbean = new MapBean();
		HashMap<String,Object> hashmap = new HashMap<String,Object>();
		hashmap.put("readfrequence", readSpeed);
		hashmap.put("readUnit",readUnit);
		mapbean.setParamMap(hashmap);
		try {
			soundPlayer.readParam(mapbean);
		} catch (RemoteException e) {
			showMessage("set param fail");
			e.printStackTrace();
		}
	}

	@Override
	public void onDeviceConnected(AidlDeviceManager deviceManager) {
		try {
			soundPlayer = AidlSoundPlayer.Stub.asInterface(deviceManager
					.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_SOUNDPLAYER));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
