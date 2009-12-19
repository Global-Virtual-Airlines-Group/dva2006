<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<c:if test="${!empty airport}">
<title><content:airline /> Schedule - ${airport.IATA}</title>
</c:if>
<c:if test="${empty airport}">
<title><content:airline /> Schedule - New Airport</title>
</c:if>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<c:set var="googleMap" value="${isNew && (!empty airport)}" scope="page" />
<c:if test="${googleMap}">
<content:js name="googleMaps" />
<map:api version="2" />
<map:vml-ie />
</c:if>
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.name, 6, 'Airport Name')) return false;
if (!validateText(form.iata, 3, 'IATA Code')) return false;
if (!validateText(form.icao, 4, 'ICAO Code')) return false;
if (!validateCombo(form.tz, 'Time Zone')) return false;
if (!validateNumber(form.latD, 0, 'Latitude Degrees')) return false;
if (!validateNumber(form.latM, 0, 'Latitude Minutes')) return false;
if (!validateNumber(form.latS, 0, 'Latitude Seconds')) return false;
if (!validateNumber(form.lonD, 0, 'Longitude Degrees')) return false;
if (!validateNumber(form.lonM, 0, 'Longitude Minutes')) return false;
if (!validateNumber(form.lonS, 0, 'Longitude Seconds')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body<c:if test="${googleMap}"> onunload="GUnload()"</c:if>>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="airlines" name="airlines" mapValues="true" sort="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="airport.do" method="post" linkID="${isNew ? '' : airport.IATA}" op="save" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">AIRPORT PROFILE</td>
</tr>
<tr>
 <td class="label">Airport Name</td>
 <td class="data"><el:text name="name" idx="*" className="pri bld req" size="36" max="36" value="${airport.name}" /></td>
</tr>
<tr>
 <td class="label">IATA Code</td>
 <td class="data"><el:text name="iata" idx="*" className="bld req" size="2" max="3" value="${airport.IATA}" /></td>
</tr>
<tr>
 <td class="label">ICAO Code</td>
 <td class="data"><el:text name="icao" idx="*" size="4" max="4" className="req" value="${airport.ICAO}" /></td>
</tr>
<tr>
 <td class="label">Latitude</td>
 <td class="data"><el:text name="latD" idx="*" size="2" max="2" value="${latD}" /> degrees 
<el:text name="latM" idx="*" size="2" max="2" value="${latM}" /> minutes 
<el:text name="latS" idx="*" size="2" max="2" value="${latS}" /> seconds 
<el:combo name="latDir" idx="*" size="1" options="${latDir}" value="${latNS}" /></td>
</tr>
<tr>
 <td class="label">Longitude</td>
 <td class="data"><el:text name="lonD" idx="*" size="2" max="4" value="${lonD}" /> degrees 
<el:text name="lonM" idx="*" size="2" max="2" value="${lonM}" /> minutes 
<el:text name="lonS" idx="*" size="2" max="2" value="${lonS}" /> seconds 
<el:combo name="lonDir" idx="*" size="1" options="${lonDir}" value="${lonEW}" /></td>
</tr>
<tr>
 <td class="label">Time Zone</td>
 <td class="data"><el:combo name="tz" size="1" idx="*" options="${timeZones}" className="req" firstEntry="-" value="${airport.TZ}" /></td>
</tr>
<tr>
 <td class="label top">Airlines</td>
 <td class="data"><el:check name="airline" idx="*" width="195" className="small" cols="4" options="${airlines}" newLine="true" checked="${airport.airlineCodes}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="hasADSE" idx="*" className="small" value="true" checked="${airport.ADSE}" label="This Airport has ADSE-X Ground Radar" /></td>
</tr>
<content:hasmsg>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data error bld"><content:sysmsg /></td>
</tr>
</content:hasmsg>
<tr class="title caps">
 <td colspan="2">WORLD TIME ZONE MAP</td>
</tr>
<tr>
 <td colspan="2" class="mid"><el:img src="worldzones.png" caption="Time Zone Map" /></td>
</tr>
<c:if test="${isNew}">
<c:if test="${!empty airport}">
<tr class="title caps">
 <td colspan="2">AIRPORT LOCATION</td>
</tr>
<tr>
 <td colspan="2"><map:div ID="googleMap" x="100%" y="550" /></td>
</tr>
</c:if>
<c:if test="${isNew && (!empty param.id)}">
<c:set var="apCode" value="${empty airport ? param.id : airport.ICAO}" scope="page" />
<tr class="title caps">
 <td colspan="2">AIRPORT INFORMATION</td>
</tr>
<tr>
 <td colspan="2" class="mid"><iframe id="airportLookup" width="97%" height="280" scrolling="auto" src="http://www.theairdb.com/airport/${apCode}.html"></iframe></td>
</tr>
</c:if>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE AIRPORT PROFILE" />&nbsp;
<el:cmdbutton ID="DeleteButton" url="airportdelete" linkID="${airport.IATA}" label="DELETE AIRPORT" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<c:if test="${googleMap}">
<script language="JavaScript" type="text/javascript">
<map:point var="mapC" point="${airport}" />
<map:marker var="apMarker" point="${airport}" color="green" />

// Build the map
var map = new GMap2(getElement("googleMap"), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});
map.addControl(new GLargeMapControl3D());
map.addControl(new GMapTypeControl());
map.setCenter(mapC, 6);
map.setMapType(G_SATELLITE_MAP);
map.enableDoubleClickZoom();
map.enableContinuousZoom();
addMarkers(map, 'apMarker');
</script>
</c:if>
</body>
<content:googleAnalytics />
</map:xhtml>
