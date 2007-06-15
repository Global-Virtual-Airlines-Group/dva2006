<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> TeamSpeak 2 Configuration Updated</title>
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
<c:if test="${!empty server}">
<c:if test="${isUpdate}">
<!-- Virtual Server Updated -->
<div class="updateHdr">TeamSpeak 2 Virtual Server Updated</div>
<br />
The TeamSpeak 2 Virtual Server <span class="sec bld">${server.name}</span> on port ${server.port} has been 
successfully updated.<br />
<c:if test="${!empty msgs}">
<br />
<span class="small">
<c:forEach var="msg" items="${msgs}">
${msg}<br />
</c:forEach>
</span>
</c:if>
</c:if>
<c:if test="${isDelete}">
<!-- Virtual Server Deleted -->
<div class="updateHdr">TeamSpeak 2 Virtual Server Deleted</div>
<br />
The TeamSpeak 2 Virtual Server <span class="sec bld">${server.name}</span> on port ${server.port} has been 
deleted. All existing client credentials have been removed from the database.<br />
</c:if>
<br />
To return to the list of TeamSpeak 2 virtual servers, <el:cmd url="ts2servers" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${!empty channel}">
<c:if test="${isUpdate}">
<div class="updateHdr">TeamSpeak 2 Voice Channel Updated</div>
<br />
The TeamSpeak 2 Voice Channel <span class="sec bld">${channel.name}</span> has been successfully updated.<br />
</c:if>
<c:if test="${isDelete}">
<div class="updateHdr">TeamSpeak 2 Voice Channel Deleted</div>
<br />
The TeamSpeak 2 Voice Channel <span class="sec bld">${channel.name}</span> has been deleted.<br />
</c:if>
To return to the list of TeamSpeak 2 voice channels, <el:cmd url="ts2channels" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
