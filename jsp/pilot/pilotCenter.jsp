<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Center - ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></title>
<content:expire expires="10" />
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<style type="text/css">
@media (min-width: 801px) {
    table#pilotCenter td.sideLabel { width: 35%; max-width: 350px; }
}
@media (max-width: 800px) {
    table#pilotCenter td.sideLabel { width: 30%; min-width: 120px; }
}
</style>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="req" value="${pageContext.request}" scope="page" />
<content:sysdata var="domain" name="airline.domain" />
<content:sysdata var="maxHeld" name="users.pirep.maxHeld" default="5" />
<content:sysdata var="acarsEnabled" name="acars.enabled" />
<content:sysdata var="fileLibEnabled" name="airline.files.enabled" />
<content:sysdata var="ts2Enabled" name="airline.voice.ts2.enabled" />
<content:sysdata var="mvsEnabled" name="airline.voice.mvs.enabled" />
<content:sysdata var="newsletterEnabled" name="airline.newsletters.enabled" />
<content:sysdata var="videoEnabled" name="airline.video.enabled" />
<content:sysdata var="resourceEnabled" name="airline.resources.enabled" />
<content:sysdata var="newsletterCats" name="airline.newsletters.categories" />
<content:sysdata var="newsletter" name="airline.newsletters.name" />
<content:sysdata var="selcalMax" name="users.selcal.max" />
<content:sysdata var="selcalReserve" name="users.selcal.reserve" />
<content:sysdata var="examLockoutHours" name="testing.lockout" />
<content:sysdata var="academyEnabled" name="academy.enabled" />
<content:sysdata var="academyFlights" name="academy.minFlights" />
<content:sysdata var="helpDeskEnabled" name="helpdesk.enabled" />
<content:sysdata var="innovataEnabled" name="schedule.innovata.enabled" />
<content:sysdata var="vaBaseEnabled" name="schedule.vabase.enabled" />
<content:sysdata var="hasIMAP" name="smtp.imap.enabled" />
<content:sysdata var="hasSC" name="users.sc.active" default="false" />
<content:sysdata var="scMaxNoms" name="users.sc.maxNominations" default="5" />
<content:sysdata var="scMinFlights" name="users.sc.minFlights" default="5" />
<content:sysdata var="scMinAge" name="users.sc.minAge" default="120" />
<content:sysdata var="fbAuthURL" name="users.facebook.url.authorize" />
<content:sysdata var="fbClientID" name="users.facebook.id" />
<content:sysdata var="fbPageID" name="users.facebook.pageID" />
<content:sysdata var="faaChartURL" name="schedule.chart.url.faa.meta" />
<content:sysdata var="currencyEnabled" name="testing.currency.enabled" />
<content:sysdata var="currencySelfEnroll" name="testing.currency.selfenroll" />
<content:sysdata var="currencyInterval" name="testing.currency.validity" />
<content:attr attr="hasDispatchAccess" value="true" roles="HR,Route,Dispatch" />
<content:attr attr="isHROperations" value="true" roles="HR,Operations" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table ID="pilotCenter" className="form">

<!-- Pilot Information -->
<tr class="title caps">
 <td colspan="2">PILOT CENTER - ${pilot.rank.name}  ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></td>
</tr>
<tr>
 <td class="sideLabel mid"><el:cmd className="bld" url="profile" link="${pilot}" op="edit">Edit My Profile</el:cmd></td>
 <td class="data">Welcome back to <span class="pri bld"><content:airline /></span>, ${pilot.firstName}.
<c:if test="${!empty pilot.pilotCode}"> Your pilot code is <span class="pri bld">${pilot.pilotCode}</span>.</c:if><br />
 You signed up on <fmt:date date="${pilot.createdOn}" fmt="d" /> (<fmt:int value="${pilotAge}" /> days ago) and have visited <fmt:quantity value="${pilot.loginCount}" single="time" />.<br />
You are visiting today from <span class="bld">${req.remoteHost}</span> (${req.remoteAddr})<c:if test="${!empty ipAddrInfo}">, in ${ipAddrInfo.location}</c:if>.</td>
</tr>
<tr>
 <td class="mid"><el:cmd url="emailupd" className="bld">Change E-mail Address</el:cmd></td>
 <td class="data">Your e-mail address is <span class="sec bld">${pilot.email}</span>. Membership at <content:airline /> is contingent on providing a valid, verified e-mail address. You may update your e-mail address and start the validation process.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="geolocate">Update Location</el:cmd></td>
<c:if test="${!empty geoLocation}">
 <td class="data">You can update your location on the <content:airline /> Pilot Board.<br />
<br />
<span class="ita">For privacy reasons, your specific location will be altered by a random number of miles each time the <content:airline /> Pilot Board is viewed.</span></td>
</c:if>
<c:if test="${empty geoLocation}">
 <td class="data">You have not specified your geographic location. By doing so, you can add your name to the <content:airline /> Pilot Board.<br />
<br />
<span class="ita">For privacy reasons, your specific location will be altered by a random number of miles each time the <content:airline /> Pilot Board is viewed.</span></td>
</c:if>
</tr>
<content:filter roles="Facebook">
<c:set var="fbPermissions" value="${fn:splice(fbPerms, ',')}" scope="page" />
<c:if test="${!empty fbPageID}"><c:set var="fbPermissions" value="${fbPermissions},manage_pages" scope="page" /></c:if>
<content:protocol var="reqProtocol" />
<script type="text/javascript" async>
golgotha.local.fbAuthorize = function() {
	var URLflags = 'height=360,width=860,menubar=no,toolbar=no,status=no,scrollbars=no,resizable=no';
	return window.open('${fbAuthURL}?client_id=${fbClientID}&redirect_uri=${reqProtocol}://${req.serverName}/fbauth.do&scope=${fbPermissions}&display=popup', 'fbAuth', URLflags);
};
</script>
<tr>
 <td class="mid"><a class="bld" href="javascript:void golgotha.local.fbAuthorize()">Authorize Us</a></td>
<c:choose>
<c:when test="${fn:hasIM(pilot, 'FB')}">
 <td class="data"><span class="pri bld">The Facebook authorization token you have given <content:airline /> has expired.</span> Further updates to your Facebook wall cannot be made until <content:airline /> has been authorized to publish to Facebook.</td>
</c:when>
<c:otherwise>
 <td class="data">If you are a Facebook member, you can connect to Facebook and allow <content:airline /> to post updates about your virtual career to your Facebook wall<c:if test="${acarsEnabled}">, as well as information about your flights flown using <content:airline /> ACARS</c:if>.</td>
</c:otherwise>
</c:choose>
</tr>
</content:filter>
<c:if test="${access.canTakeLeave}">
<content:sysdata var="inactivity_days" name="users.inactive_days" />
<tr>
 <td class="mid"><el:cmd className="bld" url="loa">Request Leave of Absence</el:cmd></td>
 <td class="data">In order to remain an active pilot, you need to log into our web site at least once every ${inactivity_days} days. If you are unable to do so, you can request a <span class="pri bld">Leave of 
 Absence</span> by clicking on the link on the left.</td>
