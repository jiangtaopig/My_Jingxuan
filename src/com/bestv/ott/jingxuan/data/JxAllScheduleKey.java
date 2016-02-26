package com.bestv.ott.jingxuan.data;

import java.util.List;

public class JxAllScheduleKey {
	private String keyname;

	private List<Schedule> schedules;

	public void setKeyname(String keyname) {
		this.keyname = keyname;
	}

	public String getKeyname() {
		return this.keyname;
	}

	public void setSchedules(List<Schedule> schedules) {
		this.schedules = schedules;
	}

	public List<Schedule> getSchedules() {
		return this.schedules;
	}
}
