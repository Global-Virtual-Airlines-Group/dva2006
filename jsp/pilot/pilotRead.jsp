<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="cspan" value="${(!empty exams) || (!empty statusUpdates) ? 6 : 1}" scope="request" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form" pad="default" space="default">
<!-- Pilot Title Bar -->
<tr class="title caps">
 <td colspan="${cspan + 1}">${pilot.rank} ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></td>
</tr>

<!-- Pilot Data -->
<tr>
 <td class="label">Pilot Status</td>
 <td colspan="${cspan}" class="data sec bld">${pilot.statusName}
<c:if test="${access.canChangeStatus}">
<c:if test="${pilot.noVoice}"> <span class="warn bld">VOICE ACCESS DISABLED</span></c:if>
<c:if test="${pilot.noExams}"> <span class="warn bld">EXAMINATION ACCESS DISABLED</span></c:if>
</c:if></td>
</tr>
<c:if test="${access.canChangeStatus}">
<tr>
 <td class="label">ACARS Capabilities</td>
 <td colspan="${cspan}" class="data">${pilot.ACARSRestrictionName}</td>
</tr>
</c:if>
<c:if test="${access.canChangeRoles && (!empty pilot.roles)}">
<tr>
 <td class="label">Security Roles</td>
 <td colspan="${cspan}" class="data"><fmt:list value="${pilot.roles}" delim=", " /></td>
</tr>
</c:if>
<tr>
 <td class="label">Equipment Type</td>
 <td colspan="${cspan}" class="data pri bld">${pilot.equipmentType}</td>
</tr>
<tr>
 <td class="label" valign="top">Additional Ratings</td>
 <td colspan="${cspan}" class="data small"><fmt:list value="${pilot.ratings}" delim=", " /></td>
</tr>
<c:if test="${!empty courses}">
<tr>
 <td class="label">Flight Academy Certifications</td>
 <td colspan="${cspan}" class="data"><fmt:list value="${courses}" delim=", " /></td>
</tr>
</c:if>
<tr>
 <td class="label">Home Airport</td>
 <td colspan="${cspan}" class="data">${airport.name} (<fmt:airport airport="${airport}" />)</td>
</tr>
<c:if test="${!empty pilot.networkIDs['VATSIM']}">
<tr>
 <td class="label">VATSIM ID#</td>
 <td colspan="${cspan}" class="data">${pilot.networkIDs['VATSIM']}</td>
</tr>
</c:if>
<c:if test="${!empty pilot.networkIDs['IVAO']}">
<tr>
 <td class="label">IVAO ID#</td>
 <td colspan="${cspan}" class="data">${pilot.networkIDs['IVAO']}</td>
</tr>
</c:if>
<tr>
 <td class="label">Location</td>
 <td colspan="${cspan}" class="data sec">${pilot.location}</td>
</tr>
<c:if test="${!empty pilot.motto}">
<tr>
 <td class="label">Personal Motto</td>
 <td colspan="${cspan}" class="data"><i>${pilot.motto}</i></td>
</tr>
</c:if>

<c:if test="${access.canViewEmail}">
<!-- E-Mail Information -->
<tr class="title">
 <td colspan="${cspan + 1}">E-MAIL / INSTANT MESSAGING INFORMATION</td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
<c:if test="${fn:isEMailValid(pilot.email)}">
 <td colspan="${cspan}" class="data"><a href="mailto:${pilot.email}">${pilot.email}</a></td>
</c:if>
<c:if test="${!fn:isEMailValid(pilot.email)}">
 <td colspan="${cspan}" class="data error bld">E-MAIL ADDRESS INVALIDATED</td>
</c:if>
</tr>
<c:if test="${!empty pilot.IMHandle['AOL']}">
<tr>
 <td class="label">AOL Instant Messenger</td>
 <td colspan="${cspan}" class="data">${pilot.IMHandle['AOL']}</td>
</tr>
</c:if>
<c:if test="${!empty pilot.IMHandle['MSN']}">
<tr>
 <td class="label">MSN Messenger</td>
 <td colspan="${cspan}" class="data">${pilot.IMHandle['MSN']}</td>
</tr>
</c:if>
</c:if>

