<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Suspended <content:airline /> Pilots</title>
<content:css name="main" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="suspendedusers">
<tr class="title">
 <td colspan="7" class="left caps"><content:airline /> SUSPENDED PILOT LIST</td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td style="width:24%">PILOT NAME</td>
 <td style="width:10%">PILOT ID</td>
 <td class="nophone" style="width:12%">JOINED ON</td>
 <td class="nophone" style="width:12%">LAST FLIGHT</td>
 <td class="nophone" style="width:8%">FLIGHTS</td>
 <td style="width:15%">SUSPENDED ON</td>
 <td>DURATION</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="pilot" items="${viewContext.results}">
<c:set var="upd" value="${updates[pilot.ID]}" scope="page" />
<tr>
 <td><el:cmd url="profile" link="${pilot}" className="bld">${pilot.name}</el:cmd></td>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td class="nophone"><fmt:date fmt="d" date="${pilot.createdOn}" default="N/A" /></td>
 <td class="nophone"><fmt:date fmt="d" date="${pilot.lastFlight}" default="N/A" /></td>
 <td class="nophone"><fmt:int value="${pilot.legs}" /></td>
<c:if test="${!empty upd}">
<c:set var="duration" value="${(now.toEpochMilli() - upd.date.toEpochMilli()) / 86400000}" scope="page" />
 <td class="bld"><fmt:date fmt="d" date="${upd.date}" /></td>
 <td class="pri bld"><fmt:int value="${duration}" /> DAYS</td>
</c:if>
<c:if test="${empty upd}">
 <td colspan="2" class="bld">UNKNOWN</td>
</c:if>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
