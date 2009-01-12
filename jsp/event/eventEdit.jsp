<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
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
<content:googleAnalytics eventSupport="true" />
<script language="JavaScript" type="text/javascript">
function updateSignups()
{
// Selectively enable fields if signups enabled
var f = document.forms[0];
enableObject(f.closeDate, f.canSignup.checked);
enableObject(f.closeTime, f.canSignup.checked);
enableObject(f.airportD, f.canSignup.checked);
enableObject(f.airportA, f.canSignup.checked);
enableObject(f.adCode, f.canSignup.checked);
enableObject(f.aaCode, f.canSignup.checked);
enableObject(f.route, f.canSignup.checked);
enableObject(f.routeName, f.canSignup.checked);
enableObject(f.maxSignups, f.canSignup.checked);
enableObject(f.signupURL, !f.canSignup.checked);

// Get the calendar button
enableElement('CloseCalendarButton', f.canSignup.checked);
return true;
}

function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.name, 5, 'Event Name')) return false;
if (!validateCombo(form.airportD, 'Departure Airport')) return false;
if (!validateCombo(form.airportA, 'Destination Airport')) return false;
if (!validateText(form.route, 5, 'Default Route')) return false;
if (!validateCheckBox(form.airline, 1, 'Participating Airline')) return false;
if (!validateText(form.briefing, 15, 'Flight Briefing')) return false;
if (!validateFile(form.bannerImg, 'jpg,png,gif', 'Banner Image')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="void updateSignups()">
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>
<content:sysdata var="dateFmt" name="time.date_format" />
<content:sysdata var="defaultNetwork" name="online.default_network" />
<content:sysdata var="sigX" name="online.banner_max.x" />
<content:sysdata var="sigY" name="online.banner_max.y" />
<content:sysdata var="sigSize" name="online.banner_max.size" />
<content:sysdata var="airlines" name="apps" mapValues="true" />
<c:set var="network" value="${empty event ? defaultNetwork : event.networkName}" scope="request" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="eventsave.do" method="post" link="${event}" allowUpload="true" validate="return validate(this)">
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
 <td class="data"><el:text name="name" className="pri bld req" idx="*" size="48" max="80" value="${event.name}" /></td>
</tr>
<tr>
 <td class="label">Online Network</td>
 <td class="data"><el:check type="radio" name="network" idx="*" className="sec bld req" options="${networks}" value="${network}" /></td>
</tr>
<tr>
 <td class="label">Event Starts on</td>
 <td class="data"><el:text name="startDate" idx="*" size="10" max="10" value="${fn:dateFmt(startTime, 'MM/dd/yyyy')}" className="req" />
 at <el:text name="startTime" idx="*" size="4" max="5" value="${fn:dateFmt(startTime, 'HH:mm')}" className="req" />
&nbsp;<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].startDate')" />
&nbsp;<span class="small">All dates/times are ${pageContext.request.userPrincipal.TZ.name}. (Format: ${dateFmt} HH:mm)</span></td>
</tr>
<tr>
 <td class="label">Event Ends on</td>
 <td class="data"><el:text name="endDate" idx="*" size="10" max="10" value="${fn:dateFmt(endTime, 'MM/dd/yyyy')}" className="req" />
 at <el:text name="endTime" idx="*" size="4" max="5" value="${fn:dateFmt(endTime, 'HH:mm')}" className="req" />
&nbsp;<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].endDate')" />
&nbsp;<span class="small">All dates/times are ${pageContext.request.userPrincipal.TZ.name}. (Format: ${dateFmt} HH:mm)</span></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="canSignup" idx="*" value="true" checked="${empty event ? true : event.canSignup}" label="Allow Signups for this Online Event" onChange="void updateSignups()" /></td>
</tr>
<tr>
 <td class="label">Airlines</td>
 <td class="data"><el:check name="airlines" width="175" options="${airlines}" className="req" checked="${(empty event) ? airlines : event.airlines}" /></td>
</tr>
<tr>
 <td class="label">Signup URL</td>
 <td class="data"><el:text name="signupURL" idx="*" size="80" max="224" value="${event.signupURL}" className="small" /></td>
</tr>
<tr>
 <td class="label">Signups Close at</td>
 <td class="data"><el:text name="closeDate" idx="*" size="10" max="10" value="${fn:dateFmt(signupDeadline, 'MM/dd/yyyy')}" className="req" />
 at <el:text name="closeTime" idx="*" size="4" max="5" value="${fn:dateFmt(signupDeadline, 'HH:mm')}" className="req" />
