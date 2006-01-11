<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_calendar.tld" prefix="calendar" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Event Calendar</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="calendar" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function switchType(combo)
{
var cType = combo.options[combo.selectedIndex].value;
self.location = '/eventcalendar.do?op=' + cType + '&startDate=<fmt:date fmt="d" d="MM/dd/yyyy" date="${startDate}" />';
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jsp" %> 
<%@ include file="/jsp/event/sideMenu.jsp" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="eventcalendar.do" method="get" validate="return false">
<el:table className="form" space="default" pad="default">
<tr class="title">
 <td width="80%" class="caps"><content:airline /> ONLINE EVENT CALENDAR - <fmt:date fmt="d" date="${startDate}" d="MMMM yyyy" /></td>
 <td class="right">CALENDAR TYPE <el:combo name="op" size="1" idx="*" options="${typeOptions}" value="30" onChange="void switchType(this)" /></td>
</tr>
</el:table>
<div class="mid">
<calendar:month date="cDate" startDate="${startDate}" entries="${events}" topBarClass="dayHdr"
	dayBarClass="dayHdr" tableClass="calendar" contentClass="contentW" scrollClass="scroll" cmd="eventcalendar">
<calendar:entry name="event">
<el:cmd url="event" linkID="0x${event.ID}" className="pri bld">${event.name}</el:cmd><br />
<span class="sec small bld">${event.networkName}</span> <span class="small"><fmt:date fmt="t" t="HH:mm" date="${event.startTime}" /> 
- <fmt:date fmt="t" t="HH:mm" date="${event.endTime}" /><br />
<c:forEach var="route" items="${event.routes}">${route.airportD.name} - ${route.airportA.name}<br /></c:forEach>
<c:set var="eventSize" value="${fn:sizeof(event.signups)}" scope="request" />
<c:if test="${!event.canSignup}">
<span class="warn bld">SIGNUPS NOT AVAILABLE</span>
</c:if>
<c:if test="${(eventSize == 0) && event.canSignup}">
<span class="bld">NO SIGNUPS YET</span>
</c:if>
<c:if test="${eventSize > 0}">
<div class="ter bld"><fmt:int value="${eventSize}" /> Participant<c:if test="${eventSize > 1}">s</c:if></div>
<c:set var="eventSignups" value="${fn:subset(event.signups, 15)}" scope="request" />
<c:forEach var="signup" items="${eventSignups}">
<c:set var="pilot" value="${pilots[signup.pilotID]}" scope="request" />
${pilot.name} <c:if test="${!empty pilot.pilotCode}">(${pilot.pilotCode})<br /></c:if>
</c:forEach>
</c:if>
</span>
<calendar:spacer><hr /></calendar:spacer>
</calendar:entry>
<calendar:empty>-</calendar:empty>
</calendar:month>
</div>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><content:filter roles="Event"><el:cmdbutton url="eventedit" label="NEW ONLINE EVENT" /></content:filter>&nbsp;</td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
