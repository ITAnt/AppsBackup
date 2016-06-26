package com.onion.appsbackup.model;

import java.io.Serializable;

import com.fasterxml.jackson.databind.JsonNode;

public class ApiResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private String code;

	private String state;

	private String message;

	private JsonNode content;

	/**
	 * 
	 * @return 请求的状态
	 */
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 
	 * @return API返回的内容
	 */
	public JsonNode getContent() {
		return content;
	}

	public void setContent(JsonNode content) {
		this.content = content;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
