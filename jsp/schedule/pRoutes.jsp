<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Preferred Routes for ${airport}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function sortBy(combo)
{
newAirport = combo.options[combo.selectedIndex].value;
self.location = '/routes.do?op=domestic&id=' + newAirport;
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form action="routes.do" method="GET" validate="return false">
<view:table className="view" pad="default" space="default" cmd="routes">

<!-- Table Header Bar -->
<tr class="title">
 <td width="25%">DESTINATION</td>
 <td width="20%">ARTCCs</td>
 <td width="10%">ROUTE</td>
 <td class="right">AIRPORT <el:combo name="sortType" idx="1" size="1" options="${airports}" value="${airport}" onChange="void sortBy(this)" /></td>
</tr>

<!-- Table Data Section -->
<c:forEach var="route" items="${viewContext.results}">
<tr>
 <td class="small">${route.airportA.name} (<fmt:airport airport="${route.airportA}" />)</td>
 <td class="sec">${route.ARTCC}</td>
 <td colspan="2" class="left">${route.route}</td>
</tr>
</c:forEach>

<!-- Scroll bar row -->
<tr class="title">
 <td colspan="4">
<c:if test="${access.canDelete && (!empty viewContext.results)}">
<el:cmdbutton url="routepurge" op="domestic" label="PURGE DOMESTIC ROUTES" />
</c:if>
<c:if test="${access.canImport}">
&nbsp;<el:cmdbutton url="routeimport" label="IMPORT DOMESTIC ROUTES" />
</c:if>
 <view:pgUp />&nbsp;<view:pgDn />
 </td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</div>
</body>
</html>
