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
<title><content:airline /> Journals</title>
<content:sysdata var="airlineName" name="airline.name" />
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:js name="common" />
<content:rss title="${airlineName} Journals" path="/blog_rss.ws" />
<content:pics />
</head>
<content:copyright visible="false" />
<body onload="void initLinks()">
<content:page>
<%@ include file="/jsp/blog/header.jspf" %> 
<%@ include file="/jsp/blog/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" space="default" pad="default" cmd="blog">
<c:if test="${!showAll}">
<c:set var="author" value="${authors[authorID]}" scope="page" />
<tr class="title caps">
 <td colspan="2">JOURNAL - ${author.name}</td>
</tr>
</c:if>
<c:forEach var="entry" items="${viewContext.results}">
<c:set var="author" value="${authors[entry.authorID]}" scope="page" />
<tr class="title caps" valign="top">
 <td width="65%">${entry.title} - <fmt:date fmt="d" date="${entry.date}" />
<c:if test="${showAll}"> <el:cmd url="blog" link="${author}">${author.name}</el:cmd></c:if></td>
 <td><fmt:int value="${entry.size}" /> COMMENTS - <el:cmd url="blogentry" link="${entry}">VIEW ENTRY</el:cmd></td>
</tr>
<tr>
 <td colspan="2" class="left"><fmt:msg value="${entry.body}" /></td>
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
<content:googleAnalytics />
</body>
</html>
