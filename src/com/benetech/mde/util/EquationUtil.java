package com.benetech.mde.util;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;

import com.benetech.mde.bean.EquationDescriptionBean;
import com.benetech.mde.bean.EquationDescriptionFileDataBean;
import com.benetech.mde.bean.EquationDescriptionParamsBean;
import com.benetech.mde.bean.JSONResponseBean;

import gov.nasa.ial.mde.describer.Describer;
import gov.nasa.ial.mde.io.TextDataFileParser;
import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.Solver;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedData;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedItem;
import gov.nasa.ial.mde.ui.graph.CartesianGraph;

public class EquationUtil {
	public static String getMathDescription(String equation) {
    	return getDescription(equation);
    }
	
	public static String getMathDescription(AnalyzedItem data){
		return getDescription(data);
	}
	
	public static String getDescription(Object data){
		String description = null;
    	MdeSettings currentSettings = new MdeSettings("myAppsMdeProperties");
        Solver solver = new Solver();
        Describer describer = new Describer(solver, currentSettings);
        describer.setOutputFormat(Describer.TEXT_OUTPUT);
        if(data instanceof String)
        	solver.add((String)data);
        else if(data instanceof AnalyzedData)
        	solver.add((AnalyzedItem)data);
        solver.solve();
        //solver.get(0).getAnalyzedItem().getFeatures();
        if (solver.anyDescribable())
            description = describer.getDescriptions("standards");
        else 
        	description = "Equation `" + data + " ` is not supported by MDE.";
        solver.removeAll(); 
    	return description;
	}
	
	public static List<AnalyzedData> getAnalyzedData(String fileName){
		File file = new File(fileName);        
        TextDataFileParser  fileParser = new TextDataFileParser(file);
        AnalyzedData data = null;
        List<AnalyzedData> list = null;
		try {
			list = fileParser.parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	
	public static EquationDescriptionParamsBean getEquationDescriptionParamsBean(String equation){
		EquationDescriptionParamsBean bean = new EquationDescriptionParamsBean();
    	MdeSettings currentSettings = new MdeSettings("myAppsMdeProperties");
        Solver solver = new Solver();
        Describer describer = new Describer(solver, currentSettings);
        describer.setOutputFormat(Describer.TEXT_OUTPUT);

        solver.add((String)equation);
        solver.solve();
        
        bean.setEquation(equation);
        bean.setSvg(getGraphSVG(solver));
        
        if (solver.anyDescribable())
            bean.setDescription(describer.getDescriptions("standards"));
        else 
        	bean.setDescription("Equation `" + equation + " ` is not supported by MDE.");
        AnalyzedItem item = solver.get(0).getAnalyzedItem();
        if(item instanceof AnalyzedEquation){
        	AnalyzedEquation ae = (AnalyzedEquation)item;
        	HashMap<String, String> params = new HashMap<String, String>();
        	for(String key : ae.getParameters())
        		params.put(key, String.valueOf(ae.getParameterValue(key)));
        	bean.setParams(params);	
        }
        solver.removeAll(); 
        return bean;        
	}
	
	public static EquationDescriptionBean getEquationDescriptionBean(String equation){
		EquationDescriptionBean bean = new EquationDescriptionBean(equation);
		bean.setDescription(getMathDescription(equation));
		return bean;
	}
	public static EquationDescriptionFileDataBean getEquationDescriptionFileDataBean(List<AnalyzedData>data){
		EquationDescriptionFileDataBean bean = new EquationDescriptionFileDataBean();
		int columns = data.size();
		int rows = data.get(0).getDataSize();
		double[][] table = new double[rows][columns + 1];
		String [] column = new String[columns + 1];
		int i = 1;
		double [] x = data.get(0).getXValues();
		for(int k = 0; k < x.length; k++)
			table[k][0] = x[k];
		column[0] = data.get(0).getXName();
		for(AnalyzedData element : data){
			double [] y  = element.getYValues();
			for(int j = 0; j < y.length; j ++)
				table[j][i] = y[j];
			column[i] = element.getYName();
			i++;
		}
		bean.setTable(table);
		bean.setColumn(column);
		bean.setDescription(getMathDescription(data.get(0)));
		return bean;
	}
	
	public static EquationDescriptionBean getEquationDescriptionBean(AnalyzedItem data){
		EquationDescriptionBean bean = new EquationDescriptionBean();
		bean.setDescription(getMathDescription(data));
		return bean;
	}
	
	public static JSONResponseBean getJSONResponseBean(boolean success, Object data){
		JSONResponseBean bean = new JSONResponseBean(success, data);
		return bean;
	}
	
	public static String getGraphSVG(Solver solver){
		MdeSettings currentSettings = new MdeSettings("myAppsMdeProperties");
		CartesianGraph grapher = new CartesianGraph(solver, currentSettings);
		JFrame window = new JFrame("Tutorial_CartesianGraph");
        window.getContentPane().add(grapher);
        window.pack();
		if (solver.anyGraphable()) {
			return grapher.getSVG();
		}else{
			return null;
		}
	}
}
