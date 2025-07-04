<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Raw Schedule</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:googleAnalytics />
<fmt:aptype var="useICAO" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:enum var="sources" className="org.deltava.beans.schedule.ScheduleSource" exclude="DRAFT" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="schedule.do" method="post" validate="return false">
<view:table cmd="rawbrowse">

<!-- Table Header Bars -->
<tr class="title caps">
 <td class="left" colspan="10"><span class="nophone"><content:airline />&nbsp;</span>RAW FLIGHT SCHEDULE</td>
</tr>
<tr class="title caps">
 <td colspan="3"><el:cmdbutton url="sched" op="edit" label="NEW RAW SCHEDULE ENTRY" /></td>
 <td class="right" colspan="7">SCHEDULE SOURCE <el:combo name="src" idx="*" size="1" required="true" firstEntry="[ SCHEDULE SOURCE ]" value="${src}" options="${sources}" onChange="void golgotha.local.update()" /></td>
</tr>
<tr class="title">
 <td class="left" colspan="3">OPERATED ON <el:text name="filterDate" size="8" max="10" value="${param.filterDate}" /></td>
 <td class="right" colspan="7">FROM <el:combo name="airportD" idx="*" size="1" className="small" firstEntry="-" options="${airportsD}" value="${airportD}" />
 <el:airportCode combo="airportD" airport="${airportD}" idx="*" /> TO <el:combo name="airportA" idx="*" size="1" className="small" firstEntry="-" options="${airportsA}" value="${airportA}" />
 <el:airportCode combo="airportA" airport="${airportA}" idx="*" />&nbsp;<el:button onClick="void golgotha.local.update()" label="GO" /></td>
</tr>
<tr class="title caps">
 <td class="nophone">LINE</td>
 <td style="width:10%">FLIGHT NUMBER</td>
 <td style="width:9%">EFFECTIVE</td>
 <td style="width:5%">DAYS</td>
 <td>EQUIPMENT</td>
 <td>AIRPORTS</td>
 <td class="nophone" style="width:6%">DEPARTS</td>
 <td class="nophone" style="width:6%">ARRIVES</td>
 <td class="nophone" style="width:7%">DISTANCE</td>
 <td style="width:6%">DURATION</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="entry" items="${viewContext.results}">
<view:row entry="${entry}">
 <td class="small nophone">${entry.lineNumber}</td>
 <td class="pri bld"><el:cmd url="sched" linkID="${entry.source}-${entry.lineNumber}">${entry.flightCode}</el:cmd></td>
 <td class="small"><fmt:date fmt="d" d="MM/dd/yy" date="${entry.startDate}" tzName="UTC" /> - <fmt:date fmt="d" d="MM/dd/yy" date="${entry.endDate}" tzName="UTC" /></td>
 <td class="small sec">${entry.dayCodes}</td>
 <td class="sec bld">${entry.equipmentType}</td>
 <td class="small">${entry.airportD.name} (<el:cmd url="airportinfo" linkID="${entry.airportD.IATA}" className="plain"><fmt:airport airport="${entry.airportD}" /></el:cmd>) to ${entry.airportA.name} (<el:cmd url="airportinfo" linkID="${entry.airportA.IATA}" className="plain"><fmt:airport airport="${entry.airportA}" /></el:cmd>)</td>
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tz="${entry.airportD.TZ}" date="${entry.timeD}" /></td>
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tz="${entry.airportA.TZ}" date="${entry.timeA}" /><c:if test="${entry.arrivalPlusDays > 0}"> +${entry.arrivalPlusDays}</c:if></td>
 <td class="sec nophone"><fmt:distance value="${entry.distance}" /></td>
 <td><fmt:duration duration="${entry.duration}" t="HH:mm" /></td> 
</view:row>
</c:forEach>

<!-- Scroll bar -->
<tr class="title">
 <td colspan="10"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>
 <view:legend width="150" labels="Regular Flight,Historic Flight" classes=" ,opt2" /></td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
<script async>
golgotha.local.createParams = function(o) {
	const params = []; 
	for (p in o) {
		if (o.hasOwnProperty(p))
			params.push(p + '=' + encodeURI(o[p]));
	}

	return params.join('&');
};

golgotha.local.update = function() {
	const f = document.forms[0];
	const p = {src:golgotha.form.getCombo(f.src),filterDate:f.filterDate.value};
	if (golgotha.form.comboSet(f.airportD))
		p.airportD = golgotha.form.getCombo(f.airportD);
	if (golgotha.form.comboSet(f.airportA))
		p.airportA = golgotha.form.getCombo(f.airportA);

	self.location = '/rawbrowse.do?' + golgotha.local.createParams(p);
	return true;
};

golgotha.onDOMReady(function() {
	const f = document.forms[0];
	golgotha.airportLoad.setHelpers([f.airportD,f.airportA]);
	return true;
});
</script>
</html>
