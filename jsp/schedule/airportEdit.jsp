<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
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
<content:favicon />
<content:js name="common" />
<content:json />
<content:js name="airportRefresh" />
<c:set var="googleMap" value="${isNew && (!empty airport)}" scope="page" />
<c:if test="${googleMap}">
<map:api version="3" /></c:if>
<content:googleAnalytics eventSupport="true" />
<script async>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.name, l:6, t:'Airport Name'});
golgotha.form.validate({f:f.country, t:'Country'});
golgotha.form.validate({f:f.iata, l:3, t:'IATA Code'});
golgotha.form.validate({f:f.icao, l:4, t:'ICAO Code'});
golgotha.form.validate({f:f.tz, t:'Time Zone'});
golgotha.form.validate({f:f.latD, min:0, t:'Latitude Degrees'});
golgotha.form.validate({f:f.latM, min:0, t:'Latitude Minutes'});
golgotha.form.validate({f:f.latS, min:0, t:'Latitude Seconds'});
golgotha.form.validate({f:f.lonD, min:0, t:'Longitude Degrees'});
golgotha.form.validate({f:f.lonM, min:0, t:'Longitude Minutes'});
golgotha.form.validate({f:f.lonS, min:0, t:'Longitude Seconds'});
golgotha.form.submit(f);
return true;
};

golgotha.onDOMReady(function() {
	const f = document.forms[0];
	const cfg = golgotha.airportLoad.config;
	cfg.airline = 'all';
	<c:if test="${empty airport}">
	if (f.country.selectedIndex > 0) cfg.country = golgotha.form.getCombo(f.country);</c:if>
	<c:if test="${!empty airport}">
	cfg.airport = '${airport.ICAO}'; cfg.dist = 50;</c:if>
	golgotha.airportLoad.setHelpers(f.oldAirport);
	f.oldAirport.loadAirports(cfg);
	return true;
});
</script>
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:empty var="emptyList" />
<content:tz var="timeZones" />
<content:sysdata var="airlines" name="airlines" mapValues="true" sort="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="airport.do" method="post" linkID="${isNew ? '' : airport.IATA}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
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
Airports inside the United States or Canada with multiple airports, use &lt;City Name&gt;-&lt;Airport Name&gt; &lt;State Abbreviation&gt;. <span class="ita">(New York-Kennedy NY)</span><br />
Airports outside the United States or Canada, use &lt;City Name&gt; &lt;Country&gt;. <span class="ita">(Trondheim Norway)</span><br />
Airports outside the United States or Canada with multiple airports, use &lt;City Name&gt;-&lt;Airport Name&gt; &lt;Country&gt;. <span class="ita">(Paris-Charles De Gaulle France)</span></td>
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
<c:if test="${!isNew}">
<content:filter roles="Schedule,Operations">
<tr>
 <td class="label">Information</td>
 <td class="data"><c:if test="${!airport.gateData}"><span class="warn bld caps">No Gate / Airline data defined</span> </c:if><el:cmd url="airportInfo" linkID="${airport.ICAO}" className="pri bld">Click Here</el:cmd> to view Airport, Runway and Gate information.</td>
</tr>
</content:filter>
</c:if>
<tr>
 <td class="label">Time Zone</td>
 <td class="data"><el:combo name="tz" size="1" idx="*" required="true" options="${timeZones}" firstEntry="-" value="${airport.TZ}" /></td>
</tr>
<tr>
 <td class="label top">Airlines</td>
 <td class="data"><el:check name="airline" idx="*" width="205" className="small" cols="4" options="${airlines}" newLine="true" checked="${airport.airlineCodes}" /></td>
</tr>
<tr>
 <td class="label top">Prior Airport</td>
 <td class="data"><el:combo name="oldAirport" idx="*" size="1" options="${emptyList}" firstEntry="-" onChange="void changeAirport(this)" />&nbsp;
<el:text ID="oldAirportCode" name="oldAirportCode" size="4" max="4" value="${airport.supercededAirport}" onBlur="void setAirport(document.forms[0].oldAirport, this.value, true)" /><br />
<span class="small ita">Prior airports exist in older simulator versions and can be substitued for this Airport despite the lack of scheduled flights in the <content:airline /> Flight Schedule.</span></td>
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
 <td colspan="2"><map:div ID="googleMap" height="550" /></td>
</tr>
</c:if>
<content:secure secure="false">
<c:if test="${isNew && (!empty param.id)}">
<c:set var="apCode" value="${empty airport ? param.id : airport.ICAO}" scope="page" />
<tr class="title caps">
 <td colspan="2">AIRPORT INFORMATION</td>
</tr>
<tr>
 <td colspan="2" class="mid"><iframe id="airportLookup" style="width:97%; height:680px; scrolling:auto;" src="http://www.theairdb.com/airport/${apCode}.html"></iframe></td>
</tr>
</c:if>
</content:secure>
</c:if>
<%@ include file="/jsp/auditLog.jspf" %>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE AIRPORT PROFILE" />&nbsp;<el:cmdbutton url="airportdelete" linkID="${airport.IATA}" label="DELETE AIRPORT" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<c:if test="${googleMap}">
<script async>
<map:point var="golgotha.local.mapC" point="${airport}" />
<map:marker var="apMarker" point="${airport}" color="green" />

// Build the map
const mapOpts = {center:golgotha.local.mapC, zoom:6, scrollwheel:false, streetViewControl:false, clickableIcons:false, mapTypeControlOptions:{mapTypeIds:golgotha.maps.DEFAULT_TYPES}};
const map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
map.setMapTypeId(google.maps.MapTypeId.SATELLITE);
apMarker.setMap(map);
</script></c:if>
</body>
</html>
