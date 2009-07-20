<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> System Error</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="ex" value="${empty servlet_exception ? exception : servlet_exception}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr"><content:airline /> SYSTEM ERROR</div>
<br />
Oops. Something bad happened. Really, really bad - and I have no idea what to do. So you figure it out.<br />
<br />
This is the message: <b>${servlet_error}</b><br />
<br />
<c:if test="${!empty ex}">
<b>${ex.class.name}</b><br />
<pre><fmt:stack exception="${ex}" /></pre>
<br />
</c:if>
<c:choose>
<c:when test="${(ex.class.name == 'javax.servlet.ServletException') && (!empty ex.rootCause)}">
This is the root cause of the exception: <b>${ex.rootCause.class.name}</b> ${ex.rootCause.message}<br />
<pre><fmt:stack exception="${ex.rootCause}" /></pre>
<br />
</c:when>
<c:when test="${!empty ex.cause}">
This is the root cause of the exception: <b>${ex.cause.class.name}</b> ${ex.cause.message}<br />
<pre><fmt:stack exception="${ex.cause}" /></pre>
<br />
</c:when>
</c:choose>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
