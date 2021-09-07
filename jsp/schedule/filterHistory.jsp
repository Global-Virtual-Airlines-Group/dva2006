<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Schedule Filter History</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="schedhistory">
<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:20%">DATE</td>
 <td style="width:12%">SOURCE</td>
 <td style="width:10%">EFFECTIVE DATE</td>
 <td style="width:5%">LEGS</td>
 <td style="width:5%" class="nophone">SKIPPED</td>
 <td style="width:5%" class="nophone">ADJUSTED</td>
 <td style="width:10%" class="nophone">USER</td>
 <td class="left nophone">AIRLINES</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="ssh" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[ssh.authorID]}" scope="page" />
<view:row entry="${ssh}">
 <td class="pri bld"><fmt:date date="${ssh.date}" t="HH:mm:ss" /> - <fmt:date date="${ssh.endDate}" t="HH:mm:ss" fmt="t" /></td>
 <td class="small sec bld">${ssh.source.description}</td>
 <td class="bld"><fmt:date date="${ssh.effectiveDate}" fmt="d" /></td>
 <td class="sec bld"><fmt:int value="${ssh.legs}" /></td>
 <td class="small nophone"><fmt:int value="${ssh.skipped}" /></td>
 <td class="pri nophone"><fmt:int value="${ssh.adjusted}" /></td>
<c:if test="${ssh.autoImport}">
 <td class="small bld nophone">AUTOMATIC</td>
</c:if>
<c:if test="${!ssh.autoImport}">
 <td class="small pri bld nophone caps">${pilot.name}</td>
</c:if> 
 <td class="left small nophone"><fmt:list value="${ssh.airlines}" delim=", " /></td>
</view:row>
</c:forEach>

<!-- Scroll bar -->
<tr class="title">
 <td colspan="8"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
