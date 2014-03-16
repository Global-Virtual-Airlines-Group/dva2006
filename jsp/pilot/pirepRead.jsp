<!DOCTYPE html>
<%@ page session="false" %>
<%@ page buffer="32kb" autoFlush="true" %> 
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Flight Report - ${pirep.flightCode}</title>
<content:css name="main" />
<content:css name="form" />
<content:sysdata var="airlineURL" name="airline.url" />
<meta property="og:title" content="<content:airline /> Flight Report - ${pirep.flightCode}" />
<meta property="og:type" content="game.achievement" />
<meta property="og:url" content="http://${airlineURL}/pirep.do?id=${pirep.hexID}" />
<meta property="og:site_name" content="<content:airline />" />
<meta property="game:points" content="1" />
<content:pics />
<content:js name="common" />
<content:googleAnalytics eventSupport="true" />
<c:if test="${googleMap}">
<map:api version="3" /></c:if>
<c:if test="${scoreCR || access.canDispose}">
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;

// Validate form
var act = form.action;
var isCR = (act.indexOf('release.do') == -1) && (act.indexOf('updrwy.do') == -1);
if (isCR) {
	if (!validateCheckBox(form.crApprove, 1, 'Check Ride status')) return false;
	if (!validateCheckBox(form.frApprove, 1, 'Flight Report status')) return false;
}

setSubmit();
disableButton('CRButton');
return true;
}
</script></c:if>
<c:if test="${isACARS}">
<content:googleJS />
<content:json />
<content:js name="acarsFlightMap" />
<script type="text/javascript">
function zoomTo(lat, lng, zoom)
{
map.setZoom((zoom == null) ? 12 : zoom);
map.panTo(new google.maps.LatLng(lat, lng));
return true;
}
<content:filter roles="PIREP,HR,Developer,Operations">
function showRunwayChoices()
{
var URLflags = 'height=330,width=690,menubar=no,toolbar=no,status=no,scrollbars=yes,resizable=no';
window.open('/rwychoices.do?id=${pirep.hexID}', 'rwyChoices', URLflags);
return true;
}
</content:filter> 
</script></c:if>
</head>
<content:copyright visible="false" />
<body onload="initLinks()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<c:set var="act" value="pirep.do" scope="page" />
<c:set var="lnk" value="${pirep}" scope="page" />
<c:set var="validation" value="return validate(this)" scope="page" /> 
<c:choose>
<c:when test="${scoreCR && extPIREP}">
<c:set var="act" value="extpirepscore.do" scope="page" />
<c:set var="lnk" value="${checkRide}" scope="page" />
</c:when>
<c:when test="${scoreCR}">
<c:set var="act" value="pirepscore.do" scope="page" />
</c:when>
<c:when test="${!access.canDispose}">
<c:set var="validation" value="return false" scope="page" />
</c:when>
</c:choose>
<el:form action="${act}" method="post" link="${lnk}" validate="${validation}">
<el:table className="form">
<!-- PIREP Title Bar -->
<tr class="title">
 <td class="caps" colspan="2">FLIGHT ${pirep.flightCode} FLOWN ON <fmt:date fmt="d" date="${pirep.date}" /> by 
 <el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
</tr>

<!-- Pirep Data -->
<tr>
 <td class="label">Pilot Code / Rank</td>
 <td class="data"><c:if test="${!empty pilot.pilotCode}">${pilot.pilotCode} </c:if>(${pilot.rank.name}, ${pilot.equipmentType})
 - <el:cmd url="logbook" link="${pilot}">VIEW LOG BOOK</el:cmd></td>
</tr>
<content:filter roles="HR,PIREP,Examination,Operations">
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><a href="mailto:${pilot.email}">${pilot.email}</a></td>
</tr>
<c:if test="${fn:network(pirep) == 'VATSIM'}">
<c:set var="vatsimID" value="${fn:networkID(pilot, 'VATSIM')}" scope="page" />
<c:if test="${!empty vatsimID}">
<tr>
 <td class="label">VATSIM ID</td>
 <td class="data"><span class="bld">${vatsimID}</span><c:if test="${empty onlineTrack}"> - View flight log at 
 <el:link url="http://www.vataware.com/pilot.cfm?cid=${fn:networkID(pilot,'VATSIM')}" external="true">
