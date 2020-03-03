package com.centerm.smartpos;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

import com.centerm.smartpos.aidl.printer.AidlPrinter;
import com.centerm.smartpos.aidl.printer.AidlPrinterStateChangeListener;
import com.centerm.smartpos.aidl.printer.PrintDataObject;
import com.centerm.smartpos.aidl.printer.PrintDataObject.ALIGN;
import com.centerm.smartpos.aidl.printer.PrintDataObject.SPACING;
import com.centerm.smartpos.aidl.printer.PrinterParams;
import com.centerm.smartpos.aidl.printer.PrinterParams.DATATYPE;
import com.centerm.smartpos.aidl.printer.PrinterParams.TYPEFACE;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.constant.DeviceErrorCode;
import com.centerm.smartpos.util.LogUtil;

/**
 * 打印机测试工具类
 * 
 * @author Tianxiaobo
 * 
 */
public class PrinterActivity extends BaseActivity {

	private AidlPrinter printDev = null;
	// 打印机回调对象
	private AidlPrinterStateChangeListener callback = new PrinterCallback(); // 打印机回调
	private Spinner spinner;
	private int typeIndex;
	private String codeStr;

	/** 打印机回调类 */
	private class PrinterCallback extends AidlPrinterStateChangeListener.Stub {

		@Override
		public void onPrintError(int arg0) throws RemoteException {
			// showMessage("打印机异常" + arg0, Color.RED);
			getMessStr(arg0);
		}

		@Override
		public void onPrintFinish() throws RemoteException {
			showMessage(getString(R.string.printer_finish), "", Color.BLACK);
		}

