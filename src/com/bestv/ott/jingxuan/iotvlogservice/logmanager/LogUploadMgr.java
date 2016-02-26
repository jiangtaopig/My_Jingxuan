package com.bestv.ott.jingxuan.iotvlogservice.logmanager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import com.bestv.ott.framework.proxy.authen.UserProfile;
import com.bestv.ott.jingxuan.iotvlogservice.beans.LogUploadBean;
import com.bestv.ott.jingxuan.iotvlogservice.logmanager.DataBaseMgr.LogItem;
import com.bestv.ott.jingxuan.iotvlogservice.utils.CommonTool;
import com.bestv.ott.jingxuan.iotvlogservice.utils.HttpUtil;
import com.bestv.ott.jingxuan.iotvlogservice.utils.utils;
import com.bestv.ott.jingxuan.livetv.OttContextMgr;

public class LogUploadMgr {
	public static final String TAG = "IOTV_QOS";
    public static UserProfile userprofile = null;
    
    class UserStatus {
    	static final int USER_STATUS_NOT_OPENED				= 0x0;
    	static final int USER_STATUS_INIT				    = 0x1;
    	static final int USER_STATUS_OPENED					= 0x2;
    	static final int USER_STATUS_LOGINED				= 0x3;
    }
    
    Context mContext;

	/**
	 * 单例
	 */
	private static LogUploadMgr instance;
	
	private static String TVID_CACHE = "";
	private static String FIRMWARE_CACHE = "";

	/**
	 * 同步锁对象：关于应用日志
	 */
	private static Object LOCK_APP = new Object();
	private static Object Lock_userfile = new Object();

	/**
	 * getInstance
	 * 
	 * @return LogUploadService instance
	 */
	public static LogUploadMgr getInstance(Context context) {
		if (instance == null) {
			instance = new LogUploadMgr(context);
		}
		
		return instance;
	}
	
	private LogUploadMgr(Context cxt){
		mContext = cxt;
	}
	
	public static boolean isUserprofileEmpty(){
		boolean b = false;
		synchronized (Lock_userfile) {
			userprofile = OttContextMgr.getInstance().getUserProfile();
			
			b = (userprofile == null);
		}
		
		return b;
	}
	
	private static boolean isOpen(){
		return true;
	}
	
	public void initUserprofile(){
	}

	/**
	 * 
	 * 开机/唤醒日志上报
	 * 
	 * @param action
	 *            0：开机，2：唤醒
	 */
	public static void bootLogUpload(int action, Context context) {
		Log.d(TAG, "bootLogUpload() prepare---");
		//String currentTime = CommonTool.yearEndSecond();

		String[] params = {isOpen() ? "1" : "0", action + "" };
		//LogUploadBean obj = new LogUploadBean(null, LogUploadMgr.TYPE_POWER, null, currentTime, null, null, params);

		//Log.d(TAG, "bootLogUpload() params:" + obj.toParamString());
		//hufuyi
		DataBaseMgr.instance(context).insert(LogUploadMgr.TYPE_POWER, params);
		/*try {
		Looper.prepare();
		getInstance().upload(obj);
		Looper.loop();
		} catch (Throwable e) {
			 e.printStackTrace();
		}*/
	}

	/**
	 * 
	 * 关机日志上报
	 * 
	 * @param action
	 *            1：关机，3：休眠
	 */
	public static void shutdownLogUpload(int action, Context context) {
		String[] params = {isOpen() ? "1" : "0", action + "" };
		//LogUploadBean obj = new LogUploadBean(null, LogUploadMgr.TYPE_POWER, null, null, null, null, params);

		//Log.d(TAG, "shutdownLogUpload() params:" + obj.toParamString());
		//hufuyi
		//getInstance().upload(obj);
		DataBaseMgr.instance(context).insert(LogUploadMgr.TYPE_POWER, params);
	}
	
	public static String getFirmwareVersion(){
		if (FIRMWARE_CACHE.equals("")){
			FIRMWARE_CACHE = OttContextMgr.getInstance().getFirmwareVersion();
		}else{
			Log.d(TAG, "get fireware version from cache = " + FIRMWARE_CACHE);
		}
		
		return FIRMWARE_CACHE;
	}
	
	public static String getTVID() {
		return OttContextMgr.getInstance().getTVID();
	}


