<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title>Edit Profile - ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:json />
<content:js name="airportRefresh" />
<content:sysdata var="forumName" name="airline.forum" />
<content:sysdata var="badDomains" name="registration.reject_domain" />
<content:sysdata var="minPwd" name="security.password.min" />
<content:sysdata var="defaultTFormat" name="time.time_format" />
<content:sysdata var="defaultDFormat" name="time.date_format" />
<fmt:aptype var="useICAO" />
<script>
golgotha.local.hasSignature = ${pilot.hasSignature};
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.firstName, l:2, t:'First (given) Name'});
golgotha.form.validate({f:f.lastName, l:2, t:'Last (family) Name'});
golgotha.form.validate({f:f.homeAirport, t:'Home Airport'});
golgotha.form.validate({f:f.df, l:7, t:'Date Format'});
golgotha.form.validate({f:f.tf, l:5, t:'Time Format'});
golgotha.form.validate({f:f.nf, l:5, t:'Number Format'});
golgotha.form.validate({f:f.viewCount, min:20, t:'View Size'});
golgotha.form.validate({f:f.coolerImg, ext:['jpg','png','gif'], t:'${forumName} Cooler Signature Image', empty:true});
golgotha.form.validate({f:f.staffTitle, l:8, t:'Staff Title'});
golgotha.form.validate({f:f.staffArea, t:'Department Name'});
golgotha.form.validate({f:f.staffBody, l:30, t:'Staff Biographical Profile'});
golgotha.form.validate({f:f.staffSort, min:1, t:'Staff Profile Sort Order'});

// Validate password
if ((f.pwd1) && (f.pwd2)) {
	if (f.pwd1.value != f.pwd2.value) {
		f.pwd1.value = '';
		f.pwd2.value = '';
		throw new golgotha.event.ValidationError('The specified passwords must match.', f.pwd1);
	}

	if (f.pwd1.value.length > 0)
		golgotha.form.validate({f:f.pwd1, l:${minPwd}, t:'New Password'});
}
<content:filter roles="!HR">
// Validate e-mail domain
<fmt:jsarray var="golgotha.form.invalidDomains" items="${badDomains}" />
if (f.email)
	golgotha.form.validate({f:f.email, addr:true, t:'Email Address'});
</content:filter>
// Set disabled checkboxes
f.useDefaultSig.checked = (f.useDefaultSig.checked && !(f.useDefaultSig.disabled));
golgotha.form.submit(f);
return true;
};

golgotha.local.disableSigBoxes = function() {
	const f = document.forms[0];
	f.coolerImg.disabled = (f.useDefaultSig.checked);
	if (golgotha.local.hasSignature) f.useDefaultSig.disabled = (!f.removeCoolerImg.checked);
	return true;
};

golgotha.local.setDefaultFormats = function() {
	const f = document.forms[0];
	f.df.value = '${defaultDFormat}';
	f.tf.value = '${defaultTFormat}';
	return true;
};

golgotha.local.checkPwd = function(t, noCascade) {
	const f = document.forms[0];
	const of = (t.name == 'pwd2' ? f.pwd1 : f.pwd2);
	let isOK = (t.value.length > ${minPwd});
	isOK &= ((t.value == of.value) || (of.value.length == 0));
	golgotha.util.removeClass(t, isOK ? 'err' : 'ok');
	golgotha.util.addClass(t, isOK ? 'ok' : 'err');
	if (!noCascade) golgotha.local.checkPwd(of, true);
	return true;
};

golgotha.onDOMReady(function() {
	const f = document.forms[0];
	const cfg = golgotha.airportLoad.config;
	f.useDefaultSig.disabled = golgotha.local.hasSignature;
	cfg.doICAO = ${useICAO}; cfg.airline = 'all';
	golgotha.airportLoad.setHelpers(f.homeAirport);
	f.homeAirport.loadAirports(cfg);
});
</script>
</head>
<content:copyright visible="false" />
<body onload="void golgotha.local.disableSigBoxes()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:attr attr="isHR" value="true" roles="HR" />
<c:set var="cspan" value="${(!empty exams) || (!empty statusUpdates) ? 6 : 1}" scope="request" />
<content:sysdata var="db" name="airline.db" />
<content:sysdata var="currencyEnabled" name="testing.currency.enabled" />
<content:sysdata var="currencyInterval" name="testing.currency.validity" />
<content:tz var="timeZones" />
<content:singleton var="airports" value="${homeAirport}" />
<content:enum var="ranks" className="org.deltava.beans.Rank" />
<content:enum var="notifyOptions" className="org.deltava.beans.Notification" />
<content:enum var="distanceUnits" className="org.deltava.beans.DistanceUnit" />
<content:enum var="weightUnits" className="org.deltava.beans.WeightUnit" />
<content:enum var="mapTypes" className="org.deltava.beans.MapType" />
<content:enum var="acarsRest" className="org.deltava.beans.acars.Restriction" />
<content:enum var="acTypes" className="org.deltava.beans.schedule.Airport$Code" />
<content:sysdata var="locations" name="locations" />
<content:sysdata var="roles" name="security.roles" />
<content:sysdata var="schemes" name="html.schemes" />
<content:sysdata var="sigX" name="cooler.sig_max.x" />
<content:sysdata var="sigY" name="cooler.sig_max.y" />
<content:sysdata var="sigSize" name="cooler.sig_max.size" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="profile.do" link="${pilot}" op="save" method="post" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Pilot Title Bar -->
<tr class="title caps">
 <td colspan="${cspan + 1}">${pilot.rank.name} ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></td>
