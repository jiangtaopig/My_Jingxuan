package com.bestv.ott.jingxuan.livetv;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;

import com.bestv.ott.framework.BesTVServicesMgr;
import com.bestv.ott.framework.IBesTVServicesConnListener;
import com.bestv.ott.framework.beans.BesTVResult;
import com.bestv.ott.framework.defines.Define;
import com.bestv.ott.framework.proxy.authen.AuthParam;
import com.bestv.ott.framework.proxy.authen.AuthResult;
import com.bestv.ott.framework.proxy.authen.UserProfile;
import com.bestv.ott.framework.proxy.config.LocalConfig;
import com.bestv.ott.framework.proxy.config.SysConfig;
import com.bestv.ott.framework.utils.DBUtils;
import com.bestv.ott.hal.BSPSystem;
import com.bestv.ott.jingxuan.net.ModuleServiceMgr;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.TimeUseDebugUtils;
import com.bestv.ott.jingxuan.util.utils;
import com.shcmcc.tools.GetSysInfo;

/**
 * 
 * @author hu.fuyi
 *  "UserID", "UserGroup", "UserGroup2", "UserToken",
 *         "ServerAuth", "AAASrvAddress", "AAASrvAddress2", "AAASrvAddress3",
 *         "EpgSrvAddress", "EpgSrvAddress2", "PlaySrvAddress",
 *         "PlaySrvAddress2", "UpgradeSrvAddress", "UpgradeSrvAddress2",
 *         "IMGSrvAddress", "IMGSrvAddress2", "ServiceAddress", "DRMSrvAddress",
 *         "DRMSrvAddress2", "DTAlogAdress", "DTAlogAdress2", "ForcedUpgrade",
 *         "LogAddress", "LogAddress2", "SmallThirdAddress",
 *         "SmallThirdAddress2", "OperAAASrvAddress", "OperAAASrvAddress2",
 *         "OperAAASrvAddress3", "ModuleAddress", "ModuleAddress2"
 * 
 */
public class OttContextMgr {
	public static final int APP_RUNNING_TYPE_IOTV = 1;
	public static final int APP_RUNNING_TYPE_JX = 2;
	
	private static final String TAG = "OttContent";
	
	public static final String OTT_SERVICE_NAME = "com.bestv.ott.aidl.IAidlOttCService";
	
	private UserProfile mProfile = null;
	private String mIP = null;
	private Context mCT = null;
	private static OttContextMgr mInstance = null;

	public static final String STR_USER_ID = "UserID";
	public static final String STR_USER_GROUP = "UserGroup";
	public static final String STR_USER_TOKEN = "UserToken";
	public static final String STR_IMGSrvAddress = "IMGSrvAddress";
	public static final String STR_EpgSrvAddress = "EpgSrvAddress";
	public static final String STR_MODULEADDRESS = "ModuleAddress";
	
	public static final String LOG_DOWNLOAD_SAMPLING_TIME = "DownloadLogSamplingTime";
	public static final String LOG_DOWNLOAD_MAX_ITEMS = "DownloadLogMaxItems";
	public static final String LOG_UPLOAD_MAX_BUFFERINGS = "UploadMaxBufferringRecords";
	public static final String LOG_UPLOAD_MAX_DOWNLOADS = "UploadMaxDownloadRecords";
	
	private static final String OEM_NAME = "oem_name";
	private static final String CLIENT_PROFILE = "client_profile";
	private static final String PLAYER_PROFILE = "player_profile";
	private static final String VERSION = "version";
	private static final String USER_AGENT_PROFILE = "useragent_profile";
	private String IOTVServicesUrl = "";
	private String JingXuanServicesUrl = "";  
	
	public static final String BESTV_CONFIG_FILE = "bestvConfig.properties";
	public static final String BESTV_OEM_FILE = "oem.properties";
	
	//the sampling period of video download detail in milliseconds. 
	public final static long SAMPLING_PERIOD = 1000 * 60 * 5;
	//the max value to trigger send download detail log
	public final static int MAX_DOWNLOAD_DETAILS = 30;
	
	OttDataLoadFinishListener mListener = null;
	List<OttDataLoadFinishListener> DataLoadFinishListenes = new ArrayList<OttDataLoadFinishListener>();
	
	//the instance can not be reseted in lock mode
	boolean mLocked = false;
	boolean mIsInited = false;
	
	BesTVServicesMgr mOttMgr;
	SysConfig mSysConfig;
	LocalConfig mLocalConfig;
	
