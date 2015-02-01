<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Aircraft<c:if test="${!empty aircraft}"> - ${aircraft.name}</c:if></title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.name, l:3, t:'Aircraft Name'});
golgotha.form.validate({f:f.fullName, l:5, t:'Aircraft Full Name'});
golgotha.form.validate({f:f.family, l:2, t:'Aircraft Family Code'});
golgotha.form.validate({f:f.range, min:1, t:'Aircraft Range'});
golgotha.form.validate({f:f.seats, min:0, t:'Passenger Capacity'});
golgotha.form.validate({f:f.maxWeight, min:1, t:'Maximum Weight'});
golgotha.form.validate({f:f.maxZFW, min:1, t:'Maximum Zero Fuel Weight'});
golgotha.form.validate({f:f.maxTWeight, min:1, t:'Maximum Takeoff Weight'});
golgotha.form.validate({f:f.maxLWeight, min:1, t:'Maximum Landing Weight'});
golgotha.form.validate({f:f.toRunwayLength, min:0, t:'Minimum Takeoff Runway Length'});
golgotha.form.validate({f:f.lndRunwayLength, min:0, t:'Minimum Landing Runway Length'});
golgotha.form.validate({f:f.engineCount, min:1, t:'Engine Count'});
golgotha.form.validate({f:f.engineType, l:4, t:'Engine Type'});
golgotha.form.validate({f:f.cruiseSpeed, min:50, t:'Cruise Speed'});
golgotha.form.validate({f:f.fuelFlow, min:100, t:'Fuel Flow'});
golgotha.form.validate({f:f.baseFuel, min:0, t:'Base Fuel Amount'});
golgotha.form.validate({f:f.taxiFuel, min:0, t:'Taxi Fuel Amount'});
golgotha.form.validate({f:f.pTanks, min:1, t:'Primary Fuel Tanks'});
golgotha.form.submit();
disableButton('SaveButton');
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:sysdata var="airlines" name="apps" mapValues="true" />
<content:enum var="tankNames" className="org.deltava.beans.schedule.FuelTank" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="aircraft.do" method="post" linkID="${aircraft.name}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">AIRCRAFT PROFILE</td>
</tr>
<tr>
 <td class="label">Aircraft</td>
 <td class="data"><el:text name="name" idx="*" className="pri bld" required="true" size="15" max="15" value="${aircraft.name}" /></td>
</tr>
<tr>
 <td class="label">Full Aircraft Name</td>
 <td class="data"><el:text name="fullName" idx="*" required="true" size="32" max="48" value="${aircraft.fullName}" /></td>
</tr>
<tr>
 <td class="label">Aircraft Family Code</td>
 <td class="data"><el:text name="family" idx="*" required="true" size="8" max="8" value="${aircraft.family}" /></td>
</tr>
<tr>
 <td class="label">Maximum Range</td>
 <td class="data"><el:text name="range" idx="*" required="true" size="4" max="5" value="${aircraft.range}" /> miles</td>
</tr>
<tr>
 <td class="label">Minimum Takeoff Runway Length</td>
 <td class="data"><el:text name="toRunwayLength" idx="*" required="true" size="4" max="5" value="${aircraft.takeoffRunwayLength}" /> feet</td>
</tr>
<tr>
 <td class="label">Minimum Landing Runway Length</td>
 <td class="data"><el:text name="lndRunwayLength" idx="*" required="true" size="4" max="5" value="${aircraft.landingRunwayLength}" /> feet</td>
</tr>
<tr>
 <td class="label">Passenger Capacity</td>
 <td class="data"><el:text name="seats" idx="*" required="true" size="3" max="3" value="${aircraft.seats}" /> seats</td>
</tr>
<tr>
 <td class="label top">IATA Equipment Code(s)</td>
 <td class="data"><el:textbox name="iataCodes" idx="*" width="30" height="3">${iataCodes}</el:textbox></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isHistoric" idx="*" value="true" checked="${aircraft.historic}" label="This is a Historic Aircraft" /><br />
