<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Accomplishment<c:if test="${!empty ap}"> - ${ap.name}</c:if></title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:js name="jsColor" />
<content:sysdata var="forumName" name="airline.forum" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.name, l:6, t:'Accomplishment Name'});
golgotha.form.validate({f:f.units, t:'Units of Measurement'});
golgotha.form.validate({f:f.value, min:0, t:'Number of Units'});
golgotha.form.validate({f:f.color, l:6, t:'${forumName} label color'});
golgotha.form.submit(f);
return true;
};

golgotha.local.toggleAll = function() {
    var f = document.forms[0];	
    golgotha.util.disable(f.value, f.doAll.checked);	
    return true;	
};

golgotha.local.showChoices = function()
{
var rows = golgotha.util.getElementsByClass('valueRow');
for (var r = rows.pop(); (r != null); r = rows.pop())
	golgotha.util.display(r, false);

golgotha.util.display('chkAll', true);
golgotha.local.toggleAll();
var f = document.forms[0];
var c = golgotha.form.getCombo(f.units);
switch (c) {
case 'COUNTRIES':
	golgotha.util.display('valueCountry', true);
	break;

case 'STATES':
	golgotha.util.display('valueState', true);
	break;

case 'AIRLINES':
	golgotha.util.display('valueAirline', true);
	break;
	
case 'CONTINENTS':
	golgotha.util.display('valueCont', true);
	break;

case 'AIRCRAFT':
case 'EQLEGS':
	golgotha.util.display('valueEQType', true);
	break;
	
case 'PROMOLEGS':
	golgotha.util.display('valueEQProgram', true);
	break;
	
case 'ADLEGS':
case 'AALEGS':
	golgotha.util.display('chkAll', false);
	golgotha.util.display('valueBox', true);
	golgotha.util.disable(f.value, false);
	break;

default:
	golgotha.util.display('valueBox', true);
	break;
}

return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onload="golgotha.local.showChoices(); golgotha.local.toggleAll()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="accomplishment.do" method="post" link="${ap}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">PILOT ACCOMPLISHMENT PROFILE<c:if test="${!empty ap}"> - ${ap.name}</c:if></td>
</tr>
<tr>
 <td class="label">Accomplishment</td>
 <td class="data"><el:text name="name" idx="*" size="32" max="32" className="pri bld req" value="${ap.name}" /></td>
</tr>
<tr>
 <td class="label">Units</td>
 <td class="data"><el:combo name="units" idx="*" options="${units}" size="1" className="req" firstEntry="-" value="${ap.unit}" onChange="void golgotha.local.showChoices()" /></td>
</tr>
<tr>
 <td class="label">Number of Units</td>
 <td class="data"><el:text name="value" idx="*" size="7" max="8" className="bld req" value="${ap.value}" /><span id="chkAll" > <el:box name="doAll" value="true" checked="${ap.value == ap.choices.size()}" label="All" onChange="void golgotha.local.toggleAll()" /></span></td>
</tr>
<tr>
 <td class="label">Label color</td>
 <td class="data"><el:text name="color" idx="*" className="color bld req" size="6" max="8" value="${ap.hexColor}" /> <span class="small">Click on the text box for a color picker.</span></td>
</tr>
<tr id="valueBox" style="display:none;" class="valueRow">
 <td class="label top">Valid Values</td>
 <td class="data"><el:textbox name="choices" idx="*" width="80%" height="3" resize="true">${fn:splice(ap.choices, ', ')}</el:textbox></td>
</tr>
<tr id="valueCountry" style="display:none;" class="valueRow">
 <td class="label top">Valid Countries</td>
 <td class="data"><el:check name="countries" idx="*" width="190" cols="5" className="small" newLine="true" checked="${ap.choices}" options="${activeCountries}"/>
<div style="clear:both;"></div><hr />
<el:check name="countries" idx="*" width="190" cols="5" className="small ita" newLine="true" checked="${ap.choices}" options="${inactiveCountries}"/></td>
</tr>
<tr id="valueCont" style="display:none;" class="valueRow">
 <td class="label top">Valid Continents</td>
 <td class="data"><el:check name="continents" idx="*" width="150" cols="6" checked="${ap.choices}" options="${continents}" /></td>
</tr>
<tr id="valueState" style="display:none;" class="valueRow">
 <td class="label top">Valid States</td>
 <td class="data"><el:check name="states" idx="*" width="120" cols="8" newLine="true" checked="${ap.choices}" options="${states}" /></td>
</tr>
<tr id="valueAirline" style="display:none;" class="valueRow">
 <td class="label top">Valid Airlines</td>
 <td class="data"><el:check name="airlines" idx="*" width="200" cols="4" newLine="true" checked="${ap.choices}" options="${airlines}" /></td>
</tr>
<tr id="valueEQType" style="display:none;" class="valueRow">
 <td class="label top">Valid Aircraft</td>
 <td class="data"><el:check name="eqTypes" idx="*" width="105" cols="7" newLine="true" checked="${ap.choices}" options="${allEQ}" /></td>
</tr>
<tr id="valueEQProgram" style="display:none;" class="valueRow">
 <td class="label top">Valid Equipment Program</td>
 <td class="data"><el:check name="eqPrograms" idx="*" width="105" cols="5" newLine="true" checked="${ap.choices}" options="${eqPrograms}" /></td>
</tr>
<c:if test="${!empty ap}">
<tr>
 <td class="label">Pilots</td>
 <td class="data">
<c:choose>
<c:when test="${ap.pilots == 0}"><span class="bld">No <content:airline /> Pilots have achieved this Accomplishment</span></c:when>
<c:otherwise><span class="pri bld"><fmt:int value="${ap.pilots}" /> <content:airline /> Pilots have achieved this Accomplishment</span></c:otherwise>
</c:choose></td>
</tr>
</c:if>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="alwaysDisplay" idx="*" value="true" checked="${ap.alwaysDisplay}"  label="Always display Accomplishment completion" /><br />
<el:box name="active" idx="*" value="true" className="bld" checked="${ap.active}" label="Accomplishment is Active" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE ACCOMPLISHMENT PROFILE" />
<c:if test="${access.canDelete}"> <el:cmdbutton ID="DeleteButton" url="accdelete" link="${ap}" label="DELETE ACCOMPLISHMENT PROFILE" /></c:if></td>
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