<el:img src="vataware.png" className="noborder" x="50" y="16" caption="View VATAWARE Flight Log" /></el:link></c:if></td>
</tr>
</c:if>
</c:if>
</content:filter>
<tr>
 <td class="label">Status</td>
 <td class="data bld sec">${statusMsg}<c:if test="${!empty pirep.disposedOn}"> on <fmt:date date="${pirep.disposedOn}" /></c:if> 
<c:if test="${fn:AssignID(pirep) > 0}"> <span class="ter bld">FLIGHT ASSIGNMENT</span></c:if>
<c:if test="${fn:isDraft(pirep) && (empty pirep.route)}"> - <el:cmd url="routeplot" link="${pirep}">Plot Route</el:cmd></c:if></td>
</tr>
<c:if test="${!empty pirep.submittedOn}">
<tr>
 <td class="label">Submitted on</td>
 <td class="data"><fmt:date date="${pirep.submittedOn}" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Airline Name</td>
 <td class="data">${pirep.airline.name}</td>
</tr>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data">${pirep.equipmentType}</td>
</tr>
<tr>
 <td class="label">Departed from</td>
 <td class="data">${pirep.airportD.name} (<fmt:airport airport="${pirep.airportD}" />)</td>
</tr>
<c:if test="${isACARS && (!empty flightInfo.SID)}">
<tr>
 <td class="label">Departure Route</td>
 <td class="data">${flightInfo.SID.name}.${flightInfo.SID.transition}<content:filter roles="Developer">.${flightInfo.SID.runway}</content:filter></td>
</tr>
</c:if>
<c:set var="isDivert" value="${isACARS && (flightInfo.airportA.ICAO != pirep.airportA.ICAO)}" scope="page" />
<tr>
 <td class="label">Arrived at</td>
 <td class="data">${pirep.airportA.name} (<fmt:airport airport="${pirep.airportA}" />)
<c:if test="${isDivert}"> <span class="data warn caps bld">Originally filed to ${flightInfo.airportA.name} (<fmt:airport airport="${flightInfo.airportA}" />)</span></c:if></td>
</tr>
<c:if test="${isACARS && !isDivert && (!empty flightInfo.STAR)}">
<tr>
 <td class="label">Arrival Route</td>
 <td class="data">${flightInfo.STAR.name}.${flightInfo.STAR.transition}<content:filter roles="Developer">.${flightInfo.STAR.runway}</content:filter></td>
</tr>
</c:if>
<c:if test="${isACARS && (!empty flightInfo.airportL)}">
<tr>
 <td class="label">Alternate</td>
 <td class="data">${flightInfo.airportL.name} (<fmt:airport airport="${flightInfo.airportL}" />)</td>
</tr>
</c:if>
<tr>
 <td class="label">Simulator</td>
 <td class="data sec bld">${pirep.FSVersion.name}</td>
</tr>
<c:if test="${access.canDispose && fn:isOnline(pirep)}">
<tr>
 <td class="label">Online Flight</td>
 <td class="data"><el:check type="radio" name="network" idx="*" width="70" firstEntry="Offline" options="${networks}" value="${fn:network(pirep)}" /></td>
</tr>
</c:if>
<tr>
 <td class="label top">Other Information</td>
 <td class="data"><c:if test="${fn:isOnline(pirep) && !access.canDispose}">Flight Leg flown online using the ${fn:network(pirep)} network<br /></c:if>
