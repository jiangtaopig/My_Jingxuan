package com.bestv.ott.jingxuan.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.bestv.ott.jingxuan.livetv.OttContextMgr;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.util.DataConfig;
import com.bestv.ott.jingxuan.util.IOTV_Date;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.proxy.qos.IOTVMediaPlayer;
import com.bestv.ott.proxy.qos.IOTVMediaPlayer.IOTVMediaPlayerEventL;
import com.bestv.jingxuan.R;

public class RestPromptView extends FrameLayout{
	private IOTVMediaPlayer mMediaPlayer;
	private IControllerBase mController;
	
	static final int MONITOR_INTERVAL_TIME = 5000;
	static final int MAX_WATCH_TIME = 60 * 1000 * 60 * 2; //2 hours
	static final int EXIT_APP_INTERVAL = 60 * 1000 * 2; //2分钟没操作，退出程序
	
	boolean mTimeLock = false; //if media play is paused, the time will be locked.
	
	static final int MSG_MONITOR = 1;
	static final int MSG_EXIT = 2;
	
	private long mDuration = 0;
	
	private int mMaxWatchTime;
	private int mExitAppInterval;
	
	boolean mQuit = false;
	
	ResetViewShowListener mListener;
	
	public interface ResetViewShowListener{
		void onShow();
	}
	
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			if (mQuit){
				return;
			}
			
			if (msg.what == MSG_EXIT){
				mController.exitApp();
			}
			
			if (msg.what == MSG_MONITOR){
				//Logger.d("restprompt duratiom = " + mDuration + " time lock = " + mTimeLock + " this = " + RestPromptView.this);
				if (DataConfig.getDataConfigValueL(getContext(), DataConfig.TEST_REST_MAX_TIME) > 0){
					if (mMaxWatchTime != DataConfig.getDataConfigValueL(getContext(), DataConfig.TEST_REST_MAX_TIME)){
						mMaxWatchTime = (int) DataConfig.getDataConfigValueL(getContext(), DataConfig.TEST_REST_MAX_TIME);
						mExitAppInterval = (int) DataConfig.getDataConfigValueL(getContext(), DataConfig.TEST_EXIT_APP_TIME);
						mDuration = 0;
					}
				}
				
				if (!mTimeLock){
					mDuration += MONITOR_INTERVAL_TIME;
					
					if (mDuration >= mMaxWatchTime){
						showMe();
						mTimeLock = true;
					}
				}
				
				mHandler.sendEmptyMessageDelayed(MSG_MONITOR, MONITOR_INTERVAL_TIME);
			}
		}
	};
	
	void showMe(){
		Logger.d("showMe restprompt view time = " + IOTV_Date.getCurrentTimeS());
		
		mMediaPlayer.pause();
		this.setVisibility(View.VISIBLE);
		mHandler.sendEmptyMessageDelayed(MSG_EXIT, mExitAppInterval);
		
		if (mListener != null){
			mListener.onShow();
		}
	}
	
	void hideME(){
		Logger.d("hideME restprompt view time = " + IOTV_Date.getCurrentTimeS());
		
		if (mHandler.hasMessages(MSG_EXIT)){
			mHandler.removeMessages(MSG_EXIT);
		}
		
		mMediaPlayer.unpause();
		this.setVisibility(View.INVISIBLE);
		reset();
		mHandler.sendEmptyMessageDelayed(MSG_MONITOR, MONITOR_INTERVAL_TIME);
	}
	
	IOTVMediaPlayerEventL mEventL = new IOTVMediaPlayer.IOTVMediaPlayerEventL(){
		@Override
		public void onBesTVMediaPlayerEvent(BesTVMediaPlayerEvent event) {
			// TODO Auto-generated method stub
		}

		@Override
		public void pause() {
			// TODO Auto-generated method stub
			super.pause();
			mTimeLock = true;
		}

		@Override
		public void unpause() {
			// TODO Auto-generated method stub
			super.unpause();
			mTimeLock = false;
		}
	};
	
	public RestPromptView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public RestPromptView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public RestPromptView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void setRestShowListener(ResetViewShowListener l){
		mListener = l;
	}

	public void init(IOTVMediaPlayer player, IControllerBase controler){
		//only open this feature in B2C
		if (!OttContextMgr.getInstance().isOEMB2C()){
			return;
		}
		
		mMediaPlayer = player;
		mController = controler;
		
		mQuit = false;
		
		mMaxWatchTime = MAX_WATCH_TIME;
		mExitAppInterval = EXIT_APP_INTERVAL;
		
		mEventL.setEventName("RestPrompt");
		mMediaPlayer.setEventListener(mEventL);
		
		mDuration = 0;
		mTimeLock = false;
		
	    findViewById(R.id.btnContinue).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub	
				hideME();
			}
		});
		
       findViewById(R.id.btnRest).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mController.exitApp();
			}
		});
       
       mHandler.sendEmptyMessageDelayed(MSG_MONITOR, MONITOR_INTERVAL_TIME);
	}
	
	public void reset(){
		mDuration = 0;
		mTimeLock = false;
	}
	
	public void resetDuration(){
		mDuration = 0;
	}
	
	public void quit(){
		mQuit = true;
	}
}
