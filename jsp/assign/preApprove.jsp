<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Pre-Approval</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="airportRefresh" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.eqType, 'Equipment Type')) return false;
if (!validateNumber(form.flight, 1, 'Flight Number')) return false;
if (!validateNumber(form.leg, 1, 'Flight Leg')) return false;
if (!validateCombo(form.airline, 'Airline')) return false;
if (!validateCombo(form.airportD, 'Departure Airport')) return false;
if (!validateCombo(form.airportA, 'Arrival Airport')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="preapprove.do" method="post" linkID="${pilotID}" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">UNSCHEDULED FLIGHT APPROVAL - ${assignPilot.name}</td>
</tr>
<tr>
 <td class="label">Aircraft Type</td>
 <td class="data"><el:combo name="eqType" size="1" idx="*" className="req" options="${eqTypes}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" className="req" options="${airlines}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Flight / Leg</td>
 <td class="data"><el:text name="flight" idx="*" size="4" max="5" className="pri bld req" value="${assignPilot.pilotNumber}" />
 <el:text name="leg" idx="*" size="1" max="1" className="req" value="1" /></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" size="1" idx="*" className="req" options="${airports}" firstEntry="-" onChange="void changeAirport(this)" />
 <el:text name="airportDCode" size="4" max="4" idx="*" value="" onBlur="void setAirport(document.forms[0].airportD, this.value)" /></td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" size="1" idx="*" className="req" options="${airports}" firstEntry="-" onChange="void changeAirport(this)" />
 <el:text name="airportACode" size="4" max="4" idx="*" value="" onBlur="void setAirport(document.forms[0].airportA, this.value)" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE FLIGHT PRE-APPROVAL" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
