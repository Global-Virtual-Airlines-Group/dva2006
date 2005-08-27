<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
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
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
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
<c:if test="${!fn:pending(questionnaire)}">
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
<tr>
 <td class="label">VATSIM ID#</td>
 <td class="data">${applicant.networkIDs['VATSIM']}</td>
</tr>
<tr>
 <td class="label">IVAO ID#</td>
 <td class="data">${applicant.networkIDs['IVAO']}</td>
</tr>
<tr>
 <td class="label">AOL Instant Messenger</td>
 <td class="data">${applicant.IMHandle}</td>
</tr>

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
 <td colspan="2">PILOT STATISTICS</td>
</tr>
<tr>
 <td class="label">Registered on</td>
 <td class="data"><fmt:date date="${applicant.createdOn}" /></td>
</tr>
<tr>
 <td class="label">Registered from</td>
 <td class="data">${applicant.registerHostName}</td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td>&nbsp;
<c:if test="${access.canEdit}">
<el:cmdbutton url="applicant" op="edit" linkID="0x${applicant.ID}" label="EDIT APPLICANT" />
</c:if>
<c:if test="${access.canApprove}">
<el:cmdbutton url="apphire" linkID="0x${applicant.ID}" label="HIRE" />
</c:if>
<c:if test="${access.canReject}">
<el:cmdbutton url="appreject" linkID="0x${applicant.ID}" label="REJECT" />
</c:if>
<c:if test="${!empty questionnaire}">
<el:cmdbutton url="questionnaire" linkID="0x${questionnaire.ID}" label="VIEW QUESTIONNAIRE" />
</c:if>
<c:if test="${access.canApprove || access.canReject}">
<el:cmdbutton url="welcome" linkID="0x${applicant.ID}" label="RESEND WELCOME MESSAGE" />
</c:if>
 </td>
</tr>
</el:table>
<content:copyright />
</div>
</body>
</html>
