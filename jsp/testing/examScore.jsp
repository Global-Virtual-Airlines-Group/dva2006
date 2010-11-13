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
<content:googleAnalytics eventSupport="true" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;

if (!confirm('Have you scored all Questions? Hit OK to submit.')) return false;
setSubmit();
disableButton('ScoreButton');
return true;
}
<c:if test="${hasQImages}">
function viewImage(id, x, y)
{
var flags = 'height=' + (y+45) + ',width=' + (x+45) + ',menubar=no,toolbar=no,status=yes,scrollbars=yes';
var w = window.open('/exam_rsrc/' + id, 'questionImage', flags);
return true;
}
</c:if>
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="examscore.do" link="${exam}" validate="return validate(this)">
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
<c:set var="late" value="${(exam.submittedOn.time - exam.expiryDate.time) / 1000}" scope="page" />
<c:set var="lateH" value="${late / 3600}" scope="page" />
<c:set var="lateM" value="${(late % 3600) / 60}" scope="page" />
<c:set var="lateS" value="${late % 60}" scope="page" />
<tr>
 <td class="label">Submitted on</td>
 <td class="data"><fmt:date date="${exam.submittedOn}" />
<c:if test="${exam.submittedOn > exam.expiryDate}"><span class="error caps">
<c:if test="${lateH > 0}"><fmt:int value="${lateH}" /> hours, </c:if>
<c:if test="${lateM > 0}"><fmt:int value="${lateM}" /> minutes, </c:if>
<c:if test="${lateS > 0}"><fmt:int value="${lateS}" /> seconds</c:if> late</span>
</c:if>
 </td>
</tr>
</c:if>

<!-- Exam Questions -->
<c:forEach var="q" items="${exam.questions}">
<c:set var="mcCSS" value="${fn:isMultiChoice(q) ? 'opt1' : ''}" scope="page" />
<c:set var="hasImage" value="${q.size > 0}" scope="page" />
<!-- Question #${q.number} -->
<tr>
 <td class="label top" rowspan="${hasImage ? '3' : '2'}">Question #<fmt:int value="${q.number}" /></td>
 <td class="data ${mcCSS}">${q.question}</td>
</tr>
<c:if test="${hasImage}">
<tr>
 <td class="data small">RESOURCE - <span class="pri bld">${q.typeName}</span> image, <fmt:int value="${q.size}" />
 bytes <span class="sec">(<fmt:int value="${q.width}" /> x <fmt:int value="${q.height}" /> pixels)</span>
 <el:link className="pri bld" url="javascript:void viewImage('${q.hexID}', ${q.width}, ${q.height})">VIEW IMAGE</el:link></td>
</tr>
</c:if>
<tr>
 <td class="data small ${mcCSS}"><span class="${q.exactMatch ? 'warn' : 'sec'}">${q.correctAnswer}</span></td>
</tr>
<c:if test="${fn:isRoutePlot(q)}">
<tr>
 <td class="label top">Map #<fmt:int value="${q.number}" /></td>
 <td class="data"><map:div ID="qMap${q.number}" x="100%" y="320" /></td>
</tr>
</c:if>

<!-- Score / Answer #${q.number} -->
<tr>
 <td class="mid"><el:box className="small" name="Score${q.number}" value="true" checked="${fn:correct(q)}" label="Correct" /></td>
 <td class="data bld ${mcCSS}">${q.answer}</td>
</tr>
</c:forEach>

<!-- Examination Comments -->
<tr>
 <td class="label top">Scorer Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="80%" height="3" resize="true">${exam.comments}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="ScoreButton" type="submit" label="SCORE EXAMINATION" /></td>
</tr>
</el:table>
</el:form>
<br />
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
<map:line var="crLine" src="crPoints" width="2" color="#af7f7f" transparency="0.6" geodesic="true" />
crLine.setMap(map);
<c:if test="${fn:sizeof(answerRoute) > 2}">
<map:points var="arPoints" items="${answerRoute}" />
<map:line var="arLine" src="arPoints" width="2" color="#4080AF" transparency="0.8" geodesic="true" />
arLine.setMap(map);
<map:markers var="arMarkers" items="${answerRoute}" />
addMarkers(map, 'arMarkers');
</c:if>
maps.push(map);
</c:if></c:forEach>
</script>
</c:if>
</body>
</map:xhtml>
