<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Event</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="datePicker" />
<content:js name="airportRefresh" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.name, 5, 'Event Name')) return false;
if (!validateCombo(form.airportA, 'Destination Airport')) return false;
if (!validateText(form.route, 5, 'Default Route')) return false;
if (!validateText(form.briefing, 15, 'Flight Briefing')) return false;
if (form.airportDCodes.value.length < 1) {
	alert('Please provide at least one Departure Airport.');
	form.airportD.focus();
	return false;
}

setSubmit();
disableButton('SaveButton');
return true;
}

function doRefresh()
{
var f = document.forms[0];
if (f.airportD.selectedIndex == 0) {
	alert('Select another Departure Airport.');
	f.airportD.focus();
	return false;
}

f.action = f.action + '?op=refresh';
f.submit();
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@ include file="/jsp/event/header.jsp" %> 
<%@ include file="/jsp/event/sideMenu.jsp" %>
<content:sysdata var="allEQ" name="eqtypes" />
<content:sysdata var="defaultNetwork" name="online.default_network" />
<c:set var="network" value="${empty event ? defaultNetwork : event.networkName}" scope="request" />

<!-- Main Body Frame -->
<div id="main">
<el:form action="eventsave.do" method="post" linkID="${((empty event) || (event.ID == 0)) ? '' : '0x'}${event.ID}" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
<c:if test="${empty event}">
 <td colspan="2">NEW <content:airline /> ONLINE EVENT</td>
</c:if>
<c:if test="${!empty event}">
 <td colspan="2"><content:airline /> ONLINE EVENT - ${event.name}</td>
</c:if>
</tr>
<tr>
 <td class="label">Event Name</td>
 <td class="data"><el:text name="name" idx="*" size="48" max="80" value="${event.name}" /></td>
</tr>
<tr>
 <td class="label">Online Network</td>
 <td class="data"><el:check type="radio" name="network" idx="*" className="sec bld" options="${networks}" value="${network}" /></td>
</tr>
<tr>
 <td class="label">Start Date/Time</td>
 <td class="data"><el:text name="startDate" idx="*" size="10" max="10" value="${fn:dateFmt(startTime, 'MM/dd/yyyy')}" />
&nbsp;<el:text name="startTime" idx="*" size="4" max="5" value="${fn:dateFmt(startTime, 'HH:mm')}" />
&nbsp;<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].startDate')" />
&nbsp;<span class="small">Your time zone is ${pageContext.request.userPrincipal.TZ.name}.</span></td>
</tr>
<tr>
 <td class="label">End Date/Time</td>
 <td class="data"><el:text name="endDate" idx="*" size="10" max="10" value="${fn:dateFmt(endTime, 'MM/dd/yyyy')}" />
&nbsp;<el:text name="endTime" idx="*" size="4" max="5" value="${fn:dateFmt(endTime, 'HH:mm')}" />
&nbsp;<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].endDate')" />
&nbsp;<span class="small">Your time zone is ${pageContext.request.userPrincipal.TZ.name}.</span></td>
</tr>
<tr>
 <td class="label">Signups Close at</td>
 <td class="data"><el:text name="closeDate" idx="*" size="10" max="10" value="${fn:dateFmt(signupDeadline, 'MM/dd/yyyy')}" />
&nbsp;<el:text name="closeTime" idx="*" size="4" max="5" value="${fn:dateFmt(signupDeadline, 'HH:mm')}" />
&nbsp;<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].closeDate')" />
&nbsp;<span class="small">Your time zone is ${pageContext.request.userPrincipal.TZ.name}.</span></td>
</tr>
<tr>
 <td class="label">Destination Airport</td>
 <td class="data"><el:combo name="airportA" size="1" firstEntry="" options="${airports}" value="${event.airportA}" />
&nbsp;<el:text name="aaCode" idx="*" size="3" max="4" onBlur="void setAirport(document.forms[0].airportA, this.value)" /></td>
</tr>
<tr>
 <td class="label" valign="top">${fn:sizeof(event.airportD)} Departure Airport(s)</td>
 <td class="data"><c:if test="${!empty event.airportD}">
<c:forEach var="airport" items="${event.airportD}">
<b>${airport.name} (<fmt:airport airport="${airport}" />)</b><br />
</c:forEach>
</c:if>
<el:combo name="airportD" size="1" options="${airports}" firstEntry="" />
&nbsp;<el:text name="adCode" idx="*" size="3" max="4" onBlur="void setAirport(document.forms[0].airportD, this.value)" />
&nbsp;<el:button onClick="void doRefresh()" className="BUTTON" label="ADD AIRPORT" /></td>
</tr>
<tr>
 <td class="label">Default Route</td>
 <td class="data"><el:text name="route" idx="*" size="64" max="128" value="${event.route}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Flight Briefing</td>
 <td class="data"><el:textbox name="briefing" idx="*" width="120" height="15">${event.briefing}</el:textbox></td>
</tr>
<tr>
 <td class="label" valign="top">Equipment Types</td>
 <td class="data"><span class="sec small">These should be unselected unless signups are restricted 
to a specific set of equipment.</span><br />
<el:check name="eqTypes" idx="*" cols="9" width="85" separator="<div style=\"clear:both;\" />" className="small" checked="${event.equipmentTypes}" options="${allEQ}" /></td>
</tr>
<c:if test="${!empty charts}">
<tr class="title caps">
 <td colspan="2">APPROACH CHARTS</td>
</tr>
<c:forEach var="chartAirport" items="${chartAirports}">
<c:set var="apCharts" value="${charts[chartAirport]}" scope="request" />
<tr>
 <td class="label" valign="top">${chartAirport.name} (<fmt:airport airport="${chartAirport}" />)</td>
 <td class="data"><el:check name="charts" cols="4" width="185" checked="${event.charts}" options="${apCharts}" separator="<div style=\"clear:both;\" />" className="small" /></td>
</tr>
</c:forEach>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td>&nbsp;
<c:if test="${access.canEdit}">
 <el:button type="SUBMIT" className="BUTTON" label="SAVE ONLINE EVENT" />
</c:if>
</tr>
</el:table>
<el:text name="airportDCodes" type="hidden" value="${adCodes}" />
</el:form>
<content:copyright />
</div>
</body>
</html>
