<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Accomplishment Award</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.id, t:'Accomplishment'});
	golgotha.form.submit(f);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="accomplishrecalc.do" method="post" link="${accomplish}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">PILOT ACCOMPLISHMENT AWARD</td>
</tr>
<c:if test="${!doAward}">
<tr>
 <td colspan="2" class="left">This page can be used to determine which <content:airline /> Pilots are eligible to be awarded
 an Accomplishment. This should be run when an Accomplishment is changed, or a new Accomplishment created to determine which
 Pilots are eligible. Otherwise, eligbility will only be calculated on Flight Report approval.</td>
</tr>
</c:if>
<tr>
 <td class="label">Accomplishment</td>
 <td class="data"><el:combo name="id" idx="*" size="1" className="req" options="${accomplishments}" firstEntry="-" value="${accomplish}" /></td>
</tr>
<c:if test="${doAward}">
<c:choose>
<c:when test="${empty pilots}">
<tr>
 <td colspan="2" class="mid pri bld caps">No new <content:airline /> Pilots are eligible to receive the ${accomplish.name} Accomplishment.</td>
</tr>
</c:when>
<c:otherwise>
<tr>
 <td class="label top">Awarded Pilots</td>
 <td class="data">The following <content:airline /> Pilots have been awarded the Accomplishment:<br />
<br />
<c:forEach var="pilot" items="${pilots}" varStatus="pStatus">
${pilot.rank.name} <el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.name}</el:cmd> (${pilot.pilotCode})<c:if test="${!pStatus.last}"><br /></c:if></c:forEach></td>
</tr>
</c:otherwise>
</c:choose>
<c:if test="${!empty cleared}">
<tr>
 <td class="label">Cleared Pilots</td>
 <td class="data">The following <content:airline /> Pilots have lost the Accomplishment:<br />
<br />
<c:forEach var="pilot" items="${cleared}" varStatus="pStatus">
${pilot.rank} <el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.name}</el:cmd> (${pilot.pilotCode})<c:if test="${!pStatus.last}"><br /></c:if></c:forEach></td>
</tr>
</c:if>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="AWARD ACCOMPLISHMENT TO ELIGIBLE PILOTS" /></td>
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
