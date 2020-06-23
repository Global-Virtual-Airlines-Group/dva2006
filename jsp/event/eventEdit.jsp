<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Online Event</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:json />
<content:js name="datePicker" />
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<fmt:aptype var="useICAO" />
<script async>
golgotha.local.updateSignups = function() {
	const f = document.forms[0];
	const tg = !f.canSignup.checked;
	golgotha.util.disable(f.closeDate, tg);
	golgotha.util.disable(f.closeTime, tg);
	golgotha.util.disable(f.airportD, tg);
	golgotha.util.disable(f.airportA, tg);
	golgotha.util.disable(f.adCode, tg);
	golgotha.util.disable(f.aaCode, tg);
	golgotha.util.disable(f.route, tg);
	golgotha.util.disable(f.routeName, tg);
	golgotha.util.disable(f.maxSignups, tg);
	golgotha.util.disable(f.signupURL, tg);
	golgotha.util.disable('CloseCalendarButton', tg);
	return true;
};

golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.name, l:5, t:'Event Name'});
	golgotha.form.validate({f:f.airportD, t:'Departure Airport'});
	golgotha.form.validate({f:f.airportA, t:'Destination Airport'});
	golgotha.form.validate({f:f.route, l:5, t:'Default Route'});
	golgotha.form.validate({f:f.airline, min:1, t:'Participating Airline'});
	golgotha.form.validate({f:f.briefing, l:15, t:'Flight Briefing'});
	golgotha.form.validate({f:f.bannerImg, ext:['jpg','png','gif'], t:'Banner Image', empty:true});
	golgotha.form.submit(f);
	return true;
};

golgotha.onDOMReady(function() {
	const f = document.forms[0];
	golgotha.airportLoad.config.doICAO = ${useICAO};
	golgotha.airportLoad.setHelpers([f.airportD,f.airportA]);
	golgotha.local.updateSignups();
});
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>
<content:sysdata var="dateFmt" name="time.date_format" />
<content:sysdata var="defaultNetwork" name="online.default_network" />
<content:sysdata var="bannerX" name="online.banner_max.x" />
<content:sysdata var="bannerY" name="online.banner_max.y" />
<content:sysdata var="bannerSize" name="online.banner_max.size" />
<content:sysdata var="airlines" name="apps" mapValues="true" />
<c:set var="network" value="${empty event ? defaultNetwork : event.network}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="eventsave.do" method="post" link="${event}" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
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
 <td class="data"><el:text name="name" className="pri bld" required="true" idx="*" size="48" max="80" value="${event.name}" /></td>
</tr>
<tr>
 <td class="label">Online Network</td>
 <td class="data"><el:check type="radio" name="network" idx="*" className="sec bld req" options="${networks}" value="${network}" /></td>
</tr>
<tr>
 <td class="label">Event Starts on</td>
 <td class="data"><el:text name="startDate" required="true" idx="*" size="10" max="10" value="${fn:dateFmt(startTime, 'MM/dd/yyyy')}" /> at <el:text name="startTime" required="true"  idx="*" size="4" max="5" value="${fn:dateFmt(startTime, 'HH:mm')}" />
&nbsp;<el:button label="CALENDAR" onClick="void show_calendar('forms[0].startDate')" />&nbsp;<span class="small">All dates/times are ${user.TZ.name}. (Format: ${dateFmt} HH:mm)</span></td>
</tr>
<tr>
 <td class="label">Event Ends on</td>
 <td class="data"><el:text name="endDate" required="true" idx="*" size="10" max="10" value="${fn:dateFmt(endTime, 'MM/dd/yyyy')}" /> at <el:text name="endTime" required="true" idx="*" size="4" max="5" value="${fn:dateFmt(endTime, 'HH:mm')}" />
&nbsp;<el:button label="CALENDAR" onClick="void show_calendar('forms[0].endDate')" />&nbsp;<span class="small">All dates/times are ${user.TZ.name}. (Format: ${dateFmt} HH:mm)</span></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="canSignup" idx="*" value="true" checked="${empty event ? true : event.canSignup}" label="Allow Signups for this Online Event" onChange="void golgotha.local.updateSignups()" /></td>
</tr>
<tr>
 <td class="label">Airlines</td>
 <td class="data"><el:check name="airlines" width="175" options="${airlines}" className="req" checked="${(empty event) ? myAirline : event.airlines}" /></td>
</tr>
<tr>
 <td class="label">Signup URL</td>
 <td class="data"><el:text name="signupURL" idx="*" size="80" max="224" value="${event.signupURL}" className="small" /></td>
