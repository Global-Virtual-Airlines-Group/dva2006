<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Simulator Version Statistics</title>
<content:css name="main" />
<content:css name="view" />
<content:css name="form" />
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
<content:enum var="sortTypes" className="org.deltava.beans.stats.FlightStatsSort" exclude="PIDS,ALEGS,OLEGS,OVLEGS,OILEGS,HLEGS,DSPLEGS,PAX,LF,SBLEGS,TLEGS" />
<content:enum var="groupTypes" className="org.deltava.beans.stats.FlightStatsGroup" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="simversionstats.do" method="post" validate="return true">
<view:table cmd="simversionstats">
<tr class="title">
 <td colspan="5" class="left caps"><content:airline /> FLIGHT SIMULATOR VERSION STATISTICS</td>
 <td colspan="7" class="right">GROUP BY <el:combo name="groupType" size="1" idx="*" options="${groupTypes}" value="${groupType}" onChange="void golgotha.local.updateSort()" />
 SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void golgotha.local.updateSort()" /></td>
</tr>
<!--  Chart Header Bar -->
<tr class="title caps">
 <td colspan="6" class="left">SIMULATOR VERSION CHART</td>
 <td colspan="5"><span class="toggle" onclick="golgotha.local.showChart(); golgotha.util.toggleExpand(this, 'chartRow');">EXPAND</span></td>
</tr>
<tr class="chartRow" style="display:none;">
 <td colspan="11"><div id="flightStats" style="height:345px;"></div></td>
</tr>
<tr class="chartRow" style="display:none;">
 <td colspan="11"><div id="flightStatsBar" style="height:345px;"></div></td>
</tr>
<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:5%;">#</td>
 <td style="width:18%;">ENTRY</td>
 <td style="width:8%;">HOURS</td>
 <td style="width:8%;">LEGS</td>
 <td class="nophone">DISTANCE</td>
<c:if test="${hasFSX}">
 <td>FSX</td>
</c:if>
<c:if test="${hasP3D}">
 <td>P3D<span class="nophone"> 64/32-BIT</span></td>
</c:if>
 <td>FS2004</td>
<c:if test="${hasMSFS}">
 <td>MSFS 24 / 20</td>
</c:if> 
 <td class="nophone">X-Plane 12 / 11 / 10</td>
<c:if test="${!hasP3D && !hasMSFS}">
 <td class="nophone">FS2002 / FS2000</td>
</c:if>
 <td class="nophone">OTHER</td>
</tr>

<!-- Table Statistics Data -->
<c:set var="entryNumber" value="${viewContext.start}" scope="page" />
<c:forEach var="stat" items="${viewContext.results}">
<view:row entry="${stat}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
<c:set var="eLegs" value="${stat.versionLegs}" scope ="page" />
<c:set var="has64" value="${eLegs['P3Dv4'] > 0}" scope="page" />
<c:set var="hasP3D3" value="${eLegs['P3D'] > 0}" scope="page" />
<c:set var="hasXP12" value="${eLegs['XP12'] > 0}" scope="page" />
<c:set var="hasXP11" value="${eLegs['XP11'] > 0}" scope="page" />
<c:set var="hasXP10" value="${eLegs['XP10'] > 0}" scope="page" />
<c:set var="hasFS2K" value="${eLegs['FS2000'] > 0}" scope="page" />
<c:set var="hasFS24" value="${eLegs['FS2024'] > 0}" scope="page" />
 <td class="sec bld small"><fmt:int value="${entryNumber}" /></td>
 <td class="pri bld">${stat.label}</td>
 <td class="bld"><fmt:dec value="${stat.hours}" /></td>
 <td class="pri bld"><fmt:int value="${stat.legs}" /></td>
 <td class="sec bld nophone"><fmt:distance value="${stat.distance}" /></td>
<c:if test="${hasFSX}"> 
 <td class="small"><fmt:int value="${eLegs['FSX']}" /> (<fmt:dec value="${eLegs['FSX'] * 100.0 / stat.legs}" />%)</td>