	// /**
	// * 开户日志上报
	// */
	// public static void openLogUpload() {
	// String[] params = { OttContext.getTVID(),
	// SysCache.getInstance().getTerminal().getUserID(),
	// SysCache.getInstance().isOpen() ? "1" : "0" };
	// LogUploadBean obj = new LogUploadBean(null, LogUploadService.TYPE_OPEN,
	// null, null, params);
	//
	// getInstance().upload(obj);
	// }

	/**
	 * 播放日志上报
	 * 
	 * @param intent
	 *            封装参数
	 */
	public static void playLogUpload(Intent intent, Context context) {
		String itemCode = intent.getStringExtra("itemCode");
		String veidoClipCode = intent.getStringExtra("veidoClipCode");
		String begintime = intent.getStringExtra("begintime");
		String endtime = intent.getStringExtra("endtime");
		int pauseSumTime = intent.getIntExtra("pauseSumTime", 0);
		int pauseCount = intent.getIntExtra("pauseCount", 0);
		int firstLoadingTime = intent.getIntExtra("firstLoadingTime", 0);
		int loadingTime = intent.getIntExtra("loadingTime", 0);
		int loadingCount = intent.getIntExtra("loadingCount", 0);
		int downAvgRate = intent.getIntExtra("downAvgRate", 0);
		int downMaxRate = intent.getIntExtra("downMaxRate", 0);
		int downMinRate = intent.getIntExtra("downMinRate", 0);
		int downMaxShake = intent.getIntExtra("downMaxShake", 0);
		int action = intent.getIntExtra("action", 0);
		String errorCode = intent.getStringExtra("errorCode");
		String taskID = intent.getStringExtra("taskID");
		
		int playAvgRate = intent.getIntExtra("playAvgRate", -1);
		int playMaxRate = intent.getIntExtra("playMaxRate", -1);
		int playMinRate = intent.getIntExtra("playMinRate", -1);
		int playRateShake = intent.getIntExtra("playRateShake", -1);
		int playRateShakeCount = intent.getIntExtra("playRateShakeCount", 0);
		int netType = intent.getIntExtra("netType", 0);
		int loadingType = intent.getIntExtra("loadingType", 0);
		String cdnSource = intent.getStringExtra("cdnSource");
		String appcode = intent.getStringExtra("appCode");
		
		//add for qos version 002
		int playtype = intent.getIntExtra("playType", 3);
		String start_duration = intent.getStringExtra("startDuration");
		if (start_duration == null){
			start_duration = "";
		}
		
		String channel_code = intent.getStringExtra("channelCode");
		if (channel_code == null){
			channel_code = "";
		}
		
		String ssid = intent.getStringExtra("SSID");
		if (ssid == null){
			ssid = "";
		}
		
		String CategroyCode = intent.getStringExtra("CategroyCode");
		if (CategroyCode == null){
			CategroyCode = "";
		}
		
		String RecID = intent.getStringExtra("RecID");
		if (RecID == null){
			RecID = "";
		}
		
		if (appcode == null){
			//if app code is not set, set default as online video
			appcode = "BesTV_OnlineVideo";
		}
		
		//if begin time is not specified, ignore this play log.
		if (begintime == null){
			Log.d(TAG, "play log's begin time field is null, so ignore it");
			return;
		}
		
		//if the begin time larger than max STB elapse time
		//means it is a real RTC time, needn't convert it.
		if (begintime.length() < CommonTool.MAX_STB_ELAPSETIME.length()) {
			Log.d(TAG, "the raw begin time and end time in this play log = "
					+ begintime + ";" + endtime);

			long lbegin = 0;
			long lend = 0;

			try {
				lbegin = Long.parseLong(begintime);
				lend = Long.parseLong(endtime);
			} catch (Throwable a) {
			}

			long current_elapsedtime = SystemClock.elapsedRealtime();
			long current_time = System.currentTimeMillis();

			Log.d(TAG,
					"current time = "
							+ CommonTool.yearEndSecond(new Date(current_time)));
			// get system clock time by system elapse time
			if (lend > 0) {
				lend = current_time - (current_elapsedtime - lend);
			}

			lbegin = current_time - (current_elapsedtime - lbegin);

			begintime = CommonTool.yearEndSecond(new Date(lbegin));

			if (lend > 0) {
				endtime = CommonTool.yearEndSecond(new Date(lend));
			}

			Log.d(TAG, "the time after convert = " + begintime + ";" + endtime);
		}else{
			Log.d(TAG, "the real RTC time, needn't convert.");
		}

		String[] params = { itemCode, veidoClipCode, begintime, endtime, pauseSumTime + "", pauseCount + "",
				firstLoadingTime + "", loadingTime + "", loadingCount + "", downAvgRate + "", downMaxRate + "",
				downMinRate + "", downMaxShake + "", action + "", errorCode, taskID ,
				playAvgRate + "", playMaxRate + "", playMinRate + "",
				playRateShake + "", playRateShakeCount + "", netType + "",
				loadingType + "", cdnSource, appcode, "" + playtype, channel_code, start_duration, ssid, CategroyCode, RecID};

		//LogUploadBean obj = new LogUploadBean(null, LogUploadMgr.TYPE_PLAY, null, null, null, params);
		//hufuyi
		//getInstance().upload(obj);
		DataBaseMgr.instance(context).insert(LogUploadMgr.TYPE_PLAY, params);
	}
	
