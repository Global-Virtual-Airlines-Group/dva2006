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
<content:js name="pilotMap" />
<map:api version="1" />
<map:vml-ie />
<script language="JavaScript" type="text/javascript">

</script>
</head>
<content:copyright visible="false" />
<body onload="void enableElement('eq', false); void enableElement('rnk', false);">
<content:page>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>
<content:sysdata var="imgPath" name="path.img" />
<content:sysdata var="ranks" name="ranks" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="pilotboard.do" method="get" validate="return false">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> PILOT LOCATIONS<span id="isLoading" /></td>
</tr>
<tr>
 <td class="data" colspan="2"><map:div ID="googleMap" x="700" y="650" /></td>
</tr>
<tr>
 <td class="label" valign="top">Equipment Programs</td>
 <td class="data"><el:check name="eqTypes" width="80" cols="6" className="small" separator="<div style=\"clear:both;\" />" checked="${eqTypes}" options="${eqTypes}" onChange="void updateMarkers(this)" /></td>
</tr>
<tr>
 <td class="label" valign="top">Pilot Ranks</td>
 <td class="data"><el:check name="ranks" width="160" cols="3" className="small" separator="<div style=\"clear:both;\" />" checked="${ranks}" options="${ranks}" onChange="void updateMarkers(this)" /></td>
</tr>
</el:table>

<content:filter roles="Pilot">
<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
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
<script language="JavaScript" type="text/javascript">
// Build the map
<map:point var="mapC" point="${mapCenter}" />
<map:marker var="hq" point="${mapCenter}" />
var map = new GMap(getElement("googleMap"), [G_MAP_TYPE, G_SATELLITE_TYPE, G_HYBRID_TYPE]);
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());
map.centerAndZoom(mapC, 13);
addMarkers(map, 'hq');

// Initialize the marker hashtables
var pMarkers = new Array();
pMarkers['EMB-120'] = new Array();
<c:forEach var="rank" items="${ranks}">
pMarkers['${rank}'] = new Array();
</c:forEach>
<c:forEach var="eqType" items="${eqTypes}">
pMarkers['${eqType}'] = new Array();
</c:forEach>

// Load the markers
var xmlreq = generateXMLRequest('${imgPath}');
xmlreq.send(null);
</script>
</body>
</html>
