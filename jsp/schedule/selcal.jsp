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
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="selcals">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="10%">SELCAL</td>
 <td width="10%">&nbsp;</td>
 <td width="10%">AIRCRAFT</td>
 <td width="15%">EQUIPMENT</td>
 <td width="10%">RESERVED ON</td>
 <td>RESERVED BY</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="sc" items="${viewContext.results}">
<c:set var="access" value="${accessMap[sc.code]}" scope="request" />
<c:set var="pilot" value="${pilots[sc.reservedBy]}" scope="request" />
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
 <td><fmt:date date="${sc.reservedOn}" /></td>
 <td>${pilot.rank} <el:cmd url="profile" linkID="0x${pilot.ID}">${pilot.name}</el:cmd></td>
</c:if>
<c:if test="${empty sc.reservedOn}">
 <td colspan="2" class="left sec bld caps">SELCAL CODE IS CURRENTLY AVAILABLE</td>
</c:if>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="6"><view:pgUp />&nbsp;<view:pgDn /></td>
</tr>
</view:table>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