</tr>
</c:if>
<tr>
 <td class="mid">&nbsp;
<c:if test="${!empty acImage}"><el:img src="${acImage}" className="nophone" caption="${pilot.equipmentType}" /></c:if>
 </td>
 <td class="data">You are a <span class="pri bld">${pilot.rank.name}</span> in the <span class="sec bld">${pilot.equipmentType}</span> program. <span class="pri bld">(Stage ${eqType.stage})</span><br />
<br />
Your Chief Pilot is <a class="bld" href="mailto:${CP.email}">${CP.name}</a>.<br />
<c:if test="${!empty asstCP}">
<c:if test="${fn:sizeof(asstCP) == 1}">
Your Assistant Chief Pilot is </c:if>
<c:if test="${fn:sizeof(asstCP) > 1}">
Your Assistant Chief Pilots are </c:if>
<c:forEach var="aCP" items="${asstCP}" varStatus="acpStatus">
<a class="bld" href="mailto:${aCP.email}">${aCP.name}</a><c:if test="${!acpStatus.last}">, </c:if></c:forEach>.
<br />
</c:if>
<br />
You are also qualified to file Flight Reports using the following aircraft:<br />
<fmt:list value="${pilot.ratings}" delim=", " />.
<content:filter roles="Dispatch">
<br /><br />
You are an ACARS Flight Dispatcher and have dispatched <fmt:int value="${pilot.dispatchFlights}" /> flights and provided Dispatch services for <fmt:dec value="${pilot.dispatchHours}" /> hours.</content:filter></td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="acceligibility">Accomplishment Eligibility</el:cmd></td>
<td class="data">
<c:if test="${empty accs}">
You have not achieved any Pilot Accomplishments yet.
</c:if>
<c:if test="${!empty accs}">
You have achieved the following Accomplishments:<br />
<br />
<c:forEach var="a" items="${accs}">
<fmt:accomplish accomplish="${a}" className="bld" />, (<fmt:int value="${a.value}" /> ${a.unit.name}) on <span class="bld"><fmt:date date="${a.date}" fmt="d" /></span><br />
</c:forEach></c:if>
<br />
To view a map of Airports to visit to complete Accomplishments, <el:cmd url="accairportmap" className="sec bld">Click Here</el:cmd>.</td>
</tr>
<content:filter roles="HR,Examination,Operations">
<tr>
 <td class="mid"><el:cmd url="prgroster" className="bld">Program Roster</el:cmd></td>
 <td class="data">You can view statistics about the ${pilot.equipmentType} program, and view the pilot roster for this equipment program.</td>
</tr>
</content:filter>
<tr>
 <td class="mid"><el:cmd className="bld" url="pilotsearch">Pilot Search</el:cmd></td>
 <td class="data">You can search the <content:airline /> Pilot Roster based on a Pilot's name or E-Mail Address.</td>
</tr>

<c:if test="${helpDeskEnabled}">
<!-- Help Desk Section -->
<tr class="title caps">
 <td colspan="2"><content:airline /> HELP DESK</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="helpdesk">Help Desk</el:cmd></td>
 <td class="data">The <content:airline /> Help Desk lets our members communicate with our Instructors and Staff to quickly and easily resolve any issues or answer questions about <content:airline />.</td>
</tr>
</c:if>

<!-- Flight Report Section -->
<tr class="title caps">
 <td colspan="2">FLIGHT REPORTS</td>
</tr>
<c:if test="${heldPIREPCount >= maxHeld}">
<tr>
 <td colspan="2" class="mid error bld">You currently have held <fmt:int value="${heldPIREPCount}" /> Flight Reports. All future submitted Flight Reports will be automatically Held until the currently held Flights are approved or rejected.</td>
</tr>
</c:if>
<tr>
 <td class="mid"><el:cmd className="bld" url="logbook" op="nolog" link="${pilot}">Flight Reports</el:cmd>&nbsp;
<el:cmd className="bld" url="logbook" op="log" link="${pilot}">Log Book</el:cmd>&nbsp;
<el:cmd className="bld" url="logcalendar" link="${pilot}">Calendar</el:cmd><br />
<el:cmd className="bld" url="acarsoffline">File Offline ACARS Flight Report</el:cmd>
<c:if test="${manualPIREP}"><br />
<el:cmd className="pri bld" url="pirep" op="edit">File New Flight Report</el:cmd></c:if></td>
 <td class="data">You have flown <fmt:quantity value="${pilot.legs}" single="flight" />, for a total of
 <fmt:dec className="bld" value="${pilot.hours}" /> hours and <fmt:distance className="bld" value="${pilot.miles}" longUnits="true" />.<br />
<c:if test="${pilot.onlineLegs > 0}">
<span class="sec bld"><fmt:int value="${pilot.onlineLegs}" /></span> of these flights and 
<span class="sec bld"><fmt:dec value="${pilot.onlineHours}" /></span> hours were logged online.<br /></c:if>
<c:if test="${acarsEnabled && (pilot.ACARSLegs > 0)}">
<span class="pri bld"><fmt:int value="${pilot.ACARSLegs}" /></span> of these flights and 
<span class="pri bld"><fmt:dec value="${pilot.ACARSHours}" /></span> hours were logged using ACARS.<br /></c:if>
<c:if test="${pilot.totalLegs > pilot.legs}">
You have flown <fmt:int value="${pilot.totalLegs}" /> flights and <fmt:dec value="${pilot.totalHours}" /> hours combined between 
<content:airline /> and our partner airlines.<br /></c:if>
<c:if test="${totalPax > 0}">
You have carried <fmt:int value="${totalPax}" /> passengers on your flights.<br /></c:if>
<c:if test="${!empty lastFlight}">
<br />
Your last flight was on <fmt:date className="sec bld" date="${lastFlight.date}" fmt="d" />:<br />
<el:cmd url="pirep" link="${lastFlight}" className="pri bld">${lastFlight}</el:cmd> - ${lastFlight.airportD.name} (<el:cmd url="airportinfo" linkID="${lastFlight.airportD.IATA}" className="plain"><fmt:airport airport="${lastFlight.airportD}" /></el:cmd>) to 
 ${lastFlight.airportA.name} (<el:cmd url="airportinfo" linkID="${lastFlight.airportA.IATA}" className="plain"><fmt:airport airport="${lastFlight.airportA}" /></el:cmd>) in a ${lastFlight.equipmentType}.</c:if></td>
