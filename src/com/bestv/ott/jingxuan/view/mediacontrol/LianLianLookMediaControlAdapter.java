package com.bestv.ott.jingxuan.view.mediacontrol;

import android.view.ViewGroup;

import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.util.IOTV_Date;
import com.bestv.ott.jingxuan.util.utils;

public class LianLianLookMediaControlAdapter extends LiveTVMediaControlAdapter{
	boolean mIsFirst = false;
	long mEndTime;
	
	public LianLianLookMediaControlAdapter(int type) {
		super(type);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void initControls(ViewGroup contentView, IControllerBase controller,
			Channel channel, Schedule schedule) {
		// TODO Auto-generated method stub
		Schedule prev = mSchedule;
		super.initControls(contentView, controller, channel, schedule);
		
		if (!isScheduleTimeTheSame(prev, schedule)){
			reset();
		}
		
		mEndTime = utils.Time2long(schedule.getEndTime());
	}
	
	@Override
	public int getSeekUnit() {
		// TODO Auto-generated method stub
		int seekunit = (int) (getScheduleTotalTime() / 100);
		return seekunit;
	}
	
	@Override
	public boolean supportAfterward() {
		// TODO Auto-generated method stub
		long curTime = IOTV_Date.getDate().getTime();
		
		if (curTime > mEndTime){
			curTime = mEndTime;
		}
		
		return curTime > mSeekBar.getProgress();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		super.reset();
		mIsFirst = true;
	}
	
	@Override
	public void startSeekVideo(int second) {
		// TODO Auto-generated method stub
		misSeeking = true;
		mMediaPlayer.setSeekTimeEx(second);
	}
	
	@Override
	public long getEnableDistance(){
		long curTime = IOTV_Date.getDate().getTime();
		
		if (curTime > mEndTime){
			curTime = mEndTime;
		}
		
		return curTime - mStartTime;
	}
	
	@Override
	public int getMediaCurrentTime() {
		// TODO Auto-generated method stub
		return mMediaPlayer.getIOTVCurTime();
	}

	@Override
	public void notifyFirstPlay() {
		// TODO Auto-generated method stub
		int pos = (int) (getEnableDistance() / 1000);
		setProgress(pos);
		
		startSeekVideo(pos);
	}
}
