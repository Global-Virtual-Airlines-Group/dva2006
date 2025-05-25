<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
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
<content:googleJS module="charts" />
<map:api version="3" />
<fmt:aptype var="useICAO" />
<script async>
golgotha.local.update = function(cb) {
	if (!golgotha.form.check()) return false;
	self.location = '/airportinfo.do?id=' + encodeURI(golgotha.form.getCombo(cb));
	golgotha.form.submit(cb.parentElement);
	return true;
};

golgotha.local.selectGate = function(e) {
	console.log(e.target.gateID);
	const mrk = golgotha.gate.mrks[e.target.gateID];
	if (mrk) google.maps.event.trigger(mrk, 'click' );
	return (mrk);
};

golgotha.local.updateGateStats = function() {
	const f = document.forms[0];
	const xreq = new XMLHttpRequest();
	xreq.timeout = 7500;
	xreq.open('get', '/gateuse.ws?a=${airport.ICAO}&a2=' + golgotha.form.getCombo(f.gateAP) + '&isDeparture=' + golgotha.form.getCheck(f.gateAirportType), true);
	xreq.onreadystatechange = function() {
		if ((xreq.readyState != 4) || (xreq.status != 200)) return false;
		const jsData = JSON.parse(xreq.responseText);
		const drSpan = document.getElementById('gateUsageDays');
		drSpan.innerText = jsData.dayRange;
		const oldRows = golgotha.util.getElementsByClass('gateStat', 'tr');
		oldRows.forEach(function(r) { r.remove(); });

		const t = document.getElementById('apInfoTable');
		for (var x = 0; x < jsData.gates.length; x++) {
			const g = jsData.gates[x];
			const r = document.createElement('tr');
			r.setAttribute('class', 'gateStat');
			const c0 = golgotha.util.createElement('td', g.name, 'label');
			c0.addEventListener('click', golgotha.local.selectGate, {passive:true});
			c0.gateID = g.id;
			r.appendChild(c0);
			const c1 = document.createElement('td');
			c1.appendChild(golgotha.util.createElement('span', g.zone, 'sec bld'));
			const as = golgotha.util.createElement('span', ' Used by ', 'small ita');
			g.airlines.forEach(function(al) {
				as.innerText += al.name;
				if (al.useCount > 0) as.innerText += (' (' + al.useCount + ')');
				as.innerText += ', ';
			});

			as.innerText = as.innerText.substring(0, Math.max(1, as.innerText.length - 2));
			c1.appendChild(as);
			r.appendChild(c1);
			r.appendChild(golgotha.util.createElement('td', g.useCount + ' Flights', 'pri bld'));
			t.appendChild(r);
		}

		return true;
	};

	xreq.send(null);
	return true;
}

golgotha.onDOMReady(function() {
	const f = document.forms[0];
	const cfg = golgotha.airportLoad.config;
	cfg.doICAO = ${useICAO};
	golgotha.airportLoad.setHelpers([f.id]);
	golgotha.local.updateGateStats();
	return true;
});
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
<el:table className="form" ID="apInfoTable">
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline />&nbsp;</span>AIRPORT INFORMATION -<span class="nophone"> ${airport.name}</span> (<fmt:airport airport="${airport}" />)</td>
 <td style="width:35%" class="nophone right">AIRPORT <el:combo name="id" size="1" idx="*" value="${airport}" options="${airports}"  onChange="void golgotha.local.update(this)" />
 <el:text name="idCode" size="3" max="4" className="caps" value="${airport.ICAO}" onBlur="void document.forms[0].id.setAirport(this.value, true)" /></td>
</tr>
<tr>
 <td class="label">IATA / ICAO Codes</td>
 <td class="data" colspan="2"><span class="pri bld">${airport.IATA}</span> / <span class="sec bld">${airport.ICAO}</span><c:if test="${!empty chartTypes}"> - <el:cmd url="charts" linkID="${a.ICAO}" className="bld caps">Approach Charts</el:cmd></c:if></td>
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
<td class="data pri bld caps" colspan="2">Continuous ${isSummer ? 'Daylight' : 'Darkness'}</td>
</c:when> 
<c:when test="${empty sunrise}">
 <td class="data sec bld" colspan="2">Continuous Darkness begins at <fmt:date date="${sunset}" fmt="t" tz="${airport.TZ}" /></td>
