<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Center - ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="pilotCenter" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.navaidCode, 2, 'Navigation Aid Code')) return false;

setSubmit();
disableButton('NavSearchButton');
disableButton('METARButton');
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
<el:table ID="pilotCenter" className="form" pad="default" space="default">

<!-- Pilot Information -->
<tr class="title">
 <td colspan="2">PILOT CENTER - <span class="caps">${pilot.rank} ${pilot.firstName} ${pilot.lastName}</span></td>
</tr>
<tr>
 <td width="350" class="mid"><el:cmd className="bld" url="profile" linkID="0x${pilot.ID}" op="edit">Edit My Profile</el:cmd></td>
 <td class="data">Welcome back to <span class="pri"><content:airline /></span>, ${pilot.firstName}. Your
 pilot code is <span class="pri bld">${pilot.pilotCode}</span>.<br />
 You signed up on <fmt:date date="${pilot.createdOn}" fmt="d" /> and have visited ${pilot.loginCount} times.<br />
 You are visiting today from <b>${pageContext.request.remoteHost}</b> (${pageContext.request.remoteAddr}).</td>
</tr>
<c:if test="${access.canTakeLeave}">
<content:sysdata var="inactivity_days" name="users.inactive_days" />
<tr>
 <td class="mid"><el:cmd className="bld" url="loa">Request Leave of Absence</el:cmd></td>
 <td class="data">In order to remain an active pilot, you need to log into our web site at least once every
 ${inactivity_days} days. If you are unable to do so, you can request a <span class="pri bld">Leave of 
 Absence</span> by clicking on the link on the left.</td>
</tr>
</c:if>
<tr>
 <td class="mid">&nbsp;
<c:if test="${!empty acImage}"><el:img src="${acImage}" caption="${pilot.equipmentType}" /></c:if>
 </td>
 <td class="data">You are a <span class="pri bld">${pilot.rank}</span> in the <span class="sec bld">${pilot.equipmentType}</span>
 program. <span class="pri bld">(Stage ${eqType.stage})</span><br />
<br />
Your Chief Pilot is <A class="bld" HREF="mailto:${eqType.CPEmail}">${eqType.CPName}</A>.<br />
<br />
You are also qualified to fly the <fmt:list value="${pilot.ratings}" delim=", " />.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="pilotsearch">Pilot Search</el:cmd></td>
 <td class="data">You can search the <content:airline /> Pilot Roster based on a Pilot's name or
 E-Mail Address.</td>
</tr>

<!-- Flight Report Section -->
<tr class="title caps">
 <td colspan="2">FLIGHT REPORTS</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="logbook" op="nolog" linkID="0x${pilot.ID}">Flight Reports</el:cmd>&nbsp;
 <el:cmd className="bld" url="logbook" op="log" linkID="0x${pilot.ID}">Log Book</el:cmd><br />
 <el:cmd className="pri bld" url="pirep" op="edit">File New Flight Report</el:cmd></td>
 <td class="data">You have flown <fmt:int value="${pilot.legs}" /> flights, for 
 a total of <fmt:dec value="${pilot.hours}" /> hours and 
 <fmt:int value="${pilot.miles}" fmt="#,###,###" /> miles.<br />
 <span class="sec bld"><fmt:int value="${pilot.onlineLegs}" /></span> of these flights and
 <span class="sec bld"><fmt:dec value="${pilot.onlineHours}" /></span> hours were logged 
 online.<br />
<c:if test="${!empty lastFlight}">
 <br />
 Your last flight was on <fmt:date date="${lastFlight.date}" fmt="d" />:<br />
 <span class="pri bld">${lastFlight}</span> - ${lastFlight.airportD.name} (<fmt:airport airport="${lastFlight.airportD}" />)
 to ${lastFlight.airportA.name} (<fmt:airport airport="${lastFlight.airportA}" />) in a ${lastFlight.equipmentType}.
</c:if></td>
</tr>

<!-- Download Section -->
<tr class="title caps">
 <td colspan="2">DOWNLOAD LIBRARIES</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="doclibrary">Document Library</el:cmd></td>
 <td class="data">The <content:airline /> Document Library contains all of Delta Virtual 
 Airlines' official airline procedure manauls, as well as operating manuals for each equipment 
 type. All manuals are stored in cross-platform Adobe Acrobat PDF files.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="fleetlibrary">Fleet Library</el:cmd></td>
 <td class="data">Our Fleet Library contains the official <content:airline /> Fleet - a collection 
 of aircraft, panels manuals and sound schemes.</td>
