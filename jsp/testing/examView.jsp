<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title>${exam.name} - ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<c:if test="${exam.routePlot}">
<map:api version="3" /></c:if>
<script type="text/javascript">
<c:if test="${hasQImages}">
function viewImage(id, x, y)
{
var flags = 'height=' + (y+45) + ',width=' + (x+45) + ',menubar=no,toolbar=no,status=yes,scrollbars=yes';
var w = window.open('/exam_rsrc/' + id, 'questionImage', flags);
return true;
}
</c:if></script>
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
 <td class="data"><fmt:date date="${exam.submittedOn}" />
<c:if test="${exam.submittedOn > exam.expiryDate}"><span class="error"><fmt:int value="${(exam.submittedOn.time - exam.expiryDate.time) / 60000}" />
 minutes late</span></c:if></td>
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
 <td class="data small">RESOURCE - <span class="pri bld">${q.typeName}</span> image, <fmt:int value="${q.size}" />
 bytes <span class="sec">(<fmt:int value="${q.width}" /> x <fmt:int value="${q.height}" /> pixels)</span>
 <el:link className="pri bld" url="javascript:void viewImage('${q.hexID}', ${q.width}, ${q.height})">VIEW IMAGE</el:link></td>
</tr>
</c:if>
<c:if test="${showAnswers}">
<tr>
 <td class="data"><span class="sec small">${q.correctAnswer}</span>
 <c:if test="${!empty qProfile}"> - <fmt:int value="${qProfile.correctAnswers}" /> / <fmt:int value="${qProfile.totalAnswers}" />
 (<span class="bld"><fmt:dec value="${qProfile.correctAnswers * 100.0 / qProfile.totalAnswers}" fmt="##0.00" /> %</span>)</c:if></td>
</tr>
</c:if>
<c:if test="${fn:isRoutePlot(q)}">
<tr>
 <td class="label top">Map #<fmt:int value="${q.number}" /></td>
 <td class="data"><map:div ID="qMap${q.number}" x="100%" y="320" /></td>
</tr>
</c:if>

<!-- Score / Answer #${q.number} -->
<tr>
<c:choose>
<c:when test="${fn:correct(q)}">
 <td class="mid"><el:img caption="Correct" border="0" src="testing/pass.png" /></td>
</c:when>
<c:when test="${fn:incorrect(exam, q)}">
 <td class="mid"><el:img caption="Incorrect" border="0" src="testing/fail.png" /></td>
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
<c:if test="${access.canEdit}">
 <el:cmdbutton url="exam" link="${exam}" op="edit" label="RESCORE EXAMINATION" />
</c:if>
<c:if test="${access.canDelete}">
 <el:cmdbutton url="examdelete" link="${exam}" label="DELETE EXAMINATION" />
</c:if>
 </td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<c:if test="${exam.routePlot}">
<script type="text/javascript">
var maps = [];
<c:forEach var="q" items="${exam.questions}"><c:if test="${fn:isRoutePlot(q)}">
<c:set var="answerRoute" value="${aRoutes[q.number]}" scope="page" />
<c:set var="correctRoute" value="${cRoutes[q.number]}" scope="page" />
<map:point var="mapC" point="${q.midPoint}" />

// Create map
var mapTypes = {mapTypeIds: [google.maps.MapTypeId.TERRAIN, google.maps.MapTypeId.SATELLITE]};
var mapOpts = {center:mapC, zoom:getDefaultZoom(${q.distance}), scrollwheel:false, streetViewControl:false, mapTypeControlOptions: mapTypes};
var map = new google.maps.Map(getElement('qMap${q.number}'), mapOpts);
map.setMapTypeId(google.maps.MapTypeId.TERRAIN);
map.infoWindow = new google.maps.InfoWindow({content: ''});
google.maps.event.addListener(map, 'click', function() { map.infoWindow.close(); });
<map:points var="crPoints" items="${correctRoute}" />
<map:line var="crLine" src="crPoints" width="2" color="#af7f7f" transparency="0.65" geodesic="true" />
crLine.setMap(map);
<c:if test="${fn:sizeof(answerRoute) > 2}">
<map:points var="arPoints" items="${answerRoute}" />
<map:line var="arLine" src="arPoints" width="2" color="#4080af" transparency="0.8" geodesic="true" />
arLine.setMap(map);
<map:markers var="arMarkers" items="${answerRoute}" />
addMarkers(map, 'arMarkers');
</c:if>
maps.push(map);
</c:if></c:forEach>
</script></c:if>
<content:googleAnalytics />
</body>
</map:xhtml>
