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
 <td colspan="2" class="left caps"><content:airline /> Flight Route Popularity</td> 
 <td colspan="4" class="right"><el:box name="noRoutes" idx="*" value="true" label="Show Pairs without Dispatch Routes" checked="${param.noRoutes}" />
 <el:box name="allFlights" idx="*" value="true" label="Inlcude non-ACARS Flights" checked="${param.allFlights}" />
 IN THE PAST <el:text name="days" idx="*" className="bld" value="${param.days}" /> DAYS 
 <el:button ID="UpdateButton" type="submit" className="BUTTON" label="GO" /></td>
</tr>
<tr class="title caps">
 <td width="5%">#</td>
 <td width="30%">DEPARTING FROM</td>
 <td width="30%">ARRIVING AT</td>
 <td width="10%">DISTANCE</td>
 <td width="10%">FLIGHTS</td>
 <td>ROUTES</td>
</tr>
 
 <!-- Table Route Data -->
 <c:set var="entryNumber" value="${viewStart}" scope="request" />
 <c:forEach var="route" items="${viewContext.results}">
 <tr>
  <td class="pri bld"><fmt:int value="${entryNumber}" /></td>
  <td colspan="2">${route.airportD.name} (<fmt:airport airport="${route.airportD}" />) to ${route.airportA.name}
  (<fmt:airport airport="${route.airportA}" />)</td>
  <td><fmt:int value="${route.distance}" /> miles</td>
  <td class="bld"><fmt:int value="${route.flights}" /> flights</td>
  <td class="pri bld"><fmt:int value="${route.routes}" /> routes</td>
 </tr>
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
 