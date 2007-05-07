<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
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
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="pirepqueue">
<!-- Table Header Bar-->
<tr class="title">
 <td width="10%">DATE</td>
 <td width="10%">INFO</td>
 <td width="15%">FLIGHT NUMBER</td>
 <td width="15%">PILOT NAME</td>
 <td width="25%">AIRPORTS</td>
 <td width="10%">EQUIPMENT</td>
 <td>DURATION</td>
</tr>

<!-- Table Flight Report Data -->
<c:set var="entryNumber" value="0" scope="request" />
<c:forEach var="pirep" items="${viewContext.results}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="request" />

<view:row entry="${pirep}">
 <td><fmt:date fmt="d" date="${pirep.date}" /></td>
 <td><c:if test="${fn:EventID(pirep) != 0}"><el:img src="network/event.png" caption="Online Event" /></c:if> 
<c:if test="${fn:isACARS(pirep)}"><el:img src="acars.png" caption="ACARS Logged" /></c:if> 
<c:if test="${fn:isCheckFlight(pirep)}"><el:img src="checkride.png" caption="Check Ride" /></c:if> 
<c:if test="${fn:isOnline(pirep)}"><el:img src="network/online.png" caption="Online Flight on ${fn:network(pirep)}" /></c:if>
<c:if test="${fn:isPromoLeg(pirep)}"><el:img src="promote.png" caption="Counts for Promotion in the ${fn:promoEQTypes(pirep)}" /></c:if></td>
 <td><el:cmd className="bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd></td>
 <td class="small">${pirep.firstName} ${pirep.lastName}</td>
 <td class="small">${pirep.airportD.name} - ${pirep.airportA.name}</td>
 <td class="sec">${pirep.equipmentType}</td>
 <td><fmt:dec fmt="#0.0" value="${pirep.length / 10}" /> hours</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="100" labels="Draft,Submitted,Held,Approved,Rejected,Check Ride" classes="opt2,opt1,warn, ,err,opt3" /></td>
</tr>
</view:table>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