<c:if test="${isACARS && !isXACARS}">
<div class="ok bld caps">Flight Leg data logged using <content:airline /> ACARS</div>
</c:if>
<c:if test="${isXACARS}">
<div class="ok bld caps">Flight Leg data logged using XACARS</div>
</c:if>
<c:if test="${fn:isDispatch(pirep)}">
<div class="pri bld caps">Flight Leg planned using <content:airline /> Dispatch</div>
</c:if>
<c:if test="${!fn:isRated(pirep)}">
<div class="error bld caps">Flight Leg flown without Aircraft type rating</div>
</c:if>
<c:if test="${fn:routeWarn(pirep)}">
<div class="error bld caps">Flight Route not found in <content:airline /> schedule</div>
</c:if>
<c:if test="${fn:rangeWarn(pirep)}">
<div class="error bld caps">Flight Distance outside Aircraft Range</div>
</c:if>
<c:if test="${fn:rwyWarn(pirep)}">
<div class="warn bld caps">Insufficient Runway Length</div>
</c:if>
<c:if test="${fn:etopsWarn(pirep)}">
<div class="error bld caps">Non-ETOPS Aircraft used on ETOPS route</div>
</c:if>
<c:if test="${fn:timeWarn(pirep)}">
<div class="warn bld caps">Flight Length outside Schedule Guidelines</div>
</c:if>
<c:if test="${fn:weightWarn(pirep)}">
<div class="warn bld caps">Excesive Aircraft Weight Detected</div>
</c:if>
<c:if test="${fn:refuelWarn(pirep)}">
<div class="warn bld caps">In-Flight Refueling Detected</div>
</c:if>
<c:if test="${fn:isCharter(pirep)}">
<div class="pri bld caps">Flight operated as a <content:airline /> Charter</div>
</c:if>
<c:if test="${fn:isHistoric(pirep)}">
<div class="ter bld caps">Flight operated as part of the <content:airline /> Historic program</div>
</c:if>
<c:if test="${fn:isPromoLeg(pirep)}">
<div class="ter bld caps">Flight Leg counts towards promotion to Captain in the <fmt:list value="${pirep.captEQType}" delim=", " /></div>
</c:if>
<c:if test="${!empty event}">
<div class="pri bld caps">Flight Leg part of the ${event.name} Online Event</div>
</c:if>
<c:if test="${fn:isAcademy(pirep)}">
<div class="pri bld caps">Flight Leg part of the <content:airline /> Flight Academy</div>
</c:if>
 </td>
</tr>
<tr>
 <td class="label">Flight Distance</td>
 <td class="data pri bld"><fmt:distance value="${pirep.distance}" longUnits="true" /></td>
</tr>
<tr>
 <td class="label">Logged Time</td>
 <td class="data"><fmt:dec value="${pirep.length / 10.0}" /> hours<c:if test="${avgTime > 0}">
 <span class="ita">(average time: <fmt:dec value="${avgTime / 10.0}" /> hours)</span></c:if></td>
</tr>
<c:if test="${!empty onlineTrack}">
<c:set var="onlinePct" value="${onlineTime * 100 / (pirep.length * 360)}" scope="page" />
<c:set var="onlinePct" value="${(onlinePct > 100) ? 100 : onlinePct}" scope="page" />
<c:set var="onlinePctClass" value="${(onlinePct < 50) ? 'warn bld' : 'visible'}" scope="page" />
<tr>
 <td class="label">Estimated Online Time</td>
 <td class="data"><fmt:int value="${onlineTime / 3600}" />:<fmt:int value="${(onlineTime % 3600) / 60}" fmt="00" />,
 <span class="${onlinePctClass}">(<fmt:dec value="${onlinePct}" fmt="#00.0" />% of flight)</span></td>
</tr>
</c:if>
<c:if test="${pirep.passengers > 0}">
<tr>
 <td class="label">Passengers Carried</td>
 <td class="data"><fmt:int value="${pirep.passengers}" /> passengers (<fmt:dec value="${pirep.loadFactor * 100.0}" fmt="##0.00" />% full)</td>
</tr>
</c:if>
<c:if test="${!isACARS && (!empty pirep.route)}">
<tr>
 <td class="label">Flight Route</td>
 <td class="data"><fmt:text value="${pirep.route}" /></td>