</tr>
<c:if test="${needReturnCharter}">
<tr>
 <td class="mid"><el:cmd className="bld" url="rcharter">Request Return Charter</el:cmd></td>
 <td class="data">There are no flights in the <content:airline /> Flight Schedule between ${lastFlight.airportA.name} (<fmt:airport airport="${lastFlight.airportA}" />) and ${lastFlight.airportD.name} (<fmt:airport airport="${lastFlight.airportD}" />).
 You can request a Return Charter flight to return to your previous airport.</td>
</tr>
</c:if>
<c:if test="${pilot.legs > 0}">
<tr>
 <td class="mid bld">Flight Information</td>
 <td class="data">To view a map of the routes you have flown, <el:cmd className="sec bld" url="myroutemap">Click Here</el:cmd>.<br />
<c:if test="${acarsEnabled && (pilot.ACARSLegs > 0)}"><br />
To view statistics about your flights, <el:cmd className="sec bld" url="mystats">Click Here</el:cmd>.</c:if>
<c:if test="${pilot.legs > 10}"><br />
To view airports you have yet to visit, <el:cmd className="sec bld" url="mynewairports">Click Here</el:cmd>.</c:if>
</td>
</tr>
</c:if>
<content:filter roles="PIREP">
<c:choose>
<c:when test="${pirepQueueStats.size > 75}">
<c:set var="queueClass" value="error bld" scope="page" />
</c:when>
<c:when test="${pirepQueueStats.size > 15}">
<c:set var="queueClass" value="sec bld" scope="page" />
</c:when>
<c:otherwise>
<c:set var="queueClass" value="sec" scope="page" />
</c:otherwise>
</c:choose>
<!-- Flight Report Admin Section -->
<tr>
 <td class="mid"><el:cmd className="bld" url="pirepqueue">Submitted Flight Reports</el:cmd></td>
 <td class="data">You can Approve, Reject or Hold submitted pilot Flight Reports here.<c:if test="${pirepQueueStats.size > 0}"><span class="${queueClass}"><br />
There <fmt:is value="${pirepQueueStats.size}" />  currently <fmt:quantity value="${pirepQueueStats.size}" single="Flight Report" /> awaiting review.
<content:filter roles="Operations,HR"> The average age of each pending flight report is <fmt:dec value="${pirepQueueStats.averageAge}" /> hours.</content:filter></span></c:if>
<c:set var="myPirepQueueSize" value="${pirepQueueStats.counts[pilot.equipmentType]}" scope="page" />
<c:if test="${myPirepQueueSize > 0}"><br /><span class="${queueClass}">There are currently <fmt:quantity value="${myPirepQueueSize}" single="Flight Report" /> awaiting 
review flown using equipment in the ${pilot.equipmentType} program.</span></c:if>
<c:if test="${checkRideQueueSize > 0}"><br />
<span class="pri bld">There <fmt:is value="${checkRideQueueSize}" />&nbsp;<fmt:quantity value="${checkRideQueueSize}" single="Check Ride" />
<content:filter roles="!HR"> in the ${pilot.equipmentType} program</content:filter> awaiting review.</span></c:if></td>
</tr>
</content:filter>
<content:filter roles="HR,Operations,Examination">
<!-- Pending Transfer Request / Examination Section -->
<tr class="title caps">
 <td colspan="2">PENDING EXAMINATIONS AND TRANSFER REQUESTS</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="txrequests">Equipment Transfer Requests</el:cmd></td>
 <td class="data">Pilots wishing to switch Equipment Programs can submit transfer requests once  they have met the necessary requirements for a new Equipment Program. You can view these transfer 
requests here, assign Check Rides, and complete the Promotion Process.<c:if test="${txQueueSize > 0}"><br />
<br />
<span class="ita">There <fmt:is value="${txQueueSize}" /> <fmt:quantity value="${txQueueSize}" single="pending Transfer Request" />.</span></c:if></td>
</tr>
<tr>
 <td class="mid bld"><el:cmd url="promoqueue">Promotion Queue</el:cmd></td>
 <td class="data">The Promotion Queue lists pilots who have successfully met all the requirements for promotion to the rank of Captain in their Equipment Program.<c:if test="${promoQueueSize > 0}"><br />
<br />
<b>There <fmt:is value="${promoQueueSize}" /> <fmt:quantity value="${promoQueueSize}" single="Pilot" /> awaiting promotion to Captain.</b></c:if></td>
</tr>
<content:filter roles="HR,Examination">
<tr>
 <td class="mid"><el:cmd className="bld" url="examqueue">Submitted Examinations</el:cmd></td>
 <td class="data">You can view and score submitted Pilot Examinations.<c:if test="${examQueueSize > 0}"><br />
<br />
<b>There <fmt:is value="${examQueueSize}" />&nbsp;<fmt:quantity value="${examQueueSize}" single="submitted Examination" /> awaiting evaluation.</b></c:if></td>
</tr>
</content:filter>
</content:filter>

<!-- Download Section -->
<tr class="title caps">
 <td colspan="2">DOWNLOAD LIBRARIES</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="doclibrary">Document Library</el:cmd></td>
 <td class="data">The <content:airline /> Document Library contains all of <content:airline />'s official airline procedure manuals, as well as operating manuals for each equipment type. All
 manuals are stored in cross-platform Adobe Acrobat PDF files.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="fleetlibrary">Fleet Library</el:cmd></td>
 <td class="data">Our Fleet Library contains the official <content:airline /> Fleet - a collection of aircraft, panels manuals and sound schemes.</td>
</tr>
<c:if test="${fileLibEnabled}">
<content:filter roles="Fleet,HR">
<tr>
 <td class="mid"><el:cmd className="bld" url="filelibrary">File Library</el:cmd></td>
 <td class="data">The <content:airline /> File Library contains a number of approved downloadable contributions from our community that will enhance your flight simulation experience.</td>
</tr>
</content:filter>
</c:if>
<c:if test="${newsletterEnabled}">
<tr>
 <td class="mid"><el:cmd className="bld" url="newsletters">Newsletters</el:cmd></td>
 <td class="data">${newsletter} is the official <content:airline /> newsletter, and is published regularly. You can view back issues of ${newsletter} here. You can also download copies of the other <content:airline />
 newsletters (<fmt:list value="${newsletterCats}" delim=", " />) here.</td>
</tr>
</c:if>
<c:if test="${resourceEnabled}">
<tr>
 <td class="mid"><el:cmd className="bld" url="resources">Web Resources</el:cmd></td>
 <td class="data">Our Web Resources contain links to a number of online sites that can assist you in all manner of flight planning and other ways.</td>
</tr>
</c:if>
<c:if test="${videoEnabled}">
<tr>
 <td class="mid"><el:cmd url="tvideolibrary" className="bld">Video Library</el:cmd></td>
 <td class="data">The <content:airline /> Video Library contains videos created by <content:airline /> staff.</td>
</tr>
</c:if>
<content:filter roles="Fleet">
<tr>
 <td class="mid"><el:cmd className="bld" url="fleetlibrary" op="admin">Fleet Library Administration</el:cmd></td>
 <td class="data">You can add or update entries in the <content:airline /> Fleet Library.</td>
