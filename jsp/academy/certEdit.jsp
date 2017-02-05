<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
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
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.showReqCert = function(combo) {
	var opt = combo.options[combo.selectedIndex];
	golgotha.util.display('reqCertRow', (opt.text == 'Specific Certification'));
	golgotha.util.display('reqEQRow', opt.text.startsWith('Flight '));
	return true;	
};

golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.name, l:10, t:'Certification Name'});
golgotha.form.validate({f:f.stage, min:1, t:'Certification Stage'});
golgotha.form.validate({f:f.preReqs, t:'Examination Prerequisites'});

// Check specific cert
var reqCertRow = document.getElementById('reqCertRow');
if (reqCertRow.style.display != 'none')
	golgotha.form.validate({f:f.preReqCert, t:'Specific Certification Prerequisite'});

// Check min flights / hours
var eqCertRow = document.getElementById('reqEQRow');
if (eqCertRow.style.display != 'none')
	golgotha.form.validate({f:f.flightCount, min:1, t:'Minimum flight Count'});

golgotha.form.submit(f);
return true;
};

golgotha.local.onload = function() {
	var f = document.forms[0];
	golgotha.form.resize(f.desc);
	golgotha.local.showReqCert(f.preReqs);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onload="void golgotha.local.onload()">
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>
<content:sysdata var="roles" name="security.roles" />
<content:sysdata var="airlines" name="apps" mapValues="true" />

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
 <td class="data"><el:combo name="preReqs" required="true" idx="*" size="1" value="${cert.reqName}" options="${preReqNames}" onChange="void golgotha.local.showReqCert(this)" firstEntry="-" /></td>
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
 <td class="label top">Required Examinations</td>
 <td class="data"><el:check name="reqExams" width="220" cols="4" className="small" newLine="true" checked="${cert.examNames}" options="${exams}" /></td>
</tr>
<tr>
 <td class="label">Check Rides</td>
 <td class="data">This certification requires <el:int name="rideCount" idx="*" min="0" max="9" size="1" className="sec bld" value="${cert.rideCount}" /> Check Rides</td>
</tr>
<tr>
 <td class="label top">Check Ride Equipment Type(s)</td>
 <td class="data"><el:check name="rideEQ" cols="8" width="95" className="small" newLine="true"  checked="${cert.rideEQ}" options="${allEQ}"  />
<div style="clear:both"></div><span class="small ita">(Leave this blank to allow check rides in any of the pilot's rated aircraft.)</span></td> 
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isActive" idx="*" className="bld" value="true" label="Certification is Active" checked="${cert.active}" /><br />
<el:box name="autoEnroll" idx="*" value="true" label="Auto-Enroll students in Course" checked="${cert.autoEnroll}" /><br />
<el:box name="visible" idx="*" value="true" label="Certificate Completion is not publicly visible" checked="${cert.visible}" /></td>
</tr>
<tr>
 <td class="label top">Instructions</td>
 <td class="data"><el:textbox name="desc" idx="*" width="90%" height="5" resize="true">${cert.description}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr class="title">
 <td><el:button ID="SaveButton" type="submit" label="SAVE CERTIFICATION PROFILE" /></td>
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