<el:box name="isETOPS" idx="*" value="true" checked="${aircraft.ETOPS}" label="This Aircraft is ETOPS-rated" /></td>
</tr>
<tr>
 <td class="label top">Web Applications</td>
 <td class="data"><el:check name="airlines" width="180" options="${airlines}" checked="${aircraft.apps}" /></td>
</tr>
<tr class="title caps">
 <td colspan="2">AIRCRAFT WEIGHTS</td>
</tr>
<tr>
 <td class="label">Maximum Weight</td>
 <td class="data"><el:text name="maxWeight" idx="*" size="6" max="7" value="${aircraft.maxWeight}" required="true" /> pounds</td>
</tr>
<tr>
  <td class="label">Maximum Zero Fuel Weight</td>
  <td class="data"><el:text name="maxZFW" idx="*" size="6" max="7" value="${aircraft.maxZeroFuelWeight}" required="true" /> pounds</td>
</tr>
<tr>
 <td class="label">Maximum Takeoff Weight</td>
 <td class="data"><el:text name="maxTWeight" idx="*" size="6" max="7" value="${aircraft.maxTakeoffWeight}" required="true" /> pounds</td>
</tr>
<tr>
 <td class="label">Maximum Landing Weight</td>
 <td class="data"><el:text name="maxLWeight" idx="*" size="6" max="7" value="${aircraft.maxLandingWeight}" required="true" /> pounds</td>
</tr>
<tr class="title caps">
 <td colspan="2">ACARS FUEL PROFILE</td>
</tr>
<tr>
 <td class="label">Engine Information</td>
 <td class="data"><el:text name="engineCount" idx="*" size="1" max="1" value="${aircraft.engines}" className="bld" required="true" />
 x <el:text name="engineType" idx="*" size="16" max="32" value="${aircraft.engineType}" required="true" /></td>
</tr>
<tr>
 <td class="label">Cruise Speed</td>
 <td class="data"><el:text name="cruiseSpeed" idx="*" size="3" max="4" value="${aircraft.cruiseSpeed}" required="true" /> knots</td>
</tr>
<tr>
 <td class="label">Fuel Flow</td>
 <td class="data"><el:text name="fuelFlow" idx="*" size="3" max="5" value="${aircraft.fuelFlow}" required="true" />
 pounds per engine per hour</td>
</tr>
<tr>
 <td class="label">Base Fuel</td>
 <td class="data"><el:text name="baseFuel" idx="*" size="3" max="5" value="${aircraft.baseFuel}" required="true" /> pounds</td>
</tr>
<tr>
 <td class="label">Taxi Fuel</td>
 <td class="data"><el:text name="taxiFuel" idx="*" size="3" max="5" value="${aircraft.taxiFuel}" required="true" /> pounds</td>
</tr>
<tr>
 <td class="label top">Primary Tanks</td>
 <td class="data"><el:check name="pTanks" idx="*" width="100" cols="6" newLine="true" checked="${aircraft.tankNames['Primary']}" options="${tankNames}" /></td>
</tr>
<tr>
 <td class="label">Primary Percentage</td>
 <td class="data">Fill to <el:text name="pPct" idx="*" size="2" max="3" value="${aircraft.tankPercent['Primary']}" required="true" />
 percent before filling Secondary tanks</td>
</tr>
<tr>
 <td class="label top">Secondary Tanks</td>
 <td class="data"><el:check name="sTanks" idx="*" width="100" cols="6" newLine="true" checked="${aircraft.tankNames['Secondary']}" options="${tankNames}" /></td>
</tr>
<tr>
 <td class="label">Secondary Percentage</td>
 <td class="data">Fill to <el:text name="sPct" idx="*" size="2" max="3" value="${aircraft.tankPercent['Secondary']}" required="true" />
 percent before filling Other tanks</td>
</tr>
<tr>
 <td class="label top">Other Tanks</td>
 <td class="data"><el:check name="oTanks" idx="*" width="100" cols="6" newLine="true" checked="${aircraft.tankNames['Other']}" options="${tankNames}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE AIRCRAFT PROFILE" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
