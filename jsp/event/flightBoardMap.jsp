<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ page buffer="none" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Flight Map</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:sysdata var="imgPath" name="path.img" />
<content:js name="googleMaps" />
<map:api version="1" />
<map:vml-ie />
<script language="JavaScript" type="text/javascript">
function setNetwork(combo)
{
var net = combo.options[combo.selectedIndex].text;
location.href = '/flightboard.do?id=' + net + '&op=map';
return true;
}

function showRoute(pilotID)
{
// Get the network name
var f = document.forms[0];
var networkName = f.networkName.options[f.networkName.selectedIndex].text;

// Try and load a cached route
selectedRoute = allRoutes[pilotID];
if (!selectedRoute) {
	var request = GXmlHttp.create();
	request.open("GET", "si_route.ws?network=" + networkName + "&id=" + pilotID, true);
	request.onreadystatechange = function()
	{
	if (request.readyState != 4) return false;
	var points = new Array();
	var xmlDoc = request.responseXML;
	var navaids = xmlDoc.documentElement.getElementsByTagName("navaid");
	for (var i = 0; i < navaids.length; i++) {
		var nav = navaids[i];
		points.push(new GPoint(parseFloat(nav.getAttribute("lng")), parseFloat(nav.getAttribute("lat"))));
	}
	
	allRoutes[pilotID] = new GPolyline(points, '#4080AF', 2, 0.8);
	selectedRoute = allRoutes[pilotID];
	addMarkers(map, 'selectedRoute');
	return true;
	}
	
	request.send(null);
	return true;
}

addMarkers(map, 'selectedRoute');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form action="flightboard.do" method="POST" validate="return false">
<el:table className="form" pad="default" space="default">
<tr class="title">
 <td width="40%" class="left">ONLINE PILOTS - ${netInfo.name} - VALID AS OF 
 <fmt:date date="${netInfo.validDate}" /></td>
 <td width="25%" class="mid"><el:cmd url="flightboard" linkID="${network}">FLIGHT BOARD</el:cmd></td>
 <td class="right">SELECT NETWORK <el:combo name="networkName" size="1" idx="1" onChange="void setNetwork(this)" options="${networks}" value="${network}" /></td>
</tr>
<tr>
 <td colspan="3"><span class="pri bld">LEGEND</span> <map:legend color="blue" className="small" legend="Member Pilot - Our Airline" />
 <map:legend color="yellow" className="small" legend="Our Airline" />
 <map:legend color="green" className="small" legend="Member Pilot" />
 <map:legend color="white" className="small" legend="${netInfo.name} Pilot" /></td>
</tr>
<tr>
 <td colspan="3"><div id="googleMap" style="width: 840px; height: 630px" /></td>
</tr>
<tr class="title mid caps">
 <td colspan="3">&nbsp;<c:if test="${netInfo.cached}">USING CACHED DATA</c:if></td>
</tr>
</el:table>
</el:form>
<content:copyright />
</div>
<script language="JavaScript" type="text/javascript">
// Create the map
var map = new GMap(getElement("googleMap"), [G_MAP_TYPE, G_SATELLITE_TYPE, G_HYBRID_TYPE]);
map.addControl(new GSmallMapControl());
map.addControl(new GMapTypeControl());

// Mark each pilot's position in hashmap
var positions = new Array();

<c:forEach var="pilot" items="${netInfo.pilots}">
<map:marker var="gPosition" point="${pilot}" />
GEvent.addListener(gPosition, 'click', function() { showRoute('${pilot.callsign}'); });
positions.push(gPosition);
</c:forEach>

// Route cache
var allRoutes = new Array();
var selectedRoute;

// Center the map and add positions
map.centerAndZoom(new GPoint(-93.25, 38.88), 13);
GEvent.addListener(map, 'infowindowclose', function() { map.removeOverlay(selectedRoute); });
addMarkers(map, 'positions');
</script>
</body>
</html>
