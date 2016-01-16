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
<title><content:airline /> Flight Academy Instruction Calendar</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="calendar" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:js name="datePicker" />
<script type="text/javascript">
golgotha.local.switchType = function(combo) {
	var cType = combo.options[combo.selectedIndex].value;
	self.location = '/academycalendar.do?op=' + cType + '&startDate=<fmt:date fmt="d" d="MM/dd/yyyy" date="${startDate}" />';
	return true;
};

golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
if (!f.comments) return false;
golgotha.form.validate({f:f.instructor, t:'Flight Instructor'});
golgotha.form.validate({f:f.startDate, l:8, t:'Busy Start Date'});
golgotha.form.validate({f:f.startTime, l:5, t:'Busy Start Time'});
golgotha.form.validate({f:f.endDate, l:8, t:'Busy End Date'});
golgotha.form.validate({f:f.endTime, l:5, t:'Busy End Time'});
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

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="insbusysave.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title">
 <td style="width:70%" class="caps"><content:airline /> INSTRUCTION CALENDAR - <fmt:date fmt="d" date="${startDate}" d="MMMM yyyy" tzName="local" /></td>
<c:if test="${isMine}">
 <td style="width:10%" class="mid"><el:cmd url="academycalendar" op="31" startDate="${startDate}">ALL SESSIONS</el:cmd></td>
</c:if>
<c:if test="${!isMine && !empty user}">
 <td style="width:10%" class="mid"><el:cmd url="academycalendar" op="31" link="${user}" startDate="${startDate}">MY SESSIONS</el:cmd></td>
</c:if>
 <td class="right">CALENDAR TYPE <el:combo name="op" size="1" idx="*" options="${typeOptions}" value="30" onChange="void golgotha.local.switchType(this)" /></td>
</tr>
</el:table>
<div class="mid">
<calendar:month date="cDate" startDate="${startDate}" entries="${sessions}" topBarClass="dayHdr"
	dayBarClass="dayHdr" tableClass="calendar" contentClass="contentM" scrollClass="scroll" cmd="academycalendar">
<calendar:entry name="session">
<c:if test="${fn:isBusyTime(session)}">
<c:set var="ins" value="${pilots[session.ID]}" scope="page" />
<c:set var="busyAccess" value="${accessMap[busy]}" scope="page" />
<span class="warn bld caps">${ins.name} IS BUSY</span><br />
<fmt:date fmt="t" t="HH:mm" date="${session.startTime}" /> - <fmt:date fmt="t" t="HH:mm" date="${session.endTime}" />
<c:if test="${busyAccess.canDelete}"><br />
<el:cmd url="insbusydelete" link="${ins}" op="${fn:dateFmt(busy.startTime, 'MMddyyyyHHmm')}" className="pri small bld">DELETE</el:cmd></c:if>
</c:if>
<c:if test="${!fn:isBusyTime(session)}">
<c:set var="pilot" value="${pilots[session.pilotID]}" scope="page" />
<c:set var="ins" value="${pilots[session.instructorID]}" scope="page" />
<el:cmd url="isession" link="${session}" className="pri bld">${session.name}</el:cmd><br />
<fmt:date fmt="t" t="HH:mm" date="${session.startTime}" /> - <fmt:date fmt="t" t="HH:mm" date="${session.endTime}" /><br />
<span class="small"><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd> (${pilot.pilotCode})</span><br />
<span class="pri small">${ins.name} ${ins.pilotCode}</span>
</c:if>
<calendar:spacer><hr /></calendar:spacer>
</calendar:entry>
<calendar:empty>-</calendar:empty>
</calendar:month>
</div>
<c:if test="${access.canCreate || access.canProxyCreate}">
<%@ include file="/jsp/academy/addBusyTime.jspf" %>
</c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
