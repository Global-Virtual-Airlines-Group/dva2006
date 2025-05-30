<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Online Flight Statistics</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:googleAnalytics />
<content:js name="common" />
<content:googleJS module="charts" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:enum var="sortTypes" className="org.deltava.beans.stats.FlightStatsSort" exclude="LEGS,MILES,HOURS,AVGHOURS,AVGMILES,PIDS,ALEGS,OLEGS,HLEGS,DSPLEGS,PAX,LF" />
<content:enum var="groupTypes" className="org.deltava.beans.stats.FlightStatsGroup" exclude="PILOT,EQ,AP,AD,AA" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="onlinestats.do" method="post" validate="return true">
<view:table cmd="onlinestats">
<tr class="title">
 <td colspan="4" class="left caps"><content:airline /> ONLINE FLIGHT STATISTICS</td>
 <td colspan="4" class="right">GROUP BY <el:combo name="groupType" size="1" idx="*" options="${groupTypes}" value="${groupType}" onChange="void golgotha.local.updateSort()" />
 SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void golgotha.local.updateSort()" /></td>
</tr>
<!--  Chart Header Bar -->
<tr class="title caps">
 <td colspan="6" class="left">NETWORK CHART</td>
 <td colspan="2"><span class="und" onclick="golgotha.util.toggleExpand(this, 'chartRow'); golgotha.local.showChart()">EXPAND</span></td>
</tr>
 <tr class="chartRow" style="display:none;">
 <td colspan="8"><div id="onlineStats" style="height:325px;"></div></td>
</tr>
 <!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:5%;">#</td>
 <td style="width:25%;">ENTRY</td>
 <td>TOTAL</td>
 <td style="width:7%">PERCENT</td>
 <td style="width:12%;">VATSIM</td>
 <td style="width:12%;">IVAO</td>
 <td style="width:12%;">PILOTEDGE</td>
 <td style="width:12%;">POSCON</td>
</tr>

<!-- Table Statistics Data -->
<content:enum var="networks" className="org.deltava.beans.OnlineNetwork" exclude="FPI,INTVAS,ACARS" />
<c:set var="entryNumber" value="${viewContext.start}" scope="page" />
<c:forEach var="stat" items="${viewContext.results}">
<c:set var="ol" value="${stat.onlineLegs}" scope="page" />
<view:row entry="${stat}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
 <td class="sec bld small"><fmt:int value="${entryNumber}" /></td>
 <td class="pri bld">${stat.label}</td>
 <td class="sec bld"><fmt:int value="${ol}" /> / <fmt:int value="${stat.totalLegs}" /></td>
 <td class="bld"><c:if test="${ol > 0}"><fmt:dec value="${(ol * 100.0) / stat.totalLegs}" />%</c:if><c:if test="${ol == 0}">-</c:if></td>
<c:forEach var="network" items="${networks}">
<c:set var="l" value="${stat.getLegs(network)}" scope="page" />
<c:set var="h" value="${stat.getHours(network)}" scope="page" />
 <td class="small"><fmt:int value="${l}" /><c:if test="${l > 0}" ><span class="sec nophone"> (<fmt:dec value="${h}"  /> hrs, <fmt:dec value="${(l * 100.0) / ol}" />%)</span></c:if></td>
</c:forEach>
</view:row>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="8"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
golgotha.local.updateSort = function() { return document.forms[0].submit(); };
google.charts.load('current', {'packages':['corechart']});
golgotha.local.showChart = function() {
	if (golgotha.local.chartData) return false;
	const p = fetch('onlinestats.ws', {signal:AbortSignal.timeout(3500)});
	p.then(function(rsp) {
		if (!rsp.ok) return false;
		rsp.json().then(function(js) {
			js.splice(0, 1); 
			golgotha.local.chartData = js.reverse();
			return golgotha.local.renderChart();
		});
	});
};

golgotha.local.renderChart = function() {
    const chart = new google.visualization.LineChart(document.getElementById('onlineStats'));
    const data = new google.visualization.DataTable();
    data.addColumn('string','Month');
    data.addColumn('number','Total');
    data.addColumn('number','VATSIM');
    data.addColumn('number','IVAO');
    data.addColumn('number','PilotEdge');
    data.addColumn('number','POSCON');
    data.addRows(golgotha.local.chartData);
    chart.draw(data,golgotha.charts.buildOptions());
    return true;
};
</script>
</body>
</html>
