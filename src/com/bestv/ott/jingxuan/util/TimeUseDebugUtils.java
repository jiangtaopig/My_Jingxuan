package com.bestv.ott.jingxuan.util;

import android.util.Log;

public class TimeUseDebugUtils {
	private static long startTime = 0;
	private final static String TAG = "TimeUseDebugUtils";
	public static void setStartPoint(){
		startTime = System.currentTimeMillis();
	}
	
	public static void timeUsageDebug(String msg){
		Log.i(TAG, msg+",time used = "+(System.currentTimeMillis()-startTime) );
	}
}
