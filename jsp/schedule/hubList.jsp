<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> - Airline Hubs</title>
<content:css name="main" />
<content:css name="view" />
<content:googleAnalytics />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:newRelic>
<content:cspHeader />
</content:newRelic>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="hubs">

<!-- Table Header Bar -->
<tr class="title">
 <td style="width:25%">AIRLINE NAME</td>
 <td>AIRPORT</td>
 <td style="width:10%"><el:cmdbutton url="hub" label="NEW HUB" /></td>
 <td style="width:15%">FLIGHTS</td>
</tr>

<!-- Table Hub Data -->
<c:forEach var="hub" items="${viewContext.results}">
<view:row entry="${hub}">
 <td class="pri bld">${hub.airline.name}</td>
 <td colspan="2"><el:cmd url="hub" linkID="${hub.airline.code}-${hub.airport.IATA}" className="bld">${hub.airport.name}</el:cmd> (<el:cmd url="airportinfo" linkID="${hub.airport.IATA}"><fmt:airport airport="${hub.airport}" /></el:cmd>)</td>
 <td><fmt:int value="${hub.destinationCount}" /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="4">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
