<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> NOTAMs</title>
<content:sysdata var="airlineName" name="airline.name" />
<content:css name="main" />
<content:css name="view" />
<content:googleAnalytics />
<content:js name="common" />
<content:rss title="${airlineName} NOTAMs" path="/notams_rss.ws" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="notams">
<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:5%">ID</td>
 <td style="width:15%">DATE</td>
 <td>SUBJECT</td>
</tr>

<!-- Table NOTAM Data -->
<c:forEach var="notam" items="${viewContext.results}">
<c:set var="ac" value="${accessMap[notam.ID]}" scope="page" />
<c:set var="author" value="${authors[notam.authorID]}" scope="page" />
<view:row entry="${notam}">
 <td class="priB"><fmt:int value="${notam.ID}" /></td>
 <td class="bld"><fmt:date fmt="d" date="${notam.date}" /></td>
<c:if test="${ac.canEdit}">
 <td><el:cmd url="notamedit" link="${notam}"><fmt:text value="${notam.subject}" /></el:cmd></td>
</c:if>
<c:if test="${!ac.canEdit}"> 
 <td><fmt:text value="${notam.subject}" /></td>
</c:if>
</view:row>
<view:row entry="${notam}">
<c:if test="${notam.isHTML}">
 <td colspan="3" class="left notam"><c:if test="${notam.hasImage}"><div class="hdr"><el:dbimg img="${notam}" style="width:${news.bannerWidth}%; max-width=${news.bannerWidth}%" caption="${notam.subject}"  /></div></c:if>
${notam.body}</td>
</c:if>
<c:if test="${!notam.isHTML}">
 <td colspan="3" class="left notam"><fmt:msg value="${notam.body}" bbCode="true" /></td>
</c:if>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="3">
<c:if test="${access.canCreateNOTAM}"><el:cmd url="notamedit">NEW NOTAM</el:cmd>&nbsp;|&nbsp;</c:if>
<el:cmd url="notams" op="all">ALL NOTAMs</el:cmd>&nbsp;|<view:scrollbar>&nbsp;<view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;|&nbsp;<el:cmd url="notams">ACTIVE NOTAMs</el:cmd></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
