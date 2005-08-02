<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ page isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> System Error</title>
<content:css name="main" browserSpecific="true" force="true" />
</head>
<content:copyright visible="false" />
<body>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>
<c:set var="ex" value="${empty servlet_exception ? exception : servlet_exception}" scope="request" />

<!-- Main Body Frame -->
<div id="main">
<div class="updateHdr"><content:airline /> SYSTEM ERROR</div>
<br />
<b>Oops. Something bad happened. Really, really bad. This is the message:</b><br />
<br />
<pre>${servlet_error}</pre><br />
<br />
<c:if test="${!empty ex}">
This is the stack trace:<br />
<pre>
<fmt:stack exception="${ex}" />
</pre>
<br />
</c:if>
<c:if test="${!empty ex.cause}">
This is the root cause of the exception:<br />
<pre>
<fmt:stack exception="${ex.cause}" />
</pre>
<br />
</c:if>
<content:copyright />
</div>
</body>
</html>
