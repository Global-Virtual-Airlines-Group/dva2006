<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Dispatch Route Plotter</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="airportRefresh" />
<map:api version="3" />
<content:js name="markermanager" />
<content:js name="progressBar" />
<content:js name="googleMapsWX" />
<content:js name="wxParsers" />
<content:js name="routePlot" />
<content:googleAnalytics eventSupport="true" />
<fmt:aptype var="useICAO" />
<script>
golgotha.local.loaders = golgotha.local.loaders || {};
golgotha.local.loaders.series = new golgotha.maps.SeriesLoader();
golgotha.local.loaders.series.setData('twcRadarHcMosaic', 0.45, 'wxRadar');
golgotha.local.loaders.series.setData('temp', 0.275, 'wxTemp');
golgotha.local.loaders.series.setData('windSpeed', 0.325, 'wxWind', 256, true);
golgotha.local.loaders.series.setData('windSpeedGust', 0.375, 'wxGust', 256, true);
golgotha.local.loaders.series.setData('sat', 0.325, 'wxSat');
golgotha.local.loaders.series.onload(function() { golgotha.util.enable('#selImg'); });

golgotha.local.validate = function(f)
{
// Check if we're saving an existing route
const routeID = parseInt(f.routeID.value);
if (!isNaN(routeID) && (routeID > 0))
	alert('Updating route #' + routeID);
	
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.airportD, t:'Departure Airport'});
golgotha.form.validate({f:f.airportA, t:'Arrival Airport'});
golgotha.form.validate({f:f.route, l:3, t:'Flight Route'});
golgotha.form.validate({f:f.cruiseAlt, l:4, t:'Cruising Altitude'});

golgotha.form.submit(f);
golgotha.event.beacon('Route Plotter', 'Save Route');
return true;
};

