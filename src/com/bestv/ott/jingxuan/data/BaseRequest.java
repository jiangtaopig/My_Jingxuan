package com.bestv.ott.jingxuan.data;

import com.bestv.ott.jingxuan.util.utils;

/**
 * @author hu.fuyi
 * 
 */
public class BaseRequest {
	private String userID;
	private String tvID;
	private String tvProfile;
	private String userGroup;
	private int pageIndex;
	private int pageSize;
	
	private Object mExtraData;

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getUserID() {
		if (userID == null || userID.equals(""))
			return "1";
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getTvID() {
		return tvID;
	}

	public void setTvID(String tvID) {
		this.tvID = tvID;
	}

	public String getTvProfile() {
		return tvProfile;
	}

	public void setTvProfile(String tvProfile) {
		this.tvProfile = tvProfile;
	}

	public String getUserGroup() {
		if (userGroup == null || userGroup.equals(""))
			return "1";
		return userGroup;
	}

	public void setUserGroup(String userToken) {
		this.userGroup = userToken;
	}
	
	public void setExtraData(Object data){
		mExtraData = data;
	}
	
	public Object getExtraData(){
		return mExtraData;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		utils.getClassString(this, this.getClass(), sb);
		return sb.toString();
	}
}
