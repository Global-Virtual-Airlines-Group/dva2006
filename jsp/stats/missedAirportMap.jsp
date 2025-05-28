<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Unvisited Airports Map - ${pilot.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<map:api version="3" />
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="allAirlines" name="airlines" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="mynewairports.do" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td width="65%"><span class="nophone"><content:airline />&nbsp;</span>UNVISITED AIRPORTS FOR ${pilot.name}</td>
 <td width="10%" class="mid"><el:cmd url="mynewairports">TABLE</el:cmd>
 <td class="right">AIRLINE <el:combo name="airline" idx="*" firstEntry="[ ALL ]"  options="${airlines}" onChange="void golgotha.local.filter(this)" /></td>
</tr>
<tr>
 <td class="data" colspan="3"><map:div ID="mapBox" height="620" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
<map:token />
<map:point var="golgotha.local.mapC" point="${mapCenter}" />
golgotha.local.airports = {all:[], airlines:[]};

// Create the map
const map = new golgotha.maps.Map(document.getElementById('mapBox'), {center:golgotha.local.mapC, zoom:6, minZoom:2, maxZoom:11, projection:'globe', style:'mapbox://styles/mapbox/outdoors-v12'});
map.addControl(new mapboxgl.FullscreenControl(), 'top-right');
map.addControl(new mapboxgl.NavigationControl(), 'top-right');
map.on('style.load', golgotha.maps.updateMapText);
map.once('load', function() { map.addControl(new golgotha.maps.BaseMapControl(golgotha.maps.DEFAULT_TYPES), 'top-left'); });

<c:forEach var="entry" items="${airports}">
<c:set var="airline" value="${allAirlines[entry.key]}" scope="page" />
golgotha.local.airports.airlines.push('${entry.key}');
<c:set var="aps" value="${entry.value}" scope="page" />
golgotha.local.airports['${entry.key}'] = [];
<c:forEach var="ap" items="${aps}">
<map:marker var="golgotha.local.airport" point="${ap}" marker="true" color="${airline.color}" />
golgotha.local.airports['${entry.key}'].push(golgotha.local.airport);
golgotha.local.airport.setMap(map);
</c:forEach>
</c:forEach>

golgotha.local.filter = function(combo) {
	if (combo.selectedIndex == 0) return golgotha.local.showAll();
	const myAL = golgotha.form.getCombo(combo);
	for (var x = 0; x < golgotha.local.airports.airlines.length; x++) {
		const al = golgotha.local.airports.airlines[x];	
		const aps = golgotha.local.airports[al];	
		const isVisible = (al == myAL);
		aps.forEach(function(ap) { ap.setMap(isVisible ? map : null); });
	}

	return true;
};

golgotha.local.showAll = function() {
	for (var x = 0; x < golgotha.local.airports.airlines.length; x++) {
		const al = golgotha.local.airports.airlines[x];
 		const aps = golgotha.local.airports[al];
 		aps.forEach(function(ap) { ap.setMap(map); });
	}

	return true;
};
</script>
</body>
</html>
