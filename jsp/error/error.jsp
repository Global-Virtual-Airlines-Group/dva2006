<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> System Error</title>
<content:css name="main" />
<content:js name="common" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
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
<c:if test="${logStackDump && (!empty ex)}">
<b>${ex.getClass().name}</b><br />
<pre><fmt:stack exception="${ex}" /></pre>
<br />
</c:if>
<c:choose>
<c:when test="${(ex.getClass().name == 'javax.servlet.ServletException') && (!empty ex.rootCause)}">
This is the root cause of the exception: <b>${ex.rootCause.getClass().name}</b> ${ex.rootCause.message}<br />
<pre><fmt:stack exception="${ex.rootCause}" /></pre>
<br />
</c:when>
<c:when test="${!empty ex.cause}">
This is the root cause of the exception: <b>${ex.cause.getClass().name}</b> ${ex.cause.message}<br />
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
