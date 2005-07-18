<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<!-- ACARS PIREP Data -->
<c:if test="${fn:ACARS_ID(pirep) != 0}">
<tr>
 <td class="label">ACARS Flight ID</td>
 <td class="data">${fn:ACARS_ID(pirep)} <a href="acarsData.ws?id=${fn:ACARS_ID(pirep)}">ACARS Data (CSV)</a></td>
</tr>
</c:if>
<tr>
 <td class="label">Start Time</td>
 <td class="data"><fmt:date className="bld" date="${pirep.startTime}" />, engines started at
 <fmt:date date="${pirep.engineStartTime}" /></td>
</tr>
<tr>
 <td class="label">Taxi from Gate</td>
 <td class="data">at <fmt:date date="${pirep.taxiTime}" />, <fmt:int value="${pirep.taxiWeight}" />
  lbs total, <fmt:int value="${pirep.taxiFuel}" /> lbs fuel</td>
</tr>
<tr>
 <td class="label">Takeoff Time</td>
 <td class="data"><fmt:date className="bld" date="${pirep.takeoffTime}" />, (XX minutes after start)</td>
</tr>
<tr>
 <td class="label">Takeoff Information</td>
 <td class="data"><fmt:int value="${pirep.takeoffSpeed}" /> knots, <fmt:dec value="${pirep.takeoffN1}" />%
 N<sub>1</sub>, <fmt:int value="${pirep.takeoffWeight}" /> lbs total, <fmt:int value="${pirep.takeoffFuel}" />
 lbs fuel</td>
</tr>
<tr>
 <td class="label">Landing Time</td>
 <td class="data"><fmt:date className="bld" date="${pirep.landingTime}" />, (XX minutes after start)</td>
</tr>
<tr>
 <td class="label">Landing Information</td>
 <td class="data"><fmt:int value="${pirep.landingSpeed}" /> knots, <fmt:int value="${pirep.landingVSpeed}" />
 feet/min, <fmt:dec value="${pirep.landingN1}" />% N<sub>1</sub>, <fmt:int value="${pirep.landingWeight}" />
 lbs total, <fmt:int value="${pirep.landingFuel}" /></td>
</tr>
<tr>
 <td class="label">Arrival Time</td>
 <td class="data"><fmt:date className="sec bld" date="${pirep.endTime}" /> (XX minutes after start)</td>
</tr>
<tr>
 <td class="label">Arrival Information</td>
 <td class="data"><fmt:int value="${pirep.gateWeight}" /> lbs total, <fmt:int value="${pirep.gateFuel}" />
 lbs fuel</td>
</tr>
<tr>
 <td class="label">Flight Time</td>
 <td class="data"><fmt:date fmt="t" className="pri bld" date="${pirep.airborneTime}" />, block time
 <fmt:date fmt="t" date="${pirep.blockTime}" /></td>
</tr>
<c:if test="${(pirep.time2X > 0) || (pirep.time4X > 0)}">
<tr>
 <td class="label">Flight Time (1X)</td>
 <td class="data"><fmt:int value="${pirep.time1X}" /> seconds</td>
</tr>
<tr>
 <td class="label">Flight Time (2X)</td>
 <td class="data bld"><fmt:int value="${pirep.time2X}" /> seconds</td>
</tr>
<tr>
 <td class="label">Flight Time (4X)</td>
 <td class="data bld"><fmt:int value="${pirep.time4X}" /> seconds</td>
</tr>
</c:if>
<c:if test="${!empty pirep.route}">
<tr>
 <td class="label">Flight Route</td>
 <td class="data">${pirep.route}</td>
</tr>
</c:if>

