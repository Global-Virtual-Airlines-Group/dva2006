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
<title><content:airline /> Pilot Profile Updated</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="db" name="airline.db" />
<content:sysdata var="acarsEnabled" name="acars.enabled" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr"><content:airline /> Pilot Profile Updated</div>
<br />
The Pilot Profile for ${pilot.rank} ${pilot.name} has been successfully updated.<br />
<br />
<ul>
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
<c:set var="userLoc" value="${userData[id]}" scope="request" />
<li><el:profile location="${userLoc}">${user.name}</el:profile> ( <a href="mailto:${user.email}">${user.email}</a> )</li>
</c:forEach>
</c:if>
<c:if test="${pwdUpdated}">
<!-- Password Updated -->
<li>The Web Site/ACARS password for ${pilot.name} has been updated.</li>
</c:if>
<c:if test="${statusUpdated}">
<!-- Updated Pilot Status -->
<li>Pilot Status has been updated. ${pilot.firstName}'s status is now <span class="pri bld">${pilot.statusName}</span>.</li>
</c:if>
<c:if test="${rankUpdated && !isPromotion}">
<!-- Updated Pilot Equipment Type and Rank -->
<li>${pilot.firstName} has been transfered to the ${pilot.equipmentType} program as a <b>${pilot.rank}</b>.</li>
</c:if>
<c:if test="${isPromotion}">
<!-- Updated Pilot Rank -->
<li>${pilot.firstName} has been promoted, and is now a <b>${pilot.rank}</b> in the ${pilot.equipmentType} program.</li>
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
<li>The Water Cooler signature image for ${pilot.name} has been removed.</li>
</c:if>
<c:if test="${sigUpdated}">
<!-- Removed Signature Image -->
<li>The Water Cooler signature image for ${pilot.name} has been updated. It is displayed below:<br />
<img alt="${pilot.name}" src="/sig/${db}/0x<fmt:hex value="${pilot.ID}" />" /></li>
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
The e-mail address for ${pilot.name} has been changed to ${addrValid.address}. <span class="warn bld">This change 
will not take effect until the address has been validated.</span> To validate the new e-mail 
address, <el:cmd url="emailupd" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${isBlocked}">
<!-- User Suspended -->
<br />
The user account for ${pilot.name} has been suspended.<c:if test="${acarsEnabled}"> Please note that 
if this user is currently logged into the ACARS server, you will need terminate the connection using 
an ACARS client.</c:if><br />
</c:if>
</ul>
<br />
To view this Pilot Profile, <el:cmd url="profile" link="${pilot}" op="read" className="sec bld">Click Here</el:cmd>.<br />
To return to the Pilot Roster, <el:cmd url="roster" className="sec bld">Click Here</el:cmd>.<br />
<c:if test="${spUpdated}">To return to the Staff Roster, <el:cmd url="staff" className="sec bld">Click Here</el:cmd>.<br /></c:if>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
