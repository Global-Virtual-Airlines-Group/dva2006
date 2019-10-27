<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<c:set var="reqType" value="${isRating ? 'Additional Rating' : 'Equipment Transfer'}" scope="page" />
<html lang="en">
<head>
<title>New <content:airline />&nbsp;${reqType} Request</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:json />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.eqType, t:'Equipment Program to transfer into'});
	golgotha.form.validate({f:f.sim, t:'Preferred Simulator version'});
	golgotha.form.submit(f);
	return true;
};

golgotha.local.loadAircraft = function() {
	const xreq = new XMLHttpRequest();
	xreq.open('get', 'crsims.ws');
	xreq.onreadystatechange = function() {
		if ((xreq.readyState != 4) || (xreq.status != 200)) return false;
		golgotha.local.eqACMap = JSON.parse(xreq.responseText);
		return true;
	};

	xreq.send(null);
};

golgotha.local.updateAircraft = function(combo) {
	const f = combo.form; const acc = f.acType; const eq = golgotha.form.getCombo(combo);
	const acTypes = golgotha.local.eqACMap[eq];
	if (!acTypes || !golgotha.form.comboSet(f.sim)) {
		golgotha.util.display('acType', false);
		acc.required = false;
		return false;
	}
	
	// Get check rides for sim
	let simTypes = acTypes[golgotha.form.getCombo(f.sim)];
	simTypes = (simTypes instanceof Array) ? simTypes : [simTypes];
	acc.options.length = simTypes.length + 1;
	for (var x = 0; x < simTypes.length; x++)
		acc.options[x + 1] = new Option(simTypes[x], simTypes[x]);

	golgotha.util.display('acType', (simTypes.length > 1));
	acc.selectedIndex = (simTypes.length > 1) ? 0 : 1;
	acc.required = (simTypes.length > 1);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onload="void golgotha.local.loadAircraft()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:empty var="emptyList" />
<c:if test="${empty availableSims}">
<content:enum var="availableSims" className="org.deltava.beans.Simulator" exclude="UNKNOWN,FS98,FS2000,FS2002,XP9" /></c:if>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="txrequest.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">NEW ${reqType} REQUEST</td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" options="${availableEQ}" required="true" firstEntry="[ EQUIPMENT PROGRAM ]" onChange="void golgotha.local.updateAircraft(this)" /></td>
</tr>
<tr>
 <td class="label">Preferred Simulator</td>
 <td class="data"><el:combo name="sim" size="1" idx="*" required="true" firstEntry="[ SELECT SIMULATOR ]" options="${availableSims}" onChange="void golgotha.local.updateAircraft(this.form.eqType)" /></td>
</tr>
<tr id="acType" style="display:none;">
 <td class="label">Preferred Aircraft</td>
 <td class="data"><el:combo name="acType" idx="*" size="1" options="${emptyList}" required="true" firstEntry="[ AIRCRAFT TYPE ]" /><span class="small ita nophone"> (Select the aircraft variant you would prefer to use for the check ride.)</span></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data">Don't see an equipment program listed? <el:cmd url="promoeligibility" className="sec bld">Click Here</el:cmd> to see what equipment programs you are eligible to switch to or request additional ratings.</td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SubmitButton" type="submit" label="SUBMIT ${reqType} REQUEST" /></td>
</tr>
</el:table>
<c:if test="${isRating}"><el:text name="ratingOnly" type="hidden" value="true" /></c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
