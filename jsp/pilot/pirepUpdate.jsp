<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Flight Report Updated</title>
<content:css name="main" />
<content:googleAnalytics />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="academyEnabled" name="academy.enabled" />
<content:sysdata var="eliteName" name="econ.elite.name" />

<!-- Main Body Frame -->
<content:region id="main">
<c:if test="${isSaved}">
<div class="updateHdr">Flight Report Saved</div>
<br />
This <content:airline /> Flight Report has been successfully and saved in the database.<br />
<c:if test="${isCreated}">
<br />
<span class="err">Please note that you must <span class="bld und">SUBMIT</span> a Flight Report if you want the flight hours and flight leg to be credited towards your flight totals here at <content:airline />. 
To Submit this Flight Report for approval,</span> <el:cmd className="sec bld" url="submit" link="${pirep}">Click Here</el:cmd>.<br />
</c:if>
</c:if>
<c:if test="${isSubmitted}">
<div class="updateHdr">Flight Report Submitted</div>
<br />
This <content:airline /> Flight Report has been submitted for review and approval. Once this Report has been approved, your flight legs and hours will be automatically updated and you will be notified via e-mail.<br />
<c:if test="${fn:isPromoLeg(pirep)}">
<br />
This Flight Leg counts as one of the <fmt:int className="sec bld" value="${promoteLegs}" /> Flight Legs required for promotion to Captain in the <span class="pri bld">${eqType.name}</span> program.<br />
</c:if>
<c:if test="${!fn:isRated(pirep)}">
<br />
<span class="warn bld">You do not appear to have a type rating in the ${pirep.equipmentType}. This may be the result of our database being out of date or otherwise inaccurate.</span> This may cause a delay in your
Flight Report being approved. Please be aware that we cannot credit flights flown using unrated aircraft.<br />
</c:if>
<c:if test="${fn:routeWarn(pirep)}">
<br />
<span class="warn bld">Your flight between ${pirep.airportD.name} (<fmt:airport airport="${pirep.airportD}" />) and ${pirep.airportA.name} (<fmt:airport airport="${pirep.airportA}" />) does not currently appear in  
the <content:airline /> flight schedule.</span> This may cause a delay in your Flight Report being approved.<br />
</c:if>
<c:if test="${fn:timeWarn(pirep)}">
<br />
<span class="warn bld">Your have logged <fmt:dec value="${pirep.length / 10.0}" /> flight hours for your flight between ${pirep.airportD.name} (<fmt:airport airport="${pirep.airportD}" />) and ${pirep.airportA.name } 
(<fmt:airport airport="${pirep.airportA}" />). The <content:airline /> Flight Schedule lists the average duration of flights between these two airports (including delays and turnaround time) as <fmt:duration duration="${avgTime }" />.</span> 
 This may cause a delay in your Flight Report being approved.<br />
