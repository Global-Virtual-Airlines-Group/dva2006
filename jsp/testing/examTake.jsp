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
<content:js name="examTake" />
<c:if test="${exam.routePlot}">
<content:js name="googleMaps" />
<map:api version="2" />
<map:vml-ie />
</c:if>
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="imgPath" name="path.img" />
<c:set var="onLoad" value="showRemaining(10)" scope="page" />
<script language="JavaScript" type="text/javascript">
var expiry = ${exam.expiryDate.time};
<c:if test="${exam.routePlot}">
var rpInfo = [];
var rpQuestions = ${rpQuestions};
var doRunways = false;
document.imgPath = '${imgPath}';

function initMaps()
{
for (var x = 0; x < rpQuestions.length; x++) {
	var idx = rpQuestions[x];
	var info = rpInfo[idx];
	info.map = new GMap2(getElement("qMap" + info.idx), {mapTypes:[G_SATELLITE_MAP, G_PHYSICAL_MAP]});
	info.map.addControl(new GSmallMapControl());
	info.map.addControl(new GMapTypeControl());
	info.map.setCenter(info.mapCenter, getDefaultZoom(info.distance));
	info.map.enableDoubleClickZoom();
	info.map.enableContinuousZoom();
	info.map.setMapType(G_PHYSICAL_MAP);
	info.map.addOverlay(info.aD);
	info.map.addOverlay(info.aA);
	info.map.addOverlay(new GPolyline([info.aD.getLatLng(), info.aA.getLatLng()], '#4080AF', 1.75, 0.65, { geodesic:true }));
}

return true;
}
<c:set var="onLoad" value="initMaps(); ${onLoad}" scope="page" />
</c:if>
//Time offset between server and client clock
var timeOffset = (new Date().getTime() - ${currentTime});
</script>
</head>
<content:copyright visible="false" />
<body onload="${onLoad}"<c:if test="${exam.routePlot}"> onunload="GUnload()"</c:if>>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="examsubmit.do" link="${exam}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
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
 <el:link className="pri bld" url="javascript:void viewImage('${q.hexID}', ${q.width}, ${q.height})">VIEW IMAGE</el:link></td>
</tr>
</c:if>
<c:if test="${isRP}">
<!-- Map #${q.number} -->
<script language="JavaScript" type="text/javascript">
var info = { examID: '${exam.hexID}', exam: ${exam.ID}, idx: ${q.number}, distance: ${q.distance} };
info.mapCenter = <map:point point="${q.midPoint}" />
info.aD = <map:marker point="${q.airportD}" />
info.aA = <map:marker point="${q.airportA}" />
rpInfo[${q.number}] = info;
</script>
<tr>
 <td class="label top">Map #<fmt:int value="${q.number}" /></td>
 <td class="data"><map:div ID="qMap${q.number}" x="100%" y="360" /></td>
</tr>
</c:if>

<!-- Answer #${q.number} -->
<tr>
 <td class="label top">Answer #<fmt:int value="${q.number}" /></td>
<c:choose>
<c:when test="${isRP}">
 <td class="data"><el:check ID="A${q.number}" onChange="void updateMap(rpInfo[${q.number}])" type="radio" idx="*" cols="1" width="500" separator="<br />" name="answer${q.number}" className="small" options="${q.choices}" value="${q.answer}" /></td>
</c:when>
<c:when test="${isMC}">
 <td class="data"><el:check ID="A${q.number}" onChange="void saveAnswer(${q.number}, ${exam.hexID})" type="radio" name="answer${q.number}" className="small" width="400" cols="1" options="${q.choices}" value="${q.answer}" /></td>
</c:when>
<c:otherwise>
 <td class="data"><el:textbox ID="A${q.number}" onBlur="void saveAnswer(${q.number}, ${exam.hexID})" name="answer${q.number}" className="small" width="90%" height="3">${q.answer}</el:textbox></td>
</c:otherwise>
</c:choose>
</tr>
</c:forEach>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td>&nbsp;
<c:if test="${access.canSubmit}">
<el:button ID="SubmitButton" type="SUBMIT" className="BUTTON" label="SUBMIT EXAMINATION" /></c:if>
 </td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</map:xhtml>
