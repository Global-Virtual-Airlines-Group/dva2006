<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title>Examination Question Profile</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:js name="examTake" />
<c:if test="${fn:isRoutePlot(question)}">
<map:api version="3" /></c:if>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<!-- Question Title Bar -->
<tr class="title caps">
 <td colspan="2">${fn:isMultiChoice(question) ? 'MULTIPLE CHOICE ' : ''}EXAMINATION QUESTION PROFILE</td>
</tr>
<tr>
 <td class="label">Question Text</td>
 <td class="data pri bld">${question.question}</td>
</tr>
<c:if test="${fn:isMultiChoice(question)}">
<tr>
 <td class="label top">Answer Choices</td>
 <td class="data"><c:forEach var="choice" items="${question.choices}">${choice}<br />
</c:forEach></td>
</tr>
</c:if>
<tr>
 <td class="label">Correct Answer</td>
 <td class="data bld">${question.correctAnswer}</td>
</tr>
<tr>
 <td class="label top">Pilot Examinations</td>
 <td class="data small"><fmt:list value="${question.exams}" delim=", " /></td>
</tr>
<tr>
 <td class="label">Statistics</td>
<c:if test="${question.totalAnswers > 0}">
 <td class="data">Answered <fmt:int value="${question.totalAnswers}" /> times,
 <fmt:int value="${question.correctAnswers}" /> correctly 
 (<fmt:dec value="${question.correctAnswers / question.totalAnswers * 100}" />%)</td>
</c:if>
<c:if test="${question.totalAnswers == 0}">
 <td class="data bld">This Question has never been included in a Pilot Examination</td>
</c:if>
</tr>
<c:if test="${question.size > 0}">
<tr>
 <td class="label">Image Information</td>
 <td class="data"><span class="pri bld">${question.typeName}</span> image, <fmt:int value="${question.size}" />
 bytes <span class="sec">(<fmt:int value="${question.width}" /> x <fmt:int value="${question.height}" />
 pixels)</span> <el:link className="pri bld small" url="javascript:void golgotha.exam.viewImage('${question.hexID}',${question.width},${question.height})">VIEW IMAGE</el:link></td>
</tr>
</c:if>
<c:if test="${fn:isRoutePlot(question)}">
<tr>
 <td class="label">Departing from</td>
 <td class="data">${question.airportD.name} (<fmt:airport airport="${question.airportD}" />)</td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data">${question.airportA.name} (<fmt:airport airport="${question.airportD}" />)</td> 
</tr>
<tr>
 <td class="label top">Route Map</td>
 <td class="data"><map:div ID="googleMap" height="400" /></td>
</tr>
</c:if>
<tr>
 <td class="label">&nbsp;</td>
<c:if test="${question.active}">
 <td class="data ter bld caps">Question is Available</td>
</c:if>
<c:if test="${!question.active}">
 <td class="data error bld caps">Question is Not Available</td>
</c:if>
</tr>
<%@ include file="/jsp/auditLog.jspf" %>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:cmdbutton url="qprofile" link="${question}" op="edit" label="EDIT QUESTION" />
<c:if test="${access.canDelete && (question.totalAnswers == 0)}">
 <el:cmdbutton url="qpdelete" link="${question}" label="DELETE QUESTION" />
</c:if>
</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
<c:if test="${fn:isRoutePlot(question)}">
<script id="mapInit">
<map:point var="mapC" point="${question.midPoint}" />

// Create map
var mapTypes = {mapTypeIds:[google.maps.MapTypeId.TERRAIN, google.maps.MapTypeId.SATELLITE]};
var mapOpts = {center:mapC, zoom:golgotha.maps.util.getDefaultZoom(${q.distance} - 1), scrollwheel:false, streetViewControl:false, clickableIcons:false, mapTypeControlOptions:mapTypes};
var map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', map.closeWindow);
<map:marker var="aD" point="${question.airportD}" />
<map:marker var="aA" point="${question.airportA}" />
<map:points var="routePoints" items="${route}" />
<map:line var="rpLine" src="routePoints" color="#4080af" width="2" transparency="0.65" geodesic="true" />
<map:markers var = "routeMarkers" items="${route}" />
rpLine.setMap(map);
map.addMarkers(routeMarkers);
aD.setMap(map);
</script>
</c:if>
</body>
</html>
