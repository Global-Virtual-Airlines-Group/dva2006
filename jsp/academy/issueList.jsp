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
<title><content:airline /> Flight Academy Help Desk</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" space="default" pad="default" cmd="academyissues">
<tr class="title">
 <td colspan="4" class="left caps"><content:airline /> FLIGHT ACADEMY HELP DESK</td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td width="50%">SUBJECT</td>
 <td width="15%">STARTED BY</td>
 <td width="5%">COMMENTS</td>
 <td class="left"><c:if test="${access.canCreate}"><el:cmd url="academyissue" op="edit">NEW ISSUE</el:cmd> | </c:if>LAST COMMENT BY</td>
</tr>

<!-- Table View data -->
<c:forEach var="issue" items="${viewContext.results}">
<c:set var="author" value="${pilots[issue.authorID]}" scope="request" />
<c:set var="cAuthor" value="${pilots[issue.lastCommentAuthorID]}" scope="request" />
<view:row entry="${issue}">
 <td><el:cmd url="academyissue" linkID="0x${issue.ID}">${issue.subject}</el:cmd></td>
 <td><el:cmd url="profile" linkID="0x${author.ID}" className="pri bld">${author.name}</el:cmd></td>
 <td><fmt:int value="${issue.commentCount}" /></td>
<c:choose>
<c:when test="${!empty cAuthor}">
 <td class="right"><fmt:date date="${issue.lastComment}" /> by <span class="pri bld">${cAuthor.name}</span></td>
</c:when>
<c:otherwise>
 <td class="small right">No Comments have been made regarding this Issue</td>
</c:otherwise>
</c:choose>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title caps">
 <td colspan="4"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn />&nbsp;</view:scrollbar>
<view:legend width="70" labels="Public,Private" classes="opt1, " /></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