golgotha.routePlot.updateAirline = function(combo) {
	const f = document.forms[0];
	golgotha.airportLoad.changeAirline([f.airportD, f.airportA], golgotha.airportLoad.config);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onload="void golgotha.util.disable('RouteSaveButton')" onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="aCode" name="airline.code" />
<content:empty var="emptyList" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="dsproutesave.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> DISPATCH ROUTE PLOTTER</td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" options="${airlines}" value="${aCode}" onChange="void golgotha.routePlot.updateAirline(this)" /></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" size="1" idx="*" options="${airportsD}" firstEntry="-" value="${airportD}" onChange="void golgotha.routePlot.updateRoute(true, true)" />
 <el:airportCode combo="airportD" airport="${airportD}" idx="*" /><span id="runways" style="visibility:hidden;"> departing <el:combo name="runway" idx="*" size="1" options="${emptyList}" firstEntry="-" onChange="void golgotha.routePlot.updateRoute(true, false)" /></span></td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" size="1" idx="*" options="${airportsA}" firstEntry="-" value="${airportA}" onChange="void golgotha.routePlot.updateRoute(true)" />
 <el:airportCode combo="airportA" airport="${airportA}" idx="*" /></td>
</tr>
<tr>
 <td class="label">Alternate</td>
 <td class="data"><el:combo name="airportL" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="golgotha.routePlot.updateRoute(); golgotha.routePlot.plotMap()" />
 <el:airportCode combo="airportL" idx="*" /></td>
</tr>
<tr id="sids" style="display:none;">
 <td class="label">Standard Departure (SID)</td>
 <td class="data"><el:combo name="sid" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="golgotha.routePlot.updateRoute(); golgotha.routePlot.plotMap()" /></td>
</tr>
<tr id="stars" style="display:none;">
 <td class="label">Terminal Arrival (STAR)</td>
 <td class="data"><el:combo name="star" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="golgotha.routePlot.updateRoute(); golgotha.routePlot.plotMap()" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="noRecenter" value="true" label="Do not move Map center on Route updates" /></td>
</tr>
<tr class="title caps">
 <td colspan="2" class="left">ROUTE SEARCH</td>
</tr>
<tr>
 <td class="label">Saved Routes</td>
 <td class="data"><el:combo name="routes" idx="*" size="1" className="small req" options="${emptyList}" firstEntry="No Routes Loaded" onChange="void golgotha.routePlot.setRoute(this)" />
 <el:box name="external" value="true" className="small" label="Search FlightAware route database" /> <el:button ID="SearchButton" onClick="void golgotha.routePlot.searchRoutes()" label="SEARCH" /></td>
</tr>
<tr class="title caps">
 <td colspan="2" class="left">PLOTTED ROUTE<span id="rtDistance"></span><span id="rtETOPS"></span></td>
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
 <td class="label">Waypoints</td>
 <td class="data"><el:text name="route" size="80" max="320" idx="*" value="" spellcheck="false" onChange="golgotha.routePlot.updateRoute(); golgotha.routePlot.plotMap()" /></td>
</tr>
<tr>
 <td class="label">Cruising Altitude</td>
 <td class="data"><el:text name="cruiseAlt" size="5" max="5" idx="*" value="35000" spellcheck="false" onChange="void golgotha.routePlot.updateRoute();" /></td>
</tr>
<tr>
 <td class="label top">Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="80%" height="4" onChange="void golgotha.routePlot.updateRoute();" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="UpdateButton" onClick="void plotMap()" label="UPDATE ROUTE MAP" />&nbsp;<el:button ID="RouteSaveButton" type="submit" label="SAVE DISPATCH ROUTE" /></td>
</tr>
</el:table>
<el:text name="routeID" type="hidden" value="true" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:sysdata var="wuAPI" name="security.key.wunderground" />
<script>
const f = document.forms[0];
golgotha.util.disable(f.routes);
golgotha.util.disable('SearchButton', (f.airportD.selectedIndex == 0) || (f.airportA.selectedIndex == 0));

// Load the airports
const cfg = golgotha.airportLoad.config; 
cfg.doICAO = '${useICAO}';
golgotha.airportLoad.setHelpers([f.airportD,f.airportA,f.airportL]);
const newCfg = cfg.clone();

<c:if test="${empty airportsD}">
newCfg.airline = golgotha.form.getCombo(f.airline); 
f.airportD.loadAirports(newCfg);</c:if>
<c:if test="${empty airportsA}">
window.setTimeout(function() { f.airportA.loadAirports(newCfg); }, 1050);</c:if>
window.setTimeout(function() { newCfg.airline = 'all'; f.airportL.loadAirports(newCfg); }, 1250);

// Create the map
const mapOpts = {center:{lat:38.88,lng:-93.25}, zoom:4, minZoom:2, maxZoom:10, scrollwheel:false, clickableIcons:false, streetViewControl:false, mapTypeControlOptions:{mapTypeIds: golgotha.maps.DEFAULT_TYPES}};
const map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
map.setMapTypeId(golgotha.maps.info.type);
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', map.closeWindow);
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);

// Build the weather layer controls
const ctls = map.controls[google.maps.ControlPosition.BOTTOM_LEFT];
const jsl = new golgotha.maps.ShapeLayer({maxZoom:8, nativeZoom:6, opacity:0.375, zIndex:golgotha.maps.z.OVERLAY}, 'Jet', 'wind-jet');
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Radar', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('twcRadarHcMosaic'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Temperature', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('temp'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Wind Speed', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('windSpeed'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Wind Gusts', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('windSpeedGust'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Clouds', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('sat'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Jet Stream'}, jsl));
ctls.push(new golgotha.maps.LayerClearControl(map));

// Display the copyright notice and text boxes
map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('copyright'));
map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('mapStatus'));

// Build departure gates marker manager
golgotha.routePlot.dGates = new MarkerManager(map, {maxZoom:14});
golgotha.routePlot.aGates = new MarkerManager(map, {maxZoom:14});
golgotha.routePlot.etopsCheck = false;

// Load data async once tiles are loaded
google.maps.event.addListenerOnce(map, 'tilesloaded', function() {
	golgotha.local.loaders.series.loadGinsu();
	google.maps.event.trigger(map, 'maptypeid_changed');
});
<c:if test="${!empty airportD}">golgotha.routePlot.plotMap();</c:if>
</script>
</body>
</html>