</tr>
<content:filter roles="Fleet">
<tr>
 <td class="mid"><el:cmd className="bld" url="fleetlibrary" op="admin">Fleet Library Administration</el:cmd></td>
 <td class="data">You can add or update entries in the <content:airline /> Fleet Library.</td>
</tr>
<tr>
 <td class="mid bld"><el:cmd url="fleetlog">Fleet Installer Logs</el:cmd><br />
<el:cmd url="fleetstats">Fleet Installer Statistics</el:cmd></td>
 <td class="data">You can view user System Information and statistics from the <content:airline /> Fleet 
Installer log entries.</td>
</tr>
</content:filter>

<!-- Flight Planning Section -->
<tr class="title caps">
 <td colspan="2">FLIGHT PLANNING RESOURCES</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="routes" op="domestic">Preferred Routes</el:cmd></td>
 <td class="data">We've obtained over 12,900 preferred flight routes from the Federal Aviation 
 Administration, providing mulitple routes from almost 100 airports in the United States and Canada.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="pri bld" url="charts" linkID="${pilot.homeAirport}">Approach Charts</el:cmd></td>
 <td class="data">We have the Q2 2003 version of EchoPlate approach charts for all United States Airports served by 
 Delta Virtual Airlines, Continental and Northwest Airlines.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="routes" op="oceanic">Oceanic Tracks</el:cmd></td>
 <td class="data">Our servers automatically download North Atlantic Track information every day.</td>
</tr>
<tr>
 <td class="mid bld">Navigation Aids</td>
 <td class="data">You can search for a particular Airport, VOR, NDB or Intersection.
<el:text name="navaidCode" size="4" max="5" value="" />&nbsp;<el:button ID="NavSearchButton" type="submit" className="BUTTON" label="SEARCH" /></td>
</tr>
<tr>
 <td class="mid bld">Airport Information</td>
 <td class="data">You can view <select name="navaidType" size="1">
<option value="APT">Airport Information</option>
<option value="MET">METAR Information</option>
</select> for the following Airport: <el:text name="airportCode" size="3" max="4" value="" />
<el:button ID="METARButton" onClick="javascript:void showNAV()" type="button" className="BUTTON" label="GO" /><br />
<span class="small sec">(Please note that these are external sites not affiliated with Delta
 Virtual Airlines. We make no representations as to the content and/or availability of these resources.)</span></td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="findflight">Find A Flight</el:cmd></td>
 <td class="data">If you're looking for a flight at random, you can select an Airport, Departure Time,
  Aircraft Type and Flight Length, and pick a flight at random!</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="assignments" linkID="open">Flight Assignments</el:cmd></td>
 <td class="data">While <content:airline /> doesn't have a formal flight bidding system, we do have 
 'Flight Assigments': routes of 2 to 6 flight legs created by our staff as suggested routes to fly, or you
 can have our automated system randomly assign flights for you to fly.</td>
</tr>

<!-- Testing Section -->
<tr class="title caps">
 <td colspan="2">PILOT TESTING AND PROMOTION</td>
</tr>
<c:if test="${captPromote}">
<tr>
 <td class="pri mid bld">Promotion to Captain</td>
 <td class="data">You are eligible for a promotion to Captain in the <span class="pri bld">${eqType.name}</span>
program. Your name is on the list of Pilots eligible for a promotion, and you can expect to be
promoted within the next 24 to 72 hours.</td>
</tr>
</c:if>
<c:if test="${!empty eqSwitch}">
<tr>
 <td class="mid"><el:cmd className="bld" url="txrequest">Switch Equipment Programs</el:cmd></td>
 <td class="data">You are eligible to transfer to the following equipment types: <fmt:list value="${eqSwitch}" delim=", " /></td>
</tr>
</c:if>
<c:if test="${!empty txreq}">
<tr>
 <td class="mid bld">Switch Equipment Programs</td>
 <td class="data">On <fmt:date fmt="d" date="${txreq.date}" />, you have requested a change of Equipment 
Program to the <b>${txreq.equipmentType}</b> program.</td>
</tr>
</c:if>
<tr>
 <td class="mid"><el:cmd className="bld" url="testcenter">Testing Center</el:cmd></td>
 <td class="data">The <content:airline /> Testing Center is your single source for the written
 examinations needed for promotions and additional type ratings. Here you can see your prior tests
 and their results, in addition to writing new aircraft tests.</td>
