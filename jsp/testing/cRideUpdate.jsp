<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Check Ride</title>
<content:css name="main" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
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
This ${checkRide.equipmentType} for ${pilot.name} has been successfully scored. An e-mail message has been sent to ${pilot.rank.name} ${pilot.lastName} notifying him or her of the scoring of this Check Ride.<br />
<br />
To view the Pilot Profile, <el:cmd url="profile" link="${pilot}">Click here</el:cmd>.<br />
To return to the Examination Queue, <el:cmd url="examqueue">Click Here</el:cmd>.<br />
To return to the list of pending equipment program transfer requests, <el:cmd url="txrequests" className="sec bld">Click Here</el:cmd>.<br />
</c:when>
<c:when test="${isAssign && !isCurrency}">
<!-- Check Ride Assigned -->
<div class="updateHdr">Check Ride Assigned</div>
<br />
This <content:airline /> ${checkRide.name} Check Ride has been assigned to ${pilot.name}, and an e-mail message has been sent to the Pilot.<br />
<c:if test="${!empty script}">
<br />
The <span class="bld">${script.program}</span> program Check Ride script for the <span class="pri bld">${script.equipmentType}</span> has been used as the template for this Check Ride.<br /></c:if>
<br />
To return to the list of pending equipment program transfer requests, <el:cmd url="txrequests" className="sec bld">Click Here</el:cmd>.<br />
</c:when>
<c:when test="${isCurrency}">
<div class="updateHdr">Check Ride Assigned</div>
<br />
You have assigend yourself a currency Check Ride for the <span class="bld">${checkRide.equipmentType}</span> program, using the ${checkRide.aircraftType}. An e-mail message has been sent
to you with details of the Check Ride.<br />
<br />
To return to the Testing Center, <el:cmd url="testcenter" className="sec bld">Click Here</el:cmd>.<br />
</c:when>
<c:when test="${isRideAlreadyAssigned && (empty tx)}">
<div class="updateHdr">Check Ride Pending</div>
<br />
A ${checkRide.equipmentType} Check Ride is currently pending for ${pilot.name}. No new Check Rides can be assigned while one is currently pending.<br />
<br />
To return to the list of pending equipment program transfer requests, <el:cmd url="txrequests" className="sec bld">Click Here</el:cmd>.<br />
</c:when>
<c:when test="${!empty tx}">
<div class="updateHdr">Transfer Request Pending</div>
<br />
An Equipment Program Transfer Request to the ${tx.equipmentType} is currently pending for ${pilot.name}. No new Check Rides be assigned while a Transfer Request is pending.<br />
</c:when>
</c:choose>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
