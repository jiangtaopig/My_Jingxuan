package com.bestv.ott.jingxuan.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.bestv.ott.jingxuan.data.BaseRequest;
import com.bestv.ott.jingxuan.livetv.OttContextMgr;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.utils;
import com.bestv.ott.jingxuan.util.Constants.Keys;

/**
 * @author hu.fuyi
 * @param <T>
 * 
 */
public abstract class AbstractInvoker<T> implements IHttpInvoker<T> {

	protected enum RequestType {
		Get, Post
	}

	protected Object data;

	protected AbstractInvoker() {
	}

	protected AbstractInvoker(Object d) {
		this.data = d;
	}

	protected List<NameValuePair> getReqParams(BaseRequest req) {

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		try {
			req.setUserID(OttContextMgr.getInstance().getUserProfile().getUserID());
			req.setUserGroup(OttContextMgr.getInstance().getUserProfile()
					.getUserGroup());
			req.setTvID(OttContextMgr.getInstance().getTVID());
			req.setTvProfile(OttContextMgr.getInstance().getTVProfile());

			params.add(new BasicNameValuePair(Keys.UserID, req.getUserID()));
			params.add(new BasicNameValuePair(Keys.TVID, req.getTvID()));
			params.add(new BasicNameValuePair(Keys.TVProfile, req
					.getTvProfile()));
			params.add(new BasicNameValuePair(Keys.UserGroup, req
					.getUserGroup()));
			params.add(new BasicNameValuePair(Keys.PageSize, req.getPageSize()
					+ ""));
			params.add(new BasicNameValuePair(Keys.PageIndex, req
					.getPageIndex() + ""));
			
			String cdnAddress = OttContextMgr.getInstance().getCDNAddress();
			if (utils.isNotNull(cdnAddress)){
				params.add(new BasicNameValuePair(OttContextMgr.CDN_ADDRESS, cdnAddress));
			}
			
			String bmsUserToken = OttContextMgr.getInstance().getBmsUserToken();
			if (utils.isNull(bmsUserToken)){
				bmsUserToken = "";
			}
			
			params.add(new BasicNameValuePair("BmsUserToken", bmsUserToken));
			
			params.add(new BasicNameValuePair("UserToken", OttContextMgr.getInstance().getUserProfile().getUserToken()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return params;
	}

	public T invoke() {

		Object result = null;
		switch (getReqType()) {
			case Get :
				result = HttpUtils.get(getUrl(), getParams(), Constants.timeOut);
				break;
			case Post :
				result = HttpUtils.post(getUrl(), getParams(), Constants.timeOut);
				break;
		}

		return (T) result;
	}
}
