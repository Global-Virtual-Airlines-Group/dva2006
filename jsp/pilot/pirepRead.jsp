<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" buffer="32kb" autoFlush="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Flight Report - ${pirep.flightCode} (<fmt:date date="${pirep.date}" fmt="d" />)</title>
<content:expire expires="${pirep.status.isComplete ? 90 : 5}" />
<content:canonical convertID="true" />
<content:css name="main" />
<content:css name="form" />
<content:sysdata var="airlineURL" name="airline.url" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<c:if test="${access.canUseSimBrief}">
<content:js name="simBrief" />
<c:if test="${empty sbPackage}">
<content:js name="simbrief.apiv1" /></c:if>
</c:if>
<content:captcha action="pirep" authOnly="true" />
<content:googleAnalytics eventSupport="true" />
<content:browser human="true"><c:if test="${googleMap}">
<map:api version="3" callback="golgotha.local.mapInit" /></c:if></content:browser>
<c:if test="${scoreCR || access.canDispose}">
<content:sysdata var="reviewDelay" name="users.pirep.review_delay" default="0" />
<content:empty var="emptyList" />
<c:set var="hasDelay" value="${reviewDelay > 0}" scope="page" />
<script async>
golgotha.local.startTime = new Date();
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	const act = f.action;
	if ((act.indexOf('release.do') == -1) && (act.indexOf('updrwy.do') == -1)) {
		const now = new Date(); golgotha.local.startTime = golgotha.local.startTime || now;
		golgotha.form.validate({f:f.crApprove, min:1, t:'Check Ride status'});
		golgotha.form.validate({f:f.frApprove, min:1, t:'Flight Report status'});
		f.reviewTime.value = now.getTime() - golgotha.local.startTime.getTime();
	}

	golgotha.form.submit(f);
	return true;
};

golgotha.local.loadLogbook = function() {
	const d = new Date();
	const p = fetch('logpreload.ws?id=${pirep.authorID}');
	p.then(function(rsp) {
		if (!rsp.ok) {
			console.log('Error ' + rsp.status + ' preloading Log Book');
			return false;
		}

		rsp.text(); const d2 = new Date(); const ms = d2.getTime() - d.getTime();
		const isHit = (rsp.headers.get('X-Cache-Hit') == '1');
		console.log('Preloaded ' + rsp.headers.get('X-Logbook-Size') + ' Flights, hit=' + isHit + ' (' + ms + 'ms)');
	});
};

golgotha.onDOMReady(golgotha.local.loadLogbook);
<c:if test="${hasDelay}">
golgotha.local.enableButtons = function() {
	const btns = golgotha.util.getElementsByClass('timedButton','input',document.forms[0]);
	btns.forEach(function(bt) { golgotha.util.disable(bt,false); });
	return true;
}

golgotha.onDOMReady(function() { window.setTimeout(golgotha.local.enableButtons, ${(fn:hasSDK(pirep) || !isACARS) ? 1250 : reviewDelay}); });</c:if>
</script></c:if>
<c:if test="${isACARS && googleMap}">
<content:googleJS module="charts" />
<content:js name="acarsFlightMap" />
<script async>
<c:if test="${googleMap}">
golgotha.local.zoomTo = function(lat, lng, zoom) {
	map.setZoom((zoom == null) ? 12 : zoom);
	map.panTo({lat:lat,lng:lng});
	return true;
};</c:if>
<content:filter roles="PIREP,HR,Developer,Operations">
<map:point var="golgotha.local.landing" point="${pirep.landingLocation}" />
golgotha.local.showRunwayChoices = function() {
	return window.open('/rwychoices.do?id=${pirep.hexID}', 'rwyChoices', 'height=360,width=770,menubar=no,toolbar=no,status=no,scrollbars=yes');
};
</content:filter>
</script></c:if>
<c:if test="${!empty eliteLevel}">
<style type="text/css">
table.form td.eliteStatus {
	color: #ffffff;
	background-color: #${eliteLevel.hexColor};
}
</style></c:if>
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:set var="act" value="pirep.do" scope="page" />
<c:set var="lnk" value="${pirep}" scope="page" />
<c:set var="validation" value="return golgotha.form.wrap(golgotha.local.validate, this)" scope="page" /> 
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
<tr class="title">
 <td class="caps" colspan="2">FLIGHT ${pirep.flightCode}&nbsp;<c:if test="${!fn:isDraft(pirep)}">FLOWN ON <fmt:date fmt="d" date="${pirep.date}" /></c:if><span class="nophone"> by <el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></span></td>
</tr>

<!-- Pirep Data -->
<tr>
 <td class="label">Pilot Code / Rank</td>
 <td class="data"><c:if test="${!empty pilot.pilotCode}">${pilot.pilotCode}&nbsp;</c:if>(${pilot.rank.name}, ${pilot.equipmentType}) - <el:cmd url="logbook" link="${pilot}">VIEW LOG BOOK</el:cmd></td>
</tr>
<content:filter roles="HR,PIREP,Examination,Operations">
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><a href="mailto:${pilot.email}">${pilot.email}</a></td>
</tr>
<c:if test="${fn:network(pirep) == 'VATSIM'}">
<c:set var="vatsimID" value="${fn:externalID(pilot, 'VATSIM')}" scope="page" />
<c:if test="${!empty vatsimID}">
<tr>
 <td class="label">VATSIM ID</td>
 <td class="data"><span class="bld">${vatsimID}</span><c:if test="${empty onlineTrack}"> - View flight log at <el:link url="https://vatstats.net/pilots/${vatsimID}" target="_new" external="true">VATSTATS</el:link></c:if></td>
</tr>
</c:if>
</c:if>
</content:filter>
<tr>
 <td class="label">Status</td>
 <td class="data bld"><span class="sec">${pirep.status}</span><c:if test="${!empty disposedBy}"> - by ${disposedBy.name}</c:if><c:if test="${!empty pirep.disposedOn}"> on <fmt:date date="${pirep.disposedOn}" /></c:if> 
