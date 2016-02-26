package com.bestv.ott.jingxuan.data;

import java.util.List;

public class JxAllScheduleItem {
	private String categorycode;

	private List<JxAllScheduleKey> keys;

	public void setCategorycode(String categorycode) {
		this.categorycode = categorycode;
	}

	public String getCategorycode() {
		return this.categorycode;
	}

	public void setKeys(List<JxAllScheduleKey> keys) {
		this.keys = keys;
	}

	public List<JxAllScheduleKey> getKeys() {
		return this.keys;
	}
}
