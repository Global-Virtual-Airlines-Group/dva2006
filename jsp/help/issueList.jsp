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
<title><content:airline /> Help Desk Issues</title>
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
<%@ include file="/jsp/help/header.jspf" %> 
<%@ include file="/jsp/help/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="${cmdName}">
<tr class="title">
 <td colspan="6" class="left caps"><content:airline /> HELP DESK</td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td>#</td>
 <td style="width:30%">SUBJECT</td>
 <td class="nophone" style="width:12%">STARTED BY</td>
 <td class="nophone" style="width:12%">ASSIGNED TO</td>
 <td class="nophone" style="width:5%">COMMENTS</td>
 <td class="left">LAST COMMENT BY</td>
</tr>

<!-- Table View data -->
<c:forEach var="issue" items="${viewContext.results}">
<c:set var="author" value="${pilots[issue.authorID]}" scope="page" />
<c:set var="cAuthor" value="${pilots[issue.lastCommentAuthorID]}" scope="page" />
<c:set var="assignedTo" value="${pilots[issue.assignedTo]}" scope="page" />
<view:row entry="${issue}">
 <td class="sec bld"><fmt:int value="${issue.ID}" /></td>
 <td><el:cmd url="hdissue" link="${issue}"><fmt:text value="${issue.subject}" /></el:cmd></td>
 <td class="nophone"><el:cmd url="profile" link="${author}" className="pri bld">${author.name}</el:cmd></td>
 <td class="nophone"><el:cmd url="profile" link="${assignedTo}" className="bld">${assignedTo.name}</el:cmd></td>
<c:choose>
<c:when test="${!empty cAuthor}">
 <td class="nophone"><fmt:int value="${issue.commentCount}" /></td>
 <td class="right"><fmt:date date="${issue.lastComment}" /> by <span class="pri bld">${cAuthor.name}</span></td>
</c:when>
<c:otherwise>
 <td colspan="2" class="small right">No Comments have been made regarding this Issue</td>
</c:otherwise>
</c:choose>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn />&nbsp;</view:scrollbar>
<view:legend width="95" labels="Open,Assigned,Resolved,FAQ Entry" classes=" ,opt2,opt1,opt3" /></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
