<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Accomplishments</title>
<content:css name="main" />
<content:css name="view" />
<content:js name="common" />
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
<content:airline /> is committed to recognizing the accomplishments of our talented pilots. Every day,
they rack up countless flight hours and legs in a variety of aircraft types. Every day, Pilots take
written examinations and check rides and gain promotions in rank, and transfers into new equipment
programs. Please join us in congratulating these <content:airline /> members who have achieved the
following accomplishments:<br />
<br />
<el:table className="view">
<!-- Table Header Row -->
<tr class="title caps">
 <td style="width:12%">DATE</td>
 <td style="width:18%">PILOT NAME</td>
 <td>&nbsp;</td>
</tr>

<c:if test="${!empty promotions}">
<!-- Pilot Promotions -->
<tr class="title caps">
 <td colspan="3" class="left">PILOTS PROMOTED TO NEW EQUIPMENT PROGRAMS</td>
</tr>
<c:forEach var="promotion" items="${promotions}">
<c:set var="pilot" value="${pilots[promotion.ID]}" scope="page" />
<tr>
 <td class="pri bld"><fmt:date fmt="d" date="${promotion.date}" /></td>
 <td class="bld"><el:cmd url="profile" link="${promotion}">${pilot.name}</el:cmd></td>
 <td class="left">${promotion.description}</td>
</tr>
</c:forEach>
</c:if>

<c:if test="${!empty rankChanges}">
<!-- Rank Changes -->
<tr class="title caps">
 <td colspan="3" class="left">PILOTS PROMOTED WITHIN AN EQUIPMENT PROGRAM</td>
</tr>
<c:forEach var="promotion" items="${rankChanges}">
<c:set var="pilot" value="${pilots[promotion.ID]}" scope="page" />
<tr>
 <td class="pri bld"><fmt:date fmt="d" date="${promotion.date}" /></td>
 <td class="bld"><el:cmd url="profile" link="${promotion}">${pilot.name}</el:cmd></td>
 <td class="left">${promotion.description}</td>
</tr>
</c:forEach>
</c:if>

<c:if test="${!empty recognition}">
<!-- Pilot Accomplishments -->
<tr class="title caps">
 <td colspan="3" class="left">PILOT ACCOMPLISHMENTS AND MILESTONES</td>
</tr>
<c:forEach var="promotion" items="${recognition}">
<c:set var="pilot" value="${pilots[promotion.ID]}" scope="page" />
<tr>
 <td class="pri bld"><fmt:date fmt="d" date="${promotion.date}" /></td>
 <td class="bld"><el:cmd url="profile" link="${promotion}">${pilot.name}</el:cmd></td>
 <td class="left">${promotion.description}</td>
</tr>
</c:forEach>
</c:if>

<c:if test="${!empty ratingChanges}">
<!-- Rating Changes -->
<tr class="title caps">
 <td colspan="3" class="left">PILOTS GAINING ADDITIONAL RATINGS</td>
</tr>
<c:forEach var="promotion" items="${ratingChanges}">
<c:set var="pilot" value="${pilots[promotion.ID]}" scope="page" />
<tr>
 <td class="pri bld"><fmt:date fmt="d" date="${promotion.date}" /></td>
 <td class="bld"><el:cmd url="profile" link="${promotion}">${pilot.name}</el:cmd></td>
 <td class="left">${promotion.description}</td>
</tr>
</c:forEach>
</c:if>

<c:if test="${!empty academyCerts}">
<!-- Flight Academy Certifications -->
<tr class="title caps">
 <td colspan="3" class="left">PILOTS OBTAINING FLIGHT ACADEMY RATINGS</td>
</tr>
<c:forEach var="cert" items="${academyCerts}">
<c:set var="pilot" value="${pilots[cert.ID]}" scope="page" />
<tr>
 <td class="pri bld"><fmt:date fmt="d" date="${cert.date}" /></td>
 <td class="bld"><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td class="left">${cert.description}</td>
</tr>
</c:forEach>
</c:if>
<tr class="title">
 <td colspan="3">&nbsp;</td>
</tr>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
