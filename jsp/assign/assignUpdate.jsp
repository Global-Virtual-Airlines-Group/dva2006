<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Flight Assignment Updated</title>
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
<c:when test="${isCreate}">
<div class="updateHdr">Flight Assignment Created</div>
<br />
This <content:airline /> Flight Assigment has been successfully saved in the database.<br />
<br />
<fmt:int value="${fn:sizeof(assign.flights)}" /> draft Flight Reports have been pre-populated within
our database.<br />
<br />
<c:forEach var="pirep" items="${assign.flights}">
<el:cmd className="bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd> - <el:cmd url="routeplot" link="${pirep}">Plot Route</el:cmd><br />
</c:forEach>
</c:when>
<c:when test="${isDelete}">
<div class="updateHdr">Flight Assignment Deleted</div>
<br />
This <content:airline /> Flight Assigment has been successfully removed from the database.<br />
<c:if test="${!empty flightsDeleted}">
<fmt:int value="${flightsDeleted}" /> Flight Reports were deleted from the database.<br />
</c:if>
<c:if test="${!empty flightsUpdated}">
<fmt:int value="${flightsUpdated}" /> Flight Reports were updated in the database.<br />
</c:if>
</c:when>
<c:when test="${isRelease}">
<div class="updateHdr">Flight Assignment Released</div>
<br />
This <content:airline /> Flight Assigment has been successfully released.<br />
<c:if test="${!empty flightsDeleted}">
<fmt:int value="${flightsDeleted}" /> Flight Reports were deleted from the database.<br />
</c:if>
<c:if test="${!empty flightsUpdated}">
<fmt:int value="${flightsUpdated}" /> Flight Reports were updated in the database.<br />
</c:if>
</c:when>
<c:when test="${isReserve}">
<div class="updateHdr">Flight Assignment Reserved</div>
<br />
This <content:airline /> Flight Assigment has been successfully reserved by ${pilot.rank.name} ${pilot.name}.<br />
<br />
<fmt:int value="${fn:sizeof(assign.flights)}" /> draft Flight Reports have been pre-populated within
our database.<br />
</c:when>
<c:otherwise>
<div class="updateHdr">Flight Assignment Updated</div>
<br />
This <content:airline /> Flight Assigment has been successfully saved in the database.<br />
</c:otherwise>
</c:choose>
<c:if test="${isPreApprove}">
<br />
<span class="bld">This Flight Assignment for ${assignPilot.name} contains a pre-approved flight leg outside the regular 
<content:airline /> Flight Schedule.</span><br />
</c:if>
<br />
<c:if test="${!empty pilot}">
To return to your Log Book, <el:cmd className="sec bld" url="logbook" op="log" link="${pilot}">Click Here</el:cmd>.<br /></c:if>
To return to the <content:airline /> Pilot Center, <el:cmd url="pilotcenter" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
