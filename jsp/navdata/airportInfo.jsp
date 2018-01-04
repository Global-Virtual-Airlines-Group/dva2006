<!DOCTYPE toRwyshtml>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
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
<map:api version="3" />
<fmt:aptype var="useICAO" />
<content:protocol var="proto" />
<script>
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
 <td colspan="2"><span class="nophone"><content:airline /> </span>AIRPORT INFORMATION - ${airport.name} (<fmt:airport airport="${airport}" />)</td>
 <td style="width:35%" class="nophone right">AIRPORT <el:combo name="id" size="1" idx="*" value="${airport}" options="${airports}"  onChange="void golgotha.local.update(this)" />
 <el:text name="idCode" size="3" max="4" className="caps" value="${airport.ICAO}" onBlur="void document.forms[0].id.setAirport(this.value, true)" /></td>
</tr>
<tr>
 <td class="label">Location</td>
 <td class="data" colspan="2"><fmt:geo pos="${airport}" /> (<fmt:int value="${airport.altitude}" /> feet MSL) - <c:if test="${!empty airport.state}">${airport.state.name}, </c:if>${airport.country.name} <el:flag countryCode="${airport.country.code}" /></td>
</tr>
<tr>
 <td class="label">Time Zone</td>
 <td class="data" colspan="2">${airport.TZ} <span class="small ita">(Current local time: <fmt:date date="${localTime}" tz="${airport.TZ}" t="HH:mm" />)</span>
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
<c:if test="${!empty airlines}">
<tr>
 <td class="label">Airlines Served</td>
 <td class="data" colspan="2"><fmt:list value="${airlines}" delim=", " /></td>
</tr>
</c:if>
<c:if test="${!empty wx}">
<tr>
 <td class="label top">Current Weather</td>
 <td class="data" colspan="2"><c:if test="${wx.windSpeed > 0}">Winds <fmt:int value="${wx.windDirection}"  fmt="000" />&deg;, <fmt:int value="${wx.windSpeed}" /> kts<c:if test="${wx.windGust > wx.windSpeed}">, gusting to
 <fmt:int value="${wx.windGust}" /> kts</c:if>
 <br /></c:if>${wx.data}</td>
</tr>
</c:if>
<tr>
 <td class="label top">Takeoff Runways</td>
 <td class="data" colspan="2"><c:forEach var="rwy" items="${toRwys}">
<c:set var="isActive" value="${fn:contains(validRunways, rwy.name)}"  scope="page" />
<div class="${isActive ? 'sec bld' : 'warn'}">Runway ${rwy.name}, (<fmt:int value="${rwy.length}" /> feet) - Heading ${rwy.heading}&deg; <span class="ita"><fmt:int value="${rwy.useCount}" /> departures</span></div>
</c:forEach></td>
</tr>
<tr>
 <td class="label top">Landing Runways</td>
 <td class="data" colspan="2"><c:forEach var="rwy" items="${ldgRwys}">
<c:set var="isActive" value="${fn:contains(validRunways, rwy.name)}"  scope="page" />
<div class="${isActive ? 'sec bld' : 'warn'}">Runway ${rwy.name}, (<fmt:int value="${rwy.length}" /> feet) - Heading ${rwy.heading}&deg; <span class="ita"><fmt:int value="${rwy.useCount}" /> arrivals</span></div> 
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
<tr>
 <td class="label">Gate Legend</td>
 <td class="data"><span class="small"><img src="${proto}://maps.google.com/mapfiles/kml/pal2/icon56.png" alt="Our Gate"  width="16" height="16" /><content:airline /> Domestic Gates
 | <img src="${proto}://maps.google.com/mapfiles/kml/pal2/icon48.png" alt="International Gate"  width="16" height="16" /><content:airline /> International Gates
 | <img src="${proto}://maps.google.com/mapfiles/kml/pal3/icon52.png" alt="Frequently Used Gate"  width="16" height="16" /> Frequently Used Gates
 | <img src="${proto}://maps.google.com/mapfiles/kml/pal3/icon60.png" alt="Other Gate"  width="16" height="16" /> Other Gates</span></td>
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
<script id="mapInit">
<map:point var="golgotha.local.mapC" point="${airport}" />
<map:bounds var="golgotha.local.mapBounds" items="${rwys}" />

// Create the map
var mapOpts = {center:golgotha.local.mapC, zoom:15, minZoom:12, maxZoom:19, scrollwheel:false, clickableIcons:false, streetViewControl:false};
var map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
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
</script>
<content:googleAnalytics />
</body>
</html>
