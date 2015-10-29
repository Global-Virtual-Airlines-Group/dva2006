<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Airline News</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<meta name="viewport" content="width=device-width, initiall-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="news">
<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:15%">DATE</td>
 <td style="width:65%">SUBJECT</td>
 <td style="width:20%">AUTHOR</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="news" items="${viewContext.results}">
<c:set var="ac" value="${accessMap[news.ID]}" scope="page" />
<tr>
 <td class="priB"><fmt:date fmt="d" date="${news.date}" /></td>
<c:if test="${ac.canEdit}">
 <td class="pri bld"><el:cmd url="newsedit" link="${news}">${news.subject}</el:cmd></td>
</c:if>
<c:if test="${!ac.canEdit}">
 <td class="pri bld">${news.subject}</td>
</c:if>
 <td class="secB">${news.authorName}</td>
</tr>
<tr>
 <td colspan="3" class="left"><fmt:text value="${news.body}" /></td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="3">
<c:if test="${access.canCreateNews}">
<el:cmd url="newsedit">NEW SYSTEM NEWS ENTRY</el:cmd>&nbsp;|&nbsp;
</c:if>
<view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
