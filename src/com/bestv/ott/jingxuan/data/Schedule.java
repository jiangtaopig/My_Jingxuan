package com.bestv.ott.jingxuan.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import android.util.Log;

import com.bestv.ott.jingxuan.util.ChannelPlayUrlUtils;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.IOTV_Date;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.utils;

public class Schedule implements Serializable, Cloneable {
	private final static String TAG = "Schedule";
	private static final long serialVersionUID = 1L;
	public static int SUBSCRIBE_STATUS_UPDATE = 1;
	public static int SUBSCRIBE_STATUS_NORMAL = 2;
	public static int SUBSCRIBE_STATUS_INVALID = 3;
	private int orderIndex; // 节目编号
	private String Code;// 节目单编号
	private String OriginalChannelCode; // 原始频道编号，从服务器拿到的原始数据。
	private String ChannelCode;// 频道编号
	private String Name;// 节目名称
	private String StartTime;// 节目播出开始时间（格式YYYYMMDDHH24MISS）
	private String EndTime;// 节目播出结束时间（格式YYYYMMDDHH24MISS）
	private String ScheduleLookbackUrl;// 回看地址（组装好每个节目的回看地址）
	private String Desc;// 描述
	private String CompleteName;// 完整名称，用于显示剧集名字+集数
	// 以下字段只在连连看是有效
	private String ClipCode;
	private String ParentCode;
	private String URI;
	private String Lenght;

	private String LiveUrl;

	private String ChannelIcon;

	private int schedule_index;
	private List<Schedule> subScheduleList;
	private String SubscribeTime;
	
	private String NamePostfix = "";

	private int SubscribeStatus = SUBSCRIBE_STATUS_NORMAL;
	
	private String lastVisitedTime="";//add shensong 此节目上一次获得焦点或者点击的时间
	private boolean hasWatched = false;//是否已经观看，用于订阅节目子集的订阅状态显示。

	
	private String ChannelID;
	
