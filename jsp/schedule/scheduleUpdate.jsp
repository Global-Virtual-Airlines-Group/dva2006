<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Schedule Updated</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${isAirport && isCreate}">
<div class="updateHdr">Airport Added</div>
<br />
The airport ${airport.name} (<fmt:airport airport="${airport}" />) has been added to the
<content:airline /> Flight Schedule.<br />
</c:when>
<c:when test="${isAirport && isUpdate}">
<div class="updateHdr">Airport Updated</div>
<br />
The airport ${airport.name} (<fmt:airport airport="${airport}" />) has been updated in the
<content:airline /> Flight Schedule.<br />
</c:when>
<c:when test="${isAirport && isDelete}">
<div class="updateHdr">Airport Deleted</div>
<br />
The airport ${airport.name} (<fmt:airport airport="${airport}" />) has been removed from the
<content:airline /> Flight Schedule.<br />
</c:when>
<c:when test="${isAirline}">
<div class="updateHdr">Airline Updated</div>
<br />
The airline ${airline.name} has been updated in the <content:airline /> Flight Schedule.<br />
</c:when>
<c:when test="${isFlights}">
<div class="updateHdr">Flight Schedule Updated</div>
<br />
The <content:airline /> Flight Schedule has been updated. <fmt:int value="${entryCount}" /> Schedule entries 
have been imported into the database. <c:if test="${doPurge}"><span class="sec">The database was purged 
before new entries were uploaded.</span></c:if><br />
<c:if test="${!empty errors}">
<br />
The following errors occured during the import process:<br />
<div class="small">
<c:forEach var="error" items="${errors}">
${error}<br />
</c:forEach>
</div>
</c:if>
</c:when>
<c:when test="${!empty scheduleEntry}">
<div class="updateHdr">Flight Schedule Updated</div>
<br />
The <content:airline /> Flight Schedule has been updated. Flight ${scheduleEntry.flightCode} has been
${isCreate ? 'added into' : 'updated in'} the database.<br />
<br />
To review this schedule entry, <el:cmd url="sched" op="edit" linkID="${scheduleEntry.flightCode}" className="sec bld">click here</el:cmd>.<br />
</c:when>
</c:choose>
<c:if test="${isAirport}">
<br />
To return to the list of airports, <el:cmd url="airports" className="sec bld">click here</el:cmd>.<br />
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
