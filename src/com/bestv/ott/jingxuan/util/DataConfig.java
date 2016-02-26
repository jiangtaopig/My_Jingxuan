package com.bestv.ott.jingxuan.util;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.bestv.ott.jingxuan.livetv.OttContextMgr;
import com.bestv.ott.jingxuan.net.ModuleServiceMgr;

public class DataConfig {
	private final static String TAG = "DataConfig";
	public static final boolean SOLID_FOR_JS = false;
	
	public static String PACKAGE_NAME = "com.bestv.jingxuan";
	
	public static final long MAX_EXPIRE_TIME_MAINDATA = 1000 * 60 * 10; //ten minutes
	public static final long MAX_EXPIRE_TIME_CDNTOKEN = IOTV_Date.HOUR_TIME * 12;
	
	//the play actions of the latest iotv programs in jingxuan
	public static final long PLAY_AS_IOTV = 1;  //play as iotv, it will not be interrupt while playing.
	public static final long PLAY_AS_IOTV_INTERRUPTED = 2; //play as iotv, it will be interrupted at the end of a program by a black scene.
	public static final long PLAY_AS_JX = 3; //play as jingxuan, it will be switched to next channel's program at current current channel's program finishing.
	
	public static final String DATA_CONFIG_PREF = "data_config_pref";
	public static final String UPDATE_DATA_TIME = "channel_update_time";
	public static final String UPDATE_CDN_TOKEN_TIME = "update_cdn_token";
	public static final String CDN_TOKEN_VALUE = "cdn_token_value";
	public static final String DEFAULT_CHANNEL_NUM = "default_channel_name";
	public static final String DEFAULT_SCHEDULE_INDEX_JX = "default_schedule_index_jx";
	public static final String DEFAULT_SHOW_DOWNDETAIL = "default_show_downdetail";
	public static final String TVOD_DATE_COUNT = "tvod_date_count";
	public static final String SUPPORT_LIVE_TIMESHIFT = "support_live_timeshift";
	public static final String LIVE_PLAY_ENDACT = "LivePlayEndAct";
	/**
	 * ����汾����Ϣ�������ǰ�汾�뻺��汾��ͬ��˵��Ӧ�ø��£���Ҫչʾ��ӭҳ
	 */
	public static final String VERSION_CODE_VALUE = "version_code_value";
	
	//for jingxuan
	public static final String JX_UPDATE_DATA_TIME = "jx_update_data_time";
	public static final long JX_MAX_EXPIRE_TIME_MAINDATA = 1000 * 60 * 120; //two hours
	
	public static final String TEST_REST_MAX_TIME = "test_rest_max_time";
	public static final String TEST_EXIT_APP_TIME = "test_exit_app_time";
	
	public static String SERVICE_URL = "http://appservice.bbtv.cn/APPService/index.php";
	static final String JS_URL = "http://jsydreplay.bbtv.cn/APPService/index.php/";
	
	//the default service url of jingxuan
	public static String JX_SERVICE_URL = "http://appservice.bbtv.cn/APPService/index.php";
	
	public static String getChannelUrl(){
		if (OttContextMgr.getInstance().isJXRunning()){
			return getServiceUrl() + "controller=Live&action=QueryChannelExt";
		}
		
		return getServiceUrl() + "controller=Live&action=QueryChannel";
	}
	
	public static String getScheduleUrl(){
		return getServiceUrl() + "controller=Live&action=QuerySchedule";
	}
	
	public static String getServiceUrl() {
		if (SOLID_FOR_JS){
			return JS_URL + "?";
		}
	
		//return "http://appservice.bbtv.cn/APPService/index.php?";
		//return "http://10.61.2.68:70/APPService/?";
		return SERVICE_URL + "?";
		//return "http://10.61.2.189/APPService/index.php?";
		//return "http://210.52.219.36/APPService/?";
		//return "http://101.231.136.58:10216/dxf_APPService/index.php?";
		//return "http://210.52.219.59/APPService/index.php?";
	}
	