</c:when>
<c:when test="${empty sunset}">
 <td class="data sec bld" colspan="2">Continuous Daylight begins at <fmt:date date="${sunrise}" fmt="t" tz="${airport.TZ}" /></td>
</c:when>
<c:otherwise>
 <td class="data" colspan="2">Sun rises at <fmt:date date="${sunrise}" fmt="t" tz="${airport.TZ}" />, sets at <fmt:date date="${sunset}" fmt="t" tz="${airport.TZ}" /></td>
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
<c:if test="${airport.isSchengen || airport.hasPFI}">
<tr>
 <td class="label top">Customs Zones</td>
 <td class="data bld caps" colspan="2"><c:if test="${airport.isSchengen}"><div class="ter">This Airport is part of the Schengen Area</div></c:if>
<c:if test="${airport.hasPFI}"><span class="pri">This Airport has a US Customs Pre-Flight Inspection station</span></c:if></td>
</tr>
</c:if>
<c:if test="${!empty wx}">
<tr>
 <td class="label top">Current Weather</td>
 <td class="data" colspan="2"><c:if test="${wx.windSpeed > 0}">Winds <fmt:int value="${wx.windDirection}"  fmt="000" />&deg;, <fmt:int value="${wx.windSpeed}" /> kts<c:if test="${wx.windGust > wx.windSpeed}">, gusting to <fmt:int value="${wx.windGust}" /> kts</c:if>
 <br /></c:if>${wx.data}</td>
</tr>
</c:if>
<c:if test="${!empty atisD}">
<tr>
 <td class="label top">${atisD.type.description} ATIS</td>
 <td class="data" colspan="2"><span class="pri bld">${atisD.code}</span> - <span class="ita">Effective <fmt:date date="${atisD.effectiveDate}" t="HH:mm" default="N/A" /></span><br />
<span class="small">${atisD.data}</span></td>
</tr>
</c:if>
<c:if test="${!empty atisA}">
<tr>
 <td class="label top">${atisA.type.description} ATIS</td>
 <td class="data" colspan="2"><span class="pri bld">${atisA.code}</span> - <span class="ita">Effective <fmt:date date="${atisA.effectiveDate}" t="HH:mm" default="N/A"/></span><br />
<span class="small">${atisA.data}</span></td>
</tr>
</c:if>
<tr>
 <td class="label top">Takeoff Runways</td>
 <td class="data" colspan="2"><c:forEach var="rwy" items="${departureRwys}">
<c:set var="isActive" value="${validRunways.contains(rwy.name)}"  scope="page" />
<div class="${isActive ? 'sec bld' : 'warn'}">Runway ${rwy.name}<c:if test="${!empty rwy.alternateCode}">&nbsp;<span class="ita">[${rwy.isAltNew() ? 'now' : 'was'}&nbsp;${rwy.alternateCode }]</span></c:if>, (<fmt:int value="${rwy.length}" /> feet<c:if test="${rwy.thresholdLength > 0}">, displaced 
<fmt:int value="${rwy.thresholdLength}" /> feet</c:if>) - Heading ${rwy.heading}&deg; <span class="ita"><fmt:int value="${rwy.useCount}" /> departures (<fmt:int value="${rwy.percentage}" />%)</span></div>
</c:forEach></td>
</tr>
<tr>
 <td class="label top">Landing Runways</td>
 <td class="data" colspan="2"><c:forEach var="rwy" items="${arrivalRwys}">