</tr>
</content:filter>
<content:filter roles="Fleet,Developer">
<tr>
 <td class="mid"><el:cmd className="bld" url="acarsinstupdate">ACARS Installer Update</el:cmd></td>
 <td class="data">You can upload new <content:airline /> ACARS Incremental Installers to the server.</td>
</tr>
</content:filter>

<!-- Flight Schedule Section -->
<tr class="title caps">
 <td colspan="2"><content:airline /> FLIGHT SCHEDULE</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="findflight">Find A Flight</el:cmd></td>
 <td class="data">If you're looking for a flight at random, you can select an Airport, Departure Time, Aircraft Type and Flight Length, and pick a flight at random!</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="browse">Browse Schedule</el:cmd></td>
 <td class="data">You are able to browse the <content:airline /> Flight Schedule, which contains <fmt:int value="${scheduleSize}" /> flight legs to a variety of different destinations.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="routemap">Route Map</el:cmd></td>
 <td class="data">You can view a map of all the destinations that <content:airline /> and its codeshare partners currently serve.</td>
</tr>
<tr>
 <td class="mid"><el:link url="/pfpxsched.ws" className="bld">PFPX Flight Schedule</el:link></td>
 <td class="data">You can export the <content:airline /> Flight Schedule in format to be loaded by Aerosoft Professional Flight Planner X.</td>
</tr>

<!-- Flight Planning Section -->
<tr class="title caps">
 <td colspan="2">FLIGHT PLANNING RESOURCES</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="routeplot">Route Plotter</el:cmd></td>
 <td class="data">You can use our Google Maps tool to visually display a flight route between two airports, including Standard Instrument Departure (SID) and Standard Terminal Arrival Route (STAR) data and waypoints.</td>
</tr>
<c:if test="${!empty lastFlight}">
<tr>
 <td class="mid"><el:cmd className="bld" url="singleassign">Random Flight</el:cmd></td>
 <td class="data">You can select one or more Flights starting from ${lastFlight.airportA.name} (<fmt:airport airport="${lastFlight.airportA}" />) from the <content:airline /> Flight Schedule. This flight is selected at random, and 
 depending on the airport there may not be flights departing today.</td>
</tr>
</c:if>
<tr>
 <td class="mid"><el:cmd className="bld" url="routeassign">Multi-Leg Route</el:cmd></td>
 <td class="data">You can build a route of one or more flight legs between two different airports using flights from the <content:airline /> Flight Schedule.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="myassign">Flight Assignments</el:cmd></td>
 <td class="data">While <content:airline /> doesn't have a formal flight bidding system, we do have 'Flight Assigments': routes of 2 to 6 flight legs created by our staff as suggested routes to fly, or you
 can have our automated system <el:cmd url="findflight">randomly select flights</el:cmd> for you to fly.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="pri bld" url="charts" linkID="${pilot.homeAirport}">Approach Charts</el:cmd></td>
 <td class="data">We have airport, instrument approach, departure and arrival charts airports served by <content:airline />.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="routes" op="oceanic">Oceanic Tracks</el:cmd></td>
 <td class="data">Our servers automatically download North Atlantic and Pacific Track information every day.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="natplot">NAT Route Plotter</el:cmd><br />
<el:cmd className="bld" url="pacotplot">PACOT Route Plotter</el:cmd><br />
<el:cmd className="bld" url="ausotplot">AUSOT Route Plotter</el:cmd></td>
 <td class="data">You can use our Google Maps tool to plot today's North Atlantic, Pacific and Australian Tracks, or NAT / PACOT / AUSOT routes for the previous several months.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="wxcenter">Weather Center</el:cmd></td>
 <td class="data">The <content:airline /> Weather Center allows you to view Airport conditions and forecast information, along with a number of interactive weather maps covering the continental United States and the world.</td>  
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="wxfinder">Airport Weather Finder</el:cmd></td>
 <td class="data">You can search for airports served by <content:airline /> that are experiencing unusual or severe weather.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="aircraftlist">Aircraft Profiles</el:cmd></td>
 <td class="data">You can view Aircraft profiles contained within the <content:airline /> Flight Schedule. This lists operational maximum weights, minimum runway distances and ACARS fuel load profiles.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="navsearch">Navigation Aids</el:cmd></td>
 <td class="data">You can search the DAFIF database for a particular Airport, VOR, NDB or Intersection.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="navcycles">Navigation Cycle Release Dates</el:cmd></td>
 <td class="data">You can view when new navigation data cycles will be released, as well as view which cycle data is currently loaded in the <content:airline /> Navigation database and FAA Approach Chart database.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="selcals">SELCAL Codes</el:cmd></td>
 <td class="data">You can view SELCAL codes used by <content:airline /> and its partner airlines' aircraft,  and temporarily reserve up to <fmt:int value="${selcalMax}" /> SELCAL codes for your own use, for up to <fmt:int value="${selcalReserve}" /> days.</td>
</tr>

<!-- Testing Section -->
<tr class="title caps">
 <td colspan="2">PILOT TESTING AND PROMOTION</td>
</tr>
<c:if test="${currencyEnabled && (currencySelfEnroll || isHROperations)}">
<tr>
 <td class="mid" colspan="2"><content:airline /> allows its Pilots to <span class="ita">opt into</span> a recurrent certification model. Pilots will continue to require the successful completion of a written Examination
 as well as an initial Check Ride for entrance into a particular equipment program. Pilot who opt into recurrent certification will require an additional operational Check Ride every <fmt:int value="${currencyInterval}" />
 days in order to retain their type ratings.<br />
<br /> 
<c:if test="${!pilot.proficiencyCheckRides}">
You are currently enrolled in our <span class="pri bld caps">LEGACY</span> certification model. Ratings never expire, and Check Rides will remain valid permanently.<br />
<br />
To discover more about our currency-based certification model, you can <el:cmd url="currencyenable" link="${pilot}" className="pri bld">Click Here</el:cmd> to review the changes that switching to this model will
have on your existing aircraft type ratings.
</c:if>
<c:if test="${pilot.proficiencyCheckRides}">
You are currently enrolled within our <span class="ter bld caps">RECURRENT</span> certification model. Check ries are only valid for <fmt:int value="${currencyInterval}" /> days and a currency Check Ride will need to
 be performed before ratings expire.<br />
<c:if test="${!empty upcomingExpirations}"><br />
The following Check Rides or Check Ride Waivers are due to expire within the next 60 days. You will need to successfully complete a currency Check Ride prior to expiration in order to maintain your raings:<br />
<br />
<c:forEach var="expCR" items="${upcomingExpirations}" varStatus="expStatus">
<c:set var="isExpired" value="${expCR.expirationDate.isBefore(now)}" scope="page" />
${expCR.name}, completed on <fmt:date fmt="d" date="${expCR.scoredOn}" /> ${isExpired ? 'expired' : 'expires'} on <fmt:date fmt="d" className="error bld" date="${expCR.expirationDate}" /><c:if test="${!expStatus.last}"><br /></c:if>
</c:forEach></c:if></c:if>
 </td>
