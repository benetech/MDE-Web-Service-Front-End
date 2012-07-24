package com.benetech.mde.bean;

public class JSONResponseBean {
	private String success;
	private Object data;
	
	public JSONResponseBean(boolean success, Object data) {
		super();
		this.success = (success == true) ? "true" : "false";
		this.data = data;
	}
	public String getSuccess() {
		return success;
	}
	public void setSuccess(String success) {
		this.success = success;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}	
}
