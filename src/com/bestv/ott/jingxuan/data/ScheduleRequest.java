package com.bestv.ott.jingxuan.data;

import com.bestv.ott.jingxuan.util.Constants;

/**
 * @author hu.fuyi
 * 
 */
public class ScheduleRequest extends BaseRequest {
	private String ChannelNo;
	private String StartDate;
	private String EndDate;
	
	private int Type = Constants.CHANNEL_IOTV; //live or lianlian-look

	public String getChannelCode() {
		return ChannelNo;
	}

	public void setChannelCode(String channelCode) {
		ChannelNo = channelCode;
	}

	public String getStartDate() {
		return StartDate;
	}

	public void setStartDate(String startDate) {
		StartDate = startDate;
	}

	public String getEndDate() {
		return EndDate;
	}

	public void setEndDate(String endDate) {
		EndDate = endDate;
	}
	
	public void setType(int t){
		Type = t;
	}
   
	public int getType(){
		return Type;
	}
}
