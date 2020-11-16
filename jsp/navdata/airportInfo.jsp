<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Airport Information</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:js name="gateInfo" />
<content:json />
<content:googleJS module="charts" />
<map:api version="3" />
<fmt:aptype var="useICAO" />
<script async>
golgotha.local.update = function(combo) {
	self.location = '/airportinfo.do?id=' + golgotha.form.getCombo(combo);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="isNorthSummer" value="${(localTime.monthValue >2) && (localTime.monthValue < 10)}" scope="page" />
<c:set var="isSummer" value="${(isNorthSummer && (airport.latitude > 0)) || (!isNorthSummer && (airport.latitude < 0))}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="airportinfo.do" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline />&nbsp;</span>AIRPORT INFORMATION - ${airport.name} (<fmt:airport airport="${airport}" />)</td>
 <td style="width:35%" class="nophone right">AIRPORT <el:combo name="id" size="1" idx="*" value="${airport}" options="${airports}"  onChange="void golgotha.local.update(this)" />
 <el:text name="idCode" size="3" max="4" className="caps" value="${airport.ICAO}" onBlur="void document.forms[0].id.setAirport(this.value, true)" /></td>
</tr>
<tr>
 <td class="label">IATA / ICAO Codes</td>
 <td class="data" colspan="2"><span class="pri bld">${airport.IATA}</span> / <span class="sec bld">${airport.ICAO}</span></td>
</tr>
<tr>
 <td class="label">Location</td>
 <td class="data" colspan="2"><fmt:geo pos="${airport}" /> (<fmt:int value="${airport.altitude}" /> feet MSL) - <c:if test="${!empty airport.state}">${airport.state.name}, </c:if>${airport.country.name}&nbsp;<el:flag countryCode="${airport.country.code}" /></td>
</tr>
<tr>
 <td class="label">Time Zone</td>
 <td class="data" colspan="2">${airport.TZ}<span class="ita"> (Current local time: <fmt:date date="${localTime}" tz="${airport.TZ}" t="HH:mm" />)</span></td>
</tr>
<tr>
 <td class="label">Sunrise / Sunset</td>
 <c:choose>
<c:when test="${(empty sunrise) && (empty sunset)}">
<td class="data pri bld caps">Continuous ${isSummer ? 'Daylight' : 'Darkness'}</td>
</c:when> 
<c:when test="${empty sunrise}">
 <td class="data sec bld">Continuous Darkness begins at <fmt:date date="${sunset}" fmt="t" tz="${airport.TZ}" /></td>
</c:when>
<c:when test="${empty sunset}">
 <td class="data sec bld">Continuous Daylight begins at <fmt:date date="${sunrise}" fmt="t" tz="${airport.TZ}" /></td>
</c:when>
<c:otherwise>
 <td class="data">Sun rises at <fmt:date date="${sunrise}" fmt="t" tz="${airport.TZ}" />, sets at <fmt:date date="${sunset}" fmt="t" tz="${airport.TZ}" /></td>
</c:otherwise>
</c:choose>
</tr>
<c:if test="${!empty schedAirlines}">
<tr>
 <td class="label top">Airlines Served</td>
 <td class="data" colspan="2"><fmt:list value="${schedAirlines}" delim=", " /></td>
</tr>
</c:if>
<tr>
 <td class="label">Flight Operations</td>
 <td class="data" colspan="2">Departures: <span class="small ita"><fmt:list value="${dDays}" delim=", " empty="NONE" /></span>, Arrivals: <span class="small ita"><fmt:list value="${aDays}" delim=", " empty="NONE" /></span></td>
</tr>
<c:if test="${!empty wx}">
<tr>
 <td class="label top">Current Weather</td>
 <td class="data" colspan="2"><c:if test="${wx.windSpeed > 0}">Winds <fmt:int value="${wx.windDirection}"  fmt="000" />&deg;, <fmt:int value="${wx.windSpeed}" /> kts<c:if test="${wx.windGust > wx.windSpeed}">, gusting to <fmt:int value="${wx.windGust}" /> kts</c:if>
 <br /></c:if>${wx.data}</td>
</tr>
</c:if>
<tr>
 <td class="label top">Takeoff Runways</td>
 <td class="data" colspan="2"><c:forEach var="rwy" items="${toRwys}">
<c:set var="isActive" value="${fn:contains(validRunways, rwy.name)}"  scope="page" />
<div class="${isActive ? 'sec bld' : 'warn'}">Runway ${rwy.name}, (<fmt:int value="${rwy.length}" /> feet<c:if test="${rwy.thresholdLength > 0}">, displaced <fmt:int value="${rwy.thresholdLength}" /> feet</c:if>) - Heading ${rwy.heading}&deg; <span class="ita"><fmt:int value="${rwy.useCount}" /> departures</span></div>
</c:forEach></td>
</tr>
<tr>
 <td class="label top">Landing Runways</td>
 <td class="data" colspan="2"><c:forEach var="rwy" items="${ldgRwys}">
<c:set var="isActive" value="${fn:contains(validRunways, rwy.name)}"  scope="page" />
<div class="${isActive ? 'sec bld' : 'warn'}">Runway ${rwy.name}, (<fmt:int value="${rwy.length}" /> feet<c:if test="${rwy.thresholdLength > 0}">, displaced <fmt:int value="${rwy.thresholdLength}" /> feet</c:if>) - Heading ${rwy.heading}&deg; <span class="ita"><fmt:int value="${rwy.useCount}" /> arrivals</span></div> 
</c:forEach></td>
</tr>
<c:if test="${!empty runways}">
<tr>
 <td class="label top">Other Runways</td>
 <td class="data" colspan="2"><c:forEach var="rwy" items="${runways}">
<c:set var="isActive" value="${fn:contains(validRunways, rwy.name)}"  scope="page" />
<div class="${isActive ? 'sec bld' : 'warn'}">Runway ${rwy.name}, (<fmt:int value="${rwy.length}" /> feet) - Heading ${rwy.heading}&deg;</div> 
</c:forEach></td>
</tr>
</c:if>
<content:filter roles="Schedule,Operations">
<c:if test="${!empty invalidRwys}">
<tr>
 <td class="label top">Obsolete Runways</td>
 <td class="data" colspan="2"><c:forEach var="rwy" items="${invalidRwys}">
<div>Runway ${rwy.name}</div></c:forEach></td>
</tr>
</c:if>
</content:filter>
<tr id="flightTimeChart" style="display:none;">
 <td class="label top">Flight Time Distribution</td>
 <td class="data"><div id="ftChart" style="height:250px;"></div></td>
</tr>
<c:if test="${!empty connectingAirports}">
<tr>
 <td class="label">Connecting Airport</td>
 <td class="data" colspan="2"><el:combo name="airportA" size="1" idx="*" firstEntry="[ SELECT AIRPORT ]" options="${connectingAirports}" onChange="void golgotha.local.updateAirportA(this, '${airport.ICAO}')" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Gate Legend</td>
 <td class="data"><span class="small"><img src="https://maps.google.com/mapfiles/kml/pal2/icon56.png" alt="Our Gate"  width="16" height="16" />&nbsp;<content:airline /> Domestic Gates
 | <img src="https://maps.google.com/mapfiles/kml/pal2/icon48.png" alt="International Gate"  width="16" height="16" />&nbsp;<content:airline /> International Gates
 | <img src="https://maps.google.com/mapfiles/kml/pal3/icon52.png" alt="Frequently Used Gate"  width="16" height="16" /> Frequently Used Gates
 | <img src="https://maps.google.com/mapfiles/kml/pal3/icon60.png" alt="Other Gate"  width="16" height="16" /> Other Gates</span></td>
 <td class="mid">&nbsp;<content:filter roles="Schedule,Operations"><c:if test="${!empty airlines}"><a id="editLink" href="javascript:void golgotha.gate.edit()">EDIT GATE DATA</a>
<el:combo ID="airlineCombo" name="airline"  size="1" idx="*" options="${airlines}" firstEntry="[ AIRLINE ]"  style="display:none;" onChange="void golgotha.gate.updateAirline(this)" />
<a id="saveLink" style="display:none;" href="javascript:void golgotha.gate.save()">SAVE GATE DATA</a>
<span id="helpText" style="display:none;" class="small"><br />Double-click to associate a gate with <span id="airlineName"></span>, right-click to mark a gate as International</span></c:if></content:filter></td>
</tr>
<tr>
 <td colspan="3"><map:div ID="googleMap" height="570" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
<map:point var="golgotha.local.mapC" point="${airport}" />
<map:bounds var="golgotha.local.mapBounds" items="${rwys}" />

// Create the map
const mapOpts = {center:golgotha.local.mapC, zoom:15, minZoom:12, maxZoom:19, scrollwheel:false, clickableIcons:false, streetViewControl:false};
const map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
map.setMapTypeId(google.maps.MapTypeId.SATELLITE);
map.fitBounds(golgotha.local.mapBounds);
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', map.closeWindow);
google.maps.event.addListener(map.infoWindow, 'closeclick', map.closeWindow);
google.maps.event.addListener(map, 'zoom_changed', function() {
	map.toggle(golgotha.local.gates, (map.getZoom() > 11));
	map.toggle(golgotha.local.ourGates, (map.getZoom() > 10));
	return true;
});

golgotha.onDOMReady(function() { golgotha.gate.load('${airport.ICAO}'); golgotha.airportLoad.setHelpers(document.forms[0].id); });

google.charts.load('current', {'packages':['corechart']});
const xmlreq = new XMLHttpRequest();
xmlreq.open('get', 'ftstats.ws?airport=${airport.ICAO}', true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	const js = JSON.parse(xmlreq.responseText);
	const lgStyle = {color:'black',fontName:'Verdana',fontSize:8};
	
	// Display the chart
	const fC = new google.visualization.ColumnChart(document.getElementById('ftChart'));
	const fData = new google.visualization.DataTable(); 
	const nf = new google.visualization.NumberFormat({pattern:'00'});
	fData.addColumn('number', 'Hour of Day'); nf.format(fData, 0); 
	fData.addColumn('number', 'Domestic Departures'); fData.addColumn('number', 'International Departures');
	fData.addColumn('number', 'Domestic Arrivals'); fData.addColumn('number', 'International Arrivals');
	js.flights.forEach(function(h) { fData.addRow([h.hour, h.dd, h.di, h.ad, h.ai]); });
	golgotha.util.display('flightTimeChart', true);
	const mnStyle = {gridlines:{color:'#cce'},title:'Hour of Day',format:'##:00'};
	fC.draw(fData,{title:'Flights by Hour of Day',isStacked:true,fontSize:10,hAxis:mnStyle,vAxis:{title:'Flight Legs'},width:'100%'});
	return true;
};

google.charts.setOnLoadCallback(function() { xmlreq.send(null); });
</script>
<content:googleAnalytics />
</body>
</html>
