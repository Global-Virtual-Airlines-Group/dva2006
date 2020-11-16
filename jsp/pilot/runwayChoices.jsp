<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Runway Options - ${pirep.flightCode}</title>
<content:expire expires="5" />
<content:css name="main" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body style="margin:8px">
<div class="updateHdr">Runway Choices for ${pirep.airportD.name} (<fmt:airport airport="${pirep.airportD}" />)</div>
<br />
Takeoff Heading = <fmt:int value="${pirep.takeoffHeading}" />&deg;, Magnetic Variation at Airport: <fmt:dec value="${rwysD.magVar}" />&deg;<br />
<br />
<c:forEach var="rc" items="${rwysD.runways}">
<c:set var="rw" value="${rc.runway}" scope="page" />
<c:set var="onRunway" value="${rw.contains(pirep.takeoffLocation) && (rc.headingDelta < 60)}" scope="page" />
<c:set var="className" value="${onRunway ? 'pri bld' : 'sec bld'}" scope="page" />
<span class="${className}">Runway ${rw.name}</span> (<fmt:int value="${rw.length}" /> feet<c:if test="${rw.thresholdLength > 0}">, displaced <fmt:int value="${rw.thresholdLength}" /> feet</c:if>) - Heading = ${rw.heading}&deg; 
&Delta;Heading = <fmt:dec value="${rc.headingDelta}" />&deg;, Bearing = <fmt:dec value="${rc.bearing}" />&deg;, &Delta;Bearing = <fmt:dec value="${rc.bearingDelta}" />&deg; - <fmt:dec value="${rc.crossBearingDelta}" fmt="0.000" /><br />  
</c:forEach>
<br />
<div class="updateHdr">Runway Choices for ${pirep.airportA.name} (<fmt:airport airport="${pirep.airportA}" />)</div>
<br />
Landing Heading = <fmt:int value="${pirep.landingHeading}" />&deg;, Magnetic Variation at Airport: <fmt:dec value="${rwysA.magVar}" />&deg;<br />
<br />
<c:forEach var="rc" items="${rwysA.runways}">
<c:set var="rw" value="${rc.runway}" scope="page" />
<c:set var="onRunway" value="${rw.contains(pirep.landingLocation) && (rc.headingDelta < 60)}" scope="page" />
<c:set var="className" value="${onRunway ? 'pri bld' : 'sec bld'}" scope="page" />
<span class="${className}">Runway ${rw.name}</span> (<fmt:int value="${rw.length}" /> feet<c:if test="${rw.thresholdLength > 0}">, displaced <fmt:int value="${rw.thresholdLength}" /> feet</c:if>) - Heading = ${rw.heading}&deg; 
&Delta;Heading = <fmt:dec value="${rc.headingDelta}" />&deg;, Bearing = <fmt:dec value="${rc.bearing}" />&deg;, &Delta;Bearing = <fmt:dec value="${rc.bearingDelta}" />&deg; - <fmt:dec value="${rc.crossBearingDelta}" fmt="0.000" /><br />  
</c:forEach>
<br />
<el:link url="javascript:void window.close()" className="sec bld">Click Here</el:link> to close this window.<br />
<br />
<content:copyright />
<content:googleAnalytics />
</body>
</html>
