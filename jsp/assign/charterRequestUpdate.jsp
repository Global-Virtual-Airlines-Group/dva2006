<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Charter Request Updated</title>
<content:css name="main" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${isCreate}">
<div class="updateHdr">Charter Flight Request Created</div>
<br />
This <content:airline /> Charter Flight Request has been successfully saved in the database.<br />
</c:when>
<c:when test="${isEdit}">
<div class="updateHdr">Charter Flight Request Updated</div>
<br />
This <content:airline /> Charter Flight Request has been successfully updated.<br />
</c:when>
<c:when test="${isDelete}">
<div class="updateHdr">Charter Flight Request Deleted</div>
<br />
This <content:airline /> Charter Flight Request has been successfully removed from the database.<br />
</c:when>
<c:when test="${!empty reqStatus}">
<content:defaultMethod var="status" object="${reqStatus}"  method="description" />
<div class="updateHdr">Charter Flight Request ${status}</div>
<br />
This <content:airline /> Charter Flight Request has been ${status}. <c:if test="${isApprove}">A Flight Assignment and pre-approved Flight Report for ${pilot.name} have been pre-populated in the database for a flight outside the <content:airline /> Flight Schedule.</c:if><br />
</c:when>
</c:choose>
<br />
<c:if test="${!empty req}">To view this Charter Flight Request, <el:cmd url="chreq" link="${req}" className="sec bld">Click Here</el:cmd>.<br /></c:if>
To return to the list of Charter Flight Requests, <el:cmd url="chreqs" className="sec bld">Click Here</el:cmd>.<br />
To return to the <content:airline /> Pilot Center, <el:cmd url="pilotcenter" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