	//the running mode of application, the default as iotv
	private int mAppRunningType = APP_RUNNING_TYPE_IOTV;
	
	
	private String mTargetOem = null;
	private boolean isInside = false;
	
	
	public interface OttDataLoadFinishListener{
		void OttDatainitFinish();
	}

	public static OttContextMgr getInstance() {
		if (null == mInstance) {
			mInstance = new OttContextMgr();
		}
		return mInstance;
	}
	
	public int getAppRunningType(){
		return mAppRunningType;
	}
	
	public boolean isIOTVRunning(){
		return mAppRunningType == APP_RUNNING_TYPE_IOTV;
	}
	
	public boolean isJXRunning(){
		return mAppRunningType == APP_RUNNING_TYPE_JX;
	}
	
	public void setAppRunningType(int runningtype){
		mAppRunningType = runningtype;
	}
	
	public void reset(){
		if (!mLocked){
			mInstance = null;
		}
	}
	
	public void lockInstance(boolean lock){
		mLocked = lock;
	}
	
	private void appInit() {
	}
	
	/**
	 * @author hufuyi 2014/3/5
	 * initialize data
	 * 
	 * @param listener
	 *        the listener that will be invoked at data initialize finish.
	 * */
	public void initContext(OttDataLoadFinishListener listener){
		TimeUseDebugUtils.timeUsageDebug(TAG+",enter initContext");
		if (mIsInited){
			//invokeListener();
			//return;
		}
		
		mListener = listener;
		
		DataLoadFinishListenes.add(mListener);
		
		mIsInited = true;
		
		TimeUseDebugUtils.timeUsageDebug(TAG+",call BesTVServicesMgr.connect");
		
		BesTVServicesMgr.getInstance(getContext()).connect(
				new IBesTVServicesConnListener() {

					@Override
					public void onBesTVServicesConnected(BesTVServicesMgr mgr) {
						// TODO Auto-generated method stub
						TimeUseDebugUtils.timeUsageDebug(TAG+",enter onBesTVServicesConnected");
						mOttMgr = mgr;
						mSysConfig = mOttMgr.getConfigService().getSysConfig();
						mLocalConfig = mOttMgr.getConfigService().getLocalConfig();
						try{
						TimeUseDebugUtils.timeUsageDebug(TAG+",start  getModule url");
						IOTVServicesUrl = mOttMgr.getModuleService().getModuleService(ModuleServiceMgr.MODULE_IOTV_LIVE).getServiceAddress();
						JingXuanServicesUrl = mOttMgr.getModuleService().getModuleService(ModuleServiceMgr.MODULE_JINGXUAN).getServiceAddress();
						}catch(Exception e){
							e.printStackTrace();
						}
						TimeUseDebugUtils.timeUsageDebug(TAG+",end  getModule url");
						setUserProfile(mgr.getUserService().getUserProfile());
						invokeListener();
						
						mTargetOem = getTargetOEM();
						
						if (null != mLocalConfig) {
							isInside = (Define.OTT_MODE_INSIDE_LITE == mLocalConfig.getOttMode());
						}
					}
				});
	}
	
	private void invokeListener(){
		Logger.d(TAG, "invokeListener , DataLoadFinishListenes size = "+DataLoadFinishListenes.size());
		for(OttDataLoadFinishListener listener : DataLoadFinishListenes){
			if (listener != null){
				listener.OttDatainitFinish();
			}
		}
//		if (mListener != null){
//			mListener.OttDatainitFinish();
//		}
	}
	
	static boolean hasOttService(Context ct) {
		boolean ret = true;
		try {
			Intent it = new Intent(OTT_SERVICE_NAME);
			List<ResolveInfo> info = ct.getPackageManager().queryIntentServices(it, 0);
			if ((null==info) || (0==info.size())) {
				ret = false;
			}
		} catch (Throwable e) {
			Logger.d("failed to check service, because of : " +
					e.toString());
			ret = false;
		}
		
		Logger.d("hasOttService = " + ret);
		
		return ret;
	}
	
	static boolean hasConfigProxys(Context ct) {
		boolean ret = true;

		try {
			Cursor cursor = ct.getContentResolver().query(
					Uri.parse("content://com.bestv.ott.provider.config/local"),
					null, null, null, null);

			if (cursor == null) {
				ret = false;
			}
		} catch (Throwable e) {
			ret = false;
		}
		
		Logger.d("hasConfigProxys = " + ret);

		return ret;
	}

