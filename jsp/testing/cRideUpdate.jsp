<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Check Ride</title>
<content:css name="main" browserSpecific="true" />
</head>
<content:copyright visible="false" />
<body>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<c:choose>
<c:when test="${isScore}">
<!-- Check Ride Scored -->
<div class="updateHdr">Check Ride Scored</div>
<br />
This ${checkRide.equipmentType} for ${pilot.name} has been successfully scored. An e-mail
message has been sent to ${pilot.rank} ${pilot.lastName} notifying him or her of the scoring
of this Check Ride.<br />
<br />
To view the Pilot Profile, <el:cmd url="profile" linkID="0x${pilot.ID}">Click here</el:cmd>.<br />
To return to the Examination Queue, <el:cmd url="examqueue">Click Here</el:cmd>.<br />
</c:when>

<c:when test="${isAssign}">
<!-- Check Ride Assigned -->
<div class="updateHdr">Check Ride Assigned</div>
<br />
This <content:airline /> ${checkRide.name} Check Ride has been assigned to ${pilot.name}, 
and an e-mail message has been sent to the Pilot.<br />
</c:when>
</c:choose>
<br />
<content:copyright />
</div>
</body>
</html>
