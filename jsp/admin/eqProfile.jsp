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
<c:if test="${!empty eqType}">
<title><content:airline /> ${eqType.name} Program</title>
</c:if>
<c:if test="${empty eqType}">
<title>New <content:airline /> Equipment Program</title>
</c:if>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.eqType, 'Equipment Type')) return false;
if (!validateCombo(form.cp, 'Chief Pilot')) return false;
if (!validateNumber(form.stage, 1, 'Equipment Stage')) return false;
if (!validateNumber(form.captLegs, 0, 'Flight Legs for promotion')) return false;
if (!validateCheckbox(form.ranks, 2, 'Ranks')) return false;
if (!validateCheckbox(form.pRatings, 1, 'Primary Rating')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>
<content:sysdata var="ranks" name="ranks" />
<content:sysdata var="allEQ" name="eqtypes" />

<!-- Main Body Frame -->
<div ID="main">
<el:form method="POST" action="eqsave.do" linkID="${eqType.name}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<!-- Equipment Profile Title Bar -->
<tr class="title caps">
<c:if test="${!empty eqType}">
 <td colspan="2">${eqType.name} PROGRAM</td>
</c:if>
<c:if test="${empty eqType}">
 <td colspan="2">NEW EQUIPMENT PROGRAM</td>
</c:if>
</tr>

<!-- Equipment Profile Data -->
<tr>
 <td class="label">Program Name</td>
 <td class="data"><el:combo name="eqType" size="1" idx="*" firstEntry="" options="${allEQ}" value="${eqType.name}" /></td>
</tr>
<tr>
 <td class="label">Chief Pilot</td>
 <td class="data"><el:combo name="cp" size="1" idx="*" firstEntry="" options="${chiefPilots}" value="${eqType.CPID}" /></td>
</tr>
<tr>
 <td class="label">Stage</td>
 <td class="data"><el:text name="stage" size="1" max="1" idx="*" value="${empty eqType ? 1 : eqType.stage}" /></td>
</tr>
<tr>
 <td class="label">Available Ranks</td>
 <td class="data"><el:check name="ranks" cols="6" width="120" className="pri small" separator="<div style=\"clear:both;\" />" checked="${eqType.ranks}" options="${ranks}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Primary Ratings</td>
 <td class="data"><el:check name="pRatings" cols="9" width="85" className="small" separator="<div style=\"clear:both;\" />" checked="${eqType.primaryRatings}" options="${allEQ}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Secondary Ratings</td>
 <td class="data"><el:check name="sRatings" cols="9" width="85" className="small" separator="<div style=\"clear:both;\" />" checked="${eqType.secondaryRatings}" options="${allEQ}" /></td>
</tr>
<tr>
 <td class="label">First Officer's Examination</td>
 <td class="data"><el:combo name="examFO" idx="*" size="1" firstEntry="" options="${exams}" value="${fn:examFO(eqType)}" /></td>
</tr>
<tr>
 <td class="label">Flight Legs for Promotion</td>
 <td class="data"><el:text name="captLegs" size="2" max="2" idx="*" value="${empty captLegs ? 10 : captLegs}" /></td>
</tr>
<tr>
 <td class="label">Captain's Examination</td>
 <td class="data"><el:combo name="examC" idx="*" size="1" firstEntry="" options="${exams}" value="${fn:examC(eqType)}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data small"><el:box name="active" idx="*" value="1" className="sec" checked="${eqType.active}" label="Equipment Program is Active" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td>&nbsp;
<c:if test="${access.canEdit}">
<el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SAVE EQUIPMENT PROGRAM" />
</c:if>
 </td>
</tr>
</el:table>
</el:form>
<content:copyright />
</div>
</body>
</html>
