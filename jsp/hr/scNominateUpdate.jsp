<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Senior Captain Nomination Updated</title>
<content:css name="main" />
<content:pics />
<content:favicon />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${isDisposed && isApproved}">
<div class="updateHdr"><content:airline /> Senior Captain Nomination Approved</div>
<br />
${pilot.name} has been succesfully promoted to Senior Captain. The profile has been updated and in
the future, each time ${pilot.name} achieves the pre-requisites for Captain in an equipment type
program, they will be automatically to the rank of Senior Captain.<br />
</c:when>
<c:when test="${isDisposed}">
<div class="updateHdr"><content:airline /> Senior Captain Nomination Rejected</div>
<br />
The nomination of ${pilot.name} for Senior Captain has been rejected. ${pilot.name} cannot be
re-nominated for Senior Captain again during the current calendar quarter.<br />
</c:when>
<c:when test="${isPurged}">
<div class="updateHdr"><content:airline /> Senior Captain Nominations Purged</div>
<br />
The nominations of the following pilots have been rejected. These pilots cannot be re-nominated for
Senior Captain again during the same calendar quarter in which they were nominated.<br />
<br />
<ul>
<c:forEach var="pilot" items="${pilots}">
<li><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></li>
</c:forEach>
</ul> 
</c:when>
<c:when test="${isPostponed}">
<div class="updateHdr"><content:airline /> Senior Captain Nominations Postponed</div>
<br />
The nominations of the following pilots have been moved from previous Quarters into the current Quarter.<br /> 
<br />
<ul>
<c:forEach var="pilot" items="${pilots}">
<li><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></li>
</c:forEach>
</ul> 
</c:when>
<c:when test="${isRescored}">
<div class="updateHdr"><content:airline /> Senior Captain Scores Recalculated</div>
<br />
All pending <content:airline /> Senior Captain nominations have been rescored.<c:if test="${!empty noms}"> The following nominations
have had their score changed:<br />
<br />
<ul>
<c:forEach var="nom" items="${noms}">
<c:set var="pilot" value="${pilots[nom.ID]}" scope="page" />
<li><el:cmd url="scnominate" link="${nom}">${pilot.name}</el:cmd> - score is now <span class="bld"><fmt:int value="${nom.score}" /></span></li>
</c:forEach>
</ul></c:if>
</c:when>
<c:otherwise>
<div class="updateHdr"><content:airline /> Senior Captain Nomination Created</div>
<br />
Thank you for nominating ${pilot.name} for the position of Senior Captain. Your contribution is
critical to recognizing our members who go above and beyond to make <content:airline /> a better
place for everyone.<br /> 
</c:otherwise>
</c:choose>
<br />
To return to the list of Senior Captain nominations, <el:cmd url="scnomcenter" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
