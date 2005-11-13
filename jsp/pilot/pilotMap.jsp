<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Map</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="googleMaps" />
<map:api version="1" />
<map:vml-ie />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>
<content:sysdata var="imgPath" name="path.img" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td><content:airline /> PILOT MAP</td>
</tr>
<tr>
 <td class="data"><map:div ID="googleMap" x="700" y="650" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td>&nbsp;<content:filter roles="Pilot"><el:cmdbutton url="geolocate" label="UPDATE LOCATION" /></content:filter></td>
</tr>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
// Add the markers
<map:markers var="pilotMarkers" items="${locations}" />

// Build the map
<map:point var="mapC" point="${mapCenter}" />
<map:marker var="hq" point="${mapCenter}" />
var map = new GMap(getElement("googleMap"), [G_MAP_TYPE, G_SATELLITE_TYPE]);
map.addControl(new GSmallZoomControl());
map.addControl(new GMapTypeControl());
map.centerAndZoom(mapC, 15);
addMarkers(map, 'pilotMarkers');
addMarkers(map, 'hq');
</script>
</body>
</html>
