<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Map</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<map:api version="3" />
<content:js name="pilotMap" />
<content:googleAnalytics />
<content:js name="progressBar" />
<content:filter roles="HR">
<script async>
golgotha.pilotMap.deleteMarker = function(id) {
	const xmlreq = new XMLHttpRequest();
	xmlreq.timeout = 7500;
	xmlreq.open('post', 'pilotmapclear.ws', true);
	xmlreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=utf-8');
	xmlreq.onreadystatechange = function() {
		if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
		for (var x = 0; x < golgotha.pilotMap.mrks.length; x++) {
			const mrk = golgotha.pilotMap.mrks[x];
			if (mrk.ID == id) {
				golgotha.pilotMap.mrks.remove(mrk);
				mrk.setMap(null);
				return true;
			}
		}

		return false;
	}

	golgotha.event.beacon('Pilot Map', 'Delete Invalid Marker');
	xmlreq.send('id=0x' + id.toString(16));
	return true;
};
</script></content:filter>
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:enum var="ranks" className="org.deltava.beans.Rank" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="pilotboard.do" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> PILOT LOCATIONS<span id="isLoading"></span></td>
</tr>
<tr>
 <td class="data" colspan="2"><map:div ID="mapBox" height="550" /></td>
</tr>
<tr class="title caps locFilter">
 <td colspan="2">PILOT LOCATION FILTERING</td>
</tr>
<tr class="locFilter">
 <td class="label nophone">Equipment Program</td>
 <td class="data"><el:combo name="eqType" size="1" firstEntry="[ ALL PROGRAMS ]" options="${eqTypes}" onChange="void golgotha.pilotMap.updateMarkers()" /></td>
</tr>
<tr class="locFilter">
 <td class="label nophone">Pilot Ranks</td>
 <td class="data"><el:combo name="rank" size="1" firstEntry="[ ALL RANKS ]" options="${ranks}" onChange="void golgotha.pilotMap.updateMarkers()" /></td>
</tr>
</el:table>

<content:filter roles="Pilot">
<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:cmdbutton url="geolocate" label="UPDATE MY LOCATION" /></td>
</tr>
</el:table>
</content:filter>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
<map:token />
<map:point var="golgotha.local.mapC" point="${mapCenter}" />
<map:marker var="hq" point="${hq}" />

const mapOpts = {center:golgotha.local.mapC,zoom:6,minZoom:2,maxZoom:11,projection:'globe',style:'mapbox://styles/mapbox/outdoors-v12'};
const map = new golgotha.maps.Map(document.getElementById('mapBox'), mapOpts);
map.addControl(new mapboxgl.FullscreenControl(), 'top-right');
map.addControl(new mapboxgl.NavigationControl(), 'top-right');
map.once('load', function() {
	map.addControl(new golgotha.maps.BaseMapControl(golgotha.maps.DEFAULT_TYPES), 'top-left');
	hq.setMap(map);
	golgotha.pilotMap.load();
});
</script>
</body>
</html>
