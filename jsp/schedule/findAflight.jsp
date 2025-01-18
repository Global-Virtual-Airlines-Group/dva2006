<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" buffer="48kb" autoFlush="true" trimDirectiveWhitespaces="true" %>
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
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<fmt:aptype var="useICAO" />
<script async>
golgotha.ff = golgotha.ff || {};
<fmt:jsarray var="golgotha.ff.famiy" items="${allFamily}" />
golgotha.ff.validate = function(f) {
	if (!golgotha.form.check()) return false;
	if (!golgotha.form.comboSet(f.eqType) && !golgotha.form.comboSet(f.airline) && !golgotha.form.comboSet(f.airportD) && !golgotha.form.comboSet(f.airportA))
		throw new golgotha.event.ValidationError('Please select at least an Airline, Aircraft Type or Departure/Arrival Airport.', f.airline);

	golgotha.form.submit(f);
	return true;
};
<c:if test="${!empty fafResults}">
golgotha.ff.buildValidate = function(f) {
	if (!golgotha.form.check()) return false;
	const chks = (f.addFA  instanceof Array) ? f.addFA : [f.addFA];
	if (chks.length == 1) {
		golgotha.form.submit(f);
		chks[0].checked = true;
		return true;
	}

	for (var x = 0; (!isOK && (x < chks.length)); x++)
		isOK |= chks[x].checked;

	return isOK && golgotha.form.submit(f);
};
</c:if>
golgotha.ff.updateAirline = function(cb) {
	const f = document.forms[0];
	const cfg = golgotha.airportLoad.config.clone();
	cfg.airline = golgotha.form.getCombo(cb);
	golgotha.airportLoad.changeAirline([f.airportD], cfg);
	golgotha.util.show('historicOpts', !golgotha.form.comboSet(f.airline));
	window.setTimeout(function() {
		const cfg2 = cfg.clone();
    	cfg2.dst = true;
    	golgotha.airportLoad.changeAirline([f.airportA], cfg2);
	}, 250);
	return true;
};

golgotha.ff.updateFamily = function(cb) { golgotha.form.setCombo(document.forms[0].eqType, '-'); };
golgotha.ff.updateEQ = function(cb) { golgotha.form.setCombo(document.forms[0].family, '-'); };
golgotha.ff.updateSort = function(cb) { return golgotha.util.disable('sortDesc', !golgotha.form.comboSet(cb)); };
golgotha.ff.refreshAirports = function() { updateAirline(document.forms[0].airline); };
golgotha.ff.refreshNV = function(checkbox, cboName, isDest)
{
const f = checkbox.form;
const srcA = golgotha.form.getCombo(f.airportD);
const cfg = golgotha.airportLoad.config.clone();
cfg.airline = golgotha.form.getCombo(f.airline); cfg.notVisited = checkbox.checked;
if (isDest && (srcA != null) && (srcA != '')) {
	cfg.dst = true;	
	cfg.code = srcA;
}

const cbo = f[cboName];
if (cbo) {
	cbo.notVisited = cfg.notVisited;
	cbo.loadAirports(cfg);
}

return true;
};

