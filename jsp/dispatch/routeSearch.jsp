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
var hasAD = (form.airportD.selectedIndex > 0);
var hasAA = (form.airportA.selectedIndex > 0);
if (!hasAD && !hasAA) {
	alert('Please select a Departure or Arrival Airport.');
	form.airportD.focus();
	return false;
}

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
 <td class="data"><el:combo name="airportD" idx="*" size="1" options="${airports}" firstEntry="-" value="${param.airportD}" /></td>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" idx="*" size="1" options="${airports}" firstEntry="-" value="${param.airportA}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr class="title">
 <td colspan="4"><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH DISPATCHER ROUTES" /></td>
</tr>
</el:table>
<c:if test="${doSearch}">
<br />
<view:table className="view" space="default" pad="default" cmd="dspsearch">
<tr class="title caps">
 <td colspan="8" class="left">DISPATCH ROUTE SEARCH RESULTS</td>
</tr>
<c:if test="${fn:sizeof(results) > 0}">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="5%">ID</td>
 <td>ROUTE</td>
 <td width="5%">USED</td>
 <td width="20%">DISPATCHER NAME</td>
 <td width="8%">CREATED</td>
 <td width="8%">LAST USED</td>
 <td width="15%">SID</td>
 <td width="15%">STAR</td>
</tr>

<!-- Routes -->
<c:forEach var="route" items="${results}">
<c:set var="author" value="${authors[route.authorID]}" scope="request" />
<tr>
 <td><el:cmd url="dsproute" link="${route}" className="pri bld"><fmt:int value="${route.ID}" /></el:cmd></td>
 <td>${route.airportD.name} (<fmt:airport airport="${route.airportD}" />)<br />
${route.airportA.name} (<fmt:airport airport="${route.airportA}" />)</td>
 <td class="sec bld"><fmt:int value="${route.useCount}" /></td>
 <td><el:cmd url="dsproutes" link="${author}" className="pri bld">${author.name}</el:cmd> (${author.pilotCode})</td>
 <td class="small bld"><fmt:date date="${route.createdOn}" fmt="d" /></td>
 <td class="small sec"><fmt:date date="${route.lastUsed}" fmt="d" default="N/A" /></td>
 <td class="small">${route.SID}</td>
 <td class="small">${route.STAR}</td>
</tr>
<c:if test="${!empty route.route}">
<tr>
 <td colspan="8" class="left small">${route.route}</td>
</tr>
</c:if>
</c:forEach>
</c:if>
<c:if test="${fn:sizeof(results) == 0}">
<tr>
 <td colspan="8" class="pri bld">No ACARS Dispatcher Routes between these Airports were found.</td>
</tr>
</c:if>
<!-- Bottom Bar -->
<tr class="title">
 <td colspan="8">&nbsp;</td>
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
