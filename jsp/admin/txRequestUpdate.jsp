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
<content:copyright visible="false" />
</head>
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<c:if test="${isNew}">
<div class="updateHdr">Transfer Request Submitted</div>
<br />
Your request to be transferred to the <span class="pri bld">${txReq.equipmentType}</span> program has been 
submitted. A Check Ride may be required in order to complete the transfer. If this is the case, you will 
be notified via e-mail within the next 24 to 72 hours regarding the requirements for your your Check Ride.<br />
</c:if>

<c:if test="${isApprove}">
<div class="updateHdr">Equipment Program Transfer Approved</div>
<br />
<span class="sec bld">${pilot.name}</span> has been successfully transferred to the <span class="pri bld">${eqType.name}</span> 
equipment program.<c:if test="${!empty newRatings}">The following additional ratings have been granted: 
<fmt:list value="${newRatings}" delim=", " />.</c:if><br />
<br />
An e-mail message has been sent to ${pilot.name} informing of the equipment program transfer.<br />
</c:if>

<c:if test="${isReject}">
<div class="updateHdr">Equipment Program Transfer Rejected</div>
<br />
This equipment program transfer request to the <span class="pri bld">${txreq.equipmentType}</span> program 
has been rejected. An e-mail message has been sent to ${pilot.name} informing of the rejection.<br />
</c:if>
<br />
<content:copyright />
</div>
</body>
</html>