<c:set var="isActive" value="${validRunways.contains(rwy.name)}"  scope="page" />
<div class="${isActive ? 'sec bld' : 'warn'}">Runway ${rwy.name}<c:if test="${!empty rwy.alternateCode}">&nbsp;<span class="ita">[${rwy.isAltNew() ? 'now' : 'was'}&nbsp;${rwy.alternateCode}]</span></c:if>, (<fmt:int value="${rwy.length}" /> feet<c:if test="${rwy.thresholdLength > 0}">, displaced 
<fmt:int value="${rwy.thresholdLength}" /> feet</c:if>) - Heading ${rwy.heading}&deg; <span class="ita"><fmt:int value="${rwy.useCount}" /> arrivals (<fmt:int value="${rwy.percentage}" />%)</span></div> 
</c:forEach></td>
<c:if test="${maxRwyLength > 0}">
<c:if test="${!empty validAC}">
<tr>
 <td class="label top">Authorized Aircraft</td>
 <td class="data" colspan="2"><span class="ita small">Due to runway lengths, only the following ${validAC.size()} aircraft are authorized for operation in and out of this Airport:</span><br /><br />
<fmt:list value="${validAC}" delim=", " /></td>
</tr>
</c:if>
<c:if test="${!empty invalidAC}">
<tr>
 <td class="label top">Unauthorized Aircraft</td>
 <td class="data" colspan="2"><span class="ita small">Due to runway lengths, the following ${validAC.size()} aircraft are <span class="pri bld">NOT</span> authorized for operation in and out of this Airport:</span><br /><br />
<fmt:list value="${invalidAC}" delim=", " /></td>
</tr>
</c:if>
</c:if>
<c:if test="${!empty otherRunways}">
<tr>
 <td class="label top">Other Runways</td>
 <td class="data" colspan="2"><c:forEach var="rwy" items="${otherRunways}">
<c:set var="dCount" value="${odRwyStats[rwy.name]}" scope="page" />
<c:set var="aCount" value="${oaRwyStats[rwy.name]}" scope="page" />
Runway ${rwy.name}<c:if test="${!empty rwy.alternateCode}">&nbsp;<span class="ita">[${rwy.isAltNew() ? 'now' : 'was'}&nbsp;${rwy.alternateCode}]</span></c:if>, (<fmt:int value="${rwy.length}" /> feet) - Heading ${rwy.heading}&deg; (<fmt:int value="${dCount.useCount}" /> departures, <fmt:int value="${aCount.useCount}" /> arrivals)<br /></c:forEach></td>
</tr>
</c:if>
<tr>
 <td class="label top">Taxi Times</td>
 <td class="data" colspan="2">Inbound: <c:if test="${!empty taxiTimeCY.inboundTime}"><span class="bld"><fmt:duration t="[H:]mm:ss" duration="${taxiTimeCY.inboundTime}" /> (${taxiTimeCY.year})</span> - </c:if><fmt:duration t="[H:]mm:ss" duration="${taxiTime.inboundTime}" /> (All Years)<br />
Outbound: <c:if test="${!empty taxiTimeCY.outboundTime}"><span class="bld"><fmt:duration t="[H:]mm:ss" duration="${taxiTimeCY.outboundTime}" /> (${taxiTimeCY.year})</span> - </c:if><fmt:duration t="[H:]mm:ss" duration="${taxiTime.outboundTime}" /> (All Years)</td>
</tr>
<c:if test="${!empty popularAlternates}">
<tr>
 <td class="label top">Common Alternates</td>
 <td class="data" colspan="2"><c:forEach var="altA" items="${popularAlternates}" varStatus="altStatus">${altA.name} (<el:cmd url="airportinfo" linkID="${altA.IATA}"><fmt:airport airport="${altA}" /></el:cmd>) <span class="sec bld">(<fmt:distance value="${altA.distanceTo(airport)}" />)</span><c:if test="${!altStats.last}"><br /></c:if></c:forEach>
</tr>
</c:if>
<tr id="flightTimeChart" style="display:none;">
 <td class="label top">Flight Time Distribution</td>
 <td class="data" colspan="2"><div id="ftChart" style="height:250px;"></div></td>
</tr>
<tr class="title caps">
 <td colspan="3">AIRPORT MAP<span id="mapToggle" class="toggle" onclick="void golgotha.util.toggleExpand(this, 'airportMap')">COLLAPSE</span></td>
</tr>
<tr class="airportMap">
 <td class="label">Gate Legend</td>
 <td class="data" colspan="2"><img src="https://maps.google.com/mapfiles/kml/pal2/icon56.png" alt="Our Gate" width="16" height="16" />&nbsp;Domestic
 | <img src="https://maps.google.com/mapfiles/kml/pal2/icon48.png" alt="International Gate"  width="16" height="16" />&nbsp;International