	public static void initPackageName(Context c){
		PACKAGE_NAME = c.getPackageName();
	}
	
	public static void initDataConfig(Context context){
		// IAuth auth = AdapterManager.getInstance().getAuth();
         //String serviceurl = auth.getModuleService(ModuleServiceMgr.MODULE_IOTV_LIVE); 
		TimeUseDebugUtils.timeUsageDebug(TAG+",enter initDataConfig");
		String serviceurl = OttContextMgr.getInstance().getIOTVServicesUrl();
				//ModuleServiceMgr.getAppAddress(context, ModuleServiceMgr.MODULE_IOTV_LIVE);
		Logger.d("init servicr url from module = " + serviceurl);
		if (utils.isNull(serviceurl)){
//		     serviceurl = OttContextMgr.getInstance().getConfig("bestvConfig.properties", "LIVE_SVR_ADDRESS");
//		     
//		     Logger.d("init servicr url from local = " + serviceurl);
//	         if (utils.isNotNull(serviceurl) && serviceurl.indexOf("app.bbtv.cn/ServerAPI/index.php") == -1){
//	        	 SERVICE_URL = serviceurl;
//	         }
			
			String temp = SERVICE_URL;
			String mTargetOEM = OttContextMgr.getInstance().getTargetOEM();
			
			if(mTargetOEM.equalsIgnoreCase("B2C")){
				temp = "http://appservice.bbtv.cn/APPService/index.php";
			}else if(mTargetOEM.equalsIgnoreCase("AHYD")){
				temp = "http://ahydappservice.bbtv.cn:8080/APPService/index.php";
			}else if(mTargetOEM.equalsIgnoreCase("JSYD")){
				temp = "http://jsydreplay.bbtv.cn/APPService/index.php";
			}else if(mTargetOEM.equalsIgnoreCase("YDJD")){
				temp = "http://bestvepg.itv.cmvideo.cn:8086/APPService/index.php";
			}
			
			SERVICE_URL = temp;
		}else{
			SERVICE_URL = serviceurl;
		}
		
		//initialization data configuration for jingxuan if need.
		if (OttContextMgr.getInstance().isJXRunning()){
			initJXDataConfig(context);
		}
		TimeUseDebugUtils.timeUsageDebug(TAG+",exit initDataConfig");
	}
	
	public static void initJXDataConfig(Context context){
		TimeUseDebugUtils.timeUsageDebug(TAG+",enter  initJXDataConfig");
		String temp =OttContextMgr.getInstance().getJingXuanServicesUrl();
				//ModuleServiceMgr.getAppAddress(context, ModuleServiceMgr.MODULE_JINGXUAN);
		if(utils.isEmpty(temp)){
			String mTargetOEM = OttContextMgr.getInstance().getTargetOEM();
			
			Logger.d("No AppAddress, TargetOEM is:" + mTargetOEM);
			
			if(mTargetOEM.equalsIgnoreCase("B2C")){
				temp = "http://appservice.bbtv.cn/APPService/index.php";
			}else if(mTargetOEM.equalsIgnoreCase("AHYD")){
				temp = "http://ahydappservice.bbtv.cn:8080/APPService/index.php";
			}else if(mTargetOEM.equalsIgnoreCase("JSYD")){
				temp = "http://jsydreplay.bbtv.cn/APPService/index.php";
			}else if(mTargetOEM.equalsIgnoreCase("YDJD")){
				temp = "http://bestvepg.itv.cmvideo.cn:8086/APPService/index.php";
			}else{
				temp = "http://appservice.bbtv.cn/APPService/index.php";
			}
			
			Logger.d("AppAddress is: " + temp);
		}
		
		JX_SERVICE_URL = temp;//"http://10.61.41.90/APPService/index.php";//temp;
		TimeUseDebugUtils.timeUsageDebug(TAG+",exit  initJXDataConfig");
	}
	
	public static String getJXCategoryUrl(){
		return getJXServiceUrl() + "controller=Live&action=QueryJxCate";
	}
	
