package com.bestv.ott.jingxuan.livetv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.bestv.ott.jingxuan.livetv.controller.IOTVControllerBase;

public class IOTV_Activity extends Activity{
	IOTVControllerBase mController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		OttContextMgr.getInstance().lockInstance(true);
		OttContextMgr.getInstance().setAppRunningType(OttContextMgr.APP_RUNNING_TYPE_IOTV);
		
		mController = IOTVBuilder.createController(IOTVBuilder.BUILD_TYPE_IOTV);
		mController.setOwner(this);
		setContentView(mController.getRootView());
		getWindow().setFormat(PixelFormat.RGBA_8888);
		mController.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		setIntent(intent);
		OttContextMgr.getInstance().lockInstance(true);
		OttContextMgr.getInstance().setAppRunningType(OttContextMgr.APP_RUNNING_TYPE_IOTV);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		mController.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mController.onDestroy();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		OttContextMgr.getInstance().lockInstance(false);
		mController.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		OttContextMgr.getInstance().lockInstance(true);
		mController.onResume();
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		mController.onStart();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Log.d("harish", "onBackPressed 1");
		mController.onBackPressed();
		Log.d("harish", "onBackPressed 2");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (mController.onKeyDown(keyCode, event)){
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
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
}
