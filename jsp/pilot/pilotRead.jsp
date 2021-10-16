<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:pilotName pilot="${pilot}">${pilot.name}</content:pilotName><c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></title>
<content:expire expires="15" />
<content:canonical convertID="true" />
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<c:if test="${!empty loginAddrs}">
<script>
golgotha.local.toggleLoginAddrs = function(lnk)
{
var addrDiv = document.getElementById('loginAddrs');
if (!addrDiv) return false;

// Toggle the visibility
var isHidden = (addrDiv.style.display == 'none');
golgotha.util.display(addrDiv, isHidden);

// Update the link
lnk.innerHTML = (!isHidden) ? 'SHOW' : 'HIDE';
return true;
};
</script></c:if>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="cspan" value="${(!empty exams) || (!empty statusUpdates) || (!empty courses) ? 6 : 1}" scope="request" />
<content:sysdata var="forumName" name="airline.forum" />
<content:sysdata var="dbName" name="airline.db" />
<content:sysdata var="acarsEnabled" name="acars.enabled" />
<content:sysdata var="currencyEnabled" name="testing.currency.enabled" />
<content:sysdata var="currencyInterval" name="testing.currency.validity" />
<content:filter roles="HR,Operations">
<c:set var="canToggleCurrency" value="${currencyEnabled}" scope="page" />
</content:filter>
<content:filter roles="HR,Signature">
<c:set var="canSigAuth" value="${pilot.hasSignature && !sigAuthorized}" scope="page" />
</content:filter>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<!-- Pilot Title Bar -->
<tr class="title caps">
 <td colspan="${cspan + 1}"><content:pilotName pilot="${pilot}">${pilot.rank.name}&nbsp;${pilot.name}</content:pilotName><c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if>
<c:if test="${access.canEdit}"> - <el:cmd url="profile" op="edit" link="${pilot}">EDIT PROFILE</el:cmd></c:if>
<c:if test="${pilot.isForgotten}">This Pilot's data has been hidden per European Union GPDR regulations</c:if></td>
</tr>

<!-- Pilot Data -->
<tr>
 <td class="label">Pilot Status</td>
 <td colspan="${cspan}" class="data sec bld">${pilot.status.description}
<c:if test="${access.canChangeStatus}">
<c:if test="${pilot.noVoice}"> <span class="warn bld">VOICE ACCESS DISABLED</span></c:if>
<c:if test="${pilot.noExams}"> <span class="warn bld">EXAMINATION ACCESS DISABLED</span></c:if>
</c:if></td>
</tr>
<c:if test="${acarsEnabled}">
<c:if test="${access.canChangeStatus}">
<tr>
 <td class="label">ACARS Capabilities</td>
 <td colspan="${cspan}" class="data">${pilot.ACARSRestriction.name}
<c:if test="${pilot.noTimeCompression}"> <span class="warn bld">NO ACARS TIME COMPRESSION</span></c:if></td>
</tr>
</c:if>
<c:if test="${empty pilot.pilotCode}">
<tr>
 <td class="label">ACARS User ID</td>
 <td colspan="${cspan}" class="data"><span class="ita">No Pilot ID assigned.</span> Use <span class="bld">${pilot.ID}</span> as a temporary ACARS User ID.</td>
</tr>
</c:if>
<tr>
 <td class="label">ACARS Update Channel</td>
 <td colspan="${cspan}" class="data ter bld">${pilot.ACARSUpdateChannel.description}</td>
</tr>
</c:if>
<c:if test="${access.canChangeRoles}">
<c:if test="${!empty pilot.roles}">
<tr>
 <td class="label">Security Roles</td>
 <td colspan="${cspan}" class="data"><fmt:list value="${pilot.roles}" delim=", " /></td>
</tr>
</c:if>
<c:if test="${pilot.isPermanent}">
<tr>
 <td class="label">&nbsp;</td>
 <td colspan="${cspan}" class="data"><span class="sec bld caps" >Permanent Account</span></td>
