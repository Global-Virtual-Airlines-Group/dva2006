<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Popular Route Pairs</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
golgotha.local.search = function(aD, aA) {
	const f = document.forms[0];
	f.airportD.value = aD;
	f.airportA.value = aA;
	f.action = '/dsprsearch.do';
	f.submit();
	return true;	
};
<c:if test="${access.canCreate}">
golgotha.local.plot = function(aD, aA) {
	const f = document.forms[0];
	f.airportD.value = aD;
	f.airportA.value = aA;
	f.action = '/dsprouteplot.do';
	f.submit();
	return true;	
};
</c:if>
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="poproutes.do" method="post" validate="return true">
<view:table cmd="poproutes">
<!-- Table Header Bar -->
<tr class="title">
 <td colspan="3" class="left caps"><content:airline /> Flight Route Popularity</td> 
 <td colspan="5" class="right"><el:box name="noRoutes" idx="*" value="true" label="Show Pairs without Dispatch Routes" checked="${param.noRoutes}" />
 <el:box name="allFlights" idx="*" value="true" label="Inlcude non-ACARS Flights" checked="${param.allFlights}" />
 in the past <el:text name="days" idx="*" size="3" max="4" className="bld" value="${dayFilter}" /> Days <el:button type="submit" label="GO" /></td>
</tr>
<tr class="title caps">
 <td style="width:5%">#</td>
 <td style="width:10%">&nbsp;</td>
 <td style="width:23%">DEPARTING FROM</td>
 <td style="width:23%">ARRIVING AT</td>
 <td style="width:10%">DISTANCE</td>
 <td style="width:10%">FLIGHTS</td>
 <td style="width:8%">ROUTES</td>
 <td>INACTIVE</td>
</tr>
 
 <!-- Table Route Data -->
<c:set var="entryNumber" value="${viewContext.start}" scope="page" />
<c:forEach var="route" items="${viewContext.results}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
<view:row entry="${route}">
 <td class="pri bld"><fmt:int value="${entryNumber}" /></td>
<c:choose>
<c:when test="${access.canCreate}">
 <td><el:button onClick="javascript:void golgotha.local.plot('${route.airportD.ICAO}', '${route.airportA.ICAO}')" label="PLOT ROUTE" /></td>
 <td colspan="2">${route.airportD.name} (<fmt:airport airport="${route.airportD}" />) to ${route.airportA.name}
 (<fmt:airport airport="${route.airportA}" />)</td>
</c:when>
<c:otherwise>
 <td colspan="3">${route.airportD.name} (<fmt:airport airport="${route.airportD}" />) to ${route.airportA.name}
 (<fmt:airport airport="${route.airportA}" />)</td>
</c:otherwise>
</c:choose>
<content:defaultMethod var="dst" object="${route}" method="distance" />
 <td><fmt:distance value="${dst}" /></td>
 <td class="bld"><fmt:int value="${route.flights}" /> flights</td>
<c:set var="allRoutes" value="${route.routes + route.inactiveRoutes}" scope="page" />
<c:if test="${allRoutes > 0}">
 <td><el:link url="javascript:void golgotha.local.search('${route.airportD.ICAO}', '${route.airportA.ICAO}')" className="sec bld"><fmt:int value="${route.routes}" /> routes</el:link></td>
 <td><el:link url="javascript:void golgotha.local.search('${route.airportD.ICAO}', '${route.airportA.ICAO}')"><fmt:int value="${route.inactiveRoutes}" /> inactive</el:link></td>
</c:if>
<c:if test="${allRoutes == 0}">
 <td colspan="2" class="sec bld">NO ROUTES</td>
</c:if>
</view:row>
</c:forEach>
 
 <!-- Table Footer Bar -->
<tr class="title">
 <td colspan="8"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="145" classes="opt1, " labels="No Dispatch Routes,Dispatch Routes" /></td>
</tr>
</view:table>
<el:text type="hidden" name="airportD" value="" readOnly="true" />
<el:text type="hidden" name="airportA" value="" readOnly="true" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
 