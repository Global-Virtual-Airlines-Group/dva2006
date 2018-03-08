<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Flight Schedule Search</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:json />
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<fmt:aptype var="useICAO" />
<script>
golgotha.ff = golgotha.ff || {};
golgotha.ff.validate = function(f)
{
if (!golgotha.form.check()) return false;

// Check that at least one option was selected
var eqOK = golgotha.form.comboSet(f.eqType);
var alOK = golgotha.form.comboSet(f.airline);
var adOK = golgotha.form.comboSet(f.airportD);
var aaOK = golgotha.form.comboSet(f.airportA);
if (eqOK || adOK || aaOK || alOK) {
	golgotha.form.submit(f);
	return true;
}

throw new golgotha.event.ValidationError('Please select at least an Airline, Aircraft Type or Departure/Arrival Airport.', f.airline);
};
<c:if test="${!empty fafResults}">
golgotha.ff. buildValidate = function(f)
{
if (!golgotha.form.check()) return false;

var isOK = false;
if (f.addFA.length) {
	for (var x = 0; ((!isOK) && (x < f.addFA.length)); x++)
		isOK = isOK || f.addFA[x].checked;
} else
	isOK = f.addFA.checked;

if (!isOK) {
	alert('Please select at least one Flight Leg to add.');
	return false;
}

golgotha.form.submit(f);
return true;
};
</c:if>
golgotha.ff.updateAirline = function(cb)
{
var f = document.forms[0];
var cfg = golgotha.airportLoad.config.clone();
cfg.airline = golgotha.form.getCombo(cb);
golgotha.airportLoad.changeAirline([f.airportD], cfg);
window.setTimeout(function() {
	var cfg2 = cfg.clone();
    cfg2.dst = true;
    golgotha.airportLoad.changeAirline([f.airportA], cfg2);
}, 250);
return true;
};

golgotha.ff.updateSort = function(cb) {
	return golgotha.util.disable('sortDesc', !golgotha.form.comboSet(cb));
};

golgotha.ff.refreshAirports = function() {
	updateAirline(document.forms[0].airline);
};

golgotha.ff.refreshNV = function(checkbox, cboName, isDest)
{
var f = checkbox.form;
var srcA = golgotha.form.getCombo(f.airportD);
var cfg = golgotha.airportLoad.config.clone();
cfg.airline = golgotha.form.getCombo(f.airline); cfg.notVisited = checkbox.checked;
if (isDest && (srcA != null) && (srcA != '')) {
	cfg.dst = true;	
	cfg.code = srcA;
}

var cbo = f[cboName];
if (cbo) {
	cbo.notVisited = cfg.notVisited;
	cbo.loadAirports(cfg);
}

return true;
};

golgotha.onDOMReady(function() {
	var f = document.forms[0];
	var cfg = golgotha.airportLoad.config;
	cfg.doICAO = ${useICAO};
	cfg.myRated = f.myEQTypes.checked;
	golgotha.airportLoad.setHelpers(f.airportD);
	golgotha.airportLoad.setHelpers(f.airportA);
	<c:if test="${!empty fafCriteria}">
	f.airportD.updateAirportCode();
	f.airportA.updateAirportCode();</c:if>
	<c:if test="${empty fafCriteria}">
	f.airline.onchange();</c:if>
	f.airportD.notVisited = f.nVD.checked;
	f.airportA.notVisited = f.nVA.checked;
	golgotha.ff.updateSort(f.sortType);
});
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="acarsEnabled" name="acars.enabled" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="findflight.do" op="search" validate="return golgotha.form.wrap(golgotha.ff.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="4"><content:airline /> FLIGHT SCHEDULE SEARCH</td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" firstEntry="-" options="${airlines}" value="${empty fafCriteria ? airline : fafCriteria.airline}" onChange="void golgotha.ff.updateAirline(this)" /></td>
 <td class="label">Equipment</td>
 <td class="data"><el:combo name="eqType" size="1" idx="*" firstEntry="-" options="${allEQ}" value="${param.myEQTypes ? '-' : fafCriteria.equipmentType}" /></td>
</tr>
<tr>
 <td class="label">Flight Number / Leg</td>
 <td class="data"><el:text name="flightNumber" idx="*" size="3" max="4" />
 <el:text name="flightLeg" idx="*" size="1" max="1" value="${fafCriteria.leg == 0 ? '' : fafCriteria.leg}" /></td>
 <td class="label">Distance</td>
 <td class="data"><el:text name="distance" idx="*" size="4" max="4" value="${fafCriteria.distance < 1 ? '' : fafCriteria.distance}" />
 +/- <el:text name="distRange" idx="*" size="4" max="4" value="${fafCriteria.distance < 1 ? '' : fafCriteria.distanceRange}" /> miles</td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" idx="*" size="1" firstEntry="-" options="${airports}" value="${fafCriteria.airportD}" onChange="this.updateAirportCode(); golgotha.airportLoad.updateOrigin(this)" />
