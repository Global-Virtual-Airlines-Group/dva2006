<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Flight Schedule</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<fmt:aptype var="useICAO" />
<script type="text/javascript">
golgotha.local.setAirportD = function(combo) {
	self.location = '/browse.do?airportD=' + escape(golgotha.form.getCombo(combo));
	return true;
};

golgotha.local.setAirportA = function(combo)
{
var f = document.forms[0];
if (golgotha.form.comboSet(combo)) {
	self.location = '/browse.do?airportD=' + escape(golgotha.form.getCombo(f.airportD)) + '&airportA=' + escape(golgotha.form.getCombo(combo));
else
	self.location = '/browse.do?airportD=' + escape(golgotha.form.getCombo(f.airportD));

return true;
};

golgotha.onDOMReady(function() {
	var f = document.forms[0];
	golgotha.airportLoad.setHelpers(f.airportD);
	golgotha.airportLoad.setHelpers(f.airportA);
	return true;
});
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:attr attr="isSchedule" value="true" roles="Schedule" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="schedule.do" method="post" validate="return false">
<view:table cmd="browse">

<!-- Table Header Bars -->
<tr class="title">
 <td class="left caps" colspan="7"><content:airline /> FLIGHT SCHEDULE<c:if test="${!empty importDate}"> IMPORTED ON <fmt:date date="${importDate}" /></c:if></td>
</tr>
<tr class="title">
 <td class="right" colspan="7">FLIGHTS FROM <el:combo name="airportD" idx="*" size="1" className="small" options="${airportsD}" value="${airportD}" onChange="void golgotha.local.setAirportD(this)" />
 <el:text name="airportDCode" idx="*" size="3" max="4" value="${useICAO ? airportD.ICAO : airportD.IATA}" onBlur="void document.forms[0].airportD.setAirport(this.value, true)" /> TO
 <el:combo name="airportA" idx="*" size="1" className="small" firstEntry="-" options="${airportsA}" value="${airportA}" onChange="void golgotha.local.setAirportA(this)" />
 <el:text name="airportACode" idx="*" size="3" max="4" value="${useICAO ? airportA.ICAO : airportA.IATA}" onBlur="void document.forms[0].airportA.setAirport(this.value, true)" />
<c:if test="${isSchedule}"><el:cmdbutton url="sched" op="edit" label="NEW FLIGHT SCHEDULE ENTRY" /></c:if></td>
</tr>
<tr class="title caps">
 <td style="width:15%">FLIGHT NUMBER</td>
 <td>EQUIPMENT</td>
 <td style="width:35%">AIRPORTS</td>
 <td style="width:9%">DEPARTS</td>
 <td style="width:9%">ARRIVES</td>
 <td style="width:10%">DISTANCE</td>
 <td style="width:10%">DURATION</td>
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
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
