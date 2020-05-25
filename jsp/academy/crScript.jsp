<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Flight Academy Check Ride Script</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:json />
<script async>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.cert, t:'Flight Academy Certification'});
golgotha.form.validate({f:f.seq, t:'Check Ride Number'});
golgotha.form.validate({f:f.body, l:15, t:'Check Ride content'});
golgotha.form.validate({f:f.sims, min:1, t:'Available Simulator'});
golgotha.form.submit(f);
return true;
};

golgotha.local.loadRideCount = function(combo)
{
const f = document.forms[0]; var ic = f.seq;
if (combo.selectedIndex < 1) {
	ic.options.length = 1;
	return false;
}

const xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'ridecount.ws?id=' + golgotha.form.getCombo(combo), true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	const jsData = (xmlreq.status == 200) ? JSON.parse(xmlreq.responseText) : [1];

	// Set combobox options
	ic.options.length = jsData.length + 1;
	for (var x = 0; x < jsData.length; x++)
		ic.options[x+1] = new Option(jsData[x], jsData[x]);

	golgotha.util.display('noNewRides', (jsData.length == 0));
	golgotha.util.display('bodyRow', (jsData.length > 0));
	return true;
};

xmlreq.send(null);
return true;
};

golgotha.onDOMReady(function() { golgotha.form.resizeAll(); });
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>
<content:empty var="emptyList" />
<content:enum var="fsVersions" className="org.deltava.beans.Simulator" exclude="UNKNOWN,FS98,FS2000,FS2002,XP9" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="arscript.do" op="save" linkID="${sc.ID}" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Title Bar -->
<tr class="title caps">
 <td colspan="2">FLIGHT ACADEMY CHECK RIDE SCRIPT<c:if test="${!empty sc}"> - ${sc.certificationName} #<fmt:int value="${sc.index}" /></c:if></td>
</tr>
<c:if test="${empty sc}">
<tr>
 <td class="label">Certification</td>
 <td class="data"><el:combo name="cert" idx="*" size="1" required="true" options="${certs}" firstEntry="-" value="${sc.certificationName}" onChange="void golgotha.local.loadRideCount(this)" /></td>
</tr>
<tr>
 <td class="label">Check Ride #</td>
 <td class="data"><el:combo name="seq" idx="*" size="1" required="true" options="${emptyList}" firstEntry="-" value="${sc.idx}" /></td>
</tr>
<tr>
 <td class="label top">Available Simulators</td>
 <td class="data"><el:check name="sims" idx="*" width="200" cols="3" newLine="true" className="small" checked="${sc.simulators}" options="${fsVersions}" /></td>
</tr>
<tr id="noNewRides" style="display:none;">
 <td class="error mid bld caps" colspan="2">All Check Ride Scripts for this Flight Academy Course have been created. You must edit or delete an existing Check Ride Script.</td>
</tr>
</c:if>
<tr id="bodyRow"<c:if test="${empty sc}"> style="display:none;"</c:if>>
 <td class="label top">Script Text</td>
 <td class="data"><el:textbox name="body" idx="*" width="90%" height="5" resize="true" className="req">${sc.description}</el:textbox></td>
</tr>
<%@ include file="/jsp/auditLog.jspf" %> 
</el:table>

<!-- Button bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE CHECK RIDE SCRIPT" />
<c:if test="${access.canDelete}">&nbsp;<el:cmdbutton url="arscriptdelete" linkID="${sc.certificationName}" label="DELETE CHECK RIDE SCRIPT" /></c:if></td>
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
