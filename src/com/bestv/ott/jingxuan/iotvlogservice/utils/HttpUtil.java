package com.bestv.ott.jingxuan.iotvlogservice.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.bestv.ott.jingxuan.iotvlogservice.beans.BesTVHttpResult;
import com.bestv.ott.jingxuan.iotvlogservice.beans.JsonResult;

import android.os.SystemClock;
import android.util.Log;


/**
 * add by leihong
 */
public class HttpUtil {
	private static final String TAG="HttpUtil";
	/**
	 * ContentCharset
	 */
	private static final String CHARSET = HTTP.UTF_8;

	/**
	 * 最大连接数
	 */
	public final static int MAX_TOTAL_CONNECTIONS = 400;
	/**
	 * 获取连接的最大等待时间
	 */
	public final static int WAIT_TIMEOUT = 30000;
	/**
	 * 每个路由最大连接数
	 */
	public final static int MAX_ROUTE_CONNECTIONS = 200;
	/**
	 * 连接超时时间
	 */
	public final static int CONNECT_TIMEOUT = 30000;
	/**
	 * 读取超时时间
	 */
	public final static int READ_TIMEOUT = 30000;

	private static HttpClient httpClient;
	
	private static final int HTTP_REQUEST_TYPE_GET=0;
	private static final int HTTP_REQUEST_TYPE_POST=1;

