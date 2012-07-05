<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<!--
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>

</body>
-->
<jsp:useBean id="cart" scope="session" class="session.MathEquationBean" />

<jsp:setProperty name="cart" property="*" />
<%
    cart.processRequest();
%>

<FONT size = 5 COLOR="#CC0000">
<br> You have the following math equations selected:

<TABLE SUMMARY="MDE Descriptions" BORDER=1>
<%
    String[] items = cart.getItems();
    for (int i=0; i<items.length; i++) {
%>
<TR> <TD><% out.print(util.HTMLFilter.filter(items[i])); %></TD>
<TD><% out.print(cart.getMathDescription(items[i])); %></TD>
<%
    }
%>
</table> 
</FONT>

<hr>
<%@ include file ="index.html" %>
</html>