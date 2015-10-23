package com.topeet.serialtest;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.topeet.serialtest.R;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Window;
import android.os.Message;

public class MainActivity extends Activity {

	/****************************************/
	String rxIdCode = "";
	String tag = "serial test";

	private EditText ET1;
	private Button RECV;
	private Button SEND;

	public boolean isStartReceive = false;
	public String TAG = "serial";
	private StartReadThread sensor;

	serial com3 = new serial();

	/****************************************/
	private FileDescriptor serialport = null;
	private InputStream inputStream = null;
	private StartReadWithInputStreamThread inputStreamThread;
	private DisplaySerialThread serialThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/********************************************/
		ET1 = (EditText) findViewById(R.id.edit1);
		RECV = (Button) findViewById(R.id.recv1);
		SEND = (Button) findViewById(R.id.send1);
		
		RECV.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if (serialThread == null) {
					serialThread = new DisplaySerialThread();
					serialThread.setHandler(callback);
					serialThread.start();
					Log.i(TAG, "start to receiveing!!!");
				}
				if (isStartReceive)
					isStartReceive = false;
				else
					isStartReceive = true;
				serialThread.setStop(isStartReceive);

				// String dev = "/dev/ttySAC3";
				// if (inputStreamThread == null) {
				// serialport = Native.SerialJNI_open(dev);
				// inputStream = new FileInputStream(serialport);
				// inputStreamThread = new StartReadWithInputStreamThread();
				// inputStreamThread.start();
				// }
				// if (isStartReceive)
				// isStartReceive = false;
				// else
				// isStartReceive = true;
				// String tmp = isStartReceive ? "\nisStartReceive = true\n"
				// : "\nisStartReceive = false\n";
				// ET1.append(tmp);
				// ET1.setSelection(ET1.length());

			}
		});
		// SEND.setOnClickListener(new manager());
	}

	DataHandler callback = new DataHandler() {
		@Override
		public void onDataReceive(int datatype, int data, Object obj) {
			Message msg = MyHandler.obtainMessage();
			msg.what = datatype;
			msg.arg1 = data;
			msg.obj = obj;
			MyHandler.sendMessage(msg);
		}
	};

	private Handler MyHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				ET1.append(msg.arg1 + "-");
				ET1.setSelection(ET1.length());
				break;
			case 111:
				String tmp = (String) msg.obj;
				ET1.append(tmp);
				ET1.setSelection(ET1.length());
				break;
			case 12:
				break;
			default:
				break;
			}
		}
	};

	class manager implements OnClickListener {
		public void onClick(View v) {
			String rxIdCode = "";
			String str;

			int i;
			switch (v.getId()) {
			// recvive
			case R.id.recv1:
				Log.d(tag, "recv start ...");
				byte[] RX = com3.Read();
				if (RX == null)
					return;
				ET1.append(new String(RX, 0, RX.length));
				ET1.setSelection(ET1.length());
				break;

			// send
			case R.id.send1:
				Log.d(tag, "send start ...");
				CharSequence tx = ET1.getText();
				int[] text = new int[2];
				text[0] = 97;
				text[1] = 97;
				// for (i=0; i<tx.length(); i++)
				// {
				// text[i] = tx.charAt(i);
				// }
				com3.Write(text, tx.length());
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	static {
		System.loadLibrary("serialtest");
	}

	public class StartReadThread extends Thread {
		@Override
		public void run() {

			// TODO Auto-generated method stub
			try {
				while (true) {
					if (isStartReceive) {
						try {
							byte[] RX = com3.Read();
							if (RX == null) {
								// Log.i(TAG, "rx = null");
								continue;
							}

							ET1.append(new String(RX, 0, RX.length));
							ET1.append("\n");
							ET1.setSelection(ET1.length());
						} catch (Exception e) {
							Log.i(TAG, e.getMessage());
						}
					}
				}
			} catch (Exception e) {
				// Log.i("", line);
				e.printStackTrace();
			}
		}

	}

	public class StartReadWithInputStreamThread extends Thread {
		int bytes;

		@Override
		public void run() {

			// TODO Auto-generated method stub
			try {
				while (true) {
					if (isStartReceive) {
						try {
							bytes = 0;
							while (bytes == 0 && isStartReceive) {
								bytes = inputStream.available();
							}
							if (bytes > 0) {
								// Log.e(TAG,"available bytes: " + bytes);
								byte[] buf = new byte[bytes];
								inputStream.read(buf, 0, bytes);
								for (int i = 0; i < buf.length; i++) {
									Log.i(TAG, "i=" + i + "buf=" + buf[i]);
									Message msg = MyHandler.obtainMessage();
									msg.what = 0;
									msg.arg1 = buf[i];
									MyHandler.sendMessage(msg);
									MyHandler.sendEmptyMessage(0);
								}
							}

						} catch (Exception e) {
							Log.i(TAG, e.getMessage());
						}
					}
				}
			} catch (Exception e) {
				// Log.i("", line);
				e.printStackTrace();
			}
		}
	}
}
