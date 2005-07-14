<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Navigation Database</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="common" />
<content:sysdata var="imgPath" name="path.img" />
<content:sysdata var="googleAPIKey" name="security.key.googleMaps" />
<content:js name="googleMaps" />
<content:js name="http://maps.google.com/maps?file=api&v=1&key=${googleAPIKey}" />
<style type="text/css">
v\:* {
	behavior:url(#default#VML);
}
</style>
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.navaidCode, 'Navigate Aid Code')) return false;

setSubmit();
disableButton('SearchButton');
return true;
}

function toogleNavaids()
{
if (!showAll) {
	for (x = 0; x < navaids.length; x++)
		map.addOverlay(navaids[x]);
} else {
	map.clearOverlays();
	map.addOverlay(gmP);
}

showAll = !(showAll);
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form action="navsearch.do" method="POST" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2">NAVIGATION AID</td>
</tr>
<tr>
 <td class="label">Name / Code</td>
 <td class="data pri bld">${navaid.name} (${navaid.code})</td>
</tr>
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
 <td class="label" valign="top">Map<br />
<br />
<el:button onClick="void toggleNavaids()" className="BUTTON" label="SHOW ALL" /></td>
 <td class="data"><div id="googleMap" style="width: 620px; height: 580px" /></td>
</tr>
</el:table>

<!-- Search Bar -->
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2">NEW SEARCH</td>
</tr>
<tr>
 <td class="label">Navigation Aid Code</td>
 <td class="data"><el:text name="navaidCode" idx="*" size="4" max="5" value="${param.navaidCode}" /></td>
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
</div>
<script language="JavaScript" type="text/javascript">
// Build the navaid
var navP = new GPoint(${navaid.longitude}, ${navaid.latitude});
var gmP = googleMarker('${imgPath}',colors[${nav.type}], navP);

// Build the surrounding navaids
var colors = ['yellow','blue','red','green',''];
var navaids = new Array();
<c:forEach var="nav" items="${navaids}">
navaids.push(googleMarker('${imgPath}',colors[${nav.type}],new GPoint(${nav.longitude},${nav.latitude}));
</c:forEach>

// Build the map
var map = new GMap(getElement("googleMap"));
map.addControl(new GSmallZoomControl());
map.addControl(new GMapTypeControl());
map.centerAndZoom(navP, getDefaultZoom(90));

// Add the navaid marker
map.addOverlay(gmP);
var showAll = false;
</script>
</body>
</html>
