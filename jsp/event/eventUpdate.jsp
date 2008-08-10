<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Event Updated</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${isNew}">
<div class="updateHdr">Online Event Created</div>
<br />
The <span class="pri bld">${event.name}</span> Online Event has been successfully created.<br />
</c:when>
<c:when test="${isDelete}">
<div class="updateHdr">Online Event Deleted</div>
<br />
The <span class="pri bld">${event.name}</span> Online Event has been deleted from the database.<br />
</c:when>
<c:otherwise>
<div class="updateHdr">Online Event Updated</div>
<br />
The <span class="pri bld">${event.name}</span> Online Event has been successfully updated.<br />
</c:otherwise>
</c:choose>
<br />
<c:if test="${!isDelete}">
To view this Online Event, <el:cmd className="sec bld" url="event" link="${event}">Click Here</el:cmd>.<br /></c:if>
<c:if test="${isNew}">
To add additional routes to this Online Event, <el:cmd className="sec bld" url="eventroutes" link="${event}">Click Here</el:cmd>.<br /></c:if>
To view the <content:airline /> Online Event Calendar, <el:cmd className="sec bld" url="eventcalendar">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
