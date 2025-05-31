<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="eliteName" name="econ.elite.name" />
<html lang="en">
<head>
<title><content:airline />&nbsp;${eliteName} Statistics</title>
<content:css name="main" />
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
<content:sysdata var="distUnit" name="econ.elite.distance" />
<content:sysdata var="pointUnit" name="econ.elite.points" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline />&nbsp;${eliteName} STATISTICS DASHBOARD</td>
</tr>
<tr>
 <td class="label top">${currentYear} Levels</td>
 <td class="data"><c:forEach var="lvl" items="${cyLevels}" varStatus="lvlStatus"><fmt:elite level="${lvl}" className="bld" />&nbsp;<fmt:int value="${lvl.legs}" /> legs, <fmt:int value="${lvl.distance}" />&nbsp;${distUnit}, <fmt:int value="${lvl.targetPercentile}" />%ile
 <c:if test="${!lvStatis.isLast()}"><br /></c:if> </c:forEach></td>
</tr>
<tr>
 <td class="label top"><c:if test="${estimatedLevels}">Estimated </c:if>${currentYear + 1} Levels</td>
 <td class="data"><c:forEach var="lvl" items="${nyLevels}" varStatus="lvlStatus"><fmt:elite level="${lvl}" className="bld" />&nbsp;<fmt:int value="${lvl.legs}" /> legs, <fmt:int value="${lvl.distance}" />&nbsp;${distUnit}, <fmt:int value="${lvl.targetPercentile}" />%ile
 <c:if test="${!lvStatis.isLast()}"><br /></c:if> </c:forEach>
 <c:if test="${estimatedLevels}"><br /><span class="ita small">Estimated requirements based on current year percentiles and flight activity since <fmt:date date="${estimateStart}" fmt="d" className="bld" tzName="UTC "/>
<c:if test="${isRollover}"> until <fmt:date date="${estimateEnd}" fmt="d" className="bld" tzName="UTC" /></c:if>.</span></c:if></td>
</tr>
<tr class="title caps">
 <td colspan="2">${eliteName}&nbsp;Flight Requirements by Year</td>
</tr>
<tr>
 <td colspan="2"><div id="lreqGraph" style="height:325px;"></div>
</tr>
<tr class="title caps">
 <td colspan="2">${eliteName}&nbsp;Distance Requirements by Year</td>
</tr>
<tr>
 <td colspan="2"><div id="dreqGraph" style="height:325px;"></div>
</tr>
<tr class="title caps">
 <td colspan="2">${eliteName}&nbsp;Pilots by Year</td>
</tr>
<tr>
 <td colspan="2"><div id="pilotGraph" style="height:500px;"></div>
</tr>
<tr class="title caps">
 <td colspan="2">${currentYear} PERCENTILES BY FLIGHT / ACCUMULATION<span id="pctToggle" class="toggle" onclick="void golgotha.util.toggleExpand(this, 'elitePct')">COLLAPSE</span></td>
</tr>
<c:forEach var="idx" items="${eppse.keys}">
<c:set var="lvl" value="${targetLvls[idx]}" scope="page" />
<tr class="elitePct">
 <td class="label"<c:if test="${!empty lvl}"> style="color:#ffffff; background-color:#${lvl.hexColor};"</c:if>><c:if test="${!empty lvl}">(${lvl.name}) </c:if><fmt:int value="${idx}" /></td>
 <td class="data">Flight: <fmt:int value="${flpse.getLegs(idx)}" className="pri bld" /> legs, <fmt:int value="${fdpse.getDistance(idx)}" className="bld" />&nbsp;${distUnit}, Elite: <fmt:int value="${elpse.getLegs(idx)}" className="pri bld" /> legs, 
 <fmt:int value="${edpse.getDistance(idx)}" className="bld" />&nbsp;${distUnit}, <fmt:int value="${eppse.getPoints(idx)}" />&nbsp;${pointUnit}</td>
</tr>
</c:forEach>
<!-- Bottom bar -->
<tr class="title"><td colspan="2">&nbsp;</td></tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<script async>
golgotha.local.showChart = function() {
	if (golgotha.local.chartData) return false;
	const p = fetch('elitestats.ws?year=${statsYear}', {signal:AbortSignal.timeout(7500)});
	p.then(function(rsp) {
		if (rsp.ok) return false;
		rsp.json().then(function(js) {
			golgotha.local.chartData = js;
			return golgotha.local.renderChart();
		});
	});
};

golgotha.local.renderChart = function() {
	const dt = golgotha.local.chartData;
	const lgStyle = {color:'black',fontName:'Verdana',fontSize:8};

    // Display the Pilot chart
    const pchart = new google.visualization.ColumnChart(document.getElementById('pilotGraph'));
    const pdata = new google.visualization.DataTable(); const barColors = [];
    pdata.addColumn('string','Year');
	dt.levels.forEach(function(lvl) { 
		pdata.addColumn('number', lvl.name);
		barColors.push('#' + lvl.color);
	});

	for (var x = 0; x < dt.years.length; x++) {
		const r = [dt.years[x].toString()];
		dt.levels.forEach(function(lvl) { r.push(dt.levelCounts[lvl.name][x]); });
		pdata.addRow(r);
	}
	
	// Build the requirement data tables
	const lrdata = new google.visualization.DataTable();
	const drdata = new google.visualization.DataTable();
	lrdata.addColumn('string','Year');
	drdata.addColumn('string','Year');
	dt.levels.slice(1).forEach(function(lvl) { 
		lrdata.addColumn('number',lvl.name);
		drdata.addColumn('number',lvl.name);
	});

	// Populate the data tables
	for (var x = 0; x < dt.years.length; x++) {
		const lr = [dt.years[x].toString()];
		const dr = [dt.years[x].toString()];
		for (var l = 1; l < dt.levels.length; l++) {
			const lvl = dt.levels[l];
			lr.push(dt.reqs[lvl.name].legs[x]);
			dr.push(dt.reqs[lvl.name].distance[x]);
		}
		
		lrdata.addRow(lr);
		drdata.addRow(dr);
	}
	
	// Display the leg/distance requirements charts
	const reqBarColors = barColors.slice(1); const hA = {title:'Year',textStyle:lgStyle};
	const lrchart = new google.visualization.LineChart(document.getElementById('lreqGraph'));
	const drchart = new google.visualization.LineChart(document.getElementById('dreqGraph'));
	lrchart.draw(lrdata,{hAxis:hA,legend:{textStyle:lgStyle},title:'Flight Leg Requirements by Year',colors:reqBarColors,vAxes:{0:{title:'Flight Legs',maxValue:600}}});
	drchart.draw(drdata,{hAxis:hA,legend:{textStyle:lgStyle},title:'Distance Requirements by Year',colors:reqBarColors,vAxes:{0:{title:'Flight Distance',maxValue:1000000}}});

	// Draw the pilot statistics chart
	pchart.draw(pdata,{hAxis:hA,legend:{textStyle:lgStyle},title:'Pilots by Year',isStacked:true,colors:barColors,vAxes:{0:{title:'Pilots'}}});
	return true;
};

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(golgotha.local.showChart);
</script>
</body>
</html>
