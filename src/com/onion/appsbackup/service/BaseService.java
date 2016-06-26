package com.onion.appsbackup.service;

import java.io.IOException;

import android.util.Log;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseStream;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.onion.appsbackup.Constant;

public abstract class BaseService{
	
	private HttpUtils httpClient;
	
	/*protected DbUtils getDbUtils(){
		return WoyoliApp.getDbUtils();
	}*/
	
	protected HttpUtils getHttpClient(){
		if(httpClient==null){
			httpClient = new HttpUtils();	
		}
		httpClient.configTimeout(6 * 1000);
		return httpClient;
	}
		
	protected  String execute(HttpMethod method, String url, RequestParams params) throws HttpException, IOException{
		String apiUrl = "https://open.weibo.cn/oauth2/authorize";
		ResponseStream stream = getHttpClient().sendSync(method, apiUrl, params);
		String content = stream.readString();
		Log.d(Constant.DEBUG_TAG, content);
		return content;
	}
	
	protected  String executeWholeUrl(HttpMethod method, String url, RequestParams params) throws HttpException, IOException{
		ResponseStream stream = getHttpClient().sendSync(method, url, params);
		String content = stream.readString();
		Log.d(Constant.DEBUG_TAG, content);
		return content;
	}
	
}
