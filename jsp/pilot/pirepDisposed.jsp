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
<title><content:airline /> Disposed Flight Reports</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:pics />
<content:favicon />
<script async>
golgotha.local.sort = function() { return document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:enum var="statuses" className="org.deltava.beans.flight.FlightStatus" exclude="DRAFT,SUBMITTED" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="disposedpireps.do" method="post" validate="return false">
<view:table cmd="disposedpireps">
<!-- Table Header Bar-->
<tr class="title">
 <td colspan="3" class="left caps"><span class="nophone"><content:airline /> RECENTLY DISPOSED </span>FLIGHT REPORTS</td>
 <td colspan="4" class="right"><el:combo name="status" size="1" idx="*" options="${statuses}" value="${status}"  onChange="void golgotha.local.sort()" /> SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void golgotha.local.sort()" /></td>
</tr>
<tr class="title caps">
 <td style="width:10%">DATE</td>
 <td class="nophone" style="width:12%">INFO</td>
 <td style="width:12%">FLIGHT NUMBER</td>
 <td style="width:15%">PILOT NAME</td>
 <td class="nophone" style="width:25%">AIRPORTS</td>
 <td>EQUIPMENT</td>
 <td style="width:15%">${status.description} BY</td>
</tr>

<!-- Table Flight Report Data -->
<c:forEach var="pirep" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[fn:PilotID(pirep)]}" scope="page" />
<c:set var="disposedBy" value="${pilots[fn:DisposalID(pirep)]}" scope="page" />
<tr>
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
 <td class="small nophone">${pirep.airportD.name} (<el:cmd url="airportinfo" linkID="${pirep.airportD.IATA}" className="plain" authOnly="true"><fmt:airport airport="${pirep.airportD}" /></el:cmd>) - 
 ${pirep.airportA.name} (<el:cmd url="airportinfo" linkID="${pirep.airportA.IATA}" className="plain" authOnly="true"><fmt:airport airport="${pirep.airportA}" /></el:cmd>)</td>
 <td class="sec">${pirep.equipmentType}</td>
 <td class="pri">${disposedBy.name}</td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
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
