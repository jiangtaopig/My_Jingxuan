package com.bestv.ott.jingxuan.net;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.bestv.ott.jingxuan.data.ChannelRequest;
import com.bestv.ott.jingxuan.util.DataConfig;

public class AllJXScheduleInvoker extends AbstractInvoker<BesTVHttpResult> {
	public AllJXScheduleInvoker(Object d) {
		super(d);
	}

	public RequestType getReqType() {
		return RequestType.Get;
	}

	public String getUrl() {
		return DataConfig.getAllJXScheduleUrl();
	}

	public List<NameValuePair> getParams() {
		ChannelRequest req = (ChannelRequest) data;
		List<NameValuePair> params = getReqParams(req);
		String endDate = (String) req.getExtraData();
		params.add(new BasicNameValuePair("EndDate", endDate));
		//params.add(new BasicNameValuePair("IncludeURL", "1"));
		return params;
	}
}