<span class="nophone"> <el:airportCode combo="airportD" idx="*" airport="${fafCriteria.airportD}" />
 <el:box name="nVD" value="true" className="small" checked="${param.nVD}" label="Only include unvisited Airports" onChange="void golgotha.ff.refreshNV(this, 'airportD')" /></span></td>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" idx="*" size="1" firstEntry="-" options="${airportsA}" value="${fafCriteria.airportA}" onChange="void this.updateAirportCode()" />
<span class="nophone"> <el:airportCode combo="airportA" idx="*" airport="${fafCriteria.airportA}" />
 <el:box name="nVA" value="true" className="small" checked="${param.nVA}" label="Only include unvisited Airports" onChange="void golgotha.ff.refreshNV(this, 'airportA', true)" /></span></td>
</tr>
<tr>
 <td class="label">Departure Time (+/- 2h)</td>
 <td class="data"><el:combo name="hourD" idx="*" size="1" options="${hours}" value="${fafCriteria.hourD}" /></td>
 <td class="label">Arrival Time (+/- 2h)</td>
 <td class="data"><el:combo name="hourA" idx="*" size="1" options="${hours}" value="${fafCriteria.hourA}" /></td>
</tr>
<tr>
 <td class="label">Sort Flights by</td>
 <td class="data"><el:combo name="sortType" idx="*" size="1" options="${sortTypes}" value="${param.sortType}" onChange="void golgotha.ff.updateSort(this)" />
 <el:box ID="sortDesc" name="sortDesc" idx="*" value="true" checked="${param.sortDesc}" label="Descending" /></td>
 <td class="label">Maximum Results</td>
 <td class="data"><el:text name="maxResults" idx="*" size="2" max="3" value="${empty fafCriteria ? 25 : fafCriteria.maxResults}" />
 total, <el:text name="maxFlights" idx="*" size="2" max="2" value="${fafCriteria.flightsPerRoute}" /> preferred between airports</td>
</tr>
<tr>
 <td class="label">Frequency</td>
 <td class="data">Show Routes flown no more than <el:text name="maxRouteLegs" size="2" max="3" value="${(empty fafCriteria || (fafCriteria.routeLegs < 0)) ? '' : fafCriteria.routeLegs}" /> times</td>
 <td class="label">Recent Routes</td>
 <td class="data">Show Routes flown at least <el:text name="maxLastFlown" size="2" max="4" value="${(empty fafCriteria || (fafCriteria.lastFlownInterval < 0)) ? '' : fafCriteria.lastFlownInterval}" /> days ago</td>
</tr>
<tr>
 <td class="label top">Search Options</td>
 <td class="data top"><el:box name="myEQTypes" value="true" checked="${param.myEQTypes}" label="My rated Equipment Types" onChange="golgotha.airportLoad.config.myRated = this.checked" /><br />
<el:box name="showUTCTimes" value="true" checked="${param.showUTCTimes}" label="Show Departure/Arrival Times as UTC" /></td>
 <td class="label top">ACARS Dispatch</td>
 <td class="data top"><el:box name="checkDispatch" idx="*" value="true" checked="${empty fafCriteria ? true : fafCriteria.checkDispatch}" label="Display Dispatch route count" /><br />
 <el:box name="dispatchOnly" idx="*" value="true" checked="${fafCriteria.dispatchOnly}" label="Flights with Dispatch routes only" /></td>
</tr>
<tr class="title mid">
 <td colspan="4"><el:button type="submit" label="SEARCH FLIGHT SCHEDULE" /></td>
</tr>
</el:table>
</el:form>
<c:if test="${!empty fafResults}">
<el:form method="post" action="buildassign.do" validate="return golgotha.form.wrap(golgotha.ff.buildValidate, this)">
<el:table className="view">
<!-- Search Results Data -->
<tr class="title caps">
 <td colspan="9" class="left"><span class="nophone">FLIGHT SCHEDULE </span>SEARCH RESULTS<c:if test="${!empty importDate}"> - IMPORTED ON <fmt:date date="${importDate}" t="HH:mm" /></c:if>
<c:if test="${!empty effectiveDate}"> REPLAY OF <fmt:date date="${effectiveDate}" fmt="d" tzName="UTC" /></c:if></td>
</tr>

<!-- Search Results Header Bar -->
<tr class="caps title">
 <td>ADD</td>
 <td style="width:12%">FLIGHT NUMBER</td>
 <td style="width:10%">EQUIPMENT</td>
 <td>AIRPORTS</td>
 <td class="nophone" style="width:8%">DEPARTS</td>
 <td class="nophone" style="width:8%">ARRIVES</td>
 <td style="width:6%">LENGTH</td>
 <td class="nophone" style="width:5%">ROUTES</td>
 <td class="nophone">DISTANCE</td>