<c:if test="${fn:AssignID(pirep) > 0}">&nbsp;<span class="ter bld">FLIGHT ASSIGNMENT</span></c:if>
<content:authUser anonymous="false">
<c:if test="${fn:isDraft(pirep)}"> - <el:cmd url="routeplot" link="${pirep}">Plot Route</el:cmd>
<c:if test="${!empty pirep.route && (empty sbPackage.flightPlans)}"> - <a href="draftplan.ws?id=${pirep.hexID}" rel="nofollow">Download Flight Plan</a></c:if>
</c:if></content:authUser></td>
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
 <td class="data">${pirep.airportD.name} (<el:cmd url="airportinfo" target="airportInfo" linkID="${pirep.airportD.IATA}" authOnly="true" className="plain"><fmt:airport airport="${pirep.airportD}" /></el:cmd>)</td>
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
 <td class="data">${pirep.airportA.name} (<el:cmd url="airportinfo" target="airportInfo" linkID="${pirep.airportA.IATA}" authOnly="true" className="plain"><fmt:airport airport="${pirep.airportA}" /></el:cmd>)
<c:if test="${isDivert}">&nbsp;<span class="data warn caps bld">Originally filed to ${flightInfo.airportA.name} (<fmt:airport airport="${flightInfo.airportA}" />)</span></c:if></td>
</tr>
<c:if test="${isACARS && !isDivert && (!empty flightInfo.STAR)}">
<tr>
 <td class="label">Arrival Route</td>
 <td class="data">${flightInfo.STAR.name}.${flightInfo.STAR.transition}<content:filter roles="Developer">.${flightInfo.STAR.runway}</content:filter></td>
</tr>
</c:if>
<c:if test="${isACARS && (!empty flightInfo.airportL) && (flightInfo.airportL.ICAO != pirep.airportA.ICAO)}">
<tr>
 <td class="label">Alternate</td>
 <td class="data">${flightInfo.airportL.name} (<el:cmd url="airportinfo" linkID="${flightInfo.airportL.IATA}" className="plain"><fmt:airport airport="${flightInfo.airportL}" /></el:cmd>)</td>
</tr>
</c:if>
<c:if test="${!fn:isDraft(pirep)}">
<tr>
 <td class="label">Simulator</td>
 <td class="data sec bld">${pirep.simulator.name}<c:if test="${flightInfo.simMajor > 1}">&nbsp;<content:simVersion sim="${pirep.simulator}" major ="${flightInfo.simMajor}" minor="${flightInfo.simMinor}" /></c:if></td>
</tr>
</c:if>
<c:if test="${access.canDispose && fn:isOnline(pirep)}">
<tr>
 <td class="label">Online Flight</td>
 <td class="data"><el:check type="radio" name="network" idx="*" width="84" firstEntry="Offline" options="${networks}" value="${fn:network(pirep)}" /></td>
</tr>
</c:if>
<c:if test="${access.canAdjustEvents}">
<c:if test="${fn:isOnline(pirep) && (empty event) && (!empty possibleEvents)}">
<tr>
 <td class="label">Online Event</td>
 <td class="data"><el:combo name="onlineEvent" size="1" firstEntry="-" options="${possibleEvents}" /> <el:cmdbutton url="updevent" post="true" link="${pirep}" label="UPDATE ONLINE EVENT" /></td>
</tr>
</c:if>
<c:if test="${(empty tour) && (!empty possibleTours)}">
<tr>
 <td class="label">Flight Tour</td>
 <td class="data"><el:combo name="flightTour" size="1" firstEntry="-" options="${possibleTours}" /> <el:cmdbutton url="updtour" post="true" link="${pirep}" label="UPDATE FLIGHT TOUR" /></td>
</tr>
</c:if>
</c:if>
<tr>
 <td class="label top">Other Information</td>
 <td class="data"><c:if test="${fn:isOnline(pirep) && !access.canDispose}">Flight Leg flown online using the ${fn:network(pirep)} network<br /></c:if>
<c:if test="${isACARS && !isXACARS && !isSimFDR}">
<div class="ok bld caps">Flight Leg data logged using <content:airline /> ACARS</div></c:if>
<c:if test="${isXACARS}">
<div class="ok bld caps">Flight Leg data logged using XACARS</div></c:if>
<c:if test="${isSimFDR}">
<div class="ok bld">FLIGHT LEG DATA LOGGED USING simFDR</div></c:if>
<c:if test="${fn:isDispatch(pirep)}">
<div class="pri bld caps">Flight Leg planned using <content:airline /> Dispatch</div></c:if>
<c:if test="${fn:isSimBrief(pirep)}">
<div class="bld caps">Flight Leg planned using SimBrief</div></c:if>
<c:if test="${isDivert}">
<div class="warn bld caps">Flight diverted to Non-Scheduled Airport</div></c:if>
<c:if test="${fn:isDivert(pirep) && !isDivert}">
<div class="ter bld caps">Flight Leg is completion of Diverted Flight</div></c:if>
<c:if test="${!fn:isRated(pirep)}">
<div class="error bld caps">Flight Leg flown without Aircraft type rating</div></c:if>
<c:if test="${fn:routeWarn(pirep)}">
<div class="error bld caps">Flight Route not found in <content:airline /> schedule</div></c:if>
<c:if test="${fn:rangeWarn(pirep)}">
<div class="error bld caps">Flight Distance outside Aircraft Range</div></c:if>
<c:if test="${fn:rwyWarn(pirep)}">
<div class="warn bld caps">Insufficient Runway Length</div></c:if>
<c:if test="${fn:airspaceWarn(pirep)}">
<div class="error bld caps">Flight flown through Prohibited/Restricted Airspace</div></c:if>
<c:if test="${fn:etopsWarn(pirep)}">
<c:if test="${fn:isDraft(pirep)}">
<div class="warn bld caps">Planned Route exceeds Aircraft ETOPS rating</div></c:if>
<c:if test="${!fn:isDraft(pirep)}">
<div class="error bld caps">Non-ETOPS Aircraft used on ETOPS route</div></c:if></c:if>
<c:if test="${fn:timeWarn(pirep)}">
<div class="warn bld caps">Flight Length outside Schedule Guidelines</div></c:if>
<c:if test="${fn:weightWarn(pirep)}">
<div class="warn bld caps">Excessive Aircraft Weight Detected</div></c:if>
<c:if test="${fn:refuelWarn(pirep)}">
<div class="warn bld caps">In-Flight Refueling Detected</div></c:if>
<c:if test="${fn:isCharter(pirep)}">
<div class="pri bld caps">Flight operated as a <content:airline /> Charter</div></c:if>
<c:if test="${fn:isHistoric(pirep)}">
<div class="ter bld caps">Flight operated as part of the <content:airline /> Historic program</div></c:if>
<c:if test="${fn:isPromoLeg(pirep)}">
<div class="ter bld caps">Flight Leg counts towards promotion to Captain in the <fmt:list value="${pirep.captEQType}" delim=", " /></div></c:if>
<c:if test="${!empty event}">
<div class="pri bld caps">Flight Leg part of the ${event.name} Online Event</div></c:if>
<c:if test="${!empty tour}">
<div class="ter bld caps">Flight Leg is Stage <fmt:int value="${tourIdx}" /> in the ${tour.name} Tour</div></c:if>
<c:if test="${fn:isAcademy(pirep)}">
<div class="pri bld caps">Flight Leg part of the <content:airline /> Flight Academy</div></c:if>
</td>
</tr>
<tr>
 <td class="label">Flight Distance</td>
 <td class="data pri bld"><fmt:distance value="${pirep.distance}" longUnits="true" /></td>
