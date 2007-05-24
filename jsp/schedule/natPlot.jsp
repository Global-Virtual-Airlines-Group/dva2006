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
<content:getCookie name="acarsMapZoomLevel" default="12" var="zoomLevel" />
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

function loadTracks()
{
// Set map as loading
var isLoading = getElement("isLoading");
if (isLoading)
	isLoading.innerHTML = " - LOADING...";

// Generate an XMLHTTP request
var f = document.forms[0];
var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "natinfo.ws?date=" + f.date.value, true);

// Build the update handler	
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	map.clearOverlays();

	// Get the XML document
	var xdoc = xmlreq.responseXML.documentElement;
	var tracks = xdoc.getElementsByTagName("track");
	for (var i = 0; i < tracks.length; i++) {
		var trackPos = new Array();
		var track = tracks[i];
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
		}

		// Draw the route
		var trackLine = new GPolyline(trackPos, track.getAttribute("color"), 2, 0.7);
		map.addOverlay(trackLine);
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
var map = new GMap2(getElement('googleMap'), [G_MAP_TYPE,G_SATELLITE_TYPE]);
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());
map.setCenter(new GLatLng(52.0, -35.0), 4);
map.setMapType(${gMapType == 'map' ? 'G_MAP_TYPE' : 'G_SATELLITE_TYPE'});
map.enableDoubleClickZoom();
map.enableContinuousZoom();
</script>
</body>
</map:xhtml>
