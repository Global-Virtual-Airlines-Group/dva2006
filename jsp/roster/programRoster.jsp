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
<title><content:airline /> Program Roster - ${eqType.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="prgroster.do" method="post" validate="return true">
<view:table className="view" space="default" pad="default" cmd="prgroster">
<tr class="title">
 <td colspan="3" class="left caps"><content:airline /> ${eqtype.name} PROGRAM METRICS</td>
 <td colspan="4" class="right">SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${param.sortType}" />
<content:filter roles="HR">
 PROGRAM <el:combo name="eqType" size="1" idx="*" options="${eqTypes}" value="${eqType.name}" />
 RANK <el:combo name="rank" size="1" idx="*" options="${ranks}" firstEntry="All Pilots" value="${param.rank}" /></content:filter>
 <el:box name="isDesc" idx="*" value="true" label="Descending" checked="${param.isDesc}" />
 <el:button type="submit" className="BUTTON" label="UPDATE" /></td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td width="10%">PILOT CODE</td>
 <td width="18%">PILOT NAME</td>
 <td width="12%">RANK</td>
 <td width="15%">TOTAL</td>
 <td width="15%">ACARS</td>
 <td width="15%">ONLINE</td>
 <td>LAST FLIGHT</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="pilot" items="${viewContext.results}">
<tr>
 <td><el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.pilotCode}</el:cmd></td>
 <td>${pilot.name}</td>
 <td class="sec bld">${pilot.rank}</td>
 <td class="small"><fmt:int value="${pilot.legs}" /> legs, <fmt:dec value="${pilot.hours}" /> hours</td>
 <td class="pri small"><fmt:int value="${pilot.ACARSLegs}" /> legs, <fmt:dec value="${pilot.ACARSHours}" /> hours</td>
 <td class="sec small"><fmt:int value="${pilot.onlineLegs}" /> legs, <fmt:dec value="${pilot.onlineHours}" /> hours</td>
 <td class="pri bld"><fmt:date fmt="d" date="${pilot.lastFlight}" default="-" /></td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<c:if test="${!empty examQueue}">
<el:table className="view" pad="default" space="default">
<tr class="title">
 <td colspan="6" class="left caps">SUBMITTED EXAMINATIONS - <fmt:int value="${fn:sizeof(examQueue)}" /> EXAMS</td>
</tr>
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="25%">EXAMINATION NAME</td>
 <td width="20%">PILOT NAME</td>
 <td width="20%">RANK / EQUIPMENT</td>
 <td width="15%">CREATED ON</td>
 <td width="10%">QUESTIONS</td>
 <td>STAGE</td>
</tr>

<!-- Table Data -->
<c:forEach var="exam" items="${examQueue}">
<c:set var="pilot" value="${pilots[exam.pilotID]}" scope="page" />
<tr>
 <td class="pri bld"><el:cmd url="exam" link="${exam}">${exam.name}</el:cmd></td>
 <td class="bld"><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td>${pilot.rank}, ${pilot.equipmentType}</td>
 <td class="sec"><fmt:date t="HH:mm" date="${exam.date}" /></td>
 <td><fmt:int value="${exam.size}" /></td>
 <td class="sec"><fmt:int value="${exam.stage}" /></td>
</tr>
</c:forEach>
</el:table>
</c:if>
<c:if test="${!empty crQueue}">
<el:table className="view" pad="default" space="default">
<tr class="title">
 <td colspan="5" class="left caps">SUBMITTED CHECK RIDES - <fmt:int value="${fn:sizeof(crQueue)}" /> CHECK RIDES</td>
</tr>
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="8%">DATE</td>
 <td width="7%">&nbsp;</td>
 <td width="20%">PILOT NAME</td>
 <td width="10%">AIRCRAFT</td>
 <td class="left">COMMENTS</td>
</tr>

<!-- Table View data -->
<c:forEach var="ride" items="${crQueue}">
<c:set var="pilot" value="${pilots[ride.pilotID]}" scope="page" />
<tr>
 <td><el:cmd url="checkride" link="${ride}"><fmt:date date="${ride.submittedOn}" fmt="d" /></el:cmd></td>
<c:if test="${ride.flightID > 0}">
 <td><el:cmd url="crview" link="${ride}" className="pri small bld">SCORE</el:cmd></td>
