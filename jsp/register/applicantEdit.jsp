<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Applicant - ${applicant.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:json />
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<script async>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.firstName, l:3, t:'First Name'});
golgotha.form.validate({f:f.lastName, l:2, t:'Last Name'});
golgotha.form.validate({f:f.email, addr:true, t:'E-Mail Address'});
golgotha.form.validate({f:f.homeAirport, t:'Home Airport'});
golgotha.form.validate({f:f.tz, t:'Time Zone'});
golgotha.form.validate({f:f.df, l:7, t:'Date Format'});
golgotha.form.validate({f:f.tf, l:5, t:'Time Format'});
golgotha.form.validate({f:f.nf, l:5, t:'Number Format'});
golgotha.form.validate({f:f.airportCodeType, min:1, t:'Airport Code type'});
golgotha.form.validate({f:f.eqType, t:'Equipment Program'});
golgotha.form.validate({f:f.rank, t:'Rank'});
golgotha.form.submit(f);
return true;
};

golgotha.local.hire = function() {
	const f = document.forms[0];
	f.doHire.value = 'true';
	return cmdPost(f.action);
};

golgotha.onDOMReady(function() {
	const f = document.forms[0];
	const cfg = golgotha.airportLoad.config; 
	cfg.airline = 'all'; cfg.useSched = false;
	golgotha.airportLoad.setHelpers(f.homeAirport);
	f.homeAirport.loadAirports(cfg);
});
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:tz var="timeZones" />
<content:singleton var="airports" value="${homeAirport}" />
<content:sysdata var="locations" name="locations" />
<content:sysdata var="schemes" name="html.schemes" />
<content:enum var="ranks" className="org.deltava.beans.Rank" />
<content:enum var="notifyOptions" className="org.deltava.beans.Notification" />
<content:enum var="distanceUnits" className="org.deltava.beans.DistanceUnit" />
<content:enum var="weightUnits" className="org.deltava.beans.WeightUnit" />
<content:enum var="acTypes" className="org.deltava.beans.schedule.Airport$Code" />
<content:enum var="fsVersions" className="org.deltava.beans.Simulator" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="applicant.do" link="${applicant}" op="save" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> PILOT APPLICATION</td>
</tr>
<tr>
 <td class="label">First / Last Name</td>
 <td class="data"><el:text name="firstName" className="pri bld" required="true" idx="*" size="14" max="24" value="${applicant.firstName}" />&nbsp;
<el:text name="lastName" className="pri bld" required="true" idx="*" size="18" max="32" value="${applicant.lastName}" /></td>
</tr>
<tr>
 <td class="label">Home Airport</td>
 <td class="data"><el:combo name="homeAirport" size="1" idx="*" options="${airports}" required="true" value="${homeAirport}" onChange="void this.updateAirportCode()" />
 <el:text name="homeAirportCode" ID="homeAirportCode" size="3" max="4" onBlur="void document.forms[0].homeAirport.setAirport(this.value)" /></td>
</tr>
<tr>
 <td class="label">Location</td>
 <td class="data"><el:combo name="location" idx="*" size="1" options="${locations}" required="true" value="${applicant.location}" /></td>
</tr>
<tr>
 <td class="label">VATSIM ID#</td>
 <td class="data"><el:text name="VATSIM_ID" idx="*" value="${fn:networkID(applicant, 'VATSIM')}" size="10" max="9" /></td>
</tr>
<tr>
 <td class="label">IVAO ID#</td>
 <td class="data"><el:text name="IVAO_ID" idx="*" value="${fn:networkID(applicant, 'IVAO')}" size="10" max="9" /></td>
</tr>
<tr>
 <td class="label">PilotEdge ID#</td>
 <td class="data"><el:text name="PilotEdge_ID" idx="*" value="${fn:networkID(applicant, 'PilotEdge')}" size="10" max="9" /></td>
</tr>

<!-- E-Mail Information -->
<tr class="title">
 <td colspan="2">E-MAIL CONTACT INFORMATION</td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:addr name="email" required="true" idx="*" size="48" max="64" value="${applicant.email}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
<c:if test="${eMailValid}">
 <td class="data ter bld caps">E-Mail Address successfully Verified</td>
</c:if>
<c:if test="${!eMailValid}">
 <td class="data warn bld caps">E-Mail Address not yet Verified</td>
</c:if>
</tr>
<tr>
 <td class="label top">E-Mail Notifications</td>
 <td class="data"><el:check name="notifyOption" idx="*" className="small" width="215" cols="3" newLine="true" options="${notifyOptions}" checked="${applicant.notifyOptions}" /></td>
</tr>

<!-- Pilot Preferences -->
<tr class="title">
 <td colspan="2">PILOT PREFERENCES</td>
</tr>
<tr>
 <td class="label">Flight Simulator</td>
 <td class="data"><el:check type="radio" name="fsVersion" idx="*" width="70" options="${fsVersions}" value="${applicant.simVersion}" /></td>
</tr>
<tr>
 <td class="label">Time Zone</td>
 <td class="data"><el:combo name="tz" idx="*" size="1" options="${timeZones}" required="true" value="${applicant.TZ}" /></td>
