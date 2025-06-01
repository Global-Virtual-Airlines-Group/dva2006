<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> CSP Violations</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:googleAnalytics />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="cspviolations">
<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:15%">DATE</td>
 <td style="width:15%">TYPE</td>
 <td style="width:20%">HOST</td>
 <td style="width:10%">COUNT</td>
 <td>SITE URLS</td>
</tr>

<!-- Table data -->
<c:forEach var="cv" items="${viewContext.results}">
<view:row entry="${cv}">
 <td class="pri bld"><fmt:date date="${cv.date}" fmt="d" /></td>
 <td class="sec bld caps">${cv.type}</td>
 <td>${cv.host}</td>
 <td class="bld"><fmt:int value="${cv.count}" /></td>
 <td class="left"><c:forEach var="url" items="${cv.URLs}" varStatus="hasNext"><el:cmd className="small plain" url="brwreports" linkID="${url}">${url}</el:cmd><c:if test="${!hasNext.last}">, </c:if></c:forEach></td>
</view:row>
</c:forEach>

<!-- Button Bar -->
<tr class="title">
 <td colspan="5"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