	private OttContextMgr() {
		mIsInited = false;
		mLocked = false;
		try {
			mIP = utils.getIPAddress();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public String getIP() {
		return mIP;
	}

	/*public void refreshUserProfile() {
		try {
			Cursor cursor = mCT
					.getContentResolver()
					.query(Uri
							.parse("content://com.bestv.ott.userprovider/getUserProfile"),
							null, null, null, null);
			if (null != cursor) {
				cursor.moveToFirst();
				mappingProfile(cursor);
				cursor.close();
			}
			mHasRefreshUserProfile = true;
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}*/
	public String getBmsUserToken(){
		if (mProfile != null){
			return mProfile.getOperToken();
		}
		
		return null;
	}

	public UserProfile getUserProfile() {
		/*try {
			if (null == mProfile) {
				mProfile = new UserProfile();
			}
			if ((utils.isNull(mProfile.getUserID()))
					|| (!mHasRefreshUserProfile)) {
				refreshUserProfile();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}*/
		
		Logger.d("before get userprofile = ");
		//block invoke thread to wait profile initialize finish
		int timeout = 10000;
		int count = 0;
		
		while(mProfile == null && count < timeout){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			count += 1000;
		}
		
		Logger.d("after get userprofile = " + count);

		return mProfile;
	}

	public void setUserProfile(UserProfile profile) {
		this.mProfile = profile;
	}

	public String getTVID() {
		String tvid = "";
		
		if (mSysConfig != null && !utils.isEmpty(mSysConfig.getTvID())){
			return mSysConfig.getTvID();
		}
		
		tvid = utils.safeString(getOemName()) + "$"
					+ utils.safeString(getTerminalType()) + "$"
					+ utils.safeString(getSN());
		
		return tvid;
	}
	
	public String operAuth(String playUrl, String channelCode) {
		Logger.d("~~~~~~~~~~~~~~~~~~~~~~~operAuth : " + playUrl + "; channelCode = " + channelCode);
		String ret = playUrl;
		try {
			AuthParam param = new AuthParam();
			if(isJsydCP()) {
				param.setItemCode("bestv-" + channelCode);
			} else {
				param.setItemCode(channelCode);
			}
			
			param.setInputPlayUrl(playUrl);

			if (mOttMgr != null){
				BesTVResult result = mOttMgr.getOrderService().operAuth(param, Define.HTTP_DEF_TIMEOUT);
				
				if (result != null){
					AuthResult authResult = mOttMgr.getOrderService().getAuthResult(result);
					
					if (authResult != null && utils.isNotNull(authResult.getPlayURL())) {
						ret = authResult.getPlayURL();
					}
				}
			}


		} catch (Throwable e) {
			e.printStackTrace();
		}
		Logger.d("~~~~~~~~~~~~~~~~~~~~~~~operAuth, return " + ret);
		return ret;
	}

	public String getTVProfile() {
		if (mSysConfig != null && !utils.isEmpty(mSysConfig.getTvProfile())){
			return mSysConfig.getTvProfile();
		}
		
		return "FirmwareVersion/" + getFirmwareVersion();
	}

	public String getFirmwareVersion() {
		String firmwareversion = "";
		
		if (mSysConfig != null && !utils.isEmpty(mSysConfig.getFirmwareVersion())){
			firmwareversion = mSysConfig.getFirmwareVersion();
		}else{
			firmwareversion = BSPSystem.getInstance(null, null).getFirmwareVersion();
		}
		
		if (utils.isNull(firmwareversion)) {
			return "BesTV_Mozart_A_1.0.0.0";
		} else {
			return firmwareversion;
		}
	}
	
	public String getErrCodePrefix(){
		if (mOttMgr != null){
			return mOttMgr.getConfigService().getLocalConfig().getErrCodePrefix();
		}
		
		return "";
	}

	public String getSN() {
		String ret = "";
		try {
			String sn = "";
			
			if (mSysConfig != null && !utils.isEmpty(mSysConfig.getSn())){
				sn = mSysConfig.getSn();
			}else{
				sn = BSPSystem.getInstance(null, null).getSnNum();
			}
			
			ret = utils.safeString(sn);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return ret;
	}

	private String getTerminalType() {
		String ret = "";
		try {	
			ret = utils.safeString(BSPSystem.getInstance(null, null)
					.getTerminalType());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return ret;
	}

	public Context getContext() {
		return mCT;
	}

	public void setContext(Context ct) {
		mCT = ct;
	}
	
	/**
	 * if in CHINA mobile BOX, return china mobile STB id
	 * else in BESTV STB BOX, return bestv user id
	 * */
	public String getSTBID(){
		String stbid = "";
		
		GetSysInfo sysinfo = GetSysInfo.getInstance("10086", "");
		
		if (sysinfo != null){
			stbid = sysinfo.getSnNum();
		}
		
		if (stbid == null || stbid.equals("")){
			if (mProfile != null){
			    stbid = mProfile.getUserID();
			}
		}
		
		if (stbid == null){
			stbid = "";
		}
		
		return stbid.toUpperCase();
	}
	
	public String getConfigPath(){
		String path = System.getenv("BESTV_CONF_PATH");
		if (utils.isNull(path)) {
			path = "/cus_config";
		}

		return path;
	}
	
	public String getConfig(String propfile, String key){
		Properties pro = new Properties();
		String res = "";
		try {
			FileInputStream input = new FileInputStream(getConfigPath() + File.separator + propfile);
			pro.load(input);
			res = pro.getProperty(key, "");
			input.close();
		} catch (Throwable e) {
			Logger.d("find a exception while get value from config file = " + e.toString());
		}
		
		Logger.d("getConfig key = " + key + " res = " + res);
		
		return res;
	}
	
	public Properties getConfig(String propfile){
		Properties pro = new Properties();
		
		try {
			FileInputStream input = new FileInputStream(getConfigPath() + File.separator + propfile);
			pro.load(input);
			input.close();
		} catch (Throwable e) {
			Logger.d("find a exception while get value from config file = " + e.toString());
		}
		
		return pro;
	}

	public String getOemName() {
		return getConfig(BESTV_OEM_FILE, OEM_NAME);
	}

	public String getClientProfile() {
		return getConfig(BESTV_OEM_FILE, CLIENT_PROFILE);
	}

	public String getPlayerProfile() {
		return getConfig(BESTV_OEM_FILE, PLAYER_PROFILE);
	}

	public String getUserAgentProfile() {
		return getConfig(BESTV_OEM_FILE, USER_AGENT_PROFILE);
	}
	
	public boolean isOEMB2C(){
		String target_oem = getConfig(BESTV_CONFIG_FILE, "TARGET_OEM");
		return target_oem != null && target_oem.equals("B2C");
	}
	
	public AuthResult playAuthen(AuthParam param, int timeout) {
		Logger.d("enter play(" + param + ", " +  timeout);
		if (mOttMgr != null){
		    return mOttMgr.getOrderService().getAuthResult(mOttMgr.getOrderService().auth(param, timeout));
		}
		
		return null;
	}
	
	private AuthResult play2(AuthParam param, int timeout) {
		Logger.d("enter play(" + param + ", " +  timeout + ")");
		return playAuthen(param, timeout);
	}
	
	//initialize CDN address and append it to play request url if it is not empty
	private String mCacheCdnAddress = "";
	protected static final String KEY_VALUE = "value";
	public static final String CDN_ADDRESS = "CDNAddress";
	protected final Uri mUri = Uri.parse("content://stbconfig/authentication/cdn_slb");

	public String getCDNAddress() {
		Logger.d("start get cdn address");
		if (utils.isNotNull(mCacheCdnAddress)){
			return mCacheCdnAddress;
		}
		
		String ret = null;
		Cursor cursor = null;
		try {
			cursor = mCT.getContentResolver().query(mUri, null, null, null,
					null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					ret = DBUtils.getString(cursor, KEY_VALUE);
				}
				cursor.close();
				cursor = null;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (null != cursor) {
				cursor.close();
			}
		}
		
		mCacheCdnAddress = ret;
		Logger.d("get cdn address result = " + mCacheCdnAddress);
		return ret;
	}
	
	public String getTargetOEM(){
		if (null == mTargetOem) {
			if (null != mLocalConfig) {
				mTargetOem = mLocalConfig.getTargetOEM();
			} else {
				mTargetOem = getConfig(BESTV_CONFIG_FILE, "TARGET_OEM");
			}
		}
		return mTargetOem;
	}

	public boolean isJsydCP(){
		return isInside && (Define.TARGET_OEM_JSYD.equalsIgnoreCase(getTargetOEM()));
	}

	public String getIOTVServicesUrl() {
		return IOTVServicesUrl;
	}

	public String getJingXuanServicesUrl() {
		return JingXuanServicesUrl;
	}

}
