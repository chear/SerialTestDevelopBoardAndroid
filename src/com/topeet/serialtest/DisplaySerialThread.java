package com.topeet.serialtest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class DisplaySerialThread extends Thread {

	serial com3 = new serial();
	private String tag = "Serial";
	private boolean isStop = false;
	DataHandler callbackHandler;

	// TODO flag to recording data
	private boolean needSaveData = false;
	// TODO recording data to disk
	File file = null;
	PrintWriter pw;
	RandomAccessFile rf;
	BufferedOutputStream bos;

	public DisplaySerialThread() {
		try {

			/* init folder */
			File sdcardDir = Environment.getExternalStorageDirectory();
			String path = sdcardDir.getPath() + "/neurosky";
			makeRootDirectory(path);
			Date d = new Date();
			file = new File(path + "/SSave-"
					+ String.format("%tH", d.getTime()) + "-"
					+ String.format("%tM", d.getTime()) + ".txt");
			file.createNewFile();
			pw = new PrintWriter(file);
			FileOutputStream outer = new FileOutputStream(file);
			bos = new BufferedOutputStream(outer);
			// rf = new RandomAccessFile(file, "rw");
		} catch (Exception ex) {
			Log.e(tag, "setHandler Exception:" + ex);
		}
	}

	public void makeRootDirectory(String filePath) {
		File file = null;
		try {
			file = new File(filePath);
			if (!file.exists()) {
				file.mkdir();
			}
		} catch (Exception e) {

		}
	}

	public void setStop(boolean _stop) {
		isStop = _stop;
	}

	private boolean getStop() {
		return isStop;
	}

	public void setHandler(DataHandler _handler) {
		try {
			Log.i(tag, "~~~~~~~~~~~~~~~~~~~~~~setting handler!!");
			callbackHandler = _handler;
		} catch (Exception ex) {
			Log.e(tag, "setHandler Exception:" + ex);
		}
	}

	@Override
	public void run() {
		com3.Open(3, 57600);
		Log.d(tag, "recv start ...");
		while (getStop()) {
//			byte[] RX = com3.Read();
			byte[] RX = com3.GetRawData();
			if (RX == null)
				continue;
			else {
				try {
//					String e = new String(RX, 0, RX.length);
//					callbackHandler.onDataReceive(111, 0, e);
					bos.write(RX);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		try {
			bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
