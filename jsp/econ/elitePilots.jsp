<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<content:sysdata var="eliteName" name="econ.elite.name" />
<html lang="en">
<head>
<title><content:airline />&nbsp;${eliteName} Pilots for ${year}</title>
<content:css name="main" />
<content:css name="form" />
<content:googleAnalytics />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
<script async>
golgotha.local.update = function() {
	const f = document.forms[0];
	self.location = '/elitepilots.do?year=' + golgotha.form.getCombo(f.year) + '&sortType=' + f.sortType.value;
	return true;
};

golgotha.local.setSort = function(st) {
	document.forms[0].sortType.value = st;
	return golgotha.local.update();
};
</script>
<style type="text/css">
<c:forEach var="lvl" items="${totals.keySet()}">
td.requal-${lvl.name} {
	background: ${fn:rgba(lvl.color, 0.325)};
}
</c:forEach>
</style>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="distUnit" name="econ.elite.distance" />
<content:sysdata var="pointUnit" name="econ.elite.points" />
<content:attr attr="isHROperations" roles="HR,Operations" value="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="elitepilots.do" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="6"><content:airline />&nbsp;${eliteName} MEMBERSHIP</td>
 <td colspan="2" class="right">YEAR <el:combo name="year" options='${years}' size="1" idx="*" value="${year}" onChange="void golgotha.local.update()" /></td>
</tr>
<tr>
 <td colspan="8" class="mid"><span class="pri bld">${eliteName}</span> is <content:airline/>'s Pilot loyalty program. It recognizes those Pilots who operate flights in the top percentiles of our Pilot population. Listed below are the Pilots who have demonstrated a sustained
 commitment to <content:airline /> through their participation in our flight operations.</td>
</tr>
<c:forEach var="lvl" items="${totals.keySet()}">
<c:set var="nl" value="${nyLevels[lvl.name]}" scope="page" />
<c:set var="lvlTotals" value="${totals[lvl]}" scope="page" />
<c:set var="ltLevel" value="${ltLevels[lvl]}" scope="page" />
<c:set var="idx" value="0" scope="page" />
<!-- ${lvl.name} -->
<tr class="title caps" style="background-color:#${lvl.hexColor};">
 <td colspan="8" ><span title="${lvl.legs} flights, ${lvl.distance} miles">${lvl.name}</span> - <fmt:int value="${lvlTotals.size()}"  /> PILOTS<c:if test="${lvlTotals.size() > 0}"><span id="elite-${lvl.name}-Toggle" class="toggle" onclick="void golgotha.util.toggleExpand(this, 'elite-${lvl.name}')">COLLAPSE</span></c:if></td>
</tr>
<tr class="mid title caps elite-${lvl.name}" style="background-color:#${lvl.hexColor};">
 <td>#</td>
 <td>PILOT NAME</td>
 <td>PILOT ID</td>
 <td>RANK</td>
 <td>EQUIPMENT</td>
 <td><a href="javascript:void golgotha.local.setSort(0)">${year} FLIGHTS</a></td>
 <td><a href="javascript:void golgotha.local.setSort(1)">${year}&nbsp;${distUnit}</a></td>
 <td><a href="javascript:void golgotha.local.setSort(2)">${year}&nbsp;${pointUnit}</a></td>
</tr>
<c:forEach var="yt" items="${lvlTotals}">
<c:set var="idx" value="${idx + 1}" scope="page" />
<c:set var="pilot" value="${pilots[yt.ID]}" scope="page" />
<c:set var="yd" value="${yt.delta(nl)}"  scope="page" />
<c:set var="isLT" value="${ltIDs.contains(yt.ID)}" scope="page" />
<tr class="mid elite-${lvl.name}">
<c:if test="${isHROperations}">
 <td><el:cmd url="eliteinfo" link="${pilot}" className="sec bld plain">${idx}</el:cmd></td>
</c:if>
<c:if test="${!isHROperations}">
 <td class="sec bld"><fmt:int value="${idx}" /></td>
</c:if>
 <td <c:if test="${isLT}"> class="requal-${lvl.name}" title="Qualifies for ${nl.name} status based on ${ltLevel.name} lifetime status"</c:if>><el:cmd url="profile" link="${pilot}" className="bld">${pilot.name}</el:cmd></td>
 <td class="bld">${pilot.pilotCode}</td>
 <td class="sec bld">${pilot.rank.name}</td>
 <td class="pri bld">${pilot.equipmentType}</td>
 <td <c:if test="${((nl.legs > 0) && (yd.legs < 1))}"> class="requal-${lvl.name}" title="Requalifies for ${nl.name} status in ${year + 1}"</c:if>><el:cmd url="logbook" link="${pilot}" className="bld"><fmt:int value="${yt.legs}" /></el:cmd></td>
 <td <c:if test="${((nl.distance > 0) && (yd.distance < 1))}"> class="requal-${lvl.name}" title="Requalifies for ${nl.name} status in ${year + 1}"</c:if>><fmt:int value="${yt.distance}" /></td>
 <td <c:if test="${(nl.points > 0) && (yd.points < 1)}"> class="requal-${lvl.name}" title="Requalifies for ${nl.name} status in ${year + 1}"</c:if>><fmt:int className="sec" value="${yt.points}" /></td>
</tr>
</c:forEach>
</c:forEach>
<!-- Bottom bar -->
<tr class="title"><td colspan="8">&nbsp;</td></tr>
</el:table>
<el:text type="hidden" name="sortType" value="${param.sortType}" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
