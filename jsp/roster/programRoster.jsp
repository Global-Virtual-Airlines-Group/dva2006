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
<title><content:airline /> Program Roster - ${eqType.name}</title>
<content:css name="main" />
<content:css name="form" />
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
<content:enum var="ranks" className="org.deltava.beans.Rank" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="prgroster.do" method="post" validate="return true">
<view:table cmd="prgroster">
<tr class="title">
 <td colspan="3" class="left caps"><content:airline /> ${eqtype.name} PROGRAM METRICS</td>
 <td colspan="4" class="right">SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${param.sortType}" />
<span class="nophone"><content:filter roles="HR">
 PROGRAM <el:combo name="eqType" size="1" idx="*" options="${eqTypes}" value="${eqType.name}" />
 RANK <el:combo name="rank" size="1" idx="*" options="${ranks}" firstEntry="All Pilots" value="${param.rank}" /></content:filter>
 <el:box name="isDesc" idx="*" value="true" label="Descending" checked="${param.isDesc}" /></span> <el:button type="submit" label="UPDATE" /></td>
</tr>
<tr class="title">
 <td colspan="7" class="left caps">PROGRAM ROSTER - <fmt:int value="${viewContext.start+1}" /> TO <fmt:int value="${viewContext.end}" /> OF
 <fmt:int value="${eqType.size}" /> PILOTS - <span class="und" onclick="void golgotha.util.toggleExpand(this, 'prgRoster')">COLLAPSE</span></td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps prgRoster">
 <td style="width:10%">PILOT CODE</td>
 <td style="max-width:25%">PILOT NAME</td>
 <td style="width:12%">RANK</td>
 <td>TOTAL</td>
 <td class="nophone" style="width:15%">ACARS</td>
 <td class="nophone" style="width:15%">ONLINE</td>
 <td>LAST FLIGHT</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="pilot" items="${viewContext.results}">
<tr class="prgRoster">
 <td><el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.pilotCode}</el:cmd></td>
 <td>${pilot.name}</td>
 <td class="sec bld">${pilot.rank.name}</td>
 <td class="small"><fmt:int value="${pilot.legs}" /> legs, <fmt:dec value="${pilot.hours}" /> hours</td>
 <td class="pri small nophone"><fmt:int value="${pilot.ACARSLegs}" /> legs, <fmt:dec value="${pilot.ACARSHours}" /> hours</td>
 <td class="sec small nophone"><fmt:int value="${pilot.onlineLegs}" /> legs, <fmt:dec value="${pilot.onlineHours}" /> hours</td>
 <td class="pri bld"><fmt:date fmt="d" date="${pilot.lastFlight}" default="-" /></td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title prgRoster">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<c:if test="${!empty examQueue}">
<el:table className="view">
<tr class="title">
 <td colspan="6" class="left caps">SUBMITTED EXAMINATIONS - <fmt:int value="${fn:sizeof(examQueue)}" /> EXAMS
 - <span class="und" onclick="void golgotha.util.toggleExpand(this, 'prgExamQueue')">COLLAPSE</span></td>
</tr>
<!-- Table Header Bar -->
<tr class="title caps prgExamQueue">
 <td style="width:25%">EXAMINATION NAME</td>
 <td style="width:20%">PILOT NAME</td>
 <td style="width:20%">RANK / EQUIPMENT</td>
 <td class="nophone" style="width:15%">CREATED ON</td>
 <td class="nophone" style="width:10%">QUESTIONS</td>
 <td class="nophone">STAGE</td>
</tr>

<!-- Table Data -->
<c:forEach var="exam" items="${examQueue}">
<c:set var="pilot" value="${pilots[exam.authorID]}" scope="page" />
<tr class="prgExamQueue">
 <td class="pri bld"><el:cmd url="exam" link="${exam}">${exam.name}</el:cmd></td>
 <td class="bld"><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td>${pilot.rank.name}, ${pilot.equipmentType}</td>
 <td class="sec nophone"><fmt:date t="HH:mm" date="${exam.date}" /></td>
 <td class="nophone"><fmt:int value="${exam.size}" /></td>
 <td class="sec nophone"><fmt:int value="${exam.stage}" /></td>
</tr>
</c:forEach>
</el:table>
</c:if>
<c:if test="${!empty crQueue}">
<el:table className="view">
<tr class="title">
 <td colspan="5" class="left caps">SUBMITTED CHECK RIDES - <fmt:int value="${fn:sizeof(crQueue)}" /> CHECK RIDES
 - <span class="und" onclick="void golgotha.util.toggleExpand(this, 'prgRideQueue')">COLLAPSE</span></td>
