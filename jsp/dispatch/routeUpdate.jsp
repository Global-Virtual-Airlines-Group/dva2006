<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ACARS Dispatcher Route Updated</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %>
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${isDelete}">
<!-- Route Deleted Message -->
<div class="updateHdr">ACARS Dispatcher Route Deleted</div>
<br />
This <content:airline /> ACARS Dispatcher route has been deleted from the database, and is no longer available 
for use by Dispatchers or Pilots.<br />
<br />
</c:when>
<c:when test="${isCreate}">
<div class="updateHdr">ACARS Dispatcher Route Created</div>
<br />
This <content:airline /> ACARS Dispatcher route has been added to the database, and is now avialable for use by 
Dispatchers and Pilots.<br />
<br />
To view this Dispatch route, <el:cmd url="dsproute" link="${route}" className="sec bld">Click Here</el:cmd>.<br />
To plot another route, <el:cmd url="routeplot" className="sec bld">Click Here</el:cmd>.<br />
</c:when>
</c:choose>
To return to the list of ACARS Dispatcher routes, <el:cmd url="dsproutes" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
