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
<title><content:airline /> Flight Reports by SDK</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:pics />
<content:favicon />
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
<el:form action="pirepsdk.do" method="post" validate="return false">
<view:table cmd="pirepsdk">
<!-- Table Header Bar-->
<tr class="title">
 <td colspan="4" class="left caps"><content:airline /> FLIGHT REPORTS BY SDK</td>
 <td colspan="4" class="right">AIRCRAFT SDK <el:combo name="sdk" size="1" idx="*" options="${SDKs}" value="${param.sdk}" firstEntry="[ AIRCRAFT SDK ]" onChange="void golgotha.local.sort()" /></td>
</tr>
<!-- Table Header Bar-->
<tr class="title">
 <td style="width:8%">DATE</td>
 <td class="nophone" style="width:7%">INFO</td>
 <td style="width:10%">FLIGHT NUMBER</td>
 <td style="width:20%">PILOT NAME</td>
 <td class="nophone" style="width:25%">AIRPORT NAMES</td>
 <td>EQUIPMENT</td>
 <td class="nophone">SIMULATOR</td>
 <td class="nophone">DURATION</td>
</tr>

<!-- Table Flight Report Data -->
<c:forEach var="pirep" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[fn:PilotID(pirep)]}" scope="page" />
<tr>
 <td><fmt:date fmt="d" date="${pirep.date}" /></td>
 <td class="nophone"><c:if test="${fn:EventID(pirep) != 0}"><el:img src="network/event.png" caption="Online Event" /></c:if> 
<c:if test="${fn:isCheckFlight(pirep)}"><el:img src="checkride.png" caption="Check Ride" /></c:if>
<c:if test="${fn:isOnline(pirep)}"><el:img src="network/icon_${fn:lower(fn:network(pirep))}.png" caption="Online Flight on ${fn:network(pirep)}" /></c:if>
<c:if test="${fn:isDispatch(pirep)}"><el:img src="dispatch.png" caption="ACARS Dispatch Services" /></c:if>
<c:if test="${fn:anyWarn(pirep)}"><el:img src="warning.png" caption="Flight Report Warning" /></c:if>
<c:if test="${fn:isPromoLeg(pirep)}"><el:img src="promote.png" caption="Counts for Promotion in the ${fn:promoEQTypes(pirep)}" /></c:if></td>
 <td><el:cmd className="bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd></td>
 <td>${pilot.name}</td>
 <td class="small nophone"><fmt:text value="${pirep.airportD.name} - ${pirep.airportA.name}" /></td>
 <td class="sec">${pirep.equipmentType}</td>
  <td class="nophone ter small">${pirep.simulator}</td>
 <td class="nophone"><fmt:duration duration="${(pirep.length > 0) ? pirep.duration : null}" t="HH:mm"  default="-" /></td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="8"><view:scrollbar force="${doScroll}"><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="120" labels="Draft,Submitted,Held,Approved,Rejected,Check Ride,Flight Academy" classes="opt2,opt1,warn, ,err,opt3,opt4" /></td>
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
