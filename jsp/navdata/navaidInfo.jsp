<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Navigation Database</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:googleAnalytics />
<content:js name="common" />
<map:api version="3" />
<content:cspHeader />
<script async>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.navaidCode, l:2, t:'Navigation Aid Code'});
	golgotha.form.submit(f);
	return true;
};

golgotha.local.zoomTo = function(combo) {
	const idx = combo.selectedIndex;
	if ((idx < 0) || (idx >= golgotha.local.navaids.length)) return false;

	// Pan the map
	const mrk = golgotha.local.navaids[idx];
	map.panTo(mrk.getLngLat());
	golgotha.local.mapC = mrk.getLngLat();
	golgotha.local.loadWaypoints();
	mrk.getElement().dispatchEvent(new Event('click'));
	return true;
};

golgotha.local.toggleMarkers = function() {
	const z = map.getZoom();
	golgotha.local.sMarkers.forEach(function(m) { m.setMap((z >= m.minZoom) ? map : null); });
};

golgotha.local.loadWaypoints = function()
{
// Get the lat/long
const lat = golgotha.local.mapC.lat;
const lng = golgotha.local.mapC.lng;
const range = golgotha.maps.degreesToMiles(map.getBounds().getNorthEast().lng - map.getBounds().getSouthWest().lng);
map.removeMarkers(golgotha.local.sMarkers);

// Check if we don't select
const f = document.forms[0];
if (!f.showAll.checked) return true;

// Status message
golgotha.form.submit();

// Build the XML Requester
const xmlreq = new XMLHttpRequest();
xmlreq.timeout = 7500;
xmlreq.open('get', 'navaidsearch.ws?airports=true&lat=' + lat + '&lng=' + lng + '&range=' + Math.min(1000, Math.round(range)), true);
xmlreq.ontimeout = function() { f.showAll.checked = false; golgotha.form.clear(); return true; };
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	const js = JSON.parse(xmlreq.responseText);
	js.items.forEach(function(wp) {
		if (wp.code == '${param.navaidCode}') return;
		const mrk = new golgotha.maps.IconMarker((wp.pal) ? {pal:wp.pal,icon:wp.icon,info:wp.info,pt:wp.ll} : {color:wp.color,info:wp.info,label:wp.code,pt:wp.ll});;
		mrk.minZoom = 6; mrk.code = wp.code;
		if (wp.type == 'Airport')
			mrk.minZoom = 7;
		else if (wp.type == 'Intersection')
			mrk.minZoom = 8;

		golgotha.local.sMarkers.push(mrk);
		if (map.getZoom() >= mrk.minZoom)
			mrk.setMap(map);
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
 <td class="data"><map:div ID="mapBox" height="500" /></td>
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
 <td><el:button type="submit" label="NEW NAVIGATION DATA SEARCH" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<div id="zoomLevel" class="mapTextLabel right"></div>
<c:if test="${!empty results}">
<script async>
<map:token />
<map:point var="golgotha.local.mapC" point="${mapCenter}" />

// Build the map
const map = new golgotha.maps.Map(document.getElementById('mapBox'), {center:golgotha.local.mapC, minZoom:6, zoom:8, maxZoom:14, projection:'globe', style:'mapbox://styles/mapbox/outdoors-v12'});
map.addControl(new mapboxgl.FullscreenControl(), 'top-right');
map.addControl(new mapboxgl.NavigationControl(), 'top-right');
map.addControl(new golgotha.maps.DIVControl('zoomLevel'), 'bottom-right');
map.on('style.load', golgotha.maps.updateMapText);
map.on('zoomend', golgotha.maps.updateZoom);
map.on('zoomend', golgotha.local.toggleMarkers);

// Build the navaid list
<map:markers var="golgotha.local.navaids" items="${results}" />
map.addMarkers(golgotha.local.navaids);
golgotha.local.sMarkers = [];

document.forms[0].navaid.selectedIndex = 0;
map.once('load', function() {
	map.addControl(new golgotha.maps.BaseMapControl(golgotha.maps.DEFAULT_TYPES), 'top-left'); 
	golgotha.local.zoomTo(document.forms[0].navaid);
	map.fire('zoomend');
});
</script></c:if>
</body>
</html>
