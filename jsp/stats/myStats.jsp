<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Flight Statistics - ${pilot.name}</title>
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
<script async>
golgotha.local.updateSort = function() { return document.forms[0].submit(); };
golgotha.local.validate = function(f) {
    if (!golgotha.form.check()) return false;
    golgotha.form.submit(f);
    return true;
};

golgotha.local.drawGraphs = function(d) {
	let data = new google.visualization.DataTable();
	data.addColumn('date', 'Month');
	for (var st = 1; st <= golgotha.local.data.maxStage; st++)
		data.addColumn('number', 'Stage ' + st);

	data.addRows(d[0]);	
	const o1 = golgotha.charts.buildOptions({isStacked:true,width:'100%'});
	o1.title = d[3] + ' by Date/Stage';
	o1.vAxis.title= 'Flight ' + d[3];
	golgotha.local.charts.stage.draw(data,o1);

	data = new google.visualization.DataTable();
	data.addColumn('date', 'Month');
	golgotha.local.data.sims.forEach(function(s) { data.addColumn('number', s); });
	data.addRows(d[1]);
	const o2 = golgotha.charts.buildOptions({isStacked:true,width:'100%'});
	o2.title = d[3] +  ' by Date/Simulator';
	o2.vAxis.title= 'Flight ' + d[3];
	golgotha.local.charts.sim.draw(data,o2);
	
	data = new google.visualization.DataTable();
	data.addColumn('date', 'Month');
	golgotha.local.data.ratings.forEach(function(s) { data.addColumn('number', s); });
	data.addRows(d[2]);
	const o3 = golgotha.charts.buildOptions({isStacked:true,width:'100%',colors:['red','orange','green','blue','purple']});
	o3.title = d[3] +  ' by Date/Landing Rating';
	o3.vAxis.title= 'Flight ' + d[3];
	golgotha.local.charts.landRating.draw(data,o3);
	return true;
};

golgotha.local.swapTimeGraphs = function(rb) {
	const data = golgotha.local.dataMap[rb.value];
	return golgotha.local.drawGraphs(data);
};

golgotha.local.sortLandings = function(t) { return golgotha.sort.exec('topLanding', t); };
golgotha.local.sortPopRoute = function(t) { return golgotha.sort.exec('popRoute', t); };
golgotha.local.sortEQLanding = function(t) { return golgotha.sort.exec('eqLanding', t); };

<fmt:jsarray var="golgotha.sort.data.topLanding" items="${landingSortData}" />
<fmt:jsarray var="golgotha.sort.data.popRoute" items="${popRouteSortData}" />
<fmt:jsarray var="golgotha.sort.data.eqLanding" items="${eqLandingSortData}" />
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:enum var="sortTypes" className="org.deltava.beans.stats.FlightStatsSort" exclude="PIDS" />
<content:enum var="groupTypes" className="org.deltava.beans.stats.FlightStatsGroup" exclude="PILOT,AP" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="mystats.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<!-- All Flight Report statistics -->
<view:table cmd="mystats">
<tr class="title">
 <td colspan="6" class="left caps"><span class="nophone"><content:airline />&nbsp;</span>FLIGHT STATISTICS FOR ${pilot.name}<span class="nophone"> - <fmt:int value="${totalLegs}" /> FLIGHTS</span></td>
 <td colspan="6" class="right">GROUP BY <el:combo name="groupType" size="1" idx="*" options="${groupTypes}" value="${groupType}" onChange="void golgotha.local.updateSort()" />
 SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void golgotha.local.updateSort()" /></td>
</tr>
<c:set var="noFooter" value="true" scope="request" />
<c:set var="noTours" value="true" scope="request" />
<%@ include file="/jsp/stats/pirepStats.jspf" %>
</view:table>

<!-- Touchdown statistics -->
<el:table className="view">
<tr class="title">
 <td colspan="9" class="left caps">TOUCHDOWN STATISTICS - <fmt:int value="${acarsLegs}" /> LANDINGS USING ACARS<span id="tdStatsToggle" class="toggle" onclick="void golgotha.util.toggleExpand(this, 'tdStats')">COLLAPSE</span></td>