	public static String getJXScheduleUrl(){
		return getJXServiceUrl() + "controller=Live&action=QueryJxCateSchedule";
	}
	
	public static String getAllJXScheduleUrl(){
		//return "http://10.61.41.90/APPService/index.php?" + "controller=Live&action=QueryJxCateScheduleExt";
		return getJXServiceUrl() + "controller=Live&action=QueryJxCateScheduleExt";
	}
	
	public static String getJXServiceUrl() {
		return JX_SERVICE_URL + "?";
	}
	
	public static String getLianLianKanUrl(){
		return getServiceUrl() + "controller=Live&action=QueryProgList";
	}
	
	public static String getFilePath(String filename){
		return getFileDir() + File.separator + filename + ".json";
	}
	
	public static String getFileDir(){
		String dir = "/data/data/" + PACKAGE_NAME + File.separator + "channeljson";
		
		File f = new File(dir);
		if (!f.exists()){
			f.mkdirs();
		}
		
		return dir;
	}
	
	public static String getNetIconDir(){
		String dir = "/data/data/" + PACKAGE_NAME + File.separator + "channelicons";
		
		File f = new File(dir);
		if (!f.exists()){
			f.mkdirs();
		}
		
		return dir;
	}
	
	public static void clearAllCacheFile(){
		try{
			String filedir = getFileDir();
		    File file = new File(filedir);
		    
			for(String tmp : file.list()){
				if(!tmp.equals(Constants.JX_SUBSCRIBE_FILE + ".json")){//订阅的缓存文件不清除
					Logger.d("delete file " + tmp);
					File f = new File(filedir + File.separator + tmp);
					f.delete();
				}
			}
		}catch(Exception e){
			Logger.d("get a exception while clear cache!!");
		}
	}
	
	public static void resetDataConfig(Context context){
		setDataConfigValueL(context, TEST_REST_MAX_TIME, -1);
		setDataConfigValueL(context, TEST_EXIT_APP_TIME, -1);
		setDataConfigValueL(context, DEFAULT_SHOW_DOWNDETAIL, -1);
		setDataConfigValueL(context, LIVE_PLAY_ENDACT, PLAY_AS_IOTV_INTERRUPTED);
	}
	
	public static long getDataConfigValueL(Context c, String key){
		SharedPreferences sp = c.getSharedPreferences(DATA_CONFIG_PREF, Context.MODE_WORLD_READABLE);
		return sp.getLong(key, -1);
	}
	
	public static String getDataConfigValueS(Context c, String key){
		SharedPreferences sp = c.getSharedPreferences(DATA_CONFIG_PREF, Context.MODE_WORLD_READABLE);
		return sp.getString(key, "");
	}
	
	public static void setDataConfigValueL(Context c, String key, long value){
		SharedPreferences sp = c.getSharedPreferences(DATA_CONFIG_PREF, Context.MODE_WORLD_WRITEABLE);
		Editor d = sp.edit();
		d.putLong(key, value);
		d.commit();
	}
	
	public static void setDataConfigValueS(Context c, String key, String value){
		SharedPreferences sp = c.getSharedPreferences(DATA_CONFIG_PREF, Context.MODE_WORLD_WRITEABLE);
		Editor d = sp.edit();
		d.putString(key, value);
		d.commit();
	}

	public static int getDataConfigValueI(Context c, String key){
		SharedPreferences sp = c.getSharedPreferences(DATA_CONFIG_PREF, Context.MODE_WORLD_READABLE);
		return sp.getInt(key, -1);
	}
	
	public static void setDataConfigValueI(Context c, String key, int value){
		SharedPreferences sp = c.getSharedPreferences(DATA_CONFIG_PREF, Context.MODE_WORLD_WRITEABLE);
		Editor d = sp.edit();
		d.putInt(key, value);
		d.commit();
	}
	
	public static boolean supportLiveTimeShift(Context c){
		long l = getDataConfigValueL(c, SUPPORT_LIVE_TIMESHIFT);
		return l == 1;
	}
}
