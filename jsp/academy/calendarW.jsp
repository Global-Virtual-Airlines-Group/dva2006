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
<content:js name="common" />
<content:js name="datePicker" />
<script type="text/javascript">
function switchType(combo)
{
var cType = combo.options[combo.selectedIndex].value;
self.location = '/academycalendar.do?op=' + cType + '&startDate=<fmt:date fmt="d" d="MM/dd/yyyy" date="${startDate}" />';
return true;
}

function validate(form)
{
if (!checkSubmit()) return false;
if (!form.comments) return false;

if (!validateCombo(form.instructor, 'Flight Instructor')) return false;
if (!validateCombo(form.startDate, 'Busy Start Date')) return false;
if (!validateCombo(form.startTime, 'Busy Start Time')) return false;
if (!validateCombo(form.endDate, 'Busy End Date')) return false;
if (!validateCombo(form.endTime, 'Busy End Time')) return false;
setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="insbusysave.do" method="post" validate="return validate(this)">
<el:table className="form">
<tr class="title">
 <td style="width:70%" class="caps"><content:airline /> INSTRUCTION CALENDAR - WEEK OF <fmt:date fmt="d" date="${startDate}" d="MMMM dd, yyyy" tzName="local" /></td>
<c:if test="${isMine}">
 <td style="width:10%" class="mid"><el:cmd url="academycalendar" op="7" startDate="${startDate}">ALL SESSIONS</el:cmd></td>
</c:if>
<c:if test="${!isMine && !empty user}">
 <td style="width:10%" class="mid"><el:cmd url="academycalendar" op="7" link="${user}" startDate="${startDate}">MY SESSIONS</el:cmd></td>
</c:if>
 <td class="right">CALENDAR TYPE <el:combo name="op" size="1" idx="*" options="${typeOptions}" value="7" onChange="void switchType(this)" /></td>
</tr>
</el:table>
<div class="mid">
<calendar:week date="cDate" startDate="${startDate}" entries="${sessions}" topBarClass="dayHdr" 
	dayBarClass="dayHdr" tableClass="calendar" contentClass="contentW" scrollClass="scroll" cmd="academycalendar">
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
</calendar:week>
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
