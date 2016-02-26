package com.bestv.ott.jingxuan.livetv.controller;

import java.util.List;

import android.app.Activity;
import android.os.Message;
import android.view.View;

import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.RecommendItem;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.model.IDataModel;
import com.bestv.ott.proxy.qos.IOTVMediaPlayer;
public interface IControllerBase {
	/**
	 * 设置应用上下文
	 * */
	void setOwner(Activity owner);
	
	/**
	 * 获取root view
	 * */
	View getRootView();
	
	/**
	 * 获取今日当前播放频道和节目数据
	 * */
	Channel getCurrentChannel();
	
	int getCurrentChannelIndex();
	
	/**
	 * 获取当前播放的节目
	 * */
	Schedule getPlayingSchedule();
	
	/**
	 * 设置当前用户选择的日期和对应频道数据
	 */
	void setCurrentChannel(String day, List<Channel> list);
	
	/**
	 * 获取当前播放类型，直播或者回看
	 * @return
	 * {@link com.bestv.ott.livetv.util.Constans.IOTV_TYPE_LIVE, com.bestv.ott.livetv.util.Constans.IOTV_TYPE_LOOKBACK}
	 * */
	int getCurrentIOTVType();
	
	/**
	 * 获取当前播放时间，如果是直播，返回当前系统时间，如果是回看，返回当前用户观看节目的播放时间
	 * */
	long getCurrentPlayingTime();
	
	/**
	 * 获取指定日志的频道列表,日志格式 yyyyMMdd
	 * */
	List<Channel> getChannelList(String day);
	/**
	 * 获取今天的频道列表，当前用户观看频道时间会随着用户选择而改变(比如用户选择昨天的回看)，其他频道都为今天
	 * */
	List<Channel> getCurrentChannelList();
	
	/**
	 * play special channel, if a null schedule is passed in, the live TV in this channel will be played. 
	 * */
	void iotvPlay(Channel channel, Schedule schedule);
	

	void iotvPlay(Channel channel, Schedule schedule, int subScheduleIndex);
	/**
	 * 获取播放记录
	 * */
	List<Channel> getPlayRecords();
	/**
	 * 跳转到播放菜单列表界面
	 * */
	void gotoChannelList();
	/**
	 * 退出程序
	 * */
	void exitApp();
	
	/**
	 * 获取当前的播放对象
	 * */
	IOTVMediaPlayer getIOTVMediaPlayer();
	
	/**
	 * 播放当前频道的上一个节目
	 * */
	void playPrevSchedule();
	/**
	 * 播放当前频道的下一个节目
	 * */
	void playNextSchedule();
	
	/**
	 * get the all categories of JingXuan
	 * */
	List<Channel> getJXCategory();
	
	/**
	 * 如果存在，返回节目数据，如果不存在，返回null，并会异步发起请求，通过response message将数据异步返回
	 * @param
	 * Channel c 要请求的channel
	 * Message response 回调消息, msg.obj保存List<Schedule>对象
	 * */
	List<Schedule> getJXSchedule(Channel c, Message response);
	
	/**
	 * 请求指定频道的随机数量的Schedule，如果存在，返回节目数据，如果不存在，返回null，并会异步发起请求，通过response message将数据异步返回
	 * @param
	 * String channelCode 要请求的channelCode
	 * int size 需要的Schedule数量
	 * Message response 回调消息, msg.obj保存List<Schedule>对象
	 * */
	List<RecommendItem> getRandomSchedules(Channel channel, int size, Message response);
	
	Schedule getNextSchedule();

	Schedule getPlayingSubSchedule();
	
	List<Schedule> getLatestSubscribeSchedules(List<Schedule> subscribeSchedules);

	Activity getOwner();
	
	void resetStopLiveDelayTime(long time);
	
	void resetMediaControlView();
	
	/*add by xubin 2015.11.24 begin*/
	IDataModel getIDataModel();
	void removeShowNextTostMsg();
	/*add by xubin 2015.11.24 end*/
}
