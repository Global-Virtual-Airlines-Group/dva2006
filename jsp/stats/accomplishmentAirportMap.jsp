<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Unvisited Airports Map - ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></title>
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

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="accairportmap.do" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td width="65%"><span class="nophone"><content:airline />&nbsp;</span>UNVISITED AIRPORTS FOR ${pilot.name}</td>
 <td class="right">ACCOMPLISHMENT <el:combo name="acc" idx="*" firstEntry="[ SELECT ]"  options="${accs}" onChange="void golgotha.local.filter(this)"  /></td>
</tr>
<tr>
 <td class="data" colspan="3"><map:div ID="mapBox" height="540" /></td>
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

// Create the map
const map = new golgotha.maps.Map(document.getElementById('mapBox'), {center:golgotha.local.mapC, zoom:6, minZoom:2, maxZoom:11, projection:'globe', style:'mapbox://styles/mapbox/outdoors-v12'});
map.addControl(new mapboxgl.FullscreenControl(), 'top-right');
map.addControl(new mapboxgl.NavigationControl(), 'top-right');
map.on('style.load', golgotha.maps.updateMapText);
map.once('load', function() { map.addControl(new golgotha.maps.BaseMapControl(golgotha.maps.DEFAULT_TYPES), 'top-left'); });

golgotha.local.allAirports = {};
<c:forEach var="ap" items="${airports}">
golgotha.local.allAirports['${ap.ICAO}'] = <map:marker point="${ap}" />;</c:forEach>
golgotha.local.accs = ${jsData};

golgotha.local.filter = function(combo) {
	map.clearOverlays();
	if (combo.selectedIndex < 1) return false;	
	const codes = golgotha.local.accs[golgotha.form.getCombo(combo)];
	if (!codes) return false;
	
	const llb = new mapboxgl.LngLatBounds();
	codes.forEach(function(c) { 
		const a = golgotha.local.allAirports[c];
		if (a) {
			a.setMap(map);
			llb.extend(a.getLngLat());
		}
	});

	map.fitBounds(llb);
	return true;
};
</script>
</body>
</html>
