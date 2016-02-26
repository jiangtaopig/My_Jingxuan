package com.bestv.ott.jingxuan.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;

import com.bestv.ott.framework.utils.utils;
import com.bestv.ott.jingxuan.data.Channel;
import com.bestv.ott.jingxuan.data.Schedule;
import com.bestv.ott.jingxuan.util.IOTV_Date;

public class ChannelManager {
	private static final String TAG = "ChannelManager";

	public static ChannelManager mChannelManager = null;

	public static ChannelManager getInstance() {
		if (mChannelManager == null) {
			mChannelManager = new ChannelManager();
		}
		return mChannelManager;
	}

	private ChannelManager() {

	}

	/**
	 * 获取指定时间，在对应频道内的节目
	 * */
	public ChannelData getSchedule(Channel channel, String time) {
		long l_time = 0;

		if (time == null || time.equals("")) {
			l_time = IOTV_Date.getDate().getTime();
		} else {
			l_time = convertTime(time);
		}

		return getSchedule(channel, l_time);
	}

	/**
	 * 获取指定时间，在对应频道内的节目
	 * */
	public ChannelData getSchedule(Channel channel, long curTime) {
		Log.d(TAG, "enter getScheduleL");

		ChannelData channelData = new ChannelData();
		if (channel != null) {
			// 取当前时间
			channelData.setCurTime(curTime);
			// 取台标本地路径
			channelData.setChannelIconPath(utils.safeString(channel.getIcon1()));
			// 取当前节目信息
			List<Schedule> schedules = channel.getSchedules();
			if (schedules != null && schedules.size() > 0) {

				Schedule schedule = binarySearchSchedule(schedules, curTime);
				if (schedule != null) {
					String endStr = schedule.getEndTime();
					String startStr = schedule.getStartTime();
					long endTime = convertTime(endStr);
					long startTime = convertTime(startStr);
					channelData.setScheduleName(utils.safeString(schedule
							.getName()));
					channelData.setStartTime(startTime);
					channelData.setStartTimeStr(converTimeStr(startStr));
					channelData.setEndTime(endTime);
					channelData.setEndTimeStr(converTimeStr(endStr));
					channelData.setSchedule(schedule);
					channelData.setChannelName(channel.getChannelName());
				}

				// for (Schedule schedule : schedules) {
				// String endStr = schedule.getEndTime();
				// String startStr = schedule.getStartTime();
				// long endTime = convertTime(endStr);
				// long startTime = convertTime(startStr);
				// if (endTime > curTime && startTime <= curTime) {
				// channelData.setScheduleName(utils.safeString(schedule
				// .getName()));
				// channelData.setStartTime(startTime);
				// channelData.setStartTimeStr(converTimeStr(startStr));
				// channelData.setEndTime(endTime);
				// channelData.setEndTimeStr(converTimeStr(endStr));
				// channelData.setSchedule(schedule);
				// break;
				// }
				// }
			} else {
				Log.w(TAG, "schedules is null");
			}
		} else {
			Log.w(TAG, "channel is null");
		}

		return channelData;
	}

	public Schedule binarySearchSchedule(List<Schedule> schedules, long curTime) {
		if (schedules == null || schedules.size() == 0){
			return null;
		}
		
		int low = 0;
		int high = schedules.size() - 1;
		while (low <= high) {
			int mid = (low + high) / 2;
			Schedule schedule = schedules.get(mid);
			String endStr = schedule.getEndTime();
			String startStr = schedule.getStartTime();
			long endTime = convertTime(endStr);
			long startTime = convertTime(startStr);
			if (endTime > curTime && startTime <= curTime) {
				return schedule;
			} else if (startTime > curTime) {
				high = mid - 1;
			} else {
				low = mid + 1;
			}
		}
		return null;
	}

	public ChannelData getCurrentSchedule(Channel channel) {
		Log.d(TAG, "enter getCurrentSchedule");
		return getSchedule(channel, null);
	}
	
	public List<Channel> cloneChannelList(List<Channel> list){
		List<Channel> newlist = new ArrayList<Channel>();
		
		for (Channel c : list){
			Channel newc = new Channel();
			c.copy(newc);
			newlist.add(newc);
		}
		
		return newlist;
	}

	/**
	 * 获取指定时间，在对应频道内的下一节目
	 * */
	public ChannelData getNextSchedule(Channel channel, String time) {
		long l_time = 0;

		if (time == null || time.equals("")) {
			l_time = IOTV_Date.getDate().getTime();
		} else {
			l_time = convertTime(time);
		}

		return getNextSchedule(channel, l_time);
	}

	/**
	 * 获取指定时间，在对应频道内的下一节目
	 * */
	public ChannelData getNextSchedule(Channel channel, long curTime) {
		ChannelData channelData = new ChannelData();
		if (channel != null) {
			// 取当前时间
			channelData.setCurTime(curTime);
			// 取台标本地路径
			channelData
					.setChannelIconPath(utils.safeString(channel.getIcon1()));
			// 取当前节目信息
			List<Schedule> schedules = channel.getSchedules();
			if (schedules != null) {
				// 是否是下一个节目
				boolean isNext = false;
				for (Schedule schedule : schedules) {
					String endStr = schedule.getEndTime();
					String startStr = schedule.getStartTime();
					long endTime = convertTime(endStr);
					long startTime = convertTime(startStr);
					if (isNext) {
						channelData.setScheduleName(utils.safeString(schedule
								.getName()));
						channelData.setStartTime(startTime);
						channelData.setStartTimeStr(converTimeStr(startStr));
						channelData.setEndTime(endTime);
						channelData.setEndTimeStr(converTimeStr(endStr));
						channelData.setSchedule(schedule);
						channelData.setChannelName(channel.getChannelName());
						break;
					}
					if (endTime > curTime && startTime <= curTime) {
						isNext = true;
					}

				}
			} else {
				Log.w(TAG, "schedules is null");
			}
		} else {
			Log.w(TAG, "channel is null");
		}

		return channelData;
	}
	
	public static Channel getChannelByCode(List<Channel> list, String channelCode){
		if (list != null && list.size() > 0) {
			for (Channel c : list) {
				if (c.getChannelCode().equals(channelCode)) {
					return c;
				}
			}
		}
		
		return new Channel();
	}

	public ChannelData getNextSchedule(Channel channel) {
		Log.d(TAG, "enter getNextSchedule");
		return getNextSchedule(channel, null);
	}

	/**
	 * 转换日期到long
	 * 
	 * @param strTime
	 * @return
	 */
	private long convertTime(String strTime) {
		long time = 0L;
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					"yyyyMMddHHmmss");
			Date date = simpleDateFormat.parse(strTime);
			time = date.getTime();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return time;
	}

	/**
	 * yyyyMMddHHmmss转化为HH:mm
	 * 
	 * @param strTime
	 * @return
	 */
	private String converTimeStr(String time) {
		String str = "";
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			SimpleDateFormat strFormat = new SimpleDateFormat("HH:mm");
			Date date = dateFormat.parse(time);
			str = strFormat.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}
}
