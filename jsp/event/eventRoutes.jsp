<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Flight Routes for Online Event - ${event.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:json />
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<fmt:aptype var="useICAO" />
<script>
<fmt:jsarray var="golgotha.local.routeIDs" items="${routeIDs}" />
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;

// Validate existing routes
for (var id = golgotha.local.routeIDs.pop(); (id != null); id = golgotha.local.routeIDs.pop()) {
	golgotha.form.validate({f:f['maxSignups' + id], min:0, t:'Maximum Signups for Route #' + id}); 
	golgotha.form.validate({f:f['route' + id], l:6, t:'Flight Route #' + id});
	golgotha.form.validate({f:f['routeName' + id], l:6, t:'Flight Route Name #' + id});
}

// Check if we're adding a new route
const hasNewRoute = ((f.route.value.length > 0) || (f.routeName.value.length > 0) || golgotha.form.comboSet(f.airportD) || golgotha.form.comboSet(f.airportA));
if (hasNewRoute) {
	golgotha.form.validate({f:f.route, l:6, t:'Flight Route'});
	golgotha.form.validate({f:f.routeName, l:6, t:'Flight Route Name'});
	golgotha.form.validate({f:f.airportD, t:'Departure Airport'});
	golgotha.form.validate({f:f.airportA, t:'Destination Airport'});
	golgotha.form.validate({f:f.maxSignups, min:0, t:'Maximum Signups'});
}

golgotha.form.submit(f);
return true;
};

golgotha.onDOMReady(function() {
	const f = document.forms[0];
	golgotha.airportLoad.config.doICAO = ${useICAO};
	golgotha.airportLoad.setHelpers([f.airportD,f.airportA]);
});
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="eventroutes.do" link="${event}" op="save" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="4">FLIGHT ROUTES FOR <el:cmd url="event" link="${event}">${event.name}</el:cmd></td>
</tr>
<c:forEach var="route" items="${event.routes}">
<c:set var="hasName" value="${!empty route.name}" scope="page" />
<c:set var="toggleBoxClass" value="${route.active ? 'warn' : 'ter'}" scope="page" />
<tr>
 <td class="label top" rowspan="${hasName ? '4' : '3'}">Route #<fmt:int value="${route.routeID}" /></td>
<c:if test="${hasName}">
 <td colspan="3" class="data"><el:text name="routeName${route.routeID}" idx="*" className="pri bld req" size="80" max="144" value="${route.name}" /></td> 
</c:if>
</tr>
<tr>
 <td class="data" colspan="3">${route.airportD.name} (<fmt:airport airport="${route.airportD}" />) 
- ${route.airportA.name} (<fmt:airport airport="${route.airportA}" />)</td>
</tr>
<tr>
 <td class="data" colspan="3"><el:text name="route${route.routeID}" idx="*" className="req" size="160" max="640" value="${route.route}" /></td>
</tr>
<tr>
 <td class="data"><el:box name="isRNAV${route.routeID}" idx="*" value="true" className="small" label="This is an RNAV Route" checked="${route.isRNAV}" /><br />
<el:box name="disable${route.routeID}" value="true" className="small ${toggleBoxClass}" label="${route.active ? 'Disable' : 'Enable'} this Route" /><br />
<el:box name="delete${route.routeID}" value="true" className="small bld" label="Delete this Route" /></td>
 <td class="label top">Maximum Signups</td>
 <td class="data top"><el:text name="maxSignups${route.routeID}" idx="*" className="req" size="2" max="3" value="${route.maxSignups}" />
 <span class="ita"><fmt:int value="${route.signups}" /> Pilots signed up</span></td>
</tr>
</c:forEach>

<!-- Add new Route -->
<tr class="title caps">
 <td colspan="4">ADD NEW FLIGHT ROUTE</td>
</tr>
<tr>
 <td class="label">Route Name</td>
 <td class="data" colspan="3"><el:text name="routeName" idx="*" size="80" max="144" className="bld req" value="" /></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" idx="*" size="1" options="${airports}" firstEntry="-" />&nbsp;
<el:text name="adCode" idx="*" size="3" max="4" onBlur="void document.forms[0].airportD.setAirport(this.value)" /></td>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" idx="*" size="1" options="${airports}" firstEntry="-" />&nbsp;
<el:text name="aaCode" idx="*" size="3" max="4" onBlur="void document.forms[0].airportA.setAirport(this.value)" /></td>
</tr>
<tr>
 <td class="label">Maximum Signups</td>
 <td class="data"><el:text name="maxSignups" idx="*" size="2" max="4" value="" /></td>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isRNAV" idx="*" value="true" label="This is an RNAV Route" checked="false" /></td>
</tr>
<tr>
 <td class="label">Flight Route</td>
 <td class="data" colspan="3"><el:text name="route" idx="*" size="160" max="640" className="req" value="" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="UPDATE FLIGHT ROUTES" /><c:if test="${access.canBalance}">&nbsp;<el:cmdbutton url="eventbalance" link="${event}" label="BALANCE ROUTE SIGNUPS" /></c:if></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