	/**
	 * 起播日志上报
	 * 
	 * @param intent
	 *            封装参数
	 */
	public static void playStartLogUpload(Intent intent, Context context) {
		String itemCode = intent.getStringExtra("itemCode");
		String veidoClipCode = intent.getStringExtra("veidoClipCode");
		String begintime = intent.getStringExtra("begintime");
		String endtime = intent.getStringExtra("endtime");
		int pauseSumTime = intent.getIntExtra("pauseSumTime", 0);
		int pauseCount = intent.getIntExtra("pauseCount", 0);
		int firstLoadingTime = intent.getIntExtra("firstLoadingTime", 0);
		int loadingTime = intent.getIntExtra("loadingTime", 0);
		int loadingCount = intent.getIntExtra("loadingCount", 0);
		int downAvgRate = intent.getIntExtra("downAvgRate", 0);
		int downMaxRate = intent.getIntExtra("downMaxRate", 0);
		int downMinRate = intent.getIntExtra("downMinRate", 0);
		int downMaxShake = intent.getIntExtra("downMaxShake", 0);
		int action = intent.getIntExtra("action", 0);
		String errorCode = intent.getStringExtra("errorCode");
		String taskID = intent.getStringExtra("taskID");
		
		int playAvgRate = intent.getIntExtra("playAvgRate", -1);
		int playMaxRate = intent.getIntExtra("playMaxRate", -1);
		int playMinRate = intent.getIntExtra("playMinRate", -1);
		int playRateShake = intent.getIntExtra("playRateShake", -1);
		int playRateShakeCount = intent.getIntExtra("playRateShakeCount", 0);
		int netType = intent.getIntExtra("netType", 0);
		int loadingType = intent.getIntExtra("loadingType", 0);
		String cdnSource = intent.getStringExtra("cdnSource");
		String appcode = intent.getStringExtra("appCode");
		
		//add for qos version 002
		int playtype = intent.getIntExtra("playType", 3);
		long wifi_strength = intent.getLongExtra("wifiStrength", 100);
		String start_duration = intent.getStringExtra("startDuration");
		if (start_duration == null){
			start_duration = "";
		}
		
		String channel_code = intent.getStringExtra("channelCode");
		if (channel_code == null){
			channel_code = "";
		}
		
		String ssid = intent.getStringExtra("SSID");
		if (ssid == null){
			ssid = "";
		}
		
		if (appcode == null){
			//if app code is not set, set default as online video
			appcode = "BesTV_OnlineVideo";
		}
		
		//if begin time is not specified, ignore this play log.
		if (begintime == null){
			Log.d(TAG, "play log's begin time field is null, so ignore it");
			return;
		}
		
		//if the begin time larger than max STB elapse time
		//means it is a real RTC time, needn't convert it.
		if (begintime.length() < CommonTool.MAX_STB_ELAPSETIME.length()) {
			Log.d(TAG, "the raw begin time and end time in this play log = "
					+ begintime + ";" + endtime);

			long lbegin = 0;
			long lend = 0;

			try {
				lbegin = Long.parseLong(begintime);
				lend = Long.parseLong(endtime);
			} catch (Throwable a) {
			}

			long current_elapsedtime = SystemClock.elapsedRealtime();
			long current_time = System.currentTimeMillis();

			Log.d(TAG,
					"current time = "
							+ CommonTool.yearEndSecond(new Date(current_time)));
			// get system clock time by system elapse time
			if (lend > 0) {
				lend = current_time - (current_elapsedtime - lend);
			}

			lbegin = current_time - (current_elapsedtime - lbegin);

			begintime = CommonTool.yearEndSecond(new Date(lbegin));

			if (lend > 0) {
				endtime = CommonTool.yearEndSecond(new Date(lend));
			}

			Log.d(TAG, "the time after convert = " + begintime + ";" + endtime);
		}else{
			Log.d(TAG, "the real RTC time, needn't convert.");
		}

		String[] params = { itemCode, veidoClipCode, begintime, endtime, pauseSumTime + "", pauseCount + "",
				firstLoadingTime + "", loadingTime + "", loadingCount + "", downAvgRate + "", downMaxRate + "",
				downMinRate + "", downMaxShake + "", action + "", errorCode, taskID ,
				playAvgRate + "", playMaxRate + "", playMinRate + "",
				playRateShake + "", playRateShakeCount + "", netType + "",
				loadingType + "", cdnSource, appcode, wifi_strength + "", playtype + "", channel_code, start_duration, ssid};

		//LogUploadBean obj = new LogUploadBean(null, LogUploadMgr.TYPE_PLAY, null, null, null, params);
		//hufuyi
		//getInstance().upload(obj);
		DataBaseMgr.instance(context).insert(LogUploadMgr.TYPE_PLAY_START, params);
	}
	/**
	 * 应用日志上报
	 * 
	 * @param intent
	 *            封装参数
	 */
	public static void appLogUpload(Intent intent, Context context) {
		synchronized (LOCK_APP) {
			String packageName = intent.getStringExtra("packageName");
            Log.d("harishhu", "package name = " + packageName);
			// 日志过滤
			if (appLogFilter(packageName)) {
				return;
			}
			
			String beginTime = CommonTool.yearEndSecond();
			String endTime = CommonTool.yearEndSecond();
			long timeCount = 0;
			String version = intent.getStringExtra("version");
			String[] params = { packageName, version, beginTime, endTime, timeCount + "" };
			DataBaseMgr.instance(context).insert(LogUploadMgr.TYPE_APP, params);

			/*//if (appTemp == null) {
				String beginTime = CommonTool.yearEndSecond();
				String endTime = CommonTool.yearEndSecond();
				long timeCount = 0;
				String version = intent.getStringExtra("version");
				String[] params = { packageName, version, beginTime, endTime, timeCount + "" };
				appTemp = new LogUploadBean(null, LogUploadMgr.TYPE_APP, null, null, null, params);
			} else {
				// begin - 将上一个应用日志上报
				String[] strs = appTemp.getParams();
				int length = strs.length;
				if (length == 5) {
					String temp = CommonTool.yearEndSecond();
					strs[length - 2] = temp;
					strs[length - 1] = (Long.parseLong(temp) - Long.parseLong(strs[length - 3])) + "";
				}
				appTemp.setParams(strs);
				//hufuyi
				//getInstance().upload(appTemp);
				DataBaseMgr.instance(context).insert(LogUploadMgr.TYPE_APP, strs);
				//insert into database
				
				// end - 将上一个应用日志上报

				// begin - 记录当前的应用日志
				String beginTime = CommonTool.yearEndSecond();
				String endTime = "";
				long timeCount = 0;
				String version = intent.getStringExtra("version");
				String[] params = { packageName, version, beginTime, endTime, timeCount + "" };
				appTemp = new LogUploadBean(null, LogUploadMgr.TYPE_APP, null, null, null, params);
				// end - 记录当前的应用日志

			}*/
		}

	}

