<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ACARS Dispatcher Routes</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" space="default" pad="default" cmd="dsproutes">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="5%">ID</td>
 <td width="15%">DEPARTING FROM</td>
 <td width="15%">ARRIVING AT</td>
 <td width="10%">CREATED ON</td>
 <td width="20%">CREATED BY</td>
 <td width="5%">USED</td>
 <td class="left">WAYPOINTS</td>
</tr>

<!-- Routes -->
<c:forEach var="route" items="${viewContext.results}">
<c:set var="author" value="${authors[entry.authorID]}" scope="request" />
<c:set var="authorLoc" value="${userData[entry.authorID]}" scope="request" />
<tr>
 <td><el:cmd url="dsproute" linkID="${route}" className="pri bld"><fmt:int value="${route.ID}" /></el:cmd></td>
 <td class="small">${route.airportD.name} (<fmt:airport airport="${route.airportD}" />)</td>
 <td class="small">${route.airportD.name} (<fmt:airport airport="${route.airportA}" />)</td>
 <td><fmt:date date="${route.createdOn}" fmt="d" /></td>
 <td><el:profile location="${authorLoc}" className="pri bld">${author.name}</el:profile></td>
 <td class="bld"><fmt:int value="${route.useCount}" /></td>
 <td class="left small">${route.route}</td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="9"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
