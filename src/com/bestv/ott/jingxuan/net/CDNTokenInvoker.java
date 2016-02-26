package com.bestv.ott.jingxuan.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.bestv.ott.jingxuan.livetv.OttContextMgr;
import com.bestv.ott.jingxuan.util.DataConfig;
import com.bestv.ott.jingxuan.util.Constants.Keys;

/**
 * @author hu.fuyi
 * 
 */
public class CDNTokenInvoker extends AbstractInvoker<BesTVHttpResult> {

	public CDNTokenInvoker(Object d) {
		super(d);
	}

	public CDNTokenInvoker() {
	}

	public RequestType getReqType() {
		return RequestType.Get;
	}

	public String getUrl() {
		return DataConfig.getServiceUrl() + "controller=Live&action=QueryCDNToken";
	}

	public List<NameValuePair> getParams() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		try {
			params.add(new BasicNameValuePair(Keys.UserID, OttContextMgr
					.getInstance().getUserProfile().getUserID()));
			params.add(new BasicNameValuePair(Keys.UserGroup, OttContextMgr
					.getInstance().getUserProfile().getUserGroup()));
			params.add(new BasicNameValuePair(Keys.UserToken, OttContextMgr
					.getInstance().getUserProfile().getUserToken()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return params;
	}
}