</tr>
<tr id="eqLandingLabel" class="title mid caps tdStats">
 <td>#</td>
 <td style="width:12%">EQUIPMENT</td>
 <td style="width:10%"><a href="javascript:void golgotha.local.sortEQLanding('legs')">FLIGHTS</a></td>
 <td class="nophone" style="width:10%"><a href="javascript:void golgotha.local.sortEQLanding('hours')">HOURS</a></td>
 <td style="width:18%"><a href="javascript:void golgotha.local.sortEQLanding('vSpeed')">AVERAGE SPEED</a></td>
 <td style="width:12%" class="nophone"><a href="javascript:void golgotha.local.sortEQLanding('vSpeedSD')">STD. DEVIATION</a></td>
 <td style="width:10%"><a href="javascript:void golgotha.local.sortEQLanding('distance')">AVERAGE DISTANCE</a></td>
 <td class="nophone"><a href="javascript:void golgotha.local.sortEQLanding('distanceSD')">STD. DEVIATION</a></td>
 <td><a href="javascript:void golgotha.local.sortEQLanding('score')">SCORE</a></td>
</tr>

<!-- Touchdown Speed Analysis -->
<c:set var="entryNumber" value="0" scope="page" />
<c:forEach var="entry" items="${eqLandingStats}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
<tr class="mid tdStats eqLandingData" id="eqLanding-${entry.equipmentType}">
 <td class="sec bld"><fmt:int value="${entryNumber}" /></td>
 <td class="pri bld">${entry.equipmentType}</td>
 <td><fmt:int value="${entry.legs}" /></td>
 <td class="nophone" ><fmt:dec value="${entry.hours}" /></td>
 <td class="pri bld"><fmt:dec value="${entry.averageSpeed}" fmt="#0.00" /> ft/min</td>
 <td class="sec nophone"><fmt:dec value="${entry.stdDeviation}" fmt="#0.00" /> ft/min</td>
<c:choose>
<c:when test="${entry.distanceStdDeviation < 1}">
 <td colspan="2">N / A</td>
</c:when>
<c:otherwise>
 <td class="bld"><fmt:dec value="${entry.averageDistance}" fmt="#0" /> ft</td>
 <td class="nophone"><fmt:dec value="${entry.distanceStdDeviation}" fmt="#0.0" /> ft</td>
</c:otherwise>
</c:choose>
 <td class="bld"> <fmt:landscore value="${entry.averageScore}" /></td>
</tr>
</c:forEach>

<!-- Best Landings -->
<tr class="title">
 <td colspan="9" class="left caps">BEST LANDINGS USING ACARS SINCE <fmt:date date="${bestLandingSince}" fmt="d" /><span id="topLandingToggle" class="toggle" onclick="void golgotha.util.toggleExpand(this, 'topLanding')">COLLAPSE</span></td>
</tr>
<tr id="topLandingLabel" class="title mid caps topLanding">
 <td>#</td>
 <td>FLIGHT</td>
 <td><a href="javascript:void golgotha.local.sortLandings('date')">DATE</a></td>
 <td>EQUIPMENT</td>
 <td><span class="nophone">RUNWAY / </span><a href="javascript:void golgotha.local.sortLandings('rwyDistance')">DISTANCE</a></td>
 <td><a href="javascript:void golgotha.local.sortLandings('vSpeed')">SPEED</a></td>
 <td><a href="javascript:void golgotha.local.sortLandings('score')">SCORE</a></td>
 <td class="nophone" colspan="2">FLIGHT ROUTE</td>
</tr>

