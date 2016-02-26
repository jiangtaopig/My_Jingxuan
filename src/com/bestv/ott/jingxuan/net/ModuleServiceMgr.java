package com.bestv.ott.jingxuan.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.bestv.ott.framework.beans.JsonResult;
import com.bestv.ott.framework.proxy.authen.UserProfile;
import com.bestv.ott.jingxuan.util.TimeUseDebugUtils;
import com.google.gson.Gson;

public class ModuleServiceMgr {
	public static final boolean DEBUG = false;
	
	public static final String MODULE_IOTV_LIVE = "SERVICE_LIVETV";
	public static final String MODULE_JINGXUAN = "SERVICE_JINGXUAN";
	
	private static final String TAG = "IOTV_Module";
	
	private static final int TIMEOUT = 30000;
	private static final String DEBUG_MODULE_SERVER_URL = "http://10.49.0.6:8080/module";
	
	private static final String OTTSERVICE_URL = "content://com.bestv.ott.userprovider/getUserProfile";
	
	public static final String STR_MODULEADDRESS = "ModuleAddress";
	public static final String GetModuleService = "/OttService/QueryAPPAdress";
	
	private static String[] AppCodes = {MODULE_IOTV_LIVE, MODULE_JINGXUAN};
	
	private static ModuleServiceMgr instance = null;
	private Context mContext;
	private UserProfile mUserProfile;
	
	private boolean isFinish = false;
	
	class ModuleNode{
		public String appcode;
		public String url;
	}
	
	public static class ModuleServiceInfo {
		public String AppCode;
		public String AppAddress;
		public String AuthAddress;
	}
	
	public static ModuleServiceMgr getInstance(Context context){
		if (instance == null){
			instance = new ModuleServiceMgr(context);
		}
		
		return instance;
	}
	
	public void setUserProfile(UserProfile up) {
		mUserProfile = up;
	}
	
	public boolean checkFinish(){
		return isFinish;
	}
	
	public ModuleServiceMgr(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	
	public void generateServiceSync(){
		Log.d(TAG, "generateServiceSync,isFinish="+isFinish);
		if(isFinish){
			return;
		}
		TimeUseDebugUtils.timeUsageDebug(TAG+",enter generateServiceSync");
		isFinish = false;
		
		if (mUserProfile == null){
			return;
		}
		
		String moduleserver = initModuleServer(mContext, mUserProfile);
		
		genServiceUrl(mContext, moduleserver, mUserProfile);
		isFinish = true;
		Log.d(TAG, "generateServiceSync finish");
		TimeUseDebugUtils.timeUsageDebug(TAG+",exit generateServiceSync");
	}
	
	private static String initModuleServer(Context context, UserProfile profile){
		String module_server = null;
		
		if (profile != null){
			module_server = profile.getModuleAddress();
		}
		
		if (DEBUG){
			module_server = DEBUG_MODULE_SERVER_URL;
		}
		
		return module_server;
	}
	
	public static String composeAppCodes(String[] appcodes){
		if (appcodes == null){
			return null;
		}
		
		if (appcodes.length < 1){
			return null;
		}
		
		StringBuilder builder = new StringBuilder();
		
		for (String appcode : appcodes){
			builder.append(appcode + ";");
		}
		
		return builder.toString();
	}
	
	private static void saveAppData(Context context, String appcode, String appaddress){
		SharedPreferences p = context.getSharedPreferences("APPCODES_DATA", Context.MODE_WORLD_WRITEABLE);
		Editor e = p.edit();
		e.putString(appcode, appaddress);
		
		e.commit();
	}
	
	public static String getAppAddress(Context context, String appcode){
		SharedPreferences p = context.getSharedPreferences("APPCODES_DATA", Context.MODE_WORLD_READABLE);
		return p.getString(appcode, "");
	}
	
	public static void genServiceUrl(Context context, String module_server, UserProfile profile) {
		Log.d(TAG, "genServiceUrl = " + module_server + " userid = " + profile.getUserID() + " USERGROUP = " + profile.getUserGroup());
		TimeUseDebugUtils.timeUsageDebug(TAG+",enter genServiceUrl");
		String temp = null;
		
		try {
			if (module_server == null || profile == null)
				return;

			temp = module_server + GetModuleService;
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			params.add(new BasicNameValuePair("UserGroup", profile.getUserGroup()));
			params.add(new BasicNameValuePair("UserID", profile.getUserID()));
			params.add(new BasicNameValuePair("UserToken", profile.getUserToken()));
			
			String apps = composeAppCodes(AppCodes);
			
			if (apps != null){
				params.add(new BasicNameValuePair("AppCode", apps));
			}
			
			BesTVHttpResult result = HttpUtils.get(temp, params, TIMEOUT);

			if (result != null) {
				JsonResult jsresult = (JsonResult) result.getJsonResult();
				JSONObject obj = (JSONObject) jsresult.getObj();
				
				JSONArray jsarray = obj.getJSONArray("Apps");
				
				for (int i = 0; i < jsarray.length(); i++){
					ModuleServiceInfo info = new Gson().fromJson(jsarray.get(i).toString(), ModuleServiceInfo.class);
					
					saveAppData(context, info.AppCode, info.AppAddress);
					Log.d(TAG, "qqqinfo = " + info.AppAddress + " app code = " + info.AppCode);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		TimeUseDebugUtils.timeUsageDebug(TAG+",exit genServiceUrl");
	}
}
