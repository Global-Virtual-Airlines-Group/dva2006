<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Edit Profile - ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:sysdata var="badDomains" name="registration.reject_domain" />
<script language="JavaScript" type="text/javascript">
var invalidDomains = ['<fmt:list value="${badDomains}" delim="','" />'];
var hasSignature = ${pilot.hasSignature};

function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.firstName, 2, 'First (given) Name')) return false;
if (!validateText(form.lastName, 2, 'Last (family) Name')) return false;
if (!validateEMail(form.email, 'E-Mail Address')) return false;
if (!validateText(form.df, 7, 'Date Format')) return false;
if (!validateText(form.tf, 5, 'Time Format')) return false;
if (!validateText(form.nf, 5, 'Number Format')) return false;
if (!validateFile(form.coolerImg, 'jpg,png,gif', 'Water Cooler Signature Image')) return false;
if (!validateText(form.staffTitle, 8, 'Staff Title')) return false;
if (!validateText(form.staffBody, 30, 'Staff Biographical Profile')) return false;
if (!validateNumber(form.staffSort, 1, 'Staff Profile Sort Order')) return false;

// Validate password
if ((form.pwd1) && (form.pwd2)) {
	if (form.pwd1.value != form.pwd2.value) {
		alert('The specified passwords must match.');
		form.pwd1.value = '';
		form.pwd2.value = '';
		form.pwd1.focus();
		return false;
	}
}

// Validate e-mail domain
var eMail = form.email.value;
var usrDomain = eMail.substring(eMail.indexOf('@') + 1, eMail.length);
for (var x = 0; x < invalidDomains.length; x++) {
	if (usrDomain == invalidDomains[x]) {
		alert('Your e-mail address (' + eMail + ') contains a forbidden domain - ' + invalidDomains[x]);
		form.email.focus();
		return false;
	}
}

// Set disabled checkboxes
form.useDefaultSig.checked = (form.useDefaultSig.checked && !(form.useDefaultSig.disabled));
setSubmit();
disableButton('SaveButton');
return true;
}

