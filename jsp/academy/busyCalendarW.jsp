<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_calendar.tld" prefix="calendar" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Academy Instructor Busy Time Calendar</title>
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
self.location = '/busycalendar.do?op=' + cType + '&startDate=<fmt:date fmt="d" d="MM/dd/yyyy" date="${startDate}" />';
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
<content:sysdata var="dateFmt" name="time.date_format" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="insbusysave.do" method="post" linkID="0x${user.ID}" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title">
 <td width="60%" class="caps"><content:airline /> INSTRUCTOR BUSY TIME CALENDAR - WEEK OF <fmt:date fmt="d" date="${startDate}" d="MMMM dd, yyyy" /></td>
<c:if test="${isMine}">
 <td width="20%" class="mid"><el:cmd url="busycalendar" op="7" startDate="${startDate}">ALL INSTRUCTORS</el:cmd></td>
</c:if>
<c:if test="${!isMine && !empty user}">
 <td width="20%" class="mid"><el:cmd url="busycalendar" op="7" linkID="0x${user.ID}" startDate="${startDate}">MY BUSY TIME</el:cmd></td>
</c:if>
 <td class="right">CALENDAR TYPE <el:combo name="op" size="1" idx="*" options="${typeOptions}" value="7" onChange="void switchType(this)" /></td>
</tr>
</el:table>
<div class="mid">
<calendar:week date="cDate" startDate="${startDate}" entries="${busyTime}" topBarClass="dayHdr" 
	dayBarClass="dayHdr" tableClass="calendar" contentClass="contentW" scrollClass="scroll" cmd="busycalendar">
<calendar:entry name="busy">
<c:set var="ins" value="${pilots[busy.ID]}" scope="request" />
<c:set var="busyAccess" value="${accessMap[busy]}" scope="request" />
<span class="warn bld caps">${ins.name} IS BUSY</span><br />
<fmt:date fmt="t" t="HH:mm" date="${busy.startTime}" /> - <fmt:date fmt="t" t="HH:mm" date="${busy.endTime}" />
<c:if test="${busyAccess.canDelete}"><br />
<el:cmd url="insbusydelete" linkID="0x${ins.ID}" op="${fn:dateFmt(busy.startTime, 'MMddyyyyHHmm')}" className="pri small bld">DELETE</el:cmd></c:if>
<calendar:spacer><hr /></calendar:spacer>
</calendar:entry>
<calendar:empty>-</calendar:empty>
</calendar:week>
</div>
<c:if test="${access.canCreate || access.canProxyCreate}">
<el:table className="form" space="default" pad="default">
<tr class="title">
 <td colspan="2" class="caps">ADD NEW BUSY TIME</td>
</tr>
<c:if test="${access.canProxyCreate}">
<tr>
 <td class="label">Flight Instructor</td>
 <td class="data"><el:combo name="instructor" idx="*" size="1" className="req" firstEntry="-" options="${instructors}" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Start Date/Time</td>
 <td class="data"><el:text name="startDate" idx="*" size="10" max="10" value="" className="req" /> at
 <el:text name="startTime" idx="*" size="4" max="5" value="" className="req" />
&nbsp;<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].startDate')" />
&nbsp;<span class="small">All dates/times are ${pageContext.request.userPrincipal.TZ.name}. (Format: ${dateFmt} HH:mm)</span></td>
</tr>
<tr>
 <td class="label">End Date/Time</td>
 <td class="data"><el:text name="endDate" idx="*" size="10" max="10" value="" className="req" /> at
 <el:text name="endTime" idx="*" size="4" max="5" value="" className="req" />
&nbsp;<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].endDate')" />
&nbsp;<span class="small">All dates/times are ${pageContext.request.userPrincipal.TZ.name}. (Format: ${dateFmt} HH:mm)</span></td>
</tr>
<tr>
 <td class="label" valign="top">Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="80%" height="4"></el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SAVE NEW BUSY TIME" /></td>
</tr>
</el:table>
</c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
