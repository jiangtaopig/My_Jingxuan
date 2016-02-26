package com.bestv.ott.jingxuan.net;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import com.bestv.ott.framework.beans.JsonResult;
import com.bestv.ott.jingxuan.iotvlogservice.utils.FileUtils;
import com.bestv.ott.jingxuan.livetv.OttContextMgr;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.utils;
import com.bestv.ott.jingxuan.util.Constants.Errors;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.util.Log;

public class HttpUtils {
	private static final String TAG = "HttpUtils";
	public static final int HTTP_REQUEST_TYPE_GET = 0;
	public static final int HTTP_REQUEST_TYPE_POST = 1;
	private static final String CHARSET = "UTF-8";
	private static int TIMEOUT = 5 * 1000;
	private static String url = "http://www.baidu.com";

	public static final boolean HTTP_SUPPORT_GZIP = true;
	
	public static boolean connectUrl(String url, int timeout) {
		boolean ret = false;
		HttpURLConnection httpUrl = null;
		long beginTime = SystemClock.uptimeMillis();
		long endTime = beginTime;

		while ((endTime - beginTime) < timeout) {
			try {
				httpUrl = (HttpURLConnection) new URL(url).openConnection();
				httpUrl.setConnectTimeout(timeout / 3);
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

	public static BesTVHttpResult get(String errorCode, String[] tags,
			String url, List<NameValuePair> params, int timeout) {
		String param = buildGetParam(params);
		String getUrl = url;
		if (utils.isNotNull(param)) {
			getUrl = url + "?" + param;
		}
		return httpRequest(errorCode, HTTP_REQUEST_TYPE_GET, tags, getUrl,
				null, timeout);
	}

	public static BesTVHttpResult get(String url, List<NameValuePair> params,
			int timeout) {
		String param = buildGetParam(params);
		String getUrl = url;
		if (utils.isNotNull(param)) {
			getUrl = utils.getUrl(url) + param;
		}
		return httpRequest(HTTP_REQUEST_TYPE_GET, getUrl, null, timeout);
	}

	public static BesTVHttpResult post(String errorCode, String[] tags,
			String url, List<NameValuePair> params, int timeout) {
		return httpRequest(errorCode, HTTP_REQUEST_TYPE_POST, tags, url,
				params, timeout);
	}

	public static BesTVHttpResult post(String url, List<NameValuePair> params,
			int timeout) {
		return httpRequest(HTTP_REQUEST_TYPE_POST, url, params, timeout);
	}

	public static BesTVHttpResult httpRequest(String errorCode, int reqType,
			String[] tags, String url, List<NameValuePair> params, int timeout) {
		BesTVHttpResult result = null;
		result = httpRequest(reqType, url, params, timeout);
		if (Constants.RESULT_SUCCESS == result.getResultCode()) {
			result.setResultMsg(tags[0]);
		} else {
			String msg = result.getResultMsg();
			result.setResultMsg(tags[1] + msg);
			result.setErrorCode(errorCode);
		}
		return result;
	}

	public static BesTVHttpResult httpRequest(int reqType, String url,
			List<NameValuePair> params, int timeout) {
		BesTVHttpResult result = new BesTVHttpResult();
		int tryTime = 2;
		HttpResponse response = null;
		int statusCode = HttpStatus.SC_REQUEST_TIMEOUT;
		String statusMsg = Constants.RESULT_MSG_FAILURE_TIMEOUT;
		try {
			while (tryTime-- > 0) {
				Logger.d("    try to request url("
						+ ((HTTP_REQUEST_TYPE_GET == reqType) ? "get" : "post")
						+ ") : " + url + ", " + params);
				if (HTTP_REQUEST_TYPE_GET == reqType) {
					response = httpGet(url, timeout);
				} else {
					response = httpPost(url, params, timeout);
				}
				if (null != response) {
					statusCode = response.getStatusLine().getStatusCode();
					statusMsg = response.getStatusLine().getReasonPhrase();
					if (HttpStatus.SC_OK == statusCode) {
						break;
					}
					utils.send(OttContextMgr.getInstance().getContext(), Errors.RequestException);
				}
			}
			
			result.setHttpStatusCode(statusCode);

			if (HttpStatus.SC_OK == statusCode) {
				String strResult = readHttpResponse(response);
				//Log.d("harish4", "str result = " + strResult);
				result.setObj(strResult);
				JsonResult jsonResult = utils.checkJsonValid(strResult);
				result.setJsonResult(jsonResult);
				if (!jsonResult.isValid()) {
					result.setResultCode(Constants.RESULT_FAILURE_JSON_INVALID);
					result.setResultMsg(Constants.RESULT_MSG_FAILURE_JSON_INVALID);
				} else if (!jsonResult.isCompleted()) {
					result.setResultCode(Constants.RESULT_FAILURE_JSON_IMPERFECT);
					result.setResultMsg(Constants.RESULT_MSG_FAILURE_JSON_IMPERFECT);
				} else if (!jsonResult.isRetOK()) {
					result.setResultCode(Constants.RESULT_FAILURE_SERVICE);
					result.setResultMsg(Constants.RESULT_MSG_FAILURE_SERVICE);
				}
			} else if (HttpStatus.SC_REQUEST_TIMEOUT == statusCode) {
				result.setResultCode(Constants.RESULT_FAILURE_TIMEOUT);
				result.setResultMsg(Constants.RESULT_MSG_FAILURE_TIMEOUT);
			} else {
				result.setResultCode(Constants.RESULT_FAILURE);
				result.setResultMsg(statusMsg);
			}

		} catch (Throwable e) {//change by zjt for 16/1/29 java.lang.ExceptionInInitializerError
			e.printStackTrace();
			result.setResultCode(Constants.RESULT_FAILURE);
			result.setResultMsg(Constants.RESULT_MSG_FAILURE_EXCEPTION);
			utils.send(OttContextMgr.getInstance().getContext(), Errors.RequestException);
		}
		return result;
	}
	
	public static String readHttpResponse(HttpResponse response) throws ParseException, IOException {
		String ret="";
		HttpEntity httpEntity = response.getEntity();

		Header encodeHeader = httpEntity.getContentEncoding();
		if ((null!=encodeHeader) &&
			(encodeHeader.getValue().contains("gzip"))) {
			InputStream is= httpEntity.getContent();
			ret = FileUtils.readByInputStream(new GZIPInputStream(is));
			Logger.d(TAG, "response data support gzip length = " + ret.length());
		} else {
			ret = EntityUtils.toString(response.getEntity());
			Logger.d(TAG, "response data unsupport gzip length = " + ret.length());
		}
		return ret;
	}

	public static HttpResponse httpPost(String url, List<NameValuePair> params,
			int timeout) throws ClientProtocolException, IOException {
		HttpResponse response = null;
		HttpPost httpRequest = new HttpPost(url);
		
		if (HTTP_SUPPORT_GZIP){
		    httpRequest.addHeader("Accept-Encoding", "gzip");
		}
		
		HttpEntity entity = new UrlEncodedFormEntity(params, "utf-8");
		httpRequest.setEntity(entity);
		HttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				timeout);
		response = client.execute(httpRequest);
		return response;
	}

	private static HttpResponse httpGet(String url, int timeout)
			throws ClientProtocolException, IOException {
		HttpResponse response = null;
		HttpGet httpRequest = new HttpGet(url);
		
		if (HTTP_SUPPORT_GZIP){
		    httpRequest.addHeader("Accept-Encoding", "gzip");
		}
		
		HttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				timeout);
		response = client.execute(httpRequest);
		
		//Log.d("harish1", "date = " + response.getHeaders("Date"));
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
							.append(null == item.getValue()
									? ""
									: URLEncoder.encode(item.getValue()
											.toString(), CHARSET));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	public static boolean connectionNetwork() {
		boolean result = false;
		HttpURLConnection httpUrl = null;
		for (int i = 0; i < 3; i++) {
			try {
				httpUrl = (HttpURLConnection) new URL(url).openConnection();
				httpUrl.setConnectTimeout(TIMEOUT);
				httpUrl.connect();
				result = true;
			} catch (IOException e) {
				e.printStackTrace();
				result = false;
			} finally {
				if (null != httpUrl)
					httpUrl.disconnect();
				httpUrl = null;
			}
		}

		return result;
	}

	public static int checkNetworkStatus(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] allnetworkinfo = connectivityManager.getAllNetworkInfo();
		
		for (NetworkInfo info : allnetworkinfo){
			if (info != null && info.isConnected()){
				Log.d("harish", "active network info, name = " + info.getTypeName());
				if (info.getType() == ConnectivityManager.TYPE_WIFI){
					return info.getType();
				}else{
					return ConnectivityManager.TYPE_ETHERNET;
				}
			}
		}
		
		/*NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		if (wifiNetInfo != null){
		   Logger.d("active network info = " + wifiNetInfo.getTypeName() + " isavialable = " + wifiNetInfo.isAvailable()
				+ "is connected = " + wifiNetInfo.isConnected());
		}
		
		if (wifiNetInfo != null && wifiNetInfo.isConnected()){
			return ConnectivityManager.TYPE_WIFI;
		}
		
		NetworkInfo ethernetNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);

		if (ethernetNetInfo != null){
			   Logger.d("active network info = " + ethernetNetInfo.getTypeName() + " isavialable = " + ethernetNetInfo.isAvailable()
					+ "is connected = " + ethernetNetInfo.isConnected());
		}
		
		if (ethernetNetInfo != null && ethernetNetInfo.isConnected()){
			return ConnectivityManager.TYPE_ETHERNET;
		}*/
			
		// activeNetInfo.isAvailable();
		return -1;
	}
}