</tr>

<!-- Pilot Data -->
<c:if test="${access.canChangeStatus || access.canChangeRoles}">
<tr>
 <td class="label">First / Last Name</td>
 <td colspan="${cspan}" class="data"><el:text name="firstName" className="pri bld" required="true" idx="*" size="14" max="24" value="${pilot.firstName}" />&nbsp;
<el:text name="lastName" className="pri bld" required="true" idx="*" size="18" max="32" value="${pilot.lastName}" /></td>
</tr>
<tr>
 <td class="label">Pilot Status</td>
 <td colspan="${cspan}" class="data"><span class="pri bld">${pilot.status.description}</span></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td colspan="${cspan}" class="data"><el:box name="noCooler" idx="*" value="true" checked="${pilot.noCooler}" label="Disable ${forumName} posting access" /><br />
<el:box name="noVoice" idx="*" value="true" checked="${pilot.noVoice}" label="Disable Private Voice access" /><br />
<el:box name="noExams" idx="*" value="true" checked="${pilot.noExams}" label="Disable Testing Center access" /><br />
<el:box name="noTimeCompress" idx="*" value="true" checked="${pilot.noTimeCompression}" label="Disable ACARS Time Compression" /><br />
<el:box name="permAccount" value="true" checked="${pilot.isPermanent}" label="This is a Permanent account and will never be marked Inactive" /></td>
</tr>
<tr>
 <td class="label">ACARS Capabilities</td>
 <td colspan="${cspan}" class="data"><el:combo name="ACARSrestrict" size="1" idx="*" options="${acarsRest}" value="${pilot.ACARSRestriction.name}" /></td>
</tr>
</c:if>
<c:if test="${pilot.ID == pageContext.request.userPrincipal.ID}">
<tr>
 <td class="label">Password</td>
 <td colspan="${cspan}" class="data"><el:text type="password" autoComplete="false" name="pwd1" idx="*" size="16" max="32" onKeyup="void golgotha.local.checkPwd(this)" value="" />, retype:
<el:text type="password" autoComplete="false" name="pwd2" idx="*" size="16" max="32" onKeyup="void golgotha.local.checkPwd(this)" value="" /></td>
</tr>
</c:if>
<c:if test="${access.canChangeRoles}">
<tr>
 <td class="label top">Security Roles</td>
 <td colspan="${cspan}" class="data"><el:check name="securityRoles" width="115" cols="7" newLine="true" checked="${pilot.roles}" options="${roles}" /></td>	
</tr>
<tr>
 <td class="label">Subversion User ID</td>
 <td colspan="${cspan}" class="data"><el:text name="uid" idx="*" size="10" max="24" value="${pilot.LDAPName}" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Rank / Equipment</td>
<c:if test="${access.canChangeStatus}">
 <td colspan="${cspan}" class="data"><el:combo name="rank" size="1" idx="*" options="${ranks}" value="${pilot.rank.name}" />
 <el:combo name="eqType" size="1" idx="*" options="${eqTypes}" value="${pilot.equipmentType}" /></td>
</c:if>
<c:if test="${!access.canChangeStatus}">
 <td colspan="${cspan}" class="data">${pilot.rank.name}, <span class="bld">${pilot.equipmentType}</span></td>
</c:if> 
</tr>
<tr>
 <td class="label top">Additional Ratings</td>
<c:if test="${access.canPromote}">
 <td colspan="${cspan}" class="data"><el:check name="ratings" idx="*" cols="9" width="95" newLine="true" className="small" checked="${pilot.ratings}" options="${allEQ}" /></td>
</c:if>
<c:if test="${!access.canPromote}">
 <td colspan="${cspan}" class="data small"><fmt:list value="${pilot.ratings}" delim=", " /></td>
</c:if>
</tr>
<tr>
 <td class="label">Home Airport</td>
 <td colspan="${cspan}" class="data"><el:combo name="homeAirport" size="1" idx="*" required="true" options="${airports}" value="${homeAirport}" onChange="void this.updateAirportCode()" />
 <el:text name="homeAirportCode" size="3" max="4" onBlur="void document.forms[0].homeAirport.setAirport(this.value)" /></td>
