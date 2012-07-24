package com.benetech.mde.bean;

public class EquationDescriptionFileDataBean extends EquationDescriptionBean{
	private double[][] table;
	private String [] column;
	public double[][] getTable() {
		return table;
	}

	public void setTable(double[][] table) {
		this.table = table;
	}

	public String[] getColumn() {
		return column;
	}

	public void setColumn(String[] column) {
		this.column = column;
	}	
}
