<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ACARS Dispatcher Routes</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.airportD, 'Departure Airport')) return false;
if (!validateCombo(form.airportA, 'Arrival Airport')) return false;

setSubmit();
disableButton('SearchButton');
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
<el:form action="dsprsearch.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<!-- Table Header Bar -->
<tr class="title">
 <td colspan="6" class="left caps"><content:airline /> ACARS DISPATCHER ROUTE SEARCH</td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" idx="*" size="1" options="${airports}" className="req" firstEntry="-" value="${param.airportD}" /></td>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" idx="*" size="1" options="${airports}" className="req" firstEntry="-" value="${param.airportA}" /></td>
</tr>

<!-- Button Bar -->
<tr class="title">
 <td colspan="4"><el:button ID="SearchButton" type="submit" className="button" label="SEARCH DISPATCHER ROUTES" /></td>
</tr>
</el:table>
<c:if test="${doSearch}">
<br />
<view:table className="view" space="default" pad="default" cmd="dspsearch">
<c:if test="${fn:sizeof(results) > 0}">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="5%">ID</td>
 <td width="15%">DEPARTING FROM</td>
 <td width="15%">ARRIVING AT</td>
 <td width="20%">CREATED BY</td>
 <td width="5%">USED</td>
 <td class="left">WAYPOINTS</td>
</tr>

<!-- Routes -->
<c:forEach var="route" items="${results}">
<c:set var="author" value="${authors[route.authorID]}" scope="request" />
<tr>
 <td><el:cmd url="dsproute" link="${route}" className="pri bld"><fmt:int value="${route.ID}" /></el:cmd></td>
 <td class="small">${route.airportD.name} (<fmt:airport airport="${route.airportD}" />)</td>
 <td class="small">${route.airportA.name} (<fmt:airport airport="${route.airportA}" />)</td>
 <td><el:cmd url="dsproutes" link="${author}" className="pri bld">${author.name}</el:cmd> (${author.pilotCode}) on <fmt:date date="${route.createdOn}" fmt="d" /></td>
 <td class="sec bld"><fmt:int value="${route.useCount}" /></td>
 <td class="left small">${route.route}</td>
</tr>
</c:forEach>
</c:if>
<c:if test="${fn:sizeof(results) == 0}">
<tr>
 <td colspan="6" class="pri bld">No Dispatcher Routes between these Airports were found.</td>
</tr>
</c:if>
<!-- Bottom Bar -->
<tr class="title">
 <td colspan="6">&nbsp;</td>
</tr>
</view:table>
</c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
