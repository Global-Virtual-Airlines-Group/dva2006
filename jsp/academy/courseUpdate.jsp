<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Academy Course Updated</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:if test="${isDelete}">
<!-- Flight Academy Course Deleted -->
<div class="updateHdr">Flight Academy Course Deleted</div>
<br />
The <content:airline /> Flight Academy Course <span class="pri bld">${course.name}</span> for 
${pilot.name} has been deleted.<br />
</c:if>
<c:if test="${isRestarted}">
<!-- Flight Academy Course Restarted -->
<div class="updateHdr">Flight Academy Course Re-Enrollment Requested</div>
<br />
You have requested re-enrollment in the <content:airline /> Flight Academy course <span class="pri bld">${course.name}</span>. A Flight
Academy Instructor should be in contact with you soon about your request.<br />
<br />
To view this Course, <el:cmd url="course" link="${course}" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${isAbandoned}">
<!-- Flight Academy Course Abandoned -->
<div class="updateHdr">Flight Academy Course Withdrawal</div>
<br />
You have withdrawn from the <content:airline /> Flight Academy course <span class="pri bld">${course.name}</span>.<br />
</c:if>
<c:if test="${isCompleted}">
<!-- Flight Academy Course Completed -->
<div class="updateHdr">Flight Academy Course Completed</div>
<br />
${pilot.name} has successfully completed the <content:airline /> Flight Academy 
course <span class="pri bld">${course.name}</span>, and has been awarded the <b>${course.name}</b> 
Certification. An e-mail message has been sent to ${pilot.rank} ${pilot.lastName}.<br />
<br />
To view the Pilot profile, <el:cmd url="profile" link="${pilot}" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${isPending}">
<!-- Flight Academy Course Enrollment Pending -->
<div class="updateHdr">Flight Academy Course Enrollment Pending</div>
<br />
Thank you for your interest in the <content:airline /> Flight Academy. Your enrollment request has been 
saved, and will be processed within 24 to 72 hours. Once you have obtained at least one Stage 1 Flight 
Certification, you will be able to enroll in new courses without any waiting period.<br />
</c:if>
<c:if test="${isSessionUpdate}">
<!-- Flight Academy Instructor Session Updated -->
<div class="updateHdr">Flight Academy Instructor Session Updated</div>
<br />
The Flight Academy Instructor Session with ${pilot.name} has been updated in the Flight Academy 
Instruction Calendar.<c:if test="${emailSent}"> An e-mail message has been sent to ${pilot.name} 
with information about this session.</c:if><br />
<br />
To view the <content:airline /> Flight Acadamy Instruction Calendar, <el:cmd url="academycalendar" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${isSessionCancel}">
<c:set var="pilot" value="${pilots[session.pilotID]}" scope="request" />
<c:set var="ins" value="${pilots[session.instructorID]}" scope="request" />
<!-- Flight Academy Instructor Session Canceled -->
<div class="updateHdr">Flight Academy Instructor Session Canceled</div>
<br />
The Flight Academy Instructor Session with ${pilot.name} and ${ins.name} has been canceled, and the 
Fleet Academy Instruction Calendar has been updated. An e-mail message has been sent to ${pilot.name} 
with information about this session.<br />
</c:if>
<c:if test="${isAssign}">
<div class="updateHdr">Flight Academy Check Ride Assigned</div>
<br />
This <content:airline /> Fleet Academy ${checkRide.name} Check Ride has been assigned to ${pilot.name}, 
and an e-mail message has been sent to the Pilot.<br />
</c:if>
<c:if test="${isRideAlreadyAssigned}">
<div class="updateHdr">Check Ride Pending</div>
<br />
A ${checkRide.equipmentType} check ride is currently pending for ${pilot.name}. No new check rides can 
be assigned while one is currently pending.<br />
</c:if>
<c:if test="${flightUpdate}">
<div class="updateHdr">Instruction Flight Log Updated</div>
<br />

</c:if>
<br />
To return to the <content:airline /> Flight Academy, <el:cmd url="academy" className="sec bld">Click Here</el:cmd>.<br />
<content:filter roles="Instructor,HR">
<br />
To view all active <content:airline /> Flight Academy courses, <el:cmd url="courses" op="active" className="sec bld">Click Here</el:cmd>.<br />
To view all <content:airline /> Flight Academy certifications, <el:cmd url="certs" className="sec bld">Click Here</el:cmd>.<br />
</content:filter>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
