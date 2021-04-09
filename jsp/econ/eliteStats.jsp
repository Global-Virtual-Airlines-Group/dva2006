<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
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
<content:js name="common" />
<content:json />
<content:googleJS module="charts" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="eliteDistance" name="econ.elite.distance" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline />&nbsp;${eliteName}&nbsp;Requirements by Year</td>
</tr>
<tr>
 <td colspan="2"><div id="reqGraph" style="height:425px;"></div>
</tr>
<tr class="title caps">
 <td colspan="2"><content:airline />&nbsp;${eliteName}&nbsp;Pilots by Year</td>
</tr>
<tr>
 <td colspan="2"><div id="pilotGraph" style="height:425px;"></div>
</tr>
<tr class="title caps">
 <td colspan="2"><content:airline />&nbsp;${eliteName}&nbsp;Current Statistics</td>
</tr>
<tr>
 <td colspan="2"><div id="statGraph" style="height:425px;"></div>
</tr>
<!-- Bottom Bar -->
<tr class="title"><td class="eliteStatus" colspan="2">&nbsp;</td></tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
<script>
golgotha.local.showChart = function() {
	if (golgotha.local.chartData) return false;

	const xmlreq = new XMLHttpRequest();
	xmlreq.open('get', 'elitestats.ws', true);
	xmlreq.onreadystatechange = function() {
		if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
		const js = JSON.parse(xmlreq.responseText);
		golgotha.local.chartData = js;
		return golgotha.local.renderChart();
	};

	xmlreq.send(null);
	return true;
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
		dt.levels.forEach(function(lvl) { r.push(lvl.data[x]); });
		pdata.addRow(r);
	}

	// Display the requirements chart
	const rchart = new google.visualization.LineChart(document.getElementById('reqGraph'));
	const rdata = new google.visualization.DataTable(); let reqVAxes = {};
	rdata.addColumn('string','Year');
	for (var l = 0; l < dt.reqs.length; l++) {
		const lvl = dt.reqs[l];
		rdata.addColumn('number',lvl.name + ' Legs');
		rdata.addColumn('number',lvl.name + ' Distance');
		reqVAxes[l * 2] = {targetAxisIndex:0};
		reqVAxes[(l * 2) + 1] = {targetAxisIndex:1};
	}
	
	for (var x = 0; x < dt.years.length; x++) {
		const r = [dt.years[x].toString()];
		for (var l = 0; l < dt.reqs.length; l++) {
			r.push(dt.reqs[l].legs[x]);
			r.push(dt.reqs[l].distance[x]);
		}

		rdata.addRow(r);
	}

	// Display the statistics chart
	const schart = new google.visualization.LineChart(document.getElementById('statGraph'));
	const sdata = new google.visualization.DataTable();
	sdata.addColumn('string','Level');

	// Draw the charts
	pchart.draw(pdata,{hAxis:{textStyle:lgStyle},legend:{textStyle:lgStyle},title:'Pilots by Year',isStacked:true,colors:barColors});
	rchart.draw(rdata,{hAxis:{textStyle:lgStyle},legend:{position:'none'},title:'Requirements by Year',colors:barColors,series:reqVAxes,vAxes:{0:{title:'Flight Legs',maxValue:600},1:{title:'Distance',maxValue:600000}}});
	return true;
};

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(golgotha.local.showChart);
</script>
</body>
</html>
