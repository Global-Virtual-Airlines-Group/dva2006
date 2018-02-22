<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Picture Gallery</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/gallery/header.jspf" %> 
<%@include file="/jsp/gallery/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="imagegallery.cmd" method="post" validate="return true">
<view:table cmd="imagegallery">
<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:25%">IMAGE TITLE</td>
 <td style="width:10%">SIZE</td>
 <td style="width:15%">AUTHOR</td>
 <td style="width:5%">SCORE</td>
 <td style="width:5%">VOTES</td>
 <td>DESCRIPTION</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="img" items="${viewContext.results}">
<c:set var="author" value="${pilots[img.authorID]}" scope="page" />
<tr>
 <td class="pri bld"><el:cmd url="image" link="${img}">${img.name}</el:cmd></td>
 <td class="small"><span class="sec bld">${img.width}x${img.height}</span>, <fmt:int value="${img.size / 1024}" />K</td>
 <td class="bld"><el:cmd url="profile" link="${author}">${author.name}</el:cmd></td>
<c:if test="${img.voteCount == 0}">
 <td colspan="2" class="small">NOT YET RATED</td>
</c:if>
<c:if test="${img.voteCount > 0}">
 <td class="pri bld"><fmt:dec value="${img.score}" /></td>
 <td class="bld"><fmt:int value="${img.voteCount}" /></td>
</c:if>
 <td class="small left">${img.description}</td>
</tr>
</c:forEach>

<!-- Scroll bar row -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
</el:form>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
