<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Equipment Transfer Request for ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.crType, 'Aircraft Type')) return false;
if (!validateCombo(form.eqType, 'Equimpment Program')) return false;
if (!validateCombo(form.rank, 'Rank in the new Equipment Program')) return false;
if (!validateText(form.comments, 25, 'Check Ride Comments')) return false;

setSubmit();
disableButton('ProfileButton');
disableButton('AssignButton');
disableButton('ApproveButton');
disableButton('RejectButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>
<c:set var="cmdName" value="${access.canApprove ? 'transfer' : 'crassign'}" scope="request" />

<!-- Main Body Frame -->
<div id="main">
<el:form action="${cmdName}.do" method="POST" linkID="0x${txReq.ID}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2">EQUIPMENT TRANSFER REQUEST - ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><span class="pri bld">${eqType.name}</span> (Stage <fmt:int value="${eqType.stage}" />)</td>
</tr>
<tr>
 <td class="label">Current Rank</td>
 <td class="data">${pilot.rank}, ${pilot.equipmentType} (Stage <fmt:int value="${currentEQ.stage}" />)</td>
</tr>
<tr>
 <td class="label">Transfer Status</td>
 <td class="data"><span class="sec bld">${txReq.statusName}</span> (Created on <fmt:date fmt="d" date="${txReq.date}" />)</td>
</tr>
<c:if test="${!empty checkRide}">
<tr>
 <td class="label">Check Ride Status</td>
<c:choose>
<c:when test="${fn:passed(checkRide)}">
 <td class="data ter bld caps">CHECK RIDE PASSED ON <fmt:date fmt="d" date="${checkRide.scoredOn}" /></td>
</c:when>
<c:when test="${fn:failed(checkRide)}">
 <td class="data error bld caps">CHECK RIDE FAILED ON <fmt:date fmt="d" date="${checkRide.scoredOn}" /></td>
</c:when>
<c:otherwise>
 <td class="data bld caps">CHECK RIDE ASSIGNED ON <fmt:date fmt="d" date="${checkRide.date}" /></td>
</c:otherwise>
</c:choose>
</tr>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data">${checkRide.equipmentType}</td>
</tr>
<tr>
 <td class="label" valign="top">Comments</td>
 <td class="data"><fmt:text value="${checkRide.comments}" /></td>
</tr>
<c:if test="${checkRide.flightID != 0}">
<tr>
 <td class="label">ACARS Flight ID</td>
 <td class="data"><fmt:int value="${checkRide.flightID}" /></td>
</tr>
</c:if>
</c:if>
<c:if test="${access.canApprove}">
<tr class="title caps">
 <td colspan="2">APPROVE TRANSFER REQUEST</td>
</tr>
<tr>
 <td class="label">Transfer to</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" firstEntry="-" options="${activeEQ}" value="${eqType}" />
 as <el:combo name="rank" idx="*" size="1" options="${eqType.ranks}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Promotion to Captain</td>
 <td class="data">Examination Status <b>${captExam ? 'PASSED' : 'NOT PASSED'}</b>, Flight Legs completed
 = <fmt:int value="${promoLegs}" />.<c:if test="${captOK}"><span class="ter bld caps">ELIGIBLE FOR PROMOTION
 TO CAPTAIN</span></c:if></td>
</tr>
</c:if>
<c:if test="${access.canAssignRide}">
<tr class="title caps">
 <td colspan="2">ASSIGN CHECK RIDE</td>
</tr>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data"><el:combo name="crType" idx="*" size="1" firstEntry="-" options="${eqType.primaryRatings}" value="${eqType.name}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="120" height="4"></el:textbox></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="useScript" idx="*" value="true" label="Append Check Ride script to comments" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:cmdbutton ID="ProfileButton" url="profile" linkID="0x${pilot.ID}" label="VIEW PROFILE" />
<c:if test="${access.canAssignRide}">
<el:button ID="AssignButton" type="submit" className="BUTTON" label="ASSIGN CHECK RIDE" />
</c:if>
<c:if test="${access.canApprove}">
<el:button ID="ApproveButton" type="submit" className="BUTTON" label="APPROVE TRANSFER" />
</c:if>
<c:if test="${access.canReject}">
<el:cmdbutton ID="RejectButton" url="txreqreject" linkID="0x${txReq.ID}" label="REJECT TRANSFER" />
</c:if>
 </td>
</tr>
</el:table>
<c:if test="${access.canAssignRide}">
<el:text type="hidden" name="eqType" value="${eqType.name}" />
</c:if>
</el:form>
<br />
<content:copyright />
</div>
</body>
</html>
