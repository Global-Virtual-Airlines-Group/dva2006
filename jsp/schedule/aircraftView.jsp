<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
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
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

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
 <td class="data"><fmt:distance value="${aircraft.range}" longUnits="true" className="pri bld" /></td>
</tr>
<c:if test="${aircraft.takeoffRunwayLength > 0}">
<tr>
 <td class="label">Minimum Takeoff Runway Length</td>
 <td class="data"><fmt:int value="${aircraft.takeoffRunwayLength}" /> feet</td>
</tr>
</c:if>
<c:if test="${aircraft.landingRunwayLength > 0}">
<tr>
 <td class="label">Minimum Landing Runway Length</td>
 <td class="data"><fmt:int value="${aircraft.landingRunwayLength}" /> feet</td>
</tr>
</c:if>
<tr>
 <td class="label">Passenger Capacity</td>
 <td class="data"><fmt:int value="${aircraft.seats}" /> seats</td>
</tr>
<c:if test="${!empty aircraft.IATA}">
<tr>
 <td class="label top">IATA Equipment Code(s)</td>
 <td class="data"><fmt:list value="${aircraft.IATA}" delim=", " /></td>
</tr>
</c:if>
<c:if test="${aircraft.historic || aircraft.ETOPS || aircraft.useSoftRunways}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><c:if test="${aircraft.historic}"><span class="sec bld caps">This is a Historic Aircraft</span>
<c:if test="${aircraft.ETOPS || aircraft.useSoftRunways}"><br /></c:if></c:if>
<c:if test="${aircraft.ETOPS}"><span class="ter bld caps">This Aircraft is ETOPS-rated</span>
<c:if test="${aircraft.useSoftRunways}"><span class="bld caps">This Aircraft is authroized for soft runway operation</span></c:if></c:if></td>
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
 <td class="sec data"><fmt:list value="${aircraft.tankNames['Primary']}" delim=", " empty="NONE" /></td>
</tr>
<tr>
 <td class="label">Primary Percentage</td>
 <td class="data">Fill to <fmt:int value="${aircraft.tankPercent['Primary']}" /> percent before filling Secondary tanks</td>
</tr>
<tr>
 <td class="label top">Secondary Tanks</td>
 <td class="sec data"><fmt:list value="${aircraft.tankNames['Secondary']}" delim=", " empty="NONE" /></td>
</tr>
<tr>
 <td class="label">Secondary Percentage</td>
 <td class="data">Fill to <fmt:int value="${aircraft.tankPercent['Secondary']}" /> percent before filling Other tanks</td>
</tr>
<tr>
 <td class="label top">Other Tanks</td>
 <td class="sec data"><fmt:list value="${aircraft.tankNames['Other']}" delim=", " empty="NONE" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;
<c:if test="${access.canEdit}"><el:cmdbutton ID="EditButton" url="aircraft" op="edit" link="${aircraft}" label="EDIT AIRCRAFT PROFILE" /></c:if>
</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