</tr>
</c:if>
<c:if test="${isFO}">
<tr>
 <td class="pri mid bld">Promotion to Captain</td>
<c:choose>
<c:when test="${captPromote}"> 
 <td class="data">You are eligible for a promotion to Captain in the <span class="pri bld">${eqType.name}</span> program. Your name is on the list of Pilots eligible for a promotion, and you can expect to be promoted 
within the next 24 to 72 hours. You are also eligible for equipment transfers and additional ratings in higher stage equipment type programs.</td></c:when>
<c:when test="${promoteLegs < eqType.promotionLegs}">
 <td class="data">You have completed <fmt:int value="${promoteLegs}" /> of the <fmt:quantity value="${eqType.promotionLegs}" single="Flight" /> in the <fmt:list value="${eqType.primaryRatings}" delim=", " /> 
<c:if test="${eqType.ACARSPromotionLegs}">using ACARS </c:if>required for promotion to the rank of Captain in the ${eqType.name} program.</td></c:when>
<c:when test="${promoteLegs >= eqType.promotionLegs}">
 <td class="data">You have completed the <fmt:int value="${eqType.promotionLegs}" /> Flight Legs in the <fmt:list value="${eqType.primaryRatings}" delim=", " /> 
<c:if test="${eqType.ACARSPromotionLegs}">using ACARS </c:if> required for promotion to the rank of Captain in the ${eqType.name} program. <span class="ita">You still need to pass the <span class="pri bld"><fmt:list value="${fn:examC(eqType)}" delim="," /></span> 
Examination(s) in order to be eligible for promotion to Captain</span>.</td></c:when>
</c:choose>
</tr>
</c:if>
<c:if test="${!empty eqSwitch || !empty eqSwitchFOExam}">
<c:set var="canSwitchFO" value="${!empty eqSwitchFOExam && (promoteLegs >= (eqType.promotionLegs / 2))}" scope="page" />
<tr>
<c:if test="${!empty eqSwitch}">
 <td class="mid"><el:cmd className="bld" url="txrequest">Switch Equipment Programs</el:cmd><br />
<el:cmd className="bld" op="rating" url="txrequest">Request Additional Ratings</el:cmd></td>
</c:if>
<c:if test="${empty eqSwitch}">
 <td class="mid">&nbsp;</td>
</c:if>
 <td class="data"><c:if test="${!empty eqSwitch}">You are eligible to transfer to or request additional ratings in the following equipment types: <b><fmt:list value="${eqSwitch}" delim=", " /></b>.</c:if>
<c:if test="${!empty eqSwitch && canSwitchFO}"><br /><br /></c:if>
<c:if test="${canSwitchFO}">You are eligible to transfer to or request additional ratings in the following equipment types upon successful completion of the First Officer's examination for these 
equipment programs: <b><fmt:list value="${eqSwitchFOExam}" delim=", " /></b>.</c:if>
<c:if test="${(isFO && !captPromote) || (promoteLegs < eqType.promotionLegs)}"><br />
<c:if test="${isFO && !captPromote && (eqType.stage == eqSwitchMaxStage)}">
<br />
<span class="ita">You will not be eligible for equipment type transfers or additional ratings in equipment type programs in Stage <fmt:int value="${eqSwitchMaxStage}" /> or above until you become eligible for promotion to Captain
 in the ${eqType.name} or another Stage <fmt:int value="${eqType.stage}" /> equipment program.</span><br />
</c:if>
<c:if test="${promoteLegs < (eqType.promotionLegs / 2)}">
<br />
<span class="ita">You will not be eligible for equipment type transfers or additional ratings in new equipment type programs in Stage <fmt:int value="${eqType.stage}" /> and below until you have completed <fmt:int value="${eqType.promotionLegs / 2}" /> 
of the <fmt:int value="${eqType.promotionLegs}" /> Flight legs in the <fmt:list value="${eqType.primaryRatings}" delim=", " />.</span></c:if>
</c:if>
</td></tr>
</c:if>
<c:if test="${(!empty txreq) && (!txreq.ratingOnly)}">
<tr>
 <td class="mid bld">Switch Equipment Programs</td>
 <td class="data">On <fmt:date fmt="d" date="${txreq.date}" />, you have requested a change of Equipment Program to the <span class="bld">${txreq.equipmentType}</span> program.<c:if test="${!empty checkRide}"> A
${checkRide.equipmentType} Check Ride was assigned on <fmt:date date="${checkRide.date}" fmt="d" />.</c:if>
<c:if test="${txAccess.canDelete}"> <span class="small"><el:cmd className="bld" url="txreqdelete" link="${txreq}">CLICK HERE</el:cmd> to withdraw this Transfer Request.</span></c:if></td>
</tr>
</c:if>
<c:if test="${(!empty txreq) && txreq.ratingOnly}">
<tr>
 <td class="mid bld">Request Additional Rating</td>
 <td class="data">On <fmt:date fmt="d" date="${txreq.date}" />, you have requested additional equipment type ratings in the <span class="bld">${txreq.equipmentType}</span> program.<c:if test="${!empty checkRide}"> A
${checkRide.equipmentType} Check Ride was assigned on <fmt:date date="${checkRide.date}" fmt="d" />.</c:if>
<c:if test="${txAccess.canDelete}"> <span class="small"><el:cmd className="bld" url="txreqdelete" link="${txreq}">CLICK HERE</el:cmd> to withdraw this Transfer Request.</span></c:if></td>
</tr>
</c:if>
<c:if test="${txPending && (empty txreq)}">
<tr>
 <td class="mid bld">Additional Rating Request</td>
 <td class="data">You currently have a pending additional ratings request with a partner virtual airline.</td>
</tr>
</c:if>
<tr>
 <td class="mid"><el:cmd className="bld" url="promoeligibility">Review Promotion Eligibility</el:cmd></td>
 <td class="data">You can review the different Equipment programs at <content:airline /> to see what you need in order to switch programs or request additional equipment type ratings.</td>
</tr>
<tr>
<c:if test="${(pilot.legs >= 5) && !pilot.noExams}">
 <td class="mid"><el:cmd className="bld" url="testcenter">Testing Center</el:cmd></td>
</c:if>
<c:if test="${pilot.legs < 5 || pilot.noExams}">
 <td class="mid bld">Testing Center</td>
</c:if>
 <td class="data">The <content:airline /> Testing Center is your single source for the written examinations needed for promotions and additional type ratings. Here you can see your prior tests 
