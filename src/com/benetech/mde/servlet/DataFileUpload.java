package com.benetech.mde.servlet;

import gov.nasa.ial.mde.solver.symbolic.AnalyzedData;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.json.JSONException;
import org.json.JSONObject;

import com.benetech.mde.bean.EquationDescriptionBean;
import com.benetech.mde.bean.EquationDescriptionFileDataBean;
import com.benetech.mde.bean.JSONResponseBean;
import com.benetech.mde.util.EquationUtil;

@WebServlet("/DataFileUpload")
//@MultipartConfig(location="C:\\Documents and Settings\\jliao\\My Documents", fileSizeThreshold=512*512, maxFileSize=512*512*5, maxRequestSize=512*512*5*5)
@MultipartConfig(location="/", fileSizeThreshold=512*512, maxFileSize=512*512*5, maxRequestSize=512*512*5*5)
public class DataFileUpload extends HttpServlet {
	private static final long serialVersionUID = 2L;
	
	public DataFileUpload() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ServletContext context= getServletContext();
		RequestDispatcher rd= context.getRequestDispatcher("/WEB-INF/fileUpload.html");
		rd.forward(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	    resp.setHeader("Cache-Control", "no-cache");
	    resp.setHeader("Pragma", "no-cache");
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		Collection<Part> parts = req.getParts();
		for(Part part : parts) 
			part.write("datafile");
		//String filePath = this.getServletContext().getRealPath("datafile");
		List<AnalyzedData> data = EquationUtil.getAnalyzedData("/datafile");
		EquationDescriptionFileDataBean bean = EquationUtil.getEquationDescriptionFileDataBean(data);
		EquationDescriptionBean base = new EquationDescriptionBean(null, bean.getDescription());
	    JSONResponseBean respData = EquationUtil.getJSONResponseBean(true, base);
	    JSONObject respJson = new JSONObject(respData);
	    try{
	    	respJson.getJSONObject("data").put("column", bean.getColumn());
	    	respJson.getJSONObject("data").put("table", bean.getTable());
	    }catch(JSONException e){
	    	e.printStackTrace();
	    }
	    out.println(respJson);
	}
	
}
