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
<title>Suspended <content:airline /> Pilots</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="suspendedusers">
<tr class="title">
 <td colspan="7" class="left caps"><content:airline /> SUSPENDED PILOT LIST</td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td width="24%">PILOT NAME</td>
 <td width="10%">PILOT ID</td>
 <td width="12%">JOINED ON</td>
 <td width="12%">LAST FLIGHT</td>
 <td width="8%">FLIGHTS</td>
 <td width="15%">SUSPENDED ON</td>
 <td>DURATION</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="pilot" items="${viewContext.results}">
<c:set var="upd" value="${updates[pilot.ID]}" scope="request" />
<tr>
 <td><el:cmd url="profile" link="${pilot}" className="bld">${pilot.name}</el:cmd></td>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><fmt:date fmt="d" date="${pilot.createdOn}" default="N/A" /></td>
 <td><fmt:date fmt="d" date="${pilot.lastFlight}" default="N/A" /></td>
 <td><fmt:int value="${pilot.legs}" /></td>
<c:if test="${!empty upd}">
<c:set var="duration" value="${(now.time - upd.createdOn.time) / 86400000}" scope="request" />
 <td class="bld"><fmt:date fmt="d" date="${upd.createdOn}" /></td>
 <td class="pri bld"><fmt:int value="${duration}" /> DAYS</td>
</c:if>
<c:if test="${empty upd}">
 <td colspan="2" class="bld">UNKNOWN</td>
</c:if>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar> </td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