</tr>
<c:if test="${fn:isDraft(pirep)}">
<c:if test="${!empty pirep.gateD || !empty pirep.timeD}">
<tr>
 <td class="label">Scheduled Departure</td>
 <td class="data"><c:if test="${!empty pirep.gateD}">From <span class="ter bld">${pirep.gateD}</span></c:if><c:if test="${!empty pirep.gateD && !empty pirep.timeD}"> at </c:if>
<c:if test="${!empty pirep.timeD}"><fmt:date fmt="t" t="HH:mm" tz="${pirep.airportD.TZ}" date="${pirep.timeD}" /></c:if></td>
</tr>
</c:if>
<c:if test="${!empty pirep.gateA || !empty pirep.timeA}">
<tr>
 <td class="label">Scheduled Arrival</td>
 <td class="data"><c:if test="${!empty pirep.gateA}">At <span class="ter bld">${pirep.gateA}</span></c:if><c:if test="${!empty pirep.gateA && !empty pirep.timeA}"> at </c:if>
<c:if test="${!empty pirep.timeA}"><fmt:date fmt="t" t="HH:mm" tz="${pirep.airportA.TZ}" date="${pirep.timeA}" /></c:if></td>
</tr>
<c:if test="${!empty pirep.altitude}">
<tr>
 <td class="label">Planned Altitude</td>
 <td class="data">${pirep.altitude}</td>
</tr>
</c:if>
</c:if>
<c:if test="${pirep.duration.toSeconds() > 0}">
<tr>
 <td class="label">Scheduled Flight Time</td>
 <td class="data bld"><fmt:duration duration="${pirep.duration}" t="HH:mm" /><c:if test="${pirep.timeA.dayOfYear != pirep.timeD.dayOfYear}">&nbsp;<span class="small ter bld caps">Flight Arrives on <fmt:date date="${pirep.timeA}" fmt="d" /></span></c:if></td> 
</tr>
</c:if>
</c:if>
<tr>
 <td class="label">Customs Zone</td>
 <td class="data ter bld">${pirep.flightType.description}</td>
</tr>
<c:if test="${pirep.length > 0}">
<tr>
 <td class="label">Logged Time</td>
 <td class="data"><fmt:dec value="${pirep.length / 10.0}" /> hours<c:if test="${avgTime.toSeconds() > 0}">&nbsp;<span class="ita">(average time: <fmt:duration duration="${avgTime}" t="HH:mm" /> hours)</span></c:if></td>
</tr>
</c:if>
<c:if test="${!empty onlineTime}">
<c:set var="onlinePct" value="${onlineTime.seconds * 100 / (pirep.length * 360)}" scope="page" />
<c:set var="onlinePct" value="${(onlinePct > 100) ? 100 : onlinePct}" scope="page" />
<c:set var="onlinePctClass" value="${(onlinePct < 50) ? 'warn bld' : 'visible'}" scope="page" />
<tr>
 <td class="label">Estimated Online Time</td>
 <td class="data"><fmt:duration duration="${onlineTime}" />, <span class="${onlinePctClass}">(<fmt:dec value="${onlinePct}" fmt="#00.0" />% of flight)</span></td>
</tr>
</c:if>
<c:if test="${!empty networkOutages}">
<tr>
 <td class="label top">${pirep.network} Outages</td>
 <td class="data small"><c:forEach var="outage" items="${networkOutages}" varStatus="otStatus"><fmt:date date="${outage.startTime}" t="HH:mm" d="MM/dd" /> - <fmt:date date="${outage.endTime}" t="HH:mm" d="MM/dd" /><c:if test="${!otStatus.last}">, </c:if></c:forEach>
 <c:if test="${networkOutages.size() > 1}">&nbsp;<span class="ita bld">Total: <fmt:duration duration="${networkOutageTotal}" /></span></c:if></td>