</tr>
<tr>
 <td class="label">Date/Time Format</td>
 <td class="data"><el:text name="df" idx="*" value="${applicant.dateFormat}" required="true" size="12" max="25" />&nbsp;
<el:text name="tf" idx="*" value="${applicant.timeFormat}" className="req" size="6" max="9" /></td>
</tr>
<tr>
 <td class="label">Number Format</td>
 <td class="data"><el:text name="nf" idx="*" value="${applicant.numberFormat}" required="true" size="9" max="15" /></td>
</tr>
<tr>
 <td class="label">Airport Codes</td>
 <td class="data"><el:check name="airportCodeType" idx="*" type="radio" cols="2" options="${acTypes}" value="${applicant.airportCodeType}" /></td>
</tr>
<tr>
 <td class="label">Distance Units</td>
 <td class="data"><el:check name="distanceUnits" idx="*" type="radio" cols="3" options="${distanceUnits}" value="${applicant.distanceType}" /></td>
</tr>
<tr>
 <td class="label">Weight Units</td>
 <td class="data"><el:check name="weightUnits" idx="*" type="radio" cols="3" options="${weightUnits}" value="${applicant.weightType}" /></td>
</tr>
<tr>
 <td class="label">User Interface</td>
 <td class="data"><el:combo name="uiScheme" idx="*" size="1" options="${schemes}" value="${applicant.UIScheme}" /></td>
</tr>
<c:if test="${!empty applicant.comments}">
<tr>
 <td class="label top">Comments</td>
 <td class="data"><fmt:msg value="${applicant.comments}" /></td>
</tr>
</c:if>

<!-- HR Comments -->
<tr class="title">
 <td colspan="2">HR COMMENTS</td>
</tr>
<tr>
 <td class="label top">HR Comments</td>
 <td class="data"><el:textbox name="HRcomments" idx="*" width="80%" height="3" resize="true">${applicant.HRComments}</el:textbox></td>
</tr>

<!-- Legacy Hours -->
<tr class="title">
 <td colspan="2">LEGACY HOURS</td>
</tr>
<tr>
 <td class="label">Legacy Flight Hours</td>
 <td class="data"><el:text name="legacyHours" idx="*" size="4" max="7" value="${applicant.legacyHours}" /></td>
</tr>
<tr>
 <td class="label">Verification URL</td>
 <td class="data"><el:text name="legacyURL" idx="*" size="64" max="128" value="${applicant.legacyURL}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="legacyOK" idx="*" value="1" label="Legacy Hours Verified" checked="${applicant.legacyVerified}" /></td>
</tr>
<c:if test="${!empty soundexUsers}">
<%@ include file="/jsp/register/appSoundexMatch.jspf" %> 
</c:if>
<c:if test="${access.canApprove}">
<!-- Hire Section -->
<tr class="title">
 <td colspan="2">APPROVE APPLICANT</td>
</tr>
<tr>
 <td class="label">Pilot Questionnaire</td>
<c:if test="${empty questionnaire}">
 <td class="data warn bld caps">Pilot Questionnaire Not Found</td>
</c:if>
<c:if test="${!empty questionnaire}">
<c:if test="${!fn:pending(questionnaire)}">
 <td class="data"><span class="ter bld caps">Completed - <fmt:int value="${questionnaire.score}" /> 
correct out of <fmt:int value="${questionnaire.size}" /> questions</span> 
<el:cmdbutton url="questionnaire" link="${questionnaire}" label="VIEW QUESTIONNAIRE" /></td>
</c:if>
<c:if test="${fn:pending(questionnaire)}">
 <td class="data"><span class="sec bld caps">Pending - <fmt:int value="${questionnaire.size}" /> questions</span></td>
</c:if>
</c:if>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" options="${eqTypes}" required="true" firstEntry="-" value="${applicant.equipmentType}" /></td>
</tr>
<tr>
 <td class="label">Rank</td>
 <td class="data"><el:combo name="rank" idx="*" size="1" options="${ranks}" required="true" firstEntry="-" value="${applicant.rank.name}" /></td>
</tr>
<tr>
 <td class="label top">Equipment Program Sizes</td>
 <td class="data"><c:forEach var="eqType" items="${eqTypes}">
<span class="sec bld">${eqType.name}</span> (Stage ${eqType.stage}) - <b><fmt:int value="${eqType.size}" /> Pilots</b><br />
</c:forEach></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>
<c:if test="${access.canApprove}"><el:button onClick="void golgotha.local.hire()" label="HIRE APPLICANT" />&nbsp;</c:if> 
<el:button type="submit" label="UPDATE APPLICANT" />
<c:if test="${access.canReject}">&nbsp;<el:cmdbutton url="appreject" link="${applicant}" label="REJECT APPLICANT" /></c:if>
<c:if test="${!empty questionnaire}">&nbsp;<el:cmdbutton url="questionnaire" link="${questionnaire}" label="VIEW QUESTIONNAIRE" />
</c:if>
 </td>
</tr>
</el:table>
<el:text name="doHire" type="hidden" value="" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
