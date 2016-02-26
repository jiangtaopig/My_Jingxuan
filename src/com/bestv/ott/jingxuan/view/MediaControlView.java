package com.bestv.ott.jingxuan.view;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.model.ChannelData;
import com.bestv.ott.jingxuan.model.ChannelManager;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.utils;
import com.bestv.ott.jingxuan.view.mediacontrol.IMediaControlAdapter;
import com.bestv.ott.jingxuan.view.mediacontrol.LianLianLookMediaControlAdapter;
import com.bestv.ott.jingxuan.view.mediacontrol.LiveTVMediaControlAdapter;
import com.bestv.ott.jingxuan.view.mediacontrol.LookbackMediaControlAdapter;
import com.bestv.ott.mediaplayer.BesTVPlayerStatus;
import com.bestv.ott.proxy.qos.IOTVMediaPlayer;

public class MediaControlView extends RelativeLayout implements IViewBase{
	public static final int HIDE_TIME_NORMAL = 5000;
	public static final int HIDE_TIME_FIRST = 2000;
	
	static final int MSG_HIDE_SELF = 99;
	static final int MSG_UPDATE_PROGRESS = 100;
	static final int MSG_REFRESH_PLAYING_SCHEDULE = 101;
	
	static final int MAX_SWITCH_COUNT = 5;
	
	IControllerBase controller;
	IOTVMediaPlayer mMediaPlayer;
	
	BitmapDrawable mSeekForwardDrawable;
	BitmapDrawable mSeekBackwardDarwable;
	
	MediaPlayerEvent mPlayEvent;
	
	ImageView mStateView;
	ImageView mLoadingView;
	AnimationDrawable mLoadingAnim;
	MediaSeekProgress mProgressBar;
	
	int mSeekUnit;

	boolean mHasKeyDown = false;
	private long mStartTime;
	private int mHideTime = HIDE_TIME_NORMAL;
	