</tr>
<tr>
 <td class="label">VATSIM ID</td>
 <td colspan="${cspan}" class="data"><el:text name="VATSIM_ID" idx="*" value="${fn:networkID(pilot, 'VATSIM')}" size="10" max="9" /></td>
</tr>
<tr>
 <td class="label">IVAO ID</td>
 <td colspan="${cspan}" class="data"><el:text name="IVAO_ID" idx="*" value="${fn:networkID(pilot, 'IVAO')}" size="10" max="9" /></td>
</tr>
<tr>
 <td class="label">PilotEdge ID</td>
 <td colspan="${cspan}" class="data"><el:text name="PilotEdge_ID" idx="*" value="${fn:networkID(pilot, 'PilotEdge')}" size="10" max="9" /></td>
</tr>
<c:if test="${empty pilotLocation}">
<tr>
 <td class="label">Location</td>
 <td colspan="${cspan}" class="data"><el:combo name="location" idx="*" size="1" options="${locations}" value="${pilot.location}" /></td>
</tr>
</c:if>
<c:if test="${!empty pilotLocation}">
<tr>
 <td class="label">Location</td>
 <td colspan="${cspan}" class="data">${pilot.location} <span class="small ita">(Set via Pilot Location Board)</span></td>
</tr>
</c:if>
<c:if test="${!empty pilot.pilotCode}">
<tr>
 <td class="label">Personal Motto</td>
 <td colspan="${cspan}" class="data"><el:text name="motto" idx="*" value="${pilot.motto}" size="40" max="60" /></td>
</tr>
</c:if>

<!-- E-Mail Information -->
<tr class="title">
 <td colspan="${cspan + 1}">E-MAIL / INSTANT MESSAGING INFORMATION</td>
</tr>
<c:choose>
<c:when test="${isHR && (user.ID != pilot.ID)}">
<tr>
 <td class="label">E-Mail Address</td>
 <td colspan="${cspan}" class="data"><el:addr name="email" required="true" value="${pilot.email}" idx="*" size="48" max="64" /></td>
</tr>
</c:when>
<c:otherwise>
<tr>
 <td class="label">E-Mail Address</td>
 <td colspan="${cspan}" class="data">${pilot.email} &nbsp;<el:cmd url="emailupd" className="pri small">Change my e-mail Address</el:cmd></td>
</tr>
</c:otherwise>
</c:choose>
<tr>
 <td class="label top">E-Mail Notifications</td>
 <td colspan="${cspan}" class="data"><el:check name="notifyOption" idx="*" width="215" cols="2" newLine="true" options="${notifyOptions}" checked="${pilot.notifyOptions}" /></td>
</tr>
<content:enum var="imTypes" className="org.deltava.beans.IMAddress" />
<c:forEach var="imType" items="${imTypes}">
<c:if test="${imType.isVisible}">
<tr>
 <td class="label">${imType} Address</td>
 <td colspan="${cspan}" class="data"><el:text name="${imType}Handle" idx="*" size="10" max="32" value="${pilot.IMHandle[imType]}" /></td>
</tr>
</c:if>
</c:forEach>
<tr>
 <td class="label top">Privacy Options</td>
 <td colspan="${cspan}" class="data"><el:check type="radio" name="privacyOption" idx="*" cols="1" separator="<br />" options="${privacyOptions}" value="${pilot.emailAccess}" /></td>
</tr>

<!-- ${forumName} Preferences -->
<tr class="title caps">
 <td colspan="${cspan + 1}">${forumName}</td>
</tr>
<tr>
 <td class="label top">Signature Image</td>
 <td colspan="${cspan}" class="data"><c:if test="${pilot.hasSignature}">
<img alt="${forumName} Signature" src="/sig/${db}/${pilot.hexID}" /><br />
<el:box name="removeCoolerImg" value="true" label="Remove ${forumName} Signature Image" onChange="void golgotha.local.disableSigBoxes()" /><br /></c:if>
<content:filter roles="HR,Signature"><c:if test="${!sigAuthorized}">
<el:box name="isAuthSig" value="true" label="Authorized ${forumName} Signature Image" /><br /></c:if></content:filter>
<el:box name="useDefaultSig" value="true" label="Use default Signature Image" checked="${pilot.hasDefaultSignature}" onChange="void golgotha.local.disableSigBoxes()" /></td>
</tr>
<tr>
 <td class="label top">Update Signature Image</td>
 <td colspan="${cspan}" class="data"><el:file name="coolerImg" className="small" idx="*" size="80" max="144" /><br />
