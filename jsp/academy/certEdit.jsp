<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<c:set var="certName" value="${empty cert ? 'New Certification' : cert.name}" scope="page" />
<html lang="en">
<head>
<title><content:airline /> Flight Academy - ${certName}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
golgotha.local.showReqCert = function(combo) {
	const opt = combo.options[combo.selectedIndex];
	golgotha.util.display('reqCertRow', (opt.text == 'Specific Certification'));
	golgotha.util.display('reqEQRow', opt.text.startsWith('Flight '));
	return true;	
};

golgotha.local.showNetworkRating = function(combo) {
	golgotha.util.display('ratingCodeRow', (combo.selectedIndex > 0));
	return true;
};

golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.name, l:10, t:'Certification Name'});
golgotha.form.validate({f:f.stage, min:1, t:'Certification Stage'});
golgotha.form.validate({f:f.preReqs, t:'Examination Prerequisites'});

// Check specific cert
const reqCertRow = document.getElementById('reqCertRow');
if (reqCertRow.style.display != 'none')
	golgotha.form.validate({f:f.reqCert, t:'Specific Certification Prerequisite'});

// Check min flights / hours
const eqCertRow = document.getElementById('reqEQRow');
if (eqCertRow.style.display != 'none')
	golgotha.form.validate({f:f.flightCount, min:1, t:'Minimum flight Count'});

// Check network rating code
const ratingRow = document.getElementById('ratingCodeRow');
if (ratingRow.style.display != 'none')
	golgotha.form.validate({f:f.ratingCode, l:2, t:'Online Network rating Code'});

golgotha.form.submit(f);
return true;
};

golgotha.onDOMReady(function() {
	const f = document.forms[0];
	golgotha.form.resize(f.desc);
	golgotha.local.showReqCert(f.preReqs);
	golgotha.local.showNetworkRating(f.network);
	return true;
});
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>
<content:sysdata var="roles" name="security.roles" />
<content:sysdata var="airlines" name="apps" mapValues="true" />
<content:sysdata var="networks" name="online.networks" />
<content:enum var="preReqNames" className="org.deltava.beans.academy.Prerequisite" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="cert.do" linkID="${cert.name}" op="save" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">FLIGHT ACADEMY CERTIFICATION - ${certName}</td>
</tr>
<tr>
 <td class="label">Certification Name</td>
 <td class="data"><el:text name="name" className="pri bld" required="true" idx="*" size="32" max="32" value="${cert.name}" /></td>
</tr>
<tr>
 <td class="label">Code</td>
 <td class="data"><el:text name="code" className="bld" required="true" idx="*" size="4" max="8" value="${cert.code}" /></td>
</tr>
<tr>
 <td class="label">Stage</td>
 <td class="data"><el:text className="bld req" name="stage" idx="*" size="1" max="1" value="${cert.stage}" /></td>
</tr>
<tr>
 <td class="label">Prerequisites</td>
 <td class="data"><el:combo name="preReqs" required="true" idx="*" size="1" value="${cert.reqs.description}" options="${preReqNames}" onChange="void golgotha.local.showReqCert(this)" firstEntry="-" /></td>
</tr>
<tr id="reqCertRow" style="display:none;">
 <td class="label">Certification</td>
 <td class="data"><el:combo name="reqCert" className="req" size="1" value="${cert.reqCert}" options="${allCerts}" firstEntry="-" /></td>
</tr>
<tr id="reqEQRow" style="display:none;">
 <td class="label">Minimum Flights</td>
 <td class="data">At least <el:text className="bld req" name="flightCount" size="3" max="3" value="${cert.flightCount}" /> flights/hours in the primary equipment types for the
 <el:combo name="eqProgram" className="req" size="1" value="${cert.equipmentProgram}" options="${allPrograms}" firstEntry = "[ ANY PROGRAM ]" /> program</td>
</tr>
<tr>
 <td class="label top">Enrollment Roles</td>
 <td class="data"><span class="ita">Select any Security Roles required for this Certification to be available for enrollment by a particular Pilot.</span><br />
<el:check name="enrollRoles" width="115" cols="7" newLine="true" checked="${cert.roles}" options="${roles}" /></td>
</tr>
<tr>
 <td class="label">Airlines</td>
 <td class="data"><el:check name="airlines" width="175" options="${airlines}" checked="${cert.airlines}" /></td>
</tr>
<tr>
 <td class="label">Online Network</td>
 <td class="data"><el:combo name="network" size="1" idx="*" required="true" options="${networks}" firstEntry="None" value="${cert.network}" onChange="void golgotha.local.showNetworkRating(this)" /></td>
</tr>
<tr id="ratingCodeRow" style="display:none;">
 <td class="label">Rating Code</td>
 <td class="data"><el:text name="ratingCode" size="5" max="5" idx="*" className="bld req" value="${cert.networkRatingCode}" /></td>
</tr>
<tr>
 <td class="label top">Required Examinations</td>
 <td class="data"><el:check name="reqExams" width="220" cols="4" className="small" newLine="true" checked="${cert.examNames}" options="${exams}" /></td>
</tr>
<tr>
 <td class="label">Check Rides</td>
 <td class="data">This certification requires <el:int name="rideCount" idx="*" min="0" max="9" size="1" className="sec bld" value="${cert.rideCount}" /> Check Rides</td>
</tr>
<tr>
 <td class="label top">Check Ride Equipment Type(s)</td>
 <td class="data"><el:check name="rideEQ" cols="8" width="110" className="small" newLine="true"  checked="${cert.rideEQ}" options="${allEQ}"  />
<div style="clear:both"></div><span class="small ita">(Leave this blank to allow check rides in any of the pilot's rated aircraft.)</span></td> 
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isActive" idx="*" className="bld" value="true" label="Certification is Active" checked="${cert.active}" /><br />
<el:box name="autoEnroll" idx="*" value="true" label="Auto-Enroll students in Course" checked="${cert.autoEnroll}" /><br />
<el:box name="visible" idx="*" value="true" label="Certificate Completion is publicly visible" checked="${cert.visible}" /></td>
</tr>
<tr>
 <td class="label top">Instructions</td>
 <td class="data"><el:textbox name="desc" idx="*" width="90%" height="5" resize="true">${cert.description}</el:textbox></td>
</tr>
<%@ include file="/jsp/auditLog.jspf" %> 
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr class="title">
 <td><el:button type="submit" label="SAVE CERTIFICATION PROFILE" /></td>
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