<c:set var="entryNumber" value="0" scope="page" />
<c:forEach var="fr"  items="${bestLandings}">
<c:set var="rw" value="${rwyDistance[fr.ID]}" scope="page" />
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
<tr class="mid topLanding topLandingData" id="topLanding-${fr.ID}">
 <td class="sec bld"><fmt:int value="${entryNumber}" /></td>
 <td><el:cmd url="pirep" link="${fr}" className="bld plain">${fr.flightCode}</el:cmd></td>
 <td><fmt:date date="${fr.date}" fmt ="d" /></td>
 <td class="sec bld">${fr.equipmentType}</td>
 <td class="small sec bld"><span class="nophone">Runway ${rw.name}, </span><fmt:int value="${rw.distance}" /> feet</td>
 <td class="pri bld"><fmt:int value="${fr.landingVSpeed}" /> feet/min</td>
 <td class="bld"><fmt:landscore default="N/A" value="${fr.landingScore}" /></td>
 <td colspan="2" class="small nophone">${fr.airportD.name} (<el:cmd url="airportinfo" linkID="${fr.airportD.IATA}"><fmt:airport airport="${fr.airportD}" /></el:cmd>) - ${fr.airportA.name} (<el:cmd url="airportinfo" linkID="${fr.airportA.IATA}"><fmt:airport airport="${fr.airportA}" /></el:cmd>)</td>
</tr>
</c:forEach>
</el:table>

<!-- Popular Routes -->
<el:table className="view">
<tr class="title">
 <td colspan="6" class="left caps">TOP <fmt:int value="${popularRoutes.size()}" /> FREQUENT FLIGHT ROUTES<span class="nophone"> (<fmt:dec value="${popularTotal * 100.0 / totalLegs}" />% OF TOTAL)<span id="popRouteToggle" class="toggle" onclick="void golgotha.util.toggleExpand(this, 'popRoute')">COLLAPSE</span></span></td>
</tr>
<tr id="popRouteLabel" class="title mid caps popRoute">
 <td>#</td>
 <td>FLIGHT ROUTE</td>
 <td style="width:12%"><a href="javascript:void golgotha.local.sortPopRoute('distance')">DISTANCE</a></td>
 <td style="width:15%"><a href="javascript:void golgotha.local.sortPopRoute('legs')">FLIGHTS</a></td>
 <td style="width:15%"><a href="javascript:void golgotha.local.sortPopRoute('acars')">ACARS</a></td>
 <td style="width:10%" class="nophone"><a href="javascript:void golgotha.local.sortPopRoute('lastFlight')">LAST FLIGHT</a></td>
</tr>

<c:set var="entryNumber" value="0" scope="page" />
<c:forEach var="entry" items="${popularRoutes}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
<content:defaultMethod var="dst" object="${entry}" method="distance" />
<tr class="mid popRoute popRouteData" id="popRoute-${entry.createKey()}">
 <td class="sec bld"><fmt:int value="${entryNumber}" /></td>
 <td class="small">${entry.airportD.name} (<el:cmd url="airportinfo" linkID="${entry.airportD.IATA}" className="plain"><fmt:airport airport="${entry.airportD}" /></el:cmd>) - ${entry.airportA.name}
 (<el:cmd url="airportinfo" linkID="${entry.airportA.IATA}" className="plain"><fmt:airport airport="${entry.airportA}" /></el:cmd>)</td>
 <td><fmt:distance value="${dst}" /></td>
 <td class="pri bld"><fmt:int value="${entry.flights}" /> (<fmt:dec value="${entry.flights * 100.0 / totalLegs}" />%)</td>
 <td class="bld"><fmt:int value="${entry.ACARSFlights}" /> (<fmt:dec value="${entry.ACARSFlights * 100.0 / entry.flights}" />%)</td>
 <td class="nophone"><fmt:date date="${entry.lastFlight}"  fmt="d" /></td>
</tr>
</c:forEach>
</el:table>
<c:if test="${!empty airframes}">
<!-- Airframe Data -->
<el:table className="view">
<tr class="title">
 <td colspan="5" class="left caps">FREQUENTLY USED AIRCRAFT<span id="airFrameToggle" class="toggle nophone" onclick="void golgotha.util.toggleExpand(this, 'airframe')">COLLAPSE</span></td>
