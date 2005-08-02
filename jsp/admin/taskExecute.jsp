<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Task Executed</title>
<content:css name="main" browserSpecific="true" />
<content:js name="common" />
<content:copyright visible="false" />
<body>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<div class="updateHdr">Scheduled Task Executed</div>
<br />
<c:if test="${empty exception}">
The ${task.name} Scheduled Task (${task.className}) has been successfully executed.<br />
</c:if>
<c:if test="${!empty exception}">
The ${task.name} Scheduled Task (${task.clasName}) encountered an error and did not complete successfully.<br />
The stack dump is as follows:<br />
<pre>
<fmt:stack exception="${exception}" />
</pre>
<br />
<c:if test="${!empty exception.rootCause}">
The root cause is as follows:<br />
<pre>${exception.rootCause.class.name}<br />
<fmt:stack exception="${exception.rootCause}" />
</pre>
</c:if>
</c:if>
<br />
To return to the <content:airline /> System Diagnostics page, <el:cmd url="diag" className="sec bld">Click here</el:cmd>.<br />
<br />
<content:copyright />
</div>
</body>
</html>
