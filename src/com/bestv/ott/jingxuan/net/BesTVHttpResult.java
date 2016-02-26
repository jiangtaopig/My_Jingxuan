package com.bestv.ott.jingxuan.net;


public class BesTVHttpResult extends BaseResult {
	
	public static final int RESULT_CODE_EXCEPTION=-10000;
	private int resultCode=0;
	private String resultMsg="";
	private Object obj=null;
	private Object jsonResult=null;
	private String errorCode="";
	private int httpStatusCode=0;
	
	public int getResultCode() {
		return resultCode;
	}
	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
	public String getResultMsg() {
		return resultMsg;
	}
	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}

	public Object getObj() {
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}
	public Object getJsonResult() {
		return jsonResult;
	}
	public void setJsonResult(Object jsonResult) {
		this.jsonResult = jsonResult;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	public void setHttpStatusCode(int statuscode){
		httpStatusCode = statuscode;
	}
	
	public int getHttpStatusCode(){
		return httpStatusCode;
	}
}
