<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Duplicate Pilots Merged</title>
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
<div class="updateHdr">Duplicate Pilots Merged</div>
<br />
The following Pilots' Examinations, Check Rides and Flight Reports have been merged under ${pilot.name}:<br />
<br />
<c:forEach var="usr" items="${oldPilots}">
<el:cmd url="profile" link="${usr}" className="bld">${usr.name}</el:cmd> <c:if test="${!empty usr.pilotCode}">
<span class="sec bld">${usr.pilotCode}</span> </c:if>(${usr.rank}, ${usr.equipmentType})<br />
</c:forEach>
<br />
<c:if test="${updatePwd}">${pilot.name}'s password has been successfully reset.<br />
<br /></c:if>
<c:if test="${addUser}">${pilot.name} has been added into the primary User Authenticator.<br />
<br /></c:if>
To view ${pilot.name}'s Pilot Profile, <el:cmd url="profile" link="${pilot}" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
