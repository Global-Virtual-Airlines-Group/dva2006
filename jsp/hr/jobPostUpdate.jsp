<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Job Posting Updated</title>
<content:pics />
<content:favicon />
<content:css name="main" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${isApply}">
<div class="updateHdr"><content:airline /> Job Application Saved</div>
<br />
${user.firstName}, thank you for your application to become a part of the <content:airline /> volunteer staff,
in the role of <span class="pri bld">${job.title}</span>! We appreciate your willingness to contribute back to
this community. We are currently accepting applications for this position until <fmt:date fmt="d" date="${job.closesOn}" />,
and will be contacting you after that date about your application.<br />
<c:if test="${saveProfile}">
<br />
Your application text has been saved in a separate location, for re-use when applying for other volunteer
opportunities at <content:airline />.<br /></c:if>
<br />
Thank you again for your contribution!<br />
</c:when>
<c:when test="${isSave}">
<div class="updateHdr"><content:airline /> Job Posting Updated</div>
<br />
The Job Posting for <span class="pri bld">${job.title}</span> has been saved, and will be available for applications
until <fmt:date fmt="d" date="${job.closesOn}" />.<br />
<c:if test="${job.staffOnly}">
<br />
<span class="sec bld">This Job Posting is currently restricted to existing members of the <content:airline /> volunteer
staff. Individuals who are not members of the staff cannot view or apply for this position.</span><br /></c:if>
</c:when>
<c:when test="${isShortlisted}">
<div class="updateHdr"><content:airline /> Job Posting Shortlist Created</div>
<br />
A list of shortlisted applicants for the <span class="pri bld">${job.title}</span> Job Posting has been generated. The
following applicants are on the shortlist:<br />
<ul>
<c:forEach var="app" items="${shortlist}">
<li><el:cmd url="profile" link="${app}">${app.firstName} ${app.lastName}</el:cmd></li>
</c:forEach>
</ul> 
<br />
To return to this <content:airline /> Job Posting, <el:cmd url="job" className="sec bld" link="${job}">Click Here</el:cmd>.<br />
</c:when>
<c:when test="${isSelected}">
<div class="updateHdr"><content:airline /> Job Posting Hire Selected</div>
<br />
One or more shortlisted applicants for the <span class="pri bld">${job.title}</span> Job Posting has been selected. The
following shortlisted applicants have been selected:<br />
<ul>
<c:forEach var="app" items="${selected}">
<li><el:cmd url="profile" link="${app}">${app.firstName} ${app.lastName}</el:cmd></li>
</c:forEach>
</ul>
<br />
To return to this <content:airline /> Job Posting, <el:cmd url="job" className="sec bld" link="${job}">Click Here</el:cmd>.<br />
</c:when>
<c:when test="${isCompleted}">
<c:set var="selectedApps" value="${job.selectedApplications}" scope="page" />
<div class="updateHdr"><content:airline /> Job Posting Completed</div>
<br />
The <content:airline /> Job Posting for <span class="pri bld">${job.title}</span> has been completed, and 
<c:forEach var="app" items="${selectedApps}" varStatus="appStatus">
<span class="bld">${app.name}</span><c:if test="${!acpStatus.last}">, </c:if></c:forEach>
 ${(fn:sizeof(selectedApps) > 1) ? 'were' : 'was' } recorded as hired.<br />
<br />
To return to this <content:airline /> Job Posting, <el:cmd url="job" className="sec bld" link="${job}">Click Here</el:cmd>.<br />
</c:when>
<c:when test="${isDelete}">
<div class="updateHdr"><content:airline /> Job Posting Deleted</div>
<br />
The <content:airline /> Job Posting for <span class="pri bld">${job.title}</span> has been deleted.<br />
</c:when>
</c:choose>
<br />
To return to the list of <content:airline /> volunteer opportunities, <el:cmd url="careers" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
