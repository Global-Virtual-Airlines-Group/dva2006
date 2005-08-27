<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Profile Updated</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<div class="updateHdr"><content:airline /> Pilot Profile Updated</div>
<br />
The Pilot Profile for ${pilot.rank} ${pilot.name} has been successfully updated.<br />
<br />
<ul>
<c:if test="${ratingsUpdated}">
<!-- Updated Equipment Ratings -->
<li>Equipment Ratings have been updated. ${pilot.firstName} is now rated to fly the <span class="small">
<fmt:list value="${pilot.ratings}" delim="," /></span>.</li>
</c:if>
<c:if test="${pwdUpdated}">
<li>The web site/ACARS password for ${pilot.name} has been updated.</li>
</c:if>
<c:if test="${statusUpdated}">
<!-- Updated Pilot Status -->
<li>Pilot Status has been updated. ${pilot.firstName}'s status is now <span class="pri bld">${pilot.statusName}</span>.</li>
</c:if>
<c:if test="${rankUpdated && !isPromotion}">
<!-- Updated Pilot Equipment Type and Rank -->
<li>${pilot.firstName} has been transfered to the ${pilot.equipmentType} program as a <b>${pilot.rank}</b>.</li>
</c:if>
<c:if test="${isPromotion}">
<!-- Updated Pilot Rank -->
<li>${pilot.firstName} has been promoted, and is now a <b>${pilot.rank}</b> in the ${pilot.equipmentType} program.</li>
</c:if>
<c:if test="${pwdUpdate}">
<!-- Updated Password -->
<li>${pilot.name}'s password has been successfully updated.</li>
</c:if>
<c:if test="${spUpdated}">
<!-- Updated Staff Profile -->
<li>The Staff Profile for ${staff.title} ${pilot.name} has been updated.</li>
</c:if>
<c:if test="${spRemoved}">
<!-- Removed Staff Profile -->
<li>The Staff Profile for ${pilot.name} has been removed from the Staff Roster.</li>
</c:if>
<c:if test="${sigUpdated}">
<!-- Updated Signature Image -->
<li>The Water Cooler signature image for ${pilot.name} has been removed.</li>
</c:if>
<c:if test="${sigRemoved}">
<!-- Removed Signature Image -->
<li>The Water Cooler signature image for ${pilot.name} has been updated. It is displayed below:<br />
<img alt="${pilot.name} (${pilot.pilotCode})" src="/sig/0x<fmt:hex value="${pilot.ID}" />" /></li>
</c:if>
</ul>
<br />
To view this Pilot Profile, <el:cmd url="profile" linkID="0x${pilot.ID}" op="read">click here</el:cmd>.<br />
To return to the Pilot Roster, <el:cmd url="roster">click here</el:cmd><br />
<br />
<content:copyright />
</div>
</body>
</html>
