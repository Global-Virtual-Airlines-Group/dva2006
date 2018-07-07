<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Unvisited Airports - ${pilot.name}</title>
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
<content:sysdata var="allAirlines" name="airlines" />

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="mynewairports">
<tr class="title">
 <td colspan="4" class="left caps"><span class="nophone"><content:airline />&nbsp;</span>UNVISITED AIRPORTS FOR ${pilot.name}</td>
 <td><el:cmd url="mynewairports" op="map">MAP</el:cmd>
 <td>&nbsp;</td>
</tr>
<c:forEach var="entry" items="${airports}">
<c:set var="airline" value="${allAirlines[entry.key]}" scope="page" />
<c:set var="aps" value="${entry.value}" scope="page" />
<tr class="title">
 <td colspan="5" class="left caps">${airline.name} - <fmt:int value="${fn:sizeof(aps)}" /> AIRPORTS</td>
 <td class="right"><span class="und" onclick="void golgotha.util.toggleExpand(this, 'ap${airline.code}')">COLLAPSE</span></td>
</tr>
<tr class="title ap${airline.code}">
 <td style="width:20%">AIRPORT NAME</td>
 <td class="nophone" style="width:7%">ICAO</td>
 <td style="width:6%">IATA</td>
 <td class="nophone" style="width:15%">COUNTRY</td>
 <td style="width:7%">AIRLINES</td>
 <td class="left nophone">SOURCE / DESTINATION AIRPORTS</td>
</tr>
<c:forEach var="ap" items="${aps}">
<c:set var="srcList" value="${srcAirports[ap]}" scope="page" />
<c:set var="srcSize" value="${fn:sizeof(srcList)}" scope="page" />
<tr class="ap${airline.code}">
 <td class="pri bld"><el:cmd url="airportinfo" linkID="${ap.IATA}">${ap.name}</el:cmd></td>
 <td class="bld nophone">${ap.ICAO}</td>
 <td class="sec">${ap.IATA}</td>
 <td class="small nophone">${ap.country.name} <el:flag countryCode="${ap.country.code}" caption="${ap.country.name}" /></td>
 <td class="bld"><fmt:int value="${fn:sizeof(ap.airlineCodes)}" /></td>
<c:choose>
<c:when test="${srcSize == 0}">
 <td class="left error bld nophone">NO FLIGHTS TO/FROM THIS AIRPORT</td>
</c:when>
<c:when test="${srcSize < 3}">
 <td class="left small nophone">
<c:forEach var="srcAp" items="${srcList}" varStatus="srcStatus">${srcAp.name} (<fmt:airport airport="${srcAp}" />)<c:if test="${!srcStatus.last}">, </c:if></c:forEach>
 </td>
</c:when>
<c:otherwise>
 <td class="left nophone" title="${srcList}">Flights ${srcList.isSource() ? 'from' : 'to'} this Airport from <fmt:int value="${srcSize}" /> different Airports</td>
</c:otherwise>
</c:choose>
</tr>
</c:forEach>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="6">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
