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
<title><content:airline /> Flight Academy Instructor Busy Time Calendar</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="calendar" />
<content:pics />
<content:js name="common" />
<content:js name="datePicker" />
<script type="text/javascript">
golgotha.local.switchType = function(combo) {
	var cType = combo.options[combo.selectedIndex].value;
	self.location = '/busycalendar.do?op=' + cType + '&startDate=<fmt:date fmt="d" d="MM/dd/yyyy" date="${startDate}" />';
	return true;
};

golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
if (!f.comments) return false;
golgotha.form.validate({f:f.instructor, t:'Flight Instructor'});
golgotha.form.validate({f:f.startDate, t:'Busy Start Date'});
golgotha.form.validate({f:f.startTime, t:'Busy Start Time'});
golgotha.form.validate({f:f.endDate, t:'Busy End Date'});
golgotha.form.validate({f:f.endTime, t:'Busy End Time'});
golgotha.form.submit();
disableButton('SaveButton');
return true;
};
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
<el:form action="insbusysave.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title">
 <td style="width:60%" class="caps"><content:airline /> INSTRUCTOR BUSY TIME CALENDAR - <fmt:date fmt="d" date="${startDate}" d="MMMM yyyy" /></td>
<c:if test="${isMine}">
 <td style="width:20%" class="mid"><el:cmd url="busycalendar" op="31" startDate="${startDate}">ALL INSTRUCTORS</el:cmd></td>
</c:if>
<c:if test="${!isMine && !empty user}">
 <td style="width:20%" class="mid"><el:cmd url="busycalendar" op="31" link="${user}" startDate="${startDate}">MY BUSY TIME</el:cmd></td>
</c:if>
 <td class="right">CALENDAR TYPE <el:combo name="op" size="1" idx="*" options="${typeOptions}" value="30" onChange="void golgotha.local.switchType(this)" /></td>
</tr>
</el:table>
<div class="mid">
<calendar:month date="cDate" startDate="${startDate}" entries="${fn:keys(accessMap)}" topBarClass="dayHdr"
	dayBarClass="dayHdr" tableClass="calendar" contentClass="contentW" scrollClass="scroll" cmd="busycalendar">
<calendar:entry name="busy">
<c:set var="ins" value="${pilots[busy.ID]}" scope="page" />
<c:set var="busyAccess" value="${accessMap[busy]}" scope="page" />
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
