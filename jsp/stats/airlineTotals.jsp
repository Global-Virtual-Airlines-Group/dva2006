<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Airline Totals</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:json />
<content:googleJS module="charts" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">AIRLINE STATISTICS AS OF <fmt:date date="${effectiveDate}" d="EEEE MMMM dd yyyy" /></td>
</tr>
<tr>
 <td class="label">Total Flights</td>
 <td class="data"><fmt:int className="pri bld" value="${totals.totalLegs}" /> flights / 
 <fmt:int className="pri bld" value="${totals.totalHours}" /> hours / 
 <fmt:distance className="pri bld" value="${totals.totalMiles}" longUnits="true" /></td>
</tr>
<tr>
 <td class="label">Total Pilots</td>
 <td class="data"><fmt:int value="${totals.totalPilots}" /> total,
 <fmt:int className="pri bld" value="${totals.activePilots}" /> active</td>
</tr>
<tr>
 <td class="label">Online Flights</td>
 <td class="data"><fmt:int className="sec bld" value="${totals.onlineLegs}" /> flights / 
 <fmt:int className="bld" value="${totals.onlineHours}" /> hours / 
 <fmt:distance className="bld" value="${totals.onlineMiles}" longUnits="true" /></td>
</tr>
<tr>
 <td class="label">ACARS Flights</td>
 <td class="data"><fmt:int className="pri bld" value="${totals.ACARSLegs}" /> flights / 
 <fmt:int className="pri bld" value="${totals.ACARSHours}" /> hours /
 <fmt:distance className="pri bld" value="${totals.ACARSMiles}" longUnits="true" /></td>
</tr>
<tr>
 <td class="label">per Pilot Average</td>
 <td class="data"><fmt:dec className="bld" value="${totals.totalLegs / totals.totalPilots}" /> flights / 
 <fmt:dec className="bld" value="${totals.totalHours / totals.totalPilots}" /> hours / 
 <fmt:distance className="bld" value="${totals.totalMiles / totals.totalPilots}" longUnits="true" /></td>
</tr>
<tr>
 <td class="label">per Day Average</td>
 <td class="data"><fmt:dec className="bld" value="${totals.totalLegs / totals.age}" /> flights / 
 <fmt:dec className="bld" value="${totals.totalHours / totals.age}" /> hours / 
 <fmt:distance className="bld" value="${totals.totalMiles / totals.age}" longUnits="true" /></td>
</tr>
<tr>
 <td class="label">Current Month</td>
 <td class="data"><fmt:int value="${totals.MTDLegs}" /> flights / <fmt:int value="${totals.MTDHours}" />
  hours / <fmt:distance value="${totals.MTDMiles}" longUnits="true" /></td>
</tr>
<tr>
 <td class="label">Current Year</td>
 <td class="data"><fmt:int value="${totals.YTDLegs}" /> flights / <fmt:int value="${totals.YTDHours}" />
  hours / <fmt:distance value="${totals.YTDMiles}" longUnits="true" /></td>
</tr>
<tr>
 <td class="label">Database Size</td>
 <td class="data"><fmt:int className="sec bld" value="${totals.DBRows}" /> rows / 
 <fmt:int className="sec bld" value="${totals.DBSize}" /> bytes</td>
</tr>
<tr>
 <td class="label top">Flight Totals Graph</td>
 <td class="data"><div id="flightStats" style="height:325px;"></div></td>
</tr>
<tr class="title caps mid">
 <td colspan="2"><content:airline /> STATISTICS COMMENCE <fmt:int value="${totals.age}" /> DAYS AGO</td>
</tr>
</el:table>
<br />
<!-- Database Information Table -->
<el:table className="view">
<tr class="title caps">
 <td style="width:35%">TABLE NAME</td>
 <td style="width:15%">ROWS</td>
 <td style="width:15%">TABLE SIZE</td>
 <td class="nophone" style="width:15%">INDEX SIZE</td>
 <td class="nophone">AVG. ROW SIZE</td>
</tr>

<!-- Database Information Data -->
<c:forEach var="tableInfo" items="${tableStatus}">
<tr>
 <td class="pri bld caps">${tableInfo.name}</td>
 <td class="sec bld"><fmt:int value="${tableInfo.rows}" /></td>
 <td class="bld"><fmt:int value="${tableInfo.size}" /> bytes</td>
 <td class="sec bld nophone"><fmt:int value="${tableInfo.indexSize}" /> bytes</td>
 <td class="nophone"><fmt:int value="${tableInfo.averageRowLength}" /> bytes/row</td>
</tr>
</c:forEach>

<!-- Footer Bar -->
<tr class="title">
 <td colspan="5">&nbsp;</td>
</tr>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(function() {
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'allstats.ws', true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var statsData = JSON.parse(xmlreq.responseText);
	var lgStyle = {color:'black',fontName:'Verdana',fontSize:8};

	// Display the chart
	var chart = new google.visualization.LineChart(document.getElementById('flightStats'));
	var data = new google.visualization.DataTable();
	data.addColumn('string','Date');
	data.addColumn('number','Total Flights');
	data.addColumn('number','Online Flights');
	data.addColumn('number','ACARS Flights');
	data.addColumn('number','Historic Flights');
	data.addRows(statsData);
	chart.draw(data,{hAxis:{textStyle:lgStyle},legend:{textStyle:lgStyle}});
	return true;
};

xmlreq.send(null);
return true;	
});
</script>
<content:googleAnalytics />
</body>
</html>
