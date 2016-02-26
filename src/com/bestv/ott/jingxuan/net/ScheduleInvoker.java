package com.bestv.ott.jingxuan.net;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.bestv.ott.jingxuan.data.ScheduleRequest;
import com.bestv.ott.jingxuan.net.AbstractInvoker;
import com.bestv.ott.jingxuan.net.BesTVHttpResult;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.DataConfig;
import com.bestv.ott.jingxuan.util.Constants.Keys;

import android.util.Log;

/**
 * @author hu.fuyi
 * 
 */
public class ScheduleInvoker extends AbstractInvoker<BesTVHttpResult> {

	public ScheduleInvoker(Object d) {
		super(d);
	}

	public RequestType getReqType() {
		return RequestType.Get;
	}

	public String getUrl() {
		ScheduleRequest req = (ScheduleRequest) data;
		
		if (req.getType() == Constants.CHANNEL_LIANLIANKAN){
			return DataConfig.getLianLianKanUrl();
		}else{
			return DataConfig.getScheduleUrl();
		}
	}

	public List<NameValuePair> getParams() {
		ScheduleRequest req = (ScheduleRequest) data;
		List<NameValuePair> params = getReqParams(req);
		params.add(new BasicNameValuePair(Keys.ChannelNo, req.getChannelCode()));
		params.add(new BasicNameValuePair(Keys.StartDate, req.getStartDate()));
		params.add(new BasicNameValuePair(Keys.EndDate, req.getEndDate()));
		params.add(new BasicNameValuePair("Version", "1201"));

		Log.d("harish1", "start date = " + req.getStartDate() + " end date = " + req.getEndDate());
		return params;
	}

}
