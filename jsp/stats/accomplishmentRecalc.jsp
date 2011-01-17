<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Accomplishment Award</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.id, 'Accomplishment')) return false;

setSubmit();
disableButton('CalcButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="accomplishrecalc.do" method="post" link="${accomplish}" op="save" validate="return validate(this)">
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
${pilot.rank} <el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.name}</el:cmd> (${pilot.pilotCode})<c:if test="${!pStatus.last}"><br /></c:if></c:forEach></td>
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
 <td><el:button ID="CalcButton" type="submit" label="AWARD ACCOMPLISHMENT TO ELIGIBLE PILOTS" /></td>
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