and their results, in addition to writing new aircraft tests.<c:if test="${pilot.proficiencyCheckRides}"> You may also schedule a currency Check Ride from the Testing Center. </c:if>
<c:if test="${examLockout}"><span class="sec bld">You completed a <content:airline /> pilot Examination with an unsatisfactory score less than <fmt:int value="${examLockoutHours}" /> hours ago, and therefore 
cannot write a new Examination until this interval has passed.</span></c:if>
<c:if test="${pilot.legs < 5}"><span class="sec bld">As a new <content:airline /> pilot, you will be eligible to take written examinations once you have completed 5 flights.</span></c:if></td>
</tr>
<c:if test="${hasSC && (pilot.legs >= scMinFlights) && (pilotAge >= scMinAge)}">
<tr>
 <td class="mid"><el:cmd className="bld" url="scnomcenter">Senior Captain Nominations</el:cmd></td>
 <td class="data">Promotion to the rank of Senior Captain is the highest individual achievement a <content:airline /> pilot can obtain. You can nominate up to <fmt:int value="${scMaxNoms}" /> individuals for promotion to
 Senior captain each calendar quarter. Please note that the final decision on promotions to Senior Captain is the exclusive perogative of the <content:airline /> Senior Staff.</td>
</tr>
</c:if>

<c:if test="${academyEnabled}">
<content:attr attr="isAcademyAdmin" value="true" roles="AcademyAdmin,AcademyAudit,Instructor,HR" />
<c:set var="academyNoFlights" value="${(pilot.legs < academyFlights) && !isAcademyAdmin}" scope="page" />
<!-- Flight Academy Section -->
<tr class="title caps">
 <td colspan="2"><content:airline /> FLIGHT ACADEMY</td>
</tr>
<tr>
<c:if test="${!academyNoFlights}">
 <td class="mid"><el:cmd className="bld" url="academy">Flight Academy</el:cmd></td>
</c:if>
<c:if test="${academyNoFlights}">
 <td class="mid bld">Flight Academy</td>
</c:if>
 <td class="data">The <content:airline /> Flight Academy is our official Pilot training program. The Academy is available to assist all members regardless of experience, skill level or ratings. 
The spectrum of training spans from formal flight instruction to informal mentoring to address specific issues like online flying, VOR tracking, ATC procedures, improved landings.<c:if test="${!empty courses}"><br />
<br />
You have completed or are enrolled in the following <content:airline /> Flight Academy courses: <span class="sec bld"><fmt:list value="${courses}" delim=", " /></span>.</c:if>
<c:if test="${!empty course}"><br />
You are currently enrolled in the <el:cmd url="course" link="${course}" className="pri bld">${course.name}</el:cmd> Flight Academy course.</c:if>
<c:if test="${academyNoFlights}"><br />
<br />
<span class="ita">You cannot enroll in a <content:airline /> Flight Academy course until you have successfully completed <fmt:int value="${academyFlights}" /> Flight legs.</span></c:if>
<c:if test="${!empty vatsim_ratings}">
<br /><br />
You have obtained the following VATSIM Pilot Ratings:<br />
<br />
<c:forEach var="pr" items="${vatsim_ratings}" varStatus="prStatus"><span class="pri bld">${pr.ratingCode}</span>, issued by <span class="ter bld">${pr.ATOName}</span> on <fmt:date fmt="d" date="${pr.issueDate}" />.<c:if test="${!prStatus.last}"><br /></c:if></c:forEach>
</c:if></td>
</tr>
<c:if test="${!empty course}">
<tr>
 <td class="mid"><el:cmd url="academycalendar" className="bld">Instruction Calendar</el:cmd></td>
 <td class="data">The <content:airline /> Flight Academy Instruction Calendar allows you to schedule a training session with a Flight Academy instructor to meet online for test flights, check rides and other 
situations where a virtual Flight Instructor can asisst you in your development.</td>
</tr>
</c:if>
<content:filter roles="HR,Instructor,AcademyAdmin,AcademyAudit">
<tr>
 <td class="mid"><el:cmd url="courses" className="bld">Active Courses</el:cmd></td>
 <td class="data">You can view Pilots currently enrolled within a <content:airline /> Flight Academy training course.</td>
</tr>
<tr>
 <td class="mid"><el:cmd url="academyridequeue" className="bld">Submitted Check Rides</el:cmd></td>
 <td class="data">You can view submitted <content:airline /> Flight Academy Check Rides that are awaiting scoring by a Flight Academy Examiner.</td>
</tr>
<tr>
 <td class="mid"><el:cmd url="arscripts" className="bld">Check Ride Scripts</el:cmd></td>
 <td class="data">You can view <content:airline /> Flight Academy Check Ride scripts.</td>
</tr>
<tr>
 <td class="mid"><el:cmd url="coursequeue" className="bld">Completed Courses</el:cmd></td>
 <td class="data">You can view <content:airline /> Flight Academy Courses that are ready for a Certification to be granted, since all Course requirements have been met.</td>
</tr>
<tr>
 <td class="mid"><el:cmd url="certs" className="bld">Certification Profiles</el:cmd></td>
 <td class="data">You can view and/or modify Flight Academy certification profiles here, to change the requirements, necessary examinations and prerequisites for each <content:airline /> Flight Academy 
pilot Certification.</td>
</tr>
<c:if test="${academyInsFlights}">
<tr>
 <td class="mid"><el:cmd url="inslogbook" className="bld">Instruction Logbook</el:cmd></td>
 <td class="data">You can view instruction flight log books for the <content:airline /> Flight Academy.</td>
</tr>
</c:if>
<c:if test="${empty course}">
<tr>
 <td class="mid"><el:cmd url="academycalendar" className="bld">Instruction Calendar</el:cmd></td>
 <td class="data">The <content:airline /> Flight Academy Instruction Calendar allows you to schedule a training session with a Flight Academy student.</td>
</tr>
</c:if>
</content:filter>
</c:if>
<content:filter roles="HR,PIREP,Examination,AcademyAdmin,Operations">
<!-- Human Resources Admin Section -->
<tr class="title caps">
 <td colspan="2">HUMAN RESOURCES</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="massmail">Group E-Mail</el:cmd></td>
 <td class="data">You can send an e-mail message to a group of pilots in a single equipment program, or to the entire airline.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="accomplishments">Pilot Accomplishments</el:cmd></td>
<content:filter roles="!HR">
 <td class="data">You can view, create and modify Pilot Accomplishment profiles.</td>
</content:filter>
<content:filter roles="HR">
 <td class="data">You can view Pilot Accomplishment profiles.</td>
</content:filter>
</tr>
</content:filter>
<tr>
 <td class="mid"><el:cmd className="bld" url="dashboard">Performance Dashboard</el:cmd></td>
 <td class="data">You can view performance metrics on Flight Report approval, Pilot Examination and Check Ride scoring delays and Flight Report logging.</td>
