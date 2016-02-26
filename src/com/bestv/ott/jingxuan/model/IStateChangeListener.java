package com.bestv.ott.jingxuan.model;

import java.util.List;

import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.net.IOTVNetException;

public interface IStateChangeListener {
	/**
	 * will be invoked with the latest updated channels which have null schedules. 
	 * */
	void channelListUpdate(List<Channel> clist);
	/**
	 * will be invoked after updated the schedules for a specified day.
	 * */
	void sheduleListUpdate(String day, List<Channel> clist);
	void loadFinish(Object extraData);
	void reportError(int type, IOTVNetException exception);
	
	void playAuthenFinish(Object data);
	void iotvDataLoadFinish(Object extraData);
}