</tr>
</c:if>
<c:if test="${!empty pirep.remarks}">
<tr>
 <td class="label top">Pilot Comments</td>
 <td class="data"><fmt:text value="${pirep.remarks}" /></td>
</tr>
</c:if>
<c:if test="${isACARS}">
<c:set var="cspan" value="1" scope="request" />
<%@ include file="/jsp/pilot/pirepACARS.jspf" %>
</c:if>
<content:browser human="true">
<c:if test="${googleMap}">
<tr>
 <td class="label">Route Map Data</td>
 <td class="data"><span class="bld">
<c:if test="${isACARS || (!empty mapRoute)}"><el:box name="showRoute" idx="*" onChange="void toggleMarkers(map, 'gRoute', this)" label="Route" checked="${!isACARS}" /> </c:if>
<c:if test="${isACARS}"><el:box name="showFDR" idx="*" onChange="void toggleMarkers(map, 'routeMarkers', this)" label="Flight Data" checked="false" /> </c:if>
<c:if test="${!empty filedRoute}"><el:box name="showFPlan" idx="*" onChange="void toggleMarkers(map, 'gfRoute', this)" label="Flight Plan" checked="true" /> </c:if>
<el:box name="showFPMarkers" idx="*" onChange="void toggleMarkers(map, 'filedMarkers', this)" label="Navaid Markers" checked="true" />
<c:if test="${!empty onlineTrack}"> <el:box name="showOTrack" idx="*" onChange="void toggleMarkers(map, 'otRoute', this)" label="Online Track" checked="false" />
 <el:box name="showOMarkers" idx="*" onChange="void toggleMarkers(map, 'otMarkers', this)" label="Online Data" checked="false" />
 <el:box name="showFIRs" idx="*" onChange="void toggleObject(map, firKML, this)" label="FIR Boundaries" checked="false" /></c:if>
</span></td>
</tr>
<tr>
 <td class="label top">Route Map</td>
 <td class="data"><map:div ID="googleMap" x="100%" y="550" /></td>
</tr>
</c:if>
<c:if test="${!googleMap}">
<tr>
 <td class="label top">Route Map</td>
 <td class="data"><img src="http://maps.fallingrain.com/perl/map.cgi?x=620&y=365&kind=topo&lat=${pirep.airportD.latitude}&long=${pirep.airportD.longitude}&name=${pirep.airportD.name}&c=1&lat=${pirep.airportA.latitude}&long=${pirep.airportA.longitude}&name=${pirep.airportA.name}&c=1"
alt="${pirep.airportD.name} to ${pirep.airportA.name}" width="620" height="365" /></td>
</tr>
</c:if>
<c:if test="${isACARS}">
<tr class="title">
 <td colspan="2">SPEED / ALTITUDE DATA<span id="chartToggle" class="und" style="float:right" onclick="void toggleExpand(this, 'flightDataChart')">COLLAPSE</span></td>
</tr>
<tr class="flightDataChart">
 <td class="label">&nbsp;</td>
 <td class="data" colspan="${cspan}"><div id="flightChart" style="height:275px"></div></td>
</tr>
</c:if>
</content:browser>
<c:if test="${!scoreCR && (access.canDispose || ((access.canViewComments || access.canUpdateComments) && (!empty pirep.comments)))}">
<tr>
 <td class="label top">Reviewer Comments</td>
<c:if test="${access.canDispose || access.canUpdateComments}">
 <td class="data"><el:textbox name="dComments" width="100" height="5">${pirep.comments}</el:textbox></td>
</c:if>
<c:if test="${!access.canDispose && access.canViewComments}">
 <td class="data"><fmt:msg value="${pirep.comments}" bbCode="true" /></td>
</c:if>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;
<c:if test="${access.canSubmit}">
 <el:cmdbutton url="submit" link="${pirep}" label="SUBMIT FLIGHT REPORT" />
