package com.bestv.ott.jingxuan.net;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import android.content.Context;

import com.bestv.ott.jingxuan.livetv.OttContextMgr;

/**
 * will be thrown if an error has occurred in network transaction or deal with response data.
 * */
public class IOTVNetException extends Exception {
	//exception type
	public final static int TYPE_TAKE_CDNTOKEN = 0x0001;
	public final static int TYPE_INIT_CHANNELLIST = 0x0002;
	public final static int TYPE_INIT_SCHEDULEDATA = 0x0004;
	public final static int TYPE_PLAY_AUTHEN = 0x0008;
	public final static int TYPE_PLAY_ERROR_LIVE = 0x0010;
	public final static int TYPE_PLAY_ERROR_LLKAN = 0x0020;
	public final static int TYPE_PLAY_ERROR_LOOKBACK = 0x0040;
	public final static int TYPE_NETWORK_UNAVAILABLE = 0x0080;
	
	public final static int TYPE_INIT_JXCATEGORY = 0x0100;
	public final static int TYPE_PLAY_ERROR_JX = 0x0200;
	
	static Map<Integer, String> mPrefixCodeMap;
	
	static{
		mPrefixCodeMap = new HashMap<Integer, String>();
		
		mPrefixCodeMap.put(TYPE_TAKE_CDNTOKEN, "2100");
		mPrefixCodeMap.put(TYPE_INIT_CHANNELLIST, "2103");
		mPrefixCodeMap.put(TYPE_INIT_SCHEDULEDATA, "2104");
		mPrefixCodeMap.put(TYPE_PLAY_AUTHEN, "2300");
		mPrefixCodeMap.put(TYPE_PLAY_ERROR_LIVE, "21");
		mPrefixCodeMap.put(TYPE_PLAY_ERROR_LOOKBACK, "22");
		mPrefixCodeMap.put(TYPE_PLAY_ERROR_LLKAN, "23");
	}
	
	private int mType;
	private String mErrorCode;
	private int mHttpResultCode = 0;
	
	public IOTVNetException(String msg, int type) {
		super(msg);
		
		mType = type;
		mErrorCode = "";
	}
	
	public int getType(){
		return mType;
	}
	
	public void setHttpResultCode(int resultCode){
		mHttpResultCode = resultCode;
		
		if (resultCode == HttpStatus.SC_NOT_FOUND){
			mErrorCode = "92";
		}else if (resultCode == HttpStatus.SC_REQUEST_TIMEOUT){
			mErrorCode = "94";
		}else if (resultCode > 400 && resultCode < 600){
			mErrorCode = "93";
		}else{
			mErrorCode = "95";
		}
	}
	
	public int getHttpResultCode(){
		return mHttpResultCode;
	}
	
	public void setErrorCode(String code){
		mErrorCode = code;
	}
	
	public String getErrorCode(){
		return mErrorCode;
	}
	
	/**
	 * complete error code = type + error code
	 * */
	public String getCompleteErrorCode(){
		String companyPrefix = "";
		
		try{
			companyPrefix = OttContextMgr.getInstance().getErrCodePrefix();
		}catch(Exception e){
			companyPrefix = "";
		}
		
		if (mErrorCode.equals("")){
			return mErrorCode;
		}
		
		if (companyPrefix == null){
			companyPrefix = "";
		}
		
		return companyPrefix + mPrefixCodeMap.get(mType) + mErrorCode;
	}
	
	public static IOTVNetException builder(Context context, int type, int strid){
		String msg = context.getResources().getString(strid);
		IOTVNetException e = new IOTVNetException(msg, type);
		return e;
	}
}
