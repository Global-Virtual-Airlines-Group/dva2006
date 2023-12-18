<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Applicant Rejected</title>
<content:css name="main" />
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
<c:if test="${isDelete}">
<div class="updateHdr">Pilot Application Deleted</div>
<br />
The <content:airline /> Pilot application from ${applicant.name} has been deleted from the database.<br />
</c:if>
<c:if test="${!isDelete}">
<div class="updateHdr">Pilot Application Rejected</div>
<br />
The <content:airline /> Pilot application from ${applicant.name} has been rejected.<c:if test="${!empty blackListAdd}"> An e-mail message has been sent to ${applicant.email}.<br />
<br />
The IP address range ${blacklistAdd} has been added to the <content:airline /> login/registration blacklist.<br /></c:if>
<br />
To review this Applicant's profile, <el:cmd url="applicant" className="sec bld" link="${applicant}">Click Here</el:cmd>.<br />
</c:if>
<br />
To return to the Applicant Queue, <el:cmd url="applicants" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
