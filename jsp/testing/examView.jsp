<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title>${exam.name} - ${pilot.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<c:if test="${exam.routePlot}">
<map:api version="3" /></c:if>
<c:if test="${hasQImages || exam.routePlot}">
<content:js name="examTake" /></c:if>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<!-- Exam Title Bar -->
<tr class="title caps">
 <td colspan="2">${exam.name} EXAMINATION - ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Taken on</td>
 <td class="data"><fmt:date date="${exam.date}" /></td>
</tr>
<c:if test="${!empty exam.submittedOn}">
<tr>
 <td class="label">Submitted on</td>
 <td class="data"><fmt:date date="${exam.submittedOn}" /><c:if test="${exam.submittedOn > exam.expiryDate}"><span class="error"><fmt:int value="${(exam.submittedOn.toEpochMilli() - exam.expiryDate.toEpochMilli()) / 60000}" /> minutes late</span></c:if></td>
</tr>
</c:if>
<c:if test="${!empty exam.scoredOn}">
<tr>
 <td class="label">Scored on</td>
 <td class="data"><fmt:date date="${exam.scoredOn}" /> by ${scorer.name}</td>
</tr>
</c:if>

<!-- Exam Questions -->
<c:forEach var="q" items="${exam.questions}">
<c:set var="qProfile" value="${qStats[q.ID]}" scope="page" />
<c:set var="hasImage" value="${q.size > 0}" scope="page" />
<c:set var="rspan" value="${hasImage ? 2 : 1}" scope="page" />
<c:if test="${showAnswers}"><c:set var="rspan" value="${rspan + 1}" scope="page" /></c:if>
<!-- Question #${q.number} -->
<tr>
 <td class="label top" rowspan="${rspan}">Question #<fmt:int value="${q.number}" /></td>
 <td class="data">${q.question}</td>
</tr>
<c:if test="${hasImage}">
<tr>
 <td class="data small">RESOURCE - <span class="pri bld">${q.typeName}</span> image, <fmt:int value="${q.size}" /> bytes <span class="sec">(<fmt:int value="${q.width}" /> x <fmt:int value="${q.height}" /> pixels)</span>
 <el:link className="pri bld" url="javascript:void golgotha.exam.viewImage('${q.hexID}', ${q.width}, ${q.height})">VIEW IMAGE</el:link></td>
</tr>
</c:if>
<c:if test="${showAnswers}">
<c:if test="${!empty q.reference}">
<tr>
 <td class="label">Reference</td>
 <td class="data ita">${q.reference}</td>
</tr>
</c:if>
<tr>
 <td class="data"><span class="sec small">${q.correctAnswer}</span>
 <c:if test="${!empty qProfile}"> - <fmt:int value="${qProfile.passCount}" /> / <fmt:int value="${qProfile.total}" /> (<span class="bld"><fmt:dec value="${qProfile.passCount * 100.0 / qProfile.total}" fmt="##0.00" /> %</span>)</c:if></td>
</tr>
</c:if>
<c:if test="${fn:isRoutePlot(q)}">
<tr>
 <td class="label top">Map #<fmt:int value="${q.number}" /></td>
 <td class="data"><map:div ID="qMap${q.number}" height="320" /></td>
</tr>
</c:if>

<!-- Score / Answer #${q.number} -->
<tr>
<c:choose>
<c:when test="${fn:correct(q)}">
 <td class="mid"><el:img caption="Correct" className="noborder" src="testing/pass.png" /></td>
</c:when>
<c:when test="${fn:incorrect(exam, q)}">
 <td class="mid"><el:img caption="Incorrect" className="noborder" src="testing/fail.png" /></td>
</c:when>
<c:otherwise>
 <td class="mid">&nbsp;</td>
</c:otherwise>
</c:choose>
 <td class="data">${q.answer}</td>
</tr>
</c:forEach>

<c:if test="${!empty exam.comments}">
<!-- Scorer Comments -->
<tr>
 <td class="label top">Scorer Comments</td>
 <td class="data"><fmt:msg value="${exam.comments}" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;
<c:if test="${access.canEdit}"><el:cmdbutton url="exam" link="${exam}" op="edit" label="RESCORE EXAMINATION" /></c:if>
<c:if test="${access.canDelete}">&nbsp;<el:cmdbutton url="examdelete" link="${exam}" label="DELETE EXAMINATION" /></c:if></td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<c:if test="${exam.routePlot}">
<script id="mapInit">
golgotha.exam.maps = [];
<c:forEach var="q" items="${exam.questions}"><c:if test="${fn:isRoutePlot(q)}">
<c:set var="answerRoute" value="${aRoutes[q.number]}" scope="page" />
<c:set var="correctRoute" value="${cRoutes[q.number]}" scope="page" />
<map:point var="mapC" point="${q.midPoint}" />

// Create map
const mapTypes = {mapTypeIds:[google.maps.MapTypeId.TERRAIN, google.maps.MapTypeId.SATELLITE]};
const mapOpts = {center:mapC, zoom:golgotha.maps.util.getDefaultZoom(${q.distance}), scrollwheel:false, streetViewControl:false, clickableIcons:false, mapTypeControlOptions:mapTypes};
const map = new golgotha.maps.Map(document.getElementById('qMap${q.number}'), mapOpts);
map.setMapTypeId(google.maps.MapTypeId.TERRAIN);
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', map.closeWindow);
<map:points var="crPoints" items="${correctRoute}" />
<map:line var="crLine" src="crPoints" width="2" color="#af7f7f" transparency="0.65" geodesic="true" />
crLine.setMap(map);
<c:if test="${fn:sizeof(answerRoute) > 2}">
<map:points var="arPoints" items="${answerRoute}" />
<map:line var="arLine" src="arPoints" width="2" color="#4080af" transparency="0.8" geodesic="true" />
arLine.setMap(map);
<map:markers var="arMarkers" items="${answerRoute}" />
map.addMarkers(arMarkers);
</c:if>
golgotha.exam.maps.push(map);
</c:if></c:forEach>
</script></c:if>
<content:googleAnalytics />
</body>
</html>
