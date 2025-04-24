<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> HTTP Rate Limiter</title>
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
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="ratelimit">

<!-- View Header Bar -->
<tr class="title caps">
 <td colspan="4" class="left">HTTP RATE LIMITER STATUS</td>
</tr>
<tr class="title caps">
 <td>ADDRESS</td>
 <td style="width:20%">REQUESTS</td>
 <td style="width:15%">OLDEST</td>
 <td style="width:15%">NEWEST</td>
</tr>
 
<!-- View Data -->
<c:forEach var="rc" items="${viewContext.results}">
<c:set var="ip" value="${rc.IPInfo}" scope="page" />
<c:set var="interval" value="${rc.newest.epochSecond - rc.oldest.epochSecond + 1}" scope="page" />
<view:row entry="${rc}">
 <td><span class="bld">${rc.address}</span><c:if test="${!empty ip}">&nbsp;<span class="small">(${ip.address}/${ip.bits} - ${ip.location})</span></c:if></td>
 <td><span class="pri bld"><fmt:int value="${rc.requests}" /></span><c:if test="${rc.requests > 1}"> (<fmt:dec value="${rc.requests * 60.0 / interval}" /> reqs/min)</c:if></td>
 <td><fmt:date date="${rc.oldest}" /></td>
 <td><fmt:date date="${rc.newest}" /></td>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="4">&nbsp;<view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="110" labels="Blocked,Degraded,None" classes="error,opt1, " /> </td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