	/**
	 * 异常日志上报
	 * 
	 * @param errorCode
	 * @param errorMsg
	 */
	public static void errorLogUpload(String errorCode, String errorMsg) {

		String[] params = { null, null, errorCode, errorMsg };

		LogUploadBean obj = new LogUploadBean(null, LogUploadMgr.TYPE_ERROR, null, null, null, params);

		// Log.d(TAG, "errorLogUpload() params:" + obj.toParamString());
		//hufuyi
		//getInstance().upload(obj);
	}

	/**
	 * 异常日志上报
	 * 
	 * @param intent
	 *            封装参数
	 */
	public static void errorLogUpload(Intent intent, Context context) {
		String packageName = intent.getStringExtra("packageName");
		String version = intent.getStringExtra("version");
		String errorCode = intent.getStringExtra("errorCode");
		String errorMsg = intent.getStringExtra("errorMsg");

		String[] params = { packageName, version, errorCode, errorMsg };

		//LogUploadBean obj = new LogUploadBean(null, LogUploadMgr.TYPE_ERROR, null, null, null, params);

		// Log.d(TAG, "errorLogUpload() params:" + obj.toParamString());
		//hufuyi
		//getInstance().upload(obj);
		DataBaseMgr.instance(context).insert(LogUploadMgr.TYPE_ERROR, params);
	}
	
