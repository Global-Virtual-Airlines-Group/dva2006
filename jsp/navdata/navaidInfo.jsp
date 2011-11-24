<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> Navigation Database</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<map:api version="3" />
<content:googleAnalytics eventSupport="true" />
<content:js name="markermanager" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.navaidCode, 'Navigation Aid Code')) return false;

setSubmit();
disableButton('SearchButton');
return true;
}

function zoomTo(combo)
{
var idx = combo.selectedIndex;
if ((idx < 0) || (idx >= navaids.length))
	return false;

// Pan the map
var mrk = navaids[idx];
map.panTo(mrk.getPosition());
loadWaypoints();
google.maps.event.trigger(mrk, 'click');
return true;
}

function loadWaypoints()
{
// Get the lat/long
var lat = map.getCenter().lat();
var lng = map.getCenter().lng();
var range = (map.getBounds().getNorthEast().lat() - map.getBounds().getSouthWest().lat()) * 69.16;
sMarkers.clearMarkers();

// Check if we don't select
var f = document.forms[0];
if (!f.showAll.checked || (map.getZoom() < 5))
	return true;

// Status message
var isLoading = document.getElementById('isLoading');
isLoading.innerHTML = ' - LOADING...';

//Build the XML Requester
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'navaidsearch.ws?airports=true&lat=' + lat + '&lng=' + lng + '&range=' + Math.round(range), true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;

	// Parse the XML
	var xml = xmlreq.responseXML;
	var xe = xml.documentElement;
	var wps = xe.getElementsByTagName('waypoint');
	for (var i = 0; i < wps.length; i++) {
		var wp = wps[i];
		var code = wp.getAttribute('code');
		if (code != '${param.navaidCode}') {
			var p = new google.maps.LatLng(parseFloat(wp.getAttribute('lat')), parseFloat(wp.getAttribute('lng')));
			var mrk;
			if (wp.getAttribute('pal'))
				mrk = googleIconMarker(wp.getAttribute('pal'), wp.getAttribute('icon'), p, wp.firstChild.data);
			else
				mrk = googleMarker(wp.getAttribute('color'), p, wp.firstChild.data);

			mrk.minZoom = 4;
			var type = wp.getAttribute('type');
			if (type == 'NDB')
				mrk.minZoom = 5;
			else if (type == 'Airport')
				mrk.minZoom = 9;
			else if (type == 'Intersection')
				mrk.minZoom = 8;

			mrk.code = code;
			sMarkers.addMarker(mrk, mrk.minZoom);
		}
	}

	isLoading.innerHTML = '';
	return true;
} // function

xmlreq.send(null);
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="navsearch.do" method="post" validate="return validate(this)">
<c:if test="${doSearch}">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">NAVIGATION AID SEARCH<span id="isLoading" /></td>
</tr>
<c:if test="${!empty results}">
<tr>
 <td class="label">Code</td>
 <td class="data pri bld"><el:combo name="navaid" idx="*" options="${options}" onChange="void zoomTo(this)" />
 <el:box name="showAll" idx="*" value="true" checked="${showSurroundingNavaids}" label="Show Surrounding Navigation Aids" onChange="void loadWaypoints()" /></td>
</tr>
<tr>
 <td class="label top">Map</td>
 <td class="data"><map:div ID="googleMap" x="100%" y="425" /></td>
</tr>
</c:if>
<c:if test="${empty results}">
<tr>
 <td class="error bld mid" colspan="2">The Navigation Aid ${param.navaidCode} was not found in the 
<content:airline /> Navigation Data database.</td>
</tr>
</c:if>
</el:table>
</c:if>

<!-- Search Bar -->
<el:table className="form">
<tr class="title caps">
 <td colspan="2">NEW NAVIGATION AID SEARCH</td>
</tr>
<tr>
 <td class="label">Navigation Aid Code</td>
 <td class="data"><el:text name="navaidCode" className="pri bld req" idx="*" size="6" max="5" value="${param.navaidCode}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SearchButton" type="submit" label="NEW NAVIGATION DATA SEARCH" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<c:if test="${!empty results}">
<script type="text/javascript">
<map:point var="mapC" point="${mapCenter}" />

// Create map options
var mapTypes = {mapTypeIds: golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:mapC, minZoom:3, zoom: getDefaultZoom(110), scrollwheel:false, streetViewControl:false, mapTypeControlOptions:mapTypes};

// Build the map
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content: ''});
google.maps.event.addListener(map, 'click', function() { map.infoWindow.close(); });

//Build the navaid list
<map:markers var="navaids" items="${results}" />
addMarkers(map, 'navaids');

// Surrounding navads
var sMarkers = new MarkerManager(map, {borderPadding:24});
document.forms[0].navaid.selectedIndex = 0;
google.maps.event.addListenerOnce(map, 'tilesloaded', function() { zoomTo(document.forms[0].navaid); });
</script></c:if>
</body>
</map:xhtml>