</c:if>
<c:if test="${fn:rangeWarn(pirep)}">
<br />
<span class="warn bld">The flight leg between <fmt:airport airport="${pirep.airportD}" /> and <fmt:airport airport="${pirep.airportA}" /> is <fmt:distance value="${pirep.distance}" longUnits="true" />, and appears to 
exceed the maximum range of the ${pirep.equipmentType}.</span> This may cause a delay in your Flight Report being approved.<br />
</c:if>
</c:if>
<c:if test="${calcLoadFactor}">
<c:set var="loadUpdated" value="${oldLoadFactor > 0}" scope="page" />
<div class="updateHdr">Pre-Flight Load Factor ${loadUpdate ? 'Updated' : 'Calculated'}</div>
<br />
A pre-flight load factor of <span class="pri bld"><fmt:dec value="${pirep.loadFactor}" fmt="##0.00%" /></span> has been calculated for this flight. <c:if test="${loadUpdated}">This updates the previously calculated 
load factor of <span class="sec bld"><fmt:dec value="${oldLoadFactor}" fmt="##0.00%" /></span>.</c:if> If the flight is flown in the ${pirep.equipmentType}, <fmt:int value="${pirep.passengers}" /> passengers will 
be boarded.<br />
<br />
For dispatch purposes, <content:airline /> assumes a per-passenger weight of <fmt:weight value="${175}" /> and per-passenger baggage weight of <fmt:weight value="${30}" />.<br />
<br />
<content:filter roles="PIREP.Operations">The target load factor for <fmt:date date="${pirep.date}" fmt="d" className="sec bld" /> is <span class="bld"><fmt:dec value="${targetLoad}" fmt="##0.00%" /></span>.<br />
<br /></content:filter>
</c:if>
<c:if test="${isApprove}">
<div class="updateHdr">Flight Report Approved</div>
<br />
This Flight Report has been approved, and an e-mail message has been sent to ${pilot.name}.<br />
<c:if test="${assignID}">
This Pilot has been assigned a Pilot ID and seniority number at <content:airline />. The new Pilot ID for ${pilot.name} is <b>${pilot.pilotCode}</b>.<br />
</c:if>
<c:if test="${!empty accomplishments}">
<br />
<c:forEach var="acc" items="${accomplishments}">
<span class="sec bld">With the approval of this Flight Report, ${pilot.name} has joined the <fmt:accomplish accomplish="${acc}" />.</span><br />
</c:forEach>
<br />
</c:if>
<c:if test="${assignComplete}">
With the approval of this Flight Report, a Flight Assignment has been successfully completed.<br />
<br /></c:if>
<c:if test="${onlineArchive}">
This flight was flown using the <span class="sec bld">${pirep.network}</span> online network, and track data has been archived for later retrieval.<br />
</c:if>
<c:if test="${acarsArchive}">
This flight was logged using <content:airline /> ACARS software, and all position data has been archived for later retrieval.<br />
<br /></c:if>
<c:if test="${checkRideScored && (!empty checkRide)}">
<br />
This Flight Report is for a <b>${checkRide.name}</b>, and this Check Ride has been graded as part of the approval process for this Flight Report. This Check Ride has been <span class="sec bld"> ${checkRide.passFail ? 'APPROVED' : 'REJECTED' }</span>.<br />
</c:if>
</c:if>
<c:if test="${isHold}">
<div class="updateHdr">Flight Report Held</div>
<br />
This Flight Report has been marked as &quot;On Hold&quot; and an e-mail message has been sent to ${pilot.name}. Please work to resolve any issues with this Flight Report within the next few days.<br />
</c:if>
<c:if test="${isReject}">
<div class="updateHdr">Flight Report Rejected</div>
<br />
This Flight Report has been rejected and an e-mail message has been sent to ${pilot.name}.<br />
</c:if>
<c:if test="${isWithdraw}">
<div class="updateHdr">Flight Report Withdrawn</div>
<br />
This submitted Flight Report has been withdrawn and is now available for you to modify.<br />
</c:if>
<c:if test="${isDeleted}">
<div class="updateHdr">Flight Report Deleted</div>
<br />
This Flight Report has been succesfully deleted from the database.<br />
<br />
</c:if>
<c:if test="${isACARSDeleted}">
<div class="updateHdr">Flight Report ACARS Data Deleted</div>
<br />
<content:airline /> ACARS data for this Flight Report has been succesfully deleted from the database.<br />
<br />
</c:if>
<c:if test="${isEliteScore}">
<content:sysdata var="pointName" name="econ.elite.points" />
<div class="updateHdr">Flight Report ${eliteName} Data Recalculated</div>
<br />
<content:airline />&nbsp;<span class="pri bld">${eliteName}</span> data associated with this Flight Report has been recalculated. <fmt:int value="${score.points}" />&nbsp;${pointName} have now been awared for this flight.<br />
<br />
</c:if>
<c:if test="${eliteDataCleared}">
<content:airline />&nbsp;<span class="pri bld">${eliteName}</span> data associated with this Flight Report has been cleared.<br />
<br /></c:if>
<content:filter roles="PIREP"><c:if test="${isApprove || isReject || isHold || isDeleted}">
To return to the <content:airline /> submitted Flight Report queue, <el:cmd url="pirepqueue" className="sec bld">Click Here</el:cmd>.<br />
<c:if test="${fn:EventID(pirep) > 0}">
To view this flight's Online Event, <el:cmd url="event" linkID="${fn:EventID(pirep)}" className="sec bld">Click Here</el:cmd>.<br /></c:if>
<br />
</c:if></content:filter>
<content:filter roles="AcademyAdmin,HR"><c:if test="${academyEnabled && (!empty checkRide)}">
To return to the <content:airline /> Flight Academy Check Ride queue, <el:cmd url="academyridequeue" className="sec bld">Click Here</el:cmd>.<br />
</c:if></content:filter>
<c:if test="${!isDeleted}">
To view this Flight Report, <el:cmd url="pirep" link="${pirep}" noCache="true" className="sec bld">Click Here</el:cmd>.<br /></c:if>
<c:if test="${isOurs}">
To return to your Log Book, <el:cmd url="logbook" className="sec bld" noCache="true">Click Here</el:cmd>.<br /></c:if>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
