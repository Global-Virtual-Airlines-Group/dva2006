<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Client Build Statistics</title>
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

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarsbuilds.do" method="post" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline />&nbsp;</span>ACARS CLIENT BUILD STATSITICS</td>
</tr>
<tr>
 <td class="label">Number of Weeks</td>
 <td class="data"><el:combo name="count" value="${param.count}" firstEntry="[ WEEKS ]" options="${weeks}" /> <el:button label="UPDATE" onClick="void golgotha.local.showChart(document.forms[0].count)" /></td>
</tr>
<tr id="chartLegs" style="display:none;">
 <td colspan="2"><div id="clientLegs" style="height:350px;"></div></td>
</tr>
<tr id="chartHours" style="display:none;">
 <td colspan="2"><div id="clientHours" style="height:350px;"></div></td>
</tr>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="2">&nbsp;</td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script>
google.charts.load('current', {'packages':['corechart']});
golgotha.local.showChart = function(cb) {
	if (!golgotha.form.comboSet(cb)) return false;
	golgotha.form.submit(cb.form);
	const weeks = golgotha.form.getCombo(cb);
	if ((golgotha.local.chartData) && (golgotha.local.chartData.weeks >= weeks)) return golgotha.local.renderChart(weeks);

	var xmlreq = new XMLHttpRequest();
	xmlreq.open('get', 'acarsbuildstats.ws?count=' + weeks, true);
	xmlreq.onreadystatechange = function() {
		if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) {
			golgotha.form.clear(cb.form);
			return false;
		}

		const js = JSON.parse(xmlreq.responseText);
		js.stats.forEach(function(e) { const dt = e.week; e.week= new Date(dt.y, dt.m, dt.d, 12, 0, 0); });
		golgotha.local.chartData = js; //.reverse();
		return golgotha.local.renderChart(weeks);
	};

	xmlreq.send(null);
	return true;
};

golgotha.local.renderChart = function(cnt) {
	var cd = golgotha.local.chartData;
	const lgStyle = {color:'black',fontName:'Verdana',fontSize:8};

	// Display the chart
	const lC = new google.visualization.ColumnChart(document.getElementById('clientLegs'));
	const hC = new google.visualization.ColumnChart(document.getElementById('clientHours'));
	const lData = new google.visualization.DataTable(); const hData = new google.visualization.DataTable();
	lData.addColumn('date', 'Week'); hData.addColumn('date', 'Week');
	cd.builds.forEach(function(b) { lData.addColumn('number', 'Build ' + b); hData.addColumn('number', 'Build ' + b); });
	for (var x = 0; x < cnt; x++) {
		const st = cd.stats[x]; const l = []; const h = [];
		l.push(st.week); h.push(st.week);
		cd.builds.forEach(function(b) { l.push(st[b].legs); h.push(st[b].hours); });
		lData.addRow(l); hData.addRow(h);
	}

	golgotha.util.display('chartLegs', true); golgotha.util.display('chartHours', true);
	const mnStyle = {title:'Week',format:'MM/dd'};
	lC.draw(lData,{title:'Flights by Build/Week',isStacked:true,fontSize:10,hAxis:mnStyle,vAxis:{title:'Flight Legs'},width:'100%'});
	hC.draw(hData,{title:'Hours by Build/Week',isStacked:true,fontSize:10,hAxis:mnStyle,vAxis:{title:'Flight Hours'},width:'100%'});
	golgotha.form.clear(document.forms[0]);
	return true;
};
</script>
<content:googleAnalytics />
</body>
</html>
