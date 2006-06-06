<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Interactive Route Map</title>
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
function updateAirlines(box)
{
var isLoading = getElement('isLoading');
isLoading.innerHTML = ' - UPDATING...';

// Update the markers
var markers = airports[box.value];
for (var x = 0; x < markers.length; x++) {
	var mrk = markers[x];
	if (box.checked) {
		map.addOverlay(mrk);
		if (!mrk.showRoutes)
			GEvent.addListener(mrk, 'click', function() { showRoutes('ATL') });
	} else
		map.removeOverlay(mrk);
}

isLoading.innerHTML = '';
return true;
}

function showRoutes(iata)
{
var isLoading = getElement('isLoading');
isLoading.innerHTML = ' - LOADING...';

// Build the XML Requester
var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "route_map.ws?iata=" + iata, true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var isLoading = getElement('isLoading');
	isLoading.innerHTML = ' - REDRAWING...';
	routes = new Array();
	
	// Parse the XML
	var xmlDoc = xmlreq.responseXML;
	var wsdata = xdoc.documentElement;
	var rts = wsdata.getElementsByTagName("route");
	for (var r = 0; r < rts.length; x++) {
		var pos = wsdata.getElementsByTagName("pos");
		var positions = new Array();
	
		// Get the positions
		for (var i = 0; i < pos.length; i++) {
			var pe = pos[i];
			var p = new GLatLng(parseFloat(pe.getAttribute("lat")), parseFloat(pe.getAttribute("lng")));
			positions.push(p);
		} // for

		// Draw the line
		var routeLine = new GPolyline(positions, '#4080AF', 2, 0.8);
		map.addOverlay(routeLine);
		routes.push(routeLine);
	}

	// Focus on the map
	isLoading.innerHTML = '';
	return true;
} // function

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
<el:form action="routemap.do" method="post" validate="return false">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> INTERACTIVE ROUTE MAP<span id="isLoading" /></td>
</tr>
<c:set var="alCount" value="${0}" scope="request" />
<tr>
 <td class="label" valign="top">Airport Legend</td>
 <td class="data small"><c:forEach var="airline" items="${airlines}">
<c:set var="alCount" value="${alCount + 1}" scope="request" />
<c:set var="alColor" value="${airportColors[airline.code]}" scope="request" />
<map:legend color="${alColor}" legend="${airline.name}" />
&nbsp;<el:box name="select${airline.code}" value="${airline.code}" label="" onChange="void updateAirlines(this)" />&nbsp;
<c:if test="${(alCount % 4) == 0)}"><br /></c:if>
</c:forEach></td>
</tr>
<tr>
 <td class="label" valign="top">Route Map</td>
 <td class="data"><map:div ID="googleMap" x="650" y="550" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
<map:point var="mapC" point="${mapCenter}" />

// Create the map
var map = new GMap2(getElement('googleMap'), G_DEFAULT_MAP_TYPES);
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());
map.setCenter(mapC, ${zoomLevel});
map.setMapType(${gMapType == 'map' ? 'G_MAP_TYPE' : 'G_SATELLITE_TYPE'});

// Save airports in JS array
var airports = new Array();
<c:forEach var="airline" items="${airlines}">
// ${airline.name}
<map:markers var="am${airline.code}" color="${airportColors[airline.code]}" items="${airportMap[airline]}" />
airports['${airline.code}'] = am${airline.code};
</c:forEach>

// Routes placeholder
var routes;
</script>
</body>
</html>