</tr>
</c:if>
<c:if test="${pirep.passengers > 0}">
<tr>
 <td class="label">Passengers ${fn:isDraft(pirep) ? 'Booked' : 'Carried'}</td>
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
<c:if test="${(!empty onTimeRoute) && (onTimeRoute.totalLegs > 0)}">
<c:set var="otPct" value="${onTimeRoute.onTimeLegs / onTimeRoute.totalLegs}" scope="page" />
<c:choose>
<c:when test="${otPct >= 0.8}">
<c:set var="otClass" value="pri" scope="page" /></c:when>
<c:when test="${otPct < 0.5}">
<c:set var="otClass" value="err" scope="page" /></c:when>
<c:otherwise>
<c:set var="otClass" value="ter" scope="page" /></c:otherwise>
</c:choose>
</c:if>
<c:if test="${(!empty onTimeRoute) && (empty onTimeEntry) && (isACARS || fn:isDraft(pirep)) && (onTimeRoute.totalLegs > 0)}">
<tr>
 <td class="label">On-Time Statistics</td>
 <td class="data small"><fmt:int className="bld" value="${onTimeRoute.totalLegs}" /> Flights, <fmt:int className="pri bld" value="${onTimeRoute.onTimeLegs}" /> On Time
 <span class="${otClass} bld">(<fmt:dec value="${otPct}" fmt="##0.0%" />)</span></td>
</tr>
</c:if>
<c:if test="${isACARS}">
<c:set var="cspan" value="1" scope="request" />
<%@ include file="/jsp/pilot/pirepACARS.jspf" %>
</c:if>
<c:if test="${access.canUseSimBrief && (empty sbPackage)}">
<content:enum var="sbFmts" className="org.deltava.beans.simbrief.PackageFormat" />
<content:enum var="weightUnits" className="org.deltava.beans.WeightUnit" />
<tr class="title caps">
 <td colspan="2">SimBrief DISPATCH SETTINGS<span id="sbToggle" class="toggle" onclick="void golgotha.util.toggleExpand(this, 'sbData')">COLLAPSE</span></td>
</tr>
<tr class="sbData">
 <td class="label">Package Format</td>
 <td class="data"><el:combo name="sbFormat" size="1" options="${sbFmts}" value="DAL" /> - Weight unit: <el:check type="radio" cols="2" name="sbWeightUnit" options="${weightUnits}" value="${pilot.weightType}" /></td>
</tr>
<tr id="sbTailCode" class="sbData" style="display:none;">
 <td class="label">Tail Code</td>
 <td class="data"><el:combo name="tailCode" size="1" options="${emptyList}" firstEntry="[ SELECT AIRCRAFT ]" onChange="void golgotha.simbrief.sbAirframeUpdate(this)" />
 <span id="sbAirframe" style="display:none;"><el:box name="disableCustomAirframe" value="true" label="Disable custom SimBrief airframe lookup" onChange="golgotha.simbrief.sbCustomToggle(this)" /><span id="sbAirframeInfo"> - <span id="sbAirframeID" class="small ter ita"></span></span></span></td>
</tr>
<tr class="sbData">
 <td class="label">ETOPS Override</td>
 <td class="data"><el:combo name="etopsOV" size="1" options="${etopsOV}" value="${acPolicy.ETOPS.time}" /></td>
</tr>
<tr class="sbData">
 <td class="label">Cost Index</td>
 <td class="data"><el:text name="costIndex" size="2" max="3" value="80" /></td>
</tr>
<tr class="title sbData">
 <td colspan="2" class="mid"><el:button label="GENERATE DISPATCH PACKAGE" onClick="void void golgotha.simbrief.sbSubmit()" /></td>
</tr>
</c:if>
<c:if test="${(access.ourFlight || (access.canViewSimBrief && fn:isDraft(pirep))) && (!empty sbPackage)}">
<tr class="title caps">
 <td colspan="2">SimBrief BRIEFING PACKAGE INFORMATION<span id="sbToggle" class="toggle" onclick="void golgotha.util.toggleExpand(this, 'sbData')">COLLAPSE</span></td>
</tr>
<tr class="sbData">
 <td class="label">SimBrief Package</td>
 <td class="data">Created on <fmt:date date="${sbPackage.createdOn}" /> [v<fmt:int value="${sbPackage.releaseVersion}" />] (AIRAC <span class="sec bld">${sbPackage.AIRAC}</span>)<span class="nophone"> - <a href="sbpackage.ws?id=${pirep.hexID}" rel="nofollow" target="sbPakage" class="bld">Download SimBrief Package</a> 
<c:if test="${fn:isDraft(pirep)}"> | <a href="javascript:void golgotha.simbrief.sbRefresh()" rel="nofollow" class="bld">Refresh Package</a> | <a href="https://dispatch.simbrief.com/briefing/${sbPackage.requestID}" rel="nofollow" class="bld" target="sbpackage">View on SimBrief</a></c:if></span>
<span id="sbMessageBox" style="display:none" class="bld"> - <span id="sbMessage" class="error"></span></span></td>
</tr>
 <c:if test="${!empty sbPackage.tailCode}">
 <tr class="sbData">
  <td class="label">Aircraft</td>
  <td class="data"><span class="sec bld">${sbPackage.tailCode}</span><c:if test="${!empty sbPackage.airframeID}"> - <span class="ter ita">SimBrief airframe ID <span class="bld">${sbPackage.airframeID}</span></span></c:if></td>
</tr>
 </c:if>
<tr class="sbData">
  <td class="label">Briefing Format</td>
  <td class="data pri bld">${sbPackage.format.description}</td>
</tr>
<tr class="sbData">
 <td class="label">Fuel Load</td>
 <td class="data"><fmt:weight value="${sbPackage.taxiFuel}" /> / <fmt:weight value="${sbPackage.baseFuel}" /> / <fmt:weight value="${sbPackage.enrouteFuel}" /> / <fmt:weight value="${sbPackage.alternateFuel}" /> taxi / base / enroute / alternate <span class="ita">(<fmt:weight value="${sbPackage.totalFuel}" /> total)</span></td>
</tr>
<tr class="sbData">
 <td class="label">Payload</td>
 <td class="data"><fmt:int value="${sbPackage.pax}" /> passengers - <fmt:weight value="${sbPackage.baggageWeight}" /> baggage<c:if test="${sbPackage.cargoWeight > 0}">, <fmt:weight value="${sbPackage.cargoWeight}" /> additional cargo 
 <span class="ita">(<fmt:weight value="${sbPackage.baggageWeight + sbPackage.cargoWeight}" /> total)</span></c:if></td>