</tr>
<!-- Table Header Bar -->
<tr class="title caps prgRideQueue">
 <td style="width:8%">DATE</td>
 <td style="width:7%">&nbsp;</td>
 <td style="width:20%">PILOT NAME</td>
 <td style="width:10%">AIRCRAFT</td>
 <td class="left nophone">COMMENTS</td>
</tr>

<!-- Table View data -->
<c:forEach var="ride" items="${crQueue}">
<c:set var="pilot" value="${pilots[ride.authorID]}" scope="page" />
<tr class="prgRideQueue">
 <td><el:cmd url="checkride" link="${ride}"><fmt:date date="${ride.submittedOn}" fmt="d" /></el:cmd></td>
<c:if test="${ride.flightID > 0}">
 <td><el:cmd url="crview" link="${ride}" className="pri small bld">SCORE</el:cmd></td>
</c:if>
<c:if test="${ride.flightID == 0}">
 <td class="small">INCOMPLETE</td>
</c:if>
 <td><el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.name}</el:cmd></td>
 <td class="sec">${ride.aircraftType}</td>
 <td class="small left nophone">${ride.comments}</td>
</tr>
</c:forEach>
</el:table>
</c:if>
<c:if test="${!empty txQueue}">
<view:table cmd="prgroster">
<tr class="title">
 <td colspan="5" class="left caps">${eqType.name} TRANSFER REQUESTS - <fmt:int value="${fn:sizeof(txQueue)}" /> TRANSFERS
 - <span class="und" onclick="void golgotha.util.toggleExpand(this, 'prgTxQueue')">COLLAPSE</span></td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps prgTxQueue">
 <td style="width:35%"><el:cmd url="txrequests" className="title" sort="P.LASTNAME">PILOT NAME</el:cmd></td>
 <td style="width:10%">PILOT ID</td>
 <td class="nophone" style="width:20%">CURRENT RANK</td>
 <td class="nophone" style="width:15%">CURRENT PROGRAM</td>
 <td>REQUESTED ON</td>
</tr>

<!-- Table Data -->
<c:forEach var="txreq" items="${txQueue}">
<c:set var="pilot" value="${pilots[txreq.ID]}" scope="page" />
<view:row entry="${txreq}" className="prgTxQueue">
 <td class="bld"><el:cmd url="txreqview" link="${txreq}">${pilot.name}</el:cmd></td>
 <td class="pri bld"><el:cmd url="profile" link="${pilot}">${pilot.pilotCode}</el:cmd></td>
 <td class="sec bld nophone">${pilot.rank.name}</td>
 <td class="nophone">${pilot.equipmentType}</td>
 <td class="sec"><fmt:date fmt="d" date="${txreq.date}" /></td>
</view:row>
</c:forEach>

<!-- Legend -->
<tr class="title prgTxQueue">
 <td colspan="5"><view:legend width="150" labels="Needs Check Ride,Ride Assigned,Ride Submitted,Complete" classes="opt2,opt1,opt3, " /></td>
</tr>
</view:table>
</c:if>
<c:if test="${!empty promoQueue}">
<view:table cmd="prgroster">
<tr class="title">
 <td colspan="7" class="left caps">${eqType.name} PROMOTION QUEUE - <fmt:int value="${fn:sizeof(promoQueue)}" /> PILOTS
 - <span class="und" onclick="void golgotha.util.toggleExpand(this, 'prgPromoQueue')">COLLAPSE</span></td>
</tr>
<!-- Table Header Bar -->
<tr class="title caps prgPromoQueue">
 <td style="width:15%">&nbsp;</td>
 <td style="width:10%">PILOT CODE</td>
 <td style="width:30%">PILOT NAME</td>
 <td style="width:10%">TOTAL</td>
 <td class="nophone" >ACARS</td>
 <td class="nophone" >ONLINE</td>
 <td>LAST FLIGHT</td>
</tr>

<!-- Table Data -->
<c:forEach var="pilot" items="${promoQueue}">
<c:set var="access" value="${promoAccess[pilot.ID]}" scope="page" />
<view:row entry="${pilot}" className="prgPromoQueue">
<c:if test="${access.canPromote}">
 <td><el:cmdbutton url="promote" link="${pilot}" label="PROMOTE" /></td>
</c:if>
<c:if test="${!access.canPromote}">
 <td>&nbsp;</td>
</c:if>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><el:cmd url="profile" link="${pilot}" className="bld">${pilot.name}</el:cmd></td>
 <td class="small"><fmt:int value="${pilot.legs}" /> legs, <fmt:dec value="${pilot.hours}" /> hours</td>
 <td class="pri small nophone"><fmt:int value="${pilot.ACARSLegs}" /> legs, <fmt:dec value="${pilot.ACARSHours}" /> hours</td>
 <td class="sec small nophone"><fmt:int value="${pilot.onlineLegs}" /> legs, <fmt:dec value="${pilot.onlineHours}" /> hours</td>
 <td><fmt:date fmt="d" date="${pilot.lastFlight}" default="-" /></td>
