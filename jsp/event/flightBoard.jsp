<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> ${netInfo.network} Online Flights</title>
<content:css name="main" />
<content:css name="view" />
<content:css name="form" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.setNetwork = function(combo) {
	location.href = '/flightboard.do?id=' + escape(golgotha.form.getCombo(combo));
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="flightboard.do" method="get" validate="return false">
<view:table cmd="flightboard">
<tr class="title">
 <td colspan="4" class="left">ONLINE PILOTS - ${netInfo.network} - VALID AS OF <fmt:date date="${netInfo.validDate}" /></td>
 <td><el:cmd url="flightboardmap" linkID="${network}">FLIGHT MAP</el:cmd></td>
 <td colspan="2" class="right">SELECT NETWORK <el:combo name="ID" size="1" idx="1" onChange="void golgotha.local.setNetwork(this)" options="${networks}" value="${network}" /></td>
</tr>

<!-- Pilot Title Bar -->
<tr class="title caps">
 <td style="width:10%">CALLSIGN</td>
 <td style="width:10%">${netInfo.network} ID</td>
 <td style="width:20%">PILOT NAME</td>
 <td class="nophone" style="width:25%">CURRENTLY FLYING</td>
 <td class="nophone" style="width:10%">EQUIPMENT</td>
 <td style="width:10%">ALTITUDE</td>
 <td>GROUND SPEED</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="pilot" items="${netInfo.pilots}">
<view:row entry="${pilot}">
 <td class="pri bld">${pilot.callsign}</td>
 <td>${pilot.ID}</td>
 <td class="bld">${pilot.name}</td>
 <td class="small nophone">${pilot.airportD.name} - ${pilot.airportA.name}</td>
 <td class="sec nophone">${pilot.equipmentCode}</td>
 <td class="small bld"><fmt:int fmt="##,###" value="${pilot.altitude}" /> feet</td>
 <td class="small bld">${pilot.groundSpeed} knots</td>
</view:row>
</c:forEach>
<tr class="title left caps">
 <td colspan="7">ONLINE CONTROLLERS - ${netInfo.network}</td>
</tr>

<!-- Table Controller Data -->
<c:forEach var="ctr" items="${netInfo.controllers}">
<view:row entry="${ctr}">
 <td class="pri">${ctr.callsign}</td>
 <td>${ctr.ID}</td>
 <td class="bld">${ctr.name}</td>
 <td class="sec">${ctr.facility.name}</td>
 <td class="bld">${ctr.frequency}</td>
 <td class="nophone" colspan="2">${ctr.rating.name} (${ctr.rating})</td>
</view:row>
</c:forEach>
</view:table>
</el:form>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
