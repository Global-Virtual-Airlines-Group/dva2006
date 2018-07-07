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
<script>
golgotha.local.updateSort = function() { return document.forms[0].submit(); };
golgotha.local.validate = function(f) {
    if (!golgotha.form.check()) return false;
    golgotha.form.submit(f);
    return true;
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
<content:enum var="simNames" className="org.deltava.beans.Simulator" />

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
<fmt:jsarray var="golgotha.local.simulators" items="${simNames}" />
google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(function() {
var xmlreq = new XMLHttpRequest();
xmlreq.open('get', 'mystats.ws?id=${pilot.hexID}', true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var statsData = JSON.parse(xmlreq.responseText);
	var lgStyle = {color:'black',fontName:'Verdana',fontSize:9};

	// Display the eqtypes chart
	var chart = new google.visualization.PieChart(document.getElementById('landingChart'));
	var data = new google.visualization.DataTable();
	data.addColumn('string','Equipment');
	data.addColumn('number','Flight Legs');
	data.addRows(statsData.eqCount);
	chart.draw(data,{title:'Flights by Equipment Type',is3D:true,legend:'none',theme:'maximized'});

	// Display the vertical speed chart
	var chart = new google.visualization.BarChart(document.getElementById('landingSpd'));
	var data = new google.visualization.DataTable();
	data.addColumn('string','Landing Speed');
	data.addColumn('number','Damaging');
	data.addColumn('number','Firm');
	data.addColumn('number','Optimal');
	data.addColumn('number','Too Soft');
	data.addRows(statsData.landingSpd);
	chart.draw(data,{title:'Touchdown Speeds',isStacked:true,colors:['red','orange','green','blue'],legend:'none',vAxis:lgStyle});

	// Display the vertical speed/runway distance chart
	var chart = new google.visualization.ScatterChart(document.getElementById('landingSct'));
	var data = new google.visualization.DataTable();
	data.addColumn('number','Touchown Distance');
	data.addColumn('number','Dangerous');
	data.addColumn('number','Acceptable');
	data.addColumn('number','Optimal');
	data.addColumn('number','Too Soft');
	data.addRows(statsData.landingSct);
	var hX = {title:'Distance from Threshold (feet)',textStyle:lgStyle};
	var yX = {title:'Landing Speed (feet/min)',textStyle:lgStyle};
	chart.draw(data,{title:'Flight Quality vs. Landing Data',colors:['red','orange','green','blue'],legend:{textStyle:lgStyle},hAxis:hX,vAxis:yX});

	// Display quality breakdown chart
	var chart = new google.visualization.PieChart(document.getElementById('qualBreakdown'));
	var data = new google.visualization.DataTable();
	data.addColumn('string','Landing Quality');
	data.addColumn('number','Flight Legs');	
	data.addRows(statsData.landingQuality);
	chart.draw(data,{title:'Landing Assessments',is3D:true,colors:['green','orange','red'],theme:'maximized'});

	// Display stage by date chart
	var chart = new google.visualization.ColumnChart(document.getElementById('stageStats'));
	var data = new google.visualization.DataTable();
	data.addColumn('date', 'Month');
	for (var st = 1; st <= statsData.maxStage; st++)
		data.addColumn('number', 'Stage ' + st);

	statsData.calendar.forEach(function(e) { var dt = e[0]; e[0] = new Date(dt.y, dt.m, dt.d, 12, 0, 0); });
	data.addRows(statsData.calendar);
	var mnStyle = {gridlines:{color:'#cce'},minorGridlines:{count:12},title:'Month',format:'MMMM yyyy'};
	chart.draw(data,{title:'Flights by Date/Stage',isStacked:true,fontSize:10,hAxis:mnStyle,vAxis:{title:'Flight Legs'},width:'100%'});

	// Display sim by date chart
	var chart = new google.visualization.ColumnChart(document.getElementById('simStats'));
	var data = new google.visualization.DataTable();
	data.addColumn('date', 'Month');
	for (var st = 0; st <= statsData.maxSim; st++)
		data.addColumn('number', golgotha.local.simulators[st]);
	
	statsData.simCalendar.forEach(function(e) { var dt = e[0]; e[0] = new Date(dt.y, dt.m, dt.d, 12, 0, 0); });
	data.addRows(statsData.simCalendar);
	chart.draw(data,{title:'Flights by Date/Simulator',isStacked:true,fontSize:10,hAxis:mnStyle,vAxis:{title:'Flight Legs'},width:'100%'});
	return true;
};

xmlreq.send(null);
return true;
});
</script>
<content:googleAnalytics />
</body>
</html>
