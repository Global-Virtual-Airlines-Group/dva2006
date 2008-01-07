<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_calendar.tld" prefix="calendar" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
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
<el:form action="insbusysave.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title">
 <td width="60%" class="caps"><content:airline /> INSTRUCTOR BUSY TIME CALENDAR - <fmt:date fmt="d" date="${startDate}" d="MMMM yyyy" /></td>
<c:if test="${isMine}">
 <td width="20%" class="mid"><el:cmd url="busycalendar" op="31" startDate="${startDate}">ALL INSTRUCTORS</el:cmd></td>
</c:if>
<c:if test="${!isMine && !empty user}">
 <td width="20%" class="mid"><el:cmd url="busycalendar" op="31" link="${user}" startDate="${startDate}">MY BUSY TIME</el:cmd></td>
</c:if>
 <td class="right">CALENDAR TYPE <el:combo name="op" size="1" idx="*" options="${typeOptions}" value="30" onChange="void switchType(this)" /></td>
</tr>
</el:table>
<div class="mid">
<calendar:month date="cDate" startDate="${startDate}" entries="${fn:keys(accessMap)}" topBarClass="dayHdr"
	dayBarClass="dayHdr" tableClass="calendar" contentClass="contentW" scrollClass="scroll" cmd="busycalendar">
<calendar:entry name="busy">
<c:set var="ins" value="${pilots[busy.ID]}" scope="request" />
<c:set var="busyAccess" value="${accessMap[busy]}" scope="request" />
<span class="warn bld caps">${ins.name} IS BUSY</span><br />
<fmt:date fmt="t" t="HH:mm" date="${busy.startTime}" /> - <fmt:date fmt="t" t="HH:mm" date="${busy.endTime}" />
<c:if test="${busyAccess.canDelete}"><br />
<el:cmd url="insbusydelete" link="${ins}" op="${fn:dateFmt(busy.startTime, 'MMddyyyyHHmm')}" className="pri small bld">DELETE</el:cmd></c:if>
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
