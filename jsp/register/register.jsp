<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Registration</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:sysdata var="badDomains" name="registration.reject_domain" />
<script language="JavaScript" type="text/javascript">
var invalidDomains = ['<fmt:list value="${badDomains}" delim="','" />'];

function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.firstName, 2, 'First (given) Name')) return false;
if (!validateText(form.lastName, 2, 'Last (family) Name')) return false;
if (!validateEMail(form.email, 'E-Mail Address')) return false;
if (!validateCombo(form.homeAirport, 'Home Airport')) return false;
if (!validateCombo(form.location, 'Location')) return false;
if (!validateCombo(form.tz, 'Time Zone')) return false;
if (!validateText(form.df, 7, 'Date Format')) return false;
if (!validateText(form.tf, 5, 'Time Format')) return false;
if (!validateText(form.nf, 5, 'Number Format')) return false;

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

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="locations" name="locations" />
<content:sysdata var="schemes" name="html.schemes" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="register.do" method="POST" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> PILOT APPLICATION</td>
</tr>
<tr>
 <td class="label">First / Last Name</td>
 <td class="data"><el:text name="firstName" className="pri bld req" idx="*" size="14" max="24" value="${param.firstName}" />&nbsp;
<el:text name="lastName" className="pri bld req" idx="*" size="18" max="32" value="${param.lastName}" /></td>
</tr>
<tr>
 <td class="label">Home Airport</td>
 <td class="data"><el:combo name="homeAirport" size="1" idx="*" options="${airports}" className="req" firstEntry="-" value="${param.homeAirport}" onChange="void changeAirport(this)" />
 <el:text name="homeAirportCode" size="3" max="4" onBlur="void setAirport(document.forms[0].homeAirport, this.value)" /></td>
</tr>
<tr>
 <td class="label">Location</td>
 <td class="data"><el:combo name="location" idx="*" size="1" options="${locations}" className="req" firstEntry="-" value="${param.location}" /></td>
</tr>
<tr>
 <td class="label">VATSIM ID#</td>
 <td class="data"><el:text name="VATSIM_ID" idx="*" size="10" max="9" value="${param.VATSIM_ID}" /></td>
</tr>
<tr>
 <td class="label">IVAO ID#</td>
 <td class="data"><el:text name="IVAO_ID" idx="*" size="10" max="9" value="${param.IVAO_ID}" /></td>
</tr>
<tr>
 <td class="label">AOL Instant Messenger</td>
 <td class="data"><el:text name="aimHandle" idx="*" size="14" max="36" value="${param.aimHandle}" /></td>
</tr>
<tr>
 <td class="label">MSN Messenger</td>
 <td class="data"><el:text name="msnHandle" idx="*" size="32" max="128" value="${param.msnHandle}" /></td>
</tr>

<!-- E-Mail Information -->
<tr class="title">
 <td colspan="2">E-MAIL CONTACT INFORMATION</td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:text name="email" className="req" idx="*" size="48" max="64" value="${param.email}" />
<c:if test="${notUnique}"><div class="small error">Another <content:airline /> Pilot or Applicant is 
currently registed with this e-mail address.</div></c:if>
 </td>
</tr>
<tr>
 <td class="label" valign="top">E-Mail Notifications</td>
 <td class="data"><el:check name="notifyOption" idx="*" className="small" width="215" cols="2" separator="<div style=\"clear:both;\" />" options="${notifyOptions}" /></td>
</tr>

<!-- Pilot Preferences -->
<tr class="title">
 <td colspan="2">PILOT PREFERENCES</td>
</tr>
<tr>
 <td class="label">Time Zone</td>
 <td class="data"><el:combo name="tz" idx="*" size="1" options="${timeZones}" className="req" firstEntry="< TIME ZONE >" value="${param.tz}" /></td>
</tr>
<tr>
 <td class="label">Date/Time Format</td>
 <td class="data"><el:text name="df" idx="*" className="req" value="${empty param.df ? 'MM/dd/yyyy' : param.df}" size="12" max="25" />&nbsp;
<el:text name="tf" idx="*" className="req" value="${empty param.tf ? 'HH:mm' : param.tf}" size="6" max="9" /></td>
</tr>
<tr>
 <td class="label">Number Format</td>
 <td class="data"><el:text name="nf" idx="*" className="req" value="${empty param.nf ? '#,##0.0' : param.nf}" size="9" max="15" /></td>
</tr>
<tr>
 <td class="label">Airport Codes</td>
 <td class="data"><el:check name="airportCodeType" idx="*" type="radio" cols="2" options="${acTypes}" value="IATA" /></td>
</tr>
<tr>
 <td class="label">User Interface</td>
 <td class="data"><el:combo name="uiScheme" idx="*" size="1" options="${schemes}" value="${param.uiScheme}" /></td>
</tr>

<!-- Legacy Hours -->
<tr class="title">
 <td colspan="2">LEGACY HOURS</td>
</tr>
<tr>
 <td class="label">Legacy Flight Hours</td>
 <td class="data"><el:text name="legacyHours" idx="*" size="4" max="7" value="${param.legacyHours}" /></td>
</tr>
<tr>
 <td class="label">Verification URL</td>
 <td class="data"><el:text name="legacyURL" idx="*" size="64" max="128" value="${param.legacyURL}" /></td>
</tr>

<!-- Applicant Comments -->
<tr class="title">
 <td colspan="2">ADDITIONAL INFORMATION</td>
</tr>
<tr>
 <td class="label" rowspan="2" valign="top">Comments</td>
 <td class="data"><span class="small">Plase provide any comments or additional information that you feel would assist
in processing your application.</span></td>
</tr>
 <td class="data"><el:textbox name="comments" idx="*" width="80%" height="4"></el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SUBMIT REGISTRATION" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
