<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> SID/STAR Plotter</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="googleMaps" />
<map:api version="2" />
<map:vml-ie />
<content:sysdata var="imgPath" name="path.img" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />
<script language="JavaScript" type="text/javascript">
function getRoutes(combo)
{
// Build the XML Requester
var d = new Date();
var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "apsidstar.ws?time=" + d.getTime(), true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	removeMarkers(map, 'routeWaypoints');
	removeMarkers(map, 'apMarker');
	removeMarkers(map, 'routeTrack');
	var cbo = document.forms[0].route;
	cbo.selectedIndex = 0;
	cbo.options.length = 1;
	routes.length = 0;

	// Parse the XML
	var xml = xmlreq.responseXML;
	if (!xml) return false;
	var xe = xml.documentElement;

	// Get the Airport and center on it
	var ap = xe.getElementsByTagName("airport")[0];
	var apLoc = new GLatLng(parseFloat(ap.getAttribute('lat')), parseFloat(ap.getAttribute('lng')));
	apMarker = googleMarker(document.imgPath, ap.getAttribute("color"), apLoc, ap.firstChild.data);
	map.setCenter(apLoc, map.getZoom());
	map.addOverlay(apMarker);

	// Get the routes
	var rt = xe.getElementsByTagName("route");
	for (var i = 0; i < rt.length; i++) {
		var tr = rt[i];
		var id = tr.getAttribute('id');
		var label = id + ' (' + tr.getAttribute('type') + ')';
		cbo.options.push(new Option(label, id));

		// Add waypoints
		tr.waypoints = new Array();
		var wps = rt.getElementsByTagName('waypoint');
		for (var j = 0; j < wps.length; j++) {
			var wp = wps[j];
			var p = new GLatLng(parseFloat(wp.getAttribute("lat")), parseFloat(wp.getAttribute("lng")));
			var mrk = googleMarker(document.imgPath, wp.getAttribute("color"), p, null);
			mrk.infoShow = clickIcon;
			mrk.infoLabel = wp.firstChild.data;
			
			// Set the the click handler
			GEvent.bind(mrk, 'click', mrk, mrk.infoShow);
			tr.waypoints.push(mrk);
		}
		
		// Add the route
		routes.push(tr);
	}

	return true;
} // function

return true;
}

function plotRoute(combo)
{
var id = combo.options[combo.selectedIndex].value;
var tr = routes[id];
if (tr == null) {
	alert('Unknown Terminal Route - ' + id);
	return;
}

// Plot the markers
removeOverlays(map, 'routeTrack');
routeWaypoints.length = 0;
var mrks = tr.waypoints;
var track = new Array();
for (var i = 0; i < mrks.length; i++) {
	var mrk = mrks[i];
	track.push(mrk.getLatLng());
	routeWaypoints.push(mrk);
}

// Display the route and the markers
routeTrack = new GPolyline(track, map.getCurrentMapType().getTextColor(), 2.0, 0.8);
map.addOverlay(routeTrack);
addOverlays(map, 'routeWaypoints');
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
<el:form action="sidstarplot.do" method="get" validate="return false">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> TERMINAL ROUTE PLOTTER</td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airport" size="1" idx="*" options="${airports}" firstEntry="-" onChange="void getRoutes(this)" /></td>
</tr>
<tr>
 <td class="label">Terminal Route</td>
 <td class="data"><el:combo name="route" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="void plotRoute(this)" /></td>
</tr>
<tr>
 <td class="label">Route Map</td>
 <td class="data"><map:div ID="googleMap" x="100%" y="550" /><div id="copyright" class="bld"></div></td>
</tr>
<tr>
 <td class="title caps" colspan="2">NEW TERMINAL ROUTE</td>
</tr>
<tr>
 <td class="label">Route Name</td>
 <td class="data"><el:text name="name" size="12" max="16" idx="*" value="" className="pri bld req" /></td>
</tr>
<tr>
 <td class="label">Transition</td>
 <td class="data"><el:text name="transition" size="5" max="5" idx="*" value="" className="bld req" /></td>
</tr>
<tr>
 <td class="label">Runway</td>
 <td class="data"><el:text name="runway" size="8" max="8" idx="*" value="" className="req" /></td>
</tr>
<tr>
 <td class="label">Find Navigation Aid</td>
 <td class="data"><el:text name="navaidCode" size="4" max="6" idx="*" value="" />
 <el:button ID="FindButton" className="BUTTON" onClick="void findNavaid(document.forms[0].navaidCode.value)" label="FIND" /></td>
</tr>
<tr>
 <td colspan="2" class="mid"><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE ROUTE" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
var map = new GMap2(getElement("googleMap"), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});

// Add routes
var apMarker;
var routeTrack;
var routes = new Array();
var routeWaypoints = new Array();

// Add map controls
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());
map.setCenter(mapC, 7);
map.enableDoubleClickZoom();
map.enableContinuousZoom();
<map:type map="map" type="${gMapType}" default="G_PHYSICAL_MAP" />
GEvent.addListener(map, 'maptypechanged', updateMapText);
GEvent.trigger(map, 'maptypechanged');
</script>
<content:googleAnalytics />
</body>
</map:xhtml>
