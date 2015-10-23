package com.topeet.serialtest;

public class serial {
	
	public native int 	Open(int Port,int Rate);
	public native int 	Close();
	public native byte[]	Read();
	public native int	Write(int[] buffer,int len);
	public native byte[] GetRawData();

}
