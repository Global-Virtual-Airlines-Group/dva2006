<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Assign Check Ride for ${pilot.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
<fmt:js var="golgotha.local.eqAircraft" object="${eqAircraft}" />
golgotha.local.updateEQ = function(combo) {
	var eq = golgotha.form.getCombo(combo);
	var acTypes = golgotha.local.eqAircraft[eq];
	var acc = document.forms[0].eqType;
	acc.options.length = acTypes.length + 1;
	acc.selectedIndex = 0;
	for (var x = 0; x < acTypes.length; x++)
		acc.options[x + 1] = new Option(acTypes[x], acTypes[x]);

	return true;
};

golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.crType, t:'Aircraft Type'});
	golgotha.form.validate({f:f.eqType, t:'Equimpment Program'});
	var hasScript = ((f.doScript) && (f.doScript.value == 'true'));
	if (!hasScript)
		golgotha.form.validate({f:f.comments, l:6, t:'Check Ride Comments'});

	golgotha.form.submit(f);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onload="void golgotha.local.updateEQ(document.forms[0].eqType)">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="nakedassign.do" method="post" link="${pilot}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">CHECK RIDE - ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" firstEntry="-" options="${eqTypes}" value="${param.eqType}" onChange="void golgotha.local.updateEQ(this)" /></td>
</tr>
<tr>
 <td class="label">Aircraft Type</td>
 <td class="data"><el:combo name="crType" idx="*" size="1" firstEntry="-" options="${empty eqType ? actypes : eqType.primaryRatings}" value="${param.crType}" /></td>
</tr>
<tr>
 <td class="label top">Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="80%" height="3" resize="true"></el:textbox></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="useScript" idx="*" value="true" checked="true" label="Append Check Ride script to comments" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:cmdbutton ID="ProfileButton" url="profile" link="${pilot}" label="VIEW PROFILE" /> 
<el:button ID="AssignButton" type="submit" label="ASSIGN CHECK RIDE" /></td>
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
