<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>${pilot.name} (${pilot.pilotCode})</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>
<c:set var="cspan" value="${(!empty exams) || (!empty statusUpdates) ? 6 : 1}" scope="request" />

<!-- Main Body Frame -->
<div id="main">
<el:table className="form" pad="default" space="default">
<!-- Pilot Title Bar -->
<tr class="title caps">
 <td colspan="${cspan + 1}">${pilot.rank} ${pilot.name} (${pilot.pilotCode})</td>
</tr>

<!-- Pilot Data -->
<tr>
 <td class="label">Pilot Status</td>
 <td colspan="${cspan}" class="data sec bld">${pilot.statusName}</td>
</tr>
<c:if test="${access.canChangeRoles && (!empty pilot.roles)}">
<tr>
 <td class="label">Security Roles</td>
 <td colspan="${cspan}" class="data"><fmt:list value="${pilot.roles}" delim=", " /></td>
</tr>
</c:if>
<tr>
 <td class="label">Equipment Type</td>
 <td colspan="${cspan}" class="data pri bld">${pilot.equipmentType}</td>
</tr>
<tr>
 <td class="label">Additional Ratings</td>
 <td colspan="${cspan}" class="data small"><fmt:list value="${pilot.ratings}" delim=", " /></td>
</tr>
<tr>
 <td class="label">Home Airport</td>
 <td colspan="${cspan}" class="data">${airport.name} (<fmt:airport airport="${airport}" />)</td>
</tr>
<c:if test="${!empty VATSIM_ID}">
<tr>
 <td class="label">VATSIM ID#</td>
 <td colspan="${cspan}" class="data">${pilot.networkIDs['VATSIM']}</td>
</tr>
</c:if>
<c:if test="${!empty IVAO_ID}">
<tr>
 <td class="label">IVAO ID#</td>
 <td colspan="${cspan}" class="data">${pilot.networkIDs['IVAO']}</td>
</tr>
</c:if>
<tr>
 <td class="label">Location</td>
 <td colspan="${cspan}" class="data sec">${pilot.location}</td>
</tr>
<tr>
 <td class="label">AOL Instant Messenger</td>
 <td colspan="${cspan}" class="data">${pilot.IMHandle}</td>
</tr>

<c:if test="${access.canViewEmail}">
<!-- E-Mail Information -->
<tr class="title">
 <td colspan="${cspan + 1}">E-MAIL CONTACT INFORMATION</td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
 <td colspan="${cspan}" class="data"><a href="mailto:${pilot.email}">${pilot.email}</a></td>
</tr>
</c:if>

<!-- Pilot Statistics -->
<tr class="title">
 <td colspan="${cspan + 1}">PILOT STATISTICS</td>
</tr>
<tr>
 <td class="label">Joined on</td>
 <td colspan="${cspan}" class="data"><fmt:date date="${pilot.createdOn}" fmt="d" d="EEEE MMMM dd, yyyy" /></td>
</tr>
<c:if test="${!empty pilot.lastLogin}">
<tr>
 <td class="label">Logins</td>
 <td colspan="${cspan}" class="data">${pilot.loginCount}, last on <fmt:date date="${pilot.lastLogin}" /></td>
</tr>
</c:if>
<c:if test="${!empty pilot.lastLogoff}">
<tr>
 <td class="label">Last Visited on</td>
 <td colspan="${cspan}" class="data"><fmt:date date="${pilot.lastLogoff}" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Flights</td>
 <td colspan="${cspan}" class="data">${pilot.legs} legs, ${pilot.hours} hours</td>
</tr>
<tr>
 <td class="label">Online Flights</td>
 <td colspan="${cspan}" class="data pri">${pilot.onlineLegs} legs, ${pilot.onlineHours} hours</td>
</tr>
<c:if test="${pilot.legacyHours > 0}">
<tr>
 <td class="label">Legacy Hours</td>
 <td colspan="${cspan}" class="data">${pilot.legacyHours} hours</td>
</tr>
</c:if>
<%@include file="/jsp/pilot/pilotExams.jsp" %>
<%@include file="/jsp/pilot/pilotStatusUpdate.jsp" %>
</el:table>
<c:if test="${access.canEdit}">
<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:cmdbutton url="profile" linkID="0x${pilot.ID}" op="edit" key="E" label="EDIT PROFILE" /></td>
</tr>
</el:table>
</c:if>
<content:copyright />
</div>
</body>
</html>