golgotha.onDOMReady(function() {
	const f = document.forms[0];
	const cfg = golgotha.airportLoad.config;
	cfg.doICAO = ${useICAO};
	cfg.myRated = f.myEQTypes.checked;
	golgotha.airportLoad.setHelpers([f.airportD,f.airportA]);
	golgotha.airportLoad.setText([f.airline,f.airportD,f.airportA]);
	f.airline.updateAirlineCode = golgotha.airportLoad.updateAirlineCode;
	<c:if test="${!empty fafCriteria}">
	f.airportD.updateAirportCode();
	f.airportA.updateAirportCode();</c:if>
	<c:if test="${empty fafCriteria}">
	f.airline.onchange();</c:if>
	f.airportD.notVisited = f.nVD.checked;
	f.airportA.notVisited = f.nVA.checked;
	golgotha.ff.updateSort(f.sortType);
	if (golgotha.form.comboSet(f.airportD)) {
		f.airportD.onchange();
<c:if test="${!empty fafCriteria.airportA}">window.setTimeout(function() { golgotha.form.setCombo(f.airportA, '${useICAO ? fafCriteria.airportA.ICAO : fafCriteria.airportA.IATA}'); }, 500);</c:if>
	}
});
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="acarsEnabled" name="acars.enabled" />
<content:enum var="inclusionOpts" className="org.deltava.beans.Inclusion" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="findflight.do" op="search" validate="return golgotha.form.wrap(golgotha.ff.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="4"><span class="nophone"><content:airline />&nbsp;</span>FLIGHT SCHEDULE SEARCH</td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" firstEntry="-" options="${airlines}" value="${empty fafCriteria ? airline : fafCriteria.airline}" onChange="this.updateAirlineCode(); void golgotha.ff.updateAirline(this)" onRightClick="return golgotha.form.resetCombo()" />
 <el:text name="airlineCode" size="2" max="3" idx="*" onChange="void golgotha.airportLoad.setAirline(document.forms[0].airline, this, true)" /></td>
 <td class="label">Equipment</td>
 <td class="data"><el:combo name="eqType" size="1" idx="*" firstEntry="-" options="${allEQ}" value="${(param.myEQTypes || (!empty eqFamily)) ? '-' : fafCriteria.equipmentType}" onChange="void golgotha.ff.updateEQ(this)" onRightClick="return golgotha.form.resetCombo()" /> - 
 family <el:combo name="family" size="1" firstEntry="-" options="${allFamily}" value="${eqFamily}" onChange="void golgotha.ff.updateFamily(this)" /></td>
</tr>
<tr>
 <td class="label">Flight Number / Leg</td>
 <td class="data"><el:text name="flightNumber" idx="*" size="3" max="4" autoComplete="false" />&nbsp;<el:text name="flightLeg" idx="*" size="1" max="1" value="${fafCriteria.leg == 0 ? '' : fafCriteria.leg}" autoComplete="false" /></td>
 <td class="label">Distance</td>
 <td class="data"><el:text name="distance" idx="*" size="4" max="4" value="${fafCriteria.distance < 1 ? '' : fafCriteria.distance}" autoComplete="false" /> +/- <el:text name="distRange" idx="*" size="4" max="4" value="${fafCriteria.distance < 1 ? '' : fafCriteria.distanceRange}" autoComplete="false" /> miles</td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" idx="*" size="1" firstEntry="-" options="${airports}" value="${fafCriteria.airportD}" onChange="this.updateAirportCode(); golgotha.airportLoad.updateOrigin(this)" onRightClick="return golgotha.form.resetCombo()" />
<span class="nophone"> <el:airportCode combo="airportD" idx="*" airport="${fafCriteria.airportD}" /> <el:box name="nVD" value="true" className="small" checked="${param.nVD}" label="Only include unvisited Airports" onChange="void golgotha.ff.refreshNV(this, 'airportD')" /></span></td>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" idx="*" size="1" firstEntry="-" options="${airportsA}" value="${fafCriteria.airportA}" onChange="void this.updateAirportCode()" onRightClick="return golgotha.form.resetCombo()" />
<span class="nophone"> <el:airportCode combo="airportA" idx="*" airport="${fafCriteria.airportA}" /> <el:box name="nVA" value="true" className="small" checked="${param.nVA}" label="Only include unvisited Airports" onChange="void golgotha.ff.refreshNV(this, 'airportA', true)" /></span></td>
</tr>
<tr>
 <td class="label">Departure Time (+1h)</td>
 <td class="data"><el:combo name="hourD" idx="*" size="1" options="${hours}" value="${fafCriteria.hourD}" onRightClick="return golgotha.form.resetCombo()" /></td>
 <td class="label">Arrival Time (+1h)</td>
 <td class="data"><el:combo name="hourA" idx="*" size="1" options="${hours}" value="${fafCriteria.hourA}" onRightClick="return golgotha.form.resetCombo()" /></td>
