<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ page isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="javax.servlet.ServletException" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> System Error</title>
<content:css name="main" browserSpecific="true" force="true" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<b>Oops. Something bad happened. Really, really bad. This is the message:</b><br />
<br />
<pre>${servlet_error}</pre><br />
<br />
This is the stack trace:<br />
<pre>
<% PrintWriter pw = new PrintWriter(out);
Throwable e = (Throwable) request.getAttribute("servlet_exception");
if ((e == null) && (exception != null))
	e = exception;

if (e != null)
	e.printStackTrace(pw); %>
</PRE>
<br />
<c:if test="${servlet_exception.class.name == 'javax.servlet.ServletException'}">
<% ServletException se = (ServletException) e;
if (se.getRootCause() != null) { %>
This is the root cause of the exception:<br />
<pre>
<% se.getRootCause().printStackTrace(pw); %>
</pre>
<% } %>
<br />
</c:if>
<content:copyright />
</div>
</body>
</html>
