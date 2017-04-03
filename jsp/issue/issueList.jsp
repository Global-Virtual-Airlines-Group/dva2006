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
<title><content:airline /> Development Issue Tracker</title>
<content:css name="main" />
<content:css name="view" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.doSort = function() { return document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="issues.do" method="post" validate="return true">
<view:table cmd="issues">
<!-- Table Sort Combo Bar -->
<tr class="title">
 <td colspan="2" class="left caps"><content:airline /> DEVELOPMENT ISSUE LIST</td>
 <td colspan="7">STATUS <el:combo name="op" idx="*" size="1" options="${statusOpts}" firstEntry="[ ALL ]" value="${param.op}" onChange="void golgotha.local.doSort()" />
<span class="nophone"> AREA <el:combo name="area" idx="*" size="1" options="${areaOpts}" firstEntry="[ ALL ]" value="${param.area}" onChange="void golgotha.local.doSort()" />
 SORT BY <el:combo name="sortType" idx="*" size="1" options="${sortTypes}" value="${viewContext.sortType}" onChange="void golgotha.local.doSort()" />
&nbsp;&nbsp;<el:cmd url="isearch">SEARCH</el:cmd></span>
<c:if test="${access.canCreate}"> | <el:cmd url="issue" op="edit">NEW ISSUE</el:cmd></c:if></td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td style="width:4%">ID</td>
 <td style="width:30%">TITLE</td>
 <td style="width:8%">PRIORITY</td>
 <td class="nophone" style="width:8%">AREA</td>
 <td class="nophone" style="width:8%">TYPE</td>
 <td style="width:8%">COMMENTS</td>
 <td class="nophone" style="width:9%">CREATED</td>
 <td style="width:10%">LAST COMMENT</td>
 <td class="nophone" >RESOLVED</td>
</tr>

<!-- Table Issue Data -->
<c:forEach var="issue" items="${viewContext.results}">
<view:row entry="${issue}">
 <td class="sec bld"><fmt:int value="${issue.ID}" /></td>
 <td class="small"><el:cmd url="issue" link="${issue}"><fmt:text value="${issue.subject}" /></el:cmd></td>
 <td class="pri bld small">${issue.priorityName}</td>
 <td class="bld small nophone">${issue.areaName}</td>
 <td class="sec bld small nophone">${issue.typeName}</td>
 <td><fmt:int value="${issue.commentCount}" /></td>
 <td class="nophone"><fmt:date fmt="d" date="${issue.createdOn}" /></td>
 <td class="sec"><fmt:date fmt="d" date="${issue.lastCommentOn}" default="-" /></td>
 <td class="bld nophone"><fmt:date fmt="d" date="${issue.resolvedOn}" default="-" /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="9"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn />&nbsp;</view:scrollbar>
<view:legend width="120" labels="Open,Fixed,Worked Around,Won't Fix,Deferred,Duplicate" classes="opt1, ,opt2,warn,err,opt3" /></td>
</tr>
</view:table>
</el:form>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
