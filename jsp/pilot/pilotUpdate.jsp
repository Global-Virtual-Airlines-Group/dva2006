<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Profile Updated</title>
<content:css name="main" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="db" name="airline.db" />
<content:sysdata var="forumName" name="airline.forum" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr"><content:airline /> Pilot Profile Updated</div>
<br />
The Pilot Profile for ${pilot.rank.name}&nbsp;${pilot.name} has been successfully updated.<br />
<br />
<content:hasmsg>
<div class="error bld"><content:sysmsg /></div><br />
</content:hasmsg>
<c:if test="${!empty vatsimValidationMsgs}">
<div class="error bld">The following problems occurred attempting to validate your VATSIM account:<br />
<br />
<c:forEach var="msg" items="${vatsimValidationMsgs}">
${msg}<br />
</c:forEach></div><br /></c:if>
<ul>
<c:if test="${vatsimOK}">
<!-- VATSIM Account Verified -->
<li>${pilot.firstName}'s VATSIM account has been successfully verified.</li>
</c:if>
<c:if test="${ratingsUpdated}">
<!-- Updated Equipment Ratings -->
<li>Equipment Ratings have been updated. ${pilot.firstName} is now rated to fly the <span class="small">
<fmt:list value="${pilot.ratings}" delim="," /></span>.</li>
</c:if>
<c:if test="${!empty newName}">
<!-- Pilot Renamed -->
<li>This Pilot's name has been changed to ${newName}.</li>
</c:if>
<c:if test="${!empty dupeResults}">
<!-- Rename Failed; Not Unique -->
<li><span class="warn">This Pilot could NOT be renamed, since the new name is not unique There are <fmt:int value="${fn:sizeof(dupeResults)}" /> 
matching <content:airline /> Pilots or Applicants with the same name or e-mail address.</span></li>
<c:forEach var="user" items="${dupeResults}">
<c:set var="userLoc" value="${userData[id]}" scope="page" />
<li><el:profile location="${userLoc}">${user.name}</el:profile> ( <a href="mailto:${user.email}">${user.email}</a> )</li>
</c:forEach>
</c:if>
<c:if test="${pwdUpdated}">
<!-- Password Updated -->
<li>The Web Site/ACARS password for ${pilot.name} has been updated.</li>
</c:if>
<c:if test="${statusUpdated}">
<!-- Updated Pilot Status -->
<li>Pilot Status has been updated. ${pilot.firstName}'s status is now <span class="pri bld">${pilot.status.description}</span>.</li>
</c:if>
<c:if test="${rankUpdated && !isPromotion}">
<!-- Updated Pilot Equipment Type and Rank -->
<li>${pilot.firstName} has been transfered to the ${pilot.equipmentType} program as a <b>${pilot.rank.name}</b>.</li>
</c:if>
<c:if test="${isPromotion}">
<!-- Updated Pilot Rank -->
<li>${pilot.firstName} has been promoted, and is now a <b>${pilot.rank.name}</b> in the ${pilot.equipmentType} program.</li>
</c:if>
<c:if test="${pwdUpdate}">
<!-- Updated Password -->
<li>${pilot.name}'s password has been successfully updated.</li>
</c:if>
<c:if test="${spUpdated}">
<!-- Updated Staff Profile -->
<li>The Staff Profile for ${staff.title} ${pilot.name} has been updated.</li>
</c:if>
<c:if test="${spRemoved}">
<!-- Removed Staff Profile -->
<li>The Staff Profile for ${pilot.name} has been removed from the Staff Roster.</li>
</c:if>
<c:if test="${sigRemoved}">
<!-- Updated Signature Image -->
<li>The ${forumName} signature image for ${pilot.name} has been removed.</li>
</c:if>
<c:if test="${sigUpdated}">
<!-- Removed Signature Image -->
<li>The ${forumName} signature image for ${pilot.name} has been updated. It is displayed below:<br />
<el:sig user="${pilot}" caption="${pilot.name}" noCache="true" /></li>
</c:if>
<c:if test="${eMailUpdateDupe}">
<!-- Duplicate E-Mail Address -->
<br />
<li>The e-mail address for ${pilot.name} cannot be changed to ${newEmail}, since another <content:airline /> pilot has registered 
using this e-mail address.</li>
</c:if>
<c:if test="${!empty addrValid}">
<!-- E-Mail Address updated -->
<br />
<li>The e-mail address for ${pilot.name} has been changed to ${addrValid.address}. <span class="warn bld">This change 
will not take effect until the address has been validated.</span> To validate the new e-mail 
address, <el:cmd url="emailupd" className="sec bld">Click Here</el:cmd>.</li>
</c:if>
<c:if test="${isBlocked}">
<!-- User Suspended -->
<br />
<li>The user account for ${pilot.name} has been Suspended.</li>
</c:if>
<c:if test="${accomplishUpdate}">
<!-- Accomplishment Update -->
The following new Pilot Accomplishments have been added to ${pilot.name}'s profile:<br />
<br />
<c:forEach var="acc" items="${accs}">
<li><fmt:accomplish className="bld" accomplish="${acc}" /> on <fmt:date fmt="d" date="${acc.date}" /></li>
</c:forEach>
</c:if>
</ul>
<br />
To view this Pilot Profile, <el:cmd url="profile" link="${pilot}" op="read" className="sec bld">Click Here</el:cmd>.<br />
To return to the Pilot Roster, <el:cmd url="roster" className="sec bld">Click Here</el:cmd>.<br />
<c:if test="${isPromotion}">To return to the Promotion Queue, <el:cmd url="promoqueue" className="sec bld">Click Here</el:cmd>.<br /></c:if>
<c:if test="${spUpdated}">To return to the Staff Roster, <el:cmd url="staff" className="sec bld">Click Here</el:cmd>.<br /></c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
