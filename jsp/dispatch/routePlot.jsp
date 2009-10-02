<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> ACARS Dispatch Route Plotter</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:js name="googleMaps" />
<content:js name="routePlot" />
<content:googleAnalytics eventSupport="true" />
<map:api version="2" />
<map:vml-ie />
<content:sysdata var="imgPath" name="path.img" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<c:if test="${!empty tileHost}"><content:js name="acarsMapWX" /></c:if>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />
<script language="JavaScript" type="text/javascript">
document.imgPath = '${imgPath}';
<c:if test="${!empty tileHost}">document.tileHost = '${tileHost}';</c:if>
var routeUpdated = false;
var getInactive = false;

function validate(form)
{
// Check if we're saving an existing route
try {
var f = document.forms[0];
var routeID = parseInt(f.routeID.value);
if (!isNaN(routeID))
	alert('Updating route #' + routeID);
	
if (!checkSubmit()) return false;
if (!validateCombo(f.airline, 'Airline')) return false;
if (!validateCombo(f.airportD, 'Departure Airport')) return false;
if (!validateCombo(f.airportA, 'Arrival Airport')) return false;
if (!validateText(f.route, 3, 'Flight Route')) return false;
if (!validateText(f.cruiseAlt, 4, 'Cruising Altitude')) return false;

setSubmit();
disableButton('SearchButton');
disableButton('UpdateButton');
disableButton('RouteSaveButton');
gaEvent('Route Plotter', 'Save Route');
return true;
} catch (err) {
	alert(err.description)
	return false;
}
}
</script>
<c:if test="${!empty tileHost}"><script src="http://${tileHost}/TileServer/jserieslist.do?function=loadSeries&amp;id=wx" type="text/javascript"></script></c:if>
</head>
<content:copyright visible="false" />
<body onunload="GUnload()" onload="disableButton('RouteSaveButton')">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="aCode" name="airline.code" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="dsproutesave.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> DISPATCH ROUTE PLOTTER</td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" options="${airlines}" firstEntry="-" value="${aCode}" /></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" size="1" idx="*" options="${airports}" firstEntry="-" value="${airportD}" onChange="updateRoute(true, true); plotMap()" />
 <el:text ID="airportDCode" name="airportDCode" idx="*" size="3" max="4" onBlur="setAirport(document.forms[0].airportD, this.value); updateRoute(true); plotMap()" />
<span id="runways" style="visibility:hidden;"> departing <el:combo name="runway" idx="*" size="1" options="${emptyList}" firstEntry="-" /></span></td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" size="1" idx="*" options="${airports}" firstEntry="-" value="${airportA}" onChange="updateRoute(true); plotMap()" />
 <el:text ID="airportACode" name="airportACode" idx="*" size="3" max="4" onBlur="setAirport(document.forms[0].airportA, this.value); updateRoute(true); plotMap()" /></td>
</tr>
<tr>
 <td class="label">Alternate</td>
 <td class="data"><el:combo name="airportL" size="1" idx="*" options="${airports}" firstEntry="-" onChange="updateRoute(); plotMap()" />
 <el:text ID="airportLCode" name="airportLCode" idx="*" size="3" max="4" onBlur="setAirport(document.forms[0].airportL, this.value); plotMap()" /></td>
</tr>
<tr>
 <td class="label">Standard Departure (SID)</td>
 <td class="data"><el:combo name="sid" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="updateRoute(); plotMap()" /></td>
</tr>
<tr>
 <td class="label">Terminal Arrival (STAR)</td>
 <td class="data"><el:combo name="star" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="updateRoute(); plotMap()" /></td>
</tr>
<tr class="title caps">
 <td colspan="2" class="left">ROUTE SEARCH</td>
</tr>
<tr>
 <td class="label">Saved Routes</td>
 <td class="data"><el:combo name="routes" idx="*" size="1" className="small req" options="${emptyList}" firstEntry="No Routes Loaded" onChange="void setRoute(this)" />
 <el:box name="external" value="true" className="small" label="Search FlightAware route database" />
 <el:button ID="SearchButton" className="BUTTON" onClick="void searchRoutes()" label="SEARCH" /></td>
</tr>
<tr class="title caps">
 <td colspan="2" class="left">PLOTTED ROUTE</td>
</tr>
<tr>
 <td class="label top">Route Map</td>
 <td class="data"><map:div ID="googleMap" x="100%" y="580" /><div id="copyright" class="small"></div></td>
</tr>
<tr>
 <td class="label">Waypoints</td>
 <td class="data"><el:text name="route" size="80" max="224" idx="*" value="" onBlur="updateRoute(); plotMap()" /></td>
</tr>
<tr>
 <td class="label">Cruising Altitude</td>
 <td class="data"><el:text name="cruiseAlt" size="5" max="5" idx="*" value="35000" onBlur="updateRoute();" /></td>
</tr>
<tr>
 <td class="label top">Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="80%" height="4" onBlur="updateRoute();" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="UpdateButton" className="BUTTON" onClick="void plotMap()" label="UPDATE ROUTE MAP" />
 <el:button ID="RouteSaveButton" type="submit" className="BUTTON" label="SAVE DISPATCH ROUTE" /></td>
</tr>
</el:table>
<el:text name="routeID" type="hidden" value="true" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
var f = document.forms[0];
enableObject(f.routes, false);

// Create the map
var map = new GMap2(getElement('googleMap'), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});
<c:if test="${!empty tileHost}">
//Build the sat layer control
getTileOverlay("sat", 0.35);
map.addControl(new WXOverlayControl("Infrared", "sat", new GSize(70, 7)));
map.addControl(new WXClearControl(new GSize(142, 7)));
</c:if>
map.addControl(new GLargeMapControl3D());
map.addControl(new GMapTypeControl());
map.setCenter(new GLatLng(38.88, -93.25), 4);
<map:type map="map" type="${gMapType}" default="G_PHYSICAL_MAP" />
map.enableDoubleClickZoom();
map.enableContinuousZoom();
GEvent.addListener(map, 'maptypechanged', updateMapText);
<c:if test="${!empty tileHost}">
//Display the copyright notice
var d = new Date();
var cp = document.getElementById('copyright');
cp.innerHTML = 'Weather Data &copy; ' + (d.getYear() + 1900) + ' The Weather Channel.'
var cpos = new GControlPosition(G_ANCHOR_BOTTOM_RIGHT, new GSize(4, 16));
cpos.apply(cp);
mapTextElements.push(cp);
map.getContainer().appendChild(cp);
</c:if>
// Update text color
GEvent.trigger(map, 'maptypechanged');
<c:if test="${!empty airportD}">
// Initialize the map
plotMap();
</c:if>
</script>
</body>
</map:xhtml>
