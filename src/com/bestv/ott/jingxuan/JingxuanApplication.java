package com.bestv.ott.jingxuan;

import com.fun.crash.HandlerCrash;

import android.app.Application;

public class JingxuanApplication extends Application {
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		HandlerCrash.getInstance().init(getApplicationContext());
	}
}
