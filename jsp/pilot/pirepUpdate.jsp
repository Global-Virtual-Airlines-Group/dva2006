<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Report Updated</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="academyEnabled" name="academy.enabled" />

<!-- Main Body Frame -->
<content:region id="main">
<c:if test="${isSaved}">
<div class="updateHdr">Flight Report Saved</div>
<br />
This <content:airline /> Flight Report has been successfully and saved in the database.<br />
<c:if test="${isCreated}">
<br />
<span class="err">Please note that you must <u>SUBMIT</u> a Flight Report if you want the flight 
hours and flight leg to be counted here at <content:airline />. To Submit this Flight Report for 
approval,</span> <el:cmd className="sec bld" url="submit" link="${pirep}">Click Here</el:cmd>.<br />
</c:if>
</c:if>
<c:if test="${isSubmitted}">
<div class="updateHdr">Flight Report Submitted</div>
<br />
This <content:airline /> Flight Report has been submitted for review and approval. Once this Report
has been approved, your flight legs and hours will be automatically updated and you will be notified
via e-mail.<br />
<c:if test="${captEQ}">
<br />
This Flight Leg counts as one of the <fmt:int className="sec bld" value="${promoteLegs}" /> Flight Legs 
required for promotion to Captain in the <span class="pri bld">${eqType.name}</span> program.<br />
</c:if>
<c:if test="${notRated}">
<br />
<span class="warn bld">You do not appear to have a type rating in the ${pirep.equipmentType}. This may be
the result of our database being out of date or otherwise inaccurate.</span> This may cause a delay in your
Flight Report being approved. Please be aware that we cannot credit flights flown using unrated aircraft.<br />
</c:if>
<c:if test="${unknownRoute}">
<br />
<span class="warn bld">Your flight between ${pirep.airportD.name} and ${pirep.airportA.name} does not
currently appear in the <content:airline /> flight schedule.</span> This may cause a delay in your
Flight Report being approved.<br />
</c:if>
<c:if test="${timeWarning}">
<br />
<span class="warn bld">Your have logged <fmt:dec value="${pirep.length / 10.0}" /> flight hours for 
your flight between <fmt:airport airport="${pirep.airportD}" /> and <fmt:airport airport="${pirep.airportA}" />. 
The <content:airline /> flight schedule lists the average duration of flights between these two airports 
(including delays and turnaround time) as <fmt:dec value="${avgTime / 10}" /> hours.</span> This may cause 
a delay in your Flight Report being approved.<br />
</c:if>
<c:if test="${rangeWarning}">
<br />
<span class="warn bld">The flight leg between <fmt:airport airport="${pirep.airportD}" /> and <fmt:airport airport="${pirep.airportA}" /> 
is <fmt:int value="${pirep.distance}" /> miles, and appears to exceed the maximum range of the ${pirep.equipmentType}.</span> 
This may cause a delay in your Flight Report being approved.<br />
</c:if>
<br />
To return to your log book, <el:cmd url="logbook" link="${pilot}" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${isApprove}">
<div class="updateHdr">Flight Report Approved</div>
<br />
This Flight Report has been approved, and an e-mail message has been sent to ${pilot.name}.<br />
<c:if test="${assignID}">
This Pilot has been assigned a Pilot ID and seniority number at <content:airline />. The new Pilot ID 
for ${pilot.name} is <b>${pilot.pilotCode}</b>.<br />
</c:if>
<c:if test="${!empty centuryClub}">
<br />
<span class="sec bld">With the approval of this Flight Report, ${pilot.name} has joined the &quot;${centuryClub}&quot;.</span><br />
</c:if>
<c:if test="${assignComplete}">
With the approval of this Flight Report, a Flight Assignment has been successfully completed.<br />
<br />
</c:if>
<c:if test="${acarsArchive}">
This flight was logged using <content:airline /> ACARS software, and all position data has been archived 
for later retrieval.<br />
<br />
</c:if>
<c:if test="${!empty checkRide}">
<br />
This Flight Report is for a <b>${checkRide.name}</b>, and this Check Ride has been graded as part
of the approval process for this Flight Report. This Check Ride has been <span class="sec bld">
${checkRide.passFail ? 'APPROVED' : 'REJECTED' }</span>.<br />
</c:if>
</c:if>
<c:if test="${isHold}">
<div class="updateHdr">Flight Report Held</div>
<br />
This Flight Report has been marked as &quot;On Hold&quot; and an e-mail message has been sent to
${pilot.name}. Please work to resolve any issues with this Flight Report within the next few days.<br />
</c:if>
<c:if test="${isReject}">
<div class="updateHdr">Flight Report Rejected</div>
<br />
This Flight Report has been rejected and an e-mail message has been sent to ${pilot.name}.<br />
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

<content:filter roles="PIREP"><c:if test="${isApprove || isReject || isHold || isDeleted}">
To return to the <content:airline /> submitted Flight Report queue, <el:cmd url="pirepqueue" className="sec bld">Click Here</el:cmd>.<br />
<br />
</c:if></content:filter>
<content:filter roles="AcademyAdmin,HR"><c:if test="${academyEnabled && (!empty checkRide)}">
To return to the <content:airline /> Flight Academy Check Ride queue, <el:cmd url="academyridequeue" className="sec bld">Click Here</el:cmd>.<br />
</c:if></content:filter>
<c:if test="${!isDeleted}">
To view this Flight Report, <el:cmd url="pirep" link="${pirep}" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
