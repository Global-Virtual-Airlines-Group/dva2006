<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title>Equipment Transfer Request for ${pilot.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
if (f.action.indexOf('txreqdelete.do') == -1) {
	golgotha.form.validate({f:f.crType, t:'Aircraft Type'});
	golgotha.form.validate({f:f.rank, t:'Rank in the new Equipment Program'});
	if ((f.eqType) && (f.eqType.options))
		golgotha.form.validate({f:f.eqType, t:'Equimpment Program'});
} else
	golgotha.form.validate({f:f.rejectComments, l:2, t:'Rejection Comments'});

golgotha.form.submit(f);
return true;
};

golgotha.local.toggleBody = function(id, type)
{
var row = document.getElementById('body' + type + id);
var linkDesc = document.getElementById('toggle' + type + id);
var visible = (row.style.display != 'none');
golgotha.util.display(row, !visible);
linkDesc.innerHTML = visible ? 'View' : 'Hide';
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="maxRides" name="users.checkride_max" default="10" /> 
<c:set var="cmdName" value="${access.canApprove ? 'transfer' : 'crassign'}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="${cmdName}.do" method="post" link="${txReq}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">EQUIPMENT TRANSFER REQUEST - ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><span class="pri bld">${eqType.name}</span> (Stage <fmt:int value="${eqType.stage}" />)</td>
</tr>
<tr>
 <td class="label">Current Rank</td>
 <td class="data">${pilot.rank.name}, ${pilot.equipmentType} (Stage <fmt:int value="${currentEQ.stage}" />)</td>
</tr>
<tr>
 <td class="label">Preferred Simulator</td>
 <td class="data bld">${txReq.simulator.name}</td>
</tr>
<tr>
 <td class="label">Preferred Aircraft</td>
 <td class="data sec bld">${txReq.aircraftType}</td>
</tr>
<tr>
 <td class="label">Transfer Status</td>
 <td class="data"><span class="sec bld">${txReq.status.description}</span> (Created on <fmt:date t="HH:mm" date="${txReq.date}" />)</td>
</tr>
<c:if test="${txReq.ratingOnly}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data ter bld">PILOT IS REQUESTING ADDITIONAL RATINGS ONLY</td>
</tr>
</c:if>
<c:if test="${!empty checkRides}">
<tr class="title caps">
 <td colspan="2">CHECK RIDE STATUS</td>
</tr>
<c:set var="rideCount" value="0" scope="page" />
<c:forEach var="rideID" items="${txReq.checkRideIDs}">
<c:set var="checkRide" value="${checkRides[rideID]}" scope="page" />
<c:set var="pirep" value="${pireps[checkRide.flightID]}" scope="page" />
<c:set var="rideCount" value="${rideCount + 1}" scope="page" />
<c:set var="hasComments" value="${(!empty pirep) && (!empty pirep.comments)}" scope="page" />
<c:set var="scorer" value="${scorers[checkRide.scorerID]}" scope="page" />
<tr>
 <td class="label top">Check Ride #<fmt:int value="${rideCount}" /></td>
 <td class="data">Assigned on <fmt:date fmt="d" date="${checkRide.date}" /> in ${checkRide.equipmentType}
&nbsp;<a href="javascript:void golgotha.local.toggleBody(${rideCount}, 'D')"><span id="toggleD${rideCount}">View</span> Description</a>
<c:if test="${hasComments}">
 <a href="javascript:void golgotha.local.toggleBody(${rideCount}, 'C')"><span id="toggleC${rideCount}">View</span> Comments</a></c:if><br />
<c:choose>
<c:when test="${fn:passed(checkRide)}">
 <span class="ter bld caps">CHECK RIDE PASSED ON <fmt:date fmt="d" date="${checkRide.scoredOn}" /> by ${scorer.name}</span>
</c:when>
<c:when test="${fn:failed(checkRide)}">
 <span class="error bld caps">CHECK RIDE FAILED ON <fmt:date fmt="d" date="${checkRide.scoredOn}" /> by ${scorer.name}</span>
</c:when>
<c:when test="${fn:submitted(checkRide)}">
 <span class="bld pri caps">CHECK RIDE SUBMITTED ON <fmt:date fmt="d" date="${checkRide.submittedOn}" /></span>
</c:when>
</c:choose></td>
</tr>
<c:if test="${!empty pirep}">
<tr>
 <td class="label">Check Ride #<fmt:int value="${rideCount}" /> Data</td>
 <td class="data">ACARS Flight <fmt:int value="${checkRide.flightID}" /> - <el:cmd url="crview" link="${checkRide}" className="pri bld">VIEW FLIGHT REPORT</el:cmd></td>
</tr>
</c:if>
<tr id="bodyD${rideCount}" style="display:none;">
 <td class="label top">Check Ride #<fmt:int value="${rideCount}" /> Description</td>
 <td class="data"><fmt:text value="${checkRide.comments}" /></td>
</tr>
<c:if test="${hasComments}">
<tr id="bodyC${rideCount}" style="display:none;">
 <td class="label top">Check Ride #<fmt:int value="${rideCount}" /> Comments</td>
 <td class="data"><fmt:text value="${pirep.comments}" /></td>
</tr>
</c:if>
</c:forEach>
</c:if>

<c:if test="${access.canApprove}">
<tr class="title caps">
 <td colspan="2">APPROVE TRANSFER REQUEST</td>
</tr>
<c:if test="${!txReq.ratingOnly}">
<tr>
 <td class="label">Transfer to</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" firstEntry="-" className="req" options="${activeEQ}" value="${eqType}" /> as <el:combo name="rank" idx="*" size="1" options="${newRanks}" className="req" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Promotion to Captain</td>
 <td class="data">Examination Status <b>${captExam ? 'PASSED' : 'NOT PASSED'}</b>, Flight Legs completed = <fmt:int value="${promoLegs}" />.<c:if test="${captOK}"><span class="ter bld caps"> ELIGIBLE FOR PROMOTION TO CAPTAIN</span></c:if></td>
</tr>
</c:if>
<tr>
 <td class="label top">Equipment Ratings</td>
 <td class="data"><el:check name="ratings" idx="*" cols="8" width="120" newLine="true" className="small" checked="${newRatings}" options="${allEQ}" /></td>
</tr>
</c:if>
<c:if test="${access.canAssignRide}">
<tr class="title caps">
 <td colspan="2">ASSIGN CHECK RIDE</td>
</tr>
<c:if test="${fn:sizeof(txReq.checkRideIDs) >= maxRides}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data warn bld caps">${pilot.name} has already been assigned <fmt:int value="${fn:sizeof(txReq.checkRideIDs)}" /> Check Rides</td>
</tr>
</c:if>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data"><el:combo name="crType" idx="*" size="1" firstEntry="-" className="req" options="${eqType.primaryRatings}" value="${txreq.aircraftType}" /></td>
</tr>
<tr>
 <td class="label top">Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="80%" height="3" resize="true"></el:textbox></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="useScript" idx="*" value="true" checked="true" label="Append Check Ride script to comments" /></td>
</tr>
</c:if>
<c:if test="${access.canReject}">
<tr class="title caps">
 <td colspan="2">REJECT EQUIPMENT TRANSFER REQUEST</td>
</tr>
<tr>
 <td class="label top">Rejection Commnents</td>
 <td class="data"><el:textbox name="rejectComments" idx="*" width="80%" height="3" resize="true"></el:textbox></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:cmdbutton ID="ProfileButton" url="profile" link="${pilot}" label="VIEW PROFILE" />
<c:if test="${!empty checkRide}">
&nbsp;<el:cmdbutton ID="CheckRideButton" url="checkride" link="${checkRide}" label="VIEW CHECK RIDE" /></c:if>
<c:if test="${access.canAssignRide}">
&nbsp;<el:button ID="AssignButton" type="submit" label="ASSIGN CHECK RIDE" /></c:if>
<c:if test="${access.canApprove}">
&nbsp;<el:button ID="ApproveButton" type="submit" label="APPROVE TRANSFER" /></c:if>
<c:if test="${access.canReject}">
&nbsp;<el:cmdbutton ID="RejectButton" url="txreqreject" link="${txReq}" post="true" label="REJECT TRANSFER" /></c:if>
<c:if test="${access.canToggleRatings}">
<c:set var="tgLabel" value="${txReq.ratingOnly ? 'CONVERT TO PROGRAM CHANGE' : 'CONVERT TO RATINGS ONLY'}" scope="page" />
&nbsp;<el:cmdbutton ID="ToggleButton" url="txreqtoggle" link="${txReq}" label="${tgLabel}" /></c:if>
<c:if test="${access.canDelete}">
&nbsp;<el:cmdbutton ID="DeleteButton" url="txreqdelete" link="${txReq}" label="DELETE TRANSFER" /></c:if>
 </td>
</tr>
</el:table>
<c:if test="${access.canAssignRide}">
<el:text type="hidden" name="eqType" value="${eqType.name}" /></c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
