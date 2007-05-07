<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Flight Routes for Online Event - ${event.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="airportRefresh" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.route, 6, 'Flight Route')) return false;
if (!validateCombo(form.airportD, 'Departure Airport')) return false;
if (!validateCombo(form.airportA, 'Destination Airport')) return false;

setSubmit();
disableButton('SaveButton');
disableButton('ViewButton');
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
<c:set var="entryNumber" value="${0}" scope="request" />
<c:forEach var="route" items="${event.routes}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="request" />
<tr>
 <td class="label" valign="top" rowspan="2">Route #<fmt:int value="${entryNumber}" /></td>
 <td class="data" colspan="3">${route.airportD.name} (<fmt:airport airport="${route.airportD}" />) 
- ${route.airportA.name} (<fmt:airport airport="${route.airportA}" />) 
<el:cmdbutton url="eventroutes" op="save&isDelete=true&route=${route.airportD.IATA}-${route.airportA.IATA}" link="${event}" label="DELETE" />
&nbsp;
<el:cmdbutton url="eventroutes" op="save&isToggle=true&route=${route.airportD.IATA}-${route.airportA.IATA}" link="${event}" label="${route.active ? 'DISABLE' : 'ENALBE'}" /></td>
</tr>
<tr>
 <td class="data" colspan="3">${route.route}</td>
</tr>
</c:forEach>

<!-- Add new Route -->
<tr class="title caps">
 <td colspan="4">ADD NEW FLIGHT ROUTE</td>
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
 <td class="label">Flight Route</td>
 <td class="data" colspan="3"><el:text name="route" idx="*" size="110" max="640" value="" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="ADD NEW FLIGHT ROUTE" />
 <el:cmdbutton ID="ViewButton" url="event" link="${event}" label="VIEW EVENT" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
