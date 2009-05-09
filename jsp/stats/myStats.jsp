<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Statistics - ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:js name="swfobject" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
setSubmit();
disableButton('SearchButton');
return true;
}

function update()
{
document.forms[0].submit();
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="swfPath" name="path.swf" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="mystats.do" method="post" validate="return validate(this)">
<!-- All Flight Report statistics -->
<view:table className="view" pad="default" space="default" cmd="mystats">
<tr class="title">
 <td colspan="4" class="left caps"><content:airline /> FLIGHT STATISTICS FOR ${pilot.name}</td>
 <td colspan="6" class="right">GROUP BY <el:combo name="groupType" size="1" idx="*" options="${groupTypes}" value="${param.groupType}" onChange="void update()" />
 SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void update()" /></td>
</tr>
<%@ include file="/jsp/stats/pirepStats.jspf" %>
</view:table>

<!-- Touchdown Speed statistics -->
<el:table className="form" space="default" pad="default">
<tr class="title">
 <td colspan="6" class="left caps">TOUCHDOWN SPEED STATISTICS - <fmt:int value="${pilot.ACARSLegs}" /> LANDINGS</td>
</tr>
<c:forEach var="vs" items="${fn:keys(landingStats)}">
<c:set var="vsCount" value="${landingStats[vs]}" scope="page" />
<c:choose>
<c:when test="${vs < -600}"><c:set var="barColor" value="red" scope="page" /></c:when>
<c:when test="${vs < -300}"><c:set var="barColor" value="orange" scope="page" /></c:when>
<c:when test="${vs < -50}"><c:set var="barColor" value="green" scope="page" /></c:when>
<c:otherwise><c:set var="barColor" value="blue" scope="page" /></c:otherwise>
</c:choose>
<tr>
 <td class="label"><fmt:int value="${vs}" /> ft/min</td>
 <td class="data" colspan="5"><span style="float:left; width:80px;"><fmt:int value="${vsCount}" /> landings</span><c:if test="${vsCount > 0}">
 <el:img y="11" x="${(vsCount * 550) / maxCount}" src="cooler/bar_${barColor}.png" caption="${vsCount} Landings" /></c:if></td>
</tr>
</c:forEach>

<!-- Table Header Bar-->
<tr class="title mid caps">
 <td>#</td>
 <td width="25%">EQUIPMENT</td>
 <td width="10%">FLIGHTS</td>
 <td width="10%">HOURS</td>
 <td width="15%">AVERAGE SPEED</td>
 <td>STD. DEVIATION</td>
</tr>

<!-- Touchdown Speed Analysis -->
<c:set var="entryNumber" value="0" scope="page" />
<c:forEach var="entry" items="${eqLandingStats}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
<tr class="mid">
 <td class="sec bld"><fmt:int value="${entryNumber}" /></td>
 <td class="pri bld">${entry.equipmentType}</td>
 <td><fmt:int value="${entry.legs}" /></td>
 <td><fmt:dec value="${entry.hours}" /></td>
 <td class="pri bld"><fmt:dec value="${entry.averageSpeed}" fmt="#0.00" /> ft/min</td>
 <td class="sec"><fmt:dec value="${entry.stdDeviation}" fmt="#0.00" /> ft/min</td>
</tr>
</c:forEach>

<!-- Flash Graph -->
<tr>
 <td class="label" valign="top">Pie Chart</td>
 <td class="data" colspan="5"><div id="flashcontent"><span class="bld">You need to upgrade your Flash Player.</span></div></td>
</tr>

<!-- Button Bar -->
<tr class="title">
 <td colspan="6">&nbsp;</td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
var so = new SWFObject('/${swfPath}/ampie.swf', 'piechart', '100%', 500, '8', '#ffffff', 'high');
so.addVariable('preloader_color', '#999999');
so.addVariable('path', '/');
so.addVariable('chart_id', 'piechart');
so.addVariable('data_file', escape('/mystats.ws?id=${pilot.hexID}'));
so.write('flashcontent');
</script>
<content:googleAnalytics />
</body>
</html>
