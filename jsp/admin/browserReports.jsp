<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Browser Reports</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:googleAnalytics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="brwreports">
<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:5%">#</td>
 <td class="nophone" style="width:10%">TYPE</td>
 <td style="width:15%">DATE</td>
 <td class="nophone" style="width:15%">AUTHOR</td>
 <td style="width:25%">URL</td>
 <td class="left">BODY</td>
</tr>

<!-- Table data -->
<c:forEach var="br" items="${viewContext.results}">
<c:set var="author" value="${pilots[br.authorID]}" scope="page" />
<view:row entry="${br}">
 <td class="bld"><fmt:int value="${br.ID}" /></td>
 <td class="nophone sec">${br.type}</td>
 <td class="pri"><fmt:date date="${br.createdOn}" t="HH:mm" /></td>
 <td class="nophone bld">${empty auhtor ? 'Anonymous' : author.name}</td>
 <td class="small">${br.URL}</td>
 <td class="left small">${br.body}</td>
</view:row>
</c:forEach>

<!-- Button Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
