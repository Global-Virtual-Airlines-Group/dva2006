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
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Flight Map</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:css name="form" />
<content:js name="common" />
<content:js name="googleMaps" />
<map:api version="1" />
<c:if test="${!empty browser$ie}">
<style type="text/css">
v\:* {
	behavior:url(#default#VML);
}
</style>
</c:if>
<script language="JavaScript" type="text/javascript">
function setNetwork(combo)
{
var net = combo.options[combo.selectedIndex].text;
location.href = '/flightboard.do?id=' + net + '&op=map';
return true;
}

function restore(overlays)
{
for (x = 0; x < overlays.length; x++)
	map.addOverlay(overlays[x]);
	
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

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
var map = new GMap(getElement("googleMap"));
map.addControl(new GSmallZoomControl());
map.addControl(new GMapTypeControl());

// Mark each pilot's position, and route in a hashmap
var positions = new Array();
var allRoutes = new Array();
var selectedRoute;

<c:forEach var="pilot" items="${netInfo.pilots}">
<map:marker var="gPosition" point="${pilot}" />
<c:if test="${!empty pilot.route}">
<map:points var="routePoints" items="${pilot.route}" />
<map:line var="gRoute" src="routePoints" color="#28405F" width="1" transparency="0.8" />
allRoutes['${pilot.ID}'] = gRoute;
GEvent.addListener(gPosition, 'click', function() { var sRoute = allRoutes['${pilot.ID}']; map.addOverlay(sRoute); selectedRoute = sRoute; });
</c:if>
positions.push(gPosition);
</c:forEach>

// Center the map and add positions
map.centerAndZoom(new GPoint(-93.25, 38.88), 13);
GEvent.addListener(map, 'infowindowclose', function() { map.removeOverlay(selectedRoute); });
restore(positions);
</script>
</body>
</html>
