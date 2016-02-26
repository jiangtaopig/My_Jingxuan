package com.bestv.ott.jingxuan.net;

import java.util.List;

import org.apache.http.NameValuePair;

import com.bestv.ott.jingxuan.data.ChannelRequest;
import com.bestv.ott.jingxuan.util.DataConfig;

public class JXCategoryInvoker extends AbstractInvoker<BesTVHttpResult> {
	public JXCategoryInvoker(Object d) {
		super(d);
	}

	public RequestType getReqType() {
		return RequestType.Get;
	}

	public String getUrl() {
		return DataConfig.getJXCategoryUrl();
	}

	public List<NameValuePair> getParams() {
		ChannelRequest req = (ChannelRequest) data;
		List<NameValuePair> params = getReqParams(req);
		//params.add(new BasicNameValuePair("Version", "1210"));	
		return params;
	}
}
