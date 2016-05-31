<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><c:if test="${empty flight}">New </c:if><content:airline /> Flight Academy Instruction Flight</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:js name="datePicker" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.eqType, t:'Equipment Type'});
golgotha.form.validate({f:f.instructor, t:'Instructor Pilot'});
golgotha.form.validate({f:f.flightDate, l:10, t:'Flight Date'});
golgotha.form.validate({f:f.flightTime, t:'Logged Hours'});
golgotha.form.submit(f);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>
<c:set var="pilot" value="${pilots[course.pilotID]}" scope="page" />
<c:set var="ins" value="${pilots[flight.instructorID]}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="insflight.do" link="${flight}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- PIREP Title Bar -->
<tr class="title caps">
<c:if test="${!empty flight}">
 <td colspan="2">FLIGHT ACADEMY FLIGHT FLOWN ON <fmt:date fmt="d" date="${flight.date}" /> by ${pilot.name}</td>
</c:if>
<c:if test="${empty flight}">
 <td colspan="2">NEW FLIGHT ACADEMY FLIGHT REPORT</td>
</c:if>
</tr>

<!-- PIREP Data -->
<tr>
 <td class="label">Student Pilot</td>
 <td class="data">${pilot.rank.name} ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></td>
</tr>
<tr>
 <td class="label">Instructor</td>
 <td class="data"><el:combo name="instructor" size="1" idx="*" options="${instructors}" value="${ins.name}" className="req" firstEntry="[ INSTRUCTOR ]" /></td>
</tr>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" options="${eqTypes}" value="${flight.equipmentType}" className="req" firstEntry="[ EQUIPMENT ]" /></td>
</tr>
<tr>
 <td class="label">Flown on</td>
 <td class="data"><el:text name="logDate" idx="*" size="10" max="10" className="req" value="${fn:dateFmt(flight.date, 'MM/dd/yyyy')}" />
 <el:button label="CALENDAR" onClick="void show_calendar('forms[0].logDate')" /></td>
</tr>
<c:set var="tmpH" value="${empty flight ? '' : (flight.length  / 10)}" scope="page" />
<c:set var="tmpM" value="${empty flight ? '' : (flight.length % 10) * 6}" scope="page" />
<tr>
 <td class="label">Logged Time</td>
 <td class="data"><el:combo name="flightTime" idx="*" size="1" className="req" firstEntry="[ HOURS ]" options="${flightTimes}" value="${flightTime}" /></td>
</tr>
<tr>
 <td class="label top">Remarks</td>
 <td class="data"><el:textbox idx="*" name="comments" width="80%" height="4" resize="true">${flight.comments}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE FLIGHT REPORT" /></td>
</tr>
</el:table>
<c:if test="${empty flight}"><el:text name="courseID" type="hidden" value="${course.hexID}" /></c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
