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
<title><content:airline /> Help Desk</title>
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
<el:table className="view" space="default" pad="default">
<tr class="title caps">
 <td class="left" colspan="6"><content:airline /> HELP DESK</td>
</tr>
<tr class="left">
 <td colspan="6">Welcome to the <content:airline /> Help Desk. This is designed as your single point of 
contact for questions and answers regarding our virtual airline community. We have collection a number 
of sources of information to allow you to discover more about our airline. If you still have questions, 
please feel free to <el:cmd url="hdissue" op="edit" className="sec bld">ask a new Question</el:cmd> and 
one of our volunteer staff will answer it soon.</td>
</tr>

<!-- Document Library -->
<tr>
 <td class="left" colspan="4">The <content:airline /> Document Library is a valuable resource for learning
 more about all aspects of our operations. All documents require Adobe Acrobat Reader 6.0 or above.</td>
 <td colspan="2"><el:cmdbutton url="doclibrary" label="DOCUMENT LIBRARY" /></td>
</tr>

<!-- Frequently Asked Questions -->
<tr>
 <td class="left" colspan="4">This is a collection of most frequently asked questions about
 <content:airline />.</td>
 <td colspan="2"><el:cmdbutton url="faq" label="FREQUENTLY ASKED QUESTIONS" /></td>
</tr>
<content:filter roles="HR,Instructor,Examiner,AcademyAdmin,PIREP,Examination,Signature">
<!-- All Issues -->
<tr>
 <td class="left" colspan="4">You can view all Issues in the <content:airline /> Help Desk.</td>
 <td colspan="2"><el:cmdbutton url="hdissues" label="All Issues" /></td>
</tr>
</content:filter>
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
 <td width="5%">#</td>
 <td>TITLE</td>
 <td width="10%">STATUS</td>
 <td width="30%">CREATED BY</td>
 <td width="15%">ASSIGNED TO</td>
 <td width="5%">COMMENTS</td>
</tr>
<c:forEach var="issue" items="${myIssues}">
<c:set var="author" value="${pilots[issue.authorID]}" scope="request" />
<c:set var="assignee" value="${pilots[issue.assignedTo]}" scope="request" />
<view:row entry="${issue}">
 <td class="sec bld"><fmt:int value="${issue.ID}" /></td>
 <td class="pri bld"><el:cmd url="hdissue" link="${issue}" className="pri bld">${issue.subject}</el:cmd></td>
 <td class="sec bld small">${issue.statusName}</td>
 <td><el:cmd url="profile" link="${author}" className="bld">${author.name}</el:cmd> on
 <fmt:date date="${issue.createdOn}" /></td>
 <td><el:cmd url="profile" link="${assignee}" className="sec bld">${assignee.name}</el:cmd></td>
 <td><fmt:int value="${issue.commentCount}" /></td>
</view:row>
</c:forEach>
</c:otherwise>
</c:choose>

<c:if test="${!empty activeIssues}">
<!-- All Active Issues -->
<tr class="title caps">
 <td colspan="6" class="left">ACTIVE ISSUES</td>
</tr>
<c:forEach var="issue" items="${activeIssues}">
<c:set var="author" value="${pilots[issue.authorID]}" scope="request" />
<c:set var="assignee" value="${pilots[issue.assignedTo]}" scope="request" />
<view:row entry="${issue}">
 <td class="sec bld"><fmt:int value="${issue.ID}" /></td>
 <td class="pri bld"><el:cmd url="hdissue" link="${issue}" className="pri bld">${issue.subject}</el:cmd></td>
 <td class="sec bld small">${issue.statusName}</td>
 <td><el:cmd url="profile" link="${author}" className="bld">${author.name}</el:cmd> on
 <fmt:date date="${issue.createdOn}" /></td>
 <td class="sec bld"><el:cmd url="profile" link="${assignee}" className="sec bld">${assignee.name}</el:cmd></td>
 <td><fmt:int value="${issue.commentCount}" /></td>
</view:row>
</c:forEach>
</c:if>

<c:if test="${(!empty myIssues) || (!empty activeIssues)}">
<!-- Legend Bar -->
<tr class="title">
 <td colspan="6"><view:legend width="95" labels="Open,Assigned,Resolved,FAQ Entry" classes=" ,opt2,opt1,opt3" /></td>
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
