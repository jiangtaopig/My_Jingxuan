package com.bestv.ott.jingxuan;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.utils;

public class ErrorInfoMgr {
	public static final String ERRORCODE_FOR_DETAIL = "060000";
	public static final String ERRORCODE_FOR_PROGRAM = "060200";
	public static final String ERRORCODE_FOR_PS = "050005";
	
	private Map<String, String> mErrorDescMap;
	private Context mContext;
	private IControllerBase mController;
	
	private static ErrorInfoMgr instance;
	
	public static ErrorInfoMgr getInstance(){
		if (instance == null){
			instance = new ErrorInfoMgr();
		}
		
		return instance;
	}
	
	public ErrorInfoMgr() {
		// TODO Auto-generated constructor stub
	}
	
	public void setContext(Context context){
		mContext = context;
		
		if (mErrorDescMap == null){
			Resources res = mContext.getResources();
			String[] errors = res.getStringArray(R.array.error_msgs);
			
			mErrorDescMap = new HashMap<String, String>();
			
			for (String str : errors){
				String key = str.substring(0, 6);
				String value = str.substring(6, str.length());
				
				//Logger.d("error info msg key = " + key + " value = " + value);
				mErrorDescMap.put(key, value);
			}
		}
	}
	
	public void setController(IControllerBase controller){
		mController = controller;
	}
	
	public String getPlayErrorPromptMsg(String errorCode){
		Logger.d("show error info dialog, error Code = " + errorCode);
		if (utils.isNull(errorCode)){
			errorCode = "059999";
		}
		
		String[] ecodes = errorCode.split(",");
		errorCode = ecodes[0];
		
		String errorMsg = "";
		
		for (String key : mErrorDescMap.keySet()) {
		    String value = mErrorDescMap.get(key);
		    
		    if (key.contains(errorCode)){
		    	errorCode = key;
		    	errorMsg = value;
		    	break;
		    }
		}
		
		if (utils.isNull(errorMsg)){
			errorMsg = mContext.getResources().getString(R.string.play_error_msg);
		}
		
		String str = mContext.getResources().getString(
				R.string.play_errorcode_prompt);
		errorCode = String.format(str, errorCode);
		
		return errorMsg + ", " + errorCode;
		//showErrorDialog(errorCode, errorMsg);
	}
}
