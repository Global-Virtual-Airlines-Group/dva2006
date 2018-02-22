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
<script type="text/javascript">
golgotha.local.validate = function(f)
{
<c:if test="${access.canApprove}">
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.eqType, t:'Equipment Program'});
golgotha.form.validate({f:f.rank, t:'Rank'});
golgotha.form.submit(f);</c:if>
return ${access.canApprove};
}
<c:if test="${access.canApprove}">
golgotha.local.checkVATSIMData = function(id, name)
{
golgotha.util.disable('ValidateButton');
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'vatsim_info.ws?id=' + id + '&name=' + escape(name));
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	if ((xmlreq.status == 404) || (xmlreq.status == 500)) {
		alert('No records found!');
		golgotha.util.disable('ValidateButton', false);
		return false;
	}

	// Parse the JSON
	var s = document.getElementById('validationInfo');
	var js = JSON.parse(xmlreq.responseText);
	s.innerHTML = 'Name : ' + js.name + 'Active : ' + js.active + 'E-Mail Domain : ' + js.domain;
	golgotha.util.display('ValidateButton', false);
	return true;
};

xmlreq.send(null);
return true;
}</c:if>
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:enum var="ranks" className="org.deltava.beans.Rank" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="apphire.do" method="post" link="${applicant}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> PILOT APPLICANT</td>
</tr>
<tr>
 <td class="label">First / Last Name</td>
 <td class="data pri bld">${applicant.name}</td>
</tr>
<tr>
 <td class="label">Applicant Status</td>
 <td class="data sec bld caps">${applicant.statusName}</td>
</tr>
<c:if test="${!empty nameMatches}">
<tr>
 <td class="label top" rowspan="2">Duplicate Users</td>
 <td class="data bld caps"><span class="error caps"><fmt:int value="${fn:sizeof(nameMatches)}" /> DUPLICATE USER NAMES/EMAIL DETECTED - PLEASE VALIDATE</span></td>
</tr>
<tr>
 <td class="data">
<c:forEach var="dupe" items="${nameMatches}">
${dupe.rank.name} <el:cmd url="profile" link="${dupe}" className="bld">${dupe.name}</el:cmd><c:if test="${!empty dupe.pilotCode}"> ${dupe.pilotCode}</c:if><br />
</c:forEach></td>
</tr>
</c:if>
<c:if test="${(!empty questionnaire) && (!fn:pending(questionnaire))}">
<tr>
 <td class="label">Questionnaire Score</td>
 <td class="data"><fmt:int value="${questionnaire.score}" /> / <fmt:int value="${questionnaire.size}" /> 
(<fmt:dec fmt="##0.0" value="${questionnaire.score * 100 / questionnaire.size}" />%)</td>
</tr>
</c:if>
<c:if test="${fn:pending(questionnaire)}">
<tr>
 <td class="label">Questionnaire Status</td>
<c:if test="${fn:submitted(questionnaire)}">
 <td class="data pri bld">APPLICATION QUESTIONNAIRE SUBMITTED</td>
</c:if>
<c:if test="${!fn:submitted(questionnaire)}">
 <td class="data warn bld">APPLICATION QUESTIONNAIRE PENDING</td>
</c:if>
</tr>
</c:if>
<c:if test="${applicant.pilotID > 0}">
<tr>
 <td class="label">Hired as</td>
 <td class="data">${applicant.rank.name}, ${applicant.equipmentType}</td>
</tr>
</c:if>
<c:if test="${!empty homeAirport}">
<tr>
 <td class="label">Home Airport</td>
 <td class="data">${homeAirport.name} (<fmt:airport airport="${homeAirport}" />)</td>
</tr>
</c:if>
<tr>
 <td class="label">Location</td>
 <td class="data">${applicant.location}</td>
</tr>
<c:set var="VATSIM_ID" value="${fn:networkID(applicant, 'VATSIM')}" scope="page" />
<c:if test="${!empty VATSIM_ID}">
<tr>
 <td class="label">VATSIM ID#</td>
 <td class="data">${VATSIM_ID}<c:if test="${access.canApprove}"> <el:button ID="ValidateButton" onClick="void golgotha.local.checkVATSIMData(${VATSIM_ID}, '${applicant.name}')" label="VALIDATE" /><span id="validationInfo" class="sec ita bld"></span></c:if></td>
</tr>
</c:if>
<c:set var="IVAO_ID" value="${fn:networkID(applicant, 'IVAO')}" scope="page" />
<c:if test="${!empty IVAO_ID}">
<tr>
 <td class="label">IVAO ID#</td>
 <td class="data">${IVAO_ID}</td>
</tr>
</c:if>
<content:enum var="imAddr" className="org.deltava.beans.IMAddress" item="AIM" />
<c:if test="${!empty applicant.IMHandle[imAddr]}">
<tr>
 <td class="label">AOL Instant Messenger</td>
 <td class="data">${applicant.IMHandle[imAddr]}</td>
</tr>
</c:if>
<content:enum var="imAddr" className="org.deltava.beans.IMAddress" item="MSN" />
<c:if test="${!empty applicant.IMHandle[imAddr]}">
<tr>
 <td class="label">MSN Messenger</td>
 <td class="data">${applicant.IMHandle[imAddr]}</td>
</tr>
</c:if>

<!-- E-Mail Information -->
<tr class="title">
 <td colspan="2">E-MAIL CONTACT INFORMATION</td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><a href="mailto:${applicant.email}">${applicant.email}</a></td>
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
 <td class="data">${applicant.notifyOptions}</td>
</tr>

<!-- Pilot Preferences -->
<tr class="title">
 <td colspan="2">PILOT PREFERENCES</td>