&nbsp;<el:button ID="CloseCalendarButton" className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].closeDate')" />
&nbsp;<span class="small">Your time zone is ${pageContext.request.userPrincipal.TZ.name}.</span></td>
</tr>
<c:if test="${event.hasBanner}">
<tr>
 <td class="label">Banner Image</td>
 <td class="data"><el:img caption="${event.name} Banner" src="/event/${event.hexID}" /><br />
<el:box name="removeBannerImg" value="true" label="Remove Event Banner Image" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Upload Banner Image</td>
 <td class="data"><el:file name="bannerImg" className="small" idx="*" size="80" max="144" /><br />
<span class="small sec">The maximum size for a banner image is <fmt:int value="${bannerX}" />x<fmt:int value="${bannerY}" /> 
pixels, and the maximum file size is <fmt:int value="${bannerSize}" /> bytes.</span>
<c:if test="${!empty system_message}"><br /><span class="bld error">${system_message}</span></c:if></td>
</tr>
<tr>
 <td class="label" valign="top">ATC Contact Addresses</td>
 <td class="data"><el:textbox name="contactAddrs" idx="*" width="50" height="2">${addrs}</el:textbox></td>
</tr>
<tr>
 <td class="label" valign="top">Flight Briefing</td>
 <td class="data"><el:textbox name="briefing" idx="*" width="90%" className="req" height="15">${event.briefing}</el:textbox></td>
</tr>
<tr>
 <td class="label" valign="top">Equipment Types</td>
 <td class="data"><span class="sec small">These should be unselected unless signups are restricted 
to a specific set of equipment.</span><br />
<el:check name="eqTypes" idx="*" cols="9" width="85" newLine="true" className="small" checked="${event.equipmentTypes}" options="${allEQ}" /></td>
</tr>
<tr class="title caps">
 <td colspan="2">AVAILABLE FLIGHT ROUTES</td>
</tr>
<c:if test="${empty event}">
<!-- Initial Flight Route -->
<tr>
 <td class="label">Route Name</td>
 <td class="data"><el:text name="routeName" idx="*" size="48" max="96" className="bld req" value="" /></td>
</tr>
<tr>
 <td class="label">Departure Airport</td>
 <td class="data"><el:combo name="airportD" idx="*" size="1" options="${airports}" firstEntry="" className="req" />&nbsp;
<el:text name="adCode" idx="*" size="3" max="4" onBlur="void setAirport(document.forms[0].airportD, this.value)" /></td>
</tr>
<tr>
 <td class="label">Destination Airport</td>
 <td class="data"><el:combo name="airportA" idx="*" size="1" options="${airports}" firstEntry="" className="req" />&nbsp;
<el:text name="aaCode" idx="*" size="3" max="4" onBlur="void setAirport(document.forms[0].airportA, this.value)" /></td>
</tr>
<tr>
 <td class="label">Flight Routing</td>
 <td class="data"><el:text name="route" idx="*" size="110" max="640" value="" className="req" /></td>
</tr>
<tr>
 <td class="label">Maximum Signups</td>
 <td class="data"><el:text name="maxSignups" idx="*" size="2" max="4" value="" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isRNAV" idx="*" value="true" label="This is an RNAV Route" checked="false" /></td>
</tr>
</c:if>
<c:if test="${!empty event}">
<c:forEach var="route" items="${event.routes}">
<c:set var="hasName" value="${!empty route.name}" scope="request" />
<view:row entry="${route}">
 <td class="label" valign="top" rowspan="2">Route #<fmt:int value="${route.routeID}" /></td>
 <td class="data"><c:if test="${hasName}"><b>${route.name}</b> </c:if>${route.airportD.name} (<fmt:airport airport="${route.airportD}" />)
 - ${route.airportA.name} (<fmt:airport airport="${route.airportA}" />)</td>
</view:row>
<tr>
 <td class="data">${route.route}</td>
</tr>
</c:forEach>
</c:if>
<c:if test="${!empty charts}">
<tr class="title caps">
 <td colspan="2">APPROACH CHARTS</td>
</tr>
<c:forEach var="chartAirport" items="${fn:keys(charts)}">
<c:set var="apCharts" value="${charts[chartAirport]}" scope="request" />
<tr>
 <td class="label" valign="top">${chartAirport.name} (<fmt:airport airport="${chartAirport}" />)</td>
 <td class="data"><el:check name="charts" cols="4" width="185" checked="${event.charts}" options="${apCharts}" newLine="true" className="small" /></td>
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
</el:form>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