</tr>
<tr>
 <td class="label">Signups Close at</td>
 <td class="data"><el:text name="closeDate" required="true" idx="*" size="10" max="10" value="${fn:dateFmt(signupDeadline, 'MM/dd/yyyy')}" /> at <el:text name="closeTime" idx="*" size="4" max="5" value="${fn:dateFmt(signupDeadline, 'HH:mm')}" className="req" />
&nbsp;<el:button ID="CloseCalendarButton" label="CALENDAR" onClick="void show_calendar('forms[0].closeDate')" />&nbsp;<span class="small">Your time zone is ${user.TZ.name}.</span></td>
</tr>
<c:if test="${event.hasBanner}">
<tr>
 <td class="label top">Banner Image</td>
 <td class="data"><img alt="${event.name} Banner" src="/event/${event.hexID}" /><br />
<el:box name="removeBannerImg" value="true" label="Remove Event Banner Image" /></td>
</tr>
</c:if>
<tr>
 <td class="label top">Upload Banner Image</td>
 <td class="data"><el:file name="bannerImg" className="small" idx="*" size="80" max="144" /><br />
<span class="small sec">The maximum size for a banner image is <fmt:int value="${bannerX}" />x<fmt:int value="${bannerY}" /> pixels, and the maximum file size is <fmt:int value="${bannerSize}" /> bytes.</span>
<content:hasmsg><br /><span class="bld error"><content:sysmsg /></span></content:hasmsg></td>
</tr>
<tr>
 <td class="label top">ATC Contact Addresses</td>
 <td class="data"><el:textbox name="contactAddrs" idx="*" width="50" height="2">${addrs}</el:textbox></td>
</tr>
<tr>
 <td class="label top">Equipment Types</td>
 <td class="data"><span class="sec small">These should be unselected unless signups are restricted to a specific set of equipment.</span><br />
<el:check name="eqTypes" idx="*" cols="9" width="95" newLine="true" className="small" checked="${event.equipmentTypes}" options="${allEQ}" /></td>
</tr>
<tr class="title caps">
 <td colspan="2">FLIGHT BRIEFING</td>
</tr>
<c:set var="hasTextBriefing" value="${!empty event.briefing && !event.briefing.isPDF}" scope="page" />
<tr>
 <td class="label top">Briefing Text</td>
 <td class="data"><el:textbox name="briefing" idx="*" width="90%" className="req" height="5" resize="true">${hasTextBriefing ? event.briefing : ''}</el:textbox></td>
</tr>
<tr>
 <td class="label">Attach File</td>
 <td class="data"><el:file name="briefPDF" idx="*" className="small" size="96" max="144" /><c:if test="${event.briefing.isPDF}"><el:box name="deleteBrief" value="true" label="Delete existing Briefing" /></c:if>
<c:if test="${hasTextBriefing}"><span class="small ita nophone"> Uploading a Briefing file will overwrite the existing Briefing!</span></c:if></td>
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
<el:text name="adCode" idx="*" size="3" max="4" onBlur="void document.forms[0].airportD.setAirport(this.value)" /></td>
</tr>
<tr>
 <td class="label">Destination Airport</td>
 <td class="data"><el:combo name="airportA" idx="*" size="1" options="${airports}" firstEntry="" className="req" />&nbsp;
<el:text name="aaCode" idx="*" size="3" max="4" onBlur="void document.forms[0].airportA.setAirport(this.value)" /></td>
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
<c:set var="hasName" value="${!empty route.name}" scope="page" />
<view:row entry="${route}">
 <td class="label top" rowspan="2">Route #<fmt:int value="${route.routeID}" /></td>
 <td class="data"><c:if test="${hasName}"><b>${route.name}</b> </c:if>${route.airportD.name} (<fmt:airport airport="${route.airportD}" />) - ${route.airportA.name} (<fmt:airport airport="${route.airportA}" />)</td>
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
<c:forEach var="chartAirport" items="${charts.keySet()}">
<c:set var="apCharts" value="${charts[chartAirport]}" scope="page" />
<tr>
 <td class="label top">${chartAirport.name} (<fmt:airport airport="${chartAirport}" />)</td>
 <td class="data"><el:check name="charts" cols="4" width="200" checked="${event.charts}" options="${apCharts}" newLine="true" className="small" /></td>
</tr>
</c:forEach>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;<c:if test="${access.canEdit}"><el:button type="submit" label="SAVE ONLINE EVENT" /></c:if></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