</tr>
</c:if>
</c:if>
<tr>
 <td class="label">Equipment Type</td>
 <td colspan="${cspan}" class="data pri bld">${pilot.equipmentType}</td>
</tr>
<tr>
 <td class="label top">Additional Ratings</td>
 <td colspan="${cspan}" class="data small"><fmt:list value="${pilot.ratings}" delim=", " /></td>
</tr>
<c:choose>
<c:when test="${currencyEnabled && pilot.proficiencyCheckRides}">
<tr>
 <td class="label">Currency Testing</td>
 <td colspan="${cspan}" class="data"><span class="ter bld">ENABLED</span>, check ride validity <fmt:int value="${currencyInterval}" /> days
<c:if test="${canToggleCurrency}"> <el:cmd url="currencydisable" link="${pilot}" className="sec bld">DISABLE<span class="nophone"> CURRENCY CHECK RIDES</span></el:cmd></c:if></td>
</tr>
</c:when>
<c:when test="${currencyEnabled && canToggleCurrency}">
<tr>
 <td class="label">Currency Testing</td>
 <td colspan="${cspan}" class="data"><span class="ter bld">DISABLED</span> - <el:cmd url="currencyenable" link="${pilot}" className="sec bld">ENABLE<span class="nophone"> CURRENCY CHECK RIDES</span></el:cmd></td> 
</tr>
</c:when>
</c:choose>
<c:if test="${!empty certs}">
<tr>
 <td class="label">Flight Academy Certifications</td>
 <td colspan="${cspan}" class="data"><fmt:list value="${certs}" delim=", " /></td>
</tr>
</c:if>
<tr>
 <td class="label">Home Airport</td>
 <td colspan="${cspan}" class="data">${airport.name} (<fmt:airport airport="${airport}" />)</td>
</tr>
<c:set var="vatsimID" value="${fn:networkID(pilot, 'VATSIM')}" scope="page" />
<c:if test="${!empty vatsimID}">
<tr>
 <td class="label">VATSIM ID</td>
 <td colspan="${cspan}" class="data">${vatsimID}</td>
</tr>
<c:if test="${!empty vatsim_ratings}">
<tr>
 <td class="label top">VATSIM Ratings</td>
 <td colspan="${cspan}" class="data"><c:forEach var="pr" items="${vatsim_ratings}" varStatus="prStatus"><span class="pri bld">${pr.ratingCode}</span>, issued by ${pr.ATOName} on <fmt:date fmt="d" date="${pr.issueDate}" />
<c:if test="${!prStatus.last}"><br /></c:if></c:forEach></td>
</tr>
</c:if>
</c:if>
<c:set var="ivaoID" value="${fn:networkID(pilot, 'IVAO')}" scope="page" />
<c:if test="${!empty ivaoID}">
<tr>
 <td class="label">IVAO ID</td>
 <td colspan="${cspan}" class="data">${ivaoID}</td>
</tr>
</c:if>
<c:set var="peID" value="${fn:networkID(pilot, 'PilotEdge')}" scope="page" />
<c:if test="${!empty peID}">
<tr>
 <td class="label">PilotEdge ID</td>
 <td colspan="${cspan}" class="data">${peID}</td>
</tr>
</c:if>
<tr>
 <td class="label">Location</td>
 <td colspan="${cspan}" class="data sec">${pilot.location}</td>
</tr>
<c:if test="${!empty pilot.motto}">
<tr>
 <td class="label">Personal Motto</td>
 <td colspan="${cspan}" class="data ita">${pilot.motto}</td>
</tr>
</c:if>

<c:if test="${access.canViewEmail}">
<!-- E-Mail Information -->
<tr class="title">
 <td colspan="${cspan + 1}">E-MAIL / INSTANT MESSAGING INFORMATION</td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
<c:if test="${fn:isEMailValid(pilot)}">
 <td colspan="${cspan}" class="data"><a href="mailto:${pilot.email}">${pilot.email}</a></td>
</c:if>
<c:if test="${!fn:isEMailValid(pilot)}">
 <td colspan="${cspan}" class="data error bld">E-MAIL ADDRESS INVALIDATED</td>