</tr>
<c:if test="${!empty sbPackage.alternates}">
<tr class="sbData">
<td class="label top">Alternates</td>
<td class="data"><c:forEach var ="ap" items="${sbPackage.alternates}" varStatus="aaStatus">
${ap.name} (<el:cmd url="airportinfo" linkID="${ap.IATA}"><fmt:airport airport="${ap}" /></el:cmd>)<span class="small"> - <fmt:distance value="${ap.distanceTo(pirep.airportA)}" /> from destination</span><c:if test="${!aaStatus.last}"><br /></c:if></c:forEach></td>
</tr>
</c:if>
<c:if test="${sbPackage.ETOPS.time > 75}" >
<tr class="sbData">
 <td class="label">ETOPS</td>
 <td class="data"><span class="sec bld">${sbPackage.ETOPS}</span> - <span class="ita">Alternates : </span><c:forEach var="ap" items="${sbPackage.ETOPSAlternates}" varStatus="hasNext">${ap.name} (<el:cmd url="airportinfo" target="airportInfo" className="plain" linkID="${ap.IATA}"><fmt:airport airport="${ap}" />)</el:cmd>
<c:if test="${!hasNext.last}">, </c:if></c:forEach></td>
</tr>
</c:if>
</c:if>
<content:authUser>
<content:sysdata var="eliteEnabled" name="econ.elite.enabled" />
<content:sysdata var="eliteName" name="econ.elite.name" />
<c:if test="${(eliteEnabled && pirep.status.isComplete) || (!empty eliteScore)}">
<tr class="title caps">
 <td class="eliteStatus" colspan="2"><content:airline />&nbsp;${eliteName} INFORMATION<span id="elToggle" class="toggle" onclick="void golgotha.util.toggleExpand(this, 'elData')">COLLAPSE</span></td>
</tr>
</c:if>
<c:choose>
<c:when test="${!empty eliteScore}">
<content:sysdata var="pointUnit" name="econ.elite.points" />
<content:sysdata var="distanceUnit" name="econ.elite.distance" />
<tr class="elData">
 <td class="label eliteStatus top">Mileage Information</td>
 <td class="data">Mileage accumulation: <fmt:int value="${eliteScore.points}" /> miles<c:if test="${!eliteScore.scoreOnly}"> / <span class="bld"><fmt:int value="${eliteScore.distance}" />&nbsp;${distanceUnit}</span></c:if>, Flown 
 as <fmt:elite className="bld" level="${eliteLevel}" nameOnly="true" /><c:if test="${access.canEliteRescore}"> - <el:cmd url="eliterescore" link="${pirep}" className="pri bld">RECACLCULATE</el:cmd></c:if><br />
<c:if test="${eliteScore.scoreOnly}"><span class="small error bld">This Flight Leg is not eligible to accumulate Flight Legs or ${distanceUnit} in the ${eliteName} Program</span><br /></c:if>
<hr />
<span class="small"><c:forEach var="esEntry" items="${eliteScore.entries}" varStatus="esStatus">
<fmt:int className="pri bld" value="${esEntry.points}" /> - ${esEntry.message}<c:if test="${esEntry.bonus}">&nbsp;<span class="ita">( BONUS )</span></c:if>
<c:if test="${!esStatus.isLast()}"><br /></c:if></c:forEach></span></td> 
</tr>
</c:when>
<c:when test="${eliteEnabled && (pirep.status == 'OK')}">
<tr class="elData">
 <td class="label">&nbsp;</td>
 <td class="data">${eliteName} Mileage accrual for this Flight has not yet been calculated.</td>
</tr>
</c:when>
<c:when test="${eliteEnabled && pirep.status.isComplete && (pirep.status != 'OK')}">
<tr class="elData">
 <td class="label">&nbsp;</td>
 <td class="data small caps bld"><span class="error">This Flight Leg is not eligible for Mileage accrual in the ${eliteName} Program</span>
</tr>
</c:when>
</c:choose>
</content:authUser>
<content:browser human="true">
<tr class="title">
 <td colspan="2">ROUTE MAP<c:if test="${filedETOPS.result.time > 75}"> - ${filedETOPS.result}</c:if><span id="mapToggle" class="toggle" onclick="void golgotha.util.toggleExpand(this, 'acarsMapData')">COLLAPSE</span></td>
</tr>
<c:choose>
<c:when test="${googleMap}">
<tr class="acarsMapData">
 <td class="label">Map Data</td>
 <td class="data"><span class="bld">
<c:if test="${isACARS || (!empty mapRoute)}"><el:box name="showRoute" idx="*" onChange="void map.toggle(golgotha.maps.acarsFlight.gRoute, this.checked)" label="Route" checked="${!isACARS}" /> </c:if>
<c:if test="${isACARS}"><el:box name="showFDR" idx="*" onChange="void map.toggle(golgotha.maps.acarsFlight.routeMarkers, this.checked)" label="Flight Data" checked="false" /> 
<el:box name="showAirspace" idx="*" onChange="void golgotha.maps.acarsFlight.toggleAirspace(this.checked)" label="Airspace Boundaries" checked="false" /> </c:if>
<c:if test="${!empty filedRoute}"><el:box name="showFPlan" idx="*" onChange="void map.toggle(golgotha.maps.acarsFlight.gfRoute, this.checked)" label="Flight Plan" checked="true" /> </c:if>
<el:box name="showFPMarkers" idx="*" onChange="void map.toggle(golgotha.maps.acarsFlight.filedMarkers, this.checked)" label="Navaid Markers" checked="true" /></span>
<c:if test="${!empty routeCycleInfo}"> <span class="small ita nophone">(Cycle <span class="pri bld">${routeCycleInfo.ID}</span> released <fmt:date date="${routeCycleInfo.releasedOn}" fmt="d" />)</span></c:if>
<c:if test="${!empty onlineTrack}"><span class="bld"> <el:box name="showOTrack" idx="*" onChange="void map.toggle(golgotha.maps.acarsFlight.otRoute, this.checked)" label="Online Track" checked="false" />
 <el:box name="showOMarkers" idx="*" onChange="void map.toggle(golgotha.maps.acarsFlight.otMarkers, this.checked)" label="Online Data" checked="false" /></span></c:if>
 <content:filter roles="Developer"><c:if test="${isACARS}}"><span class="bld"> <el:box name="rwyDebug" idx="*" onChange="void golgotha.maps.acarsFlight.toggleDebug(this.checked)" label="Runway Debug" checked="false" /></span>></c:if></content:filter>
 </td>
