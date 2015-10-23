package com.topeet.serialtest;

import java.io.FileDescriptor;

import android.util.Log;

public class Native {
	private static final String TAG = "Native";
		static{
			Log.i(TAG, "Loading NSUART library");
			try {
				System.loadLibrary("NSUART");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG,"Load lib error: "+ e.toString());
			}
			Log.i(TAG, "Loaded NSUART library");
		}

		public static native FileDescriptor SerialJNI_open(String dev);
		public static native void SerialJNI_close();
}