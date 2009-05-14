<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Schedule</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="innovataLink" name="schedule.innovata.enabled" />
<script language="JavaScript" type="text/javascript">
function setAirportD(combo)
{
var ad = combo.options[combo.selectedIndex].value;
self.location = '/browse.do?airportD=' + ad;
return true;
}

function setAirportDCode(code)
{
var f = document.forms[0];
if (setAirport(f.airportD, code))
	setAirportD(f.airportD);

return true;
}

function setAirportA(combo)
{
var f = document.forms[0];
var ad = f.airportD.options[f.airportD.selectedIndex].value;
if (combo.selectedIndex == 0)
	self.location = '/browse.do?airportD=' + ad;
else {
	var aa = combo.options[combo.selectedIndex].value;
	self.location = '/browse.do?airportD=' + ad + '&airportA=' + aa;
}

return true;
}

function initChoices()
{
var f = document.forms[0];
updateAirports(f.airportD, 'airline=all&useSched=true', ${useICAO}, '${airportD.IATA}');
updateAirports(f.airportA, 'code=${airportD.IATA}', ${useICAO}, '${airportA.IATA}');
}
</script>
</head>
<content:copyright visible="false" />
<body onload="initLinks(); initChoices()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:attr attr="isSchedule" value="true" roles="Schedule" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="schedule.do" method="post" validate="return false">
<view:table className="view" pad="default" space="default" cmd="browse">

<!-- Table Header Bars -->
<tr class="title">
 <td class="left caps" colspan="2"><content:airline /> SCHEDULE</td>
 <td class="right" colspan="5">FROM <el:combo name="airportD" idx="*" size="1" className="small" options="${emptyList}" onChange="void setAirportD(this)" />
 <el:text name="airportDCode" idx="*" size="3" max="4" value="${useICAO ? airportD.ICAO : airportD.IATA}" onBlur="void setAirportDCode(this.value)" /> TO
 <el:combo name="airportA" idx="*" size="1" className="small" firstEntry="-" options="${emptyList}" onChange="void setAirportA(this)" />
<c:if test="${isSchedule}"><el:cmdbutton url="sched" op="edit" label="NEW SCHEDULE ENTRY" /></c:if></td>
</tr>
<tr class="title caps">
 <td width="15%">FLIGHT NUMBER</td>
 <td width="10%">EQUIPMENT</td>
 <td width="35%">AIRPORTS</td>
 <td width="10%">DEPARTS</td>
 <td width="10%">ARRIVES</td>
 <td width="10%">DISTANCE</td>
 <td>DURATION</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="entry" items="${viewContext.results}">
<view:row entry="${entry}">
<c:if test="${!isSchedule}"> <td class="pri bld">${entry.flightCode}</td></c:if>
<c:if test="${isSchedule}"> <td><el:cmd className="bld" url="sched" op="edit" linkID="${entry.flightCode}">${entry.flightCode}</el:cmd></td></c:if>
 <td class="sec bld">${entry.equipmentType}</td>
 <td class="small">${entry.airportD.name} (<fmt:airport airport="${entry.airportD}" />) to
 ${entry.airportA.name} (<fmt:airport airport="${entry.airportA}" />)</td>
 <td><fmt:date fmt="t" t="HH:mm" tz="${entry.airportD.TZ}" date="${entry.dateTimeD.UTC}" /></td>
 <td><fmt:date fmt="t" t="HH:mm" tz="${entry.airportA.TZ}" date="${entry.dateTimeA.UTC}" /></td>
 <td class="sec"><fmt:distance value="${entry.distance}" /></td>
 <td><fmt:dec value="${entry.length / 10}" /> hours</td>
</view:row>
</c:forEach>

<!-- Scroll bar -->
<tr class="title">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>
 <view:legend width="150" labels="Regular Flight,Historic Flight" classes=" ,opt2" /></td>
</tr>
</view:table>
<c:if test="${innovataLink}">
<%@ include file="/jsp/schedule/innovataLink.jspf" %></c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
