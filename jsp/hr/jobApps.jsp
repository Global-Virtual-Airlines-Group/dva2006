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
<title><content:airline /> Staff Applications</title>
<content:css name="main" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="jobapplist">
<tr class="title">
 <td class="caps left" colspan="7"><content:airline /> VOLUNTEER STAFF APPLICATIONS</td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td style="width:20%">APPLICANT</td>
 <td style="width:10%">CREATED</td>
 <td style="width:20%">JOB TITLE</td>
 <td style="width:10%">STATUS</td>
 <td style="width:10%">CLOSES</td>
 <td style="width:10%">APPLICANTS</td>
 <td>HIRING MANAGER</td>
</tr>

<!-- Table Job Application Data -->
<c:forEach var="jobapp" items="${viewContext.results}">
<c:set var="job" value="${jobs[jobapp.ID]}" scope="page" />
<c:set var="hireMgr" value="${hireMgrs[job.hireManagerID]}" scope="page" />
<view:row entry="${jobapp}">
 <td><el:cmd url="profile" className="bld" linkID="${jobapp.authorID}">${jobapp.firstName} ${jobapp.lastName}</el:cmd></td>
 <td><fmt:date date="${jobapp.createdOn}" fmt="d" /></td>
 <td><el:cmd url="job" link="${job}" className="sec bld">${job.title}</el:cmd></td>
 <td class="pri bld">${job.statusName}</td>
 <td class="bld"><fmt:date date="${job.closesOn}" fmt="d" /></td>
 <td class="sec bld"><fmt:int value="${job.appCount}" /></td>
 <td>${hireMgr.name}</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="110" labels="Submitted,Shortlisted,Approved" classes=" ,opt1,opt3" /></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
