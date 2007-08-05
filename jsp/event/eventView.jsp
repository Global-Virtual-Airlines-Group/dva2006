<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Event - ${event.name}</title>
<content:sysdata var="airlineName" name="airline.name" />
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:rss title="${airlineName} Online Events" path="/event_rss.ws" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.eqType, 'Equipment Type')) return false;
if (!validateCombo(form.route, 'Flight Route')) return false;

setSubmit();
disableButton('SaveButton');
disableButton('EditButton');
disableButton('RouteButton');
disableButton('PlanButton');
disableButton('CancelButton');
disableButton('AssignButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>
<content:sysdata var="airports" name="airports" />

<!-- Main Body Frame -->
<content:region id="main">
<c:if test="${access.canSignup}">
<c:set var="formAction" value="eventsignup.do" scope="request" />
<c:set var="formValidate" value="return validate(this)" scope="request" />
</c:if>
<c:if test="${!access.canSignup}">
<c:set var="formAction" value="event.do" scope="request" />
<c:set var="formValidate" value="return false" scope="request" />
</c:if>
<el:form action="${formAction}" method="post" link="${event}" validate="${formValidate}">
<el:table className="form view" pad="default" space="default">
<tr class="title caps">
 <td colspan="6" class="left">${event.name} - <fmt:date date="${event.startTime}" d="EEEE MMMM dd yyyy" t="HH:mm" /> -
 <fmt:date date="${event.endTime}" fmt="t" t="HH:mm" /></td>
</tr>
<tr>
 <td class="label">Online Network</td>
 <td colspan="5" class="data sec bld">${event.networkName}</td>
</tr>
<c:if test="${event.canSignup}">
<tr>
 <td class="label">Signups Close on</td>
 <td colspan="5" class="data"><fmt:date date="${event.signupDeadline}" /></td>
</tr>
</c:if>
<c:if test="${!empty event.routes}">
<tr class="title caps">
 <td colspan="6" class="left">AVAILABLE FLIGHT ROUTES</td>
</tr>
<c:set var="entryNumber" value="${0}" scope="request" />
<c:forEach var="route" items="${event.routes}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="request" />
<view:row entry="${route}">
 <td class="label" valign="top" rowspan="2">Route #<fmt:int value="${entryNumber}" /></td>
 <td class="data" colspan="5">${route.airportD.name} (<fmt:airport airport="${route.airportD}" />) - ${route.airportA.name}
 (<fmt:airport airport="${route.airportA}" />)</td>
</view:row>
<view:row entry="${route}">
 <td class="data" colspan="5">${route.route}</td>
</view:row>
</c:forEach>
</c:if>
<tr>
 <td class="label" valign="top">Flight Briefing</td>
 <td colspan="5" class="data"><el:textbox name="briefing" readOnly="true" width="90%" height="8">${event.briefing}</el:textbox></td>
</tr>
<c:if test="${!empty event.equipmentTypes}">
<tr>
 <td class="label">Equipment Types</td>
 <td class="data" colspan="5"><fmt:list value="${event.equipmentTypes}" delim=", " /></td>
</tr>
</c:if>
<content:filter roles="Event,HR"><c:if test="${!empty event.contactAddrs}">
<tr>
 <td class="label" valign="top">ATC Contact Addresses</td>
 <td colspan="5" class="data"><c:forEach var="addr" items="${event.contactAddrs}">
<el:link url="mailto:${addr}">${addr}</el:link><br /></c:forEach></td>
</tr>
</c:if></content:filter>
<content:filter roles="Pilot">
<c:if test="${!empty event.charts}">
<!-- Chart Section -->
<tr class="title caps">
 <td colspan="6" class="left">NAVIGATION CHARTS - <fmt:int value="${fn:sizeof(event.charts)}" /> CHARTS</td>
</tr>
<tr class="title caps">
 <td colspan="2">CHART NAME</td>
 <td colspan="2">AIRPORT</td>
 <td>IMAGE TYPE</td>
 <td>CHART TYPE</td>
</tr>
<c:forEach var="chart" items="${event.charts}">
<c:set var="hasPDF" value="${chart.imgTypeName == 'PDF'}" scope="request" />
<c:set var="cAirport" value="${airports[chart.airport.IATA]}" scope="request" />
<view:row entry="${chart}">
<c:choose>
<c:when test="${hasPDF}">
 <td colspan="2"><el:link url="/charts/${chart.hexID}.pdf" className="bld" target="chartView">${chart.name}</el:link></td>
 <td colspan="2">${cAirport.name} (<fmt:airport airport="${cAirport}" />)</td>
 <td>Adobe PDF</td>
 <td class="sec">${chart.typeName}</td>
</c:when>
<c:otherwise>
 <td colspan="2" class="pri bld"><el:cmd url="chart" link="${chart}">${chart.name}</el:cmd></td>
 <td colspan="2">${cAirport.name} (<fmt:airport airport="${cAirport}" />)</td>
 <td>${chart.imgTypeName}</td> 
 <td class="sec">${chart.typeName}</td>
</c:otherwise>
</c:choose>
</view:row>
</c:forEach>
</c:if>

<c:if test="${!empty event.plans}">
<!-- Flight Plans Section -->
<tr class="title caps">
 <td colspan="6" class="left">FLIGHT PLANS - <fmt:int value="${fn:sizeof(event.plans)}" /> PLANS</td>
</tr>
<tr class="title caps">
 <td colspan="4">FLIGHT PLAN NAME</td>
 <td>SIZE</td>
 <td>PLAN TYPE</td>
</tr>

<c:set var="hexEventID" value="${fn:hex(event.ID)}" scope="request" />
<c:forEach var="plan" items="${event.plans}">
<view:row entry="${plan}">
 <td colspan="4"><el:link url="/fplan/${hexEventID}/${plan.fileName}">${plan.airportD.name} - ${plan.airportA.name}</el:link></td>
 <td class="sec"><fmt:int value="${plan.size}" /> bytes</td>
 <td class="pri bld">${plan.typeName}</td>
</view:row>
</c:forEach>
</c:if>
</content:filter>

<c:if test="${event.canSignup}">
<!-- Signups Section -->
<tr class="title caps">
 <td colspan="6" class="left">PARTICIPATING PILOT LIST - <fmt:int value="${fn:sizeof(event.signups)}" /> PILOTS</td>
</tr>
<tr class="title caps mid">
 <td width="10%">ID</td>
 <td width="30%">PILOT NAME</td>
 <td width="10%">EQUIPMENT</td>
 <td width="10%">${event.networkName} ID</td>
 <td colspan="2">FLIGHT ROUTE</td>
</tr>

<c:if test="${!empty event.signups}">
<c:set var="idx" value="${-1}" scope="request" />
<c:forEach var="signup" items="${event.signups}">
<c:set var="idx" value="${idx + 1}" scope="request" />
<c:set var="pilot" value="${pilots[signup.pilotID]}" scope="request" />
<c:set var="pilotCerts" value="${certs[signup.pilotID]}" scope="request" />
<c:set var="pilotLoc" value="${userData[signup.pilotID]}" scope="request" />
<c:set var="sa" value="${saAccess[signup.pilotID]}" scope="request" />
<tr class="mid">
<c:if test="${sa.canRelease}">
 <td><el:cmdbutton url="eventrelease" link="${event}" op="${fn:hex(pilot.ID)}" label="RELEASE" /></td>
</c:if>
<c:if test="${!sa.canRelease}">
 <td class="pri bld">${pilot.pilotCode}</td>
</c:if>
 <td><el:profile location="${pilotLoc}">${pilot.name}</el:profile>
<c:if test="${!empty pilotCerts}"><span class="ter bld"><fmt:list value="${pilotCerts}" delim="," /></span></c:if></td>
 <td class="sec bld">${signup.equipmentType}</td>
 <td class="pri bld">${pilot.networkIDs[event.networkName]}</td>
 <td colspan="2" class="small">${signup.airportD.name} (<fmt:airport airport="${signup.airportD}" />) - ${signup.airportA.name}
 (<fmt:airport airport="${signup.airportA}" />)</td>
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
 <td>EQUIPMENT</td>
 <td>FLIGHT NUMBER</td>
 <td colspan="2">FLIGHT ROUTE</td>
</tr>

<!-- Flight Report data -->
<c:forEach var="pirep" items="${pireps}">
<c:set var="pilot" value="${pilots[fn:PilotID(pirep)]}" scope="request" />
<view:row entry="${pirep}">
 <td class="bld"><el:cmd url="pirep" link="${pirep}"><fmt:date fmt="d" date="${pirep.date}" default="NOT FLOWN" /></el:cmd></td>
 <td><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td class="sec bld">${pirep.equipmentType}</td>
 <td>${pirep.flightCode}</td>
 <td colspan="2">${pirep.airportD.name} (<fmt:airport airport="${pirep.airportD}" />) - ${pirep.airportA.name}
 (<fmt:airport airport="${pirep.airportA}" />)</td>
</view:row>
</c:forEach>

<!-- Legend Bar -->
<tr class="title">
 <td colspan="6"><view:legend width="100" labels="Draft,Submitted,Held,Approved,Rejected" classes="opt2,opt1,warn, ,err" /></td>
</tr>
</c:if>

<!-- Signup Section -->
<tr class="title caps">
 <td colspan="6" class="left">SIGN UP FOR THIS EVENT</td>
</tr>
<c:if test="${access.canSignup}">
<tr>
 <td class="label">Flight Route</td>
 <td class="data" colspan="2"><el:combo name="route" idx="*" size="1" options="${event.activeRoutes}" firstEntry="-" className="req" /></td>
 <td class="label" valign="top" rowspan="2">Remarks</td> 
 <td class="data" rowspan="2" colspan="2"><el:textbox name="body" idx="*" width="95%" height="2"></el:textbox></td>
</tr>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data" colspan="2"><el:combo name="eqType" idx="*" size="1" options="${!empty event.equipmentTypes ? event.equipmentTypes : user.ratings}" className="req" firstEntry="-" /></td>
</tr>
</c:if>
<c:if test="${!event.canSignup}">
<tr>
 <td colspan="6" class="pri bld">This Online Event is posted for informational purposes only, and signups
 are not currently available.</td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td>&nbsp;
<c:if test="${access.canSignup}">
 <el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SIGN UP FOR THIS EVENT" />
</c:if>
<c:if test="${access.canAddPlan}">
 <el:cmdbutton ID="PlanButton" url="eventplan" link="${event}" label="ADD FLIGHT PLAN" />
 <el:cmdbutton ID="RouteButton" url="eventroutes" link="${event}" label="UPDATE ROUTES" />
</c:if>
<c:if test="${access.canEdit}">
 <el:cmdbutton ID="EditButton" url="eventedit" link="${event}" label="EDIT EVENT" />
</c:if>
<c:if test="${access.canAssignFlights}">
 <el:cmdbutton ID="AssignButton" url="eventassign" link="${event}" label="ASSIGN FLIGHTS FOR THIS EVENT" />
</c:if>
<c:if test="${access.canCancel}">
 <el:cmdbutton ID="CancelButton" url="eventcancel" link="${event}" label="CANCEL EVENT" />
</c:if>
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
