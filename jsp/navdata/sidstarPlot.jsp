<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> SID/STAR Plotter</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<map:api version="2" />
<content:js name="markermanager" />
<content:js name="googleMaps" />
<content:js name="sidstarPlot" />
<map:vml-ie />
<content:sysdata var="imgPath" name="path.img" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />
<script language="JavaScript" type="text/javascript">
document.imgPath = '${imgPath}';

function clickIcon()
{
this.openInfoWindowHtml(this.infoLabel);
return true;
}

function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.name, 5, 'Route Name')) return false;
if (!validateText(form.transition, 3, 'Transition Name')) return false;
if (!validateText(form.runway, 2, 'Runway Name')) return false;
if (!validateText(form.route, 2, 'Route Waypoints')) return false;

setSubmit();
disableButton('SaveButton');
disableButton('ToggleButton');
return false;
}
</script>
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="sidstarplot.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> TERMINAL ROUTE PLOTTER</td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airport" size="1" idx="*" options="${airports}" value="${mapCenter}" firstEntry="-" onChange="void getRoutes(this)" /></td>
</tr>
<tr>
 <td class="label">Terminal Route</td>
 <td class="data"><el:combo name="tRoutes" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="void plotRoute(this)" /></td>
</tr>
<tr>
 <td class="label" valign="top">Route Map</td>
 <td class="data"><map:div ID="googleMap" x="100%" y="550" /><div id="copyright" class="bld"></div></td>
</tr>
<tr class="title caps doPlot" style="visibility:hidden;">
 <td colspan="2">NEW TERMINAL ROUTE</td>
</tr>
<tr class="doPlot" style="visibility:hidden;">
 <td class="label">Route Name</td>
 <td class="data"><el:text name="name" size="12" max="16" idx="*" value="" className="pri bld req" /></td>
</tr>
<tr class="doPlot" style="visibility:hidden;">
 <td class="label">Transition</td>
 <td class="data"><el:text name="transition" size="5" max="5" idx="*" value="" className="bld req" /></td>
</tr>
<tr class="doPlot" style="visibility:hidden;">
 <td class="label">Runway</td>
 <td class="data"><el:text name="runway" size="8" max="8" idx="*" value="" className="req" /></td>
</tr>
<tr class="doPlot" style="visibility:hidden;">
 <td class="label">Route</td>
 <td class="data"><el:text name="route" size="128" max="192" value="" readOnly="true" /></td>
</tr>
<tr class="doPlot" style="visibility:hidden;">
 <td class="label">Find Navigation Aid</td>
 <td class="data"><el:text name="navaidCode" size="4" max="6" idx="*" value="" />
 <el:button ID="FindButton" className="BUTTON" onClick="void findNavaid(document.forms[0].navaidCode.value)" label="FIND" /></td>
</tr>
<tr class="title doPlot">
 <td colspan="2" class="mid"><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE ROUTE" />
 <el:button ID="ToggleButton" className="BUTTON" onClick="void toggleRows(true)" label="NEW ROUTE" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
<map:point var="mapC" point="${mapCenter}" />
var map = new GMap2(getElement("googleMap"), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});

// Global airport/waypoint markers
var airports = new Array();
var waypoints = new Array();

// Add routes
var routeTrack;
var routes = new Array();

// Add map controls
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());
map.setCenter(mapC, 7);
map.currentAirport = '${mapCenter.ICAO}';
map.enableDoubleClickZoom();
map.enableContinuousZoom();
<map:type map="map" type="${gMapType}" default="G_PHYSICAL_MAP" />
var mm = new MarkerManager(map, {maxZoom:14, borderPadding:32});
GEvent.addListener(map, 'maptypechanged', updateMapText);
GEvent.addListener(map, 'moveend', loadWaypoints);
GEvent.trigger(map, 'maptypechanged');
getRoutes(document.forms[0].airport);
</script>
<content:googleAnalytics />
</body>
</map:xhtml>
