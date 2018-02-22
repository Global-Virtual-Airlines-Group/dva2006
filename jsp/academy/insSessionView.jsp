<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title>Flight Academy Instruction - ${session.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>
<c:set var="pilot" value="${pilots[session.pilotID]}" scope="page" />
<c:set var="ins" value="${pilots[session.instructorID]}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> FLIGHT ACADEMY INSTRUCTION SESSION</td>
</tr>
<c:if test="${!empty pilot}">
<tr>
 <td class="label">Pilot Name</td>
 <td class="data"><el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.name}</el:cmd>
 <span class="bld">(${pilot.pilotCode})</span>, ${pilot.rank.name}, ${pilot.equipmentType}</td>
</tr>
</c:if>
<c:if test="${!empty ins}">
<tr>
 <td class="label">Instructor Name</td>
 <td class="data"><el:cmd url="profile" link="${ins}" className="bld">${ins.name}</el:cmd></td>
</tr>
</c:if>
<tr>
 <td class="label">Course Name</td>
<c:if test="${viewCourse}">
 <td class="data"><el:cmd url="course" linkID="${fn:hex(session.courseID)}" className="bld">${session.name}</el:cmd></td>
</c:if>
<c:if test="${!viewCourse}">
 <td class="data bld">${session.name}</td>
</c:if>
</tr>
<tr>
 <td class="label">Start/End Times</td>
 <td class="data"><fmt:date t="HH:mm" date="${session.startTime}" /> - <fmt:date t="HH:mm" date="${session.endTime}" /></td>
</tr>
<tr>
 <td class="label">Status</td>
 <td class="data sec">${session.statusName}</td>
</tr>
<c:if test="${session.noShow}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data warn bld">PILOT DID NOT ATTEND INSTRUCTION SESSION</td>
</tr>
</c:if>
<tr>
 <td class="label top">Remarks</td>
 <td class="data"><fmt:msg value="${session.comments}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td> 
 <el:cmdbutton ID="CalendarButton" url="academycalendar" label="INSTRUCTION CALENDAR" />
<c:if test="${access.canEdit}">
 <el:cmdbutton ID="SchedButton" url="isession" op="edit" link="${session}" label="EDIT SESSION" />
</c:if>
<c:if test="${access.canCancel}">
 <el:cmdbutton ID="CancelButton" url="icancel" link="${session}" label="CANCEL SESSION" />
</c:if>
 </td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
