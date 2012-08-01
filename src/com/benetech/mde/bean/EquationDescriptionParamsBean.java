package com.benetech.mde.bean;

import java.util.HashMap;

public class EquationDescriptionParamsBean extends EquationDescriptionBean{
	HashMap<String, String> params;
	String svg;
	
	public HashMap<String, String> getParams() {
		return params;
	}

	public void setParams(HashMap<String, String> params) {
		this.params = params;
	}

	public String getSvg() {
		return svg;
	}

	public void setSvg(String svg) {
		this.svg = svg;
	}
}
