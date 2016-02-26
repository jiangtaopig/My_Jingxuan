package com.bestv.ott.jingxuan.view.mediacontrol;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bestv.jingxuan.R;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.util.utils;
import com.bestv.ott.jingxuan.view.MediaSeekProgress;
import com.bestv.ott.proxy.qos.IOTVMediaPlayer;

public class LookbackMediaControlAdapter implements IMediaControlAdapter {
	static final int MIN_SEEK_TIME = 30; //in seconds
	
	ViewGroup mContentView;
    Channel mChannel;
    Schedule mSchedule;
    IOTVMediaPlayer mMediaPlayer;
    IControllerBase mController;
    int mType;
    long mStartTime;
    
	boolean misSeeking = false;
	MediaSeekProgress mSeekBar;
    
    public LookbackMediaControlAdapter(int type){
		setControlType(type);
	}
    
	@Override
	public void setControlType(int type) {
		// TODO Auto-generated method stub
		mType = type;
	}

	@Override
	public void initControls(ViewGroup contentView, IControllerBase controller, Channel channel, Schedule schedule) {
		// TODO Auto-generated method stub
		mContentView = contentView;
		mController = controller;
		mMediaPlayer = mController.getIOTVMediaPlayer();
		mChannel = channel;
		mSchedule = schedule;
		
		TextView start_time = (TextView) mContentView.findViewById(R.id.tv_starttime);
		start_time.setText(utils.getTime(schedule.getStartTime()));
		
		TextView end_time = (TextView) mContentView.findViewById(R.id.tv_endtime);
		end_time.setText(utils.getTime(schedule.getEndTime()));
		
		TextView channel_name = (TextView) mContentView.findViewById(R.id.mc_channelname);
		channel_name.setText(channel.getChannelName());
		
		TextView schedule_name = (TextView) mContentView.findViewById(R.id.mc_schedulesname);
		schedule_name.setText(schedule.getCompleteName() == null ? schedule.getName():schedule.getCompleteName());
		
		mStartTime = utils.Time2long(schedule.getStartTime());
		
		misSeeking = false;
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
		mController.playPrevSchedule();
	}

	@Override
	public long getStartTime() {
		// TODO Auto-generated method stub
		return mStartTime;
	}

	@Override
	public long getScheduleTotalTime() {
		// TODO Auto-generated method stub
		long totaltime = (utils.Time2long(mSchedule.getEndTime()) - mStartTime) / 1000;
		return totaltime;
	}

	@Override
	public int getMediaCurrentTime() {
		// TODO Auto-generated method stub
		return mMediaPlayer.getIOTVCurTime();
	}

	@Override
	public int getMediaTotalTime() {
		// TODO Auto-generated method stub
		return mMediaPlayer.getIOTVTotalTime();
	}

	@Override
	public int getControlType() {
		// TODO Auto-generated method stub
		return mType;
	}

	@Override
	public void startSeekVideo(int second) {
		// TODO Auto-generated method stub
		misSeeking = true;
		mMediaPlayer.setSeekTimeEx(second);
	}

	@Override
	public void seekFinish() {
		// TODO Auto-generated method stub
		misSeeking = false;
	}

	@Override
	public boolean isSeeking() {
		// TODO Auto-generated method stub
		return misSeeking;
	}

	@Override
	public void setProgressBar(MediaSeekProgress pb) {
		// TODO Auto-generated method stub
		mSeekBar = pb;
	}

	@Override
	public void setProgress(int pos) {
		// TODO Auto-generated method stub
		mSeekBar.setMaxEnablePos(0);
		mSeekBar.setProgress(pos);
	}

	@Override
	public boolean supportAfterward() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
	}
	
	protected boolean isScheduleTimeTheSame(Schedule from, Schedule to){
		if (from == null || to == null){
			return false;
		}
		
		if (from.getStartTime().equals(to.getStartTime())){
			return true;
		}
		
		return false;
	}

	@Override
	public void notifyFirstPlay() {
		// TODO Auto-generated method stub	
	}

	@Override
	public long getEnableDistance() {
		// TODO Auto-generated method stub
		return getMediaCurrentTime() * 1000;
	}

	@Override
	public void refreshPlayingSchedule(Channel channel, Schedule schedule) {
		if(mChannel != channel || mSchedule != schedule){
			Log.d("LookbackMediaControlAdapter", "channel : " + (channel == null ? "null" :channel.getChannelCode()) + "Schedule : " + (schedule == null? "null" :schedule.getCompleteName() == null ? schedule.getName():schedule.getCompleteName()));
			mChannel = channel;
			mSchedule = schedule;
			
			TextView start_time = (TextView) mContentView.findViewById(R.id.tv_starttime);
			start_time.setText(utils.getTime(schedule.getStartTime()));
			
			TextView end_time = (TextView) mContentView.findViewById(R.id.tv_endtime);
			end_time.setText(utils.getTime(schedule.getEndTime()));
			
			TextView channel_name = (TextView) mContentView.findViewById(R.id.mc_channelname);
			channel_name.setText(channel.getChannelName());
			
			TextView schedule_name = (TextView) mContentView.findViewById(R.id.mc_schedulesname);
			schedule_name.setText(schedule.getCompleteName() == null ? schedule.getName():schedule.getCompleteName());
			
			mStartTime = utils.Time2long(schedule.getStartTime());
			
			misSeeking = false;
		}
	}
}
