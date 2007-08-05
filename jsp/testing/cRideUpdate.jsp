<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Check Ride</title>
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
<c:when test="${isScore}">
<!-- Check Ride Scored -->
<div class="updateHdr">Check Ride Scored</div>
<br />
This ${checkRide.equipmentType} for ${pilot.name} has been successfully scored. An e-mail
message has been sent to ${pilot.rank} ${pilot.lastName} notifying him or her of the scoring
of this Check Ride.<br />
<br />
To view the Pilot Profile, <el:cmd url="profile" link="${pilot}">Click here</el:cmd>.<br />
To return to the Examination Queue, <el:cmd url="examqueue">Click Here</el:cmd>.<br />
</c:when>
<c:when test="${isAssign}">
<!-- Check Ride Assigned -->
<div class="updateHdr">Check Ride Assigned</div>
<br />
This <content:airline /> ${checkRide.name} Check Ride has been assigned to ${pilot.name}, 
and an e-mail message has been sent to the Pilot.<br />
</c:when>
<c:when test="${isRideAlreadyAssigned && (empty tx)}">
<div class="updateHdr">Check Ride Pending</div>
<br />
A ${checkRide.equipmentType} Check Ride is currently pending for ${pilot.name}. No new Check Rides can 
be assigned while one is currently pending.<br />
</c:when>
<c:when test="${!empty txt}">
<div class="updateHdr">Transfer Request Pending</div>
<br />
An Equipment Program Transfer Request to the ${tx.equipmentType} is currently pending for ${pilot.name}. No new
Check Rides be assigned while a Transfer Request is pending.<br />
</c:when>
</c:choose>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
