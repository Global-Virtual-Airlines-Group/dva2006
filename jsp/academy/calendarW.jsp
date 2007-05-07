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
<title><content:airline /> Flight Academy Instruction Calendar</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="calendar" />
<content:pics />
<content:js name="common" />
<content:js name="datePicker" />
<script language="JavaScript" type="text/javascript">
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
<el:table className="form" space="default" pad="default">
<tr class="title">
 <td width="70%" class="caps"><content:airline /> INSTRUCTION CALENDAR - WEEK OF <fmt:date fmt="d" date="${startDate}" d="MMMM dd, yyyy" /></td>
<c:if test="${isMine}">
 <td width="10%" class="mid"><el:cmd url="academycalendar" op="7" startDate="${startDate}">ALL SESSIONS</el:cmd></td>
</c:if>
<c:if test="${!isMine && !empty user}">
 <td width="10%" class="mid"><el:cmd url="academycalendar" op="7" link="${user}" startDate="${startDate}">MY SESSIONS</el:cmd></td>
</c:if>
 <td class="right">CALENDAR TYPE <el:combo name="op" size="1" idx="*" options="${typeOptions}" value="7" onChange="void switchType(this)" /></td>
</tr>
</el:table>
<div class="mid">
<calendar:week date="cDate" startDate="${startDate}" entries="${sessions}" topBarClass="dayHdr" 
	dayBarClass="dayHdr" tableClass="calendar" contentClass="contentW" scrollClass="scroll" cmd="academycalendar">
<calendar:entry name="session">
<c:if test="${fn:isBusyTime(session)}">
<c:set var="ins" value="${pilots[session.ID]}" scope="request" />
<c:set var="busyAccess" value="${accessMap[busy]}" scope="request" />
<span class="warn bld caps">${ins.name} IS BUSY</span><br />
<fmt:date fmt="t" t="HH:mm" date="${session.startTime}" /> - <fmt:date fmt="t" t="HH:mm" date="${session.endTime}" />
<c:if test="${busyAccess.canDelete}"><br />
<el:cmd url="insbusydelete" link="${ins}" op="${fn:dateFmt(busy.startTime, 'MMddyyyyHHmm')}" className="pri small bld">DELETE</el:cmd></c:if>
</c:if>
<c:if test="${!fn:isBusyTime(session)}">
<c:set var="pilot" value="${pilots[session.pilotID]}" scope="request" />
<c:set var="ins" value="${pilots[session.instructorID]}" scope="request" />
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
</body>
</html>
