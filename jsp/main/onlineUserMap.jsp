<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> Online User Map</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:sysdata var="imgPath" name="path.img" />
<content:js name="googleMaps" />
<content:googleAnalytics eventSupport="true" />
<map:api version="2" />
<map:vml-ie />
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="users.do" method="get" validate="return false">
<el:table className="form" pad="default" space="default">
<tr class="title">
 <td class="caps"><fmt:int value="${fn:sizeof(pilots)}" /> CURRENTLY LOGGED IN USERS
<c:if test="${!empty maxUserDate}"> - MAXIMUM <fmt:int value="${maxUsers}" /> on <fmt:date date="${maxUserDate}" /></c:if></td>
 <td width="15%" class="mid"><el:cmd url="users">VIEW LIST</el:cmd></td>
</tr>
<tr>
 <td colspan="2"><map:div ID="googleMap" x="100%" y="520" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
// Create the map
var map = new GMap2(getElement("googleMap"), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});
map.addControl(new GLargeMapControl3D());
map.addControl(new GMapTypeControl());

// Mark each pilot's position in hashmap
<map:markers var="positions" items="${pilots}" />

// Center the map and add positions
map.setCenter(new GLatLng(38.88, -93.25), 4);
map.enableDoubleClickZoom();
map.enableContinuousZoom();
map.setMapType(G_PHYSICAL_MAP);
addMarkers(map, 'positions');
</script>
</body>
</map:xhtml>
