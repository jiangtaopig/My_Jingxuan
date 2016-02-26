package com.bestv.ott.jingxuan.livetv;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.DataConfig;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.utils;

/**
 * @hufuyi
 * the class is used to generating shortcut keys
 * */
public class ShortcutKeyMgr extends Handler {
	static final String TAG = "SKey";
	
    static final String LOG_CONTROL_KEYS = "#98#87#";
    static final String SHOW_VERISON_KEYS = "0002";
    static final String CLEAR_CACHE_KEYS = "0001";
    static final String RESTPROMPT_TEST_KEYS = "#0003#";
    static final String OPEN_LIVETV_TIMESHIFT = "#0004#";
    static final String SHOW_DOWNLOAD_DETAIL = "#9999";
    
    public static final int MENU_KEY = KeyEvent.KEYCODE_STAR;
    
    static final int MSG_SHORTCUT_INPUT_TIMEOUT = 99;
    static final int KEY_DELAY_TIME = 2000;
    
    private Context mContext;
    private String current_keys = "";
    private IControllerBase mController;
    
    class ShortcutKey{
    	String keys;
    	Runnable runnable;
    	
    	public ShortcutKey(String k, Runnable r){
    		keys = k;
    		runnable = r;
    	}
    }
    
    List<ShortcutKey> mShorycutKeys = new ArrayList<ShortcutKey>();
    
	@Override
	public void handleMessage(Message arg0) {
		// TODO Auto-generated method stub
		super.handleMessage(arg0);
		
		if (arg0.what == MSG_SHORTCUT_INPUT_TIMEOUT){
			current_keys = "";
		}
	}
	
	private void showToast(String text){
		Toast t = Toast.makeText(mContext,
			     "", Toast.LENGTH_LONG);
		TextView tv = new TextView(mContext);
	    ColorDrawable cd = new ColorDrawable(Color.BLACK);
	    cd.setAlpha(175);
	    tv.setBackgroundDrawable(cd);
	    
		tv.setText(text);
		tv.setTextSize(22f);
		t.setView(tv);
		t.show();
	}
	
	public void init(Context c, IControllerBase controller){
		mController = controller;
		mContext = c;
		
		mShorycutKeys.clear();
		
		ShortcutKey tmp = new ShortcutKey(LOG_CONTROL_KEYS, new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Logger.d(TAG, "doneByShotcutKey keys = ");
				boolean isopen = utils.getQosOpenState(mContext);
				
				utils.setQosOpenState(mContext, !isopen);
				
				if (isopen){
					showToast(mContext.getResources().getString(R.string.prompt_qos_close));
				}else{
					showToast(mContext.getResources().getString(R.string.prompt_qos_open));
				}
				
				current_keys = "";
			}
		});
		
		mShorycutKeys.add(tmp);
		
		tmp = new ShortcutKey(SHOW_VERISON_KEYS, new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Logger.d(TAG, "SHOW_VERISON_KEYS keys = ");
				String str = String.format(mContext.getResources().getString(R.string.key_appinfo), utils.getVersionCode(mContext),
						                    utils.getVersionName(mContext));
				showToast(str);
				
				current_keys = "";
			}
		});
		
		mShorycutKeys.add(tmp);
		
		tmp = new ShortcutKey(CLEAR_CACHE_KEYS, new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Logger.d(TAG, "CLEAR_CACHE_KEYS keys = ");
				DataConfig.clearAllCacheFile();
				showToast(mContext.getResources().getString(R.string.key_clear));
				
				current_keys = "";
				
				ShortcutKeyMgr.this.postDelayed(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mController.exitApp();
						System.exit(1);
					}
				}, 3000);
				
				ShortcutKeyMgr.this.postDelayed(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						System.exit(1);
					}
				}, 6000);
			}
		});
		
		mShorycutKeys.add(tmp);
		
		//only register this shortcut key in B2C
		if (OttContextMgr.getInstance().isOEMB2C()){
			tmp = new ShortcutKey(RESTPROMPT_TEST_KEYS, new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Logger.d(TAG, "RESTPROMPT_TEST_KEYS keys = ");
					DataConfig.setDataConfigValueL(mContext, DataConfig.TEST_REST_MAX_TIME, Constants.TEST_MAX_WATCH_TIME);
					DataConfig.setDataConfigValueL(mContext, DataConfig.TEST_EXIT_APP_TIME, Constants.TEST_EXIT_APP_TIME);
					
					showToast(mContext.getResources().getString(R.string.rest_prompt_test));
					
					current_keys = "";
				}
			});
			
			mShorycutKeys.add(tmp);
		}
		
		tmp = new ShortcutKey(SHOW_DOWNLOAD_DETAIL, new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Logger.d(TAG, "SHOW_DOWNLOAD_DETAIL keys = ");
				DataConfig.setDataConfigValueL(mContext, DataConfig.DEFAULT_SHOW_DOWNDETAIL, 1);
				showToast(mContext.getResources().getString(R.string.show_download_detail));
				current_keys = "";
			}
		});
		
		mShorycutKeys.add(tmp);
		
		tmp = new ShortcutKey(OPEN_LIVETV_TIMESHIFT, new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Logger.d(TAG, "OPEN_LIVETV_TIMESHIFT keys = ");
				boolean isopen = DataConfig.supportLiveTimeShift(mContext);
				
				if (isopen){
					DataConfig.setDataConfigValueL(mContext, DataConfig.SUPPORT_LIVE_TIMESHIFT, 0);
					showToast(mContext.getResources().getString(R.string.timeshift_close_prompt));
				}else{
					DataConfig.setDataConfigValueL(mContext, DataConfig.SUPPORT_LIVE_TIMESHIFT, 1);
					showToast(mContext.getResources().getString(R.string.timeshift_open_prompt));
				}
				
				current_keys = "";
			}
		});
		
		mShorycutKeys.add(tmp);
	}
	
	private String convertKeyToStr(int key){
		if (key >= KeyEvent.KEYCODE_0 && key <= KeyEvent.KEYCODE_9){
			return "" + (key - KeyEvent.KEYCODE_0);
		}
		
		if (key == KeyEvent.KEYCODE_DEL){
			return "#";
		}
		
		//other keys are not used to compose shortcut key, so return a invalid character to finish this compose
		return "_";
	}
	
	public boolean isShortcutKeys(int key){
		String new_key = convertKeyToStr(key);
		
		if(hasMessages(MSG_SHORTCUT_INPUT_TIMEOUT)){
			removeMessages(MSG_SHORTCUT_INPUT_TIMEOUT);
		}
		
		current_keys += new_key;
		
		Logger.d(TAG, "current keys = " + current_keys);
		
		for (ShortcutKey shortcut_key : mShorycutKeys) {
			if (shortcut_key.keys.indexOf(current_keys) == 0) {
				if (shortcut_key.keys.length() == current_keys.length()) {
					// confirm this is a shortcut key
					shortcut_key.runnable.run();
				}else{
					this.sendEmptyMessageDelayed(MSG_SHORTCUT_INPUT_TIMEOUT, KEY_DELAY_TIME);
				}
				
				return true;
			}
		}
		
		//the input keys are not the shortcut key, clear current keys immediately
		current_keys = "";
		return false;
	}
}