</tr>
<tr>
 <td class="label">Sort Flights by</td>
 <td class="data"><el:combo name="sortType" idx="*" size="1" options="${sortTypes}" value="${param.sortType}" onChange="void golgotha.ff.updateSort(this)" /> <el:box ID="sortDesc" name="sortDesc" idx="*" value="true" checked="${param.sortDesc}" label="Descending" /></td>
 <td class="label">Maximum Results</td>
 <td class="data"><el:text name="maxResults" idx="*" size="2" max="3" value="${empty fafCriteria ? 25 : fafCriteria.maxResults}" autoComplete="false" /> total, 
 <el:text name="maxFlights" idx="*" size="2" max="2" value="${empty fafCriteria || (fafCriteria.flightsPerRoute < 0) ? '' : fafCriteria.flightsPerRoute}" autoComplete="false" /> preferred between airports</td>
</tr>
<tr>
 <td class="label">Frequency</td>
 <td class="data">Show Routes flown no more than <el:text name="maxRouteLegs" size="2" max="3" value="${(empty fafCriteria || (fafCriteria.routeLegs < 0)) ? '' : fafCriteria.routeLegs}" autoComplete="false" /> times</td>
 <td class="label">Recent Routes</td>
 <td class="data">Show Routes flown at least <el:text name="maxLastFlown" size="2" max="4" value="${(empty fafCriteria || (fafCriteria.lastFlownInterval < 0)) ? '' : fafCriteria.lastFlownInterval}" autoComplete="false" /> days ago</td>
</tr>
<tr>
 <td class="label top">Search Options</td>
 <td class="data top"><el:box name="myEQTypes" value="true" checked="${param.myEQTypes}" label="My rated Equipment Types" onChange="golgotha.airportLoad.config.myRated = this.checked" /><br />
<el:box name="showUTCTimes" value="true" checked="${param.showUTCTimes}" label="Show Departure/Arrival Times as UTC" />
<span id="historicOpts"><br />
Historic Flights - <el:combo name="historicOnly" options="${inclusionOpts}" value="${fafCriteria.excludeHistoric}" size="1" idx="*" /></span></td>
 <td class="label top">ACARS Dispatch</td>
 <td class="data top"><el:box name="checkDispatch" idx="*" value="true" checked="${empty fafCriteria ? true : fafCriteria.checkDispatch}" label="Display Dispatch route count" /><br />
Dispatch Flights - <el:combo name="dispatchOnly" options="${inclusionOpts}" value="${fafCriteria.dispatchOnly}" size="1" idx="*" /></td>
</tr>
<tr class="title mid">
 <td colspan="4"><el:button type="submit" label="SEARCH FLIGHT SCHEDULE" /></td>
</tr>
</el:table>
<el:text name="doSearch" type="hidden" value="true" />
</el:form>
<c:if test="${!empty fafResults}">
<c:set var="cspan" value="${hasLastFlown ? 9 : 10}" scope="page" />
<el:form method="post" action="buildassign.do" validate="return golgotha.form.wrap(golgotha.ff.buildValidate, this)">
<el:table className="view">
<!-- Search Results Data -->
<tr class="title caps">
 <td colspan="${cspan}" class="left"><span class="nophone"><fmt:int value="${fafResults.size()}" /> FLIGHT SCHEDULE </span>SEARCH RESULTS</td>
</tr>

<!-- Search Results Header Bar -->
<tr class="caps title">
 <td>ADD</td>
 <td style="width:12%">FLIGHT NUMBER</td>
 <td style="width:10%">EQUIPMENT</td>
 <td>AIRPORTS</td>
 <td style="width:8%">DEPARTS</td>
 <td class="nophone" style="width:8%">ARRIVES</td>
 <td class="nophone" style="width:6%">LENGTH</td>
 <td class="nophone" style="width:5%" title="ACARS Dispatch Routes">ROUTES</td>
 <td class="nophone">DISTANCE</td>
<c:if test="${hasLastFlight}" >
 <td class="nophone" title="Number of times flown">FLOWN</td>
</c:if>
</tr>

