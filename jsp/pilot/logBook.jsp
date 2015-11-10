<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Logbook for ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:authUser var="user">
<content:attr attr="showCSV" value="true" roles="HR,PIREP" />
<c:set var="showCSV" value="${showCSV || (user.ID == pilot.ID)}" scope="page" /></content:authUser> 

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="logbook.do" method="post" validate="return true">
<view:table cmd="logbook">
<!-- Title Header Bar -->
<tr class="title">
<c:set var="cspan" value="${access.canPreApprove ? 4 : 6}" scope="page" />
 <td colspan="${cspan}" class="caps left"><span class="nophone">PILOT LOGBOOK FOR </span>${pilot.rank.name} ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if>
<c:if test="${showCSV}"><span class="nophone"> - <a href="mylogbook.ws?id=${pilot.hexID}">CSV Download</a></span></c:if></td>
<c:if test="${access.canPreApprove}">
 <td colspan="2"><el:cmd url="preapprove" link="${pilot}" className="title">PRE-APPROVE FLIGHT</el:cmd></td>
</c:if>
</tr>

<!-- Sort/Filter Options -->
<c:if test="${!empty viewContext.results || !empty airports}">
<tr class="title">
 <td colspan="2">AIRCRAFT <el:combo name="eqType" size="1" idx="*" options="${eqTypes}" value="${param.eqType}" firstEntry="-" /></td>
 <td><el:cmd url="logcalendar" link="${pilot}">CALENDAR</el:cmd></td>
 <td colspan="3" class="right">FROM <el:combo name="airportD" size="1" idx="*" options="${airports}" value="${param.airportD}" firstEntry="-" /> TO
 <el:combo name="airportA" size="1" idx="*" options="${airports}" value="${param.airportA}" firstEntry="-" /> SORT BY
 <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" />
 <el:button type="submit" label="FILTER" /></td>
</tr>
</c:if>

<!-- Table Header Bar-->
<tr class="title">
 <td style="width:10%">DATE</td>
 <td class="nophone" style="width:10%">INFO</td>
 <td style="width:15%">FLIGHT NUMBER</td>
 <td class="nophone" style="width:40%">AIRPORT NAMES</td>
 <td style="width:10%">EQUIPMENT</td>
 <td class="nophone">DURATION</td>
</tr>

<!-- Table Flight Report Data -->
<c:forEach var="pirep" items="${viewContext.results}">
<view:row entry="${pirep}">
 <td class="title"><fmt:date date="${pirep.date}" fmt="d" default="-" /></td>
 <td class="nophone"><c:if test="${fn:EventID(pirep) != 0}"><el:img src="network/event.png" caption="Online Event" /></c:if> 
<c:if test="${fn:isACARS(pirep)}"><el:img src="acars.png" caption="ACARS Logged" /></c:if>
<c:if test="${fn:isCheckFlight(pirep)}"><el:img src="checkride.png" caption="Check Ride" /></c:if>
<c:if test="${fn:isOnline(pirep)}"><el:img src="network/icon_${fn:lower(fn:network(pirep))}.png" caption="Online Flight on ${fn:network(pirep)}" /></c:if>
<c:if test="${fn:isDispatch(pirep)}"><el:img src="dispatch.png" caption="ACARS Dispatch Services" /></c:if>
<c:if test="${fn:isPromoLeg(pirep)}"><el:img src="promote.png" caption="Counts for Promotion in the ${fn:promoEQTypes(pirep)}" /></c:if></td>
 <td><el:cmd className="bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd></td>
 <td class="small nophone">${pirep.airportD.name} (<fmt:airport airport="${pirep.airportD}" />) - 
 ${pirep.airportA.name} (<fmt:airport airport="${pirep.airportA}" />)</td>
 <td class="sec">${pirep.equipmentType}</td>
 <td class="nophone"><fmt:dec fmt="#0.0" value="${pirep.length / 10}" /> hours</td>
</view:row>
<c:if test="${(comments && (!empty pirep.remarks))}">
<view:row entry="${pirep}">
 <td colspan="6" class="left">${pirep.remarks}</td>
</view:row>
</c:if>
</c:forEach>
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="120" labels="Draft,Submitted,Held,Approved,Rejected,Check Ride,Flight Academy" classes="opt2,opt1,warn, ,err,opt3,opt4" /></td>
</tr>
</view:table>
<el:text name="id" type="hidden" value="${pilot.hexID}" readOnly="true" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
