<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Accomplishments</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
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
<el:table className="view" space="default" pad="default">
<!-- Table Header Row -->
<tr class="title caps">
 <td width="12%">DATE</td>
 <td width="18%">PILOT NAME</td>
 <td>&nbsp;</td>
</tr>

<c:if test="${!empty promotions}">
<!-- Pilot Promotions -->
<tr class="title caps">
 <td colspan="3" class="left">PILOTS PROMOTED TO NEW EQUIPMENT PROGRAMS</td>
</tr>
<c:forEach var="promotion" items="${promotions}">
<tr>
 <td class="pri bld"><fmt:date fmt="d" date="${promotion.createdOn}" /></td>
 <td class="bld"><el:cmd url="profile" link="${promotion}">${promotion.firstName} ${promotion.lastName}</el:cmd></td>
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
<tr>
 <td class="pri bld"><fmt:date fmt="d" date="${promotion.createdOn}" /></td>
 <td class="bld"><el:cmd url="profile" link="${promotion}">${promotion.firstName} ${promotion.lastName}</el:cmd></td>
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
<tr>
 <td class="pri bld"><fmt:date fmt="d" date="${promotion.createdOn}" /></td>
 <td class="bld"><el:cmd url="profile" link="${promotion}">${promotion.firstName} ${promotion.lastName}</el:cmd></td>
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
<tr>
 <td class="pri bld"><fmt:date fmt="d" date="${promotion.createdOn}" /></td>
 <td class="bld"><el:cmd url="profile" link="${promotion}">${promotion.firstName} ${promotion.lastName}</el:cmd></td>
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
<tr>
 <td class="pri bld"><fmt:date fmt="d" date="${cert.createdOn}" /></td>
 <td class="bld"><el:cmd url="profile" link="${cert}">${cert.firstName} ${cert.lastName}</el:cmd></td>
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
