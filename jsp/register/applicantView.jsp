<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Applicant - ${applicant.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
<c:if test="${access.canApprove}">
if (!checkSubmit()) return false;
if (!validateCombo(form.eqType, 'Equipment Program')) return false;
if (!validateCombo(form.rank, 'Rank')) return false;

setSubmit();
disableButton('EditButton');
disableButton('HireButton');
disableButton('RejectButton');
disableButton('DeleteButton');
disableButton('QuestionnaireButton');
disableButton('ResendButton');
</c:if>
return ${access.canApprove};
}
<c:if test="${access.canApprove}">
function checkVATSIMData(id, name)
{
disableButton('ValidateButton');
var xmlreq = getXMLHttpRequest();
xmlreq.open('GET', 'vatsim_info.ws?id=' + id + '&name=' + name);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	if ((xmlreq.status == 404) || (xmlreq.status == 500)) {
		alert('No records found!');
		enableElement('ValidateButton', true);
		return false;
	}

	// Parse the XML
	var xmlDoc = xmlreq.responseXML;
	if (!xmlDoc) return false;

	// Get the info
	var info = xmlDoc.documentElement;
	var infoStr = info.getAttribute('network') + ' Information for ' + info.getAttribute('id')
		+ ':\n\nPilot Name : ' + info.getAttribute('name') + '\nUser Status : '
		+ info.getAttribute('status') + '\nE-Mail Domain : ' + info.getAttribute('domain');
	alert(infoStr);
	enableElement('ValidateButton', true);
	return true;
} // function

xmlreq.send(null);
return true;
}</c:if>
</script>
</head>
<content:copyright visible="false" />
<body onload="void initLinks()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="ranks" name="ranks" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="apphire.do" method="post" link="${applicant}" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> PILOT APPLICANT</td>
</tr>
<tr>
 <td class="label">First / Last Name</td>
 <td class="data pri bld">${applicant.name}</td>
</tr>
<tr>
 <td class="label">Applicant Status</td>
 <td class="data sec bld caps">${statusName}</td>
</tr>
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
 <td class="data">${applicant.rank}, ${applicant.equipmentType}</td>
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
<c:set var="VATSIM_ID" value="${applicant.networkIDs['VATSIM']}" scope="request" />
<c:if test="${!empty VATSIM_ID}">
<tr>
 <td class="label">VATSIM ID#</td>
 <td class="data">${VATSIM_ID}
<c:if test="${access.canApprove}"> <el:button ID="ValidateButton" className="BUTTON" onClick="void checkVATSIMData(${VATSIM_ID}, '${applicant.name}')" label="VALIDATE" /></c:if>
 </td>
</tr>
</c:if>
<c:if test="${!empty applicant.networkIDs['IVAO']}">
<tr>
 <td class="label">IVAO ID#</td>
 <td class="data">${applicant.networkIDs['IVAO']}</td>
</tr>
</c:if>
<c:if test="${!empty applicant.IMHandle['AOL']}">
<tr>
 <td class="label">AOL Instant Messenger</td>
 <td class="data">${applicant.IMHandle['AOL']}</td>
</tr>
</c:if>
<c:if test="${!empty applicant.IMHandle['MSN']}">
<tr>
 <td class="label">MSN Messenger</td>
 <td class="data">${applicant.IMHandle['MSN']}</td>
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
 <td class="label" valign="top">E-Mail Notifications</td>
 <td class="data">${applicant.notifyOptions}</td>
</tr>

<!-- Pilot Preferences -->
<tr class="title">
 <td colspan="2">PILOT PREFERENCES</td>
</tr>
<tr>
 <td class="label">Flight Simulator</td>
 <td class="data bld">${applicant.simVersionCode}</td>
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
 <td class="data bld">${applicant.airportCodeType == 0 ? 'IATA' : 'ICAO'}</td>
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
<c:if test="${!empty applicant.comments}">
<tr>
 <td class="label" valign="top">Comments</td>
 <td class="data"><fmt:msg value="${applicant.comments}" /></td>
</tr>
</c:if>
<!-- HR stuff -->
<tr class="title">
 <td colspan="2">HUMAN RESOURCES DATA</td>
</tr>
<tr>
 <td class="label">Google Search</td>
 <td class="data"><a rel="external" href="http://www.google.com/search?q=${fn:escape(applicant.name)}">Click Here</a> to 
do a Google search on &quot;${applicant.name}&quot;.</td>
</tr>
<c:if test="${!empty applicant.HRComments}">
<tr>
 <td class="label" valign="top">HR Comments</td>
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
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" options="${eqTypes}" firstEntry="-" value="${applicant.equipmentType}" /></td>
</tr>
<tr>
 <td class="label">Rank</td>
 <td class="data"><el:combo name="rank" idx="*" size="1" options="${ranks}" firstEntry="-" value="${applicant.rank}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Equipment Program Sizes</td>
 <td class="data"><c:forEach var="eqType" items="${eqTypes}">
<c:set var="eqSize" value="${eqTypeStats[eqType.name]}" scope="request" />
<span class="sec bld">${eqType.name}</span> (Stage ${eqType.stage}) - <b><fmt:int value="${eqSize}" /> Pilots</b><br />
</c:forEach></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td>&nbsp;
<c:if test="${access.canEdit}">
 <el:cmdbutton ID="EditButton" url="applicant" op="edit" link="${applicant}" label="EDIT APPLICANT" />
</c:if>
<c:if test="${access.canApprove}">
 <el:button ID="HireButton" type="submit" className="BUTTON" label="HIRE" />
</c:if>
<c:if test="${access.canReject}">
 <el:cmdbutton ID="RejectButton" url="appreject" link="${applicant}" label="REJECT" />
</c:if>
<c:if test="${!empty questionnaire}">
 <el:cmdbutton ID="QuestionnaireButton" url="questionnaire" link="${questionnaire}" label="VIEW QUESTIONNAIRE" />
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