	IMediaControlAdapter mCurMediaControl;
	IMediaControlAdapter mLookbackMediaControl;
	IMediaControlAdapter mLiveTVMediaControl;
	IMediaControlAdapter mLianLianLookMediaControl;

	
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			switch(msg.what){
			case MSG_HIDE_SELF:
				Logger.d("hide media control view");
				mStateView.setBackgroundDrawable(null);
				
				hide();
				break;
			case MSG_UPDATE_PROGRESS:
			{
				int curtime = msg.arg1;
				int total = msg.arg2;
				
				if (total != mProgressBar.getMax() && total != 0){
					mProgressBar.setMax(total);
					TextView end_time = (TextView) findViewById(R.id.tv_endtime);
					end_time.setText(utils.getTimeL(mStartTime + total * 1000));
				}
				
				mProgressBar.invalidate();
				mProgressBar.setProgress(curtime);
				mProgressBar.setText(utils.getTimeL(mStartTime + curtime * 1000));
				
				if (mLoadingView.isShown()){
					mLoadingAnim.stop();
					mLoadingView.setVisibility(View.INVISIBLE);
				}
				
				break;
			}
			case MSG_REFRESH_PLAYING_SCHEDULE:
			{
				Channel c = controller.getCurrentChannel();
				Schedule sc = (controller.getPlayingSubSchedule() == null)?controller.getPlayingSchedule():controller.getPlayingSubSchedule();
				mCurMediaControl.refreshPlayingSchedule(c, sc);
				break;
			}
			default:
				break;
			}
		}
	};
	
	class MediaPlayerEvent extends IOTVMediaPlayer.IOTVMediaPlayerEventL{
		@Override
		public void onBesTVMediaPlayerEvent(BesTVMediaPlayerEvent event) {
			// TODO Auto-generated method stub
			if (!isShown()){
				return;
			}
			
			switch (event.getParam3()) {
			case BesTVPlayerStatus.BESTV_PLAYER_PLAYING:
				Logger.d("--------MediaControlView--playing------------ time = " + mMediaPlayer.getIOTVCurTime());
				//stamp time for the first playing call back
				//Logger.d("playing isseeking = " + misSeeking);
				//if (mMediaPlayer.isFirstPlaying() && mCurMediaControl instanceof LianLianLookMediaControlAdapter){
				//	mCurMediaControl.notifyFirstPlay();
				//}
				
				if (!mHasKeyDown){
					Message msg = mHandler.obtainMessage(MSG_UPDATE_PROGRESS);
					msg.arg1 = mCurMediaControl.getMediaCurrentTime();
					msg.arg2 = mCurMediaControl.getMediaTotalTime();
					msg.sendToTarget();
					
					Message msg1 = mHandler.obtainMessage(MSG_REFRESH_PLAYING_SCHEDULE);
					msg1.sendToTarget();
				}
				
				Log.d("harish", "mCurMediaControl.isSeeking() = " + mCurMediaControl.isSeeking());
				
				if (mCurMediaControl.isSeeking()){
					mCurMediaControl.seekFinish();
					controller.resetStopLiveDelayTime((mCurMediaControl.getMediaTotalTime() - mCurMediaControl.getMediaCurrentTime()) * 1000);
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							mLoadingAnim.stop();
							mLoadingView.setVisibility(View.INVISIBLE);
							
							//Adjust progress
							setProgress(mCurMediaControl.getMediaCurrentTime());
							mHandler.sendEmptyMessageDelayed(MSG_HIDE_SELF, mHideTime);
						}
					});
				}else{
					
				}
				break;
			case BesTVPlayerStatus.BESTV_PLAYER_SEEKING:
				break;
			case BesTVPlayerStatus.BESTV_PLAYER_BUFFERING:
				Logger.d("--------MediaControlView--buffering------------");
				//if it is seeking now, interrupt this event
			    interrupt = true;
				break;
			case BesTVPlayerStatus.BESTV_PLAYER_END:
				Logger.d("-------MediaControlView---play over------------");
				break;
			case BesTVPlayerStatus.BESTV_PLAYER_ERROR:
				Logger.d("-------MediaControlView---play error------------");
				break;
			}
		}
	}
	
	public MediaControlView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initResource();
	}

	public MediaControlView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initResource();
	}

	public MediaControlView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initResource();
	}
	
	private void initResource(){
		InputStream inputStream = getResources().openRawResource(R.drawable.jx_iotv_img_state_fast);
	    mSeekForwardDrawable = new BitmapDrawable(inputStream);  
		try {
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    inputStream = getResources().openRawResource(R.drawable.jx_iotv_img_state_back);
	    mSeekBackwardDarwable = new BitmapDrawable(inputStream);  
		try {
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setDataController(IControllerBase c) {
		// TODO Auto-generated method stub
		controller = c;
	}

	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		if (mHandler.hasMessages(MSG_HIDE_SELF)){
			mHandler.removeMessages(MSG_HIDE_SELF);
		}
		
		mHideTime = HIDE_TIME_NORMAL;
		
		Integer hidetime = (Integer) this.getTag();
		if (hidetime != null){
			mHideTime = hidetime;
		}
		
		if (mMediaPlayer != controller.getIOTVMediaPlayer()) {
			mMediaPlayer = controller.getIOTVMediaPlayer();

			mPlayEvent = new MediaPlayerEvent();
			mPlayEvent.setPriority(100);
			mPlayEvent.setEventName("MediaControlView");
			/*modify by xubin begin*/
			if(mMediaPlayer != null){
				mMediaPlayer.setEventListener(mPlayEvent);
			}
			/*modify by xubin end*/
		}
		
		if (mLoadingView == null){
			mProgressBar = (MediaSeekProgress) findViewById(R.id.mediaSeekbar);
			mProgressBar.setContext(this);
						
			mStateView = (ImageView) findViewById(R.id.play_state);
			
			mLoadingView = (ImageView) findViewById(R.id.mc_loading_img);
			mLoadingView.setBackgroundResource(R.anim.jx_iotv_tvba_loads);
			mLoadingAnim = (AnimationDrawable) mLoadingView.getBackground();
			mLoadingView.setVisibility(View.INVISIBLE);
			
			mLookbackMediaControl = new LookbackMediaControlAdapter(IMediaControlAdapter.MEDIA_CONTROL_LOOKBACK);
			mLiveTVMediaControl = new LiveTVMediaControlAdapter(IMediaControlAdapter.MEDIA_CONTROL_LIVETV);
			mLianLianLookMediaControl = new LianLianLookMediaControlAdapter(IMediaControlAdapter.MEDIA_CONTROL_LIANLIANLOOK);
			
			mLiveTVMediaControl.setProgressBar(mProgressBar);
			mLookbackMediaControl.setProgressBar(mProgressBar);
			mLianLianLookMediaControl.setProgressBar(mProgressBar);
		}
		
		//Log.d("harish", "the play state of media player = " + mMediaPlayer.getPlayState());
		if (mMediaPlayer != null && mMediaPlayer.getPlayState() != IOTVMediaPlayer.PlayState.STATE_PAUSE){
		    mHandler.sendEmptyMessageDelayed(MSG_HIDE_SELF, mHideTime);
		}
		
		if (initControls()){
			this.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void reset() {
		if(mCurMediaControl != null) {
			mCurMediaControl.reset();
		}
	}
	private boolean isFirstShowInCurrentPlay(){
		return mHideTime == HIDE_TIME_FIRST;
	}
	
	private boolean initControls(){
		if(mMediaPlayer != null){
			Logger.d("initControls = " + mMediaPlayer.getTotalTime() + " current time = " + mMediaPlayer.getCurrentTime());
		}
		
		Channel c = controller.getCurrentChannel();
		Schedule sc = (controller.getPlayingSubSchedule() == null)?controller.getPlayingSchedule():controller.getPlayingSubSchedule();
		
		int curType = controller.getCurrentIOTVType();
		
		if (curType == Constants.IOTV_PLAY_LIVE_SOURCE){
			if (c.getType() == Constants.CHANNEL_IOTV){
//			    ChannelData cd = ChannelManager.getInstance().getCurrentSchedule(c);
//			    sc = cd.getSchedule();
			
			    mCurMediaControl = mLiveTVMediaControl;
			    mLookbackMediaControl.reset();
			    mLianLianLookMediaControl.reset();
			}else{
			    mCurMediaControl = mLianLianLookMediaControl;
			    mLookbackMediaControl.reset();
			    mLiveTVMediaControl.reset();
			}
		}else{
			mCurMediaControl = mLookbackMediaControl;
			mLiveTVMediaControl.reset();
			mLianLianLookMediaControl.reset();
		}
		
		if (sc == null){
			if (!isFirstShowInCurrentPlay()){
			    showToast(getContext().getResources().getString(R.string.unsupport_timeshift));
			}
			return false;
		}
		
		Log.d("harish", "Channel c = " + c.getLivePlayUrl());
		
		mCurMediaControl.initControls(this, controller, c, sc);
		
		mStartTime = mCurMediaControl.getStartTime();
		
		mProgressBar.setMax((int) mCurMediaControl.getScheduleTotalTime());
		
		if (isFirstShowInCurrentPlay()){
		    mProgressBar.setText(utils.getTimeL(mStartTime + mCurMediaControl.getEnableDistance()));
		    mCurMediaControl.setProgress((int) mCurMediaControl.getEnableDistance() / 1000);
		}else{
		    mProgressBar.setText(utils.getTimeL(mStartTime + mCurMediaControl.getMediaCurrentTime() * 1000));
		    mCurMediaControl.setProgress((int) mCurMediaControl.getMediaCurrentTime());
		}
		
		mSeekUnit = mCurMediaControl.getSeekUnit();
		return true;
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		this.setVisibility(INVISIBLE);
		
		mHideTime = HIDE_TIME_NORMAL;
		this.setTag(null);
		
		if (mProgressBar != null){
			mCurMediaControl.setProgress(0);
		}
		
		if (mMediaPlayer != null){
			mMediaPlayer.unpause();
		}
	}
	
	private void showToast(String text){
		Toast t = Toast.makeText(getContext(),
			     "", Toast.LENGTH_LONG);
		TextView tv = new TextView(getContext());
	    ColorDrawable cd = new ColorDrawable(Color.BLACK);
	    cd.setAlpha(175);
	    tv.setBackgroundDrawable(cd);
	    
		tv.setText(text);
		tv.setTextSize(22f);
		t.setView(tv);
		t.show();
	}

	@Override
	public boolean isshow() {
		// TODO Auto-generated method stub
		return getVisibility() == VISIBLE;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
//		Logger.d("keydown repeat count = " + event.getRepeatCount());
		if (!isShown()){
			return super.onKeyDown(keyCode, event);
		}
		
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
			if (mHandler.hasMessages(MSG_HIDE_SELF)){
				mHandler.removeMessages(MSG_HIDE_SELF);
			}
			
			mHasKeyDown = true;
			int cur = mProgressBar.getProgress();
			
			if (cur == 0){
				if (event.getRepeatCount() == MAX_SWITCH_COUNT){
					mCurMediaControl.gotoPrevPlay();
					hide();
				}
				return super.onKeyDown(keyCode, event);
			}
			
			int seekto = cur - mSeekUnit;
			//Log.d("harish", "seek to = " + seekto + " mSu = " + mSeekUnit);
			
			if (seekto < 0){
				seekto = 0;
			}
			
			setProgress(seekto);
			
			if (mStateView.getDrawable() != mSeekBackwardDarwable){
				mStateView.setBackgroundDrawable(mSeekBackwardDarwable);
			}
		}
		
		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			if (mHandler.hasMessages(MSG_HIDE_SELF)){
				mHandler.removeMessages(MSG_HIDE_SELF);
			}
			
			mHasKeyDown = true;
			
			if (!mCurMediaControl.supportAfterward()){
				return true;
			}
			
			int cur = mProgressBar.getProgress();
			
			int seekto = cur + mSeekUnit;
			
			//Log.d("harish", "cur = " + cur + " seekTo = " + seekto);
			
			if (seekto > mProgressBar.getMax()){
				seekto = mProgressBar.getMax();
			}
			
			setProgress(seekto);
			
			if (mStateView.getDrawable() != mSeekForwardDrawable){
				mStateView.setBackgroundDrawable(mSeekForwardDrawable);
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		//Logger.d("key up repeat count = " + event.getRepeatCount());

		if (!isShown()){
			return super.onKeyDown(keyCode, event);
		}
		
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
		    if (!mHasKeyDown){
		    	return false;
		    }
		    
		    mHasKeyDown = false;
		    
		    if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && !mCurMediaControl.supportAfterward()){
		    	mHandler.sendEmptyMessageDelayed(MSG_HIDE_SELF, mHideTime);
		    	return true;
		    }
		    
			mStateView.setBackgroundDrawable(null);
			seekVideo();
			return true;
		}
		
		//main activity will switch channel, so hide-self here.
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			hide();
			return false;
		}
		
		return super.onKeyUp(keyCode, event);
	}
	
	private void seekVideo(){
		Logger.d("seek to = " + mProgressBar.getProgress());
		mCurMediaControl.startSeekVideo(mProgressBar.getProgress());
		mLoadingView.setVisibility(View.VISIBLE);
		mLoadingAnim.start();
	}
	
	private void setProgress(int progress){
		mProgressBar.invalidate();
		mCurMediaControl.setProgress(progress);
		mProgressBar.setText(utils.getTimeL(mStartTime + progress * 1000));
	}

	@Override
	public void notifyChange(String day) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setCurProgrammeViaIndex(int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPauseIconVisible(boolean visible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCurSubProgrammeViaIndex(int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFirstSelectSubProgramPos(int position) {
		// TODO Auto-generated method stub
		
	}
}