</tr>
<tr class="title caps mid airframe">
 <td>#</td>
 <td>REGISTRATION</td>
 <td>TYPE</td>
 <td>USED</td>
 <td class="nophone">LAST FLIGHT</td>
</tr>

<c:set var="entryNumber" value="0" scope="page" />
<c:forEach var="entry" items="${airframes}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
<tr class="mid airframe">
 <td class="sec bld"><fmt:int value="${entryNumber}" /></td>
 <td class="pri bld">${entry.tailCode}</td>
 <td class="bld">${entry.equipmentType}</td>
 <td><fmt:int value="${entry.useCount}" /></td>
 <td class="small pri nophone"><fmt:date date="${entry.lastUse}" fmt="d" /></td>
</tr>
</c:forEach>
</el:table>
</c:if>
<!-- Charts -->
<c:set var="uiScheme" value="${empty user.UIScheme ? 'legacy' : user.UIScheme.toLowerCase().replace(' ', '_')}" scope="page" />
<el:table className="form nophone">
<tr class="title">
 <td colspan="2" class="left">FLIGHT DATA VISUALIZATION</td>
</tr>
<tr class="mid">
 <td style="width:50%"><div id="qualBreakdown" style="width:100%; height:350px;"><el:img ID="qbSpinner" className="spinner" src="spinner_${uiScheme}.gif" caption="Loading" /></div></td>
 <td style="width:50%"><div id="landingChart" style="width:100%; height:350px;"><el:img ID="lcSpinner" className="spinner" src="spinner_${uiScheme}.gif" caption="Loading" /></div></td>
</tr>
<tr id="landingCharts" class="mid">
 <td><div id="landingSpd" style="width:100%; height:350px;"><el:img ID="lsSpinner" className="spinner" src="spinner_${uiScheme}.gif" caption="Loading" /></div></td>
 <td><div id="landingSct" style="width:100%; height:350px;"><el:img ID="ls2Spinner" className="spinner" src="spinner_${uiScheme}.gif" caption="Loading" /></div></td>
</tr>
<tr class="title">
 <td class="left">FLIGHT DATA OVER TIME</td>
 <td class="right"><el:check type="radio" name="timeGraphOpts" options="${graphOpts}" value="LEGS" onChange="void golgotha.local.swapTimeGraphs(this)" /></td>
</tr>
<tr>
 <td colspan="2"><div id="stageStats" style="width:100%; height:360px;"></div></td>
</tr>
<tr>
 <td colspan="2"><div id="simStats" style="width:100%; height:360px;"></div></td>
</tr>
<tr>
 <td colspan="2"><div id="landingRatingStats" style="width:100%; height:360px;"></div></td>
