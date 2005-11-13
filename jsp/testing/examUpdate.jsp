<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Examination</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${isSubmit}">
<!-- Examination Submitted -->
<div class="updateHdr">Examination Submitted</div>
<br />
Your ${exam.name} Pilot Examination has been submitted to <content:airline />. Please allow
between 48 and 96 hours for this Examination to be evaluated and scored. Your will receive an
e-mail notification when this is complete.<br />
<br />
To return to the <content:airline /> Pilot Center, <el:cmd url="pilotcenter">Click Here</el:cmd>.<br />
</c:when>
<c:when test="${isScore}">
<!-- Examination Scored -->
<div class="updateHdr">Examination Scored</div>
<br />
This ${exam.name} Pilot Examination for ${pilot.name} has been successfully scored. An e-mail
message has been sent to ${pilot.rank} ${pilot.lastName} notifying him or her of the scoring
of this Examination.<br />
<br />
To view the Pilot Profile, <el:cmd url="profile" linkID="0x${pilot.ID}">Click Here</el:cmd>.<br />
To return to the Examination Queue, <el:cmd url="examqueue">Click Here</el:cmd>.<br />
</c:when>
<c:when test="${isDelete}">
<!-- Examination/Check Ride Deleted -->
<div class="updateHdr">Examination/Check Ride Deleted</div>
<br />
This <content:airline /> ${isCheckRide ? 'Check Ride' : 'Examination'} has been canceled and deleted 
from the database.<br />
</c:when>
</c:choose>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
