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
<title><content:airline /> Airline News</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="news">
<!-- Table Header Bar-->
<tr class="title caps">
 <td width="15%">DATE</td>
 <td width="65%">SUBJECT</td>
 <td width="20%">AUTHOR</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="news" items="${viewContext.results}">
<tr>
 <td class="priB"><fmt:date fmt="d" date="${news.date}" /></td>
 <td class="pri bld"><el:cmd url="newsedit" link="${news}">${news.subject}</el:cmd></td>
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