<c:if test="${airport.hasPFI}"> | <img src="https://maps.google.com/mapfiles/kml/pal2/icon16.png" alt="USPFI Gate" width="16" height="16" />&nbsp;US PFI</c:if>
<c:if test="${airport.isSchengen}"> | <img src="https://maps.google.com/mapfiles/kml/pal2/icon17.png" alt="Schengen Gate" width="16" height="16" />&nbsp;Schengen</c:if>
 | <img src="https://maps.google.com/mapfiles/kml/pal3/icon52.png" alt="Frequently Used Gate"  width="16" height="16" /> Frequently Used
 | <img src="https://maps.google.com/mapfiles/kml/pal3/icon60.png" alt="Other Gate"  width="16" height="16" /> Other
<content:filter roles="Operations,Schedule"> | <el:cmd url="gateinfo" linkID="${airport.ICAO}" className="sec bld">EDIT GATE DATA</el:cmd></content:filter></td>
</tr>
<tr class="airportMap">
 <td colspan="3"><map:div ID="mapBox" height="540" /></td>
</tr>
<tr class="title caps">
 <td colspan="3"><span id="gateType">DEPARTURE</span> GATE DATA<span class="nophone"> (<span id="gateUsageDays"><fmt:int value="${gateUsageDays}" /></span> DAYS)</span><span id="gateToggle" class="toggle" onclick="void golgotha.util.toggleExpand(this, 'gateInfo')">COLLAPSE</span>
</tr>
<tr class="gateInfo" id="gateInfoHdr">
 <td class="label">Airports</td>
 <td class="data" colspan="2"><el:check name="gateAirportType" type="radio" options="${gaTypes}" value="true" onChange="void golgotha.local.updateGateStats()"  /> <el:combo name="gateAP" options="${dGateAirports}" firstEntry="[ ALL AIRPORTS ]" onChange="void golgotha.local.updateGateStats()" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
<map:token />

<map:point var="golgotha.local.mapC" point="${airport}" />
<map:bounds var="golgotha.local.mapBounds" items="${rwys}" />

// Create the map
const map = new golgotha.maps.Map(document.getElementById('mapBox'), {center:golgotha.local.mapC,zoom:15,minZoom:12,maxZoom:19,scrollZoom:false,style:'mapbox://styles/mapbox/satellite-v9'});
map.fitBounds(golgotha.local.mapBounds);
golgotha.gate.load({id:'${airport.ICAO}'});
map.on('zoomend', function() { map.toggle(golgotha.local.gates, (map.getZoom() > 11)); });

// Load charts
google.charts.load('current', {'packages':['corechart']});
const p = fetch('ftstats.ws?airport=${airport.ICAO}', {signal:AbortSignal.timeout(7500)});
p.handle = function(rsp) {
	if (rsp.status != 200) return false;
	rsp.json().then(function(js) {
		const fC = new google.visualization.ColumnChart(document.getElementById('ftChart'));
		const fData = new google.visualization.DataTable(); 
		const nf = new google.visualization.NumberFormat({pattern:'00'});
		fData.addColumn('number', 'Hour of Day'); nf.format(fData, 0); 
		fData.addColumn('number', 'Domestic Departures'); fData.addColumn('number', 'International Departures');
		fData.addColumn('number', 'Domestic Arrivals'); fData.addColumn('number', 'International Arrivals');
		js.flights.forEach(function(h) { fData.addRow([h.hour, h.dd, h.di, h.ad, h.ai]); });
		golgotha.util.display('flightTimeChart', true);
		const mnStyle = {gridlines:{color:'#cce'},title:'Hour of Day',format:'##:00'};
		const opts = golgotha.charts.buildOptions({title:'Flights by Hour of Day',isStacked:true,hAxis:mnStyle,vAxis:{title:'Flight Legs'},width:'100%'});
		fC.draw(fData,opts);
	});
};

google.charts.setOnLoadCallback(function() { p.then(p.handle) });
</script>
<content:googleAnalytics />
</body>
</html>
