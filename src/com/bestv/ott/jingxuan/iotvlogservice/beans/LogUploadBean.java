package com.bestv.ott.jingxuan.iotvlogservice.beans;

import com.bestv.ott.jingxuan.iotvlogservice.logmanager.LogUploadMgr;
import com.bestv.ott.jingxuan.iotvlogservice.utils.CommonTool;
import com.bestv.ott.jingxuan.iotvlogservice.utils.utils;

/**
 * 日志上报对象
 */
public class LogUploadBean {

	private String version;

	private int type;

	private String firmwareVersion;

	private String currentTime;

	private String tvID;

	private String userID;

	private String[] params;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	public String getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}

	public String getTvID() {
		return tvID;
	}

	public void setTvID(String tvID) {
		this.tvID = tvID;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String[] getParams() {
		return params;
	}

	public void setParams(String[] params) {
		this.params = params;
	}

	public LogUploadBean() {
	}

	/**
	 * New LogUploadBean
	 * 
	 * @param version
	 *            如果为null，则取LogUploadService.VERSION
	 * @param type
	 *            日至类型
	 * @param firmwareVersion
	 *            如果为null，则取BSPSystem.getInstance().getFirmwareVersion()
	 * @param currentTime
	 *            如果为null，则取CommonTool.yearEndSecond()
	 * @param userID
	 *            如果为null，则取SysCache.getInstance().getTerminal().getUserID()
	 * @param params
	 *            其它非共性参数
	 */
	public LogUploadBean(String version, int type, String firmwareVersion, String currentTime, String userID,
			String[] params) {
		super();
		this.version = (version == null ? LogUploadMgr.VERSION : version);
		this.type = type;
		this.firmwareVersion = (firmwareVersion == null ? LogUploadMgr.getFirmwareVersion()
				: firmwareVersion);
		this.currentTime = (currentTime == null ? CommonTool.yearEndSecond() : currentTime);
		this.userID = (userID == null ? LogUploadMgr.userprofile.getUserID() : userID);
		this.params = params;
	}

	/**
	 * New LogUploadBean
	 * 
	 * @param version
	 *            如果为null，则取LogUploadService.VERSION
	 * @param type
	 *            日至类型
	 * @param firmwareVersion
	 *            如果为null，则取BSPSystem.getInstance().getFirmwareVersion()
	 * @param currentTime
	 *            如果为null，则取CommonTool.yearEndSecond()
	 * @param tvID
	 *            如果为null，则取OttContext.getTVID()
	 * @param userID
	 *            如果为null，则取SysCache.getInstance().getTerminal().getUserID()
	 * @param params
	 *            其它非共性参数
	 */
	public LogUploadBean(String version, int type, String firmwareVersion, String currentTime, String tvID,
			String userID, String[] params) {
		super();
		this.version = (version == null ? utils.LOG_UPLOAD_VERSION : version);
		this.type = type;
		this.firmwareVersion = (firmwareVersion == null ?  LogUploadMgr.getFirmwareVersion()
				: firmwareVersion);
		this.currentTime = (currentTime == null ? CommonTool.yearEndSecond() : currentTime);
		this.tvID = (tvID == null ? LogUploadMgr.getTVID() : tvID);
		this.userID = (userID == null ? LogUploadMgr.userprofile.getUserID() : userID);
		this.params = params;
	}

	/**
	 * 转换成字符串参数
	 * 
	 * @return param字符串
	 */
	public String toParamString() {
		StringBuffer sb = new StringBuffer();
		sb.append(version).append(utils.LOG_SEPARATOR).append(type).append(utils.LOG_SEPARATOR)
				.append(ensure(firmwareVersion)).append(utils.LOG_SEPARATOR).append(ensure(userID));

		// 如果是开/关机日志类型，则保证有TVID
		if (type == LogUploadMgr.TYPE_POWER) {
			sb.append(utils.LOG_SEPARATOR).append(ensure(tvID));
		} else {
			if (tvID != null) {
				sb.append(utils.LOG_SEPARATOR).append(ensure(tvID));
			}
		}

		sb.append(utils.LOG_SEPARATOR).append(currentTime);

		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				sb.append(utils.LOG_SEPARATOR).append(ensure(params[i]));
			}
		}
		return sb.toString();
	}

	/**
	 * 确保字符串符合规范：<br>
	 * 1、null替换成""
	 * 
	 * @param str
	 * @return
	 */
	public static String ensure(String str) {
		return str == null ? "" : str;
	}

}
