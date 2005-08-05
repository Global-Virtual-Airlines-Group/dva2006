<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<!-- ACARS PIREP Data -->
<tr class="title">
 <td class="caps" colspan="2"><content:airline /> ACARS DATA</td>
</tr>
<c:if test="${fn:ACARS_ID(pirep) != 0}">
<tr>
 <td class="label">ACARS Flight ID</td>
 <td class="data">${fn:ACARS_ID(pirep)} <a href="acarsData.ws?id=${fn:ACARS_ID(pirep)}">ACARS Data (CSV)</a></td>
</tr>
</c:if>
<tr>
 <td class="label">Start Time</td>
 <td class="data"><fmt:date className="bld" date="${pirep.startTime}" /></td>
</tr>
<tr>
 <td class="label">Taxi from Gate</td>
 <td class="data">at <fmt:date date="${pirep.taxiTime}" />, <fmt:int value="${pirep.taxiWeight}" />
  lbs total, <fmt:int value="${pirep.taxiFuel}" /> lbs fuel</td>
</tr>
<tr>
 <td class="label">Takeoff Time</td>
 <td class="data"><fmt:date className="bld" date="${pirep.takeoffTime}" />, 
(<fmt:int value="${fn:delta(pirep.startTime, pirep.takeoffTime) / 60}" /> minutes after start)</td>
</tr>
<tr>
 <td class="label">Takeoff Information</td>
 <td class="data"><fmt:int value="${pirep.takeoffSpeed}" /> knots, <fmt:dec value="${pirep.takeoffN1}" />%
 N<sub>1</sub>, <fmt:int value="${pirep.takeoffWeight}" /> lbs total, <fmt:int value="${pirep.takeoffFuel}" />
 lbs fuel</td>
</tr>
<tr>
 <td class="label">Landing Time</td>
 <td class="data"><fmt:date className="bld" date="${pirep.landingTime}" />, 
(<fmt:int value="${fn:delta(pirep.startTime, pirep.landingTime) / 60}" /> minutes after start)</td>
</tr>
<tr>
 <td class="label">Landing Information</td>
 <td class="data"><fmt:int value="${pirep.landingSpeed}" /> knots, <fmt:int value="${pirep.landingVSpeed}" />
 feet/min, <fmt:dec value="${pirep.landingN1}" />% N<sub>1</sub>, <fmt:int value="${pirep.landingWeight}" />
 lbs total, <fmt:int value="${pirep.landingFuel}" /> lbs fuel</td>
</tr>
<tr>
 <td class="label">Arrival Time</td>
 <td class="data"><fmt:date className="sec bld" date="${pirep.endTime}" />, 
(<fmt:int value="${fn:delta(pirep.startTime, pirep.endTime) / 60}" /> minutes after start)</td>
</tr>
<tr>
 <td class="label">Arrival Information</td>
 <td class="data"><fmt:int value="${pirep.gateWeight}" /> lbs total, <fmt:int value="${pirep.gateFuel}" />
 lbs fuel</td>
</tr>
<c:set var="airborneTime" value="${fn:delta(pirep.takeoffTime, pirep.landingTime) / 60}" scope="request" />
<c:set var="blockTime" value="${fn:delta(pirep.startTime, pirep.endTime) / 60}" scope="request" />
<tr>
 <td class="label">Flight Time</td>
 <td class="data"><fmt:date fmt="t" t="HH:mm" className="pri bld" date="${pirep.airborneTime}" />, block time
 <fmt:date fmt="t" t="HH:mm" date="${pirep.blockTime}" /></td>
</tr>
<c:if test="${(pirep.time2X > 0) || (pirep.time4X > 0)}">
<tr>
 <td class="label">Flight Time (1X)</td>
 <td class="data"><fmt:int value="${pirep.time1X}" /> minutes</td>
</tr>
</c:if>
<c:if test="${pirep.time2X > 0}">
<tr>
 <td class="label">Flight Time (2X)</td>
 <td class="data bld"><fmt:int value="${pirep.time2X}" /> minutes</td>
</tr>
</c:if>
<c:if test="${pirep.time4X > 0}">
<tr>
 <td class="label">Flight Time (4X)</td>
 <td class="data bld"><fmt:int value="${pirep.time4X}" /> minutes</td>
</tr>
</c:if>
<c:if test="${!empty checkRide}">
<el:form method="post" action="pirepscore.do" linkID="0x${pirep.ID}" validate="return valdiate(this)">
<tr class="title">
 <td class="caps" colspan="2">CHECK RIDE INFORMATION</td>
</tr>
<tr>
 <td class="label">Name</td>
 <td class="data pri bld">${checkRide.name} (Stage <fmt:int value="${checkRide.stage}" />)</td>
</tr>
<tr>
 <td class="label">Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="100" height="5" readOnly="${crAccess.canScore}">${checkRide.comments}</el:textbox></td>
</tr>
<c:if test="${scoreCR}">
<tr>
 <td class="label">Check Ride Status</td>
 <td class="data sec bld"><el:check name="crApprove" type="radio" options="${crPassFail}" idx="*" /></td>
</tr>
<tr class="title mid">
 <td colspan="2"><el:button ID="CRButton" type="submit" className="BUTTON" label="APPROVE FLIGHT / SCORE CHECK RIDE" /></td>
</tr>
</c:if>
</el:form>
</c:if>
<c:if test="${!empty flightInfo.route}">
<tr>
 <td class="label">Flight Route</td>
 <td class="data">${flightInfo.route}</td>
</tr>
</c:if>