	public Object clone() {
		try {
			Schedule cloned = (Schedule)super.clone();  
			if(cloned.Code != null) 
				cloned.Code = new String(Code);  
			if(cloned.OriginalChannelCode != null) 
				cloned.OriginalChannelCode = new String(OriginalChannelCode);  
			if(cloned.ChannelCode != null) 
				cloned.ChannelCode = new String(ChannelCode);  
			if(cloned.Name != null) 
				cloned.Name = new String(Name);
			if(cloned.StartTime != null) 
				cloned.StartTime = new String(StartTime);  
			if(cloned.EndTime != null) 
				cloned.EndTime = new String(EndTime);  
			if(cloned.ScheduleLookbackUrl != null) 
				cloned.ScheduleLookbackUrl = new String(ScheduleLookbackUrl);  
			if(cloned.Desc != null) 
				cloned.Desc = new String(Desc);  
			if(cloned.CompleteName != null) 
				cloned.CompleteName = new String(CompleteName);  
			if(cloned.ClipCode != null) 
				cloned.ClipCode = new String(ClipCode);  
			if(cloned.ParentCode != null) 
				cloned.ParentCode = new String(ParentCode); 
			if(cloned.URI != null) 
				cloned.URI = new String(URI);  
			if(cloned.Lenght != null) 
				cloned.Lenght = new String(Lenght);  
			if(cloned.LiveUrl != null) 				
				cloned.LiveUrl = new String(LiveUrl);  
			if(cloned.ChannelIcon != null) 
				cloned.ChannelIcon = new String(ChannelIcon);  
			if(cloned.SubscribeTime != null) 
				cloned.SubscribeTime = new String(SubscribeTime); 
			if(cloned.NamePostfix != null) 
				cloned.NamePostfix = new String(NamePostfix); 
			if(cloned.subScheduleList != null) 
				cloned.subScheduleList = utils.deepCloneScheduleList(subScheduleList);
		    return cloned;  
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public String getSubscribeTime() {
		return SubscribeTime;
	}

	public void setSubscribeTime(String subscribeTime) {
		SubscribeTime = subscribeTime;
	}

	public int getSubscribeStatus() {
		return SubscribeStatus;
	}

	public void setSubscribeStatus(int subscribeStatus) {
		SubscribeStatus = subscribeStatus;
	}

	public String getCompleteName() {
		return CompleteName;
	}

	public void setCompleteName(String completeName) {
		CompleteName = completeName;
	}

	public List<Schedule> getSubScheduleList() {
		return subScheduleList;
	}

	public void setSubScheduleList(List<Schedule> subScheduleList) {
		this.subScheduleList = subScheduleList;
	}

	public String getParentCode() {
		return ParentCode;
	}

	public String getClipCode() {
		return ClipCode;
	}

	public String getURI() {
		return URI;
	}

	public String getLength() {
		return Lenght;
	}

	public int getOrderIndex() {
		return orderIndex;
	}

	public void setOrderIndex(int orderIndex) {
		this.orderIndex = orderIndex;
	}

	public String getCode() {
		return Code;
	}

	public void setCode(String code) {
		Code = code;
	}

	public String getOriginalChannelCode() {
		return OriginalChannelCode;
	}

	public void setOriginalChannelCode(String originalChannelCode) {
		OriginalChannelCode = originalChannelCode;
	}

	public String getChannelCode() {
		return ChannelCode;
	}

	public void setChannelCode(String channelCode) {
		ChannelCode = channelCode;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getStartTime() {
		return StartTime;
	}

	public void setStartTime(String startTime) {
		StartTime = startTime;
	}

	public String getEndTime() {
		return EndTime;
	}

	public void setEndTime(String endTime) {
		EndTime = endTime;
	}

	// 在lookback url的基础上追加参数
	public String getScheduleLookbackUrl() {
		//Log.i(TAG, "getScheduleLookbackUrl, ScheduleLookbackUrl="+ScheduleLookbackUrl);
		//Log.i(TAG, "getScheduleLookbackUrl, ChannelID="+ChannelID+", url in map = "+ChannelPlayUrlUtils.getChannelPlayUrl(ChannelID));
		return ScheduleLookbackUrl;
		//return ChannelPlayUrlUtils.getChannelPlayUrl(ChannelID);
	}

	public void setScheduleLookbackUrl(String scheduleLookbackUrl) {
		ScheduleLookbackUrl = scheduleLookbackUrl;
	}

	public String getDesc() {
		return Desc;
	}

	public void setDesc(String desc) {
		Desc = desc;
	}

	public String getTitle() {
		return getName();
	}

	public String getLiveUrl() {
		return LiveUrl;
	}

	public void setLiveUrl(String liveUrl) {
		LiveUrl = liveUrl;
	}

	public void setChannelIcon(String icon) {
		ChannelIcon = icon;
	}

	public String getChannelIcon() {
		return ChannelIcon;
	}

	public void setScheduleIndex(int id) {
		schedule_index = id;
	}

	public int getScheduleIndex() {
		return schedule_index;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Schedule))
			return false;
		try {
			final Schedule other = (Schedule) o;
			if (this.getChannelCode().equals(other.getChannelCode())
					&& this.getName().equals(other.getName())
					&& this.getStartTime().equals(other.getStartTime())
					&& this.getEndTime().equals(other.getEndTime())
					&& this.getScheduleLookbackUrl().equals(
							other.getScheduleLookbackUrl())) {
				return true;
			} else if (this.getChannelCode().equalsIgnoreCase(
					Constants.JX_SUBSCRP_CHANNEL_CODE)
					|| other.getChannelCode().equalsIgnoreCase(
							Constants.JX_SUBSCRP_CHANNEL_CODE)) {
				return (this.getChannelCode().equals(
						other.getOriginalChannelCode()) || other
						.getChannelCode().equals(this.getOriginalChannelCode()))
						&& this.getName().equals(other.getName())
						&& this.getStartTime().equals(other.getStartTime())
						&& this.getEndTime().equals(other.getEndTime());
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	
	public boolean equalsForSubscribe(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Schedule))
			return false;
		try {
			final Schedule other = (Schedule) o;
			if (/*this.getChannelCode().equals(other.getChannelCode())   //只对Schedule name 做对比。ding.jian，2015/6/29
					&& */this.getName().equals(other.getName())) {
				return true;
			} /*else if (this.getChannelCode().equalsIgnoreCase(		//只对Schedule name 做对比。ding.jian，2015/6/29
					Constants.JX_SUBSCRP_CHANNEL_CODE)
					|| other.getChannelCode().equalsIgnoreCase(
							Constants.JX_SUBSCRP_CHANNEL_CODE)) {
				return (this.getChannelCode().equals(
						other.getOriginalChannelCode()) || other
						.getChannelCode().equals(this.getOriginalChannelCode()))
						&& this.getName().equals(other.getName());
			}*/ else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	public String getNamePostfix() {
		return NamePostfix;
	}

	public void setNamePostfix(String namePostfix) {
		NamePostfix = namePostfix;
	}

	public String getLastVisitedTime() {
		return lastVisitedTime;
	}

	public void setLastVisitedTime(String lastVisitedTime) {
		this.lastVisitedTime = lastVisitedTime;
	}
	
	public boolean isHasWatched() {
		return hasWatched;
	}

	public void setHasWatched(boolean hasWatched) {
		this.hasWatched = hasWatched;
	}

	//节目订阅后的初始化
	public void subProgramInit(){
		
		setChannelCode(JXSubscription.CHANNECODE);    //set all channel code to Subscription.for View focus
		String timeNow = IOTV_Date.getDateFormatFull(IOTV_Date.getDate());
		setSubscribeTime(timeNow);
		
//		List<Schedule> playableSchedules=utils.filterPlayableSchedules(subScheduleList);
		
		if(null!=subScheduleList){
			
			String maxStartTime=subScheduleList.get(0).getStartTime();//所有子节目的最晚开始时间
			 
			for(Schedule sch:subScheduleList){
				//单个剧集的更新状态
				//初始化为开始时间
				sch.setLastVisitedTime(sch.getStartTime());
				sch.setSubscribeStatus(Schedule.SUBSCRIBE_STATUS_NORMAL);
				if(maxStartTime.compareTo(sch.getStartTime())<0) maxStartTime=sch.getStartTime();
			}						
						
			lastVisitedTime=maxStartTime;
			
			StartTime=maxStartTime;
			
			SubscribeStatus=Schedule.SUBSCRIBE_STATUS_NORMAL;
		}
	}
	
	//更新订阅节目的访问时间
	private void updateVisitedTime(){
		Date date=IOTV_Date.getDate();
		lastVisitedTime=IOTV_Date.getDateFormatFull(date);
	}
	
	private void updateStatus(){
		if(lastVisitedTime.compareTo(StartTime)>0){
			SubscribeStatus=Schedule.SUBSCRIBE_STATUS_NORMAL;
		}else{
			SubscribeStatus=Schedule.SUBSCRIBE_STATUS_UPDATE;
		}
	}
	
	public void update(){
		Logger.d(TAG, " enter update");
		updateVisitedTime();
		//updateStatus();
	}
	
	public String getChannelId() {
		return ChannelID;
	}

	public void setChannelId(String channelId) {
		//Log.i(TAG, "setChannelId, channelId="+channelId);
		ChannelID = channelId;
	}
}
