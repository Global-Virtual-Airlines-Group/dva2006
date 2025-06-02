<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Check Ride</title>
<content:css name="main" />
<content:css name="form" />
<content:googleAnalytics />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
<script async>
golgotha.local.toggleBody = function(id) {
	const row = document.getElementById(id);
	const linkDesc = document.getElementById('toggleC');
	const visible = (row.style.display != 'none');
	golgotha.util.display(row, !visible);
	linkDesc.innerHTML = visible ? 'View' : 'Hide';
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">${checkRide.name} FOR ${pilot.name} (${pilot.pilotCode})</td>
</tr>
<c:if test="${!fn:isWaiver(checkRide)}">
<tr>
 <td class="label">${fn:pending(checkRide) ? 'Assigned' : 'Scored'} by</td>
 <td class="data"><span class="sec bld">${scorer.name}</span> (${scorer.pilotCode})</td>
</tr>
</c:if>
<c:if test="${!checkRide.academy}">
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><span class="sec bld">${checkRide.equipmentType}</span> (Stage <fmt:int value="${checkRide.stage}" />) - <span class="sec bld">${checkRide.owner.name}</span></td>
</tr>
</c:if>
<tr>
 <td class="label">Aircraft Type</td>
 <td class="data">${checkRide.aircraftType}</td>
</tr>
<c:if test="${(checkRide.flightID != 0) && (!empty pirep)}">
<tr>
 <td class="label">ACARS Flight ID</td>
 <td class="data"><span class="sec bld"><fmt:int value="${checkRide.flightID}" /></span> - <el:cmd url="crview" link="${checkRide}" className="pri bld">VIEW FLIGHT REPORT</el:cmd></td>
</tr>
</c:if>
<c:if test="${!empty course}">
<tr>
 <td class="label">Flight Academy Course</td>
 <td class="data"><span class="bld">${course.name}</span> (Stage <fmt:int value="${course.stage}" />) - <el:cmd url="course" link="${course}" className="bld">VIEW COURSE</el:cmd></td>
</tr>
</c:if>
<c:if test="${!empty txReq}">
<tr>
 <td class="label">Transfer Request</td>
 <td class="data"><span class="pri bld">${txReq.equipmentType}</span>, created on <fmt:date date="${txReq.date}" fmt="d" /><span class="nophone"> - <span class="sec bld">${txReq.simulator.name}</span> - <el:cmd url="txreqview" link="${txReq}" className="sec bld">VIEW TRANSFER REQUEST</el:cmd></span></td>
</tr>
</c:if>
<tr>
 <td class="label">Assigned on</td>
 <td class="data"><fmt:date t="HH:mm" date="${checkRide.date}" /> - <a href="javascript:void golgotha.local.toggleBody('crComments')"><span id="toggleC">View</span> Description</a></td>
</tr>
<c:if test="${!fn:isWaiver(checkRide) && (!empty checkRide.submittedOn)}">
<tr>
 <td class="label">Submitted on</td>
 <td class="data"><fmt:date t="HH:mm" date="${checkRide.submittedOn}" /></td>
</tr>
</c:if>
<c:if test="${!fn:isWaiver(checkRide)}">
<tr>
 <td class="label">Check Ride Status</td>
<c:choose>
<c:when test="${fn:passed(checkRide)}">
 <td class="data ter bld caps">THIS CHECK RIDE HAS BEEN SUCCESSFULLY COMPLETED</td>
</c:when>
<c:when test="${fn:failed(checkRide)}">
 <td class="data error bld caps">THIS CHECK RIDE WAS NOT SUCCESSFULLY COMPLETED</td>
</c:when>
<c:otherwise>
 <td class="data sec bld">${checkRide.status.name}</td>
</c:otherwise>
</c:choose>
</tr>
</c:if>
<c:if test="${!empty checkRide.scoredOn}">
<tr>
 <td class="label">Evaluated on</td>
 <td class="data"><fmt:date t="HH:mm" date="${checkRide.scoredOn}" /> by ${scorer.name}</td>
</tr>
</c:if>
<c:if test="${!empty checkRide.expirationDate}">
<tr>
 <td class="label">Currency Expires</td>
 <td class="data"><fmt:date fmt="d" date="${checkRide.expirationDate}" /></td>
</tr>
</c:if>
<tr id="crComments" style="display:none;">
 <td class="label top">Description</td>
 <td class="data top"><fmt:msg value="${checkRide.comments}" bbCode="true" /></td>
</tr>
<c:if test="${!empty pirep}">
<tr id="frComments">
 <td class="label top">Comments</td>
 <td class="data top"><fmt:msg value="${pirep.comments}" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;
<c:if test="${!fn:pending(checkRide) && access.canEdit}"><el:cmdbutton url="checkride" link="${checkRide}" op="edit" label="RESCORE EXAMINATION" /></c:if> 
<c:if test="${access.canDelete}">&nbsp;<el:cmdbutton url="examdelete" link="${checkRide}" op="checkride" label="DELETE CHECK RIDE${fn:isWaiver(checkRide) ? ' WAIVER' : ''}" /></c:if>
 </td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
