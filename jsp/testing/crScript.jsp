<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Check Ride Script</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script async>
<fmt:js var="golgotha.local.eqACMap" object="${acTypes}" />
golgotha.local.validate = function(f) {
    if (!golgotha.form.check()) return false;
    golgotha.form.validate({f:f.eqType, t:'Aircraft Type'});
    golgotha.form.validate({f:f.programType, t:'Equipment Program'});
    golgotha.form.validate({f:f.sims, min:1, t:'Available Simulator'});
    golgotha.form.validate({f:f.msgText, l:15, t:'Check Ride content'});
    golgotha.form.submit(f);
    return true;
};

golgotha.local.updateEQ = function(combo) {
	const eq = golgotha.form.getCombo(combo);
	const acTypes = golgotha.local.eqACMap[eq];
	const acc = document.forms[0].eqType;
	acc.options.length = acTypes.length + 1;
	acc.selectedIndex = 0;
	for (var x = 0; x < acTypes.length; x++)
		acc.options[x + 1] = new Option(acTypes[x], acTypes[x]);

	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onload="void golgotha.form.resizeAll()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:empty var="emptyList" />
<content:sysdata var="currencyEnabled" name="testing.currency.enabled" />
<content:enum var="fsVersions" className="org.deltava.beans.Simulator" exclude="UNKNOWN,FS98,FS2000,FS2002,XP9" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="crscript.do" op="save" linkID="${script.auditID}" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Title Bar -->
<tr class="title caps">
 <td colspan="2"><content:airline /> CHECK RIDE SCRIPT</td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><el:combo name="programType" idx="*" size="1" required="true" options="${eqTypes}" firstEntry="[ EQUIPMENT PROGRAM ]" value="${script.program}" onChange="void golgotha.local.updateEQ(this)" /></td>
</tr>
<tr>
 <td class="label">Aircraft Type</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" required="true" options="${(empty script) ? emptyList : acTypes[script.program]}" firstEntry="[ AIRCRAFT TYPE ]" value="${script.equipmentType}" /></td>
</tr>
<tr>
 <td class="label top">Available Simulators</td>
 <td class="data"><el:check name="sims" idx="*" width="200" cols="3" newLine="true" className="small" checked="${script.simulators}" options="${fsVersions}" /></td>
</tr>
<c:if test="${currencyEnabled}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isCurrency" value="true" className="small" label="This is a Currency check ride script" checked="${script.isCurrency}" /></td> 
</tr>
</c:if>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isDefault" value="true" className="small" label="This is the default check ride script for the Equipment Program" checked="${script.isDefault}" /></td>
</tr>
<tr>
 <td class="label top">Script Text</td>
 <td class="data"><el:textbox name="msgText" idx="*" width="90%" height="5" resize="true" className="req">${script.description}</el:textbox></td>
</tr>
<%@ include file="/jsp/auditLog.jspf" %>
</el:table>

<!-- Button bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE CHECK RIDE SCRIPT" /><c:if test="${access.canDelete}">&nbsp;<el:cmdbutton url="crscriptdelete" linkID="${script.equipmentType}" label="DELETE CHECK RIDE SCRIPT" /></c:if></td>
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
