package com.bestv.ott.jingxuan.util;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class ChannelPlayUrlUtils {
	private final static String TAG = "ChannelPlayUrlUtils";
	private static Map<String, String> mChannelPlayUrlMap = new HashMap<String, String>();
	public static void clearChannelPlayUrl(){
		mChannelPlayUrlMap.clear();
	}
	
	public static void setChannelPlayUrl(String channelCode, String playUrl){
//		Log.i(TAG, "setChannelPlayUrl,channelCode="+channelCode
//				+",playUrl"+playUrl);
		mChannelPlayUrlMap.put(channelCode, playUrl);
	}
	public static String getChannelPlayUrl(String channelCode){
		Log.i(TAG, "getChannelPlayUrl,channelCode="+channelCode);
		return mChannelPlayUrlMap.get(channelCode);
	}
}
