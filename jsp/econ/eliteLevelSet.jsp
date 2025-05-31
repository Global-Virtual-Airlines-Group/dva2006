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
<title>Update <content:airline />&nbsp;${eliteName} Status Levels for ${year}</title>
<content:css name="main" />
<content:css name="form" />
<content:googleAnalytics />
<content:js name="common" />
<content:js name="datePicker" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
<script async>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.submit(f);
	return true;
};
<c:if test="${!isRollover}">
golgotha.onDOMReady(function() { golgotha.util.disable(document.getElementById('isCommit')); });
</c:if>
</script>
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
<el:form action="elitelevelset.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline />&nbsp;${eliteName}&nbsp;${year} REQUIREMENTS</td>
</tr>
<tr>
 <td class="label">Statistics Start</td>
 <td class="data"><el:text name="startDate" required="true" idx="*" size="10" max="10" value="${fn:dateFmt(startDate, user.dateFormat)}" />&nbsp;<el:button label="CALENDAR" onClick="void show_calendar('forms[0].startDate')" />
&nbsp;<span class="small">All dates/times are ${user.TZ.name}. (Format: ${user.dateFormat})</span></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="calcPoints" value="true" checked="${param.calcPoints}" className="small ita" label="Calculate ${pointUnit} thresholds in addition to Legs and ${distUnit}" /></td>
</tr>
<c:if test='${!empty oldLevels}'>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box ID="isCommit" name="isCommit" value="true" label="Write Updated ${eliteName} qualification levels to Database" /><c:if test="${!isRollover}"><br /><span class="ita">(Requirements can only be updated during the ${eliteName} status rollover period.)</span></c:if></td>
</tr>
<tr class="title caps">
 <td colspan="2">QUALIFICATION CHANGES FROM ${year - 1} TO ${year}</td>
</tr>
<c:forEach var="lvlName" items="${oldLevels.keySet()}">
<c:set var="ol" value="${oldLevels[lvlName]}" scope="page" />
<c:set var="nl" value="${newLevels[lvlName]}" scope="page" />
<c:if test="${ol.legs > 0}">
<tr>
 <td class="label top">${lvlName}</td>
 <td class="data"><span class="pri bld">${ol.year}</span> - <fmt:int value="${ol.legs}" className="bld" /> flight legs, <fmt:int value="${ol.distance}" className="sec bld" />&nbsp;${distUnit}, <fmt:int value="${ol.points}" />&nbsp;${pointUnit}<br />
<span class="pri bld">${nl.year}</span> - <fmt:int value="${nl.legs}" className="bld" /> flight legs, <fmt:int value="${nl.distance}" className="sec bld" />&nbsp;${distUnit}, <fmt:int value="${nl.points}" />&nbsp;${pointUnit}<br />
Legs: <fmt:dec value="${(nl.legs - ol.legs) * 1.0 / ol.legs}" fmt="##0.0%" className="bld" forceSign="true" />, ${distUnit}&nbsp;<fmt:dec value="${(nl.distance - ol.distance) * 1.0 / ol.distance}" className="sec bld" fmt="##0.0%" forceSign="true" />, 
${pointUnit }&nbsp;<fmt:dec value="${(nl.points / ol.points) * 1.0 / ol.points}" className="ter bld" fmt="##0.0%" forceSign="true" /><hr />
Adjust Target Percentile: <el:text name="adjust-${lvlName}" idx="*" size="2" max="3" value="${nl.targetPercentile}" /><c:if test="${ol.targetPercentile != nl.targetPercentile}"> - <span class="ita">Originally ${ol.targetPercentile}</span></c:if></td>
</tr>
</c:if>
</c:forEach>
<c:if test="${statsAdjustFactor > 1}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data ita">Levels calculated from less than a full year of flight activity. A scaling factor of <fmt:dec value="${statsAdjustFactor}" className="pri bld" fmt="#0.000" /> has been applied to project across a full year.</td>
</tr>
</c:if>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="UPDATE ${year} QUALIFICATION LEVELS" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
