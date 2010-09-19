<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="forumName" name="airline.forum" />
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ${forumName} Statistics</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:css name="form" />
<content:pics />
<script type="text/javascript">
function updateSort()
{
document.forms[0].submit();
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="coolerstats.do" method="post" validate="return true">
<view:table className="view" cmd="coolerstats">
<tr class="title">
 <td colspan="2" class="left">${forumName} STATISTICS</td>
 <td colspan="5" class="right">GROUP BY <el:combo name="groupType" size="1" idx="*" options="${groupTypes}" value="${param.groupType}" onChange="void updateSort()" />
 SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void updateSort()" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td width="5%">#</td>
 <td width="50%">${labelName}</td>
 <td width="15%">MESSAGES</td>
 <td width="15%">DISTINCT ${distinctName}</td>
 <td>POSTS PER ${distinctName}</td>
</tr>

<!-- Table Statistics Data -->
<c:set var="entryNumber" value="${viewStart}" scope="page" />
<c:forEach var="stat" items="${viewContext.results}">
<view:row entry="${stat}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
 <td class="sec bld">${entryNumber}</td>
 <td class="pri bld">${stat.label}</td>
 <td class="bld"><fmt:int value="${stat.posts}" /></td>
 <td class="pri bld"><fmt:int value="${stats.distinct}" /></td>
 <td class="sec bld"><fmt:dec value="${stats.posts / stats.distinct}" /></td>
</view:row>
</c:forEach>

<!-- Table Footer Bar -->
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
