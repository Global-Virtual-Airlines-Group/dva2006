<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> Route Plotter</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="airportRefresh" />
<map:api version="3" />
<content:js name="routePlot" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<c:if test="${!empty tileHost}"><content:js name="acarsMapWX" /></c:if>
<content:getCookie name="acarsMapZoomLevel" default="12" var="zoomLevel" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />
<script type="text/javascript">
var routeUpdated = false;
var getInactive = false;

function validate(form)
{
if (!validateCombo(form.airportD, 'Departure Airport')) return false;
if (!validateCombo(form.airportA, 'Arrival Airport')) return false;
if (!validateText(form.route, 3, 'Flight Route')) return false;
return true;
}
</script>
<map:wxList layers="radar,eurorad,sat,windspeed,temp" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="aCode" name="airline.code" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="routeplan.ws" method="post" target="_new" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> FLIGHT ROUTE PLOTTER</td>
</tr>
<c:if test="${access.canCreate}">
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" options="${airlines}" firstEntry="-" value="${aCode}" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="void updateRoute(true, true)" />
 <el:text ID="airportDCode" name="airportDCode" idx="*" size="3" max="4" onChange="setAirport(document.forms[0].airportD, this.value); updateRoute(true)" />
<span id="runways" style="visibility:hidden;"> departing <el:combo name="runway" idx="*" size="1" options="${emptyList}" firstEntry="-" onChange="void updateRoute(true, false)" /></span></td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="void updateRoute(true)" />
 <el:text ID="airportACode" name="airportACode" idx="*" size="3" max="4" onChange="setAirport(document.forms[0].airportA, this.value); updateRoute(true)" /></td>
</tr>
<tr>
 <td class="label">Alternate</td>
 <td class="data"><el:combo name="airportL" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="updateRoute(); plotMap()" />
 <el:text ID="airportLCode" name="airportLCode" idx="*" size="3" max="4" onChange="setAirport(document.forms[0].airportL, this.value); plotMap()" /></td>
</tr>
<tr id="sids" style="display:none;">
 <td class="label">Standard Departure (SID)</td>
 <td class="data"><el:combo name="sid" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="void plotMap()" /></td>
</tr>
<tr id="stars" style="display:none;">
 <td class="label">Terminal Arrival (STAR)</td>
 <td class="data"><el:combo name="star" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="void plotMap()" /></td>
</tr>
<tr>
 <td class="label">Waypoints</td>
 <td class="data"><el:text name="route" size="80" max="320" idx="*" value="" onChange="void plotMap()" /></td>
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
<el:button ID="SearchButton" onClick="void searchRoutes()" label="SEARCH" />
<content:filter roles="Route,Dispatch,Operations"><el:box name="forceFAReload" value="true" checked="false" label="Force FlightAware refresh" /></content:filter></td>
</tr>
<tr>
 <td class="label top">Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="80%" height="2" readOnly="true" /></td>
</tr>
<tr class="title caps">
 <td colspan="2" class="left">PLOTTED ROUTE<span id="rtDistance" /></td>
</tr>
<tr>
 <td colspan="2" class="data"><map:div ID="googleMap" x="100%" y="580" /></td>
</tr>
<tr>
 <td class="label">Flight Route</td>
 <td class="data"><el:text name="routeCodes" size="144" max="480" readOnly="true" value="" /></td>
</tr>
<tr>
 <td class="label">Simulator Version</td>
 <td class="data"><el:check type="radio" name="simVersion" idx="*" options="${simVersions}" /></td>
</tr>
<tr>
 <td class="label">Cruising Altitude</td>
 <td class="data"><el:text name="cruiseAlt" size="5" max="5" idx="*" value="35000" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="saveDraft" label="Save as Draft Flight Report" value="true" checked="true" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="UpdateButton" onClick="void plotMap()" label="UPDATE ROUTE MAP" />
 <el:button ID="SaveButton" type="submit" label="DOWNLOAD FLIGHT PLAN" />
</td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<fmt:aptype var="useICAO" />
<script type="text/javascript">
var f = document.forms[0];
enableObject(f.routes, false);
enableElement('SearchButton', false);

// Load the airports
updateAirports(f.airportD, 'airline=all', ${useICAO}, getValue(f.airportD));
window.setTimeout("updateAirports(f.airportA, 'airline=all', ${useICAO}, getValue(f.airportA))", 1250);
window.setTimeout("updateAirports(f.airportL, 'airline=all', ${useICAO}, getValue(f.airportL))", 1750);

// Create map options
var mapTypes = {mapTypeIds: golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:new google.maps.LatLng(38.88, -93.25), zoom:4, scrollwheel:false, streetViewControl:false, mapTypeControlOptions: mapTypes};

// Create the map
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
map.infoWindow = new google.maps.InfoWindow({content: ''});
google.maps.event.addListener(map, 'click', function() { map.infoWindow.close(); });
<c:if test="${!empty tileHost}">
// Build the weather layer controls
getTileOverlay('radar', 0.45);
getTileOverlay('eurorad', 0.45);
getTileOverlay('sat', 0.35);
getTileOverlay('temp', 0.25);
getTileOverlay('windspeed', 0.35);
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Radar', ['radar', 'eurorad']));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Infrared', 'sat'));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Temperature', 'temp'));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Wind Speed', 'windspeed'));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXClearControl());
</c:if>
<map:type map="map" type="${gMapType}" default="TERRAIN" />
google.maps.event.addListener(map, 'maptypeid_changed', updateMapText);
google.maps.event.trigger(map, 'maptypeid_changed');
</script>
</body>
</map:xhtml>
