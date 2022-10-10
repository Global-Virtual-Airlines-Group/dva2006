<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Image Gallery</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:js name="datePicker" />
<script>
golgotha.local.validate = function(f) {
    if (!golgotha.form.check()) return false;
    golgotha.form.submit(f);
    return true;	
};

golgotha.local.setSort = function() { return document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/gallery/header.jspf" %> 
<%@include file="/jsp/gallery/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="imagegallery.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<view:table cmd="imagegallery">
<!-- Table Sort Bar -->
<tr class="title">
 <td colspan="2">BY DATE <el:text name="imgDate" idx="*" size="10" max="10" value="${param.imgDate}" />&nbsp;<el:button label="CALENDAR" onClick="void show_calendar('forms[0].imgDate')" /></td>
 <td colspan="2"><el:cmd url="fleetgallery" linkID="true">FLEET GALLERY</el:cmd></td>
 <td class="nophone">SORT BY <el:combo name="sortType" idx="*" size="1" options="${sortOptions}" firstEntry="-" value="${param.sortType}" onChange="golgotha.local.setSort()" />&nbsp;<el:button type="submit" label="GO" /></td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:25%">IMAGE TITLE</td>
 <td class="nophone" style="width:10%">SIZE</td>
 <td style="width:20%">AUTHOR</td>
 <td class="nophone" style="width:5%">LIKES</td>
 <td>DESCRIPTION</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="img" items="${viewContext.results}">
<c:set var="author" value="${pilots[img.authorID]}" scope="page" />
<tr>
 <td class="pri bld"><el:cmd url="image" link="${img}">${img.name}</el:cmd></td>
 <td class="small nophone"><span class="sec bld">${img.width}x${img.height}</span>, <fmt:fileSize value="${img.size}" /></td>
 <td class="bld"><el:cmd url="profile" link="${author}">${author.name}</el:cmd></td>
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
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
