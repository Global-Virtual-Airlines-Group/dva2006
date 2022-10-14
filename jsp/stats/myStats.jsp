<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Flight Statistics - ${pilot.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:googleJS module="charts" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script async>
golgotha.local.updateSort = function() { return document.forms[0].submit(); };
golgotha.local.validate = function(f) {
    if (!golgotha.form.check()) return false;
    golgotha.form.submit(f);
    return true;
};

golgotha.local.drawGraphs = function(stData, clData, label) {
	let data = new google.visualization.DataTable();
	data.addColumn('date', 'Month');
	for (var st = 1; st <= golgotha.local.data.maxStage; st++)
		data.addColumn('number', 'Stage ' + st);

	data.addRows(stData);	
	const o1 = golgotha.charts.buildOptions();
	o1.isStacked = true;
	o1.title = label + ' by Date/Stage';
	o1.vAxis.title= 'Flight ' + label;
	golgotha.local.charts.stage.draw(data,{title:t,backgroundColor:golgotha.local.bg,isStacked:true,fontSize:10,hAxis:golgotha.local.charts.hStyle,vAxis:vs,width:'100%',titleTextStyle:golgotha.local.ttStyle,legend:{textStyle:golgotha.local.lgStyle}});

	data = new google.visualization.DataTable();
	data.addColumn('date', 'Month');
	golgotha.local.data.sims.forEach(function(s) { data.addColumn('number', s); });
	data.addRows(clData);
	const t2 = label +  ' by Date/Simulator';
	golgotha.local.charts.sim.draw(data,{title:t,backgroundColor:golgotha.local.bg,isStacked:true,fontSize:10,hAxis:golgotha.local.charts.hStyle,vAxis:vs,width:'100%',titleTextStyle:golgotha.local.ttStyle,legend:{textStyle:golgotha.local.lgStyle}});
	return true;
};

golgotha.local.swapTimeGraphs = function(rb) {
	const isLegs = (rb.value == 'LEGS');
	return golgotha.local.drawGraphs(isLegs ? golgotha.local.data.calendar : golgotha.local.data.calendarHours, isLegs ? golgotha.local.data.simCalendar : golgotha.local.data.simCalendarHours, isLegs ? 'Legs' : 'Hours');
};
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
<%@ include file="/jsp/stats/pirepStats.jspf" %>
</view:table>

<!-- Touchdown Speed statistics -->
<el:table className="form">
<tr class="title">
 <td colspan="8" class="left caps">TOUCHDOWN SPEED STATISTICS - <fmt:int value="${acarsLegs}" /> LANDINGS USING ACARS</td>
</tr>
<tr class="title mid caps tdStats">
 <td>#</td>
 <td style="width:15%">EQUIPMENT</td>
 <td style="width:10%">FLIGHTS</td>
 <td class="nophone" style="width:10%">HOURS</td>
 <td style="width:18%">AVERAGE SPEED</td>
 <td style="width:12%" class="nophone">STD. DEVIATION</td>
 <td style="width:12%">AVERAGE DISTANCE</td>
 <td class="nophone">STD. DEVIATION</td>
</tr>

<!-- Touchdown Speed Analysis -->
<c:set var="entryNumber" value="0" scope="page" />
<c:forEach var="entry" items="${eqLandingStats}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
<tr class="mid tdStats">
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
</tr>
</c:forEach>

<!-- Best Landings -->
<tr class="title">
 <td colspan="8" class="left caps">BEST LANDINGS USING ACARS SINCE <fmt:date date="${bestLandingSince}" fmt="d" /></td>
</tr>
<tr class="title mid caps topLanding">
 <td>#</td>
 <td>FLIGHT</td>
 <td>DATE</td>
 <td>EQUIPMENT</td>
 <td><span class="nophone">RUNWAY / </span>DISTANCE</td>
 <td>SPEED</td>
<td class="nophone" colspan="2">FLIGHT ROUTE</td>
</tr>

<c:set var="entryNumber" value="0" scope="page" />
<c:forEach var="fr"  items="${bestLandings}">
<c:set var="rw" value="${rwyDistance[fr.ID]}" scope="page" />
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
<tr class="mid topLanding">
 <td class="sec bld"><fmt:int value="${entryNumber}" /></td>
 <td><el:cmd url="pirep" link="${fr}" className="bld plain">${fr.flightCode}</el:cmd></td>
 <td><fmt:date date="${fr.date}" fmt ="d" /></td>
 <td class="sec bld">${fr.equipmentType}</td>
 <td class="small sec bld"><span class="nophone">Runway ${rw.name}, </span><fmt:int value="${rw.distance}" /> feet</td>
 <td class="pri bld"><fmt:int value="${fr.landingVSpeed}" /> feet/min</td>
 <td colspan="2" class="small nophone">${fr.airportD.name} (<el:cmd url="airportinfo" linkID="${fr.airportD.IATA}"><fmt:airport airport="${fr.airportD}" /></el:cmd>) - ${fr.airportA.name} (<el:cmd url="airportinfo" linkID="${fr.airportA.IATA}"><fmt:airport airport="${fr.airportA}" /></el:cmd>)</td>
</tr>
</c:forEach>
</el:table>

<!-- Popular Routes -->
<el:table className="form">
<tr class="title">
 <td colspan="8" class="left caps">TOP <fmt:int value="${popularRoutes.size()}" /> FREQUENT FLIGHT ROUTES<span class="nophone"> (<fmt:dec value="${popularTotal * 100.0 / totalLegs}" />% OF TOTAL)</span></td>
</tr>
<tr class="title mid caps">
 <td>#</td>
 <td>FLIGHT ROUTE</td>
 <td style="width:12%">DISTANCE</td>
 <td style="width:15%">FLIGHTS</td>
 <td style="width:15%">ACARS</td>
</tr>

<c:set var="entryNumber" value="0" scope="page" />
<c:forEach var="entry" items="${popularRoutes}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
<content:defaultMethod var="dst" object="${entry}" method="distance" />
<tr class="mid">
 <td class="sec bld"><fmt:int value="${entryNumber}" /></td>
 <td class="small">${entry.airportD.name} (<el:cmd url="airportinfo" linkID="${entry.airportD.IATA}" className="plain"><fmt:airport airport="${entry.airportD}" /></el:cmd>) - ${entry.airportA.name}
 (<el:cmd url="airportinfo" linkID="${entry.airportA.IATA}" className="plain"><fmt:airport airport="${entry.airportA}" /></el:cmd>)</td>
 <td><fmt:distance value="${dst}" /></td>
 <td class="pri bld"><fmt:int value="${entry.flights}" /> (<fmt:dec value="${entry.flights * 100.0 / totalLegs}" />%)</td>
 <td class="bld"><fmt:int value="${entry.ACARSFlights}" /> (<fmt:dec value="${entry.ACARSFlights * 100.0 / entry.flights}" /> %)</td>
</tr>
</c:forEach>
</el:table>

<!-- Charts -->
<el:table className="form nophone">
<tr class="title">
 <td colspan="2" class="left">FLIGHT DATA VISUALIZATION</td>
</tr>
<tr>
 <td style="width:50%"><div id="qualBreakdown" style="width:100%; height:350px;"></div></td>
 <td style="width:50%"><div id="landingChart" style="width:100%; height:350px;"></div></td>
</tr>
<tr id="landingCharts" class="mid">
 <td><div id="landingSpd" style="width:100%; height:350px;"></div></td>
 <td><div id="landingSct" style="width:100%; height:350px;"></div></td>
</tr>
<tr class="title">
 <td class="left">FLIGHT DATA OVER TIME</td>
 <td class="right"><el:check type="radio" name="timeGraphOpts" options="${graphOpts}" value="LEGS" onChange="void golgotha.local.swapTimeGraphs(this)" /></td>
</tr>
<tr>
 <td colspan="2"><div id="stageStats" style="width:100%; height:340px;"></div></td>
</tr>
<tr>
 <td colspan="2"><div id="simStats" style="width:100%; height:340px;"></div></td>
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
	chart.draw(data,{title:'Flights by Equipment Type',backgroundColor:golgotha.local.bg,is3D:true,legend:'none',theme:'maximized'});

	// Display the vertical speed chart
	chart = new google.visualization.BarChart(document.getElementById('landingSpd'));
	data = new google.visualization.DataTable();
	data.addColumn('string','Landing Speed');
	data.addColumn('number','Damaging');
	data.addColumn('number','Firm');
	data.addColumn('number','Optimal');
	data.addColumn('number','Too Soft');
	data.addRows(golgotha.local.data.landingSpd);
	const aX = {textStyle:golgotha.charts.lgStyle,titleTextStyle:golgotha.charts.ttStyle};
	chart.draw(data,{title:'Touchdown Speeds',isStacked:true,backgroundColor:golgotha.charts.bg,colors:['red','orange','green','blue'],legend:'none',vAxis:aX,hAxis:aX,titleTextStyle:golgotha.charts.ttStyle});

	// Display the vertical speed/runway distance chart
	chart = new google.visualization.ScatterChart(document.getElementById('landingSct'));
	data = new google.visualization.DataTable();
	data.addColumn('number','Touchown Distance');
	data.addColumn('number','Dangerous');
	data.addColumn('number','Acceptable');
	data.addColumn('number','Optimal');
	data.addColumn('number','Too Soft');
	data.addRows(golgotha.local.data.landingSct);
	const hX = {title:'Distance from Threshold (feet)',textStyle:golgotha.local.lgStyle,titleTextStyle:golgotha.local.ttStyle};
	const yX = {title:'Landing Speed (feet/min)',textStyle:golgotha.local.lgStyle,titleTextStyle:golgotha.local.ttStyle};
	chart.draw(data,{title:'Flight Quality vs. Landing Data',backgroundColor:golgotha.local.bg,colors:['red','orange','green','blue'],legend:{textStyle:golgotha.local.lgStyle},hAxis:hX,vAxis:yX,titleTextStyle:golgotha.local.ttStyle});

	// Display quality breakdown chart
	chart = new google.visualization.PieChart(document.getElementById('qualBreakdown'));
	data = new google.visualization.DataTable();
	data.addColumn('string','Landing Quality');
	data.addColumn('number','Flight Legs');	
	data.addRows(golgotha.local.data.landingQuality);
	chart.draw(data,{title:'Landing Assessments',is3D:true,backgroundColor:golgotha.local.bg,colors:['green','orange','red'],theme:'maximized'});

	// Massage data and init charts
	const dateTX = function(e) { const dt = e[0]; e[0] = new Date(dt.y, dt.m, dt.d, 12, 0, 0); };
	golgotha.local.data.calendar.forEach(dateTX); golgotha.local.data.calendarHours.forEach(dateTX);
	golgotha.local.data.simCalendar.forEach(dateTX); golgotha.local.data.simCalendarHours.forEach(dateTX);
	golgotha.local.charts.stage = new google.visualization.ColumnChart(document.getElementById('stageStats'));
	golgotha.local.charts.sim = new google.visualization.ColumnChart(document.getElementById('simStats'));
	golgotha.local.drawGraphs(golgotha.local.data.calendar, golgotha.local.data.simCalendar, 'Flights');
	return true;
};

xmlreq.send(null);
return true;
});
</script>
<content:googleAnalytics />
</body>
</html>
