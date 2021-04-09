<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Online Event - ${event.name}</title>
<content:canonical convertID="true" />
<content:sysdata var="airlineName" name="airline.name" />
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:rss title="${airlineName} Online Events" path="/event_rss.ws" />
<script>
golgotha.local.validate = function(f) {
    if (!golgotha.form.check()) return false;
    golgotha.form.validate({f:f.eqType, t:'Equipment Type'});
    golgotha.form.validate({f:f.route, t:'Flight Route'});
    golgotha.form.submit(f);
    return true;
};

golgotha.local.resizeBriefing = function(maxRows) {
	const txt = document.forms[0].briefing;
	golgotha.form.resize(txt);
	txt.rows = Math.min(txt.rows, maxRows);
	return true;
};

golgotha.onDOMReady(function() { golgotha.local.resizeBriefing(20); });
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>
<content:sysdata var="airports" name="airports" />
<content:attr attr="showStats" value="true" roles="Event,HR" />

<!-- Main Body Frame -->
<content:region id="main">
<c:if test="${access.canSignup}">
<c:set var="formAction" value="eventsignup.do" scope="page" />
<c:set var="formValidate" value="return golgotha.form.wrap(golgotha.local.validate, this)" scope="page" />
</c:if>
<c:if test="${!access.canSignup}">
<c:set var="formAction" value="event.do" scope="page" />
<c:set var="formValidate" value="return false" scope="page" />
</c:if>
<el:form action="${formAction}" method="post" link="${event}" validate="${formValidate}">
<el:table className="form view">
<tr class="title caps">
 <td colspan="6" class="left">${event.name} - <fmt:date date="${event.startTime}" d="EEEE MMMM dd yyyy" t="HH:mm" /> - <fmt:date date="${event.endTime}" fmt="t" t="HH:mm" /></td>
</tr>
<c:if test="${event.hasBanner}">
<tr class="nophone">
 <td colspan="6" class="mid"><img alt="${event.name} Banner" src="/event/${event.hexID}" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Online Network</td>
 <td colspan="5" class="data pri bld">${event.network}</td>
</tr>
<c:if test="${fn:sizeof(event.airlines) > 1}">
<tr>
 <td class="label">Organized by</td>
 <td colspan="5" class="data bld">${event.owner.name}</td> 
</tr>
<tr>
 <td class="label">Airlines</td>
 <td colspan="5" class="data"><c:forEach var="airline" items="${event.airlines}" varStatus="st">${airline.name}<c:if test="${!st.last}">, </c:if></c:forEach></td>
</tr>
</c:if>
<c:if test="${event.canSignup}">
<tr>
 <td class="label">Signups Close on</td>
 <td colspan="5" class="data"><fmt:date date="${event.signupDeadline}" t="HH:mm" /></td>
</tr>
</c:if>
<c:if test="${!empty event.routes}">
<tr class="title caps">
 <td colspan="6" class="left">AVAILABLE FLIGHT ROUTES</td>
</tr>
<c:forEach var="route" items="${event.routes}">
<view:row entry="${route}">
 <td class="label top" rowspan="2">Route #<fmt:int value="${route.routeID}" /></td>
 <td class="data" colspan="5">${route.airportD.name} (<fmt:airport airport="${route.airportD}" />) - ${route.airportA.name} (<fmt:airport airport="${route.airportA}" />)<c:if test="${route.isRNAV}"> (RNAV)</c:if></td>
</view:row>
<view:row entry="${route}">
 <td class="data" colspan="5">${route.route}</td>
</view:row>
</c:forEach>
</c:if>
<c:choose>
<c:when test="${event.briefing.isPDF}">
<tr>
 <td class="label top">Flight Briefing</td>
 <td colspan="5" class="data"><el:link url="/ebrief/${event.hexID}"><el:img src="library/adobe.png" className="noborder" caption="Event Flight Briefing" x="32" y="32" /></el:link> <span class="small nophone"><fmt:int value="${event.briefing.size}" /> bytes, Adobe PDF document</span></td>
</tr>
</c:when>
<c:when test="${!empty event.briefing}">
<tr class="nophone">
 <td class="label top">Flight Briefing</td>
 <td colspan="5" class="data"><el:textbox name="briefing" readOnly="true" width="90%" height="2">${event.briefing.text}</el:textbox></td>