</view:row>
</c:forEach>
</view:table>
</c:if>
<c:if test="${!empty crStats}">
<!-- Check Ride Statistics -->
<view:table cmd="prgroster">
<tr class="title caps">
 <td class="left" colspan="7">CHECK RIDE STATISTICS - <span class="und" onclick="void golgotha.util.toggleExpand(this, 'crStats')">COLLAPSE</span></td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps crStats">
 <td style="width:20%">MONTH</td>
 <td style="width:20%">SCORER</td>
 <td style="width:10%">PASSED</td>
 <td style="width:10%">TOTAL</td>
 <td style="width:15%">PASS RATE</td>
 <td class="nophone" >PILOTS</td>
 <td>AVG. TRIES</td>
</tr>

<!-- Table Statistics Data -->
<c:forEach var="crStat" items="${crStats}">
<tr class="crStats">
 <td class="pri bld">${crStat.label}</td>
 <td class="bld">${crStat.subLabel}</td>
 <td class="sec bld"><fmt:int value="${crStat.passed}" /></td>
 <td><fmt:int value="${crStat.total}" /></td>
 <td class="pri bld"><fmt:dec value="${crStat.passed * 100.0 / crStat.total}" />%</td>
 <td class="sec nophone"><fmt:int value="${crStat.users}" /></td>
 <td class="bld"><fmt:dec value="${crStat.total / crStat.users}" /></td>
</tr>
</c:forEach>
</view:table>
</c:if>
<c:if test="${!empty pirepStats}">
<!-- Flight Report Statistics -->
<el:table className="view">
<tr class="title caps">
 <td class="left" colspan="10">FLIGHT REPORT STATISTICS (PAST <fmt:int value="${flightStatsInterval}" /> DAYS)
 - <span class="und" onclick="void golgotha.util.toggleExpand(this, 'frStats')">COLLAPSE</span></td>
</tr>
<!-- Table Header Bar-->
<tr class="title caps frStats">
 <td style="width:5%">#</td>
 <td style="width:20%">ENTRY</td>
 <td style="width:8%">HOURS</td>
 <td style="width:7%">LEGS</td>
 <td style="width:10%">ACARS</td>
 <td style="width:9%">ONLINE</td>
 <td class="nophone" style="width:9%">HISTORIC</td>
 <td class="nophone" style="width:9%">DISPATCH</td>
 <td class="nophone" style="width:9%">${hasPilotID ? 'PILOTS' : 'DISTANCE'}</td>
 <td class="nophone" >AVERAGE</td>
</tr>

<!-- Table Statistics Data -->
<c:set var="entryNumber" value="0" scope="page" />
<c:forEach var="stat" items="${pirepStats}">
<view:row entry="${stat}" className="frStats">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
 <td class="sec bld small">${entryNumber}</td>
 <td class="pri bld">${stat.label}</td>
 <td class="bld"><fmt:dec value="${stat.hours}" /></td>
 <td class="pri bld"><fmt:int value="${stat.legs}" /></td>
 <td class="sec bld small"><fmt:int value="${stat.ACARSLegs}" /> (<fmt:dec value="${stat.ACARSPercent * 100}" fmt="##0.0" />%)</td>
 <td class="small"><fmt:int value="${stat.onlineLegs}" /> (<fmt:dec value="${(stat.onlineLegs * 100.0) / stat.legs}" fmt="##0.0" />%)</td>
 <td class="sec small nophone"><fmt:int value="${stat.historicLegs}" /> (<fmt:dec value="${(stat.historicLegs * 100.0) / stat.legs}" fmt="##0.0" />%)</td>
 <td class="bld small nophone"><fmt:int value="${stat.dispatchLegs}" /> (<fmt:dec value="${(stat.dispatchLegs * 100.0) / stat.legs}" fmt="##0.0" />%)</td>
<c:if test="${hasPilotID}">
 <td class="small nophone"><fmt:int value="${stat.pilotIDs}" /></td>
</c:if>
<c:if test="${!hasPilotID}">
 <td class="small nophone"><fmt:distance value="${stat.distance}" /></td>
</c:if>
 <td class="small nophone"><fmt:dec value="${stat.avgHours}" fmt="#,##0.00" /> hours, <fmt:distance value="${stat.avgDistance}" /></td>
</view:row>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="10">&nbsp;</td>
</tr>
</el:table>
</c:if>

<!-- Membership Statistics -->
<el:table className="form nophone">
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
 <td class="label">${rnk.name}</td>
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