</c:if>
</tr>
</c:if>
<c:if test="${access.canViewPush && (!empty pilot.pushEndpoints)}">
<tr>
 <td class="label top">Push Notifications</td>
 <td colspan="${cspan}" class="data"><c:forEach var="ep" items="${pilot.pushEndpoints}" varStatus="hasMoreEP">${ep.host} - since <fmt:date className="sec" fmt="d" date="${ep.createdOn}" /><c:if test="${!hasMoreEP.last}"><br /></c:if></c:forEach></td>
</tr>
</c:if>
<c:if test="${!empty acarsClientInfo}">
<!-- ACARS client system data -->
<tr class="title">
 <td colspan="${cspan + 1}">ACARS CLIENT SYSTEM INFORMATION - <span class="und" onclick="void golgotha.util.toggleExpand(this, 'acarsClient')">EXPAND</span></td>
</tr>
<c:if test="${!empty acarsClientInfo.bridgeInfo}">
<tr class="acarsClient" style="display:none;">
 <td class="label">Simulator / Bridge</td>
 <td colspan="${cspan + 1}" class="data"><span class="sec bld">${acarsClientInfo.simulator.name}</span><span class="small nophone"> (Using ${acarsClientInfo.bridgeInfo} simulator Bridge)</span></td>
</tr>
</c:if>
<tr class="acarsClient" style="display:none;">
 <td class="label">Operating System</td>
 <td colspan="${cspan + 1}" class="data"><span class="pri bld"><fmt:windows version="${acarsClientInfo.OSVersion}" /></span> (<fmt:int value="${acarsClientInfo.memorySize}" />KB memory) <span class="ita">as of <fmt:date fmt="d" date="${acarsClientInfo.date}" /></span></td>
</tr>
<tr class="acarsClient" style="display:none;">
 <td class="label">.NET Runtime</td>
 <td colspan="${cspan + 1}" class="data"><span class="bld">${acarsClientInfo.dotNETVersion}</span> <span class="small">CLR: ${acarsClientInfo.CLRVersion}</span></td>
</tr>
<tr class="acarsClient" style="display:none;">
 <td class="label">Locale / Time Zone</td>
 <td colspan="${cspan + 1}" class="data">${acarsClientInfo.locale}&nbsp;<span class="ita">Time Zone: ${acarsClientInfo.timeZone}</span></td>
</tr>
<tr class="acarsClient" style="display:none;">
 <td class="label">CPU Information</td>
 <td colspan="${cspan + 1}" class="data">${acarsClientInfo.CPU}&nbsp;<span class="sec small ita">(<fmt:int value="${acarsClientInfo.cores}" /> cores, <fmt:int value="${acarsClientInfo.threads}" /> threads)</span></td>
</tr>
<tr class="acarsClient" style="display:none;">
 <td class="label">GPU Information</td>
 <td colspan="${cspan + 1}" class="data">${acarsClientInfo.GPU}&nbsp;<span class="small ita">(<fmt:int value="${acarsClientInfo.videoMemorySize}" /> KB, ${acarsClientInfo.width}x${acarsClientInfo.height}x${acarsClientInfo.colorDepth}, ${acarsClientInfo.screenCount} screens)</span></td>
</tr>
<tr class="acarsClient" style="display:none;">
 <td class="label">GPU Driver</td>
 <td colspan="${cspan + 1}" class="data">${acarsClientInfo.GPUDriverVersion}</td>
</tr>
</c:if>
<!-- Pilot Statistics -->
<tr class="title">
 <td colspan="${cspan + 1}">PILOT STATISTICS</td>
</tr>
<tr>
 <td class="label">Joined on</td>
 <td colspan="${cspan}" class="data"><fmt:date date="${pilot.createdOn}" fmt="d" d="EEEE MMMM dd, yyyy" /></td>
</tr>
<c:if test="${!empty pilot.lastLogin}">
<tr>
 <td class="label top">Logins</td>
 <td colspan="${cspan}" class="data"><fmt:int value="${pilot.loginCount}" />, last on <fmt:date date="${pilot.lastLogin}" />