</c:if>
<c:if test="${hasP3D}">
 <td class="small"><c:if test="${has64}"><fmt:int value="${eLegs['P3Dv4']}" /></c:if><c:if test="${hasP3D3 && has64}">/</c:if><c:if test="${hasP3D3}"><fmt:int value="${eLegs['P3D']}" /></c:if> (<fmt:dec value="${(eLegs['P3D'] + eLegs['P3Dv4']) * 100.0 / stat.legs}" />%)</td>
</c:if>
 <td class="small"><fmt:int value="${eLegs['FS9']}" /> (<fmt:dec value="${eLegs['FS9'] * 100.0 / stat.legs}" />%)</td>
<c:if test="${hasMSFS}">
 <td class="small"><c:if test="${hasFS24}"><fmt:int value="${eLegs['FS2024']}" /> / </c:if><fmt:int value="${eLegs['FS2020']}" /> (<fmt:dec value="${(eLegs['FS2020'] + eLegs['FS2024']) * 100.0 / stat.legs}" />%)</td>
</c:if>
 <td class="small nophone"><c:if test="${hasXP12}"><fmt:int value="${eLegs['XP12']}" /></c:if><c:if test="${hasXP12 && hasXP11}">/</c:if><c:if test="${hasXP11}"><fmt:int value="${eLegs['XP11']}" className="ita" /></c:if><c:if test="${hasXP11 && hasXP10}">/</c:if>
<c:if test="${hasXP10}"><fmt:int value="${eLegs['XP10']}" /></c:if> (<fmt:dec value="${(eLegs['XP10'] + eLegs['XP11'] + eLegs['XP12']) * 100.0 / stat.legs}" />%)</td>
<c:if test="${!hasP3D && !hasMSFS}">
 <td class="small nophone"><fmt:int value="${eLegs['FS2002']}" />&nbsp;<c:if test="${hasFS2K}">/ <fmt:int value="${eLegs['FS2000']}" />&nbsp;</c:if>(<fmt:dec value="${(eLegs['FS2002'] + eLegs['FS2000']) * 100.0 / stat.legs}" />%)</td>
<c:set var="otherLegs" value="${eLegs['UNKNOWN']}" scope="page" />
</c:if>
<c:if test="${hasP3D || hasMSFS}">
<c:set var="otherLegs" value="${eLegs['FS2002'] + eLegs['FS2000'] + eLegs['UNKNOWN']}" scope="page" />
</c:if>
 <td class="small nophone"><fmt:int value="${otherLegs}" /> (<fmt:dec value="${otherLegs * 100.0 / stat.legs}" />%)</td>
</view:row>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="11"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
golgotha.local.updateSort = function() { return document.forms[0].submit(); };
google.charts.load('current',{'packages':['corechart']});
golgotha.local.showChart = function() {
	if (golgotha.local.chartData) return false;
	const p = fetch('simstats.ws', {signal:AbortSignal.timeout(3500)});
	p.then(function(rsp) {
		if (!rsp.ok) return false;
		p.json().then(function(js) {
			js.splice(0, 1); 
			golgotha.local.chartData = js.reverse();
			return golgotha.local.renderChart();
		});
	});
};

golgotha.local.renderChart = function() {
    const data = new google.visualization.DataTable();
    data.addColumn('string','Month');
    data.addColumn('number','MSFS 20/24');
    data.addColumn('number','X-Plane');
    data.addColumn('number','Prepar3D');
    data.addColumn('number','FSX');
    data.addColumn('number','FS2004');
    data.addColumn('number','FS2002');
    data.addColumn('number','Other');
    data.addRows(golgotha.local.chartData);

    // Draw the charts
    const hX = {gridlines:{color:'#cce'},minorGridlines:{count:12},title:'Month',textStyle:golgotha.charts.lgStyle};
    const c = new google.visualization.LineChart(document.getElementById('flightStats'));
    const cb = new google.visualization.ColumnChart(document.getElementById('flightStatsBar'));
    c.draw(data,golgotha.charts.buildOptions({title:'Flight Legs by Simulator',hAxis:hX}));
    cb.draw(data,golgotha.charts.buildOptions({title:'Percentage by Simulator',isStacked:'percent',hAxis:hX}));
    return true;
};
</script>
</body>
</html>
