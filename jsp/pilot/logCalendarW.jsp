<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_calendar.tld" prefix="calendar" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Logbook for ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></title>
<content:css name="main" />
<content:css name="form" />
<content:css name="calendar" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.switchType = function(combo) {
	self.location = '/logcalendar.do?op=' + escape(golgotha.form.getCombo(combo)) + '&id=${pilot.hexID}&startDate=<fmt:date fmt="d" d="MM/dd/yyyy" date="${startDate}" />';
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="logcalendar.do" link="${pilot}" method="get" validate="return false">
<el:table className="form">
<tr class="title">
 <td style="width:80%" class="caps">PILOT LOGBOOK FOR ${pilot.rank.name} ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></td>
 <td class="right">&nbsp;<span class="nophone">CALENDAR TYPE <el:combo name="op" size="1" idx="*" options="${typeOptions}" value="30" onChange="void golgotha.local.switchType(this)" /></span></td>
</tr>
</el:table>
<div class="mid">
<calendar:week date="cDate" startDate="${startDate}" entries="${pireps}" topBarClass="dayHdr"
	dayBarClass="dayHdr" tableClass="calendar" contentClass="contentW" scrollClass="scroll" cmd="logcalendar">
<calendar:entry name="pirep">
<el:cmd className="bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd><br />
<span class="bld">${pirep.equipmentType}</span><br />
<span class="small">${pirep.airportD.name} (<fmt:airport airport="${pirep.airportD}" />) - ${pirep.airportA.name}
 (<fmt:airport airport="${pirep.airportA}" />)</span><br />
<fmt:dec fmt="#0.0" value="${pirep.length / 10}" /> hours<br />
<c:if test="${fn:EventID(pirep) != 0}"><el:img src="network/event.png" caption="Online Event" /></c:if> 
<c:if test="${fn:isACARS(pirep)}"><el:img src="acars.png" caption="ACARS Logged" /></c:if>
<c:if test="${fn:isCheckFlight(pirep)}"><el:img src="checkride.png" caption="Check Ride" /></c:if>
<c:if test="${fn:isOnline(pirep)}"><el:img src="network/icon_${fn:lower(fn:network(pirep))}.png" caption="Online Flight on ${fn:network(pirep)}" /></c:if>
<c:if test="${fn:isDispatch(pirep)}"><el:img src="dispatch.png" caption="ACARS Dispatch Services" /></c:if>
<c:if test="${fn:isPromoLeg(pirep)}"><el:img src="promote.png" caption="Counts for Promotion in the ${fn:promoEQTypes(pirep)}" /></c:if>
<calendar:spacer><hr /></calendar:spacer>
</calendar:entry>
<calendar:empty>-</calendar:empty>
</calendar:week>
</div>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