<!-- Search Results -->
<c:forEach var="flight" items="${fafResults}">
<c:set var="srcInfo" value="${scheduleSources[flight.source]}" scope="page" />
<c:set var="srcDate" value="${fn:dateFmt(srcInfo.effectiveDate, 'MM/dd/yyyy')}" scope="page" />
<c:set var="srcFlightInfo" value="${srcInfo.source.description}, ${srcDate}" scope="page" />
<c:if test="${!empty flight.remarks}"><c:set var="srcFlightInfo" value="${srcFlightInfo} - ${flight.remarks}" scope="page" /></c:if>
<view:row entry="${flight}">
 <td><el:box name="addFA" value="${flight.flightCode}" label="" /></td>
 <td class="pri bld" title="${srcFlightInfo}">${flight.flightCode}</td>
 <td class="sec bld">${flight.equipmentType}</td>
 <td class="small"><span class="nophone">${flight.airportD.name}&nbsp;(</span><el:cmd url="airportinfo" linkID="${flight.airportD.IATA}"><fmt:airport airport="${flight.airportD}" /></el:cmd><span class="nophone">)</span> -
<span class="nophone">${flight.airportA.name}&nbsp;(</span><el:cmd url="airportinfo" linkID="${flight.airportA.IATA}"><fmt:airport airport="${flight.airportA}" /></el:cmd><span class="nophone">)</span></td>
<c:if test="${param.showUTCTimes}">
 <td><fmt:date fmt="t" t="HH:mm" tzName="UTC" date="${flight.timeD}" /> UTC</td>
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tzName="UTC" date="${flight.timeA}" /> UTC</td>
</c:if>
<c:if test="${!param.showUTCTimes}"> 
 <td><fmt:date fmt="t" t="HH:mm" tz="${flight.airportD.TZ}" date="${flight.timeD}" /></td>
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tz="${flight.airportA.TZ}" date="${flight.timeA}" /></td>
</c:if>
 <td class="small nophone"><fmt:duration duration="${flight.duration}" t="HH:mm" /></td>
 <td class="small bld nophone"><fmt:int value="${flight.dispatchRoutes}" /></td>
 <td class="sec nophone"><fmt:distance value="${flight.distance}" /></td>
<c:if test="${hasLastFlight}" >
 <td class="pri bld nophone"<c:if test="${!empty flight.lastFlownOn}"> title="Last flown on <fmt:date date="${flight.lastFlownOn}" fmt="d" />"</c:if>><fmt:int value="${flight.flightCount}" /></td>
</c:if>
</view:row>
</c:forEach>

<!-- Button Bar -->
<tr class="title">
 <td colspan="${cspan}">SET EQUIPMENT <el:combo name="eqOverride" size="1" firstEntry="-" options="${myEQ}" /> <el:button type="submit" label="ADD TO FLIGHT ASSIGNMENT" />&nbsp;<el:cmdbutton url="resetassign" label="RESET RESULTS" /></td>
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
 <td class="small"><span class="nophone">${flight.airportD.name}</span> (<el:cmd url="airportinfo" linkID="${flight.airportD.IATA}"><fmt:airport airport="${flight.airportD}" /></el:cmd>) to <span class="nophone">${flight.airportA.name}</span>
  (<el:cmd url="airportinfo" linkID="${flight.airportA.IATA}"><fmt:airport airport="${flight.airportA}" /></el:cmd>)</td>
<c:if test="${param.showUTCTimes}">
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tzName="UTC" date="${flight.timeD}" /> UTC</td>
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tzName="UTC" date="${flight.timeA}" /><c:if test="${entry.arrivalPlusDays > 0}"> +${entry.arrivalPlusDays}</c:if> UTC</td>
</c:if>
<c:if test="${!param.showUTCTimes}"> 
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tz="${flight.airportD.TZ}" date="${flight.timeD}" /></td>
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tz="${flight.airportA.TZ}" date="${flight.timeA}" /><c:if test="${entry.arrivalPlusDays > 0}"> +${entry.arrivalPlusDays}</c:if></td>
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