</tr>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="2">&nbsp;</td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
google.charts.load('current',{'packages':['corechart']});
google.charts.setOnLoadCallback(function() {
const xmlreq = new XMLHttpRequest();
xmlreq.timeout = 9500;
xmlreq.open('get', 'mystats.ws?id=${pilot.hexID}', true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	golgotha.local.data = JSON.parse(xmlreq.responseText);
	golgotha.local.charts = {hStyle:{gridlines:{color:'#cce'},minorGridlines:{count:12},title:'Month',format:'MMMM yyyy',textStyle:golgotha.charts.lgStyle,titleTextStyle:golgotha.charts.ttStyle}};

	// Display the eqtypes chart
	let chart = new google.visualization.PieChart(document.getElementById('landingChart'));
	let data = new google.visualization.DataTable();
	data.addColumn('string','Equipment');
	data.addColumn('number','Flight Legs');
	data.addRows(golgotha.local.data.eqCount);
	golgotha.util.display('qbSpinner', false);
	chart.draw(data,golgotha.charts.buildOptions({title:'Flights by Equipment Type',is3D:true,theme:'maximized',sliceVisibilityThreshold:0.005}));

	// Display the vertical speed chart
	chart = new google.visualization.BarChart(document.getElementById('landingSpd'));
	data = new google.visualization.DataTable();
	data.addColumn('string','Landing Speed');
	data.addColumn('number','Damaging');
	data.addColumn('number','Firm');
	data.addColumn('number','Optimal');
	data.addColumn('number','Too Soft');
	data.addRows(golgotha.local.data.landingSpd);
	golgotha.util.display('lcSpinner', false);
	const aX = {textStyle:golgotha.charts.lgStyle,titleTextStyle:golgotha.charts.ttStyle};
	chart.draw(data,golgotha.charts.buildOptions({title:'Touchdown Speeds',isStacked:true,colors:['red','orange','green','blue'],legend:'none'}));

	// Display the vertical speed/runway distance chart
	chart = new google.visualization.ScatterChart(document.getElementById('landingSct'));
	data = new google.visualization.DataTable();
	data.addColumn('number','Touchown Distance');
	data.addColumn('number','Dangerous');
	data.addColumn('number','Acceptable');
	data.addColumn('number','Optimal');
	data.addColumn('number','Too Soft');
	data.addRows(golgotha.local.data.landingSct);
	golgotha.util.display('lsSpinner', false);
	const hX = {title:'Distance from Threshold (feet)',textStyle:golgotha.charts.charts,titleTextStyle:golgotha.charts.ttStyle};
	const yX = {title:'Landing Speed (feet/min)',textStyle:golgotha.charts.lgStyle,titleTextStyle:golgotha.charts.ttStyle};
	chart.draw(data,golgotha.charts.buildOptions({title:'Flight Quality vs. Landing Data',colors:['red','orange','green','blue'],hAxis:hX,vAxis:yX}));

	// Display quality breakdown chart
	chart = new google.visualization.PieChart(document.getElementById('qualBreakdown'));
	data = new google.visualization.DataTable();
	data.addColumn('string','Landing Quality');
	data.addColumn('number','Flight Legs');	
	data.addRows(golgotha.local.data.landingQuality);
	golgotha.util.display('ls2Spinner', false);
	chart.draw(data,golgotha.charts.buildOptions({title:'Landing Assessments',is3D:true,colors:['red','orange','green','blue'],legend:{position:'none'},tooltip:{trigger:'selection',ignoreBounds:true}}));

	// Massage data and init charts
	golgotha.local.dataMap = {"LEGS":[golgotha.local.data.calendar,golgotha.local.data.simCalendar,golgotha.local.data.landingCalendar,"Legs"],"HOURS":[golgotha.local.data.calendarHours,golgotha.local.data.simCalendarHours,golgotha.local.data.landingCalendarHours,"Hours"],"DISTANCE":[golgotha.local.data.calendarDistance,golgotha.local.data.simCalendarDistance,golgotha.local.data.landingCalendarDistance,"Distance"]};
	golgotha.local.data.calendar.forEach(golgotha.charts.dateTX); golgotha.local.data.calendarHours.forEach(golgotha.charts.dateTX); golgotha.local.data.calendarDistance.forEach(golgotha.charts.dateTX);
	golgotha.local.data.simCalendar.forEach(golgotha.charts.dateTX); golgotha.local.data.simCalendarHours.forEach(golgotha.charts.dateTX); golgotha.local.data.simCalendarDistance.forEach(golgotha.charts.dateTX);
	golgotha.local.data.landingCalendar.forEach(golgotha.charts.dateTX); golgotha.local.data.landingCalendarHours.forEach(golgotha.charts.dateTX); golgotha.local.data.landingCalendarDistance.forEach(golgotha.charts.dateTX);
	golgotha.local.charts.stage = new google.visualization.ColumnChart(document.getElementById('stageStats'));
	golgotha.local.charts.sim = new google.visualization.ColumnChart(document.getElementById('simStats'));
	golgotha.local.charts.landRating = new google.visualization.ColumnChart(document.getElementById('landingRatingStats'));
	return golgotha.local.swapTimeGraphs(document.forms[0].timeGraphOpts);
};

xmlreq.send(null);
return true;
});
</script>
</body>
</html>
