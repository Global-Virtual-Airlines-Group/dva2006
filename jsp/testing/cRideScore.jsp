<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Check Ride</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCheckBox(form.passFail, 1, 'Check Ride status')) return false;

setSubmit();
disableButton('SubmitButton');
disableButton('DeleteButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="crscore.do" link="${checkRide}" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">${checkRide.aircraftType} CHECK RIDE FOR ${pilot.name}</td>
</tr>
<tr>
 <td class="label">${fn:pending(checkRide) ? 'Assigned' : 'Scored'} by</td>
 <td class="data sec bld">${scorer.name}</td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><span class="sec bld">${checkRide.equipmentType}</span> (Stage <fmt:int value="${checkRide.stage}" />)</td>
</tr>
<c:if test="${(checkRide.flightID != 0) && (!empty pirep)}">
<tr>
 <td class="label">ACARS Flight ID</td>
 <td class="data sec bld"><fmt:int value="${checkRide.flightID}" /> <el:cmdbutton ID="PIREPButton" url="crview" link="${checkRide}" label="VIEW FLIGHT REPORT" /></td>
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
 <td class="data"><fmt:date fmt="d" date="${checkRide.date}" /></td>
</tr>
<tr>
 <td class="label">Assigned by</td>
 <td class="data bld">${scorer.name}</td>
</tr>
<tr>
 <td class="label">Submitted on</td>
 <td class="data"><fmt:date fmt="d" date="${checkRide.submittedOn}" /></td>
</tr>
<c:if test="${access.canScore}">
<tr>
 <td class="label">Check Ride Status</td>
 <td class="data"><el:check type="radio" name="passFail" className="req" idx="*" options="${passFail}" value="${score}" /></td>
</tr>
</c:if>
<tr>
 <td class="label top">Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="80%" height="6" readOnly="${!access.canScore}">${checkRide.comments}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td>
<c:if test="${access.canDelete}">
 <el:cmdbutton ID="DeleteButton" url="examdelete" link="${checkRide}" op="checkride" label="DELETE CHECK RIDE" />
</c:if>
<c:if test="${access.canScore}">
 <el:button ID="SubmitButton" type="submit" className="BUTTON" label="SCORE CHECK RIDE" />
</c:if>
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
