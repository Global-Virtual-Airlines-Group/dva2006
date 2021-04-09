<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
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
<content:pics />
<content:favicon />
<content:js name="common" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.passFail, min:1, t:'Check Ride status'});
	golgotha.form.submit(f);
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
<el:form action="crscore.do" link="${checkRide}" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">${checkRide.aircraftType} CHECK RIDE FOR ${pilot.name} (${pilot.pilotCode})</td>
</tr>
<tr>
 <td class="label">${fn:pending(checkRide) ? 'Assigned' : 'Scored'} by</td>
 <td class="data"><span class="sec bld">${scorer.name}</span> (${scorer.pilotCode})</td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><span class="sec bld">${checkRide.equipmentType}</span> (Stage <fmt:int value="${checkRide.stage}" />)</td>
</tr>
<c:if test="${(checkRide.flightID != 0) && (!empty pirep)}">
<tr>
 <td class="label">ACARS Flight ID</td>
 <td class="data sec bld"><fmt:int value="${checkRide.flightID}" /> - <el:cmdbutton url="crview" link="${checkRide}" label="VIEW FLIGHT REPORT" /></td>
</tr>
</c:if>
<c:if test="${!empty course}">
<tr>
 <td class="label">Flight Academy Course</td>
 <td class="data"><span class="bld">${course.name}</span> (Stage <fmt:int value="${course.stage}" />)
 <el:cmdbutton url="course" link="${course}" label="VIEW COURSE" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Assigned on</td>
 <td class="data"><fmt:date t="HH:mm" date="${checkRide.date}" /></td>
</tr>
<tr>
 <td class="label">Submitted on</td>
 <td class="data"><fmt:date t="HH:mm" date="${checkRide.submittedOn}" /></td>
</tr>
<c:if test="${access.canScore}">
<tr>
 <td class="label">Check Ride Status</td>
 <td class="data"><el:check type="radio" name="passFail" className="req" idx="*" options="${passFail}" value="${score}" /></td>
</tr>
</c:if>
<tr>
 <td class="label top">Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="90%" height="4" readOnly="${!access.canScore}" resize="${access.canScore}">${checkRide.comments}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;
<c:if test="${access.canDelete}"><el:cmdbutton url="examdelete" link="${checkRide}" op="checkride" label="DELETE CHECK RIDE" /></c:if>
<c:if test="${access.canScore}">&nbsp;<el:button type="submit" label="SCORE CHECK RIDE" /></c:if>
 </td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
