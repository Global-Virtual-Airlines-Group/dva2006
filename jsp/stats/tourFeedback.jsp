<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Flight Tour Feedback</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:googleAnalytics />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
<script async>
golgotha.local.update = function(cb) {
	self.location = '/tourfb.do?id' = golgotha.form.comboGet(cb);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="tourfb.do" method="get" link="${tour}" validate="return false">
<view:table cmd="tourfb" className="form">
<tr class="title caps">
 <td colspan="2" class="left"><span class="nophone"><content:airline /> FLIGHT TOUR FEEDBACK - </span>${tour.name}</td>
 <td colspan="2" class="right"><span class="nophone">SELECT TOUR </span><el:combo name="id" idx="*" value="${tour}" options="${tours}" onChange="void golgotha.local.update(this)" /></td>
</tr>
<tr>
 <td class="label">Tour Name</td>
 <td class="data" colspan="3"><el:cmd url="tour" link="${tour}" className="sec bld">${tour.name}</el:cmd><span class="nophone"> <fmt:date date="${tour.startDate}" fmt="d" /> - <fmt:date date="${tour.endDate}" fmt="d" /></span></td>
</tr>
<tr>
 <td class="label">Score</td>
 <td class="data" colspan="3">Average Score: <fmt:dec value="${score.average}" className="pri bld" />, total Feedback: <fmt:int value="${score.size}" className="bld" /> entries</td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:10%">DATE</td>
 <td style="width:25%">AUTHOR</td>
 <td style="width:10%">SCORE</td>
 <td class="nophone">COMMENTS</td>
</tr>

<!-- Tabe Data -->
<c:forEach var="fb" items="${viewContext.results}">
<c:set var="author" value="${authors[fb.authorID]}" scope="page" />
<view:row entry="${fb}">
 <td><fmt:date date="${fb.createdOn}" t="HH:mm" /></td>
 <td class="bld">${author.name}<c:if test="${!empty author.pilotCode}"><span class="nophone"> (${author.pilotCode})</span></c:if></td>
 <td class="pri bld">${fb.score}</td>
 <td class="left small nophone"><fmt:msg value="${fb.comments}" /><c:if test="${empty fb.comments}">N/A</c:if></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="4"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar></td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