</tr>
</c:when>
</c:choose>
<c:if test="${!empty event.equipmentTypes}">
<tr>
 <td class="label">Equipment Types</td>
 <td class="data" colspan="5"><fmt:list value="${event.equipmentTypes}" delim=", " /></td>
</tr>
</c:if>
<c:if test="${!empty event.featuredAirports}">
<tr>
 <td class="label">Featured Airports</td>
 <td class="data" colspan="5"><c:forEach var="ap" items="${event.featuredAirports}" varStatus="st">${ap.name} (<fmt:airport airport="${ap}" />)<c:if test="${!st.last}">, </c:if></c:forEach></td>
</tr>
</c:if>
<content:filter roles="Event,HR"><c:if test="${!empty event.contactAddrs}">
<tr>
 <td class="label">ATC Contact Addresses</td>
 <td colspan="5" class="data"><c:forEach var="addr" items="${event.contactAddrs}" varStatus="st"><el:link url="mailto:${addr}">${addr}</el:link><c:if test="${!st.last}">, </c:if></c:forEach></td>
</tr>
</c:if></content:filter>
<content:filter roles="Pilot">
<c:if test="${!empty event.signupURL}">
<tr class="title caps">
 <td colspan="6" class="left">SIGN UP FOR THIS ONLINE EVENT</td>
</tr>
<tr>
 <td class="label">Signup URL</td>
 <td colspan="5" class="data"><el:link url="${event.signupURL}" external="true">${event.signupURL}</el:link></td>
</tr>
</c:if>
<c:if test="${!empty event.charts}">
<!-- Chart Section -->
<tr class="title caps">
 <td colspan="6" class="left">NAVIGATION CHARTS - <fmt:int value="${fn:sizeof(event.charts)}" /> CHARTS</td>
</tr>
<tr class="title caps">
 <td colspan="2">CHART NAME</td>
 <td colspan="2">AIRPORT</td>
 <td class="nophone">IMAGE TYPE</td>
 <td class="nophone">CHART TYPE</td>
</tr>
<c:forEach var="chart" items="${event.charts}">
<c:set var="hasPDF" value="${chart.imgType == 'PDF'}" scope="page" />
<c:set var="cAirport" value="${airports[chart.airport.IATA]}" scope="page" />
<view:row entry="${chart}">
<c:choose>
<c:when test="${hasPDF}">
 <td colspan="2"><el:link url="/charts/${chart.hexID}.pdf" className="bld" target="chartView">${chart.name}</el:link></td>
 <td colspan="2">${cAirport.name} (<fmt:airport airport="${cAirport}" />)</td>
 <td class="nophone">Adobe PDF</td>
 <td class="sec nophone">${chart.type}</td>
</c:when>
<c:otherwise>
 <td colspan="2" class="pri bld"><el:cmd url="chart" link="${chart}">${chart.name}</el:cmd></td>
 <td colspan="2">${cAirport.name} (<fmt:airport airport="${cAirport}" />)</td>
 <td class="nophone">${chart.imgType}</td> 
 <td class="sec nophone">${chart.type}</td>
</c:otherwise>
</c:choose>
</view:row>
</c:forEach>
</c:if>
<c:if test="${!empty event.dispatchRoutes}">
<!-- Dispatch Route Section -->
<tr class="title caps">
 <td colspan="6" class="left">ACARS DISPATCH ROUTES - <fmt:int value="${fn:sizeof(event.dispatchRoutes)}" /> ROUTES</td>
</tr>
<tr class="title caps">
 <td>ID</td>
 <td>AIRPORTS</td>
 <td>CREATED ON</td>
 <td>USED</td>
 <td colspan="2">FLIGHT ROUTE</td>
</tr>
<c:forEach var="drt" items="${event.dispatchRoutes}">
<view:row entry="${drt}">
 <td><el:cmd url="dsproute" link="${drt}" className="pri bld"><fmt:int value="${drt.ID}" /></el:cmd></td>
 <td class="small">${drt.airportD.name} (<fmt:airport airport="${drt.airportD}" />) to ${drt.airportA.name} (<fmt:airport airport="${drt.airportA}" />)</td>
 <td class="sec bld small"><fmt:date date="${drt.createdOn}" fmt="d" /></td>
 <td class="bld"><fmt:int value="${drt.useCount}" /></td>
 <td class="left small" colspan="2"><c:if test="${!empty drt.SID}">${drt.SID} </c:if>${drt.route}<c:if test="${!empty drt.STAR}"> ${drt.STAR}</c:if></td>