</c:if>
<c:if test="${ride.flightID == 0}">
 <td class="small">INCOMPLETE</td>
</c:if>
 <td><el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.name}</el:cmd></td>
 <td class="sec">${ride.aircraftType}</td>
 <td class="small left">${ride.comments}</td>
</tr>
</c:forEach>
</el:table>
</c:if>
<c:if test="${!empty txQueue}">
<view:table className="view" pad="default" space="default" cmd="prgroster">
<tr class="title">
 <td colspan="5" class="left caps">${eqType.name} TRANSFER REQUESTS - <fmt:int value="${fn:sizeof(txQueue)}" /> TRANSFERS</td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td width="35%"><el:cmd url="txrequests" className="title" sort="P.LASTNAME">PILOT NAME</el:cmd></td>
 <td width="10%">PILOT ID</td>
 <td width="20%">CURRENT RANK</td>
 <td width="15%">CURRENT PROGRAM</td>
 <td>REQUESTED ON</td>
</tr>

<!-- Table Data -->
<c:forEach var="txreq" items="${txQueue}">
<c:set var="pilot" value="${pilots[txreq.ID]}" scope="page" />
<view:row entry="${txreq}">
 <td class="bld"><el:cmd url="txreqview" link="${txreq}">${pilot.name}</el:cmd></td>
 <td class="pri bld"><el:cmd url="profile" link="${pilot}">${pilot.pilotCode}</el:cmd></td>
 <td class="sec bld">${pilot.rank}</td>
 <td>${pilot.equipmentType}</td>
 <td class="sec"><fmt:date fmt="d" date="${txreq.date}" /></td>
</view:row>
</c:forEach>
</view:table>
</c:if>
<c:if test="${!empty promoQueue}">
<view:table className="view" pad="default" space="default" cmd="prgroster">
<tr class="title">
 <td colspan="7" class="left caps">${eqType.name} PROMOTION QUEUE - <fmt:int value="${fn:sizeof(promoQueue)}" /> PILOTS</td>
</tr>
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="15%">&nbsp;</td>
 <td width="10%">PILOT CODE</td>
 <td width="30%">PILOT NAME</td>
 <td width="10%">TOTAL</td>
 <td width="10%">ACARS</td>
 <td width="10%">ONLINE</td>
 <td>LAST FLIGHT</td>
</tr>

<!-- Table Data -->
<c:forEach var="pilot" items="${promoQueue}">
<c:set var="access" value="${promoAccess[pilot.ID]}" scope="page" />
<view:row entry="${pilot}">
<c:if test="${access.canPromote}">
 <td><el:cmdbutton url="promote" link="${pilot}" label="PROMOTE" /></td>
</c:if>
<c:if test="${!access.canPromote}">
 <td>&nbsp;</td>
</c:if>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><el:cmd url="profile" link="${pilot}" className="bld">${pilot.name}</el:cmd></td>
 <td class="small"><fmt:int value="${pilot.legs}" /> legs, <fmt:dec value="${pilot.hours}" /> hours</td>
 <td class="pri small"><fmt:int value="${pilot.ACARSLegs}" /> legs, <fmt:dec value="${pilot.ACARSHours}" /> hours</td>
 <td class="sec small"><fmt:int value="${pilot.onlineLegs}" /> legs, <fmt:dec value="${pilot.onlineHours}" /> hours</td>
 <td><fmt:date fmt="d" date="${pilot.lastFlight}" default="-" /></td>
</view:row>
</c:forEach>
</view:table>
</c:if>
<!-- Flight Report Statistics -->
<el:table className="view" pad="default" space="default">
<tr class="title caps">
 <td class="left" colspan="10">FLIGHT REPORT STATISTICS (PAST <fmt:int value="${flightStatsInterval}" /> DAYS)</td>
</tr>
<!-- Table Header Bar-->
<tr class="title caps">
 <td width="5%">#</td>
 <td width="20%">ENTRY</td>
 <td width="8%">HOURS</td>
 <td width="7%">LEGS</td>
 <td width="10%">ACARS</td>
 <td width="9%">ONLINE</td>
 <td width="9%">HISTORIC</td>
 <td width="9%">DISPATCH</td>
 <td width="9%">${hasPilotID ? 'PILOTS' : 'DISTANCE'}</td>
 <td>AVERAGE</td>