</tr>
<tr class="acarsMapData">
 <td colspan="2"><map:div ID="googleMap" height="575" /></td>
</tr>
</c:when>
<c:when test="${googleStaticMap}">
<tr class="acarsMapData">
 <td colspan="2" class="mid"><map:static w="1280" h="520" scale="2" markers="${filedRoute}" center="${mapCenter}" /></td>
</tr>
</c:when>
<c:when test="${frMap}">
<tr class="acarsMapData">
 <td colspan="2"><img src="https://maps.fallingrain.com/perl/map.cgi?x=620&y=365&kind=topo&lat=${pirep.airportD.latitude}&long=${pirep.airportD.longitude}&name=${pirep.airportD.name}&c=1&lat=${pirep.airportA.latitude}&long=${pirep.airportA.longitude}&name=${pirep.airportA.name}&c=1"
alt="${pirep.airportD.name} to ${pirep.airportA.name}" width="620" height="365" /></td>
</tr>
</c:when>
</c:choose>
<c:if test="${isACARS && googleMap}">
<tr id="flightDataLabel" class="title caps">
 <td colspan="2">SPEED / ALTITUDE DATA<span id="chartToggle" class="toggle" onclick="void golgotha.util.toggleExpand(this, 'flightDataChart')">COLLAPSE</span></td>
</tr>
<tr class="flightDataChart">
 <td colspan="2"><div id="flightChart" style="height:285px"></div></td>
</tr>
<c:if test="${!empty acarsTimerInfo || !empty acarsClientInfo || !empty acarsFrames}">
<%@ include file="/jsp/pilot/pirepACARSDiag.jspf" %>
</c:if>
</c:if>
</content:browser>
<c:if test="${!empty statusHistory}">
<tr class="title caps">
 <td colspan="2"><span class="nophone">FLIGHT REPORT </span>STATUS HISTORY<span id="historyToggle" class="toggle" onclick="void golgotha.util.toggleExpand(this, 'pirepHistory')">COLLAPSE</span></td>
</tr>
<c:forEach var="upd" items="${statusHistory}">
<c:set var="updAuthor" value="${statusHistoryUsers[upd.authorID]}" scope="page" />
<tr class="pirepHistory">
 <td class="ter bld mid"><fmt:edesc object="${upd.type}" /></td>
 <td class="data ellipsis"><span><span class="nophone"><fmt:date date="${upd.date}" /> - <span class="sec bld">${empty updAuthor ? 'SYSTEM' : updAuthor.name}</span> - </span>${upd.description}</span></td>
</tr>
</c:forEach>
</c:if>
<c:if test="${!scoreCR && (access.canDispose || ((access.canViewComments || access.canUpdateComments) && (!empty pirep.comments)))}">
<tr class="title caps">
 <td colspan="2">REVIEWER COMMENTS</td>
</tr>
<tr>
<c:if test="${access.canDispose || access.canUpdateComments}">
 <td colspan="2" class="mid"><el:textbox name="dComments" resize="true" width="85%" height="5">${pirep.comments}</el:textbox><el:text name="reviewTime" type="hidden" value="0" /></td></c:if>
<c:if test="${!access.canDispose && !access.canUpdateComments && access.canViewComments}">
 <td colspan="2" class="data"><fmt:msg value="${pirep.comments}" bbCode="true" /></td></c:if>
</tr>
</c:if>
<c:if test="${access.canHold && isDivert}">
<tr class="title">
 <td colspan="2">FLIGHT DIVERSION HANDLING</td>
</tr>
<tr>
 <td class="label top">Assign Leg</td>
 <td class="data"><span class="ita nophone">This flight does not appear to have arrived at its originally filed destination. Click the box below to hold the Flight Report and automatically assign a leg to complete the originally scheduled flight.</span><br />
