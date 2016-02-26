package com.bestv.ott.jingxuan.iotvlogservice.logmanager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class WorkHandlerMgr {
	private HandlerThread workThread = new HandlerThread("database_mgr_workthread");
	private Handler mHandler = null;
	
	static WorkHandlerMgr whg = null;
	
	public static WorkHandlerMgr getInstance(){
		if (whg == null){
			whg = new WorkHandlerMgr();
		}
		
		return whg;
	}
	
	public void init(){
		workThread.start();
		mHandler = new Handler(workThread.getLooper());
	}
	
	public Handler getHandler(){
		if (mHandler == null){
			init();
		}
		
		return mHandler;
	}
	
	public Looper getMainLooper(){
		if (mHandler == null){
			init();
		}
		
		return workThread.getLooper();
	}
	
	private WorkHandlerMgr() {
		// TODO Auto-generated constructor stub
	}
}
