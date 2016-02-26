package com.bestv.ott.jingxuan.iotvlogservice.beans;

public class JsonResult {
	private boolean valid=false;
	private boolean completed=false;
	private boolean retOK=false;
	public boolean isCompleted() {
		return completed;
	}
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	private int retCode=-1;
	private String retMsg="";
	private Object obj=null;
	
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public boolean isRetOK() {
		return retOK;
	}
	public void setRetOK(boolean retOK) {
		this.retOK = retOK;
	}
	public int getRetCode() {
		return retCode;
	}
	public void setRetCode(int retCode) {
		this.retCode = retCode;
	}
	public String getRetMsg() {
		return retMsg;
	}
	public void setRetMsg(String retMsg) {
		this.retMsg = retMsg;
	}
	public Object getObj() {
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}		
}
