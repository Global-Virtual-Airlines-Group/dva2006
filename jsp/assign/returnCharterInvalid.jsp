<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Return Charter Flight Unavailable</title>
<content:css name="main" />
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
<div class="updateHdr">Return Charter Flight Unavailable</div>
<br />
<c:choose>
<c:when test="${empty lastFlight}">
You cannot request a Return Charter flight since you have not completed a flight at <content:airline />.<br />
</c:when>
<c:when test="${hasFlight}">
There is currently at least one flight available from ${lastFlight.airportA.name} (<fmt:airport airport="${lastFlight.airportA}" />) in
 the <content:airline /> Flight Schedule. You do not need to request a return Charter flight.<br />
</c:when>
<c:when test="${rangeWarning}">
You cannot request a Return Charter flight from ${lastFlight.airportA.name} (<fmt:airport airport="${lastFlight.airportA}" />) and 
${lastFlight.airportD.name} (<fmt:airport airport="${lastFlight.airportD}" />) in the ${eqType.name}. The distance between these
two airports (<fmt:distance value="${lastFlight.distance}" />) exceeds the range of the ${eqType.name} (<fmt:distance value="${eqType.range}" />).<br />
</c:when>
<c:otherwise>
You cannot request a Return Charter flight from ${lastFlight.airportA.name} (<fmt:airport airport="${lastFlight.airportA}" />) and 
${lastFlight.airportD.name} (<fmt:airport airport="${lastFlight.airportD}" />).<br />
</c:otherwise>
</c:choose>
<br />
<c:if test="${!empty assignPilot}">
To return to your Log Book, <el:cmd className="sec bld" url="logbook" op="log" link="${assignPilot}">Click Here</el:cmd>.<br /></c:if>
To return to the <content:airline /> Pilot Center, <el:cmd url="pilotcenter" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
