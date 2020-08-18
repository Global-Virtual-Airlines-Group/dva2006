<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Route Plotter</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:json />
<content:js name="airportRefresh" />
<map:api version="3" />
<content:js name="markermanager" />
<content:js name="progressBar" />
<content:js name="googleMapsWX" />
<content:js name="wxParsers" />
<content:js name="routePlot" />
<content:js name="fileSaver" />
<content:googleAnalytics eventSupport="true" />
<content:getCookie name="acarsMapZoomLevel" default="12" var="zoomLevel" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />
<script>
var loaders = {};
loaders.series = new golgotha.maps.SeriesLoader();
loaders.series.setData('twcRadarHcMosaic', 0.45, 'wxRadar');
loaders.series.setData('temp', 0.275, 'wxTemp');
loaders.series.setData('windSpeed', 0.325, 'wxWind', 256, true);
loaders.series.setData('windSpeedGust', 0.375, 'wxGust', 256, true);
loaders.series.onload(function() { golgotha.util.enable('#selImg'); });

golgotha.routePlot.keepRoute = ${fn:length(flight.route) > 0};
golgotha.local.validate = function(f) {
    golgotha.form.validate({f:f.eqType, t:'EquipmentType'});
    golgotha.form.validate({f:f.airportD, t:'Departure Airport'});
    golgotha.form.validate({f:f.airportA, t:'Arrival Airport'});
    golgotha.form.validate({f:f.route, l:3, t:'Flight Route'});
    return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="aCode" name="airline.code" />
<content:empty var="emptyList" />
<content:enum var="simVersions" className="org.deltava.beans.Simulator" exclude="UNKNOWN,FS98,FS2000,FS2002,P3D,XP9,XP10" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="routeplan.ws" method="post" target="_new" validate="return golgotha.form.wrap(golgotha.local.validate,this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> FLIGHT PLAN PLOTTER</td>
</tr>
<tr>
 <td class="label">Aircraft</td>
 <td class="data"><el:combo name="eqType" className="req" size="1" idx="*" options="${eqTypes}" firstEntry="[ AIRCRAFT ]" value="${flight.equipmentType}" onChange="void golgotha.routePlot.updateRoute(false)" /></td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" options="${airlines}" firstEntry="[ AIRLINE ]" value="${flight.airline}" /></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" className="req" size="1" idx="*" options="${airportsD}" firstEntry="-" value="${flight.airportD}" onChange="void golgotha.routePlot.updateRoute(true, true)" />
 <el:airportCode combo="airportD" airport="${flight.airportD}" idx="*" />
<span id="runways" style="visibility:hidden;"> departing <el:combo name="runway" idx="*" size="1" value="${rwy}" options="${dRwys}" firstEntry="-" onChange="void golgotha.routePlot.updateRoute(true, false)" /></span>
<c:if test="${!empty flight.airportD}"> <el:cmd url="airportInfo" linkID="${flight.airportD.ICAO}" className="small" target="_new">Airport Information</el:cmd></c:if></td>
</tr>
<tr id="gatesD" style="display:none;">
 <td class="label">Departure Gate</td>
 <td class="data"><el:combo name="gateD" size="1" idx="*" options="${emptyList}" firstEntry="[ GATE ]" onChange="golgotha.routePlot.plotMap()" /></td>
</tr>
<tr id="wxDr" style="display:none;">
 <td class="label">Origin Weather</td>
 <td class="data"><span id="wxDmetar"></span></td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" className="req" size="1" idx="*" options="${airportsA}" firstEntry="-" value="${flight.airportA}" onChange="void golgotha.routePlot.updateRoute(true)" />
 <el:airportCode combo="airportA" airport="${flight.airportA}" idx="*" />
<c:if test="${!empty flight.airportA}">&nbsp;<el:cmd url="airportInfo" linkID="${flight.airportA.ICAO}" className="small" target="_new">Airport Information</el:cmd></c:if></td>
</tr>
<tr id="gatesA" style="display:none;">
 <td class="label">Arrival Gate</td>
 <td class="data"><el:combo name="gateA" size="1" idx="*" options="${emptyList}" firstEntry="[ GATE ]" onChange="golgotha.routePlot.updateRoute(); golgotha.routePlot.plotMap()" /></td>
</tr>
<tr id="wxAr" style="display:none;">
 <td class="label top">Destination Weather</td>
 <td class="data"><span id="wxAmetar"></span><div id="wxAtaf" style="display:none;"></div></td>
</tr>
<tr id="airportL" style="display:none;">
 <td class="label">Alternate</td>
 <td class="data"><el:combo name="airportL" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="golgotha.routePlot.updateRoute(); golgotha.routePlot.plotMap()" />
 <el:airportCode combo="airportL" idx="*" /></td>
</tr>
<tr id="sids" style="display:none;">
 <td class="label">Standard Departure (SID)</td>
 <td class="data"><el:combo name="sid" size="1" idx="*" options="${sids}" value="${sid}" firstEntry="-" onChange="void golgotha.routePlot.plotMap()" /></td>
</tr>
<tr id="stars" style="display:none;">
 <td class="label">Terminal Arrival (STAR)</td>
 <td class="data"><el:combo name="star" size="1" idx="*" options="${stars}" value="${star}" firstEntry="-" onChange="void golgotha.routePlot.plotMap()" /></td>
</tr>
<tr>
 <td class="label">Waypoints</td>
 <td class="data"><el:text name="route" size="100" max="320" idx="*" value="${flight.route}" spellcheck="false" onChange="void golgotha.routePlot.plotMap()" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="noRecenter" value="true" label="Do not move Map center on Route updates" /><br />
<el:box name="showGates" value="true" label="Show Departure Gates" onChange="void golgotha.routePlot.toggleGates(golgotha.routePlot.dGates)" /><br />
<el:box name="showAGates" value="true" label="Show Arrival Gates" onChange="void golgotha.routePlot.toggleGates(golgotha.routePlot.aGates)" /></td>
</tr>
<tr class="title caps">
 <td colspan="2" class="left">ROUTE SEARCH</td>
</tr>
<tr>
 <td class="label">Saved Routes</td>
 <td class="data"><el:combo name="routes" idx="*" size="1" className="small req" options="${emptyList}" firstEntry="No Routes Loaded" onChange="void golgotha.routePlot.setRoute(this)" />
<el:button onClick="void golgotha.routePlot.searchRoutes()" label="SEARCH" />
<content:filter roles="Route,Dispatch,Operations"><el:box name="forceFAReload" value="true" checked="false" label="Force FlightAware refresh" /></content:filter></td>
</tr>
<tr>
 <td class="label top">Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="80%" height="2" readOnly="true" /></td>
</tr>
<tr class="title caps">
 <td colspan="2" class="left">PLOTTED ROUTE<span id="rtDistance"></span><span id="rtETOPS"></span></td>
</tr>
<tr id="gateLegendRow" style="display:none;">
 <td class="label">Gate Legend</td>
 <td class="data"><span class="small"><img src="https://maps.google.com/mapfiles/kml/pal2/icon56.png" alt="Our Gate"  width="16" height="16" />Domestic Gates | <img src="https://maps.google.com/mapfiles/kml/pal2/icon48.png" alt="International Gate"  width="16" height="16" />International Gates
 | <img src="https://maps.google.com/mapfiles/kml/pal3/icon52.png" alt="Frequently Used Gate"  width="16" height="16" /> Frequently Used Gates | <img src="https://maps.google.com/mapfiles/kml/pal3/icon60.png" alt="Other Gate"  width="16" height="16" /> Other Gates</span></td>
</tr>
<tr id="asWarnRow" style="display:none;'">
 <td class="label">&nbsp;</td>
 <td colspan="2" class="data bld"><span class="warn small caps">Route enters the following Restricted/Prohibited Airspace: <span id="aspaceList"></span></span></td>
</tr>
<tr>
 <td colspan="2" class="data"><map:div ID="googleMap" height="580" /><div id="copyright" class="small mapTextLabel"></div>
<div id="mapStatus" class="small mapTextLabel"></div></td>
</tr>
<tr>
 <td class="label">Simulator Version</td>
 <td class="data"><el:check type="radio" name="simVersion" idx="*" options="${simVersions}" value="${sim}" /></td>
</tr>
<tr>
 <td class="label">Cruising Altitude</td>
 <td class="data"><el:text name="cruiseAlt" size="5" max="5" idx="*" value="35000" spellcheck="false" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="saveDraft" label="Save as Draft Flight Report" value="true" checked="true" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button onClick="void plotMap()" label="UPDATE ROUTE MAP" />&nbsp;<el:button ID="SaveButton" type="submit" label="DOWNLOAD FLIGHT PLAN" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<fmt:aptype var="useICAO" />
<script id="mapInit" async>
const f = document.forms[0];
golgotha.util.disable(f.routes);
golgotha.util.disable('SearchButton');
try {
	golgotha.routePlot.hasBlob = !!new Blob;
} catch (e) {}

// Reset form links if blob download supported
if (golgotha.routePlot.hasBlob) {
	f.onsubmit = function() { return false; };
	const btn = document.getElementById('SaveButton');
	btn.onclick = golgotha.routePlot.download;
}

golgotha.airportLoad.config.doICAO = ${useICAO};
golgotha.airportLoad.config.airline = 'all';
golgotha.airportLoad.setHelpers([f.airportD,f.airportA,f.airportL]);
<c:choose>
<c:when test="${empty flight}">
// Load the airports
f.airportD.loadAirports(golgotha.airportLoad.config);
window.setTimeout(function() { f.airportA.loadAirports(golgotha.airportLoad.config); }, 700);
</c:when>
<c:otherwise>
golgotha.routePlot.keepRoute = ${(!empty sid) || (!empty star)}; 
golgotha.routePlot.updateRoute(${!empty rwy}, false);
</c:otherwise>
</c:choose>
// Create the map
const mapOpts = {center:{lat:38.88,lng:-93.25},zoom:4,minZoom:3,maxZoom:16,scrollwheel:false,clickableIcons:false,streetViewControl:false,mapTypeControlOptions:{mapTypeIds: golgotha.maps.DEFAULT_TYPES}};
const map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', map.closeWindow);
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);

