package com.bestv.ott.jingxuan.data;

import java.io.Serializable;
import java.util.List;

import com.bestv.ott.jingxuan.util.utils;

public class Channel implements Serializable {
	private String ChannelCode;// 频道编号，值目前为：1~99之间的值
	private String ChannelName;// 频道名称
	private String ChannelNo; // 频道号
	private int ChannelType;// 频道类型：0 非高清；1 高清
	private int ChannelSource;// 频道来源：0 IPTV；1 OTT HLS；2 互联网
	private int TSSupport = 0; //是否支持时移,不支持=0，支持=1

	/*
	 * 电视台台标，全路径
	 * */
	private String Icon1;// 小图标1 
	private String Icon2;// 小图标2
	private String BKImage1;// 背景图片1
	private String BKImage2;// 背景图片2
	private String MulticastUrl;// 直播的组播地址
	private String UnicastUrl;// 直播的单播地址
	private String LiveUrl;
	private int TimeShiftDuration;// 时移时长，单位为小时，0表示不支持时移， 默认为0
	private String TvodUrl;// 回看地址
	private int LookbackFlag;// 是否可以回看：0 可以；1 不可以
	private String Desc;// 描述
	private List<Schedule> schedules;// 节目列表
	private String ChannelID;
	private int Type = 0;   //标明直播还是连看,直播=0，连看=1
	//private List<List<Schedule>> list_schedules; // 全部的节目列表
	private String day; //the day of this channel
	private List<Schedule> lookback_schedules;// 节目列表

	public Channel() {
	}

	public Channel(String name, String url) {
		this.ChannelName = name;
		this.LiveUrl = url;
	}

	public String getChannelNo() {
		return ChannelNo;
	}

	public void setChannelNo(String channelNo) {
		ChannelNo = channelNo;
	}

	public List<Schedule> getSchedules() {
		if (lookback_schedules != null){
			return lookback_schedules;
		}
		
		return schedules;
	}

	public void setSchedules(List<Schedule> schedules) {		
		this.schedules = schedules;
	}

	public String getChannelCode() {
		return ChannelCode;
	}

	public void setChannelCode(String channelCode) {
		ChannelCode = channelCode;
	}

	public String getChannelName() {
		return ChannelName;
	}

	public void setChannelName(String channelName) {
		ChannelName = channelName;
	}

	public int getChannelType() {
		return ChannelType;
	}

	public void setChannelType(int channelType) {
		ChannelType = channelType;
	}

	public int getChannelSource() {
		return ChannelSource;
	}

	public void setChannelSource(int channelSource) {
		ChannelSource = channelSource;
	}

	public String getIcon1() {
		return Icon1;
	}

	public void setIcon1(String icon1) {
		Icon1 = icon1;
	}

	public String getIcon2() {
		return Icon2;
	}

	public void setIcon2(String icon2) {
		Icon2 = icon2;
	}

	public String getBKImage1() {
		return BKImage1;
	}

	public void setBKImage1(String bKImage1) {
		BKImage1 = bKImage1;
	}

	public String getBKImage2() {
		return BKImage2;
	}

	public void setBKImage2(String bKImage2) {
		BKImage2 = bKImage2;
	}

	public String getMulticastUrl() {
		return MulticastUrl;
	}

	public void setMulticastUrl(String multicastUrl) {
		MulticastUrl = multicastUrl;
	}

	public String getUnicastUrl() {
		return UnicastUrl;
	}

	public void setUnicastUrl(String unicastUrl) {
		UnicastUrl = unicastUrl;
	}

	public int getTimeShiftDuration() {
		return TimeShiftDuration;
	}

	public void setTimeShiftDuration(int timeShiftDuration) {
		TimeShiftDuration = timeShiftDuration;
	}

	// 直播地址
	public String getLivePlayUrl() {
		return LiveUrl;
	}

	public void setLivePlayUrl(String iPTVPlayUrl) {
		LiveUrl = iPTVPlayUrl;
	}

	// 回看地址
	public String getLookbackUrl() {

		if (TvodUrl != null && !TvodUrl.equals(""))
			TvodUrl = utils.getUrl(TvodUrl);

		return TvodUrl;
	}

	public void setLookbackUrl(String lookbackUrl) {
		TvodUrl = lookbackUrl;
	}

	public int getLookbackFlag() {
		return LookbackFlag;
	}

	public void setLookbackFlag(int lookbackFlag) {
		LookbackFlag = lookbackFlag;
	}

	public String getDesc() {
		return Desc;
	}

	public void setDesc(String desc) {
		Desc = desc;
	}

	public String getChannelId() {
		return ChannelID;
	}

	public void setChannelId(String channelId) {
		ChannelID = channelId;
	}
	
	public int getType(){
		return Type;
	}
	
	public void setType(int type){
		Type = type;
	}

	public String getDay(){
		return day;
	}
	
	public void setDay(String d){
		day = d;
	}
	
	public void setLookbackSchedule(List<Schedule> l){
		lookback_schedules = l;
	}

	public int getTSSupport() {
		return TSSupport;
	}

	public void setTSSupport(int tSSupport) {
		TSSupport = tSSupport;
	}
	
	public void copy(Channel channel){
		channel.BKImage1 = BKImage1;
		channel.BKImage2 = BKImage2;
		channel.ChannelCode = ChannelCode;
		channel.ChannelID = ChannelID;
		channel.ChannelName = ChannelName;
		channel.ChannelNo = ChannelNo;
		channel.ChannelSource = ChannelSource;
		channel.ChannelType = ChannelType;
		channel.day = day;
		channel.Desc = Desc;
		channel.Icon1 = Icon1;
		channel.Icon2 = Icon2;
		channel.LiveUrl = LiveUrl;
		channel.LookbackFlag = LookbackFlag;
		channel.MulticastUrl = MulticastUrl;
		channel.schedules = schedules;
		channel.TimeShiftDuration = TimeShiftDuration;
		channel.TvodUrl = TvodUrl;
		channel.Type = Type;
		channel.UnicastUrl = UnicastUrl;
		channel.TSSupport = TSSupport;
	}
}