</tr>
<content:filter roles="HR,Operations">
<tr>
 <td class="mid"><el:cmd className="bld" url="accomplishrecalc">Recalculate Pilot Accomplishments</el:cmd></td>
 <td class="data">You can recalculate which pilots are eligible for a specific Pilot Accomplishment.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="eqtypes">Equipment Type Programs</el:cmd></td>
 <td class="data">You can add new equipment programs, or modify existing programs to change Chief Pilots, automatic additional ratings or equipment types for flight legs
 that qualify for promotion to Captain.</td>
</tr>
</content:filter>
<content:filter roles="HR,Operations,PIREP">
<c:if test="${hasSC}">
<tr>
 <td class="mid"><el:cmd className="bld" url="scnomcenter">Senior Captain Nominations</el:cmd></td>
 <td class="data">You can view the status of <content:airline /> Senior Captain nominations for the current calendar quarter here.</td>
</tr>
</c:if>
</content:filter>
<content:filter roles="HR">
<tr>
 <td class="mid"><el:cmd className="bld" url="careers">Career Opportunities</el:cmd></td>
 <td class="data">You can view any <content:airline /> volunteer staff Job Posting here, and create new Job Postings for <content:airline /> staff members and Pilots to view.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="jobapplist">Job Applications</el:cmd></td>
 <td class="data">You can view any applications for current or historic <content:airline /> volunteer staff Job Postings.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="applicants">Pilot Registration</el:cmd></td>
 <td class="data">This section tracks applicants to <content:airline /> before they are hired on as Pilots.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="questionnaires">Applicant Questionnaires</el:cmd></td>
 <td class="data">You can view and score pending Applicant Questionnaires.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="dupeSearch">Duplicate Pilots</el:cmd></td>
 <td class="data">You can search for and merge duplicate pilot names and IDs into a single <content:airline /> Pilot profile.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="suspendedusers">Suspended Pilots</el:cmd></td>
 <td class="data">You can view all suspended <content:airline /> pilots and the lengths of their membership suspensions.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="inactivelist">Inactivity Purge Preview</el:cmd></td>
 <td class="data">You can view a preview of the next run of the Pilot Inactivity purge scheduled task.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="loginaddrs">Login Addresses</el:cmd></td>
 <td class="data">You can search the system logs to determine which user accounts have been access from a particular IP address, host name or address range.</td> 
</tr>
</content:filter> 
<content:filter roles="HR,Examination,TestAdmin,Operations">
<tr class="title caps">
 <td colspan="2">PILOT EXAMINATIONS</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="eprofiles">Examination Profiles</el:cmd></td>
 <td class="data">You can add new written examinations or modify the examinations to
 change the number of questions, passing score, or additional ratings granted upon
 successful completion of an examination.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="qprofiles" linkID="ALL">Examination Question Profiles</el:cmd></td>
 <td class="data">You can add new examination questions or modify existing examination questions.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="autoscoredexams">Automatically Scored Examinations</el:cmd></td>
 <td class="data">You can review all automatically scored multiple choice examinations.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="crscripts">Check Ride Scripts</el:cmd></td>
 <td class="data">Check Ride scripts allow you to save pre-defined check ride descriptions for different 
aircraft types, for easy reuse when assigning a Check Ride to a pilot.</td>
</tr>
</content:filter>

<content:filter roles="Schedule,Operations">
<!-- Schedule Admin Section -->
<tr class="title caps">
 <td colspan="2">FLIGHT SCHEDULE</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="airlines">Update Airlines</el:cmd></td>
 <td class="data">You can modify the Airline profiles contained within the <content:airline /> Flight Schedule. <span class="small ita">This information is shared between all web applications.</span></td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="airports">Update Airports</el:cmd></td>
 <td class="data">You can modify the Airport profiles contained within the <content:airline /> Flight Schedule. <span class="small ita">This information is shared between all web applications.</span></td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="aircraftlist">Update Aircraft</el:cmd></td>
 <td class="data">You can update Aircraft profiles contained within the <content:airline /> Flight Schedule. <span class="small ita">This information is shared between all web applications.</span></td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="schedimport">Import Flight Schedule</el:cmd><br />
<el:cmd className="bld" url="schedexport">Export Flight Schedule</el:cmd></td>
 <td class="data">You can import entries into the <content:airline /> Flight Schedule database from a CSV data file. You may also export entries from the Flight Schedule into a CSV data file.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="schedsync">Synchronize Flight Schedule</el:cmd></td>
 <td class="data">You may synchronize flight schedules (on an airline by airline basis) with other virtual
 airlines running on this server.</td>
</tr>
<c:if test="${!empty faaChartURL}">
<tr>
 <td class="mid"><el:cmd className="bld" url="faachartdl">FAA Approach Chart Download</el:cmd></td>
 <td class="data">You can import Approach Charts from the FAA web site.</td>
</tr>
</c:if>
<c:if test="${innovataEnabled}">
<tr>
 <td class="mid"><el:cmd className="bld" url="ivimport">Innovata Schedule Download</el:cmd></td>
 <td class="data"><content:airline /> has partnered with Innovata, LLC to provide instant real-world schedule updates which can be downloaded via FTP and imported into the Flight Schedule. If newer
 schedule data is available on Innovata's servers, it will be downloaded.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="ivstatus">Innovata Schedule Download Status</el:cmd></td>
 <td class="data">You can view a status report from the last Innovata, LLC schedule download, to list new airports or equipment codes that need to be added to the <content:airline /> Flight Schedule.</td>
</tr>
</c:if>
<c:if test="${vaBaseEnabled}">
<tr>
 <td class="mid"><el:cmd className="bld" url="vaimport">VABase Schedule Import</el:cmd><br />
<el:cmd className="bld" url="vafilter">VABase Schedule Load</el:cmd></td>
 <td class="data">You can import a VABase-formatted airline schedule into the <content:airline /> flight schedule, and load the current day's flights.</td>
</tr>
</c:if>
<tr>
 <td class="mid"><el:cmd className="bld" url="usvcairports">Unserviced Airports</el:cmd></td>
 <td class="data">As the <content:airline /> Flight Schedule is updated, certain airports may no longer be served by a particular Airline. This will display Airports with no corresponding flights in the
 Flight Schedule so that their servicing Airlines may be updated.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="navimport">Navigation Data</el:cmd><br />
<el:cmd className="bld" url="awyimport">Airway Data</el:cmd><br />
<el:cmd className="bld" url="trouteimport">Terminal Routes</el:cmd></td>
 <td class="data">You can import and purge AIRAC data stored within the <content:airline /> Navigation Data database. AIRAC data can be imported in one of three ways - Navigation Aids, Airways and Terminal Routes (SIDs / STARs).</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="airspaceimport">Airspace Data</el:cmd></td>
 <td class="data">You can import and update airspace boundary definitions in the <content:airline /> Navigation Data database.</td>
