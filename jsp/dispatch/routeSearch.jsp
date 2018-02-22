<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Dispatcher Routes</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:json />
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<script>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
if (!golgotha.form.comboSet(f.airportD) && !golgotha.form.comboSet(f.airportA)) {
	alert('Please select a Departure or Arrival Airport.');
	f.airportD.focus();
	return false;
}

golgotha.form.submit(f);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="dsprsearch.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Table Header Bar -->
<tr class="title">
 <td colspan="6" class="left caps"><content:airline /> ACARS DISPATCHER ROUTE SEARCH</td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" idx="*" size="1" options="${airports}" firstEntry="-" value="${airportD}" onChange="void this.updateAirportCode()" />
 <el:airportCode combo="airportD" idx="*" airport="${airportD}" /></td>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" idx="*" size="1" options="${airports}" firstEntry="-" value="${airportA}" onChange="void this.updateAirportCode()" />
 <el:airportCode combo="airportA" idx="*" airport="${airportA}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr class="title">
 <td colspan="4"><el:button ID="SearchButton" type="submit" label="SEARCH DISPATCHER ROUTES" /></td>
</tr>
</el:table>
<c:if test="${doSearch}">
<br />
<view:table cmd="dspsearch">
<tr class="title caps">
 <td colspan="8" class="left">DISPATCH ROUTE SEARCH RESULTS</td>
</tr>
<c:if test="${fn:sizeof(results) > 0}">
<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:5%">ID</td>
 <td>ROUTE</td>
 <td style="width:5%">USED</td>
 <td style="width:20%">DISPATCHER NAME</td>
 <td style="width:8%">CREATED</td>
 <td style="width:8%">LAST USED</td>
 <td style="width:15%">SID</td>
 <td style="width:15%">STAR</td>
</tr>

<!-- Routes -->
<c:forEach var="route" items="${results}">
<c:set var="author" value="${authors[route.authorID]}" scope="page" />
<view:row entry="${route}">
 <td><el:cmd url="dsproute" link="${route}" className="pri bld"><fmt:int value="${route.ID}" /></el:cmd></td>
 <td>${route.airportD.name} (<fmt:airport airport="${route.airportD}" />)<br />
${route.airportA.name} (<fmt:airport airport="${route.airportA}" />)</td>
 <td class="sec bld"><fmt:int value="${route.useCount}" /></td>
 <td><el:cmd url="dsproutes" link="${author}" className="pri bld">${author.name}</el:cmd> (${author.pilotCode})</td>
 <td class="small bld"><fmt:date date="${route.createdOn}" fmt="d" /></td>
 <td class="small sec"><fmt:date date="${route.lastUsed}" fmt="d" default="N/A" /></td>
 <td class="small">${route.SID}</td>
 <td class="small">${route.STAR}</td>
</view:row>
<c:if test="${!empty route.route}">
<view:row entry="${route}">
 <td colspan="8" class="left small">${route.route}</td>
</view:row>
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
 <td colspan="8">&nbsp;
<c:if test="${access.canCreate && (!empty airportD) && (!empty airportA)}">
<el:cmdbutton url="dsprouteplot" linkID="0&airportD=${airportD.ICAO}&airportA=${airportA.ICAO}" label="PLOT NEW DISPATCH ROUTE" /></c:if></td>
</tr>
</view:table>
</c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<fmt:aptype var="useICAO" />
<script type="text/javascript" defer>
var f = document.forms[0];
golgotha.airportLoad.config.doICAO = '${useICAO}';
golgotha.airportLoad.setHelpers(f.airportD);
golgotha.airportLoad.setHelpers(f.airportA);
</script>
</body>
</html>
