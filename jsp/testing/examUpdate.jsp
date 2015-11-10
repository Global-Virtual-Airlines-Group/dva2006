<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Examination</title>
<content:css name="main" />
<content:js name="common" />
<content:pics />
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
<c:when test="${isSubmit}">
<!-- Examination Submitted -->
<div class="updateHdr">Examination Submitted</div>
<br />
Your ${exam.name} Pilot Examination has been submitted to ${exam.owner.name}. Please allow
between 48 and 96 hours for this Examination to be evaluated and scored. Your will receive an
e-mail notification when this is complete.<br />
<br />
<c:if test="${exam.academy}">
To return to the <content:airline /> Flight Academy, <el:cmd url="academy" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
To return to the <content:airline /> Pilot Center, <el:cmd url="pilotcenter" className="sec bld">Click Here</el:cmd>.<br />
</c:when>
<c:when test="${isScore}">
<!-- Examination Scored -->
<div class="updateHdr">Examination Scored</div>
<br />
This ${exam.name} Pilot Examination for ${pilot.name} has been successfully scored.<c:if test="${!autoScore}"> An e-mail
message has been sent to ${pilot.rank.name} ${pilot.lastName} notifying him or her of the scoring of this Examination.</c:if><br />
<br />
<c:if test="${!empty usrLoc}">
To view the Pilot Profile, <el:profile location="${usrLoc}" className="sec bld">Click Here</el:profile>.<br /></c:if>
<c:if test="${exam.academy}">
To return to the Flight Academy, <el:cmd url="academy" className="sec bld">Click Here</el:cmd>.<br /></c:if>
To return to the Pilot Center, <el:cmd url="pilotcenter" className="sec bld">Click Here</el:cmd>.<br />
<c:if test="${autoScore}">
To return to the Testing Center, <el:cmd url="testcenter" className="sec bld">Click Here</el:cmd>.<br /></c:if>
<content:filter roles="Examination">
To return to the Examination Queue, <el:cmd url="examqueue" className="sec bld">Click Here</el:cmd>.<br /></content:filter>
</c:when>
<c:when test="${isDelete}">
<!-- Examination/Check Ride Deleted -->
<div class="updateHdr">Examination/Check Ride Deleted</div>
<br />
This ${exam.owner.name} ${isCheckRide ? 'Check Ride' : 'Examination'} has been canceled and deleted 
from the database.<br />
</c:when>
<c:when test="${isWaiver}">
<!-- Check Ride Waiver -->
<div class="updateHdr">Check Ride Waiver Created</div>
<br />
${pilot.name} has been granted a waiver for the ${eqType.name} Check Ride.<br />
</c:when>
</c:choose>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
