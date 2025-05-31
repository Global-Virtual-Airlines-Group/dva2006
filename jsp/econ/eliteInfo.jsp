<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" buffer="24kb" autoFlush="true" %>
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
<content:attr attr="isOps" value="true" roles="HR,Operations" />
<content:googleAnalytics />
<content:js name="common" />
<content:cspHeader />
<script async>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check() || ${!isOps}) return false;	
	golgotha.form.submit(f);
	return true;
};
</script>
<style type="text/css">
table.form td.eliteStatus, .button {
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
<content:sysdata var="eliteDistance" name="econ.elite.distance" />
<content:sysdata var="elitePoint" name="econ.elite.points" />
<content:sysdata var="eliteTDName" name="econ.elite.totalProgram" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="eliterecalc.do" link="${pilot}" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">

<!-- Pilot Information -->
<tr class="title caps">
 <td class="eliteStatus" colspan="2" ><content:airline />&nbsp;${eliteName} PROGRAM - <el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd><c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></td>
</tr>
<c:set var="currentLevels" value="${levels[currentYear]}" scope="page" />
<c:set var="ct" value="${totals[currentYear]}" scope="page" />
<tr>
 <td class="label eliteStatus top">Current Status</td>
 <td class="data">${eliteName}&nbsp;<fmt:elite level="${currentStatus.level}" className="bld" /> (<span class="ita">${currentStatus.level.year}</span>)
 <c:if test="${currentStatus.isLifetime}"> - Based on lifetime <fmt:ltelite level="${currentLTStatus.lifetimeStatus}" className="bld" /> status obtained on <fmt:date date="${currentLTStatus.effectiveOn}" fmt="d" /></c:if><br />
 <hr />
 ${currentYear} totals - <fmt:int value="${ct.legs}" className="pri bld" /> flight legs, <span class="sec bld"><fmt:int value="${ct.distance}" />&nbsp;${eliteDistance}</span>, <span class="bld"><fmt:int value="${ct.points}" />&nbsp;${elitePoints}</span>
 <c:if test="${pending.legs > 0}"><br />Pending ${currentYear} flights - <fmt:int value="${pending.legs}" className="pri bld" /> flight legs, <span class="sec bld"><fmt:int value="${pending.distance}" />&nbsp;${eliteDistance}</span></c:if>
 <c:if test="${((ro.legs > 0) || (ro.distance > 0))}">
 <br />
 Rolled over from <span class="pri bld">${currentYear - 1}</span>: <c:if test="${ro.legs > 0}"><fmt:int value="${ro.legs}" className="bld" /> flight legs<c:if test="${ro.distance > 0}">, </c:if></c:if>
<c:if test="${ro.distance > 0}"><span class="ter bld"><fmt:int value="${ro.distance}" />&nbsp;${eliteDistance}</span></c:if></c:if></td>
</tr>
<c:if test="${!empty eliteTDName && !totalMileage.isZero()}">
<tr>
 <td class="label eliteStatus">${eliteTDName} Progress</td>
 <td class="data"><fmt:int value="${totalMileage.legs}" className="pri bld" /> Legs, <fmt:int value="${totalMileage.distance}" className="sec bld" />&nbsp;${eliteDistance}
<c:if test="${!empty currentLTStatus && !currentStatus.isLifetime}"> - <span class="bld" style="color:#${currentLTStatus.hexColor}">${currentLTStatus.lifetimeStatus.name}</span> obtained on <fmt:date date="${currentLTStatus.effectiveOn}" fmt="d" /></c:if></td>
</tr>
</c:if>
<c:if test="${isRollover && (!empty ny)}">
<tr>
 <td class="label eliteStatus">${currentYear + 1} Flight Progress</td>
 <td class="data"><fmt:int value="${ny.legs}" className="pri bld" /> flight legs, <span class="sec bld"><fmt:int value="${ny.distance}" />&nbsp;${eliteDistance}</span>, <span class="bld"><fmt:int value="${ny.points}" />&nbsp;${elitePoints}</span> -
<span class="small ita">These totals will be used towards your ${eliteName}&nbsp;${currentYear + 2} requalification progress.</span></td>
</tr>
</c:if>
<tr class="title caps">
 <td class="eliteStatus" colspan="2">${eliteName}&nbsp;${currentYear + 1} REQUALIFICATION PROGRESS</td>
</tr>
<tr>
 <td colspan="2" class="mid">Status in the <content:airline />&nbsp;<span class="pri bld">${eliteName}</span> program is based on your flight activity during the previous year, and requires requalification each year. You can qualify for a given level
 based on the number of flight legs flown, or the total flight distance. If you reach the threshold for a level, you immediately jump to that level and can maintain it throughout the year it was achieved as well as the following status year.</td>  
</tr>
<tr>
 <td class="label eliteStatus top">${currentYear + 1} Status</td>
<c:if test="${isRollover}">
 <td class="data">The ${currentYear}&nbsp;${eliteName} status year has completed and no additional incomplete flights will be credited towards your ${currentYear + 1} status. Your ${eliteName} status will be <fmt:elite level="${nextYearStatus.level}" className="bld" nameOnly="true" />
<c:if test="${nextYearStatus.isLifetime}"> <span class="small ita">(based on your <fmt:ltelite level="${currentLTStatus.lifetimeStatus}" className="bld" /> status)</span></c:if>.</td></c:if>
<c:if test="${!isRollover}">
 <td class="data">If you do not complete any more flights this year, your ${eliteName} status will be <fmt:elite level="${nextYearStatus.level}" className="bld" nameOnly="true" />.<c:if test="${!empty projectedTotal}"> If you continue flying at your current rate, you will accumulate 
 <fmt:int value="${projectedTotal.legs}" /> flight legs and <fmt:int value="${projectedTotal.distance}" />&nbsp;${eliteDistance}, for <fmt:elite level="${projectedStatus.level}" className="bld" nameOnly="true" /> status.
<c:if test="${projectedStatus.isLifetime}"> <span class="small ita">(based on your <fmt:ltelite level="${currentLTStatus.lifetimeStatus}" className="bld" /> status)</span>)</c:if></c:if>
 <c:if test="${(legDelta < 0.1) || (distDelta < 0.1)}">
 <br /><br />
