<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Oceanic Routes</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="routes">
<!-- Table Header Bar -->
<tr class="title caps">
 <td>DATE</td>
 <td style="width:15%">&nbsp;</td>
 <td>TYPE</td>
 <td class="nophone">DESCRIPTION</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="route" items="${viewContext.results}">
<tr>
 <td class="pri bld"><fmt:date fmt="d" date="${route.date}" /></td>
 <td><el:cmdbutton url="route" op="${route.type}" linkID="${fn:dateFmt(route.date, 'MMddyyyy')}" label="VIEW ROUTE" /></td>
 <td class="sec bld">${route.type}</td>
 <td class="left nophone">${route.type} routes for <fmt:date fmt="d" date="${route.date}" />, via ${route.source}</td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="4"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