</tr>

<!-- Search Results -->
<c:forEach var="flight" items="${fafResults}">
<view:row entry="${flight}">
 <td><el:box name="addFA" value="${flight.flightCode}" label="" /></td>
 <td class="pri bld">${flight.flightCode}</td>
 <td class="sec bld">${flight.equipmentType}</td>
 <td class="small">${flight.airportD.name}&nbsp;<span class="nophone">(<el:cmd url="airportinfo" linkID="${flight.airportD.IATA}"><fmt:airport airport="${flight.airportD}" /></el:cmd>)</span> to
 ${flight.airportA.name}&nbsp;<span class="nophone">(<el:cmd url="airportinfo" linkID="${flight.airportA.IATA}"><fmt:airport airport="${flight.airportA}" /></el:cmd>)</span></td>
<c:if test="${param.showUTCTimes}">
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tzName="UTC" date="${flight.timeD}" /> UTC</td>
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tzName="UTC" date="${flight.timeA}" /> UTC</td>
</c:if>
<c:if test="${!param.showUTCTimes}"> 
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tz="${flight.airportD.TZ}" date="${flight.timeD}" /></td>
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tz="${flight.airportA.TZ}" date="${flight.timeA}" /></td>
</c:if>
 <td class="small"><fmt:duration duration="${flight.duration}" t="HH:mm" /></td>
 <td class="small bld nophone"><fmt:int value="${flight.dispatchRoutes}" /></td>
 <td class="sec nophone"><fmt:distance value="${flight.distance}" /></td>
</view:row>
</c:forEach>

<!-- Button Bar -->
<tr class="title">
 <td colspan="9">SET EQUIPMENT <el:combo name="eqOverride" size="1" firstEntry="-" options="${myEQ}" /> <el:button type="submit" label="ADD TO FLIGHT ASSIGNMENT" /> <el:cmdbutton url="resetassign" label="RESET RESULTS" /></td>
</tr>
</el:table>
</el:form>
</c:if>
<c:if test="${!empty buildAssign}">
<br />
<el:table className="view">
<!-- Flight Assignment Data -->
<tr class="caps title">
 <td colspan="7" class="left">FLIGHT ASSIGNMENT</td>
</tr>

<!-- Flight Assignment Header Bar -->
<tr class="caps title">
 <td style="width:15%">FLIGHT NUMBER</td>
 <td style="width:10%">EQUIPMENT</td>
 <td>AIRPORTS</td>
 <td class="nophone" style="width:10%">DEPARTS</td>
 <td class="nophone" style="width:10%">ARRIVES</td>
 <td style="width:10%">LENGTH</td>
 <td class="nophone" style="width:10%">DISTANCE</td>
</tr>

<!-- Flighrt Assignment Legs -->
<c:forEach var="flight" items="${buildAssign.flights}">
<tr>
 <td class="pri bld">${flight.flightCode}</td>
 <td class="sec bld">${flight.equipmentType}</td>
 <td class="small">${flight.airportD.name} (<el:cmd url="airportinfo" linkID="${flight.airportD.IATA}"><fmt:airport airport="${flight.airportD}" /></el:cmd>) to ${flight.airportA.name} (<el:cmd url="airportinfo" linkID="${flight.airportA.IATA}"><fmt:airport airport="${flight.airportA}" /></el:cmd>)</td>
<c:if test="${param.showUTCTimes}">
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tzName="UTC" date="${flight.timeD}" /> UTC</td>
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tzName="UTC" date="${flight.timeA}" /> UTC</td>
</c:if>
<c:if test="${!param.showUTCTimes}"> 
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tz="${flight.airportD.TZ}" date="${flight.timeD}" /></td>
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tz="${flight.airportA.TZ}" date="${flight.timeA}" /></td>
</c:if>
 <td class="small"><fmt:duration duration="${flight.duration}" t="HH:mm" /></td>
 <td class="sec nophone"><fmt:distance value="${flight.distance}" /></td>
</tr>
</c:forEach>

<!-- Button Bar -->
<tr class="title">
 <td colspan="7"><view:legend width="150" labels="Regular Flight,Historic Flight" classes=" ,opt2" />&nbsp;<el:cmdbutton url="assignsave" label="SAVE FLIGHT ASSIGNMENT" />&nbsp;<el:cmdbutton url="resetassign" label="CLEAR ASSIGNMENT" /></td>
</tr>
</el:table>
</c:if>

<c:if test="${doSearch && (empty fafResults)}">
<!-- No Search Results Found -->
<el:table className="view">
<tr class="title caps">
 <td class="mid">No Flights matching your Search Criteria were found.</td>
</tr>
</el:table>
</c:if>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