<content:filter roles="HR"> from <el:cmd url="loginaddrs" linkID="${pilot.loginHost}" op="net">${pilot.loginHost}</el:cmd>.
<c:if test="${!empty loginAddrs}"><a href="javascript:void golgotha.local.toggleLoginAddrs(this)">SHOW</a></c:if></content:filter>
</td>
</tr>
<content:filter roles="HR">
<c:if test="${!empty loginAddrs}">
<tr id="loginAddrs" style="display:none;">
 <td class="label">&nbsp;</td>
 <td colspan="${cspan}" class="data"><c:forEach var="loginAddr" items="${loginAddrs}">
<content:defaultMethod var="isResolved" object="${loginAddr}" method="isResolved" />
${loginAddr.remoteAddr}<c:if test="${isResolved}"> (${loginAddr.remoteHost})</c:if> - <fmt:int value="${loginAddr.loginCount}" /> logins<br /></c:forEach></td>
</tr>
</c:if>
</content:filter>
</c:if>
<c:if test="${!empty pilot.lastLogoff}">
<tr>
 <td class="label">Last Visited on</td>
 <td colspan="${cspan}" class="data"><fmt:date date="${pilot.lastLogoff}" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Total Flights</td>
 <td colspan="${cspan}" class="data"><fmt:int value="${pilot.legs}" /> legs, <fmt:dec value="${pilot.hours}" /> hours</td>
</tr>
<c:if test="${pilot.onlineLegs > 0}">
<tr>
 <td class="label">Online Flights</td>
 <td colspan="${cspan}" class="data pri"><fmt:int value="${pilot.onlineLegs}" /> legs, <fmt:dec value="${pilot.onlineHours}" /> hours</td>
</tr>
</c:if>
<c:if test="${acarsEnabled && (pilot.ACARSLegs > 0)}">
<tr>
 <td class="label">ACARS Flights</td>
 <td colspan="${cspan}" class="data sec"><fmt:int value="${pilot.ACARSLegs}" /> legs, <fmt:dec value="${pilot.ACARSHours}" /> hours
<content:filter roles="PIREP,HR">
 <el:cmd url="mystats" link="${pilot}" className="sec bld">Flight Statistics</el:cmd> 
 | <el:cmd url="myroutemap" link="${pilot}" className="bld">Route Map</el:cmd></content:filter></td>
</tr>
</c:if>
<c:if test="${(pilot.dispatchFlights > 0) || (pilot.dispatchHours > 0.1)}">
<tr>
 <td class="label">Dispatch Service</td>
 <td colspan="${cspan}" class="data"><fmt:int value="${pilot.dispatchFlights}" /> legs dispatched, <fmt:dec value="${pilot.dispatchHours}" /> hours</td>
</tr>
</c:if>
<c:if test="${pilot.legacyHours > 0}">
<tr>
 <td class="label">Legacy Hours</td>
 <td colspan="${cspan}" class="data">${pilot.legacyHours} hours</td>
</tr>
</c:if>
<content:filter roles="HR,Moderator"><c:if test="${wcPosts > 0}">
<tr>
 <td class="label">${forumName} Posts</td>
 <td colspan="${cspan}" class="data"><fmt:int value="${wcPosts}" /> total ${forumName} posts</td>
</tr>
</c:if></content:filter>
<c:if test="${access.canChangeSignature && pilot.hasSignature}">
<tr>
 <td class="label top">${forumName} Signature</td>
 <td colspan="${cspan}" class="data"><el:sig user="${pilot}" caption="${pilot.name}" /></td>
</tr>
</c:if>
<c:if test="${!empty applicant}">
<tr>
 <td class="label">Applicant Profile</td>
 <td colspan="${cspan}" class="data"><el:cmd url="applicant" link="${applicant}">Click Here</el:cmd> to view the 
Applicant profile for ${pilot.name}.</td>
</tr>
</c:if>
<c:if test="${(!empty accs) || access.canChangeStatus}">
<tr>
 <td class="label top">Pilot Accomplishments</td>
 <td colspan="${cspan}" class="data"><c:if test="${!empty accs}">
