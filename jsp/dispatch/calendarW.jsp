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
<title><content:airline /> ACARS Dispatcher Service Calendar</title>
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
self.location = '/dspcalendar.do?op=' + cType + '&startDate=<fmt:date fmt="d" d="MM/dd/yyyy" date="${startDate}" />';
return true;
}

function validate(form)
{
if (!checkSubmit()) return false;
if (!form.comments) return false;

if (!validateCombo(form.startDate, 'Service Start Date')) return false;
if (!validateCombo(form.startTime, 'Service Start Time')) return false;
if (!validateCombo(form.endDate, 'Service End Date')) return false;
if (!validateCombo(form.endTime, 'Service End Time')) return false;
setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="dspentry.do" op="save" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title">
 <td width="80%" class="caps"><content:airline /> ACARS DISPATCHER SERVICE CALENDAR - WEEK OF <fmt:date fmt="d" date="${startDate}" d="MMMM dd, yyyy" tzName="local" /></td>
 <td class="right">CALENDAR TYPE <el:combo name="op" size="1" idx="*" options="${typeOptions}" value="7" onChange="void switchType(this)" /></td>
</tr>
</el:table>
<div class="mid">
<calendar:week date="cDate" startDate="${startDate}" entries="${entries}" topBarClass="dayHdr" 
	dayBarClass="dayHdr" tableClass="calendar" contentClass="contentW" scrollClass="scroll" cmd="dspcalendar">
<calendar:entry name="entry">
<c:choose>
<c:when test="${fn:isAuthoredBean(entry)}">
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
<fmt:date fmt="t" t="HH:mm" date="${entry.startTime}" /> - <fmt:date fmt="t" t="HH:mm" date="${entry.endTime}" />
<c:if test="${entry.hasFlights}">
<br /><span class="bld"><fmt:int value="${fn:sizeof(entry.flights)}" /> Flights dispatched</span></c:if></div>
</c:otherwise>
</c:choose>
<calendar:spacer><hr /></calendar:spacer>
</calendar:entry>
<calendar:empty>-</calendar:empty>
</calendar:week>
</div>
<c:if test="${access.canCreate}">
<%@ include file="/jsp/dispatch/addServiceTime.jspf" %>
</c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
