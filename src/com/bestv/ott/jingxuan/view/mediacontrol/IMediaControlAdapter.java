package com.bestv.ott.jingxuan.view.mediacontrol;

import android.view.ViewGroup;

import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.livetv.controller.IControllerBase;
import com.bestv.ott.jingxuan.view.MediaSeekProgress;

public interface IMediaControlAdapter {
	static final int MEDIA_CONTROL_LOOKBACK = 1;
	static final int MEDIA_CONTROL_LIVETV = 2;
	static final int MEDIA_CONTROL_LIANLIANLOOK = 3;
	
	void setControlType(int type);
	int getControlType();
	
	void initControls(ViewGroup contentView, IControllerBase controller, Channel channel, Schedule schedule);
	int getSeekUnit();
	void gotoPrevPlay();
	
	long getStartTime();
	long getScheduleTotalTime();
	
	int getMediaCurrentTime();
	int getMediaTotalTime();
	
	void startSeekVideo(int second);
	void seekFinish();
	boolean isSeeking();
	
	void setProgressBar(MediaSeekProgress pb);
	void setProgress(int pos);
	
	boolean supportAfterward();
	
	void reset();
	void notifyFirstPlay();
	
	long getEnableDistance();
	
	void refreshPlayingSchedule(Channel channel, Schedule schedule);
}
