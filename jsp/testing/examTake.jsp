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
<content:js name="examTake" />
<c:if test="${exam.routePlot}">
<map:api version="3" />
<content:json /></c:if>
<content:googleAnalytics eventSupport="true" />
<c:set var="onLoad" value="golgotha.exam.showRemaining(10)" scope="page" />
<script async>
golgotha.exam.expiry = ${exam.expiryDate.toEpochMilli()};
<c:if test="${exam.routePlot}">
golgotha.exam.rpQuestions = ${rpQuestions};
golgotha.exam.initMaps = function()
{
const mapTypes = {mapTypeIds:[google.maps.MapTypeId.TERRAIN, google.maps.MapTypeId.SATELLITE]};
for (var x = 0; x < golgotha.exam.rpQuestions.length; x++) {
	var idx = golgotha.exam.rpQuestions[x];
	const info = golgotha.exam.rpInfo[idx];
	const mapOpts = {center:info.mapCenter, zoom:golgotha.maps.util.getDefaultZoom(info.distance), scrollwheel:false, clickableIcons:false, streetViewControl:false, mapTypeControlOptions:mapTypes};
	info.map = new golgotha.maps.Map(document.getElementById('qMap' + info.idx), mapOpts);
	info.map.setMapTypeId(google.maps.MapTypeId.TERRAIN);
	info.aD.setMap(info.map);
	info.aA.setMap(info.map);
	const rt = new google.maps.Polyline({map:info.map, path:[info.aD.getPosition(), info.aA.getPosition()], strokeColor:'#4080af', strokeWeight:1.75, strokeOpacity:0.65, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});
}

return true;
};
<c:set var="onLoad" value="golgotha.exam.initMaps(); ${onLoad}" scope="page" /></c:if>
// Time offset between server and client clock
golgotha.exam.timeOffset = (new Date().getTime() - ${currentTime});
</script>
</head>
<content:copyright visible="false" />
<body onload="${onLoad}">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="examsubmit.do" link="${exam}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Exam Title Bar -->
<tr class="title caps">
 <td colspan="2">${exam.name} EXAMINATION - ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Taken on</td>
 <td class="data"><fmt:date date="${exam.date}" /></td>
</tr>
<tr>
 <td class="label">Time Remaining</td>
 <td class="data"><span id="timeRemaining" class="ter bld">XX minutes</span></td>
</tr>

<!-- Exam Questions -->
<c:forEach var="q" items="${exam.questions}">
<c:set var="hasImage" value="${q.size > 0}" scope="page"/>
<c:set var="isRP" value="${fn:isRoutePlot(q)}" scope="page" />
<c:set var="isMC" value="${isRP || fn:isMultiChoice(q)}" scope="page" />
<!-- Question #${q.number} -->
<tr>
 <td class="label top" rowspan="${hasImage ? '2' : '1'}">Question #<fmt:int value="${q.number}" /></td>
 <td class="data">${q.question}</td>
</tr>
<c:if test="${hasImage}">
<tr>
 <td class="data small">RESOURCE - <span class="pri bld">${q.typeName}</span> image, <fmt:int value="${q.size}" />
 bytes <span class="sec">(<fmt:int value="${q.width}" /> x <fmt:int value="${q.height}" /> pixels)</span>
 <el:link className="pri bld" url="javascript:void golgotha.exam.viewImage('${q.hexID}', ${q.width}, ${q.height})">VIEW IMAGE</el:link></td>
</tr>
</c:if>
<c:if test="${isRP}">
<!-- Map #${q.number} -->
<script>
var info = { examID: '${exam.hexID}', exam: ${exam.ID}, idx: ${q.number}, distance: ${q.distance} };
info.mapCenter = <map:point point="${q.midPoint}" />
info.aD = <map:marker point="${q.airportD}" />
info.aA = <map:marker point="${q.airportA}" />
golgotha.exam.rpInfo[${q.number}] = info;
</script>
<tr>
 <td class="label top">Map #<fmt:int value="${q.number}" /></td>
 <td class="data"><map:div ID="qMap${q.number}" height="360" /></td>
</tr>
</c:if>

<!-- Answer #${q.number} -->
<tr>
 <td class="label top">Answer #<fmt:int value="${q.number}" /></td>
<c:choose>
<c:when test="${isRP}">
 <td class="data"><el:check ID="A${q.number}" onChange="void golgotha.exam.updateMap(golgotha.exam.rpInfo[${q.number}])" type="radio" idx="*" cols="1" width="500" separator="<br />" name="answer${q.number}" className="small" options="${q.choices}" value="${q.answer}" /></td>
</c:when>
<c:when test="${isMC}">
 <td class="data"><el:check ID="A${q.number}" onChange="void golgotha.exam.saveAnswer(${q.number}, ${exam.hexID})" type="radio" name="answer${q.number}" className="small" width="400" cols="1" options="${q.choices}" value="${q.answer}" /></td>
</c:when>
<c:otherwise>
 <td class="data"><el:textbox ID="A${q.number}" onBlur="void golgotha.exam.saveAnswer(${q.number}, ${exam.hexID})" name="answer${q.number}" className="small" width="90%" height="3" resize="true">${q.answer}</el:textbox></td>
</c:otherwise>
</c:choose>
</tr>
</c:forEach>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;
<c:if test="${access.canSubmit}"><el:button type="submit" label="SUBMIT EXAMINATION" /></c:if>
 </td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
