<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Dispatch Route Plotter</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:json />
<content:js name="airportRefresh" />
<map:api version="3" libraries="weather" />
<content:js name="markermanager" />
<content:js name="progressBar" />
<content:js name="googleMapsWX" />
<content:js name="routePlot" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<c:if test="${!empty tileHost}">
<script type="text/javascript">
var frLoader;
frLoader = new golgotha.maps.FrontLoader();
var gsLoader;
gsLoader = new golgotha.maps.GinsuLoader(2);
gsLoader.setData('radar', 0.45, 'wxRadar');
gsLoader.setData('eurorad', 0.45, 'wxRadar');
gsLoader.setData('temp', 0.275, 'wxTemp');
gsLoader.setData('windspeed', 0.325, 'wxWind');
</script>
<map:wxList layers="radar,eurorad,temp,windspeed" function="gsLoader.load" max="2" />
<map:wxFronts function="frLoader.load" /></c:if>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />
<fmt:aptype var="useICAO" />
<script type="text/javascript">
var routeUpdated = false;
var getInactive = false;

function validate(form)
{
// Check if we're saving an existing route
try {
var f = document.forms[0];
var routeID = parseInt(f.routeID.value);
if (!isNaN(routeID) && (routeID > 0))
	alert('Updating route #' + routeID);
	
if (!checkSubmit()) return false;
if (!validateCombo(f.airportD, 'Departure Airport')) return false;
if (!validateCombo(f.airportA, 'Arrival Airport')) return false;
if (!validateText(f.route, 3, 'Flight Route')) return false;
if (!validateText(f.cruiseAlt, 4, 'Cruising Altitude')) return false;

setSubmit();
disableButton('SearchButton');
disableButton('UpdateButton');
disableButton('RouteSaveButton');
golgotha.event.beacon('Route Plotter', 'Save Route');
return true;
} catch (err) {
	alert(err.description)
	return false;
}
}