</tr>
<tr>
 <td class="label">Flight Simulator</td>
 <td class="data bld">${applicant.simVersion.name}</td>
</tr>
<tr>
 <td class="label">Time Zone</td>
 <td class="data">${applicant.TZ}</td>
</tr>
<tr>
 <td class="label">Date/Time Format</td>
 <td class="data">${applicant.dateFormat}&nbsp;${applicant.timeFormat}</td>
</tr>
<tr>
 <td class="label">Number Format</td>
 <td class="data">${applicant.numberFormat}</td>
</tr>
<tr>
 <td class="label">Airport Codes</td>
 <td class="data bld">${applicant.airportCodeType}</td>
</tr>
<tr>
 <td class="label">Distance Units</td>
 <td class="data bld">${applicant.distanceType.unitName}s</td>
</tr>
<tr>
 <td class="label">Weight Units</td>
 <td class="data bld">${applicant.weightType.unitName}s</td>
</tr>
<tr>
 <td class="label">User Interface</td>
 <td class="data">${applicant.UIScheme}</td>
</tr>

<!-- Legacy Hours -->
<tr class="title">
 <td colspan="2">LEGACY HOURS</td>
</tr>
<tr>
 <td class="label">Legacy Flight Hours</td>
 <td class="data"><fmt:dec value="${applicant.legacyHours}" /></td>
</tr>
<c:if test="${!empty applicant.legacyURL}">
<tr>
 <td class="label">Verification URL</td>
 <td class="data"><el:link target="_new" url="${applicant.legacyURL}">${applicant.legacyURL}</el:link></td>
</tr>
</c:if>
<c:if test="${applicant.legacyVerified}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data ter bld caps">LEGACY FLIGHT HOURS VERIFIED</td>
</tr>
</c:if>

<!-- Pilot Statistics -->
<tr class="title">
 <td colspan="2">APPLICANT INFORMATION</td>
</tr>
<tr>
 <td class="label">Registered on</td>
 <td class="data"><fmt:date date="${applicant.createdOn}" /></td>
</tr>
<tr>
 <td class="label">Registered from</td>
 <td class="data"><el:cmd url="loginaddrs" linkID="${applicant.registerAddress}" op="net">${applicant.registerAddress}</el:cmd>
 (${applicant.registerHostName})</td>
</tr>
<c:if test="${!empty addrInfo}">
<tr>
 <td class="label">IP Address Info</td>
 <td class="data">${addrInfo} <el:flag countryCode="${addrInfo.country.code}" caption="${addrInfo.country.name}" /> ${addrInfo.location}</td>
</tr>
</c:if>
<c:if test="${!empty applicant.comments}">
<tr>
 <td class="label top">Comments</td>
 <td class="data"><fmt:msg value="${applicant.comments}" /></td>
</tr>
</c:if>
<!-- HR stuff -->
<tr class="title">
 <td colspan="2">HUMAN RESOURCES DATA</td>
</tr>
<tr>
 <td class="label">Google Search</td>
 <td class="data"><a rel="external" target="applicantSearch" href="https://www.google.com/search?q=${fn:escape(applicant.name)}">Click Here</a> to 
do a Google search on &quot;${applicant.name}&quot;.</td>
</tr>
<c:if test="${!empty applicant.HRComments}">
<tr>
 <td class="label top">HR Comments</td>
 <td class="data"><fmt:msg value="${applicant.HRComments}" /></td>
</tr>
</c:if>
<c:if test="${!empty soundexUsers}"><%@ include file="/jsp/register/appSoundexMatch.jspf" %></c:if>
<c:if test="${!empty netmaskUsers}"><%@ include file="/jsp/register/appNetmaskMatch.jspf" %></c:if>
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
<c:if test="${!empty fn:keys(applicant.typeChoices)}">
<tr>
 <td class="label top">Program Preference</td>
 <td class="data"><c:forEach var="eqStage" items="${fn:keys(applicant.typeChoices)}">
<c:set var="eqStagePref" value="${applicant.typeChoices[eqStage]}" scope="page" />
Stage <fmt:int value="${eqStage}" />: ${eqStagePref}<br /></c:forEach></td>
</tr>
</c:if>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" options="${eqTypes}" firstEntry="-" value="${applicant.equipmentType}" /></td>
</tr>
<tr>
 <td class="label">Rank</td>
 <td class="data"><el:combo name="rank" idx="*" size="1" options="${ranks}" firstEntry="-" value="${applicant.rank.name}" /></td>
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
 <td>&nbsp;
<c:if test="${access.canApprove}">
 <el:button ID="HireButton" type="submit" label="HIRE APPLICANT" />
</c:if>
<c:if test="${access.canEdit}">
 <el:cmdbutton ID="EditButton" url="applicant" op="edit" link="${applicant}" label="EDIT APPLICANT" />
</c:if>
<c:if test="${access.canReject}">
 <el:cmdbutton ID="RejectButton" url="appreject" link="${applicant}" label="REJECT" />
</c:if>
<c:if test="${!empty questionnaire}">
 <el:cmdbutton ID="QuestionnaireButton" url="questionnaire" link="${questionnaire}" label="QUESTIONNAIRE" />
</c:if>
<c:if test="${access.canNotify}">
 <el:cmdbutton ID="ResendButton" url="welcome" link="${applicant}" label="RESEND WELCOME MESSAGE" />
</c:if>
<c:if test="${access.canDelete}">
 <el:cmdbutton ID="DeleteButton" url="appdelete" link="${applicant}" label="DELETE" />
</c:if>
 </td>
</tr>
</el:table>
</el:form>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