<span class="pri bld">REQUALIFICATION ALERT</span> - You are approaching the requirements for <fmt:elite level="${nextLevel}" className="bld" nameOnly="true" /> status in ${currentYear + 1}. Just a few more flights could qualify you for a higher
 level until the end of next year. It may be a good time for a mileage run!</c:if></td></c:if>
</tr>
<tr>
 <td class="label eliteStatus"><c:if test="${isRollover}">${currentYear}&nbsp;</c:if>Flight Progress</td>
 <td class="data">
 <c:set var="hasPrevLevel" value="true" scope="page" />
 <c:set var="prevLevel" value="${baseLevel}" scope="page" />
 <c:set var="upds" value="${statusUpdates[currentYear]}" scope="page" />
 <c:forEach var="lvl" items="${currentLevels}">
<c:set var="hasLevel" value="${ct.matches(lvl)}" scope="page" />
 <c:if test="${hasLevel || isOps || lvl.isVisible}">
<c:set var="lr" value="${hasPrevLevel ? Math.max(0, lvl.legs - ct.legs) : lvl.legs}" scope="page" />
<fmt:eliteProgressBar width="19" percent="true" className="prgbar fatbar" remainingClassName="prgbar rmbar fatbar" progress="${lvl.legs - prevLevel.legs - lr}" level="${lvl}" prev="${prevLevel}" units="l" showUnits="true" /></c:if>
<c:set var="hasPrevLevel" value="${hasLevel}" scope="page" />
<c:set var="prevLevel" value="${lvl}" scope="page" /></c:forEach></td>
</tr>
<tr>
 <td class="label eliteStatus"><c:if test="${isRollover}">${currentYear}&nbsp;</c:if>Mileage Progress</td>
 <td class="data">
 <c:set var="hasPrevLevel" value="true" scope="page" />
 <c:set var="prevLevel" value="${baseLevel}" scope="page" />
 <c:forEach var="lvl" items="${currentLevels}">
 <c:set var="hasLevel" value="${ct.matches(lvl)}" scope="page" />
 <c:if test="${hasLevel || isOps || lvl.isVisible}">
