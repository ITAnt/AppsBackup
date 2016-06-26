package com.onion.appsbackup.service;

import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

public class AuthService extends BaseService{
	
	
	
	public String getAccessToken(String code) {
		String response = null;
		RequestParams params = new RequestParams();
		try {
			//params.addBodyParameter("client_id", Constant.CONSUMER_KEY);
			
			response = execute(HttpMethod.POST, "", params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
}
