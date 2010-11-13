<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> Australian Track Plotter</title>
<content:browserFilter ie8="true"><meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" /></content:browserFilter>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<map:api version="3" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<content:sysdata var="multiHost" name="weather.multiHost" />
<c:if test="${!empty tileHost}"><content:js name="acarsMapWX" /></c:if>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />
<script type="text/javascript">
<c:if test="${!empty tileHost}">document.tileHost = '${tileHost}';
document.multiHost = ${multiHost};
</c:if>
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
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'otrackinfo.ws?type=AUSOT&date=' + dt.text, true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	removeMarkers('allPoints');
	removeMarkers('allTracks');
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
			var p = new google.maps.LatLng(parseFloat(wp.getAttribute("lat")), parseFloat(wp.getAttribute("lng")));
			trackPos.push(p);

			// Create the map marker
			var mrk = googleMarker(wp.getAttribute('color'), p, label.data);
			mrk.title = track.getAttribute("code");
			mrk.trackPoints = track.getAttribute("track");
			mrk.showTrack = showTrackInfo;
			google.maps.event.addListener(mrk, 'click', function() { mrk.showTrack(this); });
			mrk.setMap(map);
			points[trackType].push(mrk);
			allPoints.push(mrk);
		}

		// Draw the route
		var trackLine = new google.maps.Polyline({path:trackPos, strokeColor:track.getAttribute("color"), strokeWeight:2, strokeOpacity:0.7, geodesic:true});
		trackLine.setMap(map);
		
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
<map:wxList layers="sat" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="pacotplot.do" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> AUSTRALIAN ROUTE PLOTTER<span id="isLoading" /></td>
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
 <td class="data"><map:div ID="googleMap" x="100%" y="550" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script type="text/javascript">
//Create map options
var mapTypes = {mapTypeIds: [google.maps.MapTypeId.SATELLITE, google.maps.MapTypeId.TERRAIN]};
var mapOpts = {center:new google.maps.LatLng(-26.0, 133.0), zoom:4, scrollwheel:false, streetViewControl:false, mapTypeControlOptions: mapTypes};

// Create the map
var map = new google.maps.Map(getElement('googleMap'), mapOpts);
map.setMapTypeId(google.maps.MapTypeId.SATELLITE);
<c:if test="${!empty tileHost}">
// Build the sat layer control
getTileOverlay('sat', 0.35);
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Infrared', 'sat'));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXClearControl());
</c:if>
map.infoWindow = new google.maps.InfoWindow({content: ''});
google.maps.event.addListener(map, 'click', function() { map.infoWindow.close(); });
google.maps.event.addListener(map, 'maptypeid_changed', updateMapText);

// Create the tracks/waypoints
var tracks = [];
var points = [];
resetTracks();

// Update text color
google.maps.event.trigger(map, 'maptypeid_changed');
</script>
</body>
</map:xhtml>
