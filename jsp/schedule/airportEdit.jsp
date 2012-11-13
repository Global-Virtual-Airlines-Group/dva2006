<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Schedule - ${!empty airport ? airport.IATA : 'New Airport'}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="airportRefresh" />
<c:set var="googleMap" value="${isNew && (!empty airport)}" scope="page" />
<c:if test="${googleMap}">
<map:api version="3" /></c:if>
<content:googleAnalytics eventSupport="true" />
<fmt:aptype var="useICAO" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.name, 6, 'Airport Name')) return false;
if (!validateCombo(form.country, 'Country')) return false;
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
disableButton('DeleteButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="void updateOldAirports()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:empty var="emptyList" />
<content:tz var="timeZones" />
<content:sysdata var="airlines" name="airlines" mapValues="true" sort="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="airport.do" method="post" linkID="${isNew ? '' : airport.IATA}" op="save" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">AIRPORT PROFILE</td>
</tr>
<tr>
 <td class="label top" rowspan="2">Airport Name</td>
 <td class="data"><el:text name="name" idx="*" className="pri bld" required="true" size="36" max="36" value="${airport.name}" /></td>
</tr>
<tr>
 <td class="data small">The airport name should be in the following formats:<br />
Airports inside the United States or Canada, use &lt;City Name&gt; &lt;State Abbreviation&gt;. <span class="ita">(Chattanooga TN)</span><br /> 
Airports inside the United States or Canada with multiple airports, use &lt;City Name&gt;-&lt;Airport Name&gt; &lt;State Abbreviation&gt;. 
 <span class="ita">(New York-Kennedy NY)</span><br />
Airports outside the United States or Canada, use &lt;City Name&gt; &lt;Country&gt;. <span class="ita">(Trondheim Norway)</span><br />
Airports outside the United States or Canada with multiple airports, use &lt;City Name&gt;-&lt;Airport Name&gt; &lt;Country&gt;.
 <span class="ita">(Paris-Charles De Gaulle France)</span></td>
</tr>
<tr>
 <td class="label">Country</td>
 <td class="data"><el:combo name="country" idx="*" required="true" options="${countries}" firstEntry="-" value="${airport.country}" onChange="void updateOldAirports()" /></td>
</tr>
<tr>
 <td class="label">IATA Code</td>
 <td class="data"><el:text name="iata" idx="*" className="bld" required="true" size="2" max="3" value="${airport.IATA}" /></td>
</tr>
<tr>
 <td class="label">ICAO Code</td>
 <td class="data"><el:text name="icao" idx="*" size="4" max="4" required="true" value="${airport.ICAO}" /></td>
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
<c:if test="${airport.maximumRunwayLength > 0}">
<tr>
 <td class="label">Maximum Runway Length</td>
 <td class="data"><fmt:int value="${airport.maximumRunwayLength}" /> feet</td>
</tr>
</c:if>
<tr>
 <td class="label">Time Zone</td>
 <td class="data"><el:combo name="tz" size="1" idx="*" required="true" options="${timeZones}" firstEntry="-" value="${airport.TZ}" /></td>
</tr>
<tr>
 <td class="label top">Airlines</td>
 <td class="data"><el:check name="airline" idx="*" width="195" className="small" cols="4" options="${airlines}" newLine="true" checked="${airport.airlineCodes}" /></td>
</tr>
<tr>
 <td class="label top">Prior Airport</td>
 <td class="data"><el:combo name="oldAirport" idx="*" size="1" options="${emptyList}" firstEntry="-" onChange="void changeAirport(this)" />&nbsp;
<el:text ID="oldAirportCode" name="oldAirportCode" size="4" max="4" value="${airport.supercededAirport}" onBlur="void setAirport(document.forms[0].oldAirport, this.value, true)" /><br />
<span class="small ita">Prior airports exist in older simulator versions and can be substitued for this Airport despite the
lack of scheduled flights in the <content:airline /> Flight Schedule.</span></td>
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
 <td colspan="2" class="mid"><iframe id="airportLookup" style="width:97%; height:280px; scrolling:auto;" src="http://www.theairdb.com/airport/${apCode}.html"></iframe></td>
</tr>
</c:if>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE AIRPORT PROFILE" />&nbsp;
<el:cmdbutton ID="DeleteButton" url="airportdelete" linkID="${airport.IATA}" label="DELETE AIRPORT" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<c:if test="${googleMap}">
<script type="text/javascript">
<map:point var="mapC" point="${airport}" />
<map:marker var="apMarker" point="${airport}" color="green" />

//Create map options
var mapTypes = {mapTypeIds: golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:mapC, zoom:6, scrollwheel:false, streetViewControl:false, mapTypeControlOptions: mapTypes};

// Build the map
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
map.setMapTypeId(google.maps.MapTypeId.SATELLITE);
addMarkers(map, 'apMarker');
</script></c:if>
<script type="text/javascript">
function updateOldAirports()
{
var f = document.forms[0];
<c:if test="${empty airport}">
var cmd = (f.country.selectedIndex > 0) ? ('country=' + getValue(f.country)) : 'airline=all';</c:if>
<c:if test="${!empty airport}">
var cmd ='airport=${airport.ICAO}&dist=50';</c:if>
updateAirports(f.oldAirport, cmd, ${useICAO}, f.oldAirportCode.value);
return true;
}
</script>
</body>
</html>
