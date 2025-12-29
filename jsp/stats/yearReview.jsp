<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<html lang="en">
<head>
<title>${year} Year in Review - ${pilot.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:googleAnalytics />
<content:js name="common" />
<map:api version="3" />
<content:googleJS module="charts" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:cspHeader />
<script>
golgotha.local.updateYear = function() { document.forms[0].submit(); };
</script>
<style>
table.form td.eliteStatus, .button {
	color: #ffffff;
	background-color: #${eliteStatus.level.hexColor};
}
</style>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="eliteEnabled" name="econ.elite.enabled" />
<content:sysdata var="eliteName" name="econ.elite.name" />
<content:sysdata var="eliteDistance" name="econ.elite.distance" />
<content:sysdata var="elitePoint" name="econ.elite.points" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="yearreview.do" method="post" validate="return true">
<el:table className="form">

<!-- Table Header Bar-->
<tr class="title caps">
 <td colspan="2" class="left">${year} YEAR IN REVIEW - ${pilot.name} (${pilot.pilotCode})</td>
 <td style="width:150px" class="right">YEAR <el:combo name="year" size="1" idx="*" required="true" firstEntry="[ SELECT ]" value="${year}" options="${years}" onChange="void golgotha.local.updateYear()" /></td>
</tr>
<tr>
 <td class="label">${year} Flights</td>
 <td colspan="2" class="data bld"><span class="pri"><fmt:int value="${cyFlights.size()}" /> Legs</span><c:if test="${!empty lyFlights}"> (<fmt:dec value="${(cyFlights.size() - lyFlights.size()) * 1.0 / lyFlights.size()}" fmt="#0.0%" /> from ${year - 1})</c:if>,
 <span class="sec"><fmt:distance value="${cyDistance}" /></span><c:if test="${!empty lyFlights}"> (<fmt:dec value="${(cyDistance - lyDistance) * 1.0 / lyDistance}" fmt="#0.0%" forceSign="true" /> from ${year - 1})</c:if></td>
</tr>
<tr>
 <td class="label top">Airports Visited</td>
 <td colspan="2" class="data"><fmt:int value="${cyAirports.size()}" className="bld" /> Airports<c:if test="${!empty newAP}">, <span class="sec bld"><fmt:int value="${newAP.size()}"/> New</span></c:if>
<c:if test="${!empty lyAirports}"> (<fmt:dec value="${(lyAirports.size() - cyAirports.size()) * 1.0 / lyAirports.size()}" fmt="#0.0%" className="bld" forceSign="true" /> from ${year - 1})</c:if><br>
<br>
<c:forEach var="ap" items="${cyAirports}" varStatus="apStatus">
<c:set var="isNew" value="${newAP.contains(ap)}" scope="page" />
<span class="${isNew ? 'ter bld' : 'plain'}">${ap.name}</span> (<el:cmd url="airportinfo" target="new" linkID="${ap.IATA}"><fmt:airport airport="${ap}" /></el:cmd>)<c:if test="${!apStatus.last}"><br></c:if></c:forEach></td>
</tr>
<tr>
 <td class="label top">Aircraft Used</td>
 <td colspan="2" class="data"><span class="bld"><fmt:int value="${cyEquipment.size()}"/> Aircraft</span><c:if test="${!empty newEQ}">, <span class="sec bld"><fmt:int value="${newEQ.size()}" /> New</span></c:if>
<c:if test="${!empty lyEquipment}"> (<fmt:dec value="${(lyEquipment.size() - cyEquipment.size()) * 1.0 / lyEquipment.size()}" fmt="#0.0%" className="bld" forceSign="true" /> from ${year - 1})</c:if><br>
<br>
<c:forEach var="eqType" items="${cyEquipment}" varStatus="eqStatus">
<c:set var="isNew" value="${newEQ.contains(eqType)}" scope="page" />
<span class="${isNew ? 'ter bld' : 'plain'}">${eqType}</span><c:if test="${!eqStatus.last}">, </c:if></c:forEach></td>
</tr>
<c:if test="${eliteEnabled}">
<tr class="title caps">
 <td colspan="3" class="eliteStatus"><span class="nophone"><content:airline /> </span> ${eliteName} PROGRAM<span id="eliteToggle" class="toggle nophone" onclick="void golgotha.util.toggleExpand(this, 'elite')">COLLAPSE</span></td>
</tr>
<tr class="elite">
 <td class="label eliteStatus">${year} Status</td> 
 <td colspan="2" class="data bld"><fmt:elite level="${eliteStatus.level}" nameOnly="true" /></td>
</tr>
<tr class="elite">
 <td class="label eliteStatus">${year} Totals</td>
 <td colspan="2" class="data"><fmt:int value="${eliteTotals.legs}" className="pri bld" /> flight legs, <span class="sec bld"><fmt:int value="${eliteTotals.distance}" />&nbsp;${eliteDistance}</span>, <span class="bld"><fmt:int value="${eliteTotals.points}" />&nbsp;${elitePoints}</span></td>
</tr>
<tr class="elite">
 <td class="label eliteStatus">${year} Results</td>
 <td colspan="2" class="data"><c:forEach var="upd" items="${eliteLog}" varStatus="updStatus">
<fmt:date date="${upd.effectiveOn}" fmt="d"  className="bld" />&nbsp;
<c:choose>
<c:when test="${upd.isLifetime}">Achieved <fmt:ltelite className="bld" level="${upd.lifetimeStatus}" /> for lifetime <fmt:elite className="bld" level="${upd.level}" nameOnly="true" /> status (Qualified via ${upd.upgradeReason.description})</c:when>
<c:when test="${upd.upgradeReason == 'ROLLOVER'}">Rolled over <fmt:elite className="bld" level="${upd.level}" nameOnly="true" /> achieved in ${upd.level.year - 1} for ${upd.level.year}</c:when>
<c:when test="${upd.upgradeReason == 'DOWNGRADE'}">Downgraded to <fmt:elite className="bld" level="${upd.level}" nameOnly="true" /> based on ${upd.level.year -1} mileage achievement</c:when>
<c:when test="${upd.upgradeReason == 'NONE'}">Initial ${eliteName} credit</c:when>
<c:otherwise>Earned <fmt:elite className="bld" level="${upd.level}" nameOnly="true" /> for ${upd.level.year} (Qualified via ${upd.upgradeReason.description})</c:otherwise>
</c:choose>
<c:if test="${!updStatus.isLast()}"><br></c:if></c:forEach></td>
</tr>
</c:if>
<tr class="title caps">
 <td colspan="3">FLIGHT EVALUATIONS<span id="evalToggle" class="toggle nophone" onclick="void golgotha.util.toggleExpand(this, 'eval')">COLLAPSE</span></td>
</tr>
<tr class="eval">
 <td colspan="3"><table style="width:100%"><tr><td style="width:48%"><div id="lsChart" style="height:360px"></div></td><td style="width:48%"><div id="fsChart" style="height:360px"></div></td></tr></table></td>
</tr>
<tr class="title caps">
 <td colspan="3" class="left">${year} FLIGHT MAP<span id="mapToggle" class="toggle nophone" onclick="void golgotha.util.toggleExpand(this, 'map')">COLLAPSE</span></td>
</tr>
<tr class="map">
 <td colspan="3"><map:div ID="mapBox" height="550" /></td>
</tr>
</el:table>
<el:text name="id" type="hidden" value="${pilot.ID}" />
</el:form>
<br>
<content:copyright />
</content:region>
</content:page>
<script>
<map:token />
<map:markers var="golgotha.local.airports" items="${cyAirports}" />
<map:bounds var="golgotha.local.bb" items="${cyAirports}" />

// Build the map
const mapOpts = {container:'mapBox', bounds:golgotha.local.bb, minZoom:2, maxZoom:15, projection:'globe', fitBoundsOptions:{padding:48}, style:'mapbox://styles/mapbox/outdoors-v12'};
const map = new golgotha.maps.Map(document.getElementById('mapBox'), mapOpts);
map.addControl(new mapboxgl.FullscreenControl(), 'top-right');
map.addControl(new mapboxgl.NavigationControl(), 'top-right');
map.on('style.load', golgotha.maps.updateMapText);
map.once('load', function() {
	map.addControl(new golgotha.maps.BaseMapControl(golgotha.maps.DEFAULT_TYPES), 'top-left');
	map.addMarkers(golgotha.local.airports);
	google.charts.load('current',{'packages':['corechart']});
	google.charts.setOnLoadCallback(golgotha.local.loadTracks(${pilot.ID}, ${year}));
});

golgotha.local.loadTracks = function(id, yr) {
	const p = fetch('yeartracks.ws?id=' + id + '&year=' + yr);
	p.then(function(rsp) {
		if (!rsp.ok) return false;
		rsp.json().then(function(js) {
			js.tracks.forEach(function(trk) {
				const l = new golgotha.maps.Line('trk-' + trk.id, {color:'#4080af', width:1.5, opacity:0.675}, trk.trk);
				map.addLine(l);
			});

			// Plot Landing Score chart
			let chart = new google.visualization.ColumnChart(document.getElementById('lsChart'));
			let data = new google.visualization.DataTable();
			data.addColumn('number','Flight Number');
			data.addColumn('number','Landing Score');
			data.addColumn({type:'string',role:'style'});
			data.addColumn({type:'string',role:'tooltip'});
			data.addRows(js.landingScores);
			chart.draw(data,golgotha.charts.buildOptions({title:'Landing Scores',legend:{position:'none'}}));
			
			// Plot Flight Score chart
			chart = new google.visualization.PieChart(document.getElementById('fsChart'));
			data = new google.visualization.DataTable();
			data.addColumn('string','Flight Quality');
			data.addColumn('number','Flights');
			data.addRows(js.flightScores);
			chart.draw(data,golgotha.charts.buildOptions({title:'Flight Scores',is3D:true,colors:['green','orange','red'],legend:{position:'none'},tooltip:{trigger:'selection',ignoreBounds:true}}));
		});
	});
};
</script>
</body>
</html>
