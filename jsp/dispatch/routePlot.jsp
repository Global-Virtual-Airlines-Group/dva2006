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
<content:getCookie name="acarsMapZoomLevel" default="12" var="zoomLevel" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />
<script language="JavaScript" type="text/javascript">
<c:if test="${!empty tileHost}">document.tileHost = '${tileHost}';</c:if>
var routeUpdated = false;
var doRunways = true;

function updateRoute(airportsChanged, rwyChanged)
{
var f = document.forms[0];
routeUpdated = true;
if (rwyChanged) {
	f.runway.selectedIndex = 0;
	f.runway.options.length = 1;
}

if (airportsChanged) {
	f.routes.selectedIndex = 0;
	f.routes.options.length = 1;
	showObject(getElement('routeList'), false);
	setRoute(f.routes);
}

return true;
}

function searchRoutes()
{
var f = document.forms[0];
var aD = f.airportD.options[f.airportD.selectedIndex].value;
var aA = f.airportA.options[f.airportA.selectedIndex].value;
var rwy = f.runway.options[f.runway.selectedIndex].value;
var ext = f.external.checked;
disableButton('SearchButton');

// Generate an XMLHTTP request
var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "dsproutes.ws?airportD=" + aD + "&airportA=" + aA + "&external=" + ext + "&runway=" + rwy, true);

//Build the update handler	
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	enableElement('SearchButton', true);
	showObject(getElement('routeList'), true);
	var xdoc = xmlreq.responseXML.documentElement;

	// Load the SID/STAR list
	var cbo = document.forms[0].routes;
	var rts = xdoc.getElementsByTagName("route");
	cbo.options.length = rts.length + 1;
	for (var x = 0; x < rts.length; x++) {
		var rt = rts[x];
		var rtw = rt.getElementsByTagName("waypoints");
		var rtn = rt.getElementsByTagName("name");
		var oLabel = rtn[0].firstChild.data;
		var oValue = rtw[0].firstChild.data;
		var opt = new Option(oLabel, oValue);
		opt.routeID = rt.getAttribute("id");
		opt.SID = rt.getAttribute("sid");
		opt.STAR = rt.getAttribute("star");
		opt.altitude = rt.getAttribute("altitude");
		opt.isExternal = rt.getAttribute("external");
		cbo.options[x + 1] = opt;
		var rtc = rt.getElementsByTagName("comments");
		var c = rtc[0].firstChild;
		if (c != null)
			opt.comments = c.data;
	}

	return true;
}

xmlreq.send(null);
gaEvent('Route Plotter', 'Route Search', aD + '-' + aA, ext ? 1 : 0);
return true;
}

function setRoute(combo)
{
var f = document.forms[0];
if (combo.selectedIndex < 1) {
	f.routeID.value = '';
	f.cruiseAlt.value = '';
	f.route.value = '';
	f.comments.value = ''
	f.sid.selectedIndex = 0;
	f.star.selectedIndex = 0;
	plotMap();
	return true;
}

// Update the route
var opt = combo.options[combo.selectedIndex];
f.cruiseAlt.value = opt.altitude;
f.route.value = opt.value;
f.comments.value = opt.comments;
f.routeID.value = opt.routeID;
setCombo(f.sid, opt.SID);
setCombo(f.star, opt.STAR);
enableElement('RouteSaveButton', opt.isExternal);
plotMap();
gaEvent('Route Plotter', 'Set Route');
return true;
}

function validate(form)
{
// Check if we're saving an existing route
var routeID = parseInt(f.routeID.value);
if (!isNaN(routeID)) {
	alert('Updating route #' + routeID);
}
	
if (!checkSubmit()) return false;
if (!validateCombo(form.airline, 'Airline')) return false;
if (!validateCombo(form.airportD, 'Departure Airport')) return false;
if (!validateCombo(form.airportA, 'Arrival Airport')) return false;
if (!validateText(form.route, 3, 'Flight Route')) return false;
if (!validateText(form.cruiseAlt, 4, 'Cruising Altitude')) return false;

setSubmit();
disableButton('SearchButton');
disableButton('UpdateButton');
disableButton('RouteSaveButton');
gaEvent('Route Plotter', 'Save Route');
return true;
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
 <td class="data"><el:combo name="airportD" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="updateRoute(true, true); plotMap()" />
 <el:text ID="airportDCode" name="airportDCode" idx="*" size="3" max="4" onBlur="setAirport(document.forms[0].airportD, this.value); updateRoute(true); plotMap()" />
<span id="runways" style="visibility:hidden;"> departing <el:combo name="runway" idx="*" size="1" options="${emptyList}" firstEntry="-" /></span></td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="updateRoute(true); plotMap()" />
 <el:text ID="airportACode" name="airportACode" idx="*" size="3" max="4" onBlur="setAirport(document.forms[0].airportA, this.value); updateRoute(true); plotMap()" /></td>
</tr>
<tr>
 <td class="label">Alternate</td>
 <td class="data"><el:combo name="airportL" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="updateRoute(); plotMap()" />
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
 <td class="label" valign="top">Saved Routes</td>
 <td class="data"><span id="routeList" style="visibility:hidden;"><el:combo name="routes" idx="*" size="1" className="small req" options="${emptyList}" firstEntry="-" onChange="void setRoute(this)" /> </span>
 <el:box name="external" value="true" className="small" label="Search FlightAware route database" />
 <el:button ID="SearchButton" className="BUTTON" onClick="void searchRoutes()" label="SEARCH" /></td>
</tr>
<tr class="title caps">
 <td colspan="2" class="left">PLOTTED ROUTE</td>
</tr>
<tr>
 <td class="label" valign="top">Route Map</td>
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
 <td class="label" valign="top">Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="80%" height="4" onBlur="updateRoute();" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="UpdateButton" className="BUTTON" onClick="void plotMap()" label="UPDATE ROUTE MAP" />
 <el:button ID="RouteSaveButton" type="submit" className="BUTTON" label="SAVE DISPATCH ROUTE" /></td>
</tr>
</tr>
</el:table>
<el:text name="routeID" type="hidden" value="true" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
// Load the airports
var f = document.forms[0];
updateAirports(f.airportD, 'airline=all', ${!useIATA}, getValue(f.airportD));
updateAirports(f.airportA, 'airline=all', ${!useIATA}, getValue(f.airportA));
updateAirports(f.airportL, 'airline=all', ${!useIATA}, getValue(f.airportL));

// Create the map
var map = new GMap2(getElement('googleMap'), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});
<c:if test="${!empty tileHost}">
//Build the sat layer control
getTileOverlay("sat", 0.35);
map.addControl(new WXOverlayControl("Infrared", "sat", new GSize(70, 7)));
map.addControl(new WXClearControl(new GSize(142, 7)));
</c:if>
map.addControl(new GLargeMapControl());
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
</script>
</body>
</map:xhtml>
