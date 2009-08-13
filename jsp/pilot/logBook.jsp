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
<title><content:airline /> Logbook for ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="logbook.do" method="post" validate="return true">
<view:table className="view" pad="default" space="default" cmd="logbook">
<!-- Title Header Bar -->
<tr class="title">
<c:if test="${access.canPreApprove}">
 <td colspan="4" class="caps left">PILOT LOGBOOK FOR ${pilot.rank} ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></td>
 <td colspan="2"><el:cmd url="preapprove" link="${pilot}" className="title">PRE-APPROVE FLIGHT</el:cmd></td>
</c:if>
<c:if test="${!access.canPreApprove}">
 <td colspan="6" class="caps left">PILOT LOGBOOK FOR ${pilot.rank} ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></td>
</c:if>
</tr>

<!-- Sort/Filter Options -->
<c:if test="${!empty viewContext.results}">
<tr class="title">
 <td colspan="2">AIRCRAFT <el:combo name="eqType" size="1" idx="*" options="${eqTypes}" value="${param.eqType}" firstEntry="-" /></td>
 <td colspan="4" class="right">FROM <el:combo name="airportD" size="1" idx="*" options="${airports}" value="${param.airportD}" firstEntry="-" /> TO
 <el:combo name="airportA" size="1" idx="*" options="${airports}" value="${param.airportA}" firstEntry="-" /> SORT BY
 <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" />
 <el:button type="submit" className="BUTTON" label="FILTER" /></td>
</tr>
</c:if>

<!-- Table Header Bar-->
<tr class="title">
 <td width="10%">DATE</td>
 <td width="10%">INFO</td>
 <td width="15%">FLIGHT NUMBER</td>
 <td width="40%">AIRPORT NAMES</td>
 <td width="10%">EQUIPMENT</td>
 <td>DURATION</td>
</tr>

<!-- Table Flight Report Data -->
<c:forEach var="pirep" items="${viewContext.results}">
<view:row entry="${pirep}">
 <td class="title"><fmt:date date="${pirep.date}" fmt="d" default="-" /></td>
 <td><c:if test="${fn:EventID(pirep) != 0}"><el:img src="network/event.png" caption="Online Event" /></c:if> 
<c:if test="${fn:isACARS(pirep)}"><el:img src="acars.png" caption="ACARS Logged" /></c:if> 
<c:if test="${fn:isCheckFlight(pirep)}"><el:img src="checkride.png" caption="Check Ride" /></c:if> 
<c:if test="${fn:isOnline(pirep)}"><el:img src="network/online.png" caption="Online Flight on ${fn:network(pirep)}" /></c:if>
<c:if test="${fn:isPromoLeg(pirep)}"><el:img src="promote.png" caption="Counts for Promotion in the ${fn:promoEQTypes(pirep)}" /></c:if></td>
 <td><el:cmd className="bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd></td>
 <td class="small">${pirep.airportD.name} (<fmt:airport airport="${pirep.airportD}" />) - 
 ${pirep.airportA.name} (<fmt:airport airport="${pirep.airportA}" />)</td>
 <td class="sec">${pirep.equipmentType}</td>
 <td><fmt:dec fmt="#0.0" value="${pirep.length / 10}" /> hours</td>
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