</view:row>
</c:forEach>
</c:if>
</content:filter>

<c:if test="${event.canSignup}">
<!-- Signups Section -->
<tr class="title caps">
 <td colspan="6" class="left">PARTICIPATING PILOT LIST - <fmt:int value="${fn:sizeof(event.signups)}" /> PILOTS
<c:if test="${!empty signupPredict}"><span class="nophone"> (EXPECTED TURNOUT - <fmt:int value="${signupPredict}" /> PILOTS)</span></c:if></td>
</tr>
<tr class="title caps mid">
 <td>ID</td>
 <td style="max-width:30%">PILOT NAME</td>
 <td class="nophone">EQUIPMENT</td>
 <td>${event.network} ID</td>
<c:if test="${showStats}">
 <td class="nophone">STATISTICS</td>
 <td>FLIGHT ROUTE</td>
</c:if>
<c:if test="${!showStats}">
 <td colspan="2">FLIGHT ROUTE</td>
</c:if>
</tr>

<c:if test="${!empty event.signups}">
<c:forEach var="signup" items="${event.signups}">
<c:set var="pilot" value="${pilots[signup.pilotID]}" scope="page" />
<c:set var="pilotCerts" value="${certs[signup.pilotID]}" scope="page" />
<c:set var="myCerts" value="${fn:filterCerts(pilotCerts, allCerts)}" scope="page" />
<c:set var="pilotRatings" value="${fn:filterRatings(allRatings[fn:networkID(pilot, event.network)], myCerts)}" scope="page" />
<c:set var="pilotLoc" value="${userData[signup.pilotID]}" scope="page" />
<c:set var="sa" value="${saAccess[signup.pilotID]}" scope="page" />
<c:set var="showPilotStats" value="${showStats && (pilot.eventSignups > 0)}" scope="page" />
<tr class="mid">
<c:if test="${sa.canRelease}">
 <td><el:cmdbutton url="eventrelease" link="${event}" op="${pilot.hexID}" label="RELEASE" /></td>
</c:if>
<c:if test="${!sa.canRelease}">
 <td class="pri bld">${pilot.pilotCode}</td>
</c:if>
 <td><el:profile location="${pilotLoc}">${pilot.name}</el:profile>
<c:if test="${!empty pilotCerts}">&nbsp;<span class="ter bld nophone"><fmt:list value="${pilotCerts}" delim=", " /></span></c:if>
<c:if test="${!empty pilotRatings}">&nbsp;<span class="sec nophone"><fmt:list value="${pilotRatings}" delim=", " /></span></c:if>
</td>
 <td class="sec bld nophone">${signup.equipmentType}</td>
 <td class="pri bld">${fn:networkID(pilot, event.network)}</td>
<c:if test="${showPilotStats}">
 <td class="small nophone"><fmt:int value="${pilot.eventSignups}" /> signups, <fmt:int value="${pilot.eventLegs}" /> legs (<fmt:dec value="${(pilot.eventLegs * 100.0) / pilot.eventSignups}" fmt="##0.0" />%)</td>
</c:if>
 <td<c:if test="${!showPilotStats}"> colspan="2"</c:if> class="small">${signup.airportD.name} (<fmt:airport airport="${signup.airportD}" />) - ${signup.airportA.name} (<fmt:airport airport="${signup.airportA}" />)</td>
</tr>
</c:forEach>
</c:if>
<c:if test="${empty event.signups}">
<tr>
 <td colspan="6" class="pri bld">No Pilots have signed up yet for this Online Event.</td>
</tr>
</c:if>
</c:if>
<c:if test="${!empty pireps}">
<!-- Flight Reports Section -->
<tr class="title caps">
 <td colspan="6" class="left">LOGGED FLIGHT REPORTS - <fmt:int value="${fn:sizeof(pireps)}" /> FLIGHT LEGS</td>
