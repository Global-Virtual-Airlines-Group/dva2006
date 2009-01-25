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
<title><content:airline /> Popular Route Pairs</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function search(aD, aA)
{
var f = document.forms[0];
f.airportD.value = aD;
f.airportA.value = aA;
f.action = '/dsprsearch.do';
f.submit();
return true;	
}
<c:if test="${access.canCreate}">
function plot(aD, aA)
{
var f = document.forms[0];
f.airportD.value = aD;
f.airportA.value = aA;
f.action = '/dsprouteplot.do';
f.submit();
return true;	
}
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
<view:table className="view" space="default" pad="default" cmd="poproutes">
<!-- Table Header Bar -->
<tr class="title">
 <td colspan="3" class="left caps"><content:airline /> Flight Route Popularity</td> 
 <td colspan="4" class="right"><el:box name="noRoutes" idx="*" value="true" label="Show Pairs without Dispatch Routes" checked="${param.noRoutes}" />
 <el:box name="allFlights" idx="*" value="true" label="Inlcude non-ACARS Flights" checked="${param.allFlights}" />
 IN THE PAST <el:text name="days" idx="*" size="3" max="4" className="bld" value="${dayFilter}" /> DAYS 
 <el:button ID="UpdateButton" type="submit" className="BUTTON" label="GO" /></td>
</tr>
<tr class="title caps">
 <td width="5%">#</td>
 <td width="10%">&nbsp;</td>
 <td width="20%">DEPARTING FROM</td>
 <td width="30%">ARRIVING AT</td>
 <td width="10%">DISTANCE</td>
 <td width="12%">FLIGHTS</td>
 <td>ROUTES</td>
</tr>
 
 <!-- Table Route Data -->
<c:set var="entryNumber" value="${viewStart}" scope="request" />
<c:forEach var="route" items="${viewContext.results}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="request" />
<view:row entry="${route}">
 <td class="pri bld"><fmt:int value="${entryNumber}" /></td>
<c:choose>
<c:when test="${access.canCreate && (route.routes == 0)}">
 <td><el:button onClick="javascript:void plot('${route.airportD.ICAO}', '${route.airportA.ICAO}')" className="BUTTON" label="PLOT ROUTE" /></td>
 <td colspan="2">${route.airportD.name} (<fmt:airport airport="${route.airportD}" />) to ${route.airportA.name}
 (<fmt:airport airport="${route.airportA}" />)</td>
</c:when>
<c:otherwise>
 <td colspan="3">${route.airportD.name} (<fmt:airport airport="${route.airportD}" />) to ${route.airportA.name}
 (<fmt:airport airport="${route.airportA}" />)</td>
</c:otherwise>
</c:choose>
 <td><fmt:distance value="${route.distance}" /></td>
 <td class="bld"><fmt:int value="${route.flights}" /> flights</td>
<c:if test="${route.routes > 0}">
 <td><el:link url="javascript:void search('${route.airportD.ICAO}', '${route.airportA.ICAO}')" className="sec bld"><fmt:int value="${route.routes}" /> routes</el:link></td>
</c:if>
<c:if test="${route.routes == 0}">
 <td class="sec bld">NO ROUTES</td>
</c:if>
</view:row>
</c:forEach>
 
 <!-- Table Footer Bar -->
<tr class="title">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
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
 