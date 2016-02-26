package com.bestv.ott.jingxuan.iotvlogservice.beans;

public class BesTVHttpResult {
	public static final int RESULT_CODE_EXCEPTION=-10000;
	private int resultCode=0;
	private String resultMsg="";
	private Object obj=null;
	private Object jsonResult=null;

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
}