<span class="small sec">The maximum size for a signature image is <fmt:int value="${sigX}" />x<fmt:int value="${sigY}" /> 
pixels, and the maximum file size is <fmt:int value="${sigSize}" /> bytes.</span></td>
</tr>
<tr>
 <td class="label top">Display Options</td>
 <td colspan="${cspan}" class="data"><el:box name="showSigs" value="true" checked="${pilot.showSignatures}" label="Show ${forumName} Signature Images" /><br />
 <el:box name="showImageThreads" value="true" checked="${pilot.showSSThreads}" label="Show ${forumName} screen shot Message Threads" /><br />
 <el:box name="scrollToNewPosts" value="true" checked="${pilot.showNewPosts}" label="Scroll to new ${forumName} Message Thread posts" /></td>
</tr>

<!-- Pilot Preferences -->
<tr class="title">
 <td colspan="${cspan + 1}">PILOT PREFERENCES</td>
</tr>
<tr>
 <td class="label">Time Zone</td>
 <td colspan="${cspan}" class="data"><el:combo name="tz" size="1" options="${timeZones}" required="true" value="${pilot.TZ}" /></td>
</tr>
<tr>
 <td class="label">Date/Time Format</td>
 <td colspan="${cspan}" class="data"><el:text name="df" value="${pilot.dateFormat}"  required="true" size="15" max="25" />&nbsp;<el:text name="tf" value="${pilot.timeFormat}" required="true" size="9" max="9" />
&nbsp;<el:button onClick="void golgotha.local.setDefaultFormats()" label="RESET" /></td>
</tr>
<tr>
 <td class="label">Number Format</td>
 <td colspan="${cspan}" class="data"><el:text name="nf" value="${pilot.numberFormat}" required="true" size="12" max="15" /></td>
</tr>
<tr>
 <td class="label">View Size</td>
 <td colspan="${cspan}" class="data"><el:text name="viewCount" value="${pilot.viewCount}" required="true" size="3" max="3" /></td>
</tr>
<tr>
 <td class="label">Airport Codes</td>
 <td colspan="${cspan}" class="data"><el:check name="airportCodeType" type="radio" cols="2" options="${acTypes}" value="${pilot.airportCodeType}" /></td>
</tr>
<tr>
 <td class="label">Distance Units</td>
 <td colspan="${cspan}" class="data"><el:check name="distanceUnits" type="radio" cols="3" options="${distanceUnits}" value="${pilot.distanceType}" /></td>
</tr>
<tr>
 <td class="label">Weight Units</td>
 <td colspan="${cspan}" class="data"><el:check name="weightUnits" type="radio" cols="3" options="${weightUnits}" value="${pilot.weightType}" /></td>
</tr>
<tr>
 <td class="label">Route Map Type</td>
 <td colspan="${cspan}" class="data"><el:combo name="mapType" size="1" options="${mapTypes}" value="${pilot.mapType.name}" /></td>
</tr>
<tr>
 <td class="label">User Interface</td>
 <td colspan="${cspan}" class="data"><el:combo name="uiScheme" size="1" options="${schemes}" value="${pilot.UIScheme}" />
<el:box name="showNavBar" value="true" label="Show Horizontal Navigation Menu" checked="${pilot.showNavBar}" />
 <span class="small nophone">(Navigation Bar is not displayed at screen widths below 1024 pixels.)</span></td>
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
 <td colspan="${cspan}" class="data"><fmt:int value="${pilot.loginCount}" /><c:if test="${pilot.loginCount > 0}">, last
 on <fmt:date date="${pilot.lastLogin}" /></c:if></td>
</tr>
<c:if test="${!empty pilot.lastLogoff}">
<tr>
 <td class="label">Last Visited on</td>
 <td colspan="${cspan}" class="data"><fmt:date date="${pilot.lastLogoff}" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Flights</td>
 <td colspan="${cspan}" class="data"><fmt:int value="${pilot.legs}" /> legs, <fmt:dec value="${pilot.hours}" /> hours</td>
</tr>
<tr>
 <td class="label">Online Flights</td>
 <td colspan="${cspan}" class="data pri"><fmt:int value="${pilot.onlineLegs}" /> legs, <fmt:dec value="${pilot.onlineHours}" /> hours</td>
</tr>
<c:if test="${access.canChangeStatus || (pilot.legacyHours > 0)}">
<tr>
 <td class="label">Legacy Hours</td>
<c:choose>
<c:when test="${access.canChangeStatus}">
 <td colspan="${cspan}" class="data"><el:text name="legacyHours" idx="*" size="4" max="6" value="${pilot.legacyHours}" /></td>
</c:when>
<c:otherwise>
 <td colspan="${cspan}" class="data"><fmt:dec value="${pilot.legacyHours}" /> hours</td>
</c:otherwise>
</c:choose>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;<c:if test="${access.canEdit}"><el:button type="submit" label="SAVE PROFILE" /></c:if></td>
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
