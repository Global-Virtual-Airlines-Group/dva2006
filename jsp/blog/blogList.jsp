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
<title><content:airline /> Journals</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
</head>
<content:copyright visible="false" />
<body onload="void initLinks()">
<content:page>
<%@ include file="/jsp/blog/header.jsp" %> 
<%@ include file="/jsp/blog/sideMenu.jsp" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" space="default" pad="default" cmd="blog">
<c:if test="${!showAll}">
<tr class="title caps">
 <td colspan="2">JOURNAL - ${author.name}</td>
</tr>
</c:if>
<c:forEach var="entry" items="${viewContext.results}">
<c:set var="author" value="${authors[entry.authorID]}" scope="request" />
<tr class="title caps" valign="top">
 <td width="65%">${entry.title} - <fmt:date fmt="d" date="${entry.date}" />
<c:if test="${showAll}"> <el:cmd url="blog" linkID="0x${author.ID}">${author.name}</el:cmd></c:if></td>
 <td><fmt:int value="${entry.size}" /> COMMENTS - <el:cmd url="blogentry" linkID="0x${entry.ID}">VIEW ENTRY</el:cmd></td>
</tr>
<tr>
 <td colspan="2" class="left"><fmt:msg value="${entry.body}" />
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="2"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