	/**
	 * 获取一个HttpClient
	 * 
	 * @param timeout
	 *            请求超时时间
	 * @return HttpClient
	 */
	public static synchronized HttpClient getHttpClient() {

		if (httpClient == null) {

			HttpParams httpParams = new BasicHttpParams();

			// 设置一些基本参数
			HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(httpParams, CHARSET);
			HttpProtocolParams.setUseExpectContinue(httpParams, false);
			HttpProtocolParams.setUserAgent(httpParams,
					"Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) "
							+ "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");

			// 设置最大连接数
			ConnManagerParams.setMaxTotalConnections(httpParams, MAX_TOTAL_CONNECTIONS);
			// 设置获取连接的最大等待时间
			ConnManagerParams.setTimeout(httpParams, WAIT_TIMEOUT);
			// 设置每个路由最大连接数
			ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(MAX_ROUTE_CONNECTIONS));

			// 设置连接超时时间
			HttpConnectionParams.setConnectionTimeout(httpParams, CONNECT_TIMEOUT);
			// 设置读取超时时间
			HttpConnectionParams.setSoTimeout(httpParams, READ_TIMEOUT);

			// 设置HttpClient支持HTTP模式
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			// schReg.register(new Scheme("https", SSLSocketFactory
			// .getSocketFactory(), 443));

			// 使用线程安全的连接管理来创建HttpClient
			ClientConnectionManager connMgr = new ThreadSafeClientConnManager(httpParams, registry);

			httpClient = new DefaultHttpClient(connMgr, httpParams);

		}
		return httpClient;
	}
	
	public static boolean connectUrl(String url, int timeout) {
		boolean ret = false;
		HttpURLConnection httpUrl = null;
		long beginTime=SystemClock.uptimeMillis();
		long endTime = beginTime;
		
		while ((endTime - beginTime) < timeout){
			try {
				httpUrl = (HttpURLConnection) new URL(url).openConnection();
				httpUrl.setConnectTimeout(timeout/3);
				httpUrl.connect();
				ret = true;
				break;
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				if (null != httpUrl) {
					httpUrl.disconnect();
				}
				httpUrl = null;
			}
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			endTime = SystemClock.uptimeMillis();
		}
		return ret;
	}
	
	public static BesTVHttpResult get(String url, List<NameValuePair> params, int timeout) {
		String param = buildGetParam(params);
		String getUrl=url;
		if (null != param) {
			getUrl = url + "?" + param;
		}
		return httpRequest(HTTP_REQUEST_TYPE_GET, getUrl, null, timeout);
	}
	
	public static BesTVHttpResult post(String url, List<NameValuePair> params, int timeout){
		return httpRequest(HTTP_REQUEST_TYPE_POST, url, params, timeout);
	}
	
	private static BesTVHttpResult httpRequest(int reqType, String url, List<NameValuePair> params, int timeout) {
		BesTVHttpResult result=new BesTVHttpResult();
		int tryTime=1;
		HttpResponse response=null;
		int statusCode=HttpStatus.SC_REQUEST_TIMEOUT;
		String statusMsg = ResultDef.RESULT_MSG_FAILURE_TIMEOUT;
		try {
			while (tryTime-- > 0){
				Log.d(TAG, "    try to request url(" + ((HTTP_REQUEST_TYPE_GET==reqType)?"get":"post") + ") : " + url + ", " + params);
				if (HTTP_REQUEST_TYPE_GET == reqType) {
					response = httpGet(url, timeout);
				} else {
					response = httpPost(url, params, timeout);
				}
				if (null != response){
					statusCode = response.getStatusLine().getStatusCode();
					statusMsg = response.getStatusLine().getReasonPhrase();
					if (HttpStatus.SC_OK == statusCode){
						break;
					}
				}
			} 
						
			if (HttpStatus.SC_OK == statusCode){
				String strResult = EntityUtils.toString(response.getEntity());
				Log.d(TAG, "" + strResult);
				result.setObj(strResult);
				JsonResult jsonResult=utils.checkJsonValid(strResult);
				result.setJsonResult(jsonResult);
				if (!jsonResult.isValid()) {
					result.setResultCode(ResultDef.RESULT_FAILURE_JSON_INVALID);
					result.setResultMsg(ResultDef.RESULT_MSG_FAILURE_JSON_INVALID);
				} else if (!jsonResult.isCompleted()) {
					result.setResultCode(ResultDef.RESULT_FAILURE_JSON_IMPERFECT);
					result.setResultMsg(ResultDef.RESULT_MSG_FAILURE_JSON_IMPERFECT);
				} else if (!jsonResult.isRetOK()) {
					result.setResultCode(ResultDef.RESULT_FAILURE_SERVICE_RET_ERROR);
					result.setResultMsg(ResultDef.RESULT_MSG_FAILURE_SERVICE);
				} else {
					result.setResultCode(ResultDef.RESULT_SUCCESS);
				}			
			} else if ((HttpStatus.SC_NOT_FOUND==statusCode) ||
						(null==response)){
				result.setResultCode(ResultDef.RESULT_FAILURE_HTTP_CAN_NOT_CONNECT);
				result.setResultMsg(ResultDef.RESULT_MSG_FAILURE_HTTP_CAN_NOT_CONNECT);
			} else if (HttpStatus.SC_REQUEST_TIMEOUT == statusCode) {
				result.setResultCode(ResultDef.RESULT_FAILURE_TIMEOUT);
				result.setResultMsg(ResultDef.RESULT_MSG_FAILURE_TIMEOUT);
			} else if (statusCode >= 400){
				result.setResultCode(ResultDef.RESULT_FAILURE_HTTP_FAILED);
				result.setResultMsg(ResultDef.RESULT_MSG_FAILURE_HTTP_FAILED);
			} else {
				result.setResultCode(ResultDef.RESULT_FAILURE);
				result.setResultMsg(statusMsg);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			result.setResultCode(ResultDef.RESULT_FAILURE);
			result.setResultMsg(ResultDef.RESULT_MSG_FAILURE_EXCEPTION);
		}
		return result;
	}

	public static HttpResponse httpPost(String url, List<NameValuePair> params, int timeout) {
		HttpResponse response=null;
		HttpPost httpRequest=new HttpPost(url);
		try {
			HttpEntity entity = new UrlEncodedFormEntity(params, "utf-8");
			httpRequest.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout/2); 
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout/2);
			response = client.execute(httpRequest);
		} catch (Throwable e) {
			e.printStackTrace();
		} 
		return response;
	}
		
	private static HttpResponse httpGet(String url, int timeout) {
		HttpResponse response=null;
		HttpGet httpRequest=new HttpGet(url);
		try {
			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000); 
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
			response = client.execute(httpRequest);
		} catch (Throwable e) {
			e.printStackTrace();
		} 
		return response;
	}
	
	private static String buildGetParam(List<NameValuePair> params) {
		StringBuffer sb = new StringBuffer();
		try {
			if (null != params) {
				for (NameValuePair item : params) {
					if (sb.length() > 0) {
						sb.append("&");
					}
					sb.append(item.getName())
					  .append("=")
					  .append(null == item.getValue() ? "" : URLEncoder
								.encode(item.getValue().toString(),
										CHARSET));
					   
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sb.toString();
	}
}
