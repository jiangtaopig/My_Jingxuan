package com.bestv.ott.jingxuan.net;

import com.bestv.ott.jingxuan.util.utils;

public abstract class BaseResult {

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		utils.getClassString(this, this.getClass(), sb);
		return sb.toString();
	}
}
