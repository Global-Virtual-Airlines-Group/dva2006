<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> SELCAL Codes</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script>
golgotha.local.sortBy = function(combo) {
	self.location = '/selcals.do?sortType=' + escape(golgotha.form.getCombo(combo));
	return true;
};

golgotha.local.filterBy = function(combo) {
	if (combo.selectedIndex > 0) self.location = '/selcals.do?eqType=' + escape(golgotha.form.getCombo(combo));
	return true;
};
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
<view:table cmd="selcals">
<tr class="title">
 <td class="left caps" colspan="4"><content:airline /> SELCAL CODES</td>
 <td class="right" colspan="3">AIRCRAFT <el:combo name="eqType" idx="*" size="1" options="${eqTypes}" firstEntry="-" value="${param.eqType}" onChange="void golgotha.local.filterBy(this)" />
 SORT BY <el:combo name="sortType" idx="*" size="1" options="${sortOptions}" value="${param.sortType}" onChange="void golgotha.local.sortBy(this)" /></td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:8%">SELCAL</td>
 <td style="width:10%">&nbsp;</td>
 <td style="width:10%">AIRCRAFT</td>
 <td style="width:10%">EQUIPMENT</td>
 <td style="width:10%">RESERVED ON</td>
 <td style="width:10%">RELEASING ON</td>
 <td>RESERVED BY</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="sc" items="${viewContext.results}">
<c:set var="access" value="${accessMap[sc.code]}" scope="page" />
<c:set var="pilot" value="${pilots[sc.reservedBy]}" scope="page" />
<c:set var="releaseDate" value="${releaseDates[sc.code]}" scope="page" />
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
 <td>${pilot.rank.name} <el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
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
<content:googleAnalytics />
</body>
</html>
