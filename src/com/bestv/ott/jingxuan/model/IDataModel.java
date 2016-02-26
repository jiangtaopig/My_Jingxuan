package com.bestv.ott.jingxuan.model;

import java.util.List;

import android.content.Context;
import android.os.Message;

import com.bestv.ott.framework.proxy.authen.AuthParam;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.Schedule;

public interface IDataModel {
	void setContext(Context context);
	/**
	 * start to sync channel data with EPG server
	 * 
	 * @param load_ok 
	 * the message will be invoked at epg data load finish.
	 * */
	void start(boolean forceRefresh);
	
	/**
	 * stop to sync channel data with EPG server
	 * */
	void stop();
	
	void getCDNToken();
	void getPlayUrl(AuthParam param);
	
	/**
	 * get all channels of the specified day in sync mode
	 * 
	 * @param day 
	 * the day in format: yyyyMMdd
	 * */
	List<Channel> getChannelsSync(String day, boolean forcerefresh);
	
	/**
	 * add a listener, it will be invoked at data change.
	 * */
	void addStateChangeListener(IStateChangeListener listener);
	
	/**
	 * async taking schedules of specify category, the data will be returned in obj that was attached with message.  
	 * */
	List<Schedule> getScheduleByCategory(String category, Message msg);
	
	/**
	 * 用于获取某频道下一条节目
	 * @param ChannelIndex “最新”分类中的频道index
	 * @return
	 */
	Schedule getNextScheduleOfChannel(int ChannelIndex);
	
	List<Schedule> getLatestSubscribeSchedules(List<Schedule> subscribeSchedules);
	void refreshData(String today, Message msg);
	List<Channel> getIotvChannelListByDay(String today);
}