</tr>

<!-- Table Statistics Data -->
<c:set var="entryNumber" value="0" scope="page" />
<c:forEach var="stat" items="${pirepStats}">
<view:row entry="${stat}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
 <td class="sec bld small">${entryNumber}</td>
 <td class="pri bld">${stat.label}</td>
 <td class="bld"><fmt:dec value="${stat.hours}" /></td>
 <td class="pri bld"><fmt:int value="${stat.legs}" /></td>
 <td class="sec bld small"><fmt:int value="${stat.ACARSLegs}" /> (<fmt:dec value="${stat.ACARSPercent * 100}" fmt="##0.0" />%)</td>
 <td class="small"><fmt:int value="${stat.onlineLegs}" /> (<fmt:dec value="${(stat.onlineLegs * 100.0) / stat.legs}" fmt="##0.0" />%)</td>
 <td class="sec small"><fmt:int value="${stat.historicLegs}" /> (<fmt:dec value="${(stat.historicLegs * 100.0) / stat.legs}" fmt="##0.0" />%)</td>
 <td class="bld small"><fmt:int value="${stat.dispatchLegs}" /> (<fmt:dec value="${(stat.dispatchLegs * 100.0) / stat.legs}" fmt="##0.0" />%)</td>
<c:if test="${hasPilotID}">
 <td class="small"><fmt:int value="${stat.pilotIDs}" /></td>
</c:if>
<c:if test="${!hasPilotID}">
 <td class="small"><fmt:distance value="${stat.miles}" /></td>
</c:if>
 <td class="small"><fmt:dec value="${stat.avgHours}" fmt="#,##0.00" /> hours, 
<fmt:distance value="${stat.avgMiles}" /></td>
</view:row>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="10">&nbsp;</td>
</tr>
</el:table>

<!-- Membership Statistics -->
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td class="left" colspan="2">STATUS TOTALS - <fmt:int value="${metrics.size}" /> PILOTS</td>
</tr>
<c:set var="maxCount" value="${metrics.maxStatusCount}" scope="page" />
<c:forEach var="st" items="${fn:keys(metrics.statusCounts)}">
<c:set var="stCount" value="${metrics.statusCounts[st]}" scope="page" />
<tr>
 <td class="label">${st}</td>
 <td class="data"><span style="float: left; width: 96px;"><fmt:int value="${stCount}" /> pilots</span>
 <el:img y="12" x="${(stCount * 650) / maxCount}" src="cooler/bar_blue.png" caption="${stCount} Pilots" /></td>
</tr>
</c:forEach>
<tr class="title caps">
 <td class="left" colspan="2">RANK TOTALS - <fmt:int value="${metrics.size}" /> PILOTS</td>
</tr>
<c:set var="maxCount" value="${metrics.maxRankCount}" scope="page" />
<c:forEach var="rnk" items="${fn:keys(metrics.rankCounts)}">
<c:set var="rnkCount" value="${metrics.rankCounts[rnk]}" scope="page" />
<tr>
 <td class="label">${rnk}</td>
 <td class="data"><span style="float: left; width: 96px;"><fmt:int value="${rnkCount}" /> pilots</span>
 <el:img y="12" x="${(rnkCount * 650) / maxCount}" src="cooler/bar_blue.png" caption="${rnkCount} Pilots" /></td>
</tr>
</c:forEach>
<tr class="title caps">
 <td class="left" colspan="2">HIRE DATE TOTALS - <fmt:int value="${metrics.size}" /> PILOTS</td>
</tr>
<c:set var="maxCount" value="${metrics.maxHireCount}" scope="page" />
<c:forEach var="hd" items="${fn:keys(metrics.hireCounts)}">
<c:set var="hireCount" value="${metrics.hireCounts[hd]}" scope="page" />
<tr>
 <td class="label"><fmt:date fmt="d" date="${hd}" d="MMMM yyyy" /></td>
 <td class="data"><span style="float: left; width: 96px;"><fmt:int value="${hireCount}" /> pilots</span>
 <el:img y="12" x="${(hireCount * 650) / maxCount}" src="cooler/bar_blue.png" caption="${hireCount} Pilots" /></td>
</tr>
</c:forEach>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
