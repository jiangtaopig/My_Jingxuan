package com.bestv.ott.jingxuan;

//import com.bestv.ott.framework.proxy.authen.AuthenProxy;
//import com.bestv.ott.framework.proxy.config.ConfigProxy;
import com.bestv.ott.jingxuan.livetv.IOTVBuilder;
import com.bestv.ott.jingxuan.livetv.OttContextMgr;
import com.bestv.ott.jingxuan.livetv.controller.IOTVControllerBase;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.utils;
import com.bestv.ott.proxy.qos.PageVisitedLog;
import com.bestv.ott.proxy.qos.QosBaseActivity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

public class JX_Activity extends QosBaseActivity{
	private static final String TAG = "JX_Activity";
	IOTVControllerBase mController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.d("Apk info: " + utils.getVersionCode(getApplicationContext()) + ", "
				+ utils.getVersionName(getApplicationContext()));
		OttContextMgr.getInstance().lockInstance(true);
		OttContextMgr.getInstance().setAppRunningType(OttContextMgr.APP_RUNNING_TYPE_JX);
		mController = IOTVBuilder.createController(IOTVBuilder.BUILD_TYPE_JX);
		mController.setOwner(this);
		setContentView(mController.getRootView());
		getWindow().setFormat(PixelFormat.RGBA_8888);
		mController.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		setIntent(intent);
		OttContextMgr.getInstance().lockInstance(true);
		OttContextMgr.getInstance().setAppRunningType(OttContextMgr.APP_RUNNING_TYPE_JX);
		super.onNewIntent(intent);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		mController.onStop();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mController.onDestroy();
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		OttContextMgr.getInstance().lockInstance(false);
		mController.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		OttContextMgr.getInstance().lockInstance(true);
		mController.onResume();
		super.onResume();
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		mController.onStart();
		super.onStart();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		mController.onBackPressed();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.d("onKeyDown", "keyCode: " + keyCode);
		if (mController.onKeyDown(keyCode, event)){
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.d("onKeyUp", "keyCode: " + keyCode);
	    if (mController.onKeyUp(keyCode, event)){
	    	return true;
	    }
	    
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		mController.dispatchKeyEvent(event);
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	protected void setPageVisitedLogParam() {
		mPageVisitedLog = new PageVisitedLog();
		mPageVisitedLog.setPageName("JingxuanPage"); 
		mPageVisitedLog.setPageType(PageVisitedLog.PAGE_TYPE_OTHER);
		mPageVisitedLog.setContentType(PageVisitedLog.CONTENT_TYPE_JINGXUAN); 
	}
}