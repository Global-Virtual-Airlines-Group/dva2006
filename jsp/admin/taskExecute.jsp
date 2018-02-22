<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Task Executed</title>
<content:css name="main" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
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
<br />
The stack dump is as follows:<br />
<pre>
<fmt:stack exception="${ex}" />
</pre>
<br />
<c:if test="${!empty ex.cause}">
This is the root cause of the exception: <b>${ex.cause.getClass().name}</b><br />
<pre>
<fmt:stack exception="${ex.cause}" />
</pre>
<br />
</c:if>
</c:if>
<br />
To return to the <content:airline /> System Diagnostics page, <el:cmd url="diag" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
