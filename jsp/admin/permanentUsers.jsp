<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Permanent <content:airline /> Pilots</title>
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
<view:table cmd="permusers">
<tr class="title">
 <td colspan="7" class="left caps"><content:airline /> PERMANENT PILOT LIST</td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td style="width:25%">PILOT NAME</td>
 <td>PILOT ID</td>
 <td style="width:20%" class="nophone">RANK / PROGRAM</td>
 <td class="nophone">JOINED ON</td>
 <td class="nophone">FLIGHTS</td>
 <td>LAST FLIGHT</td>
 <td>LAST LOGIN</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="pilot" items="${viewContext.results}">
<view:row entry="${pilot}">
 <td><el:cmd url="profile" link="${pilot}" className="bld">${pilot.name}</el:cmd></td>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td class="sec bld small nophone">${pilot.rank.name}, ${pilot.equipmentType}</td>
 <td class="bld nophone"><fmt:date fmt="d" date="${pilot.createdOn}" /></td>
 <td class="nophone"><fmt:int value="${pilot.legs}" /></td>
 <td class="sec bld"><fmt:date fmt="d" date="${pilot.lastFlight}"  default="N/A" /></td>
 <td class="pri bld"><fmt:date fmt="d" date="${pilot.lastLogin}" default="N/A" /></td>
</view:row>
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
