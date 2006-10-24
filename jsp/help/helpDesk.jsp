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
<tr class="title caps left">
 <td colspan="x"><content:airline /> HELP DESK</td>
</tr>
<tr class="left">
 <td colspan="x">Welcome to the <content:airline /> Help Desk. This is designed as your single point of 
contact for questions and answers regarding our virtual airline community. We have collection a number 
of sources of information to allow you to discover more about our airline. If you still have questions, 
please feel free to <el:cmd url="hdissue" op="edit" className="sec bld">ask a new Question</el:cmd> and 
one of our volunteer staff will answer it soon.</td>
</tr>

<!-- Document Library -->
<tr>
 <td class="left" colspan="x">The <content:airline /> Document Library is a valuable resource for learning
 more about all aspects of our operations. All documents require Adobe Acrobat Reader 6.0 or above.</td>
 <td colspan="y"><el:cmdbutton url="doclibrary" label="DOCUMENT LIBRARY" /></td>
</tr>

<!-- Frequently Asked Questions -->
<tr>
 <td class="left" colspan="x">This is a collection of most frequently asked questions about
 <content:airline />.</td>
 <td colspan="y"><el:cmdbutton url="faq" label="FREQUENTLY ASKED QUESTIONS" /></td>
</tr>

<!-- My Issues -->
<tr class="title left caps">
 <td colspan="x">MY ISSUES</td>
</tr>
<c:choose>
<c:when test="${!empty myIssues}">
<tr>
 <td colspan="x"><span class="pri bld left">You do not currently have any Help Desk Issues.</span>
 <el:cmd url="hdissue" op="edit" className="sec bld">Click Here</el:cmd> to create a new Issue.</td>
</tr>
</c:when>
<c:otherwise>
<tr class="title">
 <td width="5%">#</td>
 <td width="30%">TITLE</td>
 <td width="10%">STATUS</td>
</tr>
<c:forEach var="issue" items="${myIssues}">
<view:row entry="${issue}">


</view:row>
</c:forEach>
</c:otherwise>
</c:choose>

<c:if test="${!emtpy activeIssues}">
<!-- All Active Issues -->
<tr class="title left caps">
 <td colspan="x">ACTIVE ISSUES</td>
</tr>


<c:forEach var="issue" items="${activeIssues}">
<view:row entry="${issue}">



</view:row>
</c:forEach>
</c:if>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
