package com.benetech.mde;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class HelloServlet
 */
@WebServlet("/HelloServlet")
public class HelloServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HelloServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String name = request.getParameter("name");
		PrintWriter pw = response.getWriter();
		pw.write("Hello " + name);
		pw.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">" +
		"<html>" +
		"<head>" +
		"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">" +
		"<title>Insert title here</title>" +
		"<script type=\"text/javascript\" src=\"http://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML\"></script>" +
		"</head>" +
		"<body>" +
		"<math xmlns=\"http://www.w3.org/1998/Math/MathML\" display=\"block\">" +
		  "<mrow>" +
		    "<mi>f</mi>" +
		    "<mrow>" +
		      "<mo>(</mo>" +
		      "<mi>a</mi>" +
		      "<mo>)</mo>" +
		    "</mrow>" +
		    "<mo>=</mo>" +
		    "<msub>" +
		      "<mo>&#x222E;</mo>" +
		      "<mrow>" +
		        "<mi>&#x3B3;</mi>" +
		      "</mrow>" +
		    "</msub>" +
		    "<mfrac>" +
		      "<mrow>" +
		        "<mi>f</mi>" +
		        "<mo>(</mo>" +
		        "<mi>z</mi>" +
		        "<mo>)</mo>" +
		      "</mrow>" +
		      "<mrow>" +
		        "<mi>z</mi>" +
		        "<mo>&#x2212;</mo>" +
		        "<mi>a</mi>" +
		      "</mrow>" +
		    "</mfrac>" +
		    "<mi>d</mi>" +
		    "<mi>z</mi>" +
		  "</mrow>" +
		"</math>" +
		"</body>" +
		"</html>");
		pw.close();
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