<br /><el:box name="holdDivert" value="true" label="Create diversion completion Flight Assignment" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td class="caps">
<c:if test="${access.canSubmit}">
<el:cmdbutton url="submit" link="${pirep}" label="SUBMIT FLIGHT REPORT" /></c:if>
<c:if test="${access.canApprove && !scoreCR}">
&nbsp;<el:cmdbutton className="timedButton" url="dispose" link="${pirep}" op="approve" post="true" disabled="${hasDelay}" label="APPROVE" /></c:if>
<c:if test="${access.canHold}">
&nbsp;<el:cmdbutton className="timedButton" url="dispose" link="${pirep}" op="hold" post="true" disabled="${hasDelay}" label="HOLD" /></c:if>
<c:if test="${access.canRelease}">
&nbsp;<el:cmdbutton url="release" link="${pirep}" post="true" label="RELEASE HOLD" /></c:if>
<c:if test="${access.canWithdraw}">
&nbsp;<el:cmdbutton url="withdraw" link="${pirep}" post="true" label="WITHDRAW" /></c:if>
<c:if test="${access.canReject && (!fn:isCheckFlight(pirep) || !fn:pending(checkRide)) && !scoreCR}">
&nbsp;<el:cmdbutton className="timedButton" url="dispose" link="${pirep}" op="reject" post="true" disabled="${hasDelay}" label="REJECT" />
<c:if test="${isACARS && (empty checkRide)}"><content:filter roles="HR,PIREP,Operations">
&nbsp;<el:cmdbutton className="timedButton" url="crflag" link="${pirep}" disabled="${hasDelay}" label="MARK AS CHECK RIDE" /></content:filter></c:if>
</c:if>
<c:if test="${access.canDispose && (empty checkRide)}">
<c:set var="bLabel" value="${empty pirep.captEQType ? 'SET' : 'CLEAR'}" scope="page" />
&nbsp;<el:cmdbutton url="promotoggle" link="${pirep}" label="${bLabel} PROMOTION FLAG" /></c:if>
<c:if test="${access.canEdit}">
&nbsp;<el:cmdbutton url="pirep" link="${pirep}" op="edit" label="EDIT REPORT" /></c:if>
<c:if test="${access.canDelete}">
&nbsp;<el:cmdbutton url="pirepdelete" link="${pirep}" label="DELETE REPORT" />
<c:if test="${isACARS}">
&nbsp;<el:cmdbutton url="acarsdelete" link="${pirep}" label="DELETE ACARS DATA" /></c:if>
</c:if>
<c:if test="${access.canCalculateLoad}">
&nbsp;<el:cmdbutton url="calclf" link="${pirep}" label="CALCULATE PASSENGER LOAD" /></c:if>
<content:filter roles="PIREP,HR,Developer,Operations">
<c:if test="${isACARS}"><span class="nophone">&nbsp;<el:button label="RUNWAY CHOICES" key="R" onClick="void golgotha.local.showRunwayChoices()" /></span> <el:cmdbutton url="gaterecalc" link="${pirep}" label="LOAD GATES" /></c:if>
</content:filter>
<c:if test="${fn:isDraft(pirep) && (!empty assignmentInfo) && assignAccess.canRelease}">
&nbsp;<el:cmdbutton url="assignrelease" link="${assignmentInfo}" label="RELEASE ASSIGNMENT" /></c:if>
<c:if test="${access.canUpdateComments}">
&nbsp;<el:cmdbutton url="updcomments" link="${pirep}" post="true" label="UPDATE COMMENTS" /></c:if>
<c:if test="${access.canUseSimBrief && (!empty sbPackage.flightPlans)}">&nbsp;<el:button label="VIEW PILOT BRIEFING" onClick="void golgotha.simbrief.sbBriefingText()" /><span class="nophone bld"> | DOWNOAD FLIGHT PLAN <el:combo name="sbPlanName" size="1" idx="*" firstEntry="[ SELECT FORMAT ]" options="${sbPackage.flightPlans}" onChange="golgotha.simbrief.sbDownloadPlan(this)" /></span></c:if>&nbsp;</td>
</tr>
</el:table>
</el:form>
<content:copyright />
</content:region>
</content:page>
<content:browser human="true">
<c:choose>
<c:when test="${googleMap}">
<script async>
<c:if test="${!isACARS}">
golgotha.maps.acarsFlight = golgotha.maps.acarsFlight || {};</c:if>
golgotha.local.mapInit = function() {
<map:point var="golgotha.local.mapC" point="${mapCenter}" />

// Build the map
const mapOpts = {center:golgotha.local.mapC,minZoom:2,maxZoom:18,zoom:golgotha.maps.util.getDefaultZoom(${pirep.distance}),scrollwheel:false,clickableIcons:false,streetViewControl:false,mapTypeControlOptions:{mapTypeIds:golgotha.maps.DEFAULT_TYPES}};
map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
map.setMapTypeId(golgotha.maps.info.type);
map.infoWindow = new google.maps.InfoWindow({content:'',zIndex:golgotha.maps.z.INFOWINDOW, headerDisabled:true});
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
google.maps.event.addListener(map, 'click', map.closeWindow);
google.maps.event.addListenerOnce(map, 'tilesloaded', function() { google.maps.event.trigger(map, 'maptypeid_changed'); });

// Build the route line and map center
<c:if test="${!empty mapRoute}">
<map:points var="golgotha.maps.acarsFlight.routePoints" items="${mapRoute}" />
<map:line var="golgotha.maps.acarsFlight.gRoute" src="golgotha.maps.acarsFlight.routePoints" color="#4080af" width="3" transparency="0.75" geodesic="true" /></c:if>
<c:if test="${empty mapRoute && isACARS}">
<map:point var="golgotha.local.takeoff" point="${pirep.takeoffLocation}" />
<map:point var="golgotha.local.landing" point="${pirep.landingLocation}" />
golgotha.maps.acarsFlight.getACARSData(${fn:ACARS_ID(pirep)}, ${access.canApprove}, ${!empty user});</c:if>
<c:if test="${!empty filedRoute}">
<map:points var="golgotha.maps.acarsFlight.filedPoints" items="${filedRoute}" />
<map:markers var="golgotha.maps.acarsFlight.filedMarkers" items="${filedRoute}" />
<map:line var="golgotha.maps.acarsFlight.gfRoute" src="golgotha.maps.acarsFlight.filedPoints" color="#80800f" width="2" transparency="0.5" geodesic="true" />
map.addMarkers(golgotha.maps.acarsFlight.gfRoute);
map.addMarkers(golgotha.maps.acarsFlight.filedMarkers);</c:if>
<c:if test="${!empty onlineTrack}">
<map:points var="golgotha.maps.acarsFlight.onlinePoints" items="${onlineTrack}" />
<map:markers var="golgotha.maps.acarsFlight.otMarkers" items="${onlineTrack}" />
<map:line var="golgotha.maps.acarsFlight.otRoute" src="golgotha.maps.acarsFlight.onlinePoints" color="#f06f4f" width="3" transparency="0.55" geodesic="true" /></c:if>
<c:if test="${!empty mapRoute}">
map.addMarkers(golgotha.maps.acarsFlight.gRoute);</c:if>
<c:if test="${!empty sbPackage.alternates}">
<map:markers var="golgotha.maps.acarsFlight.alts" items="${sbPackage.alternates}" />
map.addMarkers(golgotha.maps.acarsFlight.alts);</c:if>
<c:if test="${!empty sbMarkers}">
<map:markers var="golgotha.maps.acarsFlight.sbMrks" items="${sbMarkers}" />
map.addMarkers(golgotha.maps.acarsFlight.sbMrks);</c:if>
<c:if test="${empty filedRoute}">
// Airport markers
<map:marker var="golgotha.maps.acarsFlight.gmA" point="${pirep.airportA}" />
<map:marker var="golgotha.maps.acarsFlight.gmD" point="${pirep.airportD}" />
golgotha.maps.acarsFlight.filedMarkers = [golgotha.maps.acarsFlight.gmA, golgotha.maps.acarsFlight.gmD];
map.addMarkers(golgotha.maps.acarsFlight.filedMarkers);</c:if>
};
<c:if test="${isACARS && googleMap}">
google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(function()
{
const p = fetch('pirepstats.ws?id=${pirep.hexID}');
p.then(function(rsp) {
	if (rsp.status != 200) {
		golgotha.util.display('flightDataLabel', false);
		golgotha.util.display('flightDataChart', false);
		return false;
	}

	rsp.json().then(function(statsData) {
		if (statsData.isSimTime) console.log('Using simulator date/time');
		statsData.data.forEach(function(e) { e[0] = new Date(e[0]); });

		// Build the Data
		const data = new google.visualization.DataTable();
		data.addColumn('datetime', 'Date/Time (UTC)');
		data.addColumn('number', 'Ground Speed');
		data.addColumn('number', 'Altitude');
		<c:if test="${!isXACARS}">    data.addColumn('number', 'Ground Elevation');</c:if>
	    data.addRows(statsData.data);
	
		// Read CSS selectors for graph lines
	    const pr = golgotha.util.getStyle('main.css', '.pri') || '#0000a1'; 
	    const sc = golgotha.util.getStyle('main.css', '.sec') || '#008080';

		// Create formatting options
		const ha = {gridlines:{count:10},minorGridlines:{count:5},title:'Date/Time',textStyle:golgotha.charts.lgStyle,titleTextStyle:golgotha.charts.ttStyle};
		const va0 = {maxValue:statsData.maxSpeed,gridlines:{count:5,multiple:100},title:'Knots',textStyle:golgotha.charts.lgStyle,titleTextStyle:golgotha.charts.ttStyle};
		const va1 = {maxValue:statsData.maxAlt,gridlines:{count:5,interval:[statsData.altInterval]},ticks:statsData.altIntervals,title:'Feet',textStyle:golgotha.charts.lgStyle,titleTextStyle:golgotha.charts.ttStyle};
		const s = [{targetAxisIndex:0},{targetAxisIndex:1},{targetAxisIndex:1,type:'area',areaOpacity:0.7}];
		const opts = golgotha.charts.buildOptions({series:s,vAxes:[va0,va1],colors:[pr,sc,'#b8b8d8']});
		const chart = new google.visualization.ComboChart(document.getElementById('flightChart'));
		chart.draw(data,opts);
		<c:if test="${access.canApprove}">golgotha.util.toggleExpand(document.getElementById('chartToggle'), 'flightDataChart');</c:if>	
	});
});

return true;
});</c:if>
</script>
</c:when>
<c:when test="${googleStatic}">
<!--  Google static Map -->
</c:when>
</c:choose>
<c:if test="${access.canUseSimBrief}">
<!-- SimBrief integration -->
<script async>
golgotha.simbrief.id = '${pirep.hexID}';
golgotha.simbrief.acType = '${acInfo.ICAO}';
<c:if test="${empty sbPackage}">golgotha.simbrief.loadAirframes();</c:if>
<c:if test="${!empty sbPackage}">golgotha.simbrief.planURL = '${sbPackage.basePlanURL}';</c:if>
</script>
<c:set var="altIdx" value="0" scope="page" />
<el:form ID="sbapiform" method="post" action="" validate="return false">
<el:text name="planformat" type="hidden" value="dal" />
<el:text name="orig" type="hidden" value="${pirep.airportD.ICAO}" />
<el:text name="dest" type="hidden" value="${pirep.airportA.ICAO}" />
<el:text name="type" type="hidden" value="${acInfo.ICAO}" />
<el:text name="airline" type="hidden"  value="${pirep.airline.ICAO}" />
<el:text name="fltnum" type="hidden" value="${pirep.flightNumber}" />
<el:text name="callsign" type="hidden" value="${pirep.callsign}" />
<el:text name="reg" type="hidden" value="" />
<el:text name="route" type="hidden" value="${pirep.route}" />
<el:text name="cpt" type="hidden" value="${pilot.name}" />
<el:text name="dxname" type="hidden" value="Golgotha v${versionInfo}" />
<el:text name="static_id" type="hidden" value="${pirep.hexID}" />
<el:text name="resvrule" type="hidden" value="45" />
<el:text name="taxiout" type="hidden" value="${avgTaxiOutTime.outboundTime.toMinutes()}" />
<el:text name="taxiin" type="hidden" value="${avgTaxiInTime.inboundTime.toMinutes()}" />
<el:text name="pax" type="hidden" value="${pirep.passengers}" />
<el:text name="date" type="hidden" value="${fn:upper(fn:dateFmt(departureTime,'ddMMMyy'))}" />
<el:text name="etopsrule" type="hidden" value="${acPolicy.ETOPS.time}" />
<el:text name="deph" type="hidden" value="${departureTimeUTC.hour}" />
<el:text name="depm" type="hidden" value="${departureTimeUTC.minute}" />
<el:text name="steh" type="hidden" value="${pirep.duration.toHoursPart()}" />
<el:text name="stem" type="hidden" value="${pirep.duration.toMinutesPart()}" />
<el:text name="units" type="hidden" value="${pilot.weightType}S" />
<el:text name="climb" type="hidden" value="250/310/80" />
<el:text name="descent" type="hidden" value="82/300/250" />
<el:text name="notams" type="hidden" value="0" />
<el:text name="firnot" type="hidden" value="0" />
<el:text name="maps" type="hidden" value="none" />
<el:text name="cruise" type="hidden" value="CI" />
<el:text name="civalue" type="hidden" value="80" />
<el:text name="altn_count" type="hidden" value="${alternates.size()}" />
<c:forEach var="alt" items="${alternates}">
<c:set var="altIdx" value="${altIdx + 1}" scope="page" />
<el:text name="altn_${altIdx}_id" type="hidden" value="${alt.ICAO}" /></c:forEach>
</el:form>
</c:if>
</content:browser>
</body>
</html>
