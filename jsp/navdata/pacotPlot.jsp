<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> Pacific Track Plotter</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="googleMaps" />
<content:googleAnalytics eventSupport="true" />
<map:api version="2" />
<map:vml-ie />
<content:sysdata var="imgPath" name="path.img" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<c:if test="${!empty tileHost}"><content:js name="acarsMapWX" /></c:if>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />
<script language="JavaScript" type="text/javascript">
<c:if test="${!empty tileHost}">document.tileHost = '${tileHost}';</c:if>
function showTrackInfo(marker)
{
var label = getElement("trackLabel");
var data = getElement("trackData");
if ((!label) || (!data))
	return false;
	
label.innerHTML = "Track " + marker.title;
data.innerHTML = marker.trackPoints;
return true;
}

function resetTracks()
{
// Initialize map data arrays
tracks['W'] = [];
tracks['E'] = [];
allTracks = [];
points['W'] = [];
points['E'] = [];
allPoints = [];

// Reset checkboxes
var f = document.forms[0];
for (var x = 0; x < f.showTracks.length; x++)
	f.showTracks[x].checked = true;

// Reset track data label
var label = getElement("trackLabel");
var data = getElement("trackData");
if ((!label) || (!data))
	return false;

label.innerHTML = 'Track Data';
data.innerHTML = 'N/A';
return true;
}

function updateTracks(checkbox)
{
var xtracks = tracks[checkbox.value];
var trackPoints = points[checkbox.value];

// Toggle the points
for (var x = 0; x < trackPoints.length; x++) {
	if (checkbox.checked)
		trackPoints[x].show();
	else
		trackPoints[x].hide();
}

// Toggle the tracks
for (var x = 0; x < xtracks.length; x++) {
	if (checkbox.checked)
		map.addOverlay(xtracks[x]);
	else
		map.removeOverlay(xtracks[x]);
}

return true;
}

function loadTracks()
{
// Check the combobox
var f = document.forms[0];
var dt = f.date.options[f.date.selectedIndex];
if (f.date.selectedIndex == 0)
	return;

// Set map as loading
var isLoading = getElement("isLoading");
if (isLoading)
	isLoading.innerHTML = " - LOADING...";

// Generate an XMLHTTP request
var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "otrackinfo.ws?type=PACOT&date=" + dt.text, true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	removeMarkers(map, 'allPoints');
	removeMarkers(map, 'allTracks');
	resetTracks();

	// Get the XML document
	var xdoc = xmlreq.responseXML.documentElement;
	var xtracks = xdoc.getElementsByTagName("track");
	for (var i = 0; i < xtracks.length; i++) {
		var trackPos = [];
		var track = xtracks[i];
		var trackType = track.getAttribute("type");
		var waypoints = track.getElementsByTagName("waypoint");
		for (var j = 0; j < waypoints.length; j++) {
			var wp = waypoints[j];
			var label = wp.firstChild;
			var p = new GLatLng(parseFloat(wp.getAttribute("lat")), parseFloat(wp.getAttribute("lng")));
			trackPos.push(p);

			// Create the map marker
			var mrk = googleMarker('${imgPath}', wp.getAttribute('color'), p, label.data);
			mrk.title = track.getAttribute("code");
			mrk.trackPoints = track.getAttribute("track");
			mrk.showTrack = showTrackInfo;
			GEvent.addListener(mrk, 'click', function() { mrk.showTrack(this); });
			map.addOverlay(mrk);
			points[trackType].push(mrk);
			allPoints.push(mrk);
		}

		// Draw the route
		var trackLine = new GPolyline(trackPos, track.getAttribute("color"), 2, 0.7, { geodesic:true });
		map.addOverlay(trackLine);
		
		// Save the route/points
		tracks[trackType].push(trackLine);
		allTracks.push(trackLine);
	}

	// Focus on the map
	if (isLoading)
		isLoading.innerHTML = '';

	return true;
}

xmlreq.send(null);
return true;
}
</script>
<c:if test="${!empty tileHost}"><script src="http://${tileHost}/TileServer/jserieslist.do?function=loadSeries&amp;id=wx" type="text/javascript"></script></c:if>
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="pacotplot.do" method="get" validate="return false">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> PACIFIC ROUTE PLOTTER<span id="isLoading" /></td>
</tr>
<tr>
 <td class="label">Date</td>
 <td class="data"><el:combo name="date" idx="*" firstEntry="-" options="${dates}" value="${param.date}" onChange="void loadTracks()" /></td>
</tr>
<tr>
 <td class="label"><span id="trackLabel">Track Data</span></td>
 <td class="data"><span id="trackData">N/A</span></td>
</tr>
<tr>
 <td class="label">Map Legend</td>
 <td class="data"><map:legend color="white" legend="Eastbound" />  <map:legend color="orange" legend="Westbound" /></td>
</tr>
<tr>
 <td class="label">Display Tracks</td>
 <td class="data"><el:check name="showTracks" idx="*" options="${trackTypes}" checked="${trackTypes}" width="100" cols="3" onChange="void updateTracks(this)" /></td>
</tr>
<tr>
 <td class="label top">Route Map</td>
 <td class="data"><map:div ID="googleMap" x="100%" y="550" /><div id="copyright" class="small"></div></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
// Create the map
var map = new GMap2(getElement('googleMap'), {mapTypes:[G_SATELLITE_MAP, G_PHYSICAL_MAP]});
<c:if test="${!empty tileHost}">
// Build the sat layer control
getTileOverlay("sat", 0.35);
map.addControl(new WXOverlayControl("Infrared", "sat", new GSize(70, 7)));
map.addControl(new WXClearControl(new GSize(142, 7)));
</c:if>
// Add map controls
map.addControl(new GLargeMapControl3D());
map.addControl(new GMapTypeControl());
map.setCenter(new GLatLng(42.0, -165.0), 4);
map.setMapType(G_SATELLITE_MAP);
map.enableDoubleClickZoom();
map.enableContinuousZoom();
GEvent.addListener(map, 'maptypechanged', updateMapText);

// Create the tracks/waypoints
var tracks = [];
var points = [];
resetTracks();
<c:if test="${!empty tileHost}">
// Display the copyright notice
var d = new Date();
var cp = document.getElementById('copyright');
cp.innerHTML = 'Weather Data &copy; ' + (d.getYear() + 1900) + ' The Weather Channel.'
var cpos = new GControlPosition(G_ANCHOR_BOTTOM_RIGHT, new GSize(4, 16));
cpos.apply(cp);
mapTextElements.push(cp);
map.getContainer().appendChild(cp);

//Update text color
GEvent.trigger(map, 'maptypechanged');
</c:if></script>
</body>
</map:xhtml>
