package com.bestv.ott.jingxuan.net;

import java.util.List;

import org.apache.http.NameValuePair;

import com.bestv.ott.jingxuan.net.AbstractInvoker.RequestType;

/**
 * @author xu.jian
 * 
 */
public interface IHttpInvoker<T> {

	RequestType getReqType();

	List<NameValuePair> getParams();

	String getUrl();
	
	T invoke();
}
