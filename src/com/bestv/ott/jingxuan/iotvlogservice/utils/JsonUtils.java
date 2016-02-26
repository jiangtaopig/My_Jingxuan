package com.bestv.ott.jingxuan.iotvlogservice.utils;

import org.json.JSONObject;

import com.bestv.ott.jingxuan.iotvlogservice.beans.JsonResult;

public class JsonUtils {
	
	public static JsonResult checkJsonValid(String json){
		JsonResult result=new JsonResult();
		
		try{
			JSONObject jsonObject = new JSONObject(json); 
			result.setValid(true);
			JSONObject responseObj = jsonObject.getJSONObject("Response");
			JSONObject headerObj = responseObj.getJSONObject("Header");
			JSONObject bodyObj = responseObj.getJSONObject("Body");
			result.setCompleted(true);
			if (null !=  headerObj){
				result.setRetCode(headerObj.getInt("RC"));
				result.setRetMsg(headerObj.getString("RM"));
				if (0 == result.getRetCode()) {
					result.setRetOK(true);
				}
				result.setObj(bodyObj);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			result.setRetCode(ResultDef.RESULT_FAILURE);
			result.setRetMsg(e.getStackTrace().toString());
		}
		
		return result;
	}
}