function updateAirline(combo)
{
var f = document.forms[0];
golgotha.airportLoad.changeAirline([f.airportD, f.airportA], golgotha.airportLoad.config);
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="disableButton('RouteSaveButton')">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="aCode" name="airline.code" />
<content:empty var="emptyList" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="dsproutesave.do" method="post" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> DISPATCH ROUTE PLOTTER</td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" options="${airlines}" value="${aCode}" onChange="void updateAirline(this)" /></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" size="1" idx="*" options="${airportsD}" firstEntry="-" value="${airportD}" onChange="void updateRoute(true, true)" />
 <el:text ID="airportDCode" name="airportDCode" idx="*" size="3" max="4" onChange="document.forms[0].airportD.setAirport(this.value); updateRoute(true)" />
<span id="runways" style="visibility:hidden;"> departing <el:combo name="runway" idx="*" size="1" options="${emptyList}" firstEntry="-" onChange="void updateRoute(true, false)" /></span></td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" size="1" idx="*" options="${airportsA}" firstEntry="-" value="${airportA}" onChange="void updateRoute(true)" />
 <el:text ID="airportACode" name="airportACode" idx="*" size="3" max="4" onChange="void document.forms[0].airportA.setAirport(this.value, true)" /></td>
</tr>
<tr>
 <td class="label">Alternate</td>
 <td class="data"><el:combo name="airportL" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="updateRoute(); plotMap()" />
 <el:text ID="airportLCode" name="airportLCode" idx="*" size="3" max="4" onChange="void document.forms[0].airportL.setAirport(this.value, true)" /></td>
</tr>
<tr id="sids" style="display:none;">
 <td class="label">Standard Departure (SID)</td>
 <td class="data"><el:combo name="sid" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="updateRoute(); plotMap()" /></td>
</tr>
<tr id="stars" style="display:none;">
 <td class="label">Terminal Arrival (STAR)</td>
 <td class="data"><el:combo name="star" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="updateRoute(); plotMap()" /></td>
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
 <td class="data"><el:combo name="routes" idx="*" size="1" className="small req" options="${emptyList}" firstEntry="No Routes Loaded" onChange="void setRoute(this)" />
 <el:box name="external" value="true" className="small" label="Search FlightAware route database" />
 <el:button ID="SearchButton" onClick="void searchRoutes()" label="SEARCH" /></td>
</tr>
<tr class="title caps">
 <td colspan="2" class="left">PLOTTED ROUTE<span id="rtDistance"></span></td>
</tr>
<tr>
 <td colspan="2" class="data"><map:div ID="googleMap" x="100%" y="580" /><div id="copyright" class="small mapTextLabel"></div>
<div id="mapStatus" class="small mapTextLabel"></div></td>
</tr>
<tr>
 <td class="label">Waypoints</td>
 <td class="data"><el:text name="route" size="80" max="320" idx="*" value="" onChange="updateRoute(); plotMap()" /></td>
</tr>
<tr>
 <td class="label">Cruising Altitude</td>
 <td class="data"><el:text name="cruiseAlt" size="5" max="5" idx="*" value="35000" onChange="updateRoute();" /></td>
</tr>
<tr>
 <td class="label top">Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="80%" height="4" onChange="updateRoute();" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="UpdateButton" onClick="void plotMap()" label="UPDATE ROUTE MAP" />
 <el:button ID="RouteSaveButton" type="submit" label="SAVE DISPATCH ROUTE" /></td>
</tr>
</el:table>
<el:text name="routeID" type="hidden" value="true" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<fmt:aptype var="useICAO" />
<script type="text/javascript">
var f = document.forms[0];
enableObject(f.routes, false);
enableElement('SearchButton', (f.airportD.selectedIndex > 0) && (f.airportA.selectedIndex > 0));

// Load the airports
var cfg = golgotha.airportLoad.config; 
cfg.doICAO = '${useICAO}';
golgotha.airportLoad.setHelpers(f.airportD);
golgotha.airportLoad.setHelpers(f.airportA);
golgotha.airportLoad.setHelpers(f.airportL);
var newCfg = cfg.clone();

<c:if test="${empty airportsD}">
newCfg.airline = getValue(f.airline); 
f.airportD.loadAirports(newCfg);</c:if>
<c:if test="${empty airportsA}">
window.setTimeout('f.airportA.loadAirports(newCfg)', 1050);</c:if>
window.setTimeout("newCfg.airline = 'all'; f.airportL.loadAirports(newCfg)", 1250);

// Create map options
var mapTypes = {mapTypeIds: golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:new google.maps.LatLng(38.88, -93.25), zoom:4, minZoom:2, maxZoom:10, scrollwheel:false, streetViewControl:false, mapTypeControlOptions:mapTypes};

// Create the map
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', function() { map.infoWindow.close(); });
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('copyright'));
map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('mapStatus'));

// Create the jetstream layer
var jsOpts = {maxZoom:8, nativeZoom:5, opacity:0.55, zIndex:golgotha.maps.z.OVERLAY};
var hjsl = new golgotha.maps.ShapeLayer(jsOpts, 'High Jet', 'wind-jet');
var ljsl = new golgotha.maps.ShapeLayer(jsOpts, 'Low Jet', 'wind-lojet');
<c:if test="${!empty tileHost}">
// Build the weather layer controls
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Radar', [gsLoader.getLatest('radar'), gsLoader.getLatest('eurorad')]));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Temperature', gsLoader.getLatest('temp')));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Wind Speed', gsLoader.getLatest('windspeed')));
</c:if>
// Build the standard weather layers
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Fronts', frLoader.getLayer()));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Clouds', new google.maps.weather.CloudLayer()));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Lo Jetstream', ljsl));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Hi Jetstream', hjsl));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerClearControl(map));

//Build departure gates marker manager
var dGates = new MarkerManager(map, {maxZoom:18});

// Update text color
google.maps.event.trigger(map, 'maptypeid_changed');
<c:if test="${!empty airportD}">plotMap();</c:if>
</script>
</body>
</html>