// Create the jetstream layers
const jsOpts = {maxZoom:8, nativeZoom:5, opacity:0.55, zIndex:golgotha.maps.z.OVERLAY};
const hjsl = new golgotha.maps.ShapeLayer(jsOpts, 'High Jet', 'wind-jet');
const ljsl = new golgotha.maps.ShapeLayer(jsOpts, 'Low Jet', 'wind-lojet');

// Build the layer controls
const ctls = map.controls[google.maps.ControlPosition.BOTTOM_LEFT];
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Radar', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('twcRadarHcMosaic'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Temperature', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('temp'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Wind Speed', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('windSpeed'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Wind Gusts', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('windSpeedGust'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Clouds', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('sat'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Lo Jetstream'}, ljsl));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Hi Jetstream'}, hjsl));
ctls.push(new golgotha.maps.LayerClearControl(map));

// Display the copyright notice and text boxes
map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('copyright'));
map.controls[google.maps.ControlPosition.RIGHT_TOP].push(document.getElementById('mapStatus'));

// Build gates marker managers
golgotha.routePlot.dGates = new MarkerManager(map, {maxZoom:17});
golgotha.routePlot.aGates = new MarkerManager(map, {maxZoom:17});

// Load data async once tiles are loaded
google.maps.event.addListenerOnce(map, 'tilesloaded', function() {
	loaders.series.loadGinsu();
	google.maps.event.trigger(map, 'maptypeid_changed');
});
</script>
</body>
</html>
