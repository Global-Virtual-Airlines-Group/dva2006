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
<content:sysdata var="eliteDistance" name="econ.elite.distance" />

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${isUpdate}">
<div class="updateHdr">${eliteName} Level Updated</div>
<br />
The <span class="pri bld">${eliteName}</span>&nbsp;<span style="color:${lvl.hexColor}" class="bld">${lvl.name}</span> status level definition has been updated in the database. This will not make any changes to existing status levels.<br />
</c:when>
<c:when test="${isRecalc}">
<div class="updateHdr">${eliteName} Status Recalculated</div>
<br />
<span class="pri bld">${eliteName}</span> status for ${pilot.name} has been recalculated.<br />
<br />
<fmt:int value="${total.legs}" className="pri bld" /> Flight Legs were re-scored, and ${pilot.name} has flown <fmt:distance value="${total.distance}" /> in ${total.year}.<br />
<br />
<c:forEach var="msg" items="${msgs}">
${msg}<br /></c:forEach>
<br />
</c:when>
<c:when test="${isLevelSet}">
<div class="updateHdr">${eliteName} Levels Calculated</div>
<br />
<span class="pri bld">${eliteName}</span>&nbsp; status levels for ${year} have been calculated based on existing percentiles and updated in the database. The levels for ${year} are as follows:<br />
<br />
<c:forEach var="lvl" items="${newLevels}">
<fmt:elite level="${lvl}" className="bld" nameOnly="true" /> - <fmt:int value="${lvl.legs}" /> flight legs, <fmt:int value="${lvl.distance}" /> ${eliteDistance}<br />
</c:forEach>
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
