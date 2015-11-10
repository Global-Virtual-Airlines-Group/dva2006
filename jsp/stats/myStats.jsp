<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
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
<content:googleJS />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
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

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="mystats.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<!-- All Flight Report statistics -->
<view:table cmd="mystats">
<tr class="title">
 <td colspan="6" class="left caps"><span class="nophone"><content:airline /> </span>FLIGHT STATISTICS FOR ${pilot.name}</td>
 <td colspan="6" class="right">GROUP BY <el:combo name="groupType" size="1" idx="*" options="${groupTypes}" value="${param.groupType}" onChange="void golgotha.local.updateSort()" />
 SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void golgotha.local.updateSort()" /></td>
</tr>
<%@ include file="/jsp/stats/pirepStats.jspf" %>
</view:table>

<!-- Touchdown Speed statistics -->
<el:table className="form">
<tr class="title">
 <td colspan="8" class="left caps">TOUCHDOWN SPEED STATISTICS - <fmt:int value="${pilot.ACARSLegs}" /> LANDINGS USING ACARS</td>
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
<script type="text/javascript" defer>
google.load('visualization','1.0',{'packages':['corechart']});
google.setOnLoadCallback(function() {
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'mystats.ws?id=${pilot.hexID}', true);
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
	return true;
};

xmlreq.send(null);
return true;
});
</script>
<content:googleAnalytics />
</body>
</html>
