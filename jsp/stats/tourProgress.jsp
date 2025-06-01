<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Flight Tour Progress</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:googleAnalytics />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
<script async>
golgotha.local.updateTour = function() { document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="tourprogress.do" method="post" validate="return true">
<view:table cmd="tourprogress">

<!-- Table Header Bar-->
<tr class="title caps">
 <td colspan="2" class="left"><span class="nophone"><content:airline />&nbsp;</span>FLIGHT TOUR PROGRESS</td>
 <td colspan="4" class="right"><span class="nophone">SELECT TOUR </span><el:combo name="id" size="1" idx="*" required="true" firstEntry="[ SELECT ]" value="${tour}" options="${tours}" onChange="void golgotha.local.updateTour()" /></td>
</tr>
<tr class="title caps">
 <td style="width:40%">PILOT NAME</td>
 <td>PILOT ID</td>
 <td class="nophone">RANK</td>
 <td class="nophone">FIRST LEG</td>
 <td class="nophone">LAST LEG</td>
 <td>FLIGHTS</td>
</tr>

<!-- Table Tour Data -->
<c:forEach var="tp" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[tp.ID]}" scope="page" />
<tr>
 <td class="pri bld"><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td class="bld">${pilot.pilotCode}</td>
 <td class="sec nophone">${pilot.rank.name}, ${pilot.equipmentType}</td>
 <td class="nophone"><fmt:date date="${tp.firstLeg}" fmt="d" /></td>
 <td class="nophone"><fmt:date date="${tp.lastLeg}" fmt="d" /></td>
 <td class="bld"><fmt:int value="${tp.legs}" /> / <fmt:int value="${tour.flightCount}" /></td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="6">&nbsp;<view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar></td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
