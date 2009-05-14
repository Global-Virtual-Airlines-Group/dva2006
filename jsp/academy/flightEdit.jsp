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
<title><c:if test="${empty flight}">New </c:if><content:airline /> Flight Academy Instruction Flight</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="datePicker" />
<content:js name="hourCalc" />
<script language="javascript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.eqType, 'Equipment Type')) return false;
if (!validateCombo(form.instructor, 'Instructor Pilot')) return false;
if (!validateText(form.flightDate, 10, 'Flight Date')) return false;
if (!validateCombo(form.flightTime, 'Logged Hours')) return false;

setSubmit();
disableButton('SaveButton');
disableButton('CalcButton');
return true;
}
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
<el:form method="post" action="insflight.do" link="${flight}" op="save" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
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
 <td class="data">${pilot.rank} ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></td>
</tr>
<tr>
 <td class="label">Instructor</td>
 <td class="data"><el:combo name="instructor" size="1" idx="*" options="${instructors}" value="${ins.name}" className="req" firstEntry="< INSTRUCTOR >" /></td>
</tr>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" options="${eqTypes}" value="${flight.equipmentType}" className="req" firstEntry="< EQUIPMENT >" /></td>
</tr>
<tr>
 <td class="label">Flown on</td>
 <td class="data"><el:text name="logDate" idx="*" size="10" max="10" className="req" value="${fn:dateFmt(flight.date, 'MM/dd/yyyy')}" />
 <el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].logDate')" /></td>
</tr>
<c:set var="tmpH" value="${empty flight ? '' : (flight.length  / 10)}" scope="page" />
<c:set var="tmpM" value="${empty flight ? '' : (flight.length % 10) * 6}" scope="page" />
<tr>
 <td class="label">Logged Time</td>
 <td class="data"><el:combo name="flightTime" idx="*" size="1" className="req" firstEntry="< HOURS >" options="${flightTimes}" value="${flightTime}" />&nbsp;
<el:text name="tmpHours" size="1" max="2" value="${tmpH}" /> hours, <el:text name="tmpMinutes" size="1" max="2" value="${tmpM}" /> minutes&nbsp;
<el:button ID="CalcButton" className="BUTTON" label="CALCULATE" onClick="void hoursCalc()" /></td>
</tr>
<tr>
 <td class="label top">Remarks</td>
 <td class="data"><el:textbox idx="*" name="comments" width="80%" height="5">${flight.comments}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SAVE FLIGHT REPORT" /></td>
</tr>
</el:table>
<c:if test="${empty flight}"><el:text name="courseID" type="hidden" value="${fn:hex(course.ID)}" /></c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