	/**
	 * hufuyi
	 * video play buffering log
	 * */
	public static void bufferingLogUpload(Intent intent, Context context) {
		String type = intent.getStringExtra("playtype");
		String itemcode = intent.getStringExtra("itemCode");
		String videoClipCode = intent.getStringExtra("veidoClipCode");
		String channelno = intent.getStringExtra("channelno");
		String duration = intent.getStringExtra("duration");
		String taskID = intent.getStringExtra("taskID");
		String appCode = intent.getStringExtra("appCode");
		String cdn = intent.getStringExtra("cdnSource");
		if (cdn == null){
			cdn = "";
		}
		
		String detail = intent.getStringExtra("detail");
		if (detail == null){
			detail = "";
		}
		
		String ssid = intent.getStringExtra("SSID");
		if (ssid == null){
			ssid = "";
		}

		String[] params = { type, itemcode, videoClipCode, channelno, duration, taskID, appCode, detail, cdn , ssid};

		//LogUploadBean obj = new LogUploadBean(null, LogUploadMgr.TYPE_ERROR, null, null, null, params);

		// Log.d(TAG, "errorLogUpload() params:" + obj.toParamString());
		//hufuyi
		//getInstance().upload(obj);
		DataBaseMgr.instance(context).insert(LogUploadMgr.TYPE_BUFFERING, params);
	}
	
	/**
	 * hufuyi
	 * video play download log
	 * */
	public static void downloadLogUpload(Intent intent, Context context) {
		Log.d("harish4", "enter download logupload");
		String type = intent.getStringExtra("playtype");
		String itemcode = intent.getStringExtra("itemCode");
		String videoClipCode = intent.getStringExtra("veidoClipCode");
		String channelno = intent.getStringExtra("channelno");
		String duration = intent.getStringExtra("duration");
		String taskID = intent.getStringExtra("taskID");
		String appCode = intent.getStringExtra("appCode");
		String cdn = intent.getStringExtra("cdnSource");
		if (cdn == null){
			cdn = "";
		}
		
		String detail = intent.getStringExtra("detail");
		if (detail == null){
			detail = "";
		}
		
		String ssid = intent.getStringExtra("SSID");
		if (ssid == null){
			ssid = "";
		}

		String[] params = { type, itemcode, videoClipCode, channelno, duration, taskID, appCode, detail, cdn, ssid };

		Log.d("harish4", "enter download before inert!");
		//LogUploadBean obj = new LogUploadBean(null, LogUploadMgr.TYPE_ERROR, null, null, null, params);

		// Log.d(TAG, "errorLogUpload() params:" + obj.toParamString());
		//hufuyi
		//getInstance().upload(obj);
		DataBaseMgr.instance(context).insert(LogUploadMgr.TYPE_DOWNLOAD_DETAIL, params);
	}

	/**
	 * 升级文件下载日志上报
	 */
	public static void upgradeLogUpload(Intent intent, Context context) {
		//hufuyi
		//getInstance().upload(obj);
		String[] params = intent.getStringArrayExtra("fields");
		
		String pkgname = intent.getStringExtra("pkgName");
		String[] temp = null;
		
		if (pkgname != null){
			temp = new String[params.length + 1];
			
			for (int i = 0; i < params.length; i++){
				temp[i] = params[i];
			}
			
			temp[params.length] = pkgname;
			params = temp;
		}
		
		DataBaseMgr.instance(context).insert(LogUploadMgr.TYPE_UPGRADE, params);
	}
	
	/**
	 * 颗粒化升级结果日志
	 */
	public static void beanUpgradeResultLogUpload(Intent intent, Context context) {
		//hufuyi
		//getInstance().upload(obj);
		String p = intent.getStringExtra("param");
		
		if (p == null){
			return;
		}
		
		String params[] = p.split("\\" + utils.LOG_SEPARATOR);
		
		String pkgname = intent.getStringExtra("pkgName");
		String[] temp = null;
		
		if (pkgname != null){
			temp = new String[params.length + 1];
			
			for (int i = 0; i < params.length; i++){
				temp[i] = params[i];
			}
			
			temp[params.length] = pkgname;
			params = temp;
		}
		
		DataBaseMgr.instance(context).insert(LogUploadMgr.TYPE_BEAN_UPGRADE_RESULT, params);
	}

