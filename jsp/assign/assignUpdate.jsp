<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Assignment Updated</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<c:choose>
<c:when test="${isCreate}">
<div class="updateHdr">Flight Assignment Created</div>
<br />
This <content:airline /> Flight Assigment has been successfully saved in the database.<br />
</c:when>

<c:when test="${isDelete}">
<div class="updateHdr">Flight Assignment Deleted</div>
<br />
This <content:airline /> Flight Assigment has been successfully removed from the database.<br />
<c:if test="${!empty flightsDeleted}">
<fmt:int value="${flightsDeleted}" /> Flight Reports were deleted from the database.<br />
</c:if>
<c:if test="${!empty flightsUpdated}">
<fmt:int value="${flightsUpdated}" /> Flight Reports were updated in the database.<br />
</c:if>
</c:when>

<c:when test="${isRelease}">
<div class="updateHdr">Flight Assignment Released</div>
<br />
This <content:airline /> Flight Assigment has been successfully released.<br />
<c:if test="${!empty flightsDeleted}">
<fmt:int value="${flightsDeleted}" /> Flight Reports were deleted from the database.<br />
</c:if>
<c:if test="${!empty flightsUpdated}">
<fmt:int value="${flightsUpdated}" /> Flight Reports were updated in the database.<br />
</c:if>
</c:when>

<c:when test="${isReserve}">
<div class="updateHdr">Flight Assignment Reserved</div>
<br />
This <content:airline /> Flight Assigment has been successfully reserved by ${pilot.rank} ${pilot.name}.<br />
<br />
<fmt:int value="${fn:sizeof(assign.flights)}" /> draft Flight Reports have been pre-populated within
our database.<br />
</c:when>

<c:otherwise>
<div class="updateHdr">Flight Assignment Updated</div>
<br />
This <content:airline /> Flight Assigment has been successfully saved in the database.<br />
</c:otherwise>
</c:choose>
<br />
To return to the <content:airline /> Pilot Center, <el:cmd url="pilotcenter">Click Here.</el:cmd><br />
<br />
<content:copyright />
</div>
</body>
</html>
