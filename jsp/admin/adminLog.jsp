<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Staff Activity Log</title>
<content:css name="main" />
<content:css name="view" />
<content:googleAnalytics />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:newRelic>
<content:cspHeader />
</content:newRelic>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="adminlog">
<tr class="title caps">
 <td class="left" colspan="5">ACTIVITY LOG STATISTICS - <fmt:int value="${days}" /> DAYS</td>
</tr>
<c:forEach var="authorID" items="${stats.keySet()}">
<c:set var="author" value="${authors[authorID]}" scope="page" />
 <td colspan="2" class="pri bld"><el:cmd url="pilot" link="${author}">${author.name}</el:cmd></td>
 <td colspan="3" class="sec bld right"><fmt:int value="${stats[authorID]}" /> actions</td>
</c:forEach>

<!-- Table Header Bar -->
<tr class="title caps">
 <td class="left" colspan="5">ACTIVITY LOG ENTRIES</td>
</tr>

<!-- Table Legend Bar -->
<tr class="title caps">
 <td style="width:10%">DATE</td>
 <td style="width:15%">TYPE</td>
 <td style="width:15%">ID</td>
 <td>AUTHOR</td>
 <td class="nophone" style="width:40%">FROM</td>
</tr>

<!-- Table Audit Data -->
<c:forEach var="log" items="${viewContext.results}">
<c:set var="author" value="${authors[log.authorID]}" scope="page" />
<c:set var="ipInfo" value="${ip[log.remoteAddr]}" scope="page" />
<tr>
 <td><fmt:date date="${log.date}" t="HH:mm" /></td>
 <td class="sec">${log.auditType}</td>
 <td class="pri bld">${log.auditID}</td>
 <td>${author.name}</td>
 <td class="small nophone"><fmt:ipaddr addr="${log}" info="${ipInfo}" showFlag="true" /></td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="5"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
