package com.bestv.ott.jingxuan.view.mediacontrol;

import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;

import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.IOTV_Date;
import com.bestv.ott.proxy.qos.IOTVMediaPlayer.VideoPlayLogItemInfo;
import com.bestv.ott.proxy.qos.QosLogManager;

public class LiveTVMediaControlAdapter extends LookbackMediaControlAdapter {
	String mCurLiveUrl = "";
	String mCurPlayUrl = "";
	
	int mCurTimeShift = 0;
    int mTmpTimeShift = 0;
	
	public LiveTVMediaControlAdapter(int type){
		super(type);
	}
	
	@Override
	public void initControls(ViewGroup contentView, IControllerBase controller,
			Channel channel, Schedule schedule) {
		// TODO Auto-generated method stub		
		Schedule prev = mSchedule;
		
		super.initControls(contentView, controller, channel, schedule);
		
		if (!mCurLiveUrl.equals(schedule.getLiveUrl()) || !isScheduleTimeTheSame(prev, schedule)){
			mCurLiveUrl = schedule.getLiveUrl();
			mTmpTimeShift = 0;
			mCurTimeShift = 0;
			genPlayUrl();
		}
	}

	@Override
	public void setControlType(int type) {
		// TODO Auto-generated method stub
		mType = type;
	}

	@Override
	public int getSeekUnit() {
		// TODO Auto-generated method stub
		int seekunit = (int) (getScheduleTotalTime() / 100);
		return seekunit;
	}

	@Override
	public void gotoPrevPlay() {
		// TODO Auto-generated method stub
		super.gotoPrevPlay();
	}

	@Override
	public int getMediaCurrentTime() {
		// TODO Auto-generated method stub
		long currentTime = getEnableDistance() - mCurTimeShift;
		return (int) (currentTime / 1000);
	}

	@Override
	public void setProgress(int pos) {
		// TODO Auto-generated method stub
		long maxEnable = getEnableDistance();
		mSeekBar.setMaxEnablePos((int) maxEnable / 1000);
		
		mSeekBar.setProgress(pos);
	}
	
	@Override
	public int getMediaTotalTime() {
		// TODO Auto-generated method stub
		return (int) getScheduleTotalTime();
	}

	@Override
	public void startSeekVideo(int second) {
		// TODO Auto-generated method stub
		misSeeking = true;
		
		long maxEnable = getEnableDistance();
		
		mTmpTimeShift = (int) ((maxEnable / 1000 - second) / 10);
		
		//at least 30s time shift
		if (mTmpTimeShift <= 3){
			mTmpTimeShift = 0;
		}
		
		genPlayUrl();
	
		startPlay(mCurPlayUrl);
	}

	@Override
	public void seekFinish() {
		// TODO Auto-generated method stub
		misSeeking = false;
		
		mCurTimeShift = mTmpTimeShift * 10 * 1000;
	}

	@Override
	public boolean isSeeking() {
		// TODO Auto-generated method stub
		return misSeeking;
	}
	
	private void startPlay(String playUrl){
		mMediaPlayer.stop();
		mMediaPlayer.close();
		
		VideoPlayLogItemInfo iteminfo = new VideoPlayLogItemInfo();
		iteminfo.channelCode = mChannel.getChannelCode();
		iteminfo.playType = QosLogManager.TYPE_JINGXUAN;
		iteminfo.playUrl = playUrl;
		
		if (mSchedule != null){
			iteminfo.itemCode = mSchedule.getCode();
			iteminfo.videoClipCode = mSchedule.getClipCode();
		}
		
		String taskID = Uri.parse(playUrl).getQueryParameter("taskID");
		if (taskID != null){
			iteminfo.taskID = taskID;
		}
	    
		boolean isLive = false;
		if(mController != null && mController.getCurrentIOTVType() == Constants.IOTV_PLAY_LIVE_SOURCE){
			isLive = true;
		}
		mMediaPlayer.setPlayUrl(playUrl, isLive);
//		mMediaPlayer.setPlayUrl(playUrl);
		mMediaPlayer.play(iteminfo);
	}

	@Override
	public boolean supportAfterward() {
		// TODO Auto-generated method stub
		String timeshift = Uri.parse(mCurPlayUrl).getQueryParameter("d");

		boolean unsuppoort = timeshift.equals("0") || timeshift.equals("1");
		return !unsuppoort;
	}
	
	protected String genPlayUrl(){
		Uri.Builder builder = Uri.parse(mCurLiveUrl).buildUpon();
		builder.appendQueryParameter("d", "" + mTmpTimeShift);
		
		mCurPlayUrl = builder.build().toString();
		Log.d("harish", "play url = " + mCurPlayUrl);
		return mCurPlayUrl;
	}
	
	@Override
	public long getEnableDistance(){
		return IOTV_Date.getDate().getTime() - mStartTime;
	}
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		mCurLiveUrl = "";
	}
}
