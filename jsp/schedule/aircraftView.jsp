<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Aircraft Information - ${aircraft.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:enum var="tankTypes" className="org.deltava.beans.schedule.TankType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">AIRCRAFT PROFILE - ${aircraft.name}</td>
</tr>
<tr>
 <td class="label">Full Aircraft Name</td>
 <td class="data bld">${aircraft.fullName}</td>
</tr>
<content:filter roles="Schedule,Operations">
<tr>
 <td class="label">Aircraft Family Code</td>
 <td class="data">${aircraft.family}</td>
</tr>
</content:filter>
<tr>
 <td class="label top">Virtual Airlines</td>
 <td class="data sec"><fmt:list value="${aircraft.apps}" delim=", " empty="NONE" /></td>
</tr>
<tr>
 <td class="label">Maximum Range</td>
 <td class="data"><fmt:distance value="${opts.range}" longUnits="true" className="pri bld" /></td>
</tr>
<c:if test="${opts.takeoffRunwayLength > 0}">
<tr>
 <td class="label">Minimum Takeoff Runway Length</td>
 <td class="data"><fmt:int value="${opts.takeoffRunwayLength}" /> feet</td>
</tr>
</c:if>
<c:if test="${opts.landingRunwayLength > 0}">
<tr>
 <td class="label">Minimum Landing Runway Length</td>
 <td class="data"><fmt:int value="${opts.landingRunwayLength}" /> feet</td>
</tr>
</c:if>
<tr>
 <td class="label">Passenger Capacity</td>
 <td class="data"><fmt:int value="${opts.seats}" /> seats</td>
</tr>
<c:if test="${!empty aircraft.IATA}">
<tr>
 <td class="label top">IATA Equipment Code(s)</td>
 <td class="data"><fmt:list value="${aircraft.IATA}" delim=", " /></td>
</tr>
</c:if>
<c:if test="${aircraft.historic || isETOPS || opts.useSoftRunways}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><c:if test="${aircraft.historic}"><span class="sec bld caps">This is a Historic Aircraft</span>
<c:if test="${isETOPS || opts.useSoftRunways}"><br /></c:if></c:if>
<c:if test="${isETOPS}"><span class="ter bld caps">This Aircraft is ETOPS-rated (${opts.ETOPS})</span>
<c:if test="${opts.useSoftRunways}"><span class="bld caps">This Aircraft is authroized for soft runway operation</span></c:if></c:if></td>
</tr>
</c:if>
<tr class="title caps">
 <td colspan="2">AIRCRAFT WEIGHTS</td>
</tr>
<tr>
 <td class="label">Maximum Weight</td>
 <td class="data"><fmt:int value="${aircraft.maxWeight}" /> pounds</td>
</tr>
<c:if test="${aircraft.maxZeroFuelWeight > 0}">
<tr>
  <td class="label">Maximum Zero Fuel Weight</td>
  <td class="data"><fmt:int value="${aircraft.maxZeroFuelWeight}" /> pounds</td>
</tr>
</c:if>
<tr>
 <td class="label">Maximum Takeoff Weight</td>
 <td class="data"><fmt:int value="${aircraft.maxTakeoffWeight}" /> pounds</td>
</tr>
<tr>
 <td class="label">Maximum Landing Weight</td>
 <td class="data"><fmt:int value="${aircraft.maxLandingWeight}" /> pounds</td>
</tr>
<tr class="title caps">
 <td colspan="2">ACARS FUEL PROFILE</td>
</tr>
<tr>
 <td class="label">Engine Information</td>
 <td class="data pri bld"><fmt:int value="${aircraft.engines}" /> x ${aircraft.engineType}</td>
</tr>
<tr>
 <td class="label">Cruise Speed</td>
 <td class="data"><fmt:int value="${aircraft.cruiseSpeed}" /> knots</td>
</tr>
<tr>
 <td class="label">Fuel Flow</td>
 <td class="data bld"><fmt:int value="${aircraft.fuelFlow}" /> pounds per engine per hour</td>
</tr>
<tr>
 <td class="label">Base Fuel</td>
 <td class="data"><fmt:int value="${aircraft.baseFuel}" /> pounds</td>
</tr>
<tr>
 <td class="label">Taxi Fuel</td>
 <td class="data"><fmt:int value="${aircraft.taxiFuel}" /> pounds</td>
</tr>
<tr>
 <td class="label top">Primary Tanks</td>
 <td class="sec data"><fmt:list value="${aircraft.tankNames[tankTypes[0]]}" delim=", " empty="NONE" /></td>
</tr>
<tr>
 <td class="label">Primary Percentage</td>
 <td class="data">Fill to <fmt:int value="${aircraft.tankPercent[tankTypes[0]]}" /> percent before filling Secondary tanks</td>
</tr>
<tr>
 <td class="label top">Secondary Tanks</td>
 <td class="sec data"><fmt:list value="${aircraft.tankNames[tankTypes[1]]}" delim=", " empty="NONE" /></td>
</tr>
<tr>
 <td class="label">Secondary Percentage</td>
 <td class="data">Fill to <fmt:int value="${aircraft.tankPercent[tankTypes[1]]}" /> percent before filling Other tanks</td>
</tr>
<tr>
 <td class="label top">Other Tanks</td>
 <td class="sec data"><fmt:list value="${aircraft.tankNames[tankTypes[2]]}" delim=", " empty="NONE" /></td>
</tr>
<%@ include file="/jsp/auditLog.jspf" %>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;<c:if test="${access.canEdit}"><el:cmdbutton url="aircraft" op="edit" link="${aircraft}" label="EDIT AIRCRAFT PROFILE" /></c:if>
</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
