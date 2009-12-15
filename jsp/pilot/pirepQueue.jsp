<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Report Queue</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function sort()
{
document.forms[0].submit();
return true;
}
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
<view:table className="view" pad="default" space="default" cmd="pirepqueue">
<!-- Table Header Bar-->
<tr class="title">
 <td colspan="4" class="left caps"><content:airline /> PENDING FLIGHT REPORT QUEUE</td>
 <td colspan="3" class="right">SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void sort()" /></td>
</tr>
<tr class="title">
 <td width="10%">DATE</td>
 <td width="10%">INFO</td>
 <td width="15%">FLIGHT NUMBER</td>
 <td width="15%">PILOT NAME</td>
 <td width="30%">AIRPORTS</td>
 <td width="10%">EQUIPMENT</td>
 <td>DURATION</td>
</tr>
<c:if test="${!empty myHeld}">
<!-- Flight Reports held by ${user.name} -->
<tr class="title">
 <td colspan="7" class="left caps"><fmt:int value="${fn:sizeof(myHeld)}" /> FLIGHT REPORTS HELD BY ${user.name}</td>
</tr>
<c:forEach var="pirep" items="${myHeld}">
<c:set var="pilot" value="${pilots[fn:PilotID(pirep)]}" scope="page" />
<view:row entry="${pirep}">
 <td><fmt:date fmt="d" date="${pirep.date}" /></td>
 <td><c:if test="${fn:EventID(pirep) != 0}"><el:img src="network/event.png" caption="Online Event" /></c:if> 
<c:if test="${fn:isACARS(pirep)}"><el:img src="acars.png" caption="ACARS Logged" /></c:if> 
<c:if test="${fn:isCheckFlight(pirep)}"><el:img src="checkride.png" caption="Check Ride" /></c:if> 
<c:if test="${fn:isOnline(pirep)}"><el:img src="network/online.png" caption="Online Flight on ${fn:network(pirep)}" /></c:if>
<c:if test="${fn:isPromoLeg(pirep)}"><el:img src="promote.png" caption="Counts for Promotion in the ${fn:promoEQTypes(pirep)}" /></c:if></td>
 <td><el:cmd className="bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd></td>
 <td class="small">${pilot.name}</td>
 <td class="small">${pirep.airportD.name} - ${pirep.airportA.name}</td>
 <td class="sec">${pirep.equipmentType}</td>
 <td><fmt:dec fmt="#0.0" value="${pirep.length / 10}" /> hours</td>
</view:row>
</c:forEach>
</c:if>
<c:if test="${!empty myEQType}">
<!-- Flight Reports in the ${myEQ.name} program -->
<tr class="title">
 <td colspan="7" class="left caps"><fmt:int value="${fn:sizeof(myEQType)}" /> FLIGHT REPORTS FOR THE ${myEQ.name} PROGRAM</td>
</tr>
<c:forEach var="pirep" items="${myEQType}">
<c:set var="pilot" value="${pilots[fn:PilotID(pirep)]}" scope="page" />
<view:row entry="${pirep}">
 <td><fmt:date fmt="d" date="${pirep.date}" /></td>
 <td><c:if test="${fn:EventID(pirep) != 0}"><el:img src="network/event.png" caption="Online Event" /></c:if> 
<c:if test="${fn:isACARS(pirep)}"><el:img src="acars.png" caption="ACARS Logged" /></c:if> 
<c:if test="${fn:isCheckFlight(pirep)}"><el:img src="checkride.png" caption="Check Ride" /></c:if> 
<c:if test="${fn:isOnline(pirep)}"><el:img src="network/online.png" caption="Online Flight on ${fn:network(pirep)}" /></c:if>
<c:if test="${fn:isPromoLeg(pirep)}"><el:img src="promote.png" caption="Counts for Promotion in the ${fn:promoEQTypes(pirep)}" /></c:if></td>
 <td><el:cmd className="bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd></td>
 <td class="small">${pilot.name}</td>
 <td class="small">${pirep.airportD.name} - ${pirep.airportA.name}</td>
 <td class="sec">${pirep.equipmentType}</td>
 <td><fmt:dec fmt="#0.0" value="${pirep.length / 10}" /> hours</td>
</view:row>
</c:forEach>
</c:if>
<c:if test="${subLists}">
<tr class="title">
 <td colspan="7" class="left caps"><fmt:int value="${fn:sizeof(viewContext.results)}" /> PENDING FLIGHT REPORTS</td>
</tr>
</c:if>
<!-- Table Flight Report Data -->
<c:forEach var="pirep" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[fn:PilotID(pirep)]}" scope="page" />
<view:row entry="${pirep}">
 <td><fmt:date fmt="d" date="${pirep.date}" /></td>
 <td><c:if test="${fn:EventID(pirep) != 0}"><el:img src="network/event.png" caption="Online Event" /></c:if> 
<c:if test="${fn:isACARS(pirep)}"><el:img src="acars.png" caption="ACARS Logged" /></c:if> 
<c:if test="${fn:isCheckFlight(pirep)}"><el:img src="checkride.png" caption="Check Ride" /></c:if> 
<c:if test="${fn:isOnline(pirep)}"><el:img src="network/online.png" caption="Online Flight on ${fn:network(pirep)}" /></c:if>
<c:if test="${fn:isPromoLeg(pirep)}"><el:img src="promote.png" caption="Counts for Promotion in the ${fn:promoEQTypes(pirep)}" /></c:if></td>
 <td><el:cmd className="bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd></td>
<td class="small">${pilot.name}</td>
 <td class="small"><fmt:text value="${pirep.airportD.name} - ${pirep.airportA.name}" /></td>
 <td class="sec">${pirep.equipmentType}</td>
 <td><fmt:dec fmt="#0.0" value="${pirep.length / 10}" /> hours</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7"><view:scrollbar force="${doScroll}"><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="120" labels="Submitted,Held,Check Ride,Flight Academy" classes="opt1,warn,opt3,opt4" /></td>
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
