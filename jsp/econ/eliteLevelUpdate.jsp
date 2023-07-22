<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="eliteName" name="econ.elite.name" />
<html lang="en">
<head>
<title><content:airline />&nbsp;${eliteName} Updated</title>
<content:css name="main" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:sysdata var="distUnit" name="econ.elite.distance" />
<content:sysdata var="pointUnit" name="econ.elite.points" />

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${isUpdate}">
<div class="updateHdr">${eliteName} Level Updated</div>
<br />
The <span class="pri bld">${eliteName}</span>&nbsp;<fmt:elite level="${lvl}" className="bld" nameOnly="true" /> status level definition has been updated in the database. This will not make any changes to existing ${eliteName} status levels.<br />
</c:when>
<c:when test="${isRecalc}">
<div class="updateHdr">${eliteName} Status Recalculated</div>
<br />
<span class="pri bld">${eliteName}</span> status for ${pilot.name} has been recalculated.<br />
<br />
<fmt:int value="${total.legs}" className="pri bld" /> Flight Legs were re-scored, and ${pilot.name} has flown <fmt:int value="${total.distance}" />&nbsp;${distUnit} in ${total.year}.<br />
<br />
<c:forEach var="msg" items="${msgs}">
${msg}<br /></c:forEach>
<br />
</c:when>
<c:when test="${isLevelSet}">
<div class="updateHdr">${eliteName} Requirements Calculated</div>
<br />
The <content:airline />&nbsp;<span class="pri bld">${eliteName}</span> status requirements for <span class="bld">${year}</span> have been calculated based on existing percentiles and updated in the database. The levels for ${year} are as follows:<br />
<br />
<c:forEach var="lvlName" items="${oldLevels.keySet()}">
<c:set var="ol" value="${oldLevels[lvlName]}" scope="page" />
<c:set var="nl" value="${newLevels[lvlName]}" scope="page" />
<fmt:elite level="${lvl}" className="bld" nameOnly="true" /><br />
<br />
<span class="pri bld">${ol.year}</span> - <fmt:int value="${ol.legs}" className="bld" /> flight legs, <fmt:int value="${ol.distance}" className="sec bld" />&nbsp;${distUnit}, <fmt:int value="${ol.points}" />&nbsp;${pointUnit}<br />
<span class="pri bld">${nl.year}</span> - <fmt:int value="${nl.legs}" className="bld" /> flight legs, <fmt:int value="${nl.distance}" className="sec bld" />&nbsp;${distUnit}, <fmt:int value="${nl.points}" />&nbsp;${pointUnit}<br />
Legs: <fmt:dec value="${(nl.legs - ol.legs) * 1.0 / ol.legs}" fmt="##0.0%" className="bld" forceSign="true" />, ${distUnit}&nbsp;<fmt:dec value="${(nl.distance - ol.distance) * 1.0 / ol.distance}" className="sec bld" fmt="##0.0%" forceSign="true" />, 
${pointUnit }&nbsp;<fmt:dec value="${(nl.points / ol.points) * 1.0 / ol.points}" className="ter bld" fmt="##0.0%" forceSign="true" /><hr />
<br />
</c:forEach>
</c:when>
<c:when test="${isRollover}">
<div class="updateHdr">${eliteName} Status Rollover for ${year}</div>
<br />
<content:airline />&nbsp;${eliteName} stauts has been rolled over for the ${year} program year:<br />
<br />
<c:forEach var="msg" items="${msgs}">
${msg}<br /></c:forEach>
<br />
</c:when>
</c:choose>
<br />
To return to the list of ${eliteName} status levels, <el:cmd url="elitelevels" className="sec bld">Click Here</el:cmd>.<br />
<c:if test="${!empty pilot}">To return to the Pilot profile for ${pilot.name}, <el:cmd url="profile" link="${pilot}" className="sec bld">Click here</el:cmd>.<br /></c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
