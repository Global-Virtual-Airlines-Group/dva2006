<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Logbook for ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></title>
<content:expire expires="10" />
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:js name="fileSaver" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script async>
golgotha.local.export = function(cb) {
	if (!golgotha.form.check()) return false;
	const f = document.forms[0];
	golgotha.form.submit(f);
	const t = golgotha.form.getCombo(cb);
	const xmlreq = new XMLHttpRequest();
	xmlreq.timeout = 15500;
	xmlreq.open('post', '/mylogbook.ws', true);
	xmlreq.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
	xmlreq.responseType = 'blob';
	xmlreq.ontimeout = function() { alert('Timed out exporting logbook'); return true; };
	xmlreq.onreadystatechange = function() {
		if (xmlreq.readyState != 4) return false;
		golgotha.form.clear(f);
		if (xmlreq.status != 200) {
			alert('Error ' + xmlreq.status + ' exporting logbook');
			return false;
		}

		const ct = xmlreq.getResponseHeader('Content-Type');
		const b = new Blob([xmlreq.response], {type:ct.substring(0, ct.indexOf(';')), endings:'native'});
		saveAs(b, xmlreq.getResponseHeader('X-Logbook-Filename'));
		return true;
	};

	const params = golgotha.util.createURLParams({export:t,id:f.id.value});
	xmlreq.send(params);
	return true;
};

golgotha.local.validate = function(f) { 
	if (!golgotha.form.check()) return false;
	golgotha.form.submit(f);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:authUser var="user">
<content:attr attr="showExport" value="true" roles="HR,PIREP,Operations" />
<c:set var="showExport" value="${showExport || (user.ID == pilot.ID)}" scope="page" /></content:authUser> 

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="logbook.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate,this)">
<view:table cmd="logbook">
<!-- Title Header Bar -->
<tr class="title">
<c:set var="cspan" value="${access.canPreApprove ? 4 : 8}" scope="page" />
 <td colspan="${cspan}" class="caps left"><span class="nophone">PILOT LOGBOOK FOR </span>${pilot.rank.name}&nbsp;<el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd><c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if>
<c:if test="${showExport}"><span class="nophone"> - EXPORT AS <el:combo name="export" size="1" idx="*" options="${exportTypes}" firstEntry="[ SELECT FORMAT]" onChange="void golgotha.local.export(this)" /></span></c:if></td>
<c:if test="${access.canPreApprove}">
 <td class="nophone" colspan="4"><el:cmd url="preapprove" link="${pilot}" className="title">PRE-APPROVE FLIGHT</el:cmd></td>
</c:if>
</tr>

<!-- Sort/Filter Options -->
<c:if test="${!empty viewContext.results || !empty airports}">
<tr class="title">
 <td colspan="2"><span class="nophone">AIRCRAFT <el:combo name="eqType" size="1" idx="*" options="${eqTypes}" value="${param.eqType}" firstEntry="-" /></span></td>
 <td><el:cmd url="logcalendar" link="${pilot}">CALENDAR</el:cmd></td>
 <td colspan="5" class="right nophone">FROM <el:combo name="airportD" size="1" idx="*" options="${airports}" value="${param.airportD}" firstEntry="-" onRightClick="return golgotha.form.resetCombo()" /> TO
 <el:combo name="airportA" size="1" idx="*" options="${airports}" value="${param.airportA}" firstEntry="-" onRightClick="return golgotha.form.resetCombo()" /> SORT BY
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
 <td>EQUIPMENT</td>
 <td class="nophone">DISTANCE</td>
 <td class="nophone">SIMULATOR</td>
 <td class="nophone">DURATION</td>
</tr>

<!-- Table Flight Report Data -->
<c:forEach var="pirep" items="${viewContext.results}">
<view:row entry="${pirep}">
 <td><fmt:date date="${fn:isDraft(pirep) ? null : pirep.date}" fmt="d" default="-" /></td>
 <td class="nophone"><c:if test="${fn:EventID(pirep) != 0}"><el:img src="network/event.png" caption="Online Event" /></c:if> 
<c:if test="${fn:TourID(pirep) != 0}"><el:img src="tour.png" caption="Flight Tour" /></c:if>
<c:if test="${fn:isACARS(pirep)}"><el:img src="acars.png" caption="ACARS Logged" /></c:if>
<c:if test="${fn:isCheckFlight(pirep)}"><el:img src="checkride.png" caption="Check Ride" /></c:if>
<c:if test="${fn:isOnline(pirep)}"><el:img src="network/icon_${fn:lower(fn:network(pirep))}.png" caption="Online Flight on ${fn:network(pirep)}" /></c:if>
<c:if test="${fn:isDispatch(pirep)}"><el:img src="dispatch.png" caption="ACARS Dispatch Services" /></c:if>
<c:if test="${fn:isSimBrief(pirep)}"><el:img src="icon_simbrief.png" caption="SimBrief Dispatch Services" /></c:if>
<c:if test="${fn:isPromoLeg(pirep)}"><el:img src="promote.png" caption="Counts for Promotion in the ${fn:promoEQTypes(pirep)}" /></c:if></td>
 <td><el:cmd className="pri bld" url="pirep" link="${pirep}" authOnly="true">${pirep.flightCode}</el:cmd></td>
 <td class="small nophone">${pirep.airportD.name} (<el:cmd url="airportinfo" linkID="${pirep.airportD.IATA}" className="plain" authOnly="true"><fmt:airport airport="${pirep.airportD}" /></el:cmd>) - 
 ${pirep.airportA.name} (<el:cmd url="airportinfo" linkID="${pirep.airportA.IATA}" className="plain" authOnly="true"><fmt:airport airport="${pirep.airportA}" /></el:cmd>)</td>
 <td class="sec">${pirep.equipmentType}</td>
 <td class="nophone small"><fmt:distance value="${pirep.distance}" /></td>
 <td class="nophone ter small">${pirep.simulator}</td>
 <td class="nophone"><fmt:duration duration="${(pirep.length > 0) ? pirep.duration : null}" t="HH:mm"  default="-" /></td>
</view:row>
<c:if test="${comments && (!empty pirep.remarks)}">
<view:row entry="${pirep}">
 <td colspan="8" class="left">${pirep.remarks}</td>
</view:row>
</c:if>
</c:forEach>
<tr class="title">
 <td colspan="8"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
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
