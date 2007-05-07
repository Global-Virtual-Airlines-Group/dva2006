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
<title><content:airline /> SELCAL Codes</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function sortBy(combo)
{
self.location = 'selcals.do?sortType=' + combo.options[combo.selectedIndex].value;
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="selcals.do" method="get" validate="return false">
<view:table className="view" pad="default" space="default" cmd="selcals">
<tr class="title">
 <td class="left caps" colspan="5"><content:airline /> SELCAL CODES</td>
 <td class="right" colspan="2">SORT BY <el:combo name="sortType" idx="*" size="1" options="${sortOptions}" value="${param.sortType}" onChange="void sortBy(this)" /></td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td width="8%">SELCAL</td>
 <td width="10%">&nbsp;</td>
 <td width="10%">AIRCRAFT</td>
 <td width="10%">EQUIPMENT</td>
 <td width="10%">RESERVED ON</td>
 <td width="10%">RELEASING ON</td>
 <td>RESERVED BY</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="sc" items="${viewContext.results}">
<c:set var="access" value="${accessMap[sc.code]}" scope="request" />
<c:set var="pilot" value="${pilots[sc.reservedBy]}" scope="request" />
<c:set var="releaseDate" value="${releaseDates[sc.code]}" scope="request" />
<view:row entry="${se}">
 <td class="pri bld">${sc.code}</td>
<c:choose>
<c:when test="${access.canReserve}">
 <td><el:cmdbutton url="selcal" op="reserve" linkID="${sc.code}" label="RESERVE" /></td>
</c:when>
<c:when test="${access.canRelease}">
 <td><el:cmdbutton url="selcal" op="free" linkID="${sc.code}" label="RELEASE" /></td>
</c:when>
<c:otherwise>
 <td>&nbsp;</td>
</c:otherwise>
</c:choose>
 <td>${sc.aircraftCode}</td>
 <td class="sec bld">${sc.equipmentType}</td>
<c:if test="${!empty sc.reservedOn}">
 <td><fmt:date fmt="d" date="${sc.reservedOn}" /></td>
 <td><fmt:date fmt="d" date="${releaseDate}" /></td>
 <td>${pilot.rank} <el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
</c:if>
<c:if test="${empty sc.reservedOn}">
 <td colspan="3" class="left ter bld caps">SELCAL CODE IS CURRENTLY AVAILABLE</td>
</c:if>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
