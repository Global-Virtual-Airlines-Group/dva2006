<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Unvisited Airports Map - ${pilot.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<map:api version="3" />
<script>
golgotha.local.filter = function(combo) {
	if (combo.selectedIndex == 0)
		return golgotha.local.showAll();
		
	var myAL = golgotha.form.getCombo(combo);
	for (var x = 0; x < golgotha.local.airports.airlines.length; x++) {
		var al = golgotha.local.airports.airlines[x];	
		var aps = golgotha.local.airports[al];	
		var isVisible = (al == myAL);
		for (var y = 0; y < aps.length; y++)
			aps[y].setVisible(isVisible);
	}
	
	return true;
};

golgotha.local.showAll = function()
{
for (var x = 0; x < golgotha.local.airports.airlines.length; x++) {
	var al = golgotha.local.airports.airlines[x];
	var aps = golgotha.local.airports[al];
	for (var y = 0; y < aps.length; y++)
		aps[y].setVisible(true);	
}
	
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload(map)">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="allAirlines" name="airlines" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="mynewairports.do" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td width="65%"><content:airline /> UNVISITED AIRPORTS FOR ${pilot.name}</td>
 <td width="10%" class="mid"><el:cmd url="mynewairports">TABLE</el:cmd>
 <td class="right">AIRLINE <el:combo name="airline" idx="*" firstEntry="[ ALL ]"  options="${airlines}" onChange="void golgotha.local.filter(this)"  /></td>
</tr>
<tr>
 <td class="data" colspan="3"><map:div ID="googleMap" height="425" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script id="mapInit" defer>
<map:point var="golgotha.local.mapC" point="${mapCenter}" />
var mapOpts = {center:golgotha.local.mapC, zoom:6, minZoom:2, maxZoom:11, streetViewControl:false, scrollwheel:true, mapTypeControlOptions:{mapTypeIds:golgotha.maps.DEFAULT_TYPES}};
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', map.closeWindow);
golgotha.local.airports = {all:[], airlines:[]};
<c:forEach var="entry" items="${airports}">
<c:set var="airline" value="${allAirlines[entry.key]}" scope="page" />
golgotha.local.airports.airlines.push('${entry.key}');
<c:set var="aps" value="${entry.value}" scope="page" />
golgotha.local.airports['${entry.key}'] = [];
<c:forEach var="ap" items="${aps}">
<map:marker var="airport" point="${ap}" marker="true" color="${airline.color}" />
golgotha.local.airports['${entry.key}'].push(airport);
airport.setMap(map);
</c:forEach>
</c:forEach>
</script>
</body>
</html>