</tr>

<content:filter roles="PIREP">
<!-- Flight Report Admin Section -->
<tr class="title caps">
 <td colspan="2">FLIGHT REPORTS</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="pirepqueue">Submitted Flight Reports</el:cmd></td>
 <td class="data">You can Approve, Reject or Hold submited pilot Flight Reports here.</td>
</tr>
</content:filter>

<content:filter roles="HR,PIREP,Examination">
<!-- Human Resources Admin Section -->
<tr class="title caps">
 <td colspan="2">HUMAN RESOURCES</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="txrequests">Equipment Transfer Requests</el:cmd></td>
 <td class="data">Pilots wishing to switch Equipment Programs can submit transfer requests once 
they have met the necessary requirements for a new Equipment Program. You can view these transfer 
requests here, assign Check Rides, and complete the Promotion Process.</td>
</tr>
<tr>
 <td class="mid bld"><el:cmd url="promoqueue">Promotion Queue</el:cmd></td>
 <td class="data">The Promotion Queue lists pilots who have successfully met all the requirements
for promotion to the rank of Captain in their Equipment Program.</td>
</tr>
</content:filter>
<content:filter roles="HR,PIREP">
<tr>
 <td class="mid"><el:cmd className="bld" url="massmail">Group E-Mail</el:cmd></td>
 <td class="data">You can send an e-mail message to a group of pilots in a single equipment program,
 or to the entire airline.</td>
</tr>
</content:filter>
<content:filter roles="HR">
<tr>
 <td class="mid"><el:cmd className="bld" url="eqtypes">Equipment Type Programs</el:cmd></td>
 <td class="data">You can add new equipment programs, or modify existing programs to
 change Chief Pilots, automatic additional ratings or equipment types for flight legs
 that qualify for promotion to Captain.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="msgtemplates">E-Mail Message Templates</el:cmd></td>
 <td class="data">The <content:airline /> web site sends out e-mail notification messages to inform members of
new Online Events, System News entries and NOTAMs, and Flight Report approval or rejection.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="applicants">Pilot Registration</el:cmd></td>
 <td class="data">This section tracks applicants to Delta Virtual Airlines before they
 are hired on as Pilots.</td>
</tr>
</content:filter> 
<content:filter roles="HR,Examination">
<tr>
 <td class="mid"><el:cmd className="bld" url="eprofiles">Examination Profiles</el:cmd></td>
 <td class="data">You can add new written examinations or modify the examinations to
 change the number of questions, passing score, or additional ratings granted upon
 successful completion of an examination.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="qprofiles" linkID="ALL">Examination Question Profiles</el:cmd></td>
 <td class="data">You can add new examination questionss or modify existing examination 
 questions.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="examqueue">Submitted Examinations</el:cmd></td>
 <td class="data">You can view and score submitted Pilot Examinations.</td>
</tr>
</content:filter>

<content:filter roles="Schedule">
<!-- Schedule Admin Section -->
<tr class="title caps">
 <td colspan="2">FLIGHT SCHEDULE</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="airlines">Update Airlines</el:cmd></td>
 <td class="data">You can modify the Airline profiles contained within the <content:airline /> Flight Schedule.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="airports">Update Airports</el:cmd></td>
 <td class="data">You can modify the Airport profiles contained within the <content:airline /> Flight Schedule.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="navimport">Navigation Data</el:cmd></td>
 <td class="data">You can import and purge AIRAC data stored within the <content:airline /> Navigation Data
database.</td>
</tr>
</content:filter>

<content:filter roles="Admin">
<!-- System Admin Section -->
<tr class="title caps">
 <td colspan="2">SYSTEM ADMINISTRATION</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="diag">Diagnostics</el:cmd></td>
 <td class="data">You can view information about the application server and Java Virtual Machine.</td>
</tr>
<tr>
 <td class="mid"><el:cmd className="bld" url="systemlog">System Log</el:cmd></td>
 <td class="data">You can view and search for entries in the Application, Scheduled Task and ACARS Log 
databases.</td>
</tr>
</content:filter>
<tr class="title"><td colspan="2">&nbsp;</td></tr>
</el:table>
</el:form>
<content:copyright />
</div>
</body>
</html>
