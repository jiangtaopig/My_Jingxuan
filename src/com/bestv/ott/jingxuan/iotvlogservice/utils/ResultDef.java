package com.bestv.ott.jingxuan.iotvlogservice.utils;

public class ResultDef {
	public static final int RESULT_BESTV_SERVICE_SUCCESS=0;	
	
	public static final int RESULT_FAILURE_EXCEPTION=-99;
	public static final int RESULT_FAILURE_SERVICE_RET_ERROR=-98;
	public static final int RESULT_FAILURE_JSON_IMPERFECT=-97;
	public static final int RESULT_FAILURE_JSON_INVALID=-96;
	public static final int RESULT_FAILURE_HTTP_EXCEPTION=-95;	
	public static final int RESULT_FAILURE_HTTP_TIMEOUT=-94;
	public static final int RESULT_FAILURE_HTTP_FAILED=-93;
	public static final int RESULT_FAILURE_HTTP_CAN_NOT_CONNECT=-92;
	public static final int RESULT_FAILURE_TIMEOUT=-91;	
	public static final int RESULT_FAILURE_PARAMETER_ERROR = -90;
	public static final int RESULT_FAILURE = -1;
	public static final int RESULT_SUCCESS = 0;
	
	public static final String RESULT_MSG_FAILURE_DEFAULT="处理错误";
	public static final String RESULT_MSG_FAILURE_TIMEOUT="超时";
	public static final String RESULT_MSG_FAILURE_PARAMETER_ERROR = "请求参数出错";
	public static final String RESULT_MSG_FAILURE_EXCEPTION = "处理异常";
	
	public static final String RESULT_MSG_FAILURE_HTTP_CAN_NOT_CONNECT="连接不到服务器";
	public static final String RESULT_MSG_FAILURE_HTTP_FAILED="服务请求失败";
	public static final String RESULT_MSG_FAILURE_HTTP_TIMEOUT="服务请求超时";
	public static final String RESULT_MSG_FAILURE_HTTP_EXCEPTION = "服务请求异常";
	public static final String RESULT_MSG_FAILURE_JSON_INVALID = "服务返回数据无效";
	public static final String RESULT_MSG_FAILURE_JSON_IMPERFECT = "服务返回数据结构不完整";
	public static final String RESULT_MSG_FAILURE_SERVICE = "服务返回失败";
	
	public static String getResultMsg(int code) {
		String ret="";
		switch (code) {
		case RESULT_FAILURE:
			ret = RESULT_MSG_FAILURE_DEFAULT;
			break;
		case RESULT_FAILURE_PARAMETER_ERROR:
			ret = RESULT_MSG_FAILURE_PARAMETER_ERROR;
			break;
		case RESULT_FAILURE_TIMEOUT:
			ret = RESULT_MSG_FAILURE_TIMEOUT;
			break;
		case RESULT_FAILURE_HTTP_FAILED:
			ret = RESULT_MSG_FAILURE_HTTP_FAILED;
			break;
		case RESULT_FAILURE_HTTP_TIMEOUT:
			ret = RESULT_MSG_FAILURE_HTTP_TIMEOUT;
			break;
		case RESULT_FAILURE_HTTP_EXCEPTION:
			ret = RESULT_MSG_FAILURE_HTTP_EXCEPTION;
			break;
		case RESULT_FAILURE_JSON_INVALID:
			ret = RESULT_MSG_FAILURE_JSON_INVALID;
			break;
		case RESULT_FAILURE_JSON_IMPERFECT:
			ret = RESULT_MSG_FAILURE_JSON_IMPERFECT;
			break;
		case RESULT_FAILURE_SERVICE_RET_ERROR:
			ret = RESULT_MSG_FAILURE_SERVICE;
			break;
		case RESULT_FAILURE_EXCEPTION:
			ret = RESULT_MSG_FAILURE_EXCEPTION;
			break;
		}
		return ret;
	}
	
}
