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
<title><content:airline /> Water Cooler</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/cooler/header.jspf" %> 
<%@include file="/jsp/cooler/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="staff">
<tr class="title caps">
 <td class="left" colspan="5">WATER COOLER CHANNELS</td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td width="35%">CHANNEL NAME / DESCRIPTION</td>
 <td width="8%">THREADS</td>
 <td width="8%">POSTS</td>
 <td width="8%">VIEWS</td>
 <td>LAST POST INFORMATION</td>
</tr>

<!-- Table Channel Data -->
<c:forEach var="channel" items="${channels}">
<tr>
 <td class="left">
<c:if test="${channel.lastThreadID == 0}">
 <b>${channel.name}</b><br />
</c:if>
<c:if test="${channel.lastThreadID != 0}">
 <el:cmd url="channel" linkID="${channel.name}" className="bld">${channel.name}</el:cmd><br />
</c:if>
 <span class="small">${channel.description}</span></td>
<c:if test="${channel.lastThreadID != 0}">
 <td class="bld"><fmt:int value="${channel.threadCount}" /></td>
 <td><fmt:int value="${channel.postCount}" /></td>
 <td><fmt:int value="${channel.viewCount}" /></td>
<c:set var="post" value="${posts[channel.lastThreadID]}" scope="request" />
<c:set var="author" value="${authors[post.authorID]}" scope="request" />
 <td class="right"><span class="small">${author.rank}</span> <span class="pri bld small">${author.name}</span>
 <span class="small"><c:if test="${!empty author.pilotCode}">(${author.pilotCode}) </c:if>at
 <fmt:date date="${post.createdOn}" /></span><br />
 in <el:cmd url="thread" linkID="${post.threadID}" className="bld">${post.subject}</el:cmd></td>
</c:if>
<c:if test="${channel.lastThreadID == 0}">
 <td colspan="4" class="right"><i>No Topics have been posted in this Channel</i></td>
</c:if>
</tr>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="5">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
