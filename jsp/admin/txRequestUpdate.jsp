<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Transfer Request</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
<content:copyright visible="false" />
</head>
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:if test="${isNew}">
<c:if test="${txReq.ratingOnly}">
<div class="updateHdr">Additional Rating Request Submitted</div>
<br />
Your request for additional ratings from the <span class="pri bld">${txReq.equipmentType}</span></c:if>
<c:if test="${!txReq.ratingOnly}">
<div class="updateHdr">Transfer Request Submitted</div>
<br />
Your request to be transferred to the <span class="pri bld">${txReq.equipmentType}</span></c:if>
<br />
program has been submitted. A Check Ride may be required in order to complete the process. If this is the case, 
you will be notified via e-mail within the next 24 to 72 hours regarding the requirements for your Check Ride.<br />
</c:if>
<c:if test="${isApprove}">
<c:if test="${txReq.ratingOnly}">
<div class="updateHdr">Additional Ratings Approved</div>
<br />
<span class="sec bld">${pilot.name}</span>'s additional rating request has been approved.<br />
</c:if>
<c:if test="${!txReq.ratingOnly}">
<div class="updateHdr">Equipment Program Transfer Approved</div>
<br />
<span class="sec bld">${pilot.name}</span> has been successfully transferred to the <span class="pri bld">${eqType.name}</span> 
equipment program.<br />
</c:if>
<c:if test="${!empty addedRatings}">
<br />
The following equipment type ratings have been granted: <fmt:list value="${addedRatings}" delim=", " />.<br />
</c:if>
<c:if test="${!empty removedRatings}">
<br />
The following equipment type ratings have been removed: <fmt:list value="${removedRatings}" delim=", " />.<br />
</c:if>
<br />
An e-mail message has been sent to ${pilot.name} informing of the equipment program transfer.<br />
</c:if>
<c:if test="${isReject}">
<div class="updateHdr">Equipment Program Transfer Rejected</div>
<br />
This equipment program transfer request to the <span class="pri bld">${txreq.equipmentType}</span> program 
has been rejected. An e-mail message has been sent to ${pilot.name} informing of the rejection.<br />
<c:if test="${checkRideDelete}">
<br />
${pilot.name}'s pending Check Ride has been deleted.<br />
</c:if>
</c:if>
<c:if test="${isDelete}">
<div class="updateHdr">Equipment Program Transfer Deleted</div>
<br />
${pilot.name}'s equipment program transfer request to the <span class="pri bld">${txreq.equipmentType}</span> program 
has been deleted.<c:if test="${checkRideDelete}"> ${pilot.name}'s pending Check Ride has been deleted.</c:if><br />
</c:if>
<c:if test="${isEmpty}">
<div class="updateHdr">Equipment Program Transfer Unavailable</div>
<br />
You are not able to request a transfer into or additional ratings in any <content:airline /> Equipment programs.<br />
</c:if>
<c:if test="${isApprove || isReject || isDelete}">
<br />
<c:if test="${!isDelete || !isOwn}">
To return to the list of pending equipment program transfer requests, <el:cmd url="txrequests" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${isDelete && isOwn}">
To return to the <content:airline /> Pilot Center, <el:cmd url="pilotcenter" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
