<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Flight Report Queue</title>
<content:expire expires="5" />
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:pics />
<content:favicon />
<script type="text/javascript">
golgotha.local.sort = function() { return document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="subLists" value="${(!empty myHeld) || (!empty myEQType)}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="pirepqueue.do" method="post" validate="return false">
<view:table cmd="pirepqueue">
<!-- Table Header Bar-->
<tr class="title">
 <td colspan="4" class="left caps"><span class="nophone"><content:airline />&nbsp;</span>PENDING FLIGHT REPORT QUEUE</td>
 <td colspan="3" class="right nophone">SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void golgotha.local.sort()" /></td>
</tr>
<tr class="title caps">
 <td style="max-width:104px;">DATE</td>
 <td class="nophone">INFO</td>
 <td>FLIGHT NUMBER</td>
 <td>PILOT NAME</td>
 <td class="nophone" style="width:30%">AIRPORTS</td>
 <td class="nophone">EQUIPMENT</td>
 <td class="nophone">DURATION</td>
</tr>
<c:if test="${!empty myHeld}">
<!-- Flight Reports held by ${user.name} -->
<tr class="title">
 <td colspan="7" class="left caps"><fmt:int value="${myHeld.size()}" /> FLIGHT REPORTS HELD BY ${user.name}</td>
</tr>
<c:forEach var="pirep" items="${myHeld}">
<c:set var="pilot" value="${pilots[fn:PilotID(pirep)]}" scope="page" />
<view:row entry="${pirep}">
 <td><fmt:date fmt="d" date="${pirep.date}" /></td>
 <td class="nophone"><c:if test="${fn:EventID(pirep) != 0}"><el:img src="network/event.png" caption="Online Event" /></c:if> 
<c:if test="${fn:isACARS(pirep)}"><el:img src="acars.png" caption="ACARS Logged" /></c:if> 
<c:if test="${fn:isOnline(pirep)}"><el:img src="network/icon_${fn:lower(fn:network(pirep))}.png" caption="Online Flight on ${fn:network(pirep)}" /></c:if>
<c:if test="${fn:isDispatch(pirep)}"><el:img src="dispatch.png" caption="ACARS Dispatch Services" /></c:if>
<c:if test="${fn:anyWarn(pirep)}"><el:img src="warning.png" caption="Flight Report Warning" /></c:if>
<c:if test="${fn:isPromoLeg(pirep)}"><el:img src="promote.png" caption="Counts for Promotion in the ${fn:promoEQTypes(pirep)}" /></c:if></td>
 <td><el:cmd className="bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd></td>
 <td class="small">${pilot.name}</td>
 <td class="small nophone">${pirep.airportD.name} - ${pirep.airportA.name}</td>
 <td class="sec nophone">${pirep.equipmentType}</td>
 <td class="nophone"><fmt:dec fmt="#0.0" value="${pirep.length / 10}" /> hours</td>
</view:row>
</c:forEach>
</c:if>
<c:if test="${!empty myEQType}">
<!-- Flight Reports in the ${myEQ.name} program -->
<tr class="title">
 <td colspan="7" class="left caps"><fmt:int value="${myEQType.size()}" /> FLIGHT REPORTS FOR THE ${myEQ.name} PROGRAM</td>
</tr>
<c:forEach var="pirep" items="${myEQType}">
<c:set var="pilot" value="${pilots[fn:PilotID(pirep)]}" scope="page" />
<view:row entry="${pirep}">
 <td><fmt:date fmt="d" date="${pirep.date}" /></td>
 <td class="nophone"><c:if test="${fn:EventID(pirep) != 0}"><el:img src="network/event.png" caption="Online Event" /></c:if> 
<c:if test="${fn:isACARS(pirep)}"><el:img src="acars.png" caption="ACARS Logged" /></c:if> 
<c:if test="${fn:isCheckFlight(pirep)}"><el:img src="checkride.png" caption="Check Ride" /></c:if>
<c:if test="${fn:isOnline(pirep)}"><el:img src="network/icon_${fn:lower(fn:network(pirep))}.png" caption="Online Flight on ${fn:network(pirep)}" /></c:if>
<c:if test="${fn:isDispatch(pirep)}"><el:img src="dispatch.png" caption="ACARS Dispatch Services" /></c:if>
<c:if test="${fn:anyWarn(pirep)}"><el:img src="warning.png" caption="Flight Report Warning" /></c:if>
<c:if test="${fn:isPromoLeg(pirep)}"><el:img src="promote.png" caption="Counts for Promotion in the ${fn:promoEQTypes(pirep)}" /></c:if></td>
 <td><el:cmd className="bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd></td>
 <td class="small">${pilot.name}</td>
 <td class="small nophone">${pirep.airportD.name} - ${pirep.airportA.name}</td>
 <td class="sec nophone">${pirep.equipmentType}</td>
 <td class="nophone"><fmt:dec fmt="#0.0" value="${pirep.length / 10}" /> hours</td>
</view:row>
</c:forEach>
</c:if>
<c:if test="${subLists}">
<tr class="title">
 <td colspan="7" class="left caps"><fmt:int value="${viewContext.results.size()}" /> PENDING FLIGHT REPORTS</td>
</tr>
</c:if>
<!-- Table Flight Report Data -->
<c:forEach var="pirep" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[fn:PilotID(pirep)]}" scope="page" />
<view:row entry="${pirep}">
 <td><fmt:date fmt="d" date="${pirep.date}" /></td>
 <td class="nophone"><c:if test="${fn:EventID(pirep) != 0}"><el:img src="network/event.png" caption="Online Event" /></c:if> 
<c:if test="${fn:isACARS(pirep)}"><el:img src="acars.png" caption="ACARS Logged" /></c:if>
<c:if test="${fn:isCheckFlight(pirep)}"><el:img src="checkride.png" caption="Check Ride" /></c:if>
<c:if test="${fn:isOnline(pirep)}"><el:img src="network/icon_${fn:lower(fn:network(pirep))}.png" caption="Online Flight on ${fn:network(pirep)}" /></c:if>
<c:if test="${fn:isDispatch(pirep)}"><el:img src="dispatch.png" caption="ACARS Dispatch Services" /></c:if>
<c:if test="${fn:anyWarn(pirep)}"><el:img src="warning.png" caption="Flight Report Warning" /></c:if>
<c:if test="${fn:isPromoLeg(pirep)}"><el:img src="promote.png" caption="Counts for Promotion in the ${fn:promoEQTypes(pirep)}" /></c:if></td>
 <td><el:cmd className="bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd></td>
 <td class="small">${pilot.name}</td>
 <td class="small nophone"><fmt:text value="${pirep.airportD.name} - ${pirep.airportA.name}" /></td>
 <td class="sec nophone">${pirep.equipmentType}</td>
 <td class="nophone"><fmt:dec fmt="#0.0" value="${pirep.length / 10}" /> hours</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7"><view:scrollbar force="${doScroll}"><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="116" labels="Submitted,Held,Check Ride,Flight Academy" classes="opt1,warn,opt3,opt4" /></td>
</tr>
</view:table>
<br />
<content:copyright />
</el:form>
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
