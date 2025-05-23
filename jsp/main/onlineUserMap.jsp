<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Online User Map</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<map:api version="3" />
<content:googleAnalytics eventSupport="true" />
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="users.do" method="get" validate="return false">
<el:table className="form">
<tr class="title">
 <td class="caps"><fmt:int value="${pilots.size()}" /> CURRENTLY LOGGED IN USERS<c:if test="${!empty maxUserDate}"><span class="nophone"> - MAXIMUM <fmt:int value="${maxUsers}" /> on <fmt:date date="${maxUserDate}" /></span></c:if></td>
 <td style="width:15%" class="mid"><el:cmd url="users">VIEW LIST</el:cmd></td>
</tr>
<tr>
 <td colspan="2"><map:div ID="mapBox" height="540" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
<map:token />
const mapOpts = {container:'mapBox', zoom:5, maxZoom:12, projection:'globe', center:[-93.25,38.88], style:'mapbox://styles/mapbox/outdoors-v12'};
const map = new golgotha.maps.Map(document.getElementById('mapBox'), mapOpts);
map.addControl(new mapboxgl.FullscreenControl(), 'top-right');
map.addControl(new mapboxgl.NavigationControl(), 'top-right');
map.addControl(new golgotha.maps.BaseMapControl(golgotha.maps.DEFAULT_TYPES), 'top-left');
map.on('style.load', golgotha.maps.updateMapText);

// Center the map and add positions
<map:markers var="golgotha.local.positions" items="${pilots}" />
map.addMarkers(golgotha.local.positions);
</script>
</body>
</html>
