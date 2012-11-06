<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Runway Options - ${pirep.flightCode}</title>
<content:css name="main" />
<content:pics />
</head>
<content:copyright visible="false" />
<body style="margin:8px">
<div class="updateHdr">Runway Choices for ${pirep.airportD.name} (<fmt:airport airport="${pirep.airportD}" />)</div>
<br />
Takeoff Heading = <fmt:int value="${pirep.takeoffHeading}" />&deg;, Magnetic Variation at Airport: 
<fmt:dec value="${pirep.airportD.magVar}" />&deg;<br />
<br />
<c:forEach var="rc" items="${rwysD.runways}">
<span class="pri bld">Runway ${rc.runway.name}</span> (<fmt:int value="${rc.runway.length}" /> feet) - Heading =
${rc.runway.heading}&deg; &Delta;Heading = <fmt:dec value="${rc.headingDelta}" />&deg;, Bearing = 
<fmt:dec value="${rc.bearing}" />&deg;, &Delta;Bearing = <fmt:dec value="${rc.bearingDelta}" />&deg;<br />  
</c:forEach>
<br />
<div class="updateHdr">Runway Choices for ${pirep.airportA.name} (<fmt:airport airport="${pirep.airportA}" />)</div>
<br />
Landing Heading = <fmt:int value="${pirep.landingHeading}" />&deg;, Magnetic Variation at Airport: 
<fmt:dec value="${pirep.airportA.magVar}" />&deg;<br />
<br />
<c:forEach var="rc" items="${rwysA.runways}">
<span class="pri bld">Runway ${rc.runway.name}</span> (<fmt:int value="${rc.runway.length}" /> feet) - Heading =
${rc.runway.heading}&deg; &Delta;Heading = <fmt:dec value="${rc.headingDelta}" />&deg;, Bearing = 
<fmt:dec value="${rc.bearing}" />&deg;, &Delta;Bearing = <fmt:dec value="${rc.bearingDelta}" />&deg;<br />  
</c:forEach>
<br />
<el:link url="javascript:void window.close()" className="sec bld">Click Here</el:link> to close this window.<br />
<br />
<content:copyright />
<content:googleAnalytics />
</body>
</html>