function disableSigBoxes()
{
var f = document.forms[0];
f.coolerImg.disabled = (f.useDefaultSig.checked);
if (hasSignature)
	f.useDefaultSig.disabled = (!f.removeCoolerImg.checked);

return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="changeAirport(document.forms[0].homeAirport); disableSigBoxes()">
<content:page>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>
<c:set var="cspan" value="${(!empty exams) || (!empty statusUpdates) ? 6 : 1}" scope="request" />
<content:sysdata var="db" name="airline.db" />
<content:sysdata var="ranks" name="ranks" />
<content:sysdata var="locations" name="locations" />
<content:sysdata var="allEQ" name="eqtypes" sort="true" />
<content:sysdata var="airports" name="airports" mapValues="true" sort="true" />
<content:sysdata var="roles" name="security.roles" />
<content:sysdata var="schemes" name="html.schemes" />
<content:sysdata var="sigX" name="cooler.sig_max.x" />
<content:sysdata var="sigY" name="cooler.sig_max.y" />
<content:sysdata var="sigSize" name="cooler.sig_max.size" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="profile.do" linkID="0x${pilot.ID}" op="save" method="post" allowUpload="true" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<!-- Pilot Title Bar -->
<tr class="title caps">
 <td colspan="${cspan + 1}">${pilot.rank} ${pilot.name} (${pilot.pilotCode})</td>
</tr>

<!-- Pilot Data -->
<c:if test="${access.canChangeRoles}">
<tr>
 <td class="label">First / Last Name</td>
 <td colspan="${cspan}" class="data"><el:text name="firstName" className="pri bld req" idx="*" size="14" max="24" value="${pilot.firstName}" />&nbsp;
<el:text name="lastName" className="pri bld req" idx="*" size="18" max="32" value="${pilot.lastName}" /></td>
</tr>
</c:if>
<tr>
 <td class="label" valign="top">Pilot Status</td>
<c:if test="${access.canChangeStatus}">
 <td colspan="${cspan}" class="data"><el:combo name="status" size="1" idx="*" options="${statuses}" value="${status}" /><br />
<el:box name="noVoice" idx="*" value="true" checked="${pilot.noVoice}" label="Disable Private Voice access" /><br />
<el:box name="noExams" idx="*" value="true" checked="${pilot.noExams}" label="Disable Testing Center access" /></td>
</tr>
<tr>
 <td class="label">ACARS Capabilities</td>
 <td colspan="${cspan}" class="data"><el:combo name="ACARSrestrict" size="1" idx="*" options="${acarsRest}" value="${pilot.ACARSRestrictionName}" /></td>
</c:if>  
<c:if test="${!access.canChangeStatus}">
 <td colspan="${cspan}" class="data sec bld">${status}</td>
</c:if>
</tr>
<c:if test="${pilot.ID == pageContext.request.userPrincipal.ID}">
<tr>
 <td class="label">Password</td>
 <td colspan="${cspan}" class="data"><el:text type="password" name="pwd1" idx="*" size="16" max="32" value="" />, retype:
<el:text type="password" name="pwd2" idx="*" size="16" max="32" value="" /></td>
</tr>
</c:if>
<c:if test="${access.canChangeRoles}">
<tr>
 <td class="label" valign="top">Security Roles</td>
 <td colspan="${cspan}" class="data"><el:check name="securityRoles" width="85" cols="6" separator="<div style=\"clear:both;\" />" checked="${pilot.roles}" options="${roles}" /></td>	
</tr>
<tr>
 <td class="label">Subversion User ID</td>
 <td colspan="${cspan}" class="data"><el:text name="uid" idx="*" size="10" max="24" value="${pilot.LDAPName}" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Rank / Equipment</td>
<c:if test="${access.canChangeStatus}">
 <td colspan="${cspan}" class="data"><el:combo name="rank" size="1" idx="*" options="${ranks}" value="${pilot.rank}" />
 <el:combo name="eqType" size="1" idx="*" options="${eqTypes}" value="${pilot.equipmentType}" /></td>
</c:if>
<c:if test="${!access.canChangeStatus}">
 <td colspan="${cspan}" class="data">${pilot.rank}, ${pilot.equipmentType}</td>
</c:if> 
</tr>
<tr>
 <td class="label" valign="top">Additional Ratings</td>
<c:if test="${access.canPromote}">
 <td colspan="${cspan}" class="data"><el:check name="ratings" idx="*" cols="9" width="85" separator="<div style=\"clear:both;\" />" className="small" checked="${pilot.ratings}" options="${allEQ}" /></td>
</c:if>
<c:if test="${!access.canPromote}">
 <td colspan="${cspan}" class="data small"><fmt:list value="${pilot.ratings}" delim=", " /></td>
</c:if>
</tr>
<tr>
 <td class="label">Home Airport</td>
 <td colspan="${cspan}" class="data"><el:combo name="homeAirport" size="1" idx="*" options="${airports}" value="${pilot.homeAirport}" onChange="void changeAirport(this)" />
 <el:text name="airportCode" size="3" max="4" onBlur="void setAirport(document.forms[0].airport, this.value)" /></td>
</tr>
<tr>
 <td class="label">VATSIM ID#</td>
 <td colspan="${cspan}" class="data"><el:text name="VATSIM_ID" idx="*" value="${pilot.networkIDs['VATSIM']}" size="10" max="9" /></td>
</tr>
<tr>
 <td class="label">IVAO ID#</td>
 <td colspan="${cspan}" class="data"><el:text name="IVAO_ID" idx="*" value="${pilot.networkIDs['IVAO']}" size="10" max="9" /></td>
</tr>
<tr>
 <td class="label">Location</td>
 <td colspan="${cspan}" class="data"><el:combo name="location" idx="*" size="1" options="${locations}" value="${pilot.location}" /></td>
</tr>
<c:if test="${!empty pilot.pilotCode}">
<tr>
 <td class="label">Personal Motto</td>
 <td colspan="${cspan}" class="data"><el:text name="motto" idx="*" value="${pilot.motto}" size="24" max="36" /></td>
</tr>
</c:if>


<!-- E-Mail Information -->
<tr class="title">
 <td colspan="${cspan + 1}">E-MAIL / INSTANT MESSAGING INFORMATION</td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
 <td colspan="${cspan}" class="data"><el:text name="email" value="${pilot.email}" idx="*" size="48" max="64" className="req" /></td>
</tr>
<tr>
 <td class="label" valign="top">E-Mail Notifications</td>
 <td colspan="${cspan}" class="data"><el:check name="notifyOption" idx="*" width="215" cols="2" separator="<DIV STYLE=\"clear:both;\" />" options="${notifyOptions}" checked="${pilot.notifyOptions}" /></td>
</tr>
<tr>
 <td class="label">AOL Instant Messenger</td>
 <td colspan="${cspan}" class="data"><el:text name="aimHandle" idx="*" size="10" max="32" value="${pilot.IMHandle['AOL']}" /></td>
</tr>
<tr>
 <td class="label">MSN Messenger</td>
 <td colspan="${cspan}" class="data"><el:text name="msnHandle" idx="*" size="32" max="128" value="${pilot.IMHandle['MSN']}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Privacy Options</td>
 <td colspan="${cspan}" class="data"><el:check type="radio" name="privacyOption" idx="*" cols="1" separator="<br />" options="${privacyOptions}" value="${pilot.emailAccess}" /></td>
</tr>

<!-- Water Cooler Preferences -->
<tr class="title">
 <td colspan="${cspan + 1}">WATER COOLER</td>
</tr>
<tr>
 <td class="label" valign="top">Signature Image</td>
 <td colspan="${cspan}" class="data"><c:if test="${pilot.hasSignature}">
<img alt="Water Cooler Signature" src="/sig/${db}/0x<fmt:hex value="${pilot.ID}" />" /><br />
<el:box name="removeCoolerImg" value="true" label="Remove Water Cooler Signature Image" onChange="void disableSigBoxes()" /><br /></c:if>
<el:box name="useDefaultSig" value="true" label="Use default Signature Image" onChange="void disableSigBoxes()" /></td>
</tr>
<tr>
 <td class="label" valign="top">Update Signature Image</td>
 <td colspan="${cspan}" class="data"><el:file name="coolerImg" className="small" idx="*" size="80" max="144" /><br />
<span class="small sec">The maximum size for a signature image is <fmt:int value="${sigX}" />x<fmt:int value="${sigY}" /> 
pixels, and the maximum file size is <fmt:int value="${sigSize}" /> bytes.</span></td>
</tr>
<tr>
 <td class="label" valign="top">Display Options</td>
 <td colspan="${cspan}" class="data"><el:box name="showSigs" value="true" checked="${pilot.showSignatures}" label="Show Water Cooler Signature Images" /><br />
 <el:box name="showImageThreads" value="true" checked="${pilot.showSSThreads}" label="Show Water Cooler screen shot Message Threads" />
 </td>
</tr>

<!-- Pilot Preferences -->
<tr class="title">
 <td colspan="${cspan + 1}">PILOT PREFERENCES</td>
</tr>
<tr>
 <td class="label">Time Zone</td>
 <td colspan="${cspan}" class="data"><el:combo name="tz" size="1" options="${timeZones}" className="req" value="${pilot.TZ}" /></td>
</tr>
<tr>
 <td class="label">Date/Time Format</td>
 <td colspan="${cspan}" class="data"><el:text name="df" value="${pilot.dateFormat}" className="req" size="15" max="25" />
 <el:text name="tf" value="${pilot.timeFormat}" className="req" size="9" max="9" /></td>
</tr>
<tr>
 <td class="label">Number Format</td>
 <td colspan="${cspan}" class="data"><el:text name="nf" value="${pilot.numberFormat}" className="req" size="12" max="15" /></td>
</tr>
<tr>
 <td class="label">Airport Codes</td>
 <td colspan="${cspan}" class="data"><el:check name="airportCodeType" type="RADIO" cols="2" options="${acTypes}" value="${pilot.airportCodeTypeName}" /></td>
</tr>
<tr>
 <td class="label">Route Map Type</td>
 <td colspan="${cspan}" class="data"><el:combo name="mapType" size="1" options="${mapTypes}" value="${pilot.mapTypeName}" /></td>
</tr>
<tr>
 <td class="label">User Interface</td>
 <td colspan="${cspan}" class="data"><el:combo name="uiScheme" size="1" options="${schemes}" value="${pilot.UIScheme}" /></td>
</tr>
<%@ include file="/jsp/pilot/staffEdit.jspf" %>
<%@ include file="/jsp/pilot/eMailEdit.jspf" %>
<%@ include file="/jsp/pilot/pilotExams.jspf" %>
<%@ include file="/jsp/pilot/pilotStatusUpdate.jspf" %>
<!-- Pilot Statistics -->
<tr class="title">
 <td colspan="${cspan + 1}">PILOT STATISTICS</td>
</tr>
<tr>
 <td class="label">Joined on</td>
 <td colspan="${cspan}" class="data"><fmt:date date="${pilot.createdOn}" fmt="d" /></td>
</tr>
<tr>
 <td class="label">Site Logins</td>
 <td colspan="${cspan}" class="data"><fmt:int value="${pilot.loginCount}" />, last on <fmt:date date="${pilot.lastLogin}" /></td>
</tr>
<c:if test="${!empty pilot.lastLogoff}">
<tr>
 <td class="label">Last Visited on</td>
 <td colspan="${cspan}" class="data"><fmt:date date="${pilot.lastLogoff}" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Flights</td>
 <td colspan="${cspan}" class="data"><fmt:int value="${pilot.legs}" /> legs, 
<fmt:dec value="${pilot.hours}" /> hours</td>
</tr>
<tr>
 <td class="label">Online Flights</td>
 <td colspan="${cspan}" class="data pri"><fmt:int value="${pilot.onlineLegs}" /> legs, 
<fmt:dec value="${pilot.onlineHours}" /> hours</td>
</tr>
<c:if test="${access.canChangeStatus || (pilot.legacyHours > 0)}">
<tr>
 <td class="label">Legacy Hours</td>
<c:choose>
<c:when test="${access.canChangeStatus}">
 <td colspan="${cspan}" class="data"><el:text name="legacyHours" idx="*" size="2" max="6" value="${pilot.legacyHours}" /></td>
</c:when>
<c:otherwise>
 <td colspan="${cspan}" class="data"><fmt:dec value="${pilot.legacyHours}" /> hours</td>
</c:otherwise>
</c:choose>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
<c:if test="${access.canEdit}">
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SAVE PROFILE" /></td>
</c:if>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script langage="JavaScript" type="text/javascript">
var f = document.forms[0];
f.useDefaultSig.disabled = hasSignature;
</script>
</body>
</html>
