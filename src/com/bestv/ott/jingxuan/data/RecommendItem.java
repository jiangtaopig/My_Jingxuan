package com.bestv.ott.jingxuan.data;

public class RecommendItem {
	private Channel mChannel;
	private Schedule mSchedule;
	
	public RecommendItem(Channel channel, Schedule schedule){
		this.mChannel = channel;
		this.mSchedule = schedule;
	}
	public Channel getChannel() {
		return mChannel;
	}

	public void setChannel(Channel mChannel) {
		this.mChannel = mChannel;
	}

	public Schedule getSchedule() {
		return mSchedule;
	}

	public void setSchedule(Schedule mSchedule) {
		this.mSchedule = mSchedule;
	}
}