</c:if>
<c:if test="${access.canApprove && !scoreCR}">
 <el:cmdbutton url="dispose" link="${pirep}" op="approve" post="true" key="A" label="APPROVE" />
</c:if>
<c:if test="${access.canHold}">
 <el:cmdbutton url="dispose" link="${pirep}" op="hold" post="true" key="H" label="HOLD" />
</c:if>
<c:if test="${access.canRelease}">
 <el:cmdbutton url="release" link="${pirep}" post="true" label="RELEASE HOLD" />
</c:if>
<c:if test="${access.canReject && (!fn:isCheckFlight(pirep) || !fn:pending(checkRide))}">
 <el:cmdbutton url="dispose" link="${pirep}" op="reject" post="true" label="REJECT" />
<c:if test="${isACARS && (empty checkRide)}"><content:filter roles="HR,PIREP,Operations">
 <el:cmdbutton url="crflag" link="${pirep}" label="MARK AS CHECK RIDE" />
</content:filter></c:if>
</c:if>
<c:if test="${access.canDispose && (empty checkRide)}">
<c:set var="bLabel" value="${(fn:sizeof(pirep.captEQType) == 0) ? 'SET' : 'CLEAR'}" scope="page" />
 <el:cmdbutton url="promotoggle" link="${pirep}" label="${bLabel} PROMOTION FLAG" />
</c:if>
<c:if test="${access.canEdit}">
 <el:cmdbutton url="pirep" link="${pirep}" op="edit" key="E" label="EDIT REPORT" />
</c:if>
<c:if test="${access.canDelete}">
 <el:cmdbutton url="pirepdelete" link="${pirep}" label="DELETE REPORT" />
<c:if test="${isACARS}">
 <el:cmdbutton url="acarsdelete" link="${pirep}" label="DELETE ACARS DATA" />
</c:if> </c:if>
<content:filter roles="PIREP,HR,Developer,Operations">
<c:if test="${isACARS}">
 <el:button label="RUNWAY CHOICES" key="R" onClick="void showRunwayChoices()" />
 <el:cmdbutton url="gaterecalc" link="${pirep}" label="LOAD GATES" />
</c:if>
</content:filter>
<c:if test="${fn:isDraft(pirep) && (!empty assignmentInfo) && assignAccess.canRelease}">
 <el:cmdbutton url="assignrelease" link="${assignmentInfo}" label="RELEASE ASSIGNMENT" />
</c:if>
<c:if test="${access.canUpdateComments}">
 <el:cmdbutton url="updcomments" link="${pirep}" post="true" label="UPDATE COMMENTS" /> 
</c:if></td>
</tr>
</el:table>
</el:form>
<content:copyright />
</content:region>
</content:page>
<content:browser human="true">
<c:if test="${googleMap}">
<script type="text/javascript">
<map:point var="mapC" point="${mapCenter}" />

// Create map options
var mapTypes = {mapTypeIds: golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:mapC, minZoom:2, maxZoom:18, zoom:getDefaultZoom(${pirep.distance}), scrollwheel:false, streetViewControl:false, mapTypeControlOptions: mapTypes};

// Build the map
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
google.maps.event.addListener(map, 'click', function() { map.infoWindow.close(); });

