<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Task Executed</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Scheduled Task Executed</div>
<br />
<c:if test="${empty ex}">
The ${task.name} Scheduled Task (${task.className}) has been successfully executed.<br />
</c:if>
<c:if test="${!empty ex}">
The ${task.name} Scheduled Task (${task.className}) encountered an error and did not complete successfully.<br />
The stack dump is as follows:<br />
<pre>
<fmt:stack exception="${ex}" />
</pre>
<br />
<c:if test="${!empty ex.cause}">
This is the root cause of the exception: <b>${ex.cause.class.name}</b><br />
<pre>
<fmt:stack exception="${ex.cause}" />
</pre>
<br />
</c:if>
</c:if>
<br />
To return to the <content:airline /> System Diagnostics page, <el:cmd url="diag" className="sec bld">Click here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