	/**
	 * 应用日志过滤
	 * 
	 * @param packageName
	 * @return boolean true:过滤, false:不过滤
	 */
	private static boolean appLogFilter(String packageName) {
		if (packageName == null){
			return true;
		}
		for (int i = 0; i < filter_appLog.length; i++) {
			if (packageName.equals(filter_appLog[i])) {
				Log.d(TAG, "appLogFilter() is filter:" + packageName);
				return true;
			}
		}
		return false;
	}

	/**
	 * the default upload method by http get.
	 * 
	 * @param obj
	 *            日志上报对象
	 * @return boolean 上报是否成功, true:成功, false:失败
	 */
	public synchronized boolean upload(LogUploadBean obj) {
		Log.d("harish2", "HPPT Get upload---begin---log upload params:" + obj.toParamString());
		// 上报是否成功
		boolean result = false;

		String param = null;
		try {
			param = URLEncoder.encode(obj.toParamString(), utils.CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.e(TAG, "upload()---" + e.toString());
		}
		
		/* modified by xu.chaokun in 20120904 for getting log-address from aaa */
		String logServer="";		
		try {
			if (userprofile != null){
				logServer = userprofile.getLogAddress();
				Log.d(TAG, "get log address from login response data");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		
		Log.d(TAG, "the errorlog upload server address = " + logServer);
		
		if (CommonTool.isNull(logServer)){
			Log.d(TAG, "upload()---, aaa.logServer is null, use default.");
			logServer = utils.LOG_UPLOAD_ADDRESS;
		}
		
		// 拼接URL
		//String url = (SysParam.LOG_UPLOAD_ADDRESS + URL_UPLOAD) + "?log=" + (param != null ? param : "");
		String url = logServer + URL_UPLOAD;
		
		Uri.Builder builder = Uri.parse(url).buildUpon();
		
		if (userprofile != null){
			builder.appendQueryParameter("UserID", userprofile.getUserID());
			builder.appendQueryParameter("UserToken", userprofile.getUserToken());
			builder.appendQueryParameter("UserGroup", userprofile.getUserGroup());
		}
		
		/* end modified by xu.chaokun in 20120904 for getting log-address from aaa */
		url = builder.build().toString();
		url += "&log=" + (param != null ? param : "");
		if (doUploadGet(url)) {
			Log.d(TAG, "11doGet() return true");
			result = true;
			//hufuyi
			if (utils.checkDebug()){
				utils.saveUploadFinish(obj.toParamString());
			}
		} else {
			Log.d(TAG, "doGet() return false");
		}

		Log.d(TAG, "upload---end---result:" + result);
		return result;
	}
	
	public synchronized boolean uploadPOST(int type, String values) {
		// 上报是否成功
		boolean result = false;
		
		/* modified by xu.chaokun in 20120904 for getting log-address from aaa */
		String logServer="";		
		try {
			if (userprofile != null){
				logServer = userprofile.getLogAddress();
				Log.d(TAG, "get log address from login response data");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		Log.d(TAG, "the errorlog upload server address = " + logServer);
		
		if (CommonTool.isNull(logServer)){
			Log.d(TAG, "upload()---, aaa.logServer is null, use default.");
			logServer = utils.LOG_UPLOAD_ADDRESS;
		}
		
		if (utils.POST_TEST){
			logServer = utils.POST_URL_TEST;
		}
		
		String temp = OttContextMgr.getInstance().getConfig(OttContextMgr.BESTV_CONFIG_FILE, "LOG_POST_URL");
		if (!temp.equals("")){
			logServer = temp;
		}
		
		// 拼接URL
		//String url = (SysParam.LOG_UPLOAD_ADDRESS + URL_UPLOAD) + "?log=" + (param != null ? param : "");
		String url = (logServer + URL_UPLOAD_POST);
		/* end modified by xu.chaokun in 20120904 for getting log-address from aaa */
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("LogType", type + ""));
		params.add(new BasicNameValuePair("QoSData", values));
		
		if (userprofile != null){
			params.add(new BasicNameValuePair("UserID", userprofile.getUserID()));
			params.add(new BasicNameValuePair("UserToken", userprofile.getUserToken()));
			params.add(new BasicNameValuePair("UserGroup", userprofile.getUserGroup()));
		}
		
		Log.d("harish4", "post upload-start type = " + type + " url = " + url + " values = " + params);
		
		if (doUploadPost(url, params)) {
			Log.d(TAG, "11doPost() return true");
			result = true;
			//hufuyi
			if (utils.checkDebug()){
				utils.saveUploadFinish(values);
			}
		} else {
			Log.d(TAG, "doGet() return false");
		}

		Log.d(TAG, "upload---end---result:" + result);
		return result;
	}
	
	public boolean upload(LogItem item){
		return upload(convertToBean(item));
	}
	
	public static LogUploadBean convertToBean(LogItem item){
		int log_type = item.type;
		LogUploadBean obj = null;
		
		switch(log_type){
		case LogUploadMgr.TYPE_PLAY:
			obj = new LogUploadBean(item.version, LogUploadMgr.TYPE_PLAY, item.farmversion, item.time, null, item.params);
			break;
		case LogUploadMgr.TYPE_POWER:
			obj = new LogUploadBean(item.version, LogUploadMgr.TYPE_POWER, item.farmversion, item.time, null, null, item.params);
		    break;
		case LogUploadMgr.TYPE_ERROR:
			obj = new LogUploadBean(item.version, LogUploadMgr.TYPE_ERROR, item.farmversion, item.time, null, item.params);
			break;
		case LogUploadMgr.TYPE_APP:
			obj = new LogUploadBean(item.version, LogUploadMgr.TYPE_APP, item.farmversion, item.time, null, item.params);
			break;
		case LogUploadMgr.TYPE_PLAY_START:
			obj = new LogUploadBean(item.version, LogUploadMgr.TYPE_PLAY_START, item.farmversion, item.time, null, item.params);
			break;
		case LogUploadMgr.TYPE_UPGRADE:
			obj = new LogUploadBean(item.version, LogUploadMgr.TYPE_UPGRADE, item.farmversion, item.time, null, item.params);
			break;
		case LogUploadMgr.TYPE_BEAN_UPGRADE_RESULT:
			obj = new LogUploadBean(item.version, LogUploadMgr.TYPE_BEAN_UPGRADE_RESULT, item.farmversion, item.time, null, item.params);
			break;
		case LogUploadMgr.TYPE_BUFFERING:
			obj = new LogUploadBean(item.version, LogUploadMgr.TYPE_BUFFERING, item.farmversion, item.time, null, item.params);
			break;
		case LogUploadMgr.TYPE_DOWNLOAD_DETAIL:
			obj = new LogUploadBean(item.version, LogUploadMgr.TYPE_DOWNLOAD_DETAIL, item.farmversion, item.time, null, item.params);
			break;
		}
		
		return obj;
	}
	
	public boolean upload(String params) {
		Log.d(TAG, "upload---begin---log upload params:" + params);
		// 上报是否成功
		boolean result = false;

		String param = null;
		try {
			param = URLEncoder.encode(params, utils.CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.e(TAG, "upload()---" + e.toString());
		}
		
		/* modified by xu.chaokun in 20120904 for getting log-address from aaa */
		String logServer="";		
		try {
			if (userprofile != null){
				logServer = userprofile.getLogAddress();
				Log.d(TAG, "get log address from login response data");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		
		Log.d(TAG, "the errorlog upload server address = " + logServer);
		
		if (CommonTool.isNull(logServer)){
			Log.d(TAG, "upload()---, aaa.logServer is null, use default.");
			logServer = utils.LOG_UPLOAD_ADDRESS;
		}
		
		// 拼接URL
		//String url = (SysParam.LOG_UPLOAD_ADDRESS + URL_UPLOAD) + "?log=" + (param != null ? param : "");
		String url = (logServer + URL_UPLOAD) + "?log=" + (param != null ? param : "");
		/* end modified by xu.chaokun in 20120904 for getting log-address from aaa */
		
		if (doUploadGet(url)) {
			Log.d(TAG, "doGet() return true");
			result = true;
		} else {
			Log.d(TAG, "doGet() return false");
		}

		Log.d(TAG, "upload---end---result:" + result);
		return result;
	}

	/**
	 * 发送Post请求
	 * 
	 * @param url
	 * @return 请求是否完成
	 */
	public static boolean doUploadPost(String url, List<NameValuePair> params) {
		Log.d(TAG, "doUpload post()--- get request url: " + url);
		try {
			HttpResponse response = HttpUtil.httpPost(url, params, 3000);

			if (response == null) {
				Log.d(TAG, "dopost()--- request server fail! response is null " + url);
				return false;
			}

			Log.d("harish4", "dopost()--- response code = " + response.getStatusLine().getStatusCode());
			// 如果请求返回码等于200，则认为请求完成
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.d(TAG, "doGet()--- request complete!   response code:" + response.getStatusLine().toString());
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "doGet()---" + e.toString());
			return false;
		}

		return false;
	}
	
	/**
	 * 发送Get请求
	 * 
	 * @param url
	 * @return 请求是否完成
	 */
	public static boolean doUploadGet(String url) {
		Log.d(TAG, "doGet()--- get request url: " + url);

		HttpGet request = new HttpGet(url);
		try {
			HttpResponse response = HttpUtil.getHttpClient().execute(request);

			if (response == null) {
				Log.d(TAG, "doGet()--- request server fail! response is null " + url);
				request.abort();
				return false;
			}

			// 如果请求返回码不等于500，则认为请求完成
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_INTERNAL_SERVER_ERROR) {
				Log.d(TAG, "doGet()--- request complete!   response code:" + response.getStatusLine().toString());
				return true;
			}
		} catch (Exception e) {
			request.abort();
			e.printStackTrace();
			Log.e(TAG, "doGet()---" + e.toString());
			return false;
		}

		return false;
	}


	/**
	 * URL：上载相对路径
	 */
	private static final String URL_UPLOAD = "/ottLogUpload";
	
	/**
	 * URL: the absolute path for http post
	 */
	private static final String URL_UPLOAD_POST = "/service/QoSLogUpload";

	/**
	 * 日志版本
	 */
	public static final String VERSION = "003";

	/**
	 * 日志类型：开/关机
	 */
	public static final int TYPE_POWER = 0;
	/**
	 * 日志类型：开户
	 */
	public static final int TYPE_OPEN = 1;
	/**
	 * 日志类型：应用
	 */
	public static final int TYPE_APP = 2;
	/**
	 * 日志类型：升级下载
	 */
	public static final int TYPE_UPGRADE = 3;
	/**
	 * 日志类型：播放
	 */
	public static final int TYPE_PLAY = 4;
	/**
	 * 日志类型：起播
	 */
	public static final int TYPE_PLAY_START = 5;
	/**
	 * 日志类型：颗粒化升级结果
	 */
	public static final int TYPE_BEAN_UPGRADE_RESULT = 6;
	/**
	 * 日志类型：异常
	 */
	public static final int TYPE_ERROR = 999;
	
	//add by hufuyi
	public static final int TYPE_BUFFERING = 8;
	public static final int TYPE_DOWNLOAD_DETAIL = 9;

	/**
	 * 应用日志过滤包名数组
	 */
	private static final String[] filter_appLog = { "com.bestv.message" };

	/**
	 * 异常日志错误代码：开户-连接服务器-连接失败
	 */
	public static final String E_OPEN_FAIL = "000100";
	/**
	 * 异常日志错误代码：开户-连接服务器-连接超时
	 */
	public static final String E_OPEN_TIMEOUT = "000101";
	/**
	 * 异常日志错误代码：登录-连接服务器-连接失败
	 */
	public static final String E_LOGIN_FAIL = "010100";
	/**
	 * 异常日志错误代码：登录-连接服务器-连接超时
	 */
	public static final String E_LOGIN_TIMEOUT = "010101";
	/**
	 * 异常日志错误代码：升级-下载-连接失败
	 */
	public static final String E_UPGRADE_FAIL = "020000";
	/**
	 * 异常日志错误代码：升级-下载-连接超时
	 */
	public static final String E_UPGRADE_TIMEOUT = "020001";
	/**
	 * 异常日志错误代码：升级-下载-存储不够
	 */
	public static final String E_UPGRADE_NOTENOUGH = "020002";
	/**
	 * 异常日志错误代码：升级-升级-调用升级接口失败
	 */
	public static final String E_UPGRADE_BSPFAIL = "020100";
	/**
	 * 异常日志错误代码：网络-本地网络-连接失败
	 */
	public static final String E_NET_FAIL = "030000";
	/**
	 * 异常日志错误代码：网络-服务器-连接失败
	 */
	public static final String E_NET_SERVER_FAIL = "030101";
	
	/**
	 * 异常日志错误代码：运营商登录-连接服务器-连接失败
	 */
	public static final String E_OPERATOR_LOGIN_FAIL = "110100";
	/**
	 * 异常日志错误代码：运营商登录-连接服务器-连接超时
	 */
	public static final String E_OPERATOR_LOGIN_TIMEOUT = "110101";

}