<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Registration</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:json />
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="badDomains" name="registration.reject_domain" />
<c:set var="cspan" value="${!empty manuals ? 3 : 1}" scope="page" />
<script type="text/javascript">
<fmt:jsarray var="golgotha.form.invalidDomains" items="${badDomains}" />
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.firstName, l:2, t:'First (given) Name'});
golgotha.form.validate({f:f.lastName, l:2, t:'Last (family) Name'});
golgotha.form.validate({f:f.email, addr:true, t:'E-Mail Address'});
golgotha.form.validate({f:f.homeAirport, t:'Home Airport'});
golgotha.form.validate({f:f.location, t:'Location'});
golgotha.form.validate({f:f.tz, t:'Time Zone'});
golgotha.form.validate({f:f.df, l:7, t:'Date Format'});
golgotha.form.validate({f:f.tf, l:5, t:'Time Format'});
golgotha.form.validate({f:f.nf, l:5, t:'Number Format'});
golgotha.form.validate({f:f.distanceUnits, min:1, t:'Distance Unit'});
golgotha.form.validate({f:f.weightUnits, min:1, t:'Weight Unit'});
golgotha.form.submit(f);
return true;
};

golgotha.local.checkUnique = function()
{
var f = document.forms[0];
var fN = f.firstName.value;
var lN = f.lastName.value;
var eMail = f.email.value;
if ((fN.length < 2) || (lN.length < 2) || (golgotha.local.uniqueCheck)) return false;
	
// Create the AJAX request
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'dupename.ws?fName=' + fN + '&lName=' + lN + "&eMail=" + escape(eMail));
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var dupes = (parseInt(xmlreq.responseText) > 0);
	var rows = golgotha.util.getElementsByClass('dupeFound');
	for (var x = 0; x < rows.length; x++) {
		golgotha.util.display(rows[x], dupes);
		rows[x].focus();
	}

	// Disable form elements
	for (var x = 0; x < f.elements.length; x++)
		f.elements[x].disabled = dupes;

	return true;
};

xmlreq.send(null);
return true;
};

golgotha.local.resetUniqueCheck = function(isPermanent)
{
golgotha.local.uniqueCheck = isPermanent;
var rows = golgotha.util.getElementsByClass('dupeFound');
for (var x = 0; x < rows.length; x++)
	golgotha.util.display(rows[x], false);

var f = document.forms[0];
for (var x = 0; x < f.elements.length; x++)
	f.elements[x].disabled = false;

return true;
};

golgotha.local.sendDupeInfo = function() {
	var f = document.forms[0];
	self.location = '/register.do?op=dupe&firstName=' + f.firstName.value + '&lastName=' + f.lastName.value + '&email=' + f.email.value;
	return true;
};

golgotha.onDOMReady(function() {
	var f = document.forms[0];
	var cfg = golgotha.airportLoad.config;
	cfg.airline = 'all'; cfg.useSched = false;
	golgotha.airportLoad.setHelpers(f.homeAirport);
	f.homeAirport.loadAirports(cfg);
});
</script>
</head>
<content:copyright visible="false" />
<body onload="void golgotha.local.resetUniqueCheck(false)">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:tz var="timeZones" />
<content:empty var="emptyList" />
<content:sysdata var="locations" name="locations" />
<content:sysdata var="schemes" name="html.schemes" />
<content:sysdata var="airlineDomain" name="airline.domain" />
<content:enum var="notifyOptions" className="org.deltava.beans.Notification" />
<content:enum var="distanceUnits" className="org.deltava.beans.DistanceUnit" />
<content:enum var="weightUnits" className="org.deltava.beans.WeightUnit" />
<content:enum var="acTypes" className="org.deltava.beans.schedule.Airport$Code" />
<content:enum var="fsVersions" className="org.deltava.beans.Simulator" exclude="FS98,FS2000,FS2002,XP9" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="register.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<c:if test="${!empty manuals}">
<tr class="title caps">
 <td colspan="4">THANK YOU FOR YOUR INTEREST IN <content:airline />!</td>
</tr>
<tr>
 <td colspan="4" class="pri bld">You'll find that <content:airline /> is one of the largest yet friendliest and most sophisticated virtual airlines
 on the Internet. Many aspects of our operations are significantly different from other virtual airlines, specifically in promotions, ratings and 
 what flights are credited for hours. Please take a few moments to download and review some of our manuals to help determine if <content:airline />
 is the right virtual airline for you.<br />
<br />
This is also a good time to review <content:airline />'s <el:cmd url="privacy" className="pri bld">Privacy Policy</el:cmd>.</td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps mid">
 <td class="nophone">&nbsp;</td>
 <td style="width:23%">TITLE</td>
 <td class="nophone" style="width:12%">SIZE</td>
 <td class="left">DESCRIPTION</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="doc" items="${manuals}">
<view:row entry="${doc}">
 <td class="mid nophone"><el:link url="/library/${doc.fileName}"><el:img src="library/adobe.png" className="noborder" caption="Download PDF manual" x="32" y="32" /></el:link></td>
 <td class="pri bld mid"><el:link url="/library/${doc.fileName}">${doc.name}</el:link></td>
 <td class="sec bld mid nophone"><fmt:int value="${doc.size}" /> bytes</td>
 <td class="small"><fmt:text value="${doc.description}" /></td>
</view:row>
</c:forEach>
</c:if>
<tr class="title caps">
 <td colspan="${cspan + 1}"><content:airline /> PILOT APPLICATION</td>
</tr>
<tr>
 <td class="label">First / Last Name</td>
 <td class="data" colspan="${cspan}"><el:text name="firstName" className="pri bld" required="true" idx="*" size="14" max="24" value="${param.firstName}" onBlur="void golgotha.local.checkUnique()" />
&nbsp;<el:text name="lastName" className="pri bld" required="true" idx="*" size="18" max="32" value="${param.lastName}" onBlur="void golgotha.local.checkUnique()" /></td>
</tr>
<tr class="dupeFound" style="display:none;">
 <td colspan="${cspan + 1}" class="mid"><span class="error bld">Another person with the same name has already registered at <content:airline />. If you have
 already registered with us, you can simply reactivate your old user account. This is a much faster and simpler process than re-registering.</span><br />
<br />
<a href="javascript:void golgotha.local.sendDupeInfo()" class="pri bld">I'm already a <content:airline /> Pilot. Reactivate my Account.</a><br />
<br />
<a href="javascript:void golgotha.local.resetUniqueCheck(true)" class="sec">I've never registered with <content:airline /> before.</a></td>
</tr>
<tr>
 <td class="label">Home Airport</td>
 <td class="data" colspan="${cspan}"><el:combo name="homeAirport" size="1" idx="*" options="${emptyList}" className="req" firstEntry="-" value="${param.homeAirport}" onChange="void this.updateAirportCode()" />
 <el:text name="homeAirportCode" ID="homeAirportCode" size="3" max="4" onBlur="void document.forms[0].homeAirport.setAirport(this.value)" /></td>
</tr>
<tr>
 <td class="label">Location</td>
 <td class="data" colspan="${cspan}"><el:combo name="location" idx="*" size="1" options="${locations}" className="req" firstEntry="-" value="${param.location}" /></td>
</tr>
<tr>
 <td class="label">VATSIM ID#</td>
 <td class="data" colspan="${cspan}"><el:text name="VATSIM_ID" idx="*" size="10" max="9" value="${param.VATSIM_ID}" /></td>
</tr>
<tr>
 <td class="label">IVAO ID#</td>
 <td class="data" colspan="${cspan}"><el:text name="IVAO_ID" idx="*" size="10" max="9" value="${param.IVAO_ID}" /></td>
</tr>
<tr>
 <td class="label">AOL Instant Messenger</td>
 <td class="data" colspan="${cspan}"><el:text name="aimHandle" idx="*" size="14" max="36" value="${param.aimHandle}" /></td>
</tr>
<tr>
 <td class="label">MSN Messenger</td>
 <td class="data" colspan="${cspan}"><el:text name="msnHandle" idx="*" size="32" max="128" value="${param.msnHandle}" /></td>
</tr>

<!-- E-Mail Information -->
<tr class="title">
 <td colspan="${cspan + 1}">E-MAIL CONTACT INFORMATION</td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data" colspan="${cspan}"><el:addr name="email" required="true" idx="*" size="48" max="64" value="${param.email}" onBlur="void golgotha.local.checkUnique()" /> 
<span class="small ita">Please ensure that your spam blockers are set to accept email from ${airlineDomain}.</span></td>
</tr>
<tr>
 <td class="label top">E-Mail Notifications</td>
 <td class="data" colspan="${cspan}"><el:check name="notifyOption" idx="*" className="small" width="215" cols="2" newLine="true" options="${notifyOptions}" /></td>
</tr>

<!-- Pilot Preferences -->
<tr class="title">
 <td colspan="${cspan + 1}">PILOT PREFERENCES</td>
</tr>
<tr>
 <td class="label">Flight Simulator</td>
 <td class="data" colspan="${cspan}"><el:check type="radio" name="fsVersion" idx="*" width="125" options="${fsVersions}" value="FS2004" /></td>
</tr>
<tr>
 <td class="label">Time Zone</td>
 <td class="data" colspan="${cspan}"><el:combo name="tz" idx="*" size="1" options="${timeZones}" className="req" firstEntry="[ TIME ZONE ]" value="${(!empty myTZ) ? myTZ : param.tz}" />
<c:if test="${!empty ipInfo}"> <span class="nophone small">( <el:flag countryCode="${ipInfo.country.code}" caption="${ipInfo.location}" /> ${ipInfo.location} )</span></c:if></td>
</tr>
<tr>
 <td class="label">Date/Time Format</td>
 <td class="data" colspan="${cspan}"><el:text name="df" idx="*" className="req" value="${empty param.df ? 'MM/dd/yyyy' : param.df}" size="12" max="25" />&nbsp;
<el:text name="tf" idx="*" className="req" value="${empty param.tf ? 'HH:mm' : param.tf}" size="6" max="9" /></td>
</tr>
<tr>
 <td class="label">Number Format</td>
 <td class="data" colspan="${cspan}"><el:text name="nf" idx="*" className="req" value="${empty param.nf ? '#,##0.0' : param.nf}" size="9" max="15" /></td>
</tr>
<tr>
 <td class="label">Airport Codes</td>
 <td class="data" colspan="${cspan}"><el:check name="airportCodeType" idx="*" type="radio" cols="2" options="${acTypes}" value="IATA" /></td>
</tr>
<tr>
 <td class="label">Distance Units</td>
 <td class="data" colspan="${cspan}"><el:check name="distanceUnits" idx="*" type="radio" cols="3" options="${distanceUnits}" value="MI" /></td>
</tr>
<tr>
 <td class="label">Wegith Units</td>
 <td class="data" colspan="${cspan}"><el:check name="weightUnits" idx="*" type="radio" cols="3" options="${weightUnits}" value="LB" /></td>
</tr>
<tr>
 <td class="label">User Interface</td>
 <td class="data" colspan="${cspan}"><el:combo name="uiScheme" idx="*" size="1" options="${schemes}" value="${param.uiScheme}" /></td>
</tr>

<!-- Legacy Hours -->
<tr class="title">
 <td colspan="${cspan + 1}">LEGACY HOURS</td>
</tr>
<tr>
 <td class="label">Legacy Flight Hours</td>
 <td class="data" colspan="${cspan}"><el:text name="legacyHours" idx="*" size="4" max="7" value="${param.legacyHours}" /> <span class="ita">For reference only.</span></td>
</tr>
<tr>
 <td class="label">Verification URL</td>
 <td class="data" colspan="${cspan}"><el:text name="legacyURL" idx="*" size="56" max="128" value="${param.legacyURL}" /></td>
</tr>

<!-- Equipment Type preferences -->
<tr class="title">
 <td colspan="${cspan + 1}">EQUIPMENT PROGRAM PREFERENCE</td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data" colspan="${cspan}"><span class="small">To most effectively place you in the equipment type program of your choice, please
 select your preferred equipment type program in each stage below. You may be placed in a different program than the one you
 selected depending on demand and pilot numbers.</span></td>
</tr>
<c:forEach var="stage" items="${fn:keys(eqTypes)}">
<c:set var="stageEQ" value="${eqTypes[stage]}" scope="page" />
<c:set var="sXparam" value="s${stage}prefs" scope="page" />
<c:if test="${fn:sizeof(stageEQ) > 1}">
<tr>
 <td class="label">Stage <fmt:int value="${stage}" /></td>
 <td class="data" colspan="${cspan}"><el:check name="${sXparam}" type="radio" width="100" cols="6" options="${stageEQ}" checked="${param[sXparam]}" /></td>
</tr>
</c:if>
</c:forEach>

<!-- Applicant Comments -->
<tr class="title">
 <td colspan="${cspan + 1}">ADDITIONAL INFORMATION</td>
</tr>
<tr>
 <td class="label top" rowspan="2">Comments</td>
 <td class="data" colspan="${cspan}"><span class="small">Please provide any comments or additional information that you feel would assist in processing 
 your application.</span></td>
</tr>
<tr>
 <td class="data" colspan="${cspan}"><el:textbox name="comments" idx="*" width="80%" height="4" resize="true"></el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SUBMIT REGISTRATION" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
