<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Schedule Search</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:sysdata var="airlines" name="airlines" mapValues="true" sort="true" />
<content:sysdata var="airports" name="airports" mapValues="true" sort="true" />
<content:sysdata var="allEQ" name="eqtypes" sort="true" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.eqType, 'Equipment Type')) return false;

setSubmit();
disableButton('SearchButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form method="POST" action="findflight.do" op="search" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="4"><content:airline /> SCHEDULE SEARCH</td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" firstEntry=" " options="${airlines}" value="${fafCriteria.airline}" /></td>
 <td class="label">Equipment</td>
 <td class="data"><el:combo name="eqType" size="1" idx="*" firstEntry="< SELECT >" options="${allEQ}" value="${fafCriteria.equipmentType}" /></td>
</tr>
<tr>
 <td class="label">Flight Number / Leg</td>
 <td class="data"><el:text name="flightNumber" idx="*" size="3" max="4" />
 <el:text name="flightLeg" idx="*" size="1" max="1" value="${fafCriteria.leg == 0 ? '' : fafCriteria.leg}" /></td>
 <td class="label">Distance (+/- 150mi)</td>
 <td class="data"><el:text name="distance" idx="*" size="4" max="4" value="${fafCriteria.distance == 0 ? '' : fafCriteria.distance}" /></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" idx="*" size="1" firstEntry=" " options="${airports}" value="${fafCriteria.airportD}" /></td>
 <td class="label">Arriving at</td> 
 <td class="data"><el:combo name="airportA" idx="*" size="1" firstEntry=" " options="${airports}" value="${fafCriteria.airportA}" /></td>
</tr>
<tr>
 <td class="label">Flight Time (+/- 1h)</td>
 <td class="data"><el:text name="flightTime" idx="*" size="4" max="5" /></td>
 <td class="label">Maximum Results</td>
 <td class="data"><el:text name="maxResults" idx="*" size="2" max="3" value="${!empty fafCriteria ? 20 : fafCriteria.maxResults}" /></td>
</tr>
<tr class="title mid">
 <td colspan="4"><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH FLIGHT SCHEDULE" /></td>
</tr>
</el:table>
</el:form>
<c:if test="${!empty fafResults}">
<el:form method="POST" action="buildAssign.do" validate="return true">
<el:table className="view" space="default" pad="default">
<!-- Search Results Data -->
<tr class="caps title left">
 <td colspan="7">SEARCH RESULTS</td>
</tr>

<!-- Search Results Header Bar -->
<tr class="caps title">
 <td width="5%">ADD</td>
 <td width="15%">FLIGHT NUMBER</td>
 <td width="10%">EQUIPMENT</td>
 <td width="35%">AIRPORTS</td>
 <td width="10%">DEPARTS</td>
 <td width="10%">ARRIVES</td>
 <td>DISTANCE</td>
</tr>

<!-- Search Results -->
<c:forEach var="flight" items="${fafResults}">
<tr>
 <td><input type="checkbox" class="check" name="addFA" value="0x<fmt:hex value="${flight.ID}" />" /></td>
 <td class="pri bld">${flight.flightCode}</td>
 <td class="sec bld">${flight.equipmentType}</td>
 <td class="small">${flight.airportD.name} (<fmt:airport airport="${flight.airportD}" />) to
 ${flight.airportA.name} (<fmt:airport airport="${flight.airportA}" />)</td>
 <td><fmt:date fmt="t" t="hh:mm" tz="${flight.airportD.TZ}" date="${flight.timeD}" /></td>
 <td><fmt:date fmt="t" t="hh:mm" tz="${flight.airportA.TZ}" date="${flight.timeA}" /></td>
 <td class="sec"><fmt:int value="${flight.distance}" /> miles</td>
</tr>
</c:forEach>

<tr class="title">
 <td colspan="7"><el:button type="submit" className="BUTTON" label="BUILD FLIGHT ASSIGNMENT" />&nbsp;
<el:cmdbutton url="buildassign" op="reset" label="RESET RESULTS" /></td>
</tr>
</el:table>
</el:form>
</c:if>
<c:if test="${!empty buildAssign}">
<br />
<el:table className="view" space="default" pad="default">
<!-- Flight Assignment Data -->
<tr class="caps title left">
 <td colspan="5">FLIGHT ASSIGNMENT</td>
</tr>

<!-- Flight Assignment Header Bar -->
<tr class="caps title">
 <td width="20%">FLIGHT NUMBER</td>
 <td width="15%">EQUIPMENT</td>
 <td width="45%">AIRPORTS</td>
 <td>DISTANCE</td>
</tr>

<!-- Search Results -->
<c:forEach var="flight" items="${buildAssign.flights}">
<tr>
 <td class="pri bld">${flight.flightCode}</td>
 <td class="sec bld">${flight.equipmentType}</td>
 <td class="small">${flight.airportD.name} (<fmt:airport airport="${flight.airportD}" />) to
 ${flight.airportA.name} (<fmt:airport airport="${flight.airportA}" />)</td>
 <td class="sec"><fmt:int value="${flight.distance}" /> miles</td>
 <td><fmt:dec value="${flight.length / 10}" /> hours</td>
</tr>
</c:forEach>
<tr class="title">
 <td colspan="5"><el:cmdbutton url="assignsave" label="SAVE FLIGHT ASSIGMENT" />&nbsp;
<el:cmdbutton url="buildassign" op="reset" label="CLEAR FLIGHT ASSIGNMENT" /></td>
</tr>
</el:table>
</c:if>
<content:copyright />
</div>
</body>
</html>
