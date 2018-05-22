<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Roster</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script>
golgotha.local.sort = function() { return document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="roster.do" method="post" validate="return false">
<view:table cmd="roster">
<!-- Table Sort Combo Bar -->
<tr class="title">
 <td colspan="3" class="left caps"><span class="nophone"><content:airline />&nbsp;</span>PILOT ROSTER</td>
 <td colspan="5" class="right">PROGRAM <el:combo name="eqType" size="1" idx="*" options="${eqTypes}" firstEntry="All Programs" value="${param.eqType}" onChange="void golgotha.local.sort()" />
<span class="nophone"> SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void golgotha.local.sort()" /></span></td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td style="width:10%">PILOT CODE</td>
 <td style="width:30%">PILOT NAME</td>
 <td style="width:10%">EQUIPMENT</td>
 <td style="width:16%">RANK</td>
 <td class="nophone" style="width:6%">HOME</td>
 <td style="width:8%">FLIGHTS</td>
 <td class="nophone" style="width:8%">HOURS</td>
 <td>LAST FLIGHT</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="pilot" items="${viewContext.results}">
<tr>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td class="sec bld">${pilot.equipmentType}</td>
 <td class="pri bld">${pilot.rank.name}</td>
 <td class="sec nophone">${pilot.homeAirport}</td>
 <td><fmt:int value="${pilot.legs}" /></td>
 <td class="nophone"><fmt:dec value="${pilot.hours}" /></td>
 <td><fmt:date fmt="d" date="${pilot.lastFlight}" default="-" /></td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="8"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
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