// Build the route line and map center
<c:if test="${!empty mapRoute}">
<map:points var="routePoints" items="${mapRoute}" />
<map:line var="gRoute" src="routePoints" color="#4080af" width="3" transparency="0.75" geodesic="true" />
</c:if>
<c:if test="${empty mapRoute && isACARS}">
var gRoute;
var routePoints = [];
var routeMarkers = [];
getACARSData(${fn:ACARS_ID(pirep)}, ${access.canApprove});
google.maps.event.addListener(map.infoWindow, 'closeclick', function() { removeMarkers('selectedFIRs'); });
</c:if>
<c:if test="${!empty filedRoute}">
<map:points var="filedPoints" items="${filedRoute}" />
<map:markers var="filedMarkers" items="${filedRoute}" />
<map:line var="gfRoute" src="filedPoints" color="#80800f" width="2" transparency="0.5" geodesic="true" />
</c:if>
<c:if test="${!empty onlineTrack}">
<map:points var="onlinePoints" items="${onlineTrack}" />
<map:markers var="otMarkers" items="${onlineTrack}" />
<map:line var="otRoute" src="onlinePoints" color="#f06f4f" width="3" transparency="0.55" geodesic="true" />
var firKML = new google.maps.KmlLayer(self.location.protocol + '//' + self.location.host + '/servinfo/firs.kmz', {preserveViewport:true, clickable:false});
</c:if>
<c:if test="${!empty mapRoute}">
// Add the route and markers
addMarkers(map, 'gRoute');
</c:if>
<c:if test="${!empty filedRoute}">
addMarkers(map, 'gfRoute');
addMarkers(map, 'filedMarkers');
</c:if>
<c:if test="${empty filedRoute}">
// Airport markers
<map:marker var="gmA" point="${pirep.airportA}" />
<map:marker var="gmD" point="${pirep.airportD}" />
var filedMarkers = [gmA, gmD];
addMarkers(map, 'filedMarkers');
</c:if>
<c:if test="${isACARS}">
google.load('visualization','1.0',{'packages':['corechart']});
google.setOnLoadCallback(function() {
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'pirepstats.ws?id=${pirep.hexID}', true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	if (xmlreq.status != 200) {
		displayObject(document.getElementById('flightDataChart'),false);
		return false;
	}

	var lgStyle = {color:'black',fontName:'Verdana',fontSize:8};
	var statsData = JSON.parse(xmlreq.responseText); var sd = statsData.data;
	for (var x = 0; x < sd.length; x++)
		sd[x][0] = new Date(sd[x][0]);

	// Build the chart
	var chart = new google.visualization.ComboChart(document.getElementById('flightChart'));
	var data = new google.visualization.DataTable();
	data.addColumn('datetime', 'Date/Time');
	data.addColumn('number', 'Ground Speed');
	data.addColumn('number', 'Altitude');
	<c:if test="${!isXACARS}">
	data.addColumn('number', 'Ground Elevation');</c:if>
	data.addRows(sd);
	
	// Read CSS selectors for graph lines
	var pr = '#0000a1'; var sc = '#008080';
	if (!golgotha.util.oldIE) {
		for (var x = 0; x < document.styleSheets.length; x++) {
			var ss = document.styleSheets[x];
			if ((ss.href == null) || (ss.href.indexOf('main.css') == -1)) continue;
			for (var y = 0; y < ss.cssRules.length; y++) {
				var cs = ss.cssRules[y];
				if (cs.selectorText) {
					if (cs.selectorText.indexOf('.pri') > -1)
						pr = cs.style.color;
					else if (cs.selectorText.indexOf('.sec') > -1)
						sc = cs.style.color;
				}
			}
		}
	}
	
	// Create formatting options
	var ha = {gridlines:{count:10},minorGridlines:{count:5},title:'Date/Time',textStyle:lgStyle};
	var va0 = {maxValue:statsData.maxAlt,title:'Altitude',textStyle:lgStyle};
	var va1 = {maxValue:statsData.maxSpeed,gridlines:{count:5},title:'Speed',textStyle:lgStyle};
	var s = [{},{targetAxisIndex:1},{targetAxisIndex:1,type:'area',areaOpacity:0.7}];
	chart.draw(data,{series:s,vAxes:[va1,va0],hAxis:ha,fontName:'Tahoma',fontSize:10,colors:[pr,sc,'#b8b8d8']});
<c:if test="${access.canApprove}">toggleExpand(document.getElementById('chartToggle'), 'flightDataChart');</c:if>	
	return true;
}

xmlreq.send(null);
return true;
});
</c:if>
// Update text color
google.maps.event.trigger(map, 'maptypeid_changed');
</script>
</c:if></content:browser>
</body>
</html>
