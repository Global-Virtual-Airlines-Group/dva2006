<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline />&nbsp;${netInfo.network} Online Flights</title>
<content:css name="main" />
<content:css name="view" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script async>
golgotha.local.setNetwork = function(combo) {
	location.href = '/flightboard.do?id=' + encodeURI(golgotha.form.getCombo(combo));
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
 <td colspan="4" class="left">${netInfo.network} ONLINE DATA<span class="nophone"> - VALID AS OF <fmt:date date="${netInfo.validDate}" /></span></td>
 <td><el:cmd url="flightboardmap" linkID="${network}">FLIGHT MAP</el:cmd></td>
 <td colspan="2" class="right">NETWORK <el:combo name="ID" size="1" idx="1" onChange="void golgotha.local.setNetwork(this)" options="${networks}" value="${network}" /></td>
</tr>

<!-- Pilot Data Header -->
<tr class="title caps">
 <td class="left" colspan="7"><fmt:int value="${netInfo.pilots.size()}" /> ONLINE PILOTS - ${netInfo.network}<span id="ctrToggle" class="und" style="float:right;" onclick="void golgotha.util.toggleExpand(this, 'pilot')">COLLAPSE</span></td>
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
<view:row entry="${pilot}" className="pilot">
 <td class="pri bld">${pilot.callsign}</td>
 <td>${pilot.ID}</td>
 <td class="bld">${pilot.name}</td>
 <td class="small nophone">${pilot.airportD.name} - ${pilot.airportA.name}</td>
 <td class="sec nophone">${pilot.equipmentCode}</td>
 <td class="small bld"><fmt:int fmt="##,###" value="${pilot.altitude}" /> feet</td>
 <td class="small bld">${pilot.groundSpeed} knots</td>
</view:row>
</c:forEach>

<!-- Controller Data Header -->
<tr class="title caps">
 <td class="left" colspan="7"><fmt:int value="${netInfo.controllers.size()}" /> ONLINE CONTROLLERS - ${netInfo.network}<span id="ctrToggle" class="und" style="float:right;" onclick="void golgotha.util.toggleExpand(this, 'ctr')">COLLAPSE</span></td>
</tr>

<!-- Table Controller Data -->
<c:forEach var="ctr" items="${netInfo.controllers}">
<c:set var="freqs" value="${ctr.frequencies}" scope="page" />
<view:row entry="${ctr}" className="ctr">
 <td class="pri">${ctr.callsign}</td>
 <td>${ctr.ID}</td>
 <td class="bld">${ctr.name}</td>
 <td class="sec">${ctr.facility.name}</td>
<c:choose>
<c:when test="${freqs.isEmpty()}">
 <td class="ter bld">NOT SET</td>
</c:when>
<c:when test="${(freqs.size() > 1) && !ctr.isObserver()}">
 <td class="bld" title="<fmt:list value="${freqs}" delim=", " />">${ctr.frequency} + <fmt:int value="${freqs.size() - 1}" /></td>
</c:when>
<c:otherwise>
 <td class="bld">${ctr.frequency}</td>
</c:otherwise>
</c:choose>
 <td class="nophone" colspan="2">${ctr.rating.name} (${ctr.rating})</td>
</view:row>
</c:forEach>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