<c:set var="dr" value="${hasPrevLevel ? Math.max(0, lvl.distance - ct.distance) : lvl.distance}" scope="page" />
<fmt:eliteProgressBar width="19" percent="true" className="prgbar fatbar" remainingClassName="prgbar rmbar fatbar" progress="${lvl.distance - prevLevel.distance - dr}" level="${lvl}" prev="${prevLevel}" units="d" showUnits="true" /></c:if>
<c:set var="hasPrevLevel" value="${hasLevel}" scope="page" />
<c:set var="prevLevel" value="${lvl}" scope="page" /></c:forEach></td>
</tr>
<tr>
 <td class="label top eliteStatus">${currentYear} Results</td>
 <td class="data"><c:forEach var="upd" items="${upds}" varStatus="updStatus">
<fmt:date date="${upd.effectiveOn}" fmt="d"  className="bld" />&nbsp;
<c:choose>
<c:when test="${upd.isLifetime}">Achieved <fmt:ltelite className="bld" level="${upd.lifetimeStatus}" /> for lifetime <fmt:elite className="bld" level="${upd.level}" nameOnly="true" /> status (Qualified via ${upd.upgradeReason.description})</c:when>
<c:when test="${upd.upgradeReason == 'ROLLOVER'}">Rolled over <fmt:elite className="bld" level="${upd.level}" nameOnly="true" /> achieved in ${upd.level.year - 1} for ${upd.level.year}</c:when>
<c:when test="${upd.upgradeReason == 'DOWNGRADE'}">Downgraded to <fmt:elite className="bld" level="${upd.level}" nameOnly="true" /> based on ${upd.level.year -1} mileage achievement</c:when>
<c:when test="${upd.upgradeReason == 'NONE'}">Initial ${eliteName} credit</c:when>
<c:otherwise>Earned <fmt:elite className="bld" level="${upd.level}" nameOnly="true" /> for ${upd.level.year} (Qualified via ${upd.upgradeReason.description})</c:otherwise>
</c:choose>
<c:if test="${!updStatus.isLast()}"><br /></c:if></c:forEach></td>
</tr>
<tr class="title caps">
 <td colspan="2" class="eliteStatus">${eliteName} STATUS HISTORY</td>
</tr>
<c:forEach var="yr" items="${totals.keySet()}">
<c:set var="total" value="${totals[yr]}" scope="page" /><c:set var="upds" value="${statusUpdates[yr]}" scope="page" />
<c:set var="lvls" value="${levels[yr]}" scope="page" />
<c:set var="yearMax" value="${maxStatus[yr].level}" scope="page" />
<c:if test="${(yr ne currentYear) && (!empty upds)}">
<tr>
 <td class="label top" style="background-color:#${yearMax.hexColor};" title="Year-end ${yr} status: ${yearMax.name}">${yr} Results</td>
 <td class="data">${yr} totals - <fmt:int value="${total.legs}" className="pri bld" /> flight legs, <span class="sec bld"><fmt:int value="${total.distance}" />&nbsp;${eliteDistance}</span>, <span class="bld"><fmt:int value="${total.points}" />&nbsp;${elitePoints}</span><br />
