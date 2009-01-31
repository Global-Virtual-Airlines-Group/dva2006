<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Flight Routes for Online Event - ${event.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<script language="JavaScript" type="text/javascript">
<fmt:jsarray var="routeIDs" items="${routeIDs}" />

function validate(form)
{
if (!checkSubmit()) return false;

// Validate existing routes
for (var id in routeIDs) {
	if (!validateNumber(eval('form.maxSignups' + id), 0, 'Maximum Signups for Route #' + id)) return false; 
	if (!validateText(eval('form.route' + id), 6, 'Flight Route #' + id)) return false;
	if (!validateText(eval('form.routeName' + id), 6, 'Flight Route Name #' + id)) return false;
}

// Check if we're adding a new route
var hasNewRoute = ((form.route.value.length > 0) || (form.routeName.value.length > 0) ||
		(form.airportD.selectedIndex > 0) || (form.airportA.selectedIndex > 0));
if (hasNewRoute)
{
	if (!validateText(form.route, 6, 'Flight Route')) return false;
	if (!validateText(form.routeName, 6, 'Flight Route Name')) return false;
	if (!validateCombo(form.airportD, 'Departure Airport')) return false;
	if (!validateCombo(form.airportA, 'Destination Airport')) return false;
	if (!validateNumber(form.maxSignups, 0, 'Maximum Signups')) return false;
}

setSubmit();
disableButton('SaveButton');
disableButton('ViewButton');
disableButton('BalanceButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="eventroutes.do" link="${event}" op="save" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="4">FLIGHT ROUTES FOR ${event.name}</td>
</tr>
<c:forEach var="route" items="${event.routes}">
<c:set var="hasName" value="${!empty route.name}" scope="request" />
<c:set var="toggleBoxClass" value="${route.active ? 'warn' : 'ter'}" scope="request" />
<tr>
 <td class="label" valign="top" rowspan="${hasName ? '4' : '3'}">Route #<fmt:int value="${route.routeID}" /></td>
<c:if test="${hasName}">
 <td colspan="3" class="data"><el:text name="routeName${route.routeID}" idx="*" className="pri bld req" size="80" max="144" value="${route.name}" /></td> 
</tr>
</c:if>
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
 <td class="label" valign="top">Maximum Signups</td>
 <td class="data" valign="top"><el:text name="maxSignups${route.routeID}" idx="*" className="req" size="2" max="3" value="${route.maxSignups}" />
 <i><fmt:int value="${route.signups}" /> Pilots signed up</i></td>
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
<el:text name="adCode" idx="*" size="3" max="4" onBlur="void setAirport(document.forms[0].airportD, this.value)" /></td>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" idx="*" size="1" options="${airports}" firstEntry="-" />&nbsp;
<el:text name="aaCode" idx="*" size="3" max="4" onBlur="void setAirport(document.forms[0].airportA, this.value)" /></td>
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
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="UPDATE FLIGHT ROUTES" />
 <el:cmdbutton ID="ViewButton" url="event" link="${event}" label="VIEW EVENT" />
<c:if test="${access.canBalance}">
 <el:cmdbutton ID="BalanceButton" url="eventbalance" link="${event}" label="BALANCE ROUTE SIGNUPS" />
</c:if></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
