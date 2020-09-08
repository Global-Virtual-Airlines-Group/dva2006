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
<content:json />
<content:googleJS module="charts" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script async>
golgotha.local.charts = {hStyle:{gridlines:{color:'#cce'},minorGridlines:{count:12},title:'Month',format:'MMMM yyyy'}};
golgotha.local.updateSort = function() { return document.forms[0].submit(); };
golgotha.local.validate = function(f) {
    if (!golgotha.form.check()) return false;
    golgotha.form.submit(f);
    return true;
};

golgotha.local.drawGraphs = function(stData, clData, label) {
	var data = new google.visualization.DataTable();
	data.addColumn('date', 'Month');
	for (var st = 1; st <= golgotha.local.data.maxStage; st++)
		data.addColumn('number', 'Stage ' + st);

	data.addRows(stData);	
	var t = label + ' by Date/Stage'; const vs = {title:'Flight ' + label};
	golgotha.local.charts.stage.draw(data,{title:t,isStacked:true,fontSize:10,hAxis:golgotha.local.charts.hStyle,vAxis:vs,width:'100%'});

	var data = new google.visualization.DataTable();
	data.addColumn('date', 'Month');
	golgotha.local.data.sims.forEach(function(s) { data.addColumn('number', s); });
	data.addRows(clData);	
	var t = label +  ' by Date/Simulator';
	golgotha.local.charts.sim.draw(data,{title:t,isStacked:true,fontSize:10,hAxis:golgotha.local.charts.hStyle,vAxis:vs,width:'100%'});
	return true;
};

golgotha.local.swapTimeGraphs = function(rb) {
	const isLegs = (rb.value == 'LEGS');
	return golgotha.local.drawGraphs(isLegs ? golgotha.local.data.calendar : golgotha.local.data.calendarHours, isLegs ? golgotha.local.data.simCalendar : golgotha.local.data.simCalendarHours, isLegs ? 'Flights' : 'Hours');
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
<%@ include file="/jsp/stats/pirepStats.jspf" %>
</view:table>

<!-- Touchdown Speed statistics -->
<el:table className="form">
<tr class="title">
 <td colspan="8" class="left caps">TOUCHDOWN SPEED STATISTICS - <fmt:int value="${acarsLegs}" /> LANDINGS USING ACARS</td>
</tr>

<!-- Table Header Bar-->
<tr class="title mid caps">
 <td>#</td>
 <td style="width:15%">EQUIPMENT</td>
 <td style="width:10%">FLIGHTS</td>
 <td class="nophone" style="width:10%">HOURS</td>
 <td style="width:15%">AVERAGE SPEED</td>
 <td style="width:12%">STD. DEVIATION</td>
 <td style="width:15%">AVERAGE DISTANCE</td>
 <td>STD. DEVIATION</td>
</tr>

<!-- Touchdown Speed Analysis -->
<c:set var="entryNumber" value="0" scope="page" />
<c:forEach var="entry" items="${eqLandingStats}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
<tr class="mid">
 <td class="sec bld"><fmt:int value="${entryNumber}" /></td>
 <td class="pri bld">${entry.equipmentType}</td>
 <td><fmt:int value="${entry.legs}" /></td>
 <td class="nophone" ><fmt:dec value="${entry.hours}" /></td>
 <td class="pri bld"><fmt:dec value="${entry.averageSpeed}" fmt="#0.00" /> ft/min</td>
 <td class="sec"><fmt:dec value="${entry.stdDeviation}" fmt="#0.00" /> ft/min</td>
<c:choose>
<c:when test="${entry.distanceStdDeviation < 1}">
 <td colspan="2">N / A</td>
</c:when>
<c:otherwise>
 <td class="bld"><fmt:dec value="${entry.averageDistance}" fmt="#0" /> ft</td>
 <td><fmt:dec value="${entry.distanceStdDeviation}" fmt="#0.0" /> ft</td>
</c:otherwise>
</c:choose>
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

<!-- Button Bar -->
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
google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(function() {
const xmlreq = new XMLHttpRequest();
xmlreq.open('get', 'mystats.ws?id=${pilot.hexID}', true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	golgotha.local.data = JSON.parse(xmlreq.responseText);
	const lgStyle = {color:'black',fontName:'Verdana',fontSize:9};

	// Display the eqtypes chart
	let chart = new google.visualization.PieChart(document.getElementById('landingChart'));
	let data = new google.visualization.DataTable();
	data.addColumn('string','Equipment');
	data.addColumn('number','Flight Legs');
	data.addRows(golgotha.local.data.eqCount);
	chart.draw(data,{title:'Flights by Equipment Type',is3D:true,legend:'none',theme:'maximized'});

	// Display the vertical speed chart
	chart = new google.visualization.BarChart(document.getElementById('landingSpd'));
	data = new google.visualization.DataTable();
	data.addColumn('string','Landing Speed');
	data.addColumn('number','Damaging');
	data.addColumn('number','Firm');
	data.addColumn('number','Optimal');
	data.addColumn('number','Too Soft');
	data.addRows(golgotha.local.data.landingSpd);
	chart.draw(data,{title:'Touchdown Speeds',isStacked:true,colors:['red','orange','green','blue'],legend:'none',vAxis:lgStyle});

	// Display the vertical speed/runway distance chart
	chart = new google.visualization.ScatterChart(document.getElementById('landingSct'));
	data = new google.visualization.DataTable();
	data.addColumn('number','Touchown Distance');
	data.addColumn('number','Dangerous');
	data.addColumn('number','Acceptable');
	data.addColumn('number','Optimal');
	data.addColumn('number','Too Soft');
	data.addRows(golgotha.local.data.landingSct);
	const hX = {title:'Distance from Threshold (feet)',textStyle:lgStyle};
	const yX = {title:'Landing Speed (feet/min)',textStyle:lgStyle};
	chart.draw(data,{title:'Flight Quality vs. Landing Data',colors:['red','orange','green','blue'],legend:{textStyle:lgStyle},hAxis:hX,vAxis:yX});

	// Display quality breakdown chart
	chart = new google.visualization.PieChart(document.getElementById('qualBreakdown'));
	data = new google.visualization.DataTable();
	data.addColumn('string','Landing Quality');
	data.addColumn('number','Flight Legs');	
	data.addRows(golgotha.local.data.landingQuality);
	chart.draw(data,{title:'Landing Assessments',is3D:true,colors:['green','orange','red'],theme:'maximized'});

	// Massage data and init charts
	const dateTX = function(e) { var dt = e[0]; e[0] = new Date(dt.y, dt.m, dt.d, 12, 0, 0); };
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
