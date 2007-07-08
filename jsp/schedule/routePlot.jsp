<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> Route Plotter</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:js name="googleMaps" />
<map:api version="2" />
<map:vml-ie />
<content:sysdata var="imgPath" name="path.img" />
<content:getCookie name="acarsMapZoomLevel" default="12" var="zoomLevel" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />
<script language="JavaScript" type="text/javascript">
function plotMap()
{
// Set map as loading
var isLoading = getElement("isLoading");
if (isLoading)
	isLoading.innerHTML = " - LOADING...";
	
// Build the POST data
var f = document.forms[0];
var params = new Array();
if (f.airportD.selectedIndex > 0) {
	params.push('airportD=' + f.airportD.options[f.airportD.selectedIndex].value);
	f.airportDCode.value = f.airportD.options[f.airportD.selectedIndex].value;
}
	
if (f.airportA.selectedIndex > 0) {
	params.push('airportA=' + f.airportD.options[f.airportA.selectedIndex].value);
	f.airportACode.value = f.airportA.options[f.airportA.selectedIndex].value;
}
	
if (f.sid.selectedIndex > 0)
	params.push('sid=' + f.sid.options[f.sid.selectedIndex].value);
	
if (f.star.selectedIndex > 0)
	params.push('star=' + f.star.options[f.star.selectedIndex].value);
	
if (f.route.value.length > 0)
	params.push('route=' + f.route.value);

// Generate an XMLHTTP request
var xmlreq = GXmlHttp.create();
xmlreq.open("POST", "routeplot.ws", true);
xmlreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

// Build the update handler	
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	map.clearOverlays();
	
	// Draw the markers and load the codes
	var positions = new Array();
	var codes = new Array();
	var xdoc = xmlreq.responseXML.documentElement;
	var waypoints = xdoc.getElementsByTagName("pos");
	for (var i = 0; i < waypoints.length; i++) {
		var wp = waypoints[i];
		var label = wp.firstChild;
		var p = new GLatLng(parseFloat(wp.getAttribute("lat")), parseFloat(wp.getAttribute("lng")));
		positions.push(p);
		codes.push(wp.getAttribute("code"));
		map.addOverlay(googleMarker('${imgPath}', wp.getAttribute('color'), p, label.data));
	} // for
	
	// Draw the route
	map.addOverlay(new GPolyline(positions, '#4080AF', 2, 0.8));
	
	// Save the codes
	var f = document.forms[0];
	f.routeCodes.value = codes.join(' ');

	// Get the midpoint and center the map
	var mps = xdoc.getElementsByTagName("midpoint");
	var mpp = mps[0];
	if (mpp) {
		var mp = new GLatLng(parseFloat(mpp.getAttribute("lat")), parseFloat(mpp.getAttribute("lng")));
		map.setCenter(mp, getDefaultZoom(parseInt(mpp.getAttribute("distance"))));
	}

	// Load the SID/STAR list
	var sids = xdoc.getElementsByTagName("sid");
	var stars = xdoc.getElementsByTagName("star");
	updateRoutes(f.sid, sids);
	updateRoutes(f.star, stars);
	
	// Focus on the map
	if (isLoading)
		isLoading.innerHTML = '';

	return true;
}

xmlreq.send(params.join('&'));
return true;
}

function updateRoutes(combo, elements)
{
// Save the old value
var oldCode = combo.options[combo.selectedIndex].value;

// Update the combobox choices
combo.options.length = elements.length + 1;
combo.options[0] = new Option("-", "");
for (var i = 0; i < elements.length; i++) {
	var e = elements[i];
	var name = e.getAttribute("name") + "." + e.getAttribute("transition");
	var rCode = e.getAttribute("code");
	combo.options[i+1] = new Option(name, rCode);
	if (oldCode == rCode)
		combo.selectedIndex = (i+1);
} // for

return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="routeplot.do" method="get" validate="return false">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> FLIGHT ROUTE PLOTTER<span id="isLoading" /></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="void plotMap()" />
 <el:text name="airportDCode" idx="*" size="3" max="4" onBlur="setAirport(document.forms[0].airportD, this.value); plotMap()" /></td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="void plotMap()" />
 <el:text name="airportACode" idx="*" size="3" max="4" onBlur="setAirport(document.forms[0].airportA, this.value); plotMap()" /></td>
</tr>
<tr>
 <td class="label">Standard Departure (SID)</td>
 <td class="data"><el:combo name="sid" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="void plotMap()" /></td>
</tr>
<tr>
 <td class="label">Terminal Arrival (STAR)</td>
 <td class="data"><el:combo name="star" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="void plotMap()" /></td>
</tr>
<tr>
 <td class="label">Waypoints</td>
 <td class="data"><el:text name="route" size="80" max="192" idx="*" value="" /></td>
</tr>
<tr>
 <td class="label" valign="top">Route Map</td>
 <td class="data"><map:div ID="googleMap" x="100%" y="580" /></td>
</tr>
<tr>
 <td class="label">Flight Route</td>
 <td class="data"><el:text name="routeCodes" size="144" max="320" readOnly="true" value="" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="UpdateButton" className="BUTTON" onClick="void plotMap()" label="UPDATE ROUTE MAP" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
// Load the airports
var f = document.forms[0];
updateAirports(f.airportD, 'airline=all', ${!useIATA}, getValue(f.airportD));
updateAirports(f.airportA, 'airline=all', ${!useIATA}, getValue(f.airportD));

// Create the map
var map = new GMap2(getElement('googleMap'), G_DEFAULT_MAP_TYPES);
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());
map.setCenter(new GLatLng(38.88, -93.25), 4);
map.setMapType(${gMapType == 'map' ? 'G_MAP_TYPE' : 'G_SATELLITE_TYPE'});
</script>
<content:googleAnalytics />
</body>
</map:xhtml>
