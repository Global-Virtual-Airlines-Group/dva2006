<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Help Desk</title>
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
<el:table className="view">
<tr class="title caps">
 <td class="left" colspan="2"><span class="nophone"><content:airline />&nbsp;</span>HELP DESK</td>
</tr>
<tr>
 <td colspan="2">Welcome to the <content:airline /> Help Desk. This is designed as your single point of contact for questions and answers regarding our virtual airline community. We have collection a number 
of sources of information to allow you to discover more about our airline.<br />
If you still have questions, please feel free to <el:cmd url="hdissue" op="edit" className="sec bld">ask a new Question</el:cmd> and one of our volunteer staff will answer it soon.</td>
</tr>
<tr>
<td style="min-width:156px;"><el:cmd url="doclibrary" className="pri bld">Document Library</el:cmd></td>
 <td class="left">The <content:airline /> Document Library is a valuable resource for learning more about all aspects of our operations. All documents require Adobe Acrobat Reader.</td>
</tr>
<tr>
<td><el:cmd url="faq" className="pri bld">Frequently Asked Questions</el:cmd></td>
 <td class="left" >This is a collection of most frequently asked questions about <content:airline />.</td>
</tr>
<tr>
 <td><el:cmd url="hdsearch" className="pri bld">Help Desk Search</el:cmd></td>
 <td class="left">Have a question or a problem? Chances are you're not the first person to need help. You can search the Help Desk to see if other people have had the same Issue, and how it was resolved.</td>
</tr>
<content:filter roles="HR">
<tr>
 <td><el:cmd url="rsptemplates" className="pri bld">Response Templates</el:cmd></td>
 <td class="left">You can create and modify response templates to easily answer some of the more commonly asked Help Desk questions.</td>
</tr>
</content:filter>
</el:table>

<el:table className="view">
<!-- My Issues -->
<tr class="title caps">
 <td class="left" colspan="6">MY ISSUES</td>
</tr>
<c:choose>
<c:when test="${empty myIssues}">
<tr>
 <td colspan="6"><span class="pri bld left">You do not currently have any Help Desk Issues.</span>
<c:if test="${access.canCreate}"> <el:cmd url="hdissue" op="edit" className="sec bld">Click Here</el:cmd>
 to create a new Issue.</c:if></td>
</tr>
</c:when>
<c:otherwise>
<tr class="title">
 <td style="width:5%">#</td>
 <td>TITLE</td>
 <td style="width:10%">STATUS</td>
 <td class="nophone" style="width:30%">CREATED BY</td>
 <td style="width:15%">ASSIGNED TO</td>
 <td style="width:5%">COMMENTS</td>
</tr>
<c:forEach var="issue" items="${myIssues}">
<c:set var="author" value="${pilots[issue.authorID]}" scope="page" />
<c:set var="assignee" value="${pilots[issue.assignedTo]}" scope="page" />
<view:row entry="${issue}">
 <td class="sec bld"><fmt:int value="${issue.ID}" /></td>
 <td class="pri bld"><el:cmd url="hdissue" link="${issue}" className="pri bld">${issue.subject}</el:cmd></td>
 <td class="sec bld small">${issue.statusName}</td>
 <td class="nophone"><el:cmd url="profile" link="${author}" className="bld">${author.name}</el:cmd><span class="nophone"> on <fmt:date date="${issue.createdOn}" t="HH:mm"/></span></td>
 <td><el:cmd url="profile" link="${assignee}" className="sec bld">${assignee.name}</el:cmd></td>
 <td><fmt:int value="${issue.commentCount}" /></td>
</view:row>
</c:forEach>
</c:otherwise>
</c:choose>

<c:if test="${!empty viewContext.results}">
<!-- All Active Issues -->
<tr class="title caps">
 <td colspan="6" class="left">ACTIVE ISSUES</td>
</tr>
<c:forEach var="issue" items="${viewContext.results}">
<c:set var="author" value="${pilots[issue.authorID]}" scope="page" />
<c:set var="assignee" value="${pilots[issue.assignedTo]}" scope="page" />
<view:row entry="${issue}">
 <td class="sec bld"><fmt:int value="${issue.ID}" /></td>
 <td class="pri bld"><el:cmd url="hdissue" link="${issue}" className="pri bld">${issue.subject}</el:cmd></td>
 <td class="sec bld small">${issue.statusName}</td>
 <td class="nophone"><el:cmd url="profile" link="${author}" className="bld">${author.name}</el:cmd><span class="nophone"> on <fmt:date date="${issue.createdOn}" t="HH:mm" /></span></td>
 <td class="sec bld"><el:cmd url="profile" link="${assignee}" className="sec bld">${assignee.name}</el:cmd></td>
 <td><fmt:int value="${issue.commentCount}" /></td>
</view:row>
</c:forEach>
</c:if>

<c:if test="${(!empty myIssues) || (!empty viewContext.results)}">
<!-- Legend Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="92" labels="Open,Assigned,Resolved,FAQ Entry" classes=" ,opt2,opt1,opt3" /></td>
</tr>
</c:if>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
