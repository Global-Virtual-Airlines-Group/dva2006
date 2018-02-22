<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="forumName" name="airline.forum" />
<html lang="en">
<head>
<title><content:airline /> ${forumName}</title>
<content:css name="main" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/cooler/header.jspf" %> 
<%@ include file="/jsp/cooler/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="staff">
<tr class="title caps">
 <td class="left" colspan="5">${forumName} CHANNELS</td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:35%">CHANNEL NAME<span class="nophone"> / DESCRIPTION</span></td>
 <td>THREADS</td>
 <td class="nophone">POSTS</td>
 <td class="nophone">VIEWS</td>
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
 <td class="nophone"><fmt:int value="${channel.postCount}" /></td>
 <td class="nophone"><fmt:int value="${channel.viewCount}" /></td>
<c:set var="post" value="${posts[channel.lastThreadID]}" scope="page" />
<c:set var="author" value="${authors[post.authorID]}" scope="page" />
 <td class="right"><span class="small nophone">${author.rank.name} </span><span class="pri bld small">${author.name}</span>
 <span class="small nophone"><c:if test="${!empty author.pilotCode}">(${author.pilotCode}) </c:if>at
 <fmt:date date="${post.createdOn}" /></span><br />
 in <el:cmd url="thread" linkID="${post.threadID}" className="bld">${post.subject}</el:cmd></td>
</c:if>
<c:if test="${channel.lastThreadID == 0}">
 <td colspan="4" class="right ita">No Topics have been posted in this Channel</td>
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
<content:googleAnalytics />
</body>
</html>
