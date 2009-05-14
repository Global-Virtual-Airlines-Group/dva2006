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
<title><content:airline /> ACARS Dispatcher Routes<c:if test="${!empty author}"> - ${author.name}</c:if></title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function update(combo)
{
combo.enabled = false;
if (combo.selectedIndex == 0)
	self.location = '/dsproutes.do';
else
	self.location = '/dsproutes.do?id=' + combo.options[combo.selectedIndex].value;

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
<el:form action="dsproutes.do" method="post" validate="return false">
<view:table className="view" space="default" pad="default" cmd="dsproutes">
<!-- Table Header Bar -->
<tr class="title">
 <td colspan="4" class="left caps"><content:airline /> ACARS DISPATCHER ROUTES<c:if test="${!empty author}"> - ${author.name}</c:if></td>
 <td><el:cmd url="dsprsearch">SEARCH</el:cmd></td>
 <td colspan="2" class="right">DISPATCHER <el:combo name="id" idx="*" size="1" options="${authorNames}" firstEntry="-" value="${author}" onChange="update(this)" /></td>
</tr>
<tr class="title caps">
 <td width="5%">ID</td>
 <td>ROUTE</td>
 <td width="5%">USED</td> 
<c:if test="${empty author}"><td width="20%">CREATED BY</td></c:if>
<c:if test="${!empty author}"><td width="8%">CREATED ON</td></c:if>
 <td width="8%">LAST USED</td>
 <td width="15%">SID</td>
 <td width="15%">STAR</td>
</tr>

<!-- Routes -->
<c:forEach var="route" items="${viewContext.results}">
<c:set var="rAuthor" value="${authors[route.authorID]}" scope="page" />
<tr>
 <td><el:cmd url="dsproute" link="${route}" className="pri bld"><fmt:int value="${route.ID}" /></el:cmd></td>
 <td>${route.airportD.name} (<fmt:airport airport="${route.airportD}" />)<br />
${route.airportA.name} (<fmt:airport airport="${route.airportA}" />)</td>
 <td class="sec bld"><fmt:int value="${route.useCount}" /></td>
 <td class="small"><c:if test="${empty author}"><el:cmd url="dsproutes" link="${rAuthor}" className="pri bld">${rAuthor.name}</el:cmd> (${rAuthor.pilotCode}) on </c:if><fmt:date date="${route.createdOn}" fmt="d" /></td>
 <td class="small sec"><fmt:date date="${route.lastUsed}" fmt="d" default="N/A" /></td>
 <td class="small">${route.SID}</td>
 <td class="small">${route.STAR}</td>
</tr>
<c:if test="${!empty route.route}">
<tr>
 <td class="left small" colspan="7">${route.route}</td>
</tr>
</c:if>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
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
