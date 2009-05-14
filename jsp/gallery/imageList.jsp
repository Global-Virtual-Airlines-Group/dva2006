<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Image Gallery</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:js name="datePicker" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
setSubmit();
disableButton('CalendarButton');
disableButton('SubmitButton');
return true;	
}

function setSort()
{
var f = document.forms[0];
f.submit();
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/gallery/header.jspf" %> 
<%@include file="/jsp/gallery/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="imagegallery.do" method="post" validate="return validate(this)">
<view:table className="view" space="default" pad="default" cmd="imagegallery">
<!-- Table Sort Bar -->
<tr class="title">
 <td colspan="2">BY DATE <el:text name="imgDate" idx="*" size="10" max="10" value="${param.imgDate}" />
 <el:button ID="CalendarButton" label="CALENDAR" className="BUTTON" onClick="void show_calendar('forms[0].imgDate')" /></td>
 <td colspan="2"><el:cmd url="fleetgallery" linkID="true">FLEET GALLERY</el:cmd></td>
 <td colspan="2">SORT BY <el:combo name="sortType" idx="*" size="1" options="${sortOptions}" firstEntry="-" value="${param.sortType}" onChange="setSort()" />
 <el:button ID="SubmitButton" type="submit" className="BUTTON" label="GO" /></td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td width="25%">IMAGE TITLE</td>
 <td width="10%">SIZE</td>
 <td width="15%">AUTHOR</td>
 <td width="5%">SCORE</td>
 <td width="5%">VOTES</td>
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
