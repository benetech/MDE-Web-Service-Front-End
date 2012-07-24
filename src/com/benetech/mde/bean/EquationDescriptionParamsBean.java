package com.benetech.mde.bean;

import java.util.HashMap;

public class EquationDescriptionParamsBean extends EquationDescriptionBean{
	HashMap<String, String> params;
	
	public HashMap<String, String> getParams() {
		return params;
	}

	public void setParams(HashMap<String, String> params) {
		this.params = params;
	}
}
