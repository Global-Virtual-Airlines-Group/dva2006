<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Flight Schedule Updated</title>
<content:css name="main" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:sysdata var="airlines" name="airlines" />

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${isAirport && isCreate}">
<div class="updateHdr">Airport Added</div>
<br />
The airport ${airport.name} (<fmt:airport airport="${airport}" />) has been added to the <content:airline /> Flight Schedule.<br />
</c:when>
<c:when test="${isAirport && isUpdate}">
<div class="updateHdr">Airport Updated</div>
<br />
The airport ${airport.name} (<fmt:airport airport="${airport}" />) has been updated in the <content:airline /> Flight Schedule.<br />
<c:if test="${!empty accsUpdated}">
<br />
The following Pilot Accomplishments have been updated:<br />
<br />
<c:forEach var="acc" items="${accsUpdated}">
<fmt:accomplish className="bld" accomplish="${ac}" /><br />
</c:forEach>
</c:if>
</c:when>
<c:when test="${isAirport && isDelete}">
<div class="updateHdr">Airport Deleted</div>
<br />
The airport ${airport.name} (<fmt:airport airport="${airport}" />) has been removed from the <content:airline /> Flight Schedule.<br />
</c:when>
<c:when test="${isRawSchedule && isDelete}">
<div class="updateHdr">Schedule Entry Deleted</div>
<br />
The <content:airline /> Raw Schedule Entry from ${src} Line <fmt:int value="${srcLine}" /> was delete from the database and is no longer eligible for inclusion in the Flight Schedule.<br />
</c:when>
<c:when test="${isAirline}">
<div class="updateHdr">Airline Updated</div>
<br />
The airline ${airline.name} has been updated in the <content:airline /> Flight Schedule.<br />
</c:when>
<c:when test="${isFilter}">
<div class="updateHdr">Flight Schedule Updated</div>
<br />
The <content:airline /> Flight Schedule has been reloaded from the raw schedule database. The following operations were performed on these raw schedule sources:<br />
<br />
<c:forEach var="srcInfo" items="${srcs}">
<span class="pri bld">${srcInfo.source}</span> (${srcInfo.source.description}) - <fmt:int value="${srcInfo.legs}" /> flights loaded, <fmt:int value="${srcInfo.skipped}" /> flights skipped, <fmt:int value="${srcInfo.adjusted}" /> arrival times adjusted.
<c:if test="${srcInfo.purged}"> <span class="ter bld">[ PURGED ]</span></c:if><br />
</c:forEach>
<br />
</c:when>
<c:when test="${!empty scheduleEntry}">
<div class="updateHdr">Flight Schedule Updated</div>
<br />
The <content:airline /> Flight Schedule has been updated. Flight ${scheduleEntry.flightCode} has been ${isCreate ? 'added into' : 'updated in'} the database.<br />
<br />
To review this schedule entry, <el:cmd url="sched" op="edit" linkID="${scheduleEntry.source}-${scheduleEntry.lineNumber}" className="sec bld">Click Here</el:cmd>.<br />
To view flights between ${scheduleEntry.airportD.name} (<fmt:airport airport="${scheduleEntry.airportD}" />) and ${scheduleEntry.airportA.name} (<fmt:airport airport="${scheduleEntry.airportA}" />),
 <el:link url="/browse.do?airportD=${scheduleEntry.airportD.ICAO}&airportA=${scheduleEntry.airportA.ICAO}" className="sec bld">Click Here</el:link>.<br />
</c:when>
<c:when test="${scheduleSync}">
<div class="updateHdr">Flight Schedule Synchronized</div>
<br />
The <content:airline /> Flight Schedule has been updated by synchronizing <span class="sec bld">${airline.name}</span> schedule entries from  the <span class="pri bld">${src.name}</span> Flight Schedule.<br />
<br />
<c:if test="${entriesPurged > 0}">
<fmt:int value="${entriesPurged}" /> Flight Schedule entries were purged prior to the synchronization.<br /></c:if>
<fmt:int value="${entriesCopied}" /> Flight Schedule entries were copied from the ${src.name} Flight Schedule.<br />
<br />
To synchronize another airline's schedule entries, please <el:cmd className="sec bld" url="schedsync">Click Here</el:cmd>.<br />
</c:when>
</c:choose>
<c:if test="${isAirport}">
<br />
To return to the list of airports, <el:cmd url="airports" className="sec bld">Click Here</el:cmd>.<br />
<br />
This airport is serviced by <fmt:int value="${airport.airlineCodes.size()}" /> airlines:<br />
<br />
<c:forEach var="aCode" items="${airport.airlineCodes}">
<c:set var="al" value="${airlines[aCode]}" scope="page" />
To view the list of airports serviced by ${al.name}, <el:link url="/airports.do?airline=${aCode}" className="bld">Click Here</el:link>.<br /></c:forEach>
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