<br />
<c:forEach var="upd" items="${upds}" varStatus="updStatus">
<fmt:date date="${upd.effectiveOn}" fmt="d"  className="bld" />&nbsp;
<c:choose>
<c:when test="${upd.isLifetime}">Achieved <fmt:ltelite className="bld" level="${upd.lifetimeStatus}" /> for lifetime <fmt:elite className="bld" level="${upd.level}" nameOnly="true" /> status (Qualified via ${upd.upgradeReason.description})</c:when>
<c:when test="${upd.upgradeReason == 'ROLLOVER'}">Rolled over <fmt:elite className="bld" level="${upd.level}" nameOnly="true" /> achieved in ${upd.level.year - 1} for ${upd.level.year}</c:when>
<c:when test="${upd.upgradeReason == 'DOWNGRADE'}">Downgraded to <fmt:elite className="bld" level="${upd.level}" nameOnly="true" /> based on ${upd.level.year -1} mileage achievement</c:when>
<c:when test="${upd.upgradeReason == 'NONE'}">Initial ${eliteName} credit</c:when>
<c:otherwise>Earned <fmt:elite className="bld" level="${upd.level}" nameOnly="true" /> for ${upd.level.year} (Qualified via ${upd.upgradeReason.description})</c:otherwise>
</c:choose>
<c:if test="${!updStatus.isLast()}"><br /></c:if></c:forEach>
<hr />
<c:set var="hasPrevLevel" value="true" scope="page" />
<c:set var="prevLevel" value="${baseLevel}" scope="page" />
<c:forEach var="lvl" items="${lvls}">
<c:set var="hasLevel" value="${total.matches(lvl)}" scope="page" />
<c:if test="${hasLevel || (hasPrevLevel && (isOps || lvl.isVisible))}">
<c:set var="lr" value="${hasPrevLevel ? Math.max(0, lvl.legs - total.legs) : lvl.legs}" scope="page" />
<fmt:eliteProgressBar width="19" percent="true" className="prgbar nrbar" remainingClassName="prgbar rmbar nrbar" progress="${lvl.legs - prevLevel.legs - lr}" level="${lvl}" prev="${prevLevel}"  units="l" showUnits="true" /></c:if>
<c:set var="hasPrevLevel" value="${hasLevel}" scope="page" />
<c:set var="prevLevel" value="${lvl}" scope="page" /></c:forEach><br />
<div style="height:4px;"></div>
<c:set var="hasPrevLevel" value="true" scope="page" />
<c:set var="prevLevel" value="${baseLevel}" scope="page" />
 <c:forEach var="lvl" items="${lvls}">
<c:set var="hasLevel" value="${total.matches(lvl)}" scope="page" />
<c:if test="${hasLevel || (hasPrevLevel && (isOps || lvl.isVisible))}">
<c:set var="dr" value="${hasPrevLevel ? Math.max(0, lvl.distance - total.distance) : lvl.distance}" scope="page" />
<fmt:eliteProgressBar width="19" percent="true" className="prgbar nrbar" remainingClassName="prgbar rmbar nrbar" progress="${lvl.distance - prevLevel.distance - dr}" level="${lvl}" prev="${prevLevel}"  units="d" showUnits="true" /></c:if>
<c:set var="hasPrevLevel" value="${hasLevel}" scope="page" />
<c:set var="prevLevel" value="${lvl}" scope="page" /></c:forEach></td>
</tr>
</c:if>
</c:forEach>
<c:if test="${isOps}">
<tr class="title caps">
 <td colspan="2" class="eliteStatus">RECALCULATE <span class="nophone">${pilot.name}&nbsp;${eliteName} STATUS</span> FOR ${currentYear}</td>
</tr>
<tr>
 <td class="eliteStatus label">&nbsp;</td>
 <td class="data"><el:box name="saveChanges" label="Persist changes to database" value="true" /></td>
</tr>
</c:if>

<!-- Bottom Bar -->
<tr class="title mid"><td class="eliteStatus" colspan="2"><el:cmdbutton url="logbook" link="${pilot}" label="VIEW LOGBOOK" /><c:if test="${isOps}">&nbsp;<el:button type="submit" label="RECALCULATE ${eliteName} STATUS" /></c:if></td></tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
