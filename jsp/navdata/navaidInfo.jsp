<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Navigation Database</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:json />
<map:api version="3" />
<content:googleAnalytics eventSupport="true" />
<content:js name="markermanager" />
<content:js name="markerWithLabel" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.navaidCode, l:2, t:'Navigation Aid Code'});
	golgotha.form.submit(f);
	return true;
};

golgotha.local.zoomTo = function(combo) {
	var idx = combo.selectedIndex;
	if ((idx < 0) || (idx >= golgotha.local.navaids.length)) return false;

	// Pan the map
	var mrk = golgotha.local.navaids[idx];
	map.panTo(mrk.getPosition());
	golgotha.local.mapC = mrk.getPosition();
	golgotha.local.loadWaypoints();
	google.maps.event.trigger(mrk, 'click');
	return true;
};

golgotha.local.loadWaypoints = function()
{
// Get the lat/long
var lat = golgotha.local.mapC.lat();
var lng = golgotha.local.mapC.lng();
var range = golgotha.maps.degreesToMiles(map.getBounds().getNorthEast().lng() - map.getBounds().getSouthWest().lng());
golgotha.local.sMarkers.clearMarkers();

// Check if we don't select
var f = document.forms[0];
if (!f.showAll.checked) return true;

// Status message
golgotha.form.submit();

// Build the XML Requester
var xmlreq = new XMLHttpRequest();
xmlreq.open('get', 'navaidsearch.ws?airports=true&lat=' + lat + '&lng=' + lng + '&range=' + Math.min(1000, Math.round(range)), true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var js = JSON.parse(xmlreq.responseText);
	js.items.forEach(function(wp) {
		if (wp.code == '${param.navaidCode}') return;
		var mrk;
		if (wp.pal)
			mrk = new golgotha.maps.IconMarker({pal:wp.pal, icon:wp.icon, info:wp.info}, wp.ll);
		else
			mrk = new golgotha.maps.Marker({color:wp.color, info:wp.info, label:wp.code}, wp.ll);

		mrk.minZoom = 6; mrk.code = wp.code;
		if (wp.type == 'Airport')
			mrk.minZoom = 7;
		else if (wp.type == 'Intersection')
			mrk.minZoom = 8;

		golgotha.local.sMarkers.addMarker(mrk, mrk.minZoom);
	});

	golgotha.form.clear();
	return true;
};

xmlreq.send(null);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="navsearch.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<c:if test="${doSearch}">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">NAVIGATION AID SEARCH<span id="isLoading"></span></td>
</tr>
<c:if test="${!empty results}">
<tr>
 <td class="label">Code</td>
 <td class="data pri bld"><el:combo name="navaid" idx="*" options="${options}" onChange="void golgotha.local.zoomTo(this)" />
 <el:box name="showAll" idx="*" value="true" checked="${showSurroundingNavaids}" label="Show Surrounding Navigation Aids" onChange="void golgotha.local.loadWaypoints()" /></td>
</tr>
<tr>
 <td class="label top">Map</td>
 <td class="data"><map:div ID="googleMap" height="450" /></td>
</tr>
</c:if>
<c:if test="${empty results}">
<tr>
 <td class="error bld mid" colspan="2">The Navigation Aid ${param.navaidCode} was not found in the <content:airline /> Navigation Data database.</td>
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
<div id="zoomLevel" class="mapTextLabel"></div>
<c:if test="${!empty results}">
<script id="mapInit" async>
<map:point var="golgotha.local.mapC" point="${mapCenter}" />

// Build the map
var mapOpts = {center:golgotha.local.mapC, minZoom:6, zoom:8, scrollwheel:true, streetViewControl:false, clickableIcons:false, mapTypeControlOptions:{mapTypeIds:golgotha.maps.DEFAULT_TYPES}};
var map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', map.closeWindow);
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
google.maps.event.addListener(map, 'zoom_changed', golgotha.maps.updateZoom);
map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('zoomLevel'));

// Build the navaid list
<map:markers var="golgotha.local.navaids" items="${results}" />
map.addMarkers(golgotha.local.navaids);

// Surrounding navads
golgotha.local.sMarkers = new MarkerManager(map, {borderPadding:32});
document.forms[0].navaid.selectedIndex = 0;
google.maps.event.addListenerOnce(map, 'tilesloaded', function() { 
	golgotha.local.zoomTo(document.forms[0].navaid);
	google.maps.event.trigger(map, 'zoom_changed');
	google.maps.event.trigger(map, 'maptypeid_changed');
});
</script></c:if>
</body>
</html>
