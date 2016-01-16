<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_calendar.tld" prefix="calendar" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Dispatcher Service Calendar</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="calendar" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:js name="datePicker" />
<script type="text/javascript">
golgotha.local.switchType = function(combo) {
	self.location = '/dspcalendar.do?op=' + escape(golgotha.form.getCombo(combo)) + '&startDate=<fmt:date fmt="d" d="MM/dd/yyyy" date="${startDate}" />';
	return true;
};

golgotha.local.validate = function(f)
{
if ((!golgotha.form.check()) || (!f.comments)) return false;
golgotha.form.validate({f:f.startDate, l:8, t:'Service Start Date'});
golgotha.form.validate({f:f.startTime, l:5, t:'Service Start Time'});
golgotha.form.validate({f:f.endDate, l:8, t:'Service End Date'});
golgotha.form.validate({f:f.endTime, l:5, t:'Service End Time'});
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
<el:form action="dspentry.do" op="save" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title">
 <td style="width:80%" class="caps"><content:airline /> ACARS DISPATCHER SERVICE CALENDAR - <fmt:date fmt="d" date="${startDate}" d="MMMM yyyy" tzName="local" /></td>
 <td class="right">CALENDAR TYPE <el:combo name="op" size="1" idx="*" options="${typeOptions}" value="30" onChange="void golgotha.local.switchType(this)" /></td>
</tr>
</el:table>
<div class="mid">
<calendar:month date="cDate" startDate="${startDate}" entries="${entries}" topBarClass="dayHdr"
	dayBarClass="dayHdr" tableClass="calendar" contentClass="contentM" scrollClass="scroll" cmd="dspcalendar">
<calendar:entry name="entry">
<c:choose>
<c:when test="${entry.getClass().simpleName == 'DispatchScheduleEntry'}">
<c:set var="dispatcher" value="${pilots[entry.authorID]}" scope="page" />
<c:set var="eAccess" value="${accessMap[entry]}" scope="page" />
<div class="small"><span class="pri bld">${dispatcher.name}</span> (${dispatcher.pilotCode})<br />
<fmt:date fmt="t" t="HH:mm" date="${entry.startTime}" /> - <fmt:date fmt="t" t="HH:mm" date="${entry.endTime}" />
<c:if test="${eAccess.canEdit}"><br /><el:cmd url="dspentry" link="${entry}" op="edit" className="small sec bld">EDIT ENTRY</el:cmd></c:if>
<c:if test="${!empty entry.comments}"><br />${entry.comments}</c:if></div>
</c:when>
<c:otherwise>
<c:set var="dispatcher" value="${pilots[entry.pilotID]}" scope="page" />
<div class="small"><span class="pri bld">${dispatcher.name}</span> (${dispatcher.pilotCode})<br />
<fmt:date fmt="t" t="HH:mm" date="${entry.startTime}" /> - <fmt:date fmt="t" t="HH:mm" date="${entry.endTime}" default=" " />
<c:if test="${entry.hasFlights}">
<br /><span class="bld"><fmt:int value="${fn:sizeof(entry.flights)}" /> Flights dispatched</span></c:if></div>
</c:otherwise>
</c:choose>
<calendar:spacer><hr /></calendar:spacer>
</calendar:entry>
<calendar:empty>-</calendar:empty>
</calendar:month>
</div>
<c:if test="${access.canCreate}">
<%@ include file="/jsp/dispatch/addServiceTime.jspf" %></c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