</tr>
<c:if test="${acarsEnabled}">
<tr>
 <td class="mid"><el:cmd className="bld" url="dsptrouteupdate">Dispatch SID/STAR Update</el:cmd></td>
 <td class="data">You can update the <content:airline /> ACARS Dispatch Route Terminal Routes. This should be performed each time the Terminal Routes have been imported from AIRAC data.</td>
</tr>
</c:if>
</content:filter>
<c:if test="${acarsEnabled}">
<content:filter roles="HR,Route,Dispatch,PIREP">
<!-- ACARS Dispatch Section -->
<tr class="title caps">
 <td colspan="2">ACARS DISPATCH OPERATIONS</td>
</tr>
<c:if test="${hasDispatchAccess}">
<tr>
 <td class="mid"><el:cmd className="bld" url="dsproutes">Dispatcher Routes</el:cmd></td>
 <td class="data">You can view or delete flight routes previously created by <content:airline /> dispatchers.</td>
</tr>
</c:if>
<tr>
 <td class="mid"><el:cmd className="bld" url="dsprsearch">Dispatch Route Search</el:cmd></td>
 <td class="data">You can search <content:airline /> dispatcher routes by Origin and Destination airport.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="dspcalendar">Dispatcher Service Calendar</el:cmd></td>
 <td class="data">This calendar allows <content:airline /> ACARS Dispatchers to announce the times they will be providing Dispatch services.</td>
</tr>
<c:if test="${hasDispatchAccess}">
<tr>
 <td class="mid"><el:cmd className="bld" url="poproutes">Popular Flight Routes</el:cmd></td>
 <td class="data">You can sort route pairs based on popularity, to determine popular routes not covered by routes in the <content:airline /> ACARS Dispatch database.</td>
</tr>
</c:if>
<content:filter roles="HR,Route">
<tr>
 <td class="mid"><el:cmd className="bld" url="dsprouteplot">Dispatch Route Plotter</el:cmd></td>
 <td class="data">You can graphically plot new routes to add into the <content:airline /> Dispatch Route database.</td>
</tr>
</content:filter>
</content:filter>
<content:filter roles="HR,Developer">
<!-- ACARS Logging Section -->
<tr class="title caps">
 <td colspan="2">ACARS SERVER DATA</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="acarslogc">Connection Log</el:cmd></td>
 <td class="data">You can view the <content:airline /> ACARS server Connection log.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="acarslogf">Flight Information Log</el:cmd></td>
 <td class="data">You can view flight information from the <content:airline /> ACARS server flight log.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="acarserrors">Client Error Log</el:cmd></td>
 <td class="data">You can view error log submissions from <content:airline /> ACARS 2.0 clients.</td>
</tr>
<content:filter roles="HR">
<tr>
 <td class="mid"><el:cmd className="bld" url="acarslogm">Text Message Log</el:cmd></td>
 <td class="data">You can view the <content:airline /> ACARS server text message log.</td>
</tr>
</content:filter>
</content:filter>
</c:if>
<content:filter roles="HR,Developer,Operations">
<!-- System Admin Section -->
<tr class="title caps">
 <td colspan="2">SYSTEM ADMINISTRATION</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="msgtemplates">E-Mail Message Templates</el:cmd></td>
 <td class="data">The <content:airline /> web site sends out e-mail notification messages to inform members of new Online Events, System News entries and NOTAMs, and Flight Report approval or rejection.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="metadata">System Metadata</el:cmd></td>
 <td class="data">You can view system-generated metadata for the <content:airline /> web site.</td>
</tr>
</content:filter>
<content:filter roles="Developer,Operations">
<tr>
 <td class="mid"><el:cmd className="bld" url="ip6import">IPv6 Netblock Import</el:cmd></td>
 <td class="data">You can import GeoCities IPv6 City network block data files into the database.</td>
</tr>
</content:filter>
<content:filter roles="HR,Developer">
<c:if test="${acarsEnabled}">
<tr>
 <td class="mid"><el:cmd className="bld" url="liveries">ACARS Multi-Player Liveries</el:cmd></td>
 <td class="data">You can update multi-player liveries for the <content:airline /> ACARS server.</td>
</tr>
</c:if>
</content:filter>
<content:filter roles="HR">
<c:if test="${hasIMAP}">
<tr>
 <td class="mid"><el:cmd className="bld" url="imaplist">IMAP Mailbox Profiles</el:cmd></td>
 <td class="data">You can view and update profiles for hosted IMAP mailboxes.</td>
</tr>
</c:if>
<tr>
 <td class="mid"><el:cmd className="bld" url="examstats">Examination / Check Ride Statistics</el:cmd></td>
 <td class="data">You can view performance metrics on Pilot Examination and Check Ride scoring.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="memberstats">Membership Statistics</el:cmd></td>
 <td class="data">You can view statistics about the <content:airline /> membership and how long they have been members of the <content:airline />.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="rejectedpireps">Rejected Flight Reports</el:cmd></td>
 <td class="data">You can view rejected <content:airline /> Flight Reports.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="permusers">Permanent User Accounts</el:cmd></td>
 <td class="data">You can view a list of <content:airline /> Pilots with permanent user accounts.</td>
</tr>
</content:filter>
<content:filter roles="HR,Operations">
<tr>
 <td class="mid"><el:cmd className="bld" url="changelog">Change Log</el:cmd></td>
 <td class="data">You can view the Change Log summarizing modifications to certain common Airline data.</td>
</tr>
</content:filter>
<content:filter roles="Admin">
<tr>
 <td class="mid"><el:cmd className="bld" url="diag">Diagnostics</el:cmd></td>
 <td class="data">You can view information about the application server and Java Virtual Machine.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="tzones">Time Zones</el:cmd></td>
 <td class="data">You can view and update Time Zone profiles.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="securityroles">Security Roles</el:cmd></td>
 <td class="data">You can view all <content:airline /> Pilots who have one or more web application security roles.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="cmdstats">Command Statistics</el:cmd></td>
 <td class="data">You can view statistics about Web Site Command invocations, application server and database usage times here.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="cmdlog">Command Log</el:cmd></td>
 <td class="data">You can view and search for entries in the Command Log database.</td>
</tr>
<c:if test="${ts2Enabled}">
<tr>
 <td class="mid"><el:cmd className="bld" url="ts2servers">TeamSpeak 2 Virtual Servers</el:cmd><br />
<el:cmd className="bld" url="ts2channels">TeamSpeak 2 Voice Channels</el:cmd></td>
 <td class="data">You can update TeamSpeak 2 virtual server permissions and configurations, and create, edit or delete TeamSpeak 2 voice channels.</td>
</tr>
</c:if>
<c:if test="${mvsEnabled}">
<tr>
 <td class="mid"><el:cmd className="bld" url="mvschannels">MVS Voice Channels</el:cmd></td>
 <td class="data">You can update Modern Voice Server permanent channels.</td>
</tr>
</c:if>
</content:filter>
<tr class="title"><td colspan="2">&nbsp;</td></tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
