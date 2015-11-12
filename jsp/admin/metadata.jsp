<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<html lang="en">
<head>
<title><content:airline /> System Metadata</title>
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
<view:table cmd="metadata">
<!-- Table Header Bar -->
<tr class="title caps">
 <td colspan="2" class="left"><content:airline /> SYSTEM METADATA VALUES</td>
</tr>
<tr class="title">
 <td style="width:20%">METADATA KEY</td>
 <td class="left">METADATA VALUE</td>
</tr>

<!-- Table Statistics Data -->
<c:forEach var="mdK" items="${fn:keys(data)}">
<c:set var="mdV" value="${data[mdK]}" scope="page" />
<tr class="view">
 <td class="pri bld">${mdK}</td>
 <td class="left">${mdV}</td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="2"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