		@Override
		public void onPrintOutOfPaper() throws RemoteException {
			showMessage(getString(R.string.printer_need_paper), "", Color.RED);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.setContentView(R.layout.printer_activity);
		super.onCreate(savedInstanceState);
		spinner = (Spinner) findViewById(R.id.code_type);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				typeIndex = position;
				switch (position) {
				case 6:
					codeStr = "6901234567892";
					break;
				case 7:
					codeStr = "12345678";
					break;
				case 11:
					codeStr = "123456789012";
					break;
				case 12:
					codeStr = "01234565";
					break;
				default:
					codeStr = "123456";
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	// 设置灰度为一级
	public void setGrayOne(View v) {
		LogUtil.print(getString(R.string.printer_gray1));
		try {
			printDev.setPrinterGray(0x01);
			showMessage(getString(R.string.printer_gray1_success), Color.GREEN);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(
					getString(R.string.printer_gray_exception)
							+ e.getLocalizedMessage(), Color.RED);
		}
	}

	// 设置灰度为二级
	public void setGrayTwo(View v) {
		LogUtil.print(getString(R.string.printer_gray2));
		try {
			printDev.setPrinterGray(0x02);
			showMessage(getString(R.string.printer_gray2_success), Color.GREEN);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(
					getString(R.string.printer_gray_exception)
							+ e.getLocalizedMessage(), Color.RED);
		}
	}

	// 设置灰度为三级
	public void setGrayThree(View v) {
		LogUtil.print(getString(R.string.printer_gray3));
		try {
			printDev.setPrinterGray(0x03);
			showMessage(getString(R.string.printer_gray3_success), Color.GREEN);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(
					getString(R.string.printer_gray_exception)
							+ e.getLocalizedMessage(), Color.RED);
		}
	}

	// 设置灰度为四级
	public void setGrayFour(View v) {
		LogUtil.print(getString(R.string.printer_gray4));
		try {
			printDev.setPrinterGray(0x04);
			showMessage(getString(R.string.printer_gray4_success), Color.GREEN);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(
					getString(R.string.printer_gray_exception)
							+ e.getLocalizedMessage(), Color.RED);
		}
	}

	// 打印文本内容
	public void printText(View v) {
		try {
			final List<PrintDataObject> list = new ArrayList<PrintDataObject>();
			list.add(new PrintDataObject(
					getString(R.string.printer_textsize_bigger), 24));
			list.add(new PrintDataObject(getString(R.string.printer_textsize_blod),
					8, true));
			list.add(new PrintDataObject(getString(R.string.printer_left), 8,
					false, ALIGN.LEFT));
			list.add(new PrintDataObject(getString(R.string.printer_center), 8,
					false, ALIGN.CENTER));
			list.add(new PrintDataObject(getString(R.string.printer_right), 8,
					false, ALIGN.RIGHT));
			list.add(new PrintDataObject(getString(R.string.printer_underline), 8,
					false, ALIGN.LEFT, true));
			list.add(new PrintDataObject(getString(R.string.printer_amount), 8,
					true, ALIGN.LEFT, false, false));
			list.add(new PrintDataObject("888.66", 24, true, ALIGN.LEFT, false,
					true));
			list.add(new PrintDataObject(getString(R.string.printer_newline), 8,
					false, ALIGN.LEFT, false, true));
			list.add(new PrintDataObject(
					getString(R.string.printer_acceptorid_name)));
			list.add(new PrintDataObject(getString(R.string.printer_line_distance),
					8, false, ALIGN.LEFT, false, true, 40, 28));
			for (int i = 0; i < 38; i += 3) {
				list.add(new PrintDataObject(
						getString(R.string.printer_left_distance) + (i * 10), 8,
						false, ALIGN.LEFT, false, true, 24, 0, (i * 10)));
			}
			printDev.printText(list, callback);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printBmpFast(View v) {
		try {
			InputStream ins = getResources().getAssets().open(
					"image/smartposTest.bmp");
			Bitmap bmp = BitmapFactory.decodeStream(ins);
			printDev.printBmpFast(bmp, Constant.ALIGN.CENTER, callback); // 位图快速打印接口更改为支持打印队列的接口
																			// 2017/03/28
																			// wengtao@centerm.com
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void getMessStr(int ret) {
		switch (ret) {
		case DeviceErrorCode.DEVICE_PRINTER.DEVICE_BUSY:
			showMessage(getString(R.string.printer_device_busy));
			break;
		case DeviceErrorCode.DEVICE_PRINTER.DEVICE_OK:
			showMessage("打印机状态正常");
			break;
		case DeviceErrorCode.DEVICE_PRINTER.DEVICE_PRINTER_OUT_OF_PAPER:
			showMessage(getString(R.string.printer_lack_paper));
			break;
		case DeviceErrorCode.DEVICE_PRINTER.DEVICE_PRINTER_HEAD_OVER_HEIGH:
			showMessage(getString(R.string.printer_over_heigh));
			break;
		case DeviceErrorCode.DEVICE_PRINTER.DEVICE_PRINTER_OVER_HEATER:
			showMessage(getString(R.string.printer_over_heat));
			break;
		case DeviceErrorCode.DEVICE_PRINTER.DEVICE_PRINTER_LOW_POWER:
			showMessage(getString(R.string.printer_low_power));
			break;
		default:
			showMessage(getString(R.string.printer_other_exception_code) + ret);
			break;
		}

	}

	// 吐纸
	public void spitPaper(View v) {
		try {
			printDev.spitPaper(100);
			showMessage(getString(R.string.printer_spit_success));
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(
					getString(R.string.printer_spit_exception)
							+ e.getLocalizedMessage(), Color.RED);
		}
	}

	// 获取打印机状态
	public void getPrintState(View v) {
		try {
			int retCode = printDev.getPrinterState();
			getMessStr(retCode);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(
					getString(R.string.printer_status_exception)
							+ e.getLocalizedMessage(), Color.RED);
		}
	}

	// 初始化打印参数
	public void initPrinter(View v) {
		// begin 017/03/09 14:11 子线程，防止有多个打印任务时点击初始化参数出现anr现象
		// wengtao@centerm.com
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					printDev.initPrinter();
					showMessage(getString(R.string.printer_init_success));
				} catch (Exception e) {
					e.printStackTrace();
					showMessage(getString(R.string.printer_init_exception));
				}
			}
		}).start();
		// end 017/03/09 14:11
	}

	// 倍高
	public void printDoubleHigh(View v) {
		try {
			PrintDataObject printDataObject = new PrintDataObject(
					getString(R.string.printer_textsize_normal));
			List<PrintDataObject> list = new ArrayList<PrintDataObject>();
			list.add(printDataObject);
			list.add(printDataObject);
			list.add(printDataObject);
			PrintDataObject printDataObject2 = new PrintDataObject(
					getString(R.string.printer_textsize_normal));
			printDataObject2.setText(getString(R.string.printer_texisize_higher));
			printDataObject2.setSpacing(SPACING.DOUBLE_HIGH);
			list.add(printDataObject2);
			list.add(printDataObject2);
			list.add(printDataObject2);
			int ret = printDev.printTextEffect(list);
			Log.e("test", "返回码：" + ret);
			getMessStr(ret);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 倍宽
	public void printDoubleWidth(View v) {
		try {
			PrintDataObject printDataObject = new PrintDataObject(
					getString(R.string.printer_textsize_normal));
			List<PrintDataObject> list = new ArrayList<PrintDataObject>();
			list.add(printDataObject);
			list.add(printDataObject);
			list.add(printDataObject);
			PrintDataObject printDataObject2 = new PrintDataObject(
					getString(R.string.printer_textsize_normal));
			printDataObject2.setText(getString(R.string.printer_textsize_wider));
			printDataObject2.setSpacing(SPACING.DOUBLE_WIDTH);
			list.add(printDataObject2);
			list.add(printDataObject2);
			list.add(printDataObject2);
			int ret = printDev.printTextEffect(list);
			Log.e("test", "返回码：" + ret);
			getMessStr(ret);		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 倍高宽
	public void printDoubleHighWidth(View v) {
		try {
			PrintDataObject printDataObject = new PrintDataObject(
					getString(R.string.printer_textsize_normal));
			List<PrintDataObject> list = new ArrayList<PrintDataObject>();
			list.add(printDataObject);
			list.add(printDataObject);
			list.add(printDataObject);
			PrintDataObject printDataObject2 = new PrintDataObject(
					getString(R.string.printer_textsize_normal));
			printDataObject2.setText(getString(R.string.printer_higher_wider));
			printDataObject2.setSpacing(SPACING.DOUBLE_HIGH_WIDTH);
			list.add(printDataObject2);
			list.add(printDataObject2);
			list.add(printDataObject2);
			int ret = printDev.printTextEffect(list);
			Log.e("test", "返回码：" + ret);
			getMessStr(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 开启打印队列测试 author :wengtao@centerm.com time :2017-3-24 16:10
	 */
	public void openQueue(View v) {
		try {
			if (printDev.setPrintQueue(true)) {
				showMessage(getString(R.string.open_queue_success));
			} else {
				showMessage(getString(R.string.open_queue_failed));
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.open_queue_exception)
					+ e.getMessage());
		}
	}

	/**
	 * 关闭打印队列测试 author :wengtao@centerm.com time :2017-3-24 16:10
	 */
	public void closeQueue(View v) {
		try {
			if (printDev.setPrintQueue(false)) {
				showMessage(getString(R.string.close_queue_success));
			} else {
				showMessage(getString(R.string.close_queue_failed));
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage(getString(R.string.close_queue_exception)
					+ e.getMessage());
		}
	}

	public void printCodeType(View v) {
		try {
			printDev.printBarCodeExtend(codeStr, typeIndex, 600, 600,
					Constant.ALIGN.CENTER, callback);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDeviceConnected(AidlDeviceManager deviceManager) {
		try {
			printDev = AidlPrinter.Stub.asInterface(deviceManager
					.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_PRINTERDEV));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printDatas(View v) {
		try {
			List<PrinterParams> paramList = new ArrayList<PrinterParams>();
			PrinterParams PrinterParams1 = new PrinterParams();
			PrinterParams1.setDataType(DATATYPE.TEXT);
			PrinterParams1.setText("打印测试数据");
			PrinterParams1.setTextSize(30);
			PrinterParams1.setBold(true);
			PrinterParams1
					.setAlign(com.centerm.smartpos.aidl.printer.PrinterParams.ALIGN.CENTER);
			PrinterParams1.setTypeface(TYPEFACE.DEFAULT);
			PrinterParams1.setLineHeight(30);
			PrinterParams PrinterParams3 = new PrinterParams();
			PrinterParams3.setDataType(DATATYPE.FEED_LINE);
			PrinterParams3.setFeedlineNum(2);
			PrinterParams PrinterParams4 = new PrinterParams();
			PrinterParams4.setDataType(DATATYPE.BARCODE);
			PrinterParams4.setBarcodeHeight(150);
			PrinterParams4.setBarcodeWidth(300);
			PrinterParams4.setText("12345678");
			PrinterParams PrinterParams5 = new PrinterParams();
			PrinterParams5.setDataType(DATATYPE.QRCODE);
			PrinterParams5.setText("1234567890123456");
			PrinterParams5.setQrcodeWidth(300);
			PrinterParams5
					.setAlign(com.centerm.smartpos.aidl.printer.PrinterParams.ALIGN.CENTER);
			PrinterParams PrinterParams6 = new PrinterParams();
			PrinterParams6.setDataType(DATATYPE.IMAGE);
			InputStream ins;
			ins = getResources().getAssets().open("image/smartposTest.bmp");
			Bitmap bmp = BitmapFactory.decodeStream(ins);
			PrinterParams6.setBitmap(bmp);
			PrinterParams6.setImgHeigth(100);
			PrinterParams6.setImgWidth(200);
			PrinterParams PrinterParams7 = new PrinterParams();
			PrinterParams7
					.setAlign(com.centerm.smartpos.aidl.printer.PrinterParams.ALIGN.LEFT);
			PrinterParams7.setText("下划线加粗测试");
			PrinterParams7.setUnderLine(true);
			PrinterParams7.setTextSize(25);
			PrinterParams7.setBold(true);
			paramList.add(PrinterParams1);
			paramList.add(PrinterParams7);
			paramList.add(PrinterParams3);
			paramList.add(PrinterParams4);
			paramList.add(PrinterParams5);
			paramList.add(PrinterParams6);
			int ret = printDev.printData(paramList);
			getMessStr(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