</tr>
<tr class="title caps">
 <td>DATE</td>
 <td>PILOT NAME</td>
 <td class="nophone">EQUIPMENT</td>
 <td class="nophone">FLIGHT NUMBER</td>
 <td class="nophone" colspan="2">FLIGHT ROUTE</td>
</tr>

<!-- Flight Report data -->
<c:forEach var="pirep" items="${pireps}">
<c:set var="pilotLoc" value="${userData[fn:PilotID(pirep)]}" scope="page" />
<c:set var="pilot" value="${pilots[fn:PilotID(pirep)]}" scope="page" />
<view:row entry="${pirep}">
 <td class="bld "><el:cmd domain="${pilotLoc.domain}" url="pirep" link="${pirep}"><fmt:date fmt="d" date="${pirep.date}" default="NOT FLOWN" /></el:cmd></td>
 <td><el:profile location="${pilotLoc}">${pilot.name}</el:profile></td>
 <td class="sec bld nophone">${pirep.equipmentType}</td>
 <td class="nophone">${pirep.flightCode}</td>
 <td colspan="2" class="small nophone">${pirep.airportD.name} (<fmt:airport airport="${pirep.airportD}" />) - ${pirep.airportA.name} (<fmt:airport airport="${pirep.airportA}" />)</td>
</view:row>
</c:forEach>

<!-- Legend Bar -->
<tr class="title">
 <td colspan="6"><view:legend width="100" labels="Draft,Submitted,Held,Approved,Rejected" classes="opt2,opt1,warn, ,err" /></td>
</tr>
</c:if>

<content:authUser var="user">
<c:if test="${!access.canSignup && event.canSignup && fn:isEventOpen(event) && (empty fn:networkID(user, event.network))}">
<!-- No Online ID Notice -->
<tr>
 <td colspan="6" class="mid"><span class="pri bld">You cannot sign up for this Online Event because you have not provided your ${event.network} ID.</span><br />
<br />
<el:cmd className="sec bld" url="profile" link="${user}" op="edit">Click Here</el:cmd> to update your <content:airline /> pilot profile.</td>
</tr>
</c:if>
</content:authUser>
<c:if test="${access.canSignup || (!event.canSignup)}">
<!-- Signup Section -->
<tr class="title caps">
 <td colspan="6" class="left">SIGN UP FOR THIS EVENT</td>
</tr>
<c:if test="${access.canSignup}">
<tr>
 <td class="label">Flight Route</td>
 <td class="data" colspan="2"><el:combo name="route" idx="*" size="1" options="${event.activeRoutes}" firstEntry="-" className="req" /></td>
 <td class="label top" rowspan="2">Remarks</td> 
 <td class="data" rowspan="2" colspan="2"><el:textbox name="body" idx="*" width="95%" height="2" resize="true"></el:textbox></td>
</tr>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data" colspan="2"><el:combo name="eqType" idx="*" size="1" options="${!empty event.equipmentTypes ? event.equipmentTypes : user.ratings}" className="req" firstEntry="-" /></td>
</tr>
</c:if>
<c:if test="${!event.canSignup}">
<tr>
 <td colspan="6" class="pri bld">This Online Event is posted for informational purposes only, and signups are not currently available.</td>
</tr>
</c:if>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;
<c:if test="${access.canSignup}"><el:button type="submit" label="SIGN UP FOR THIS EVENT" /></c:if>
<c:if test="${access.canEdit}">
&nbsp;<el:cmdbutton url="eventroutes" link="${event}" label="UPDATE ROUTES" />&nbsp;<el:cmdbutton url="eventedit" link="${event}" label="EDIT EVENT" /></c:if>
<c:if test="${access.canAssignFlights}">&nbsp;<el:cmdbutton url="eventassign" link="${event}" label="ASSIGN FLIGHTS FOR THIS EVENT" /></c:if>
<c:if test="${access.canBalance}">&nbsp;<el:cmdbutton url="eventbalance" link="${event}" label="BALANCE SIGNUPS" /></c:if>
<c:if test="${access.canCancel}">&nbsp;<el:cmdbutton url="eventcancel" link="${event}" label="CANCEL EVENT" /></c:if>
<c:if test="${access.canDelete}">&nbsp;<el:cmdbutton url="eventdelete" link="${event}" label="DELETE EVENT" /></c:if>
 </td>
</tr>
</el:table>
</el:form>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
