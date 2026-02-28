<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Challenging Runways</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:googleAnalytics />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:newRelic>
<content:cspHeader />
</content:newRelic>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="challengerunways">
<!-- Table Title Bar -->
<tr class="title caps">
 <td colspan="7" class="left"><span class="nophone"><content:airline />&nbsp;</span>CHALLENGING RUNWAYS</td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:10%">#</td>
 <td>AIRPORT</td>
 <td>RUNWAY</td>
 <td style="width:15%">SCORE</td>
 <td style="width:15%">DISTANCE</td>
 <td style="width:10%">VERT. SPEED</td>
 <td style="width:6%">LANDINGS</td>
</tr>

<!-- Table Runway Data -->
<c:set var="idx" value="0" scope="page" />
<c:forEach var="rls" items="${viewContext.results}">
<c:set var="idx" value="${idx + 1}" scope="page" />
<c:set var="rwyID" value="${rls.airport.ICAO}-${rls.runway}" scope="page" />
<c:set var="rwy" value="${rwys[rwyID]}" scope="page" />
<tr>
 <td class="bld"><fmt:int value="${idx}" /></td>
 <td class="small"><span class="nophone">${rls.airport.name} - </span><el:cmd url="airportinfo" linkID="${rls.airport.ICAO}" className="plain" authOnly="true"><fmt:airport airport="${rls.airport}" /></el:cmd></td>
 <td><span class="pri bld">${rls.runway}</span><span class="nophone"> - <fmt:int value="${rwy.length}" /> feet, ${rwy.heading} degrees</span><c:if test="${!empty rwy.alternateCode}"> <span class="small ita"> [now ${rwy.name}]</span></c:if></td>
 <td><fmt:landscore value="${rls.averageScore}" className="bld" /><c:if test="${rls.count > 1}"> +/- <fmt:dec value="${rls.scoreSD}" /></c:if></td>
 <td><fmt:int value="${rls.averageDistance}" /><c:if test="${rls.count > 1}"> +/- <fmt:int value="${rls.distanceSD}" /></c:if> ft</td>
 <td><fmt:int value="${rls.averageVerticalSpeed }" /><c:if test="${rls.count > 1}"> +/- <fmt:int value="${rls.verticalSpeedSD}" /></c:if> ft/min</td>
 <td class="bld"><fmt:int value="${rls.count}" /></td>
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
</body>
</html>
