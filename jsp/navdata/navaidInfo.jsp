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
<title><content:airline /> Navigation Database</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="googleMaps" />
<map:api version="2" />
<map:vml-ie />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.navaidCode, 'Navigate Aid Code')) return false;

setSubmit();
disableButton('SearchButton');
return true;
}

function toggleNavaids()
{
var btn = getElement('ToggleButton');
if (!showAll) {
	addMarkers(map, 'navaids');
	btn.value = 'HIDE SURROUNDING NAVAIDS';
} else {
	map.clearOverlays();
	addMarkers(map, 'nav');
	btn.value = 'SHOW SURROUNDING NAVAIDS';
}

showAll = !(showAll);
return true;
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
<el:form action="navsearch.do" method="post" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2">NAVIGATION AID</td>
</tr>
<c:if test="${!empty navaid}">
<c:if test="${!fn:isIntersection(navaid)}">
<tr>
 <td class="label">Name / Code</td>
 <td class="data pri bld">${navaid.name} (${navaid.code})</td>
</tr>
</c:if>
<c:if test="${fn:isIntersection(navaid)}">
<tr>
 <td class="label">Code</td>
 <td class="data pri bld">${navaid.code}</td>
</tr>
</c:if>
<tr>
 <td class="label">Type</td>
 <td class="data sec bld">${navaid.typeName}</td>
</tr>
<tr>
 <td class="label">Position</td>
 <td class="data"><fmt:geo pos="${navaid.position}" /></td>
</tr>
<c:if test="${fn:isVOR(navaid) || fn:isNDB(navaid)}">
<tr>
 <td class="label">Frequency</td>
 <td class="data bld">${navaid.frequency}</td>
</tr>
</c:if>
<c:if test="${fn:isRunway(navaid)}">
<tr>
 <td class="label">Heading / Length</td>
 <td class="data"><fmt:dec value="${navaid.length}" /> feet, <fmt:dec value="${navaid.heading}" fmt="000" />
<sup>o</sup>. <c:if test="${!empty navaid.frequency}"><span class="sec bld">ILS Frequency:&nbsp;
${navaid.frequency}</span></c:if></td>
</tr>
</c:if>
<c:if test="${fn:isAirport(navaid)}">
<tr>
 <td class="label">Airport Altitude</td>
 <td class="data"><fmt:dec value="${navaid.altitude}" /> feet</td>
</tr>
</c:if>
<tr>
 <td class="label">Legend</td>
 <td class="data"><map:legend color="red" legend="${navaid.code}" />
 <map:legend color="blue" legend="VOR" /> <map:legend color="orange" legend="NDB" />
 <map:legend color="green" legend="Airport" /> <map:legend color="white" legend="Intersection" />
 <el:button ID="ToggleButton" onClick="void toggleNavaids()" className="BUTTON" label="SHOW SURROUNDING NAVAIDS" /></td>
</tr>
<tr>
 <td class="label top">Map</td>
 <td class="data"><map:div ID="googleMap" x="650" y="550" /></td>
</tr>
</c:if>
<c:if test="${empty navaid}">
<tr>
 <td class="error bld mid" colspan="2">The Navigation Aid ${param.navaidCode} was not found in the 
<content:airline /> Navigation Data database.</td>
</tr>
</c:if>
</el:table>

<!-- Search Bar -->
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2">NEW SEARCH</td>
</tr>
<tr>
 <td class="label">Navigation Aid Code</td>
 <td class="data"><el:text name="navaidCode" className="pri bld req" idx="*" size="6" max="5" value="${param.navaidCode}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SearchButton" type="submit" className="BUTTON" label="NEW NAVIGATION DATA SEARCH" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<c:if test="${!empty navaid}">
<script language="JavaScript" type="text/javascript">
// Build the navaid and surrounding navaids
<map:point var="navP" point="${navaid}" />
<map:markers var="nav" items="${results}" color="red" />
<map:markers var="navaids" items="${navaids}" />
var showAll = false;

// Build the map
var map = new GMap2(getElement("googleMap"), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});
map.addControl(new GLargeMapControl3D());
map.addControl(new GMapTypeControl());
map.setCenter(navP, getDefaultZoom(90));
<map:type map="map" type="${gMapType}" default="G_PHYSICAL_MAP" />
GEvent.addListener(map, 'maptypechanged', updateMapText);
addMarkers(map, 'nav');
GEvent.trigger(map, 'maptypechanged');
</script></c:if>
<content:googleAnalytics />
</body>
</map:xhtml>
