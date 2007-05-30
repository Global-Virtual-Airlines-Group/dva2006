<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> North Atlantic Track Plotter</title>
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
tracks['W'] = new Array();
tracks['E'] = new Array();
tracks['C'] = new Array();
points['W'] = new Array();
points['E'] = new Array();
points['C'] = new Array();

// Reset checkboxes
var f = document.forms[0];
for (var x = 0; x < f.showTracks.length; x++)
	f.showTracks[x].checked = true;

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
xmlreq.open("GET", "natinfo.ws?date=" + dt.text, true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	map.clearOverlays();
	resetTracks();

	// Get the XML document
	var xdoc = xmlreq.responseXML.documentElement;
	var xtracks = xdoc.getElementsByTagName("track");
	for (var i = 0; i < xtracks.length; i++) {
		var trackPos = new Array();
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
		}

		// Draw the route
		var trackLine = new GPolyline(trackPos, track.getAttribute("color"), 2, 0.7);
		map.addOverlay(trackLine);
		
		// Save the route/points
		tracks[trackType].push(trackLine);
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
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="natplot.do" method="get" validate="return false">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> NORTH ATLANTIC ROUTE PLOTTER<span id="isLoading" /></td>
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
 <td class="data"><map:legend color="white" legend="Eastbound" />  <map:legend color="orange" legend="Westbound" />
 <map:legend color="blue" legend="Concorde" /></td>
</tr>
<tr>
 <td class="label">Display Tracks</td>
 <td class="data"><el:check name="showTracks" idx="*" options="${trackTypes}" checked="${trackTypes}" width="100" cols="3" onChange="void updateTracks(this)" /></td>
</tr>
<tr>
 <td class="label" valign="top">Route Map</td>
 <td class="data"><map:div ID="googleMap" x="100%" y="600" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
// Create the map
var map = new GMap2(getElement('googleMap'), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP]});
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());
map.setCenter(new GLatLng(52.0, -35.0), 4);
map.setMapType(${gMapType == 'map' ? 'G_NORMAL_MAP' : 'G_SATELLITE_MAP'});
map.enableDoubleClickZoom();
map.enableContinuousZoom();

// Create the tracks/waypoints
var tracks = new Array();
var points = new Array();
resetTracks();
</script>
</body>
</map:xhtml>
