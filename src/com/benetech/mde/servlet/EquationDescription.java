package com.benetech.mde.servlet;

import gov.nasa.ial.mde.util.ResourceUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.benetech.mde.bean.EquationDescriptionBean;
import com.benetech.mde.bean.EquationDescriptionParamsBean;
import com.benetech.mde.bean.JSONResponseBean;
import com.benetech.mde.util.EquationUtil;


@WebServlet("/EquationDescription")
public class EquationDescription extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public EquationDescription() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	    response.setHeader("Cache-Control", "no-cache");
	    response.setHeader("Pragma", "no-cache");
	    response.setContentType("text/javascript");
	    String equation = request.getParameter("equation");
	    EquationDescriptionParamsBean data = EquationUtil.getEquationDescriptionParamsBean(equation);
	    String graphFilePath = getServletContext().getRealPath("/") + "data/graph.svg";
	    ResourceUtil.saveFile(graphFilePath, data.getSvg().getBytes());
	    EquationDescriptionBean base = new EquationDescriptionBean(data.getEquation(), data.getDescription());
	    PrintWriter out = response.getWriter();
	    JSONResponseBean resp = EquationUtil.getJSONResponseBean(true, base);
	    JSONObject respJson = new JSONObject(resp);
	    try{
	    	respJson.getJSONObject("data").remove("params");
	    	respJson.getJSONObject("data").put("params", data.getParams());
	    }catch(JSONException e){
	    	e.printStackTrace();
	    }
	    out.println(respJson);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
	

}
