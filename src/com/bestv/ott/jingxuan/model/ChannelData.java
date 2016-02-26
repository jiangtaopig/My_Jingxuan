package com.bestv.ott.jingxuan.model;

import com.bestv.ott.jingxuan.data.Schedule;

public class ChannelData {
	private long curTime;
	private long startTime;
	private long endTime;
	private String channelIconPath;
	private String scheduleName;
	private String startTimeStr;
	private String endTimeStr;
	private Schedule schedule;
	private String channelName;
	
	public void setChannelName(String name){
		channelName = name;
	}
	
	public String getChannelName(){
		return channelName;
	}


	/**
	 * 当前时间
	 * 
	 * @return
	 */
	public long getCurTime() {
		return curTime;
	}

	/**
	 * 当前时间
	 * 
	 * @param curTime
	 */
	public void setCurTime(long curTime) {
		this.curTime = curTime;
	}

	/**
	 * 节目开始时间
	 * 
	 * @return
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * 节目开始时间
	 * 
	 * @param startTime
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * 节目结束时间
	 * 
	 * @return
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * 节目结束时间
	 * 
	 * @param endTime
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	/**
	 * 频道Icon路径
	 * 
	 * @return
	 */
	public String getChannelIconPath() {
		return channelIconPath;
	}

	/**
	 * 频道Icon路径
	 * 
	 * @param channelIconPath
	 */
	public void setChannelIconPath(String channelIconPath) {
		this.channelIconPath = channelIconPath;
	}

	/**
	 * 当前节目名字
	 * 
	 * @return
	 */
	public String getScheduleName() {
		return scheduleName;
	}

	/**
	 * 当前节目名字
	 * 
	 * @param scheduleName
	 */
	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	/**
	 * 当前节目开始时间HH:mm
	 * 
	 * @return
	 */
	public String getStartTimeStr() {
		return startTimeStr;
	}

	/**
	 * 当前节目开始时间HH:mm
	 * 
	 * @param startTimeStr
	 */
	public void setStartTimeStr(String startTimeStr) {
		this.startTimeStr = startTimeStr;
	}

	/**
	 * 当前节目结束时间HH:mm
	 * 
	 * @return
	 */
	public String getEndTimeStr() {
		return endTimeStr;
	}

	/**
	 * 当前节目结束时间HH:mm
	 * 
	 * @param endTimeStr
	 */
	public void setEndTimeStr(String endTimeStr) {
		this.endTimeStr = endTimeStr;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	@Override
	public String toString() {
		return "ChannelData [curTime=" + curTime + ", startTime=" + startTime
				+ ", endTime=" + endTime + ", channelIconPath="
				+ channelIconPath + ", scheduleName=" + scheduleName
				+ ", startTimeStr=" + startTimeStr + ", endTimeStr="
				+ endTimeStr + ", schedule=" + schedule + "]";
	}

}