<c:if test="${!pilot.noVoice && !empty ts2Clients}"><content:filter roles="HR,Instructor">
<!-- TeamSpeak 2 Virtual Server access -->
<tr class="title">
 <td colspan="${cspan + 1}">TEAMSPEAK 2 ACCESS</td>
</tr>
<tr>
 <td class="label" valign="top">Virtual Servers</td>
 <td colspan="${cspan}" class="data">${pilot.name} has access to the following TeamSpeak 2 Virtual Servers:<br />
<br />
<c:forEach var="ts2Client" items="${ts2Clients}">
<c:if test="${ts2Client.serverID != 0}">
<c:set var="ts2Server" value="${ts2Servers[ts2Client.serverID]}" scope="request" />
<span class="sec bld">${ts2Server.name}</span> (Port ${ts2Server.port}) - ${ts2Server.description}
<c:if test="${ts2Client.autoVoice}"> <span class="ter small bld">AUTO-VOICE</span></c:if>
<c:if test="${ts2Client.serverOperator}"> <span class="sec small bld">SERVER OPERATOR</span></c:if>
<c:if test="${ts2Client.serverAdmin}"> <span class="pri small bld">SERVER ADMINISTRATOR</span></c:if><br />
</c:if></c:forEach></td>
</tr>
</content:filter></c:if>

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
 <td class="label">Logins</td>
 <td colspan="${cspan}" class="data"><fmt:int value="${pilot.loginCount}" />, last on <fmt:date date="${pilot.lastLogin}" />
<content:filter roles="HR,Moderator"> from ${pilot.loginHost}</content:filter>.</td>
</tr>
</c:if>
<c:if test="${!empty pilot.lastLogoff}">
<tr>
 <td class="label">Last Visited on</td>
 <td colspan="${cspan}" class="data"><fmt:date date="${pilot.lastLogoff}" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Flights</td>
 <td colspan="${cspan}" class="data"><fmt:int value="${pilot.legs}" /> legs, <fmt:dec value="${pilot.hours}" /> hours</td>
</tr>
<tr>
 <td class="label">Online Flights</td>
 <td colspan="${cspan}" class="data pri"><fmt:int value="${pilot.onlineLegs}" /> legs, <fmt:dec value="${pilot.onlineHours}" /> hours</td>
</tr>
<c:if test="${pilot.legacyHours > 0}">
<tr>
 <td class="label">Legacy Hours</td>
 <td colspan="${cspan}" class="data">${pilot.legacyHours} hours</td>
</tr>
</c:if>
<c:if test="${!empty applicant}">
<tr>
 <td class="label">Applicant Profile</td>
 <td colspan="${cspan}" class="data"><el:cmd url="applicant" linkID="0x${applicant.ID}">Click here</el:cmd> to view the 
Applicant profile for ${pilot.name}.</td>
</tr>
</c:if>
<%@ include file="/jsp/pilot/pilotExams.jspf" %>
<%@ include file="/jsp/pilot/pilotStatusUpdate.jspf" %>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td>
<el:cmdbutton url="logbook" op="log" linkID="0x${pilot.ID}" key="L" label="LOG BOOK" />
<c:if test="${access.canEdit}">
 <el:cmdbutton url="profile" linkID="0x${pilot.ID}" op="edit" key="E" label="EDIT PROFILE" />
</c:if>
<c:if test="${access.canTransfer}">
 <el:cmdbutton url="txairline" linkID="0x${pilot.ID}" label="INTER-AIRLINE TRANSFER" />
</c:if>
<c:if test="${access.canAssignRide}">
 <el:cmdbutton url="nakedassign" linkID="0x${pilot.ID}" label="ASSIGN CHECK RIDE" />
</c:if>
<content:filter roles="HR,PIREP,Examination">
 <el:cmdbutton url="invalidate" linkID="0x${pilot.ID}" label="INVALIDATE E-MAIL" />
</content:filter>
<content:filter roles="HR">
 <el:cmdbutton url="statuscomment" linkID="0x${pilot.ID}" label="COMMENT" />
</content:filter>
<c:if test="${access.canSuspend}">
 <el:cmdbutton url="suspend" linkID="0x${pilot.ID}" label="SUSPEND" />
</c:if>
<content:filter roles="Admin">
 <el:cmdbutton url="su" linkID="0x${pilot.ID}" label="SWITCH TO USER" />
</content:filter>
</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
