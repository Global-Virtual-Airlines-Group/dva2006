<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_calendar.tld" prefix="calendar" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Event Calendar</title>
<content:sysdata var="airlineName" name="airline.name" />
<content:css name="main" />
<content:css name="form" />
<content:css name="calendar" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:rss title="${airlineName} Online Events" path="/event_rss.ws" />
<script type="text/javascript">
golgotha.local.switchType = function(combo) {
	self.location = '/eventcalendar.do?op=' + escape(golgotha.form.getCombo(combo)) + '&startDate=<fmt:date fmt="d" d="MM/dd/yyyy" date="${startDate}" />';
	return true;
};

golgotha.local.expandSection = function(id) {
    var s = document.getElementById(id);
    if (s) s.style.display = (s.style.display == 'none') ? '' : 'none';
    return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="eventcalendar.do" method="get" validate="return false">
<el:table className="form">
<tr class="title">
 <td style="width:80%" class="caps"><content:airline /> ONLINE EVENT CALENDAR - WEEK OF <fmt:date fmt="d" date="${startDate}" d="MMMM dd, yyyy" /></td>
 <td class="right">&nbsp;<span class="nophone">CALENDAR TYPE <el:combo name="op" size="1" idx="*" options="${typeOptions}" value="7" onChange="void golgotha.local.switchType(this)" /></span></td>
</tr>
</el:table>
<div class="mid">
<calendar:week date="cDate" startDate="${startDate}" entries="${events}" topBarClass="dayHdr" dayBarClass="dayHdr" tableClass="calendar" contentClass="contentW" scrollClass="scroll" cmd="eventcalendar">
<calendar:entry name="event">
<c:set var="eventSize" value="${event.signups.size()}" scope="page" />
<c:set var="eventLargeSignup" value="${eventSize > 20}" scope="page" />
<el:cmd url="event" link="${event}" className="pri bld">${event.name}</el:cmd><br />
<span class="sec small bld">${event.network}</span> <span class="small"><fmt:date fmt="t" t="HH:mm" date="${event.startTime}" /> - <fmt:date fmt="t" t="HH:mm" date="${event.endTime}" /></span><br />
<c:forEach var="route" items="${event.routes}">
<c:if test="${((route.maxSignups == 0) || (route.signups < route.maxSignups))}">
<div class="small">${route.airportD.name} - ${route.airportA.name}<br />
<span class="sec"><fmt:distance longUnits="true" value="${route.distance}" /></span></div></c:if></c:forEach>
<c:if test="${!event.canSignup}">
<c:if test="${!empty event.signupURL}">
<el:link external="true" url="${event.signupURL}" target="eventSignup" className="bld small">SIGNUP</el:link>
</c:if>
<c:if test="${empty event.signupURL}">
<span class="small warn bld">SIGNUPS NOT AVAILABLE</span>
</c:if>
</c:if>
<c:if test="${(eventSize == 0) && event.canSignup}">
<span class="small bld">NO SIGNUPS YET</span>
</c:if>
<c:if test="${eventSize > 0}">
<br />
<c:if test="${eventLargeSignup}">
<a href="javascript:golgotha.local.expandSection('eSignup${event.hexID}')" class="small ter bld"><fmt:int value="${eventSize}" /> Participants</a>
</c:if>
<c:if test="${!eventLargeSignup}">
<span class="small ter bld"><fmt:int value="${eventSize}" /> Participant<c:if test="${eventSize > 1}">s</c:if></span>
</c:if>
<br />
<div id="eSignup${event.hexID}" class="small"<c:if test="${eventLargeSignup}"> style="display:none;"</c:if>>
<c:forEach var="signup" items="${event.signups}">
<c:set var="pilot" value="${pilots[signup.pilotID]}" scope="page" />
${pilot.name} <c:if test="${!empty pilot.pilotCode}">(${pilot.pilotCode})<br /></c:if>
</c:forEach>
</div>
</c:if>
<calendar:spacer><hr /></calendar:spacer>
</calendar:entry>
<calendar:empty>-</calendar:empty>
</calendar:week>
</div>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
