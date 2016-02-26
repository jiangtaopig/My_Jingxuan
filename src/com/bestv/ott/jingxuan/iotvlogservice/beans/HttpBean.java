package com.bestv.ott.jingxuan.iotvlogservice.beans;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

public class HttpBean {
	private HttpGet httpGet;
	private HttpPost httpPost;
	private HttpResponse response;

	public HttpBean(){}
	
	public HttpBean(HttpGet httpGet, HttpResponse response) {
		super();
		this.httpGet = httpGet;
		this.response = response;
	}
	
	public HttpBean(HttpPost httpPost, HttpResponse response) {
		super();
		this.httpPost = httpPost;
		this.response = response;
	}
	
	public HttpResponse getResponse() {
		return response;
	}

	public void setResponse(HttpResponse response) {
		this.response = response;
	}

	public HttpGet getHttpGet() {
		return httpGet;
	}

	public void setHttpGet(HttpGet httpGet) {
		this.httpGet = httpGet;
	}

	public HttpPost getHttpPost() {
		return httpPost;
	}

	public void setHttpPost(HttpPost httpPost) {
		this.httpPost = httpPost;
	}
	
	public void abort(){
		if(httpGet != null)httpGet.abort();
		if(httpPost != null)httpPost.abort();
	}
}
