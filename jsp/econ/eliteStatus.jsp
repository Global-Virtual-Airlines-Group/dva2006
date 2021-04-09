<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="eliteName" name="econ.elite.name" />
<html lang="en">
<head>
<title><content:airline />&nbsp;${eliteName} Status - ${pilot.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<style type="text/css">
table.form td.eliteStatus {
	color: #ffffff;
	background-color: #${currentStatus.level.hexColor};
}
span.prgbar {
	display: inline-block;
	color: #ffffff;
	font-weight: bold;
	text-align: center;
	text-overflow: clip;
	overflown: hidden;
	white-space: nowrap;
}
span.fatbar {
	padding-top: 5px;
	padding-bottom: 5px;
}
span.nrbar {
	padding-top: 1px;
	padding-bottom: 1px;
}
span.rmbar {
	opacity: .225;
}
</style>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:attr attr="showAll" value="true" roles="HR,Operations" />
<content:sysdata var="eliteDistance" name="econ.elite.distance" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">

<!-- Pilot Information -->
<tr class="title caps">
 <td class="eliteStatus" colspan="2" ><content:airline />&nbsp;${eliteName} PROGRAM - ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></td>
</tr>
<c:set var="currentLevels" value="${levels[currentYear]}" scope="page" />
<c:set var="ct" value="${totals[currentYear]}" scope="page" />
<tr>
 <td class="label eliteStatus top">Current Status</td>
 <td class="data">${eliteName}&nbsp;<fmt:elite level="${currentStatus.level}" className="bld" /> (<span class="ita">${currentStatus.level.year}</span>)<br />
 <hr />
 ${currentYear} totals - <fmt:int value="${ct.legs}" className="pri bld" /> flight legs, <span class="sec bld" ><fmt:int value="${ct.distance}" />&nbsp;${eliteDistance}</span></td>
</tr>
<tr>
 <td class="label eliteStatus">Flight Progress</td>
 <td class="data">
 <c:set var="hasPrevLevel" value="true" scope="page" />
 <c:set var="prevLevel" value="${baseLevel}" scope="page" />
 <c:forEach var="lvl" items="${currentLevels}">
<c:set var="hasLevel" value="${ct.matches(lvl)}" scope="page" />
 <c:if test="${hasLevel || showAll || lvl.isVisible}">
<c:set var="lr" value="${hasPrevLevel ? Math.max(0, lvl.legs - ct.legs) : lvl.legs}" scope="page" />
<fmt:eliteProgressBar width="15" percent="true" className="prgbar fatbar" remainingClassName="prgbar rmbar fatbar" progress="${lvl.legs - prevLevel.legs - lr}" level="${lvl}" prev="${prevLevel}" units="l" showUnits="true" /></c:if>
<c:set var="hasPrevLevel" value="${hasLevel}" scope="page" />
<c:set var="prevLevel" value="${lvl}" scope="page" /></c:forEach></td>
</tr>
<tr>
 <td class="label eliteStatus">Mileage Progress</td>
 <td class="data">
 <c:set var="hasPrevLevel" value="true" scope="page" />
 <c:set var="prevLevel" value="${baseLevel}" scope="page" />
 <c:forEach var="lvl" items="${currentLevels}">
 <c:set var="hasLevel" value="${ct.matches(lvl)}" scope="page" />
 <c:if test="${hasLevel || showAll || lvl.isVisible}">
<c:set var="dr" value="${hasPrevLevel ? Math.max(0, lvl.distance - ct.distance) : lvl.distance}" scope="page" />
<fmt:eliteProgressBar width="15" percent="true" className="prgbar fatbar" remainingClassName="prgbar rmbar fatbar" progress="${lvl.distance - prevLevel.distance - dr}" level="${lvl}" prev="${prevLevel}" units="d" showUnits="true" /></c:if>
<c:set var="hasPrevLevel" value="${hasLevel}" scope="page" />
<c:set var="prevLevel" value="${lvl}" scope="page" /></c:forEach></td>
</tr>
<tr class="title caps">
 <td colspan="2" class="eliteStatus">${eliteName} STATUS HISTORY</td>
</tr>
<c:forEach var="yr" items="${totals.keySet()}">
<c:set var="total" value="${totals[yr]}" scope="page" />
<c:set var="upds" value="${statusUpdates[yr]}" scope="page" />
<c:set var="lvls" value="${levels[yr]}" scope="page" />
<c:set var="yearMax" value="${maxStatus[yr].level}" scope="page" />
<c:if test="${(yr ne currentYear) && (!empty upds)}">
<tr>
 <td class="label top" style="background-color:#${yearMax.hexColor};" title="Year-end ${yr} status: ${yearMax.name}">${yr} Progress</td>
 <td class="data">${yr} totals - <fmt:int value="${total.legs}" className="pri bld" /> flight legs, <span class="sec bld"><fmt:int value="${total.distance}" />&nbsp;${eliteDistance}</span><br />
<br />
<c:forEach var="upd" items="${upds}" varStatus="updStatus">
<c:set var="action" value="${(upd.upgradeReason == 'ROLLOVER') ? 'Rolled over' : 'Earned'}" scope="page" />
<fmt:date date="${upd.effectiveOn}" fmt="d" />&nbsp;${action}&nbsp;<fmt:elite className="bld" level="${upd.level}" nameOnly="true" /> for ${upd.level.year} ${ypd.upgradeReason}<c:if test="${!updStatus.isLast()}"><br /></c:if></c:forEach>
<hr />
<c:set var="hasPrevLevel" value="true" scope="page" />
<c:set var="prevLevel" value="${baseLevel}" scope="page" />
<c:forEach var="lvl" items="${lvls}">
<c:set var="hasLevel" value="${total.matches(lvl)}" scope="page" />
<c:if test="${hasLevel || (hasPrevLevel && (showAll || lvl.isVisible))}">
<c:set var="lr" value="${hasPrevLevel ? Math.max(0, lvl.legs - total.legs) : lvl.legs}" scope="page" />
<fmt:eliteProgressBar width="15" percent="true" className="prgbar nrbar" remainingClassName="prgbar rmbar nrbar" progress="${lvl.legs - prevLevel.legs - lr}" level="${lvl}" prev="${prevLevel}"  units="l" showUnits="false" /></c:if>
<c:set var="hasPrevLevel" value="${hasLevel}" scope="page" />
<c:set var="prevLevel" value="${lvl}" scope="page" /></c:forEach><br />
<div style="height:4px;"></div>
<c:set var="hasPrevLevel" value="true" scope="page" />
<c:set var="prevLevel" value="${baseLevel}" scope="page" />
 <c:forEach var="lvl" items="${lvls}">
<c:set var="hasLevel" value="${total.matches(lvl)}" scope="page" />
<c:if test="${hasLevel || (hasPrevLevel && (showAll || lvl.isVisible))}">
<c:set var="dr" value="${hasPrevLevel ? Math.max(0, lvl.distance - total.distance) : lvl.distance}" scope="page" />
<fmt:eliteProgressBar width="15" percent="true" className="prgbar nrbar" remainingClassName="prgbar rmbar nrbar" progress="${lvl.distance - prevLevel.distance - dr}" level="${lvl}" prev="${prevLevel}"  units="d" showUnits="true" /></c:if>
<c:set var="hasPrevLevel" value="${hasLevel}" scope="page" />
<c:set var="prevLevel" value="${lvl}" scope="page" /></c:forEach></td>
</tr>
</c:if>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title"><td class="eliteStatus" colspan="2">&nbsp;</td></tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
