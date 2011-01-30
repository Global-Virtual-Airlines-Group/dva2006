<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Academy Approval Queue</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" cmd="coursequeue">
<tr class="title">
 <td colspan="7" class="left caps"><content:airline /> FLIGHT ACADEMY COMPLETION QUEUE</td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td width="20%">COURSE NAME</td>
 <td width="20%">PILOT NAME</td>
 <td width="7%">STAGE</td>
 <td width="20%">INSTRUCTOR NAME</td>
 <td width="10%">STATUS</td>
 <td width="10%">STARTED</td>
 <td>LAST COMMENT</td>
</tr>

<!-- Table View data -->
<c:forEach var="course" items="${viewContext.results}">
<c:set var="pilotLoc" value="${userData[course.pilotID]}" scope="page" />
<c:set var="pilot" value="${pilots[course.pilotID]}" scope="page" />
<c:set var="ins" value="${pilots[course.instructorID]}" scope="page" />
<view:row entry="${course}">
 <td><el:cmd url="course" link="${course}" className="pri bld">${course.name}</el:cmd></td>
 <td><el:profile location="${pilotLoc}" className="sec bld">${pilot.name}</el:profile> <span class="small">(${pilot.pilotCode})</span></td>
 <td class="bld"><fmt:int value="${course.stage}" /></td>
<c:choose>
<c:when test="${empty ins}">
 <td class="sec bld">Self-Directed</td>
</c:when>
<c:otherwise>
 <td>${instructor.name}</td>
</c:otherwise>
</c:choose>
 <td class="pri bld">${course.statusName}</td>
 <td class="small"><fmt:date fmt="d" date="${course.startDate}" /></td>
 <td class="sec small"><fmt:date fmt="d" date="${course.lastComment}" default="-" /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title caps">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
