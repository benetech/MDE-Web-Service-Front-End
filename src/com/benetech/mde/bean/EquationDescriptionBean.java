package com.benetech.mde.bean;

public class EquationDescriptionBean {
	private String equation;
	private String description;
	
	public EquationDescriptionBean() {
		super();
		// TODO Auto-generated constructor stub
	}
	public EquationDescriptionBean(String equation) {
		super();
		this.equation = equation;
	}
	
	public EquationDescriptionBean(String equation, String description) {
		super();
		this.equation = equation;
		this.description = description;
	}
	public String getEquation() {
		return equation;
	}
	public void setEquation(String equation) {
		this.equation = equation;
	}
	public String getDescription() {
		return description;			
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
}