<c:forEach var="a" items="${accs}">
<fmt:accomplish accomplish="${a}" className="bld" />, (<fmt:int value="${a.value}" />&nbsp;${a.unit.name}) on <span class="bld"><fmt:date date="${a.date}" fmt="d" /></span><br />
</c:forEach></c:if>
<c:if test="${access.canChangeStatus}"><c:if test="${!empty accs}"><br /></c:if><el:cmd url="accalc" className="sec bld" link="${pilot}">Recalculate Accomplishments</el:cmd>
<content:filter roles="HR"> | <el:cmd url="acceligibility" className="bld" link="${pilot}">Accomplishment Eligibility</el:cmd></content:filter>
</c:if></td>
</tr>
</c:if>
<content:authUser>
<c:set var="showExamToggle" value="true" scope="page" />
<c:set var="examCollapse" value="${exams.size() >= 10}" scope="page" />
</content:authUser>
<%@ include file="/jsp/pilot/pilotExams.jspf" %>
<content:authUser>
<c:set var="showCourseToggle" value="true" scope="page" />
<c:set var="courseCollapse" value="${courses.size() >= 10}" scope="page" />
</content:authUser>
<%@ include file="/jsp/pilot/pilotCourses.jspf" %>
<content:authUser>
<c:set var="showStatusToggle" value="true" scope="page" />
<c:set var="statusCollapse" value="${statusUpdates.size() >= 15}" scope="page" />
</content:authUser>
<%@ include file="/jsp/pilot/pilotStatusUpdate.jspf" %>
<content:authUser>
<c:set var="showDeliveryToggle" value="true" scope="page" />
<c:set var="deliveryCollapse" value="${emailDelivery.size() >= 15}" scope="page" />
</content:authUser>
<%@ include file="/jsp/pilot/pilotEMailDelivery.jspf" %>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>
<c:if test="${!crossDB}"><el:cmdbutton url="logbook" op="log" link="${pilot}" key="L" label="LOG BOOK" /></c:if>
<c:if test="${access.canEdit}">&nbsp;<el:cmdbutton url="profile" link="${pilot}" op="edit" key="E" label="EDIT PROFILE" /></c:if>
<c:if test="${access.canTransfer}">&nbsp;<el:cmdbutton url="txairline" link="${pilot}" label="INTER-AIRLINE TRANSFER" /></c:if>
<c:if test="${access.canAssignRide}">&nbsp;<el:cmdbutton url="nakedassign" link="${pilot}" label="ASSIGN CHECK RIDE" /></c:if>
<c:if test="${canSigAuth}">&nbsp;<el:cmdbutton url="sigauth" link="${pilot}" label="APPROVE SIGNATURE" /></c:if>
<c:if test="${!crossDB}">
<c:if test="${!pilot.isInvalid()}"><content:filter roles="HR,PIREP,Examination">
&nbsp;<el:cmdbutton url="invalidate" link="${pilot}" label="INVALIDATE E-MAIL" /></content:filter></c:if>
<content:filter roles="HR">&nbsp;<el:cmdbutton url="statuscomment" link="${pilot}" key="C" label="COMMENT" /></content:filter>
<c:if test="${access.canInactivate}">&nbsp;<el:cmdbutton url="suspend" link="${pilot}" label="SUSPEND" />&nbsp;<el:cmdbutton url="retire" link="${pilot}" label="RETIRE" /></c:if>
<c:if test="${access.canActivate}">&nbsp;<el:cmdbutton url="activate" link="${pilot}" label="ACTIVATE" /></c:if>
<content:filter roles="Admin">
&nbsp;<c:if test="${!pilot.isForgotten}"><el:cmdbutton url="forget" link="${pilot}" label="FORGET"></el:cmdbutton>&nbsp;</c:if><el:cmdbutton url="su" link="${pilot}" label="SWITCH TO USER" /></content:filter>
</c:if>
</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
