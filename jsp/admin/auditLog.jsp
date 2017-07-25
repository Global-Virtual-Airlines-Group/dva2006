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
<title><content:airline /> Data Change Log</title>
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
<view:table cmd="changelog">
<!-- Table Header Bar -->
<tr class="title caps">
 <td colspan="6">CHANGE LOG ENTRIES</td>
</tr>

<!-- Table Legend Bar -->
<tr class="title caps">
 <td style="width:10%">DATE</td>
 <td style="width:10%">TYPE</td>
 <td style="width:10%">ID</td>
 <td style="width:15%">AUTHOR</td>
 <td style="width:25%" class="nophone">FROM</td>
 <td class="right">DESCRIPTION</td>
</tr>

<!-- Table Audit Data -->
<c:forEach var="log" items="${viewContext.results}">
<c:set var="ud" value="${userData[log.authorID]}" scope="page" />
<c:set var="author" value="${authors[log.authorID]}" scope="page" />
<c:set var="ipInfo" value="${ip[log.remoteAddr]}" scope="page" />
<tr>
 <td><fmt:date date="${log.date}" t="HH:mm" /></td>
 <td class="sec">${log.auditType}</td>
 <td class="pri bld">${log.auditID}</td>
 <td>${author.name}</td>
 <td class="small nophone">${log.remoteAddr} (${log.remoteHost})<c:if test="${!empty ipInfo}"><br /><el:flag countryCode="${ipInfo.country.code}" caption="${ipInfo.location}" /> ${ipInfo.location}</c:if></td>
 <td class="small right"><fmt:text value="${log.description}"></fmt:text></td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
