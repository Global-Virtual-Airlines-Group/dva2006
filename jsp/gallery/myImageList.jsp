<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Image Gallery - ${pilot.name}</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:googleAnalytics />
<content:js name="common" />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/gallery/header.jspf" %> 
<%@include file="/jsp/gallery/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="myimgs">
<!-- Table Sort Bar -->
<tr class="title">
 <td colspan="5"><span class="nophone"><content:airline /> </span>IMAGE GALLERY<span class="nophone"> - ${pilot.name} (${pilot.pilotCode})</span></td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:30%">IMAGE TITLE</td>
 <td style="width:10%">CREATED</td>
 <td class="nophone" style="width:10%">SIZE</td>
 <td class="nophone" style="width:5%">LIKES</td>
 <td>DESCRIPTION</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="img" items="${viewContext.results}">
<tr>
 <td class="pri bld"><el:cmd url="image" link="${img}">${img.name}</el:cmd></td>
 <td><fmt:date date="${img.createdOn}" fmt="d" /></td>
 <td class="small nophone"><span class="sec bld">${img.width}x${img.height}</span>, <fmt:fileSize value="${img.size}" /></td>
<c:if test="${img.likeCount == 0}">
 <td class="small bld nophone">-</td>
</c:if>
<c:if test="${img.likeCount > 0}">
 <td class="bld nophone"><fmt:int value="${img.likeCount}" /></td>
</c:if>
 <td class="small left"><fmt:text value="${img.description}" /></td>
</tr>
</c:forEach>

<!-- Scroll bar row -->
<tr class="title">
 <td colspan="5"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
