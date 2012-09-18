<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Password Reset</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:attr roles="HR" attr="isHR" value="true" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
<c:if test="${!empty dupeUsers}">
if (!validateCheckBox(form.pilotCode, 1, 'Pilot Code')) return false;</c:if>
if (!validateText(form.fName, 2, 'First Name')) return false;
if (!validateText(form.lName, 2, 'Last Name')) return false;
<content:filter roles="!HR">
if (!validateText(form.eMail, 10, 'E-Mail Address')) return false;</content:filter>

setSubmit();
disableButton('SaveButton');
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
<el:form method="post" action="pwdreset.do" validate="return validate(this)">
<el:table className="form">
<tr class="title">
 <td colspan="2" class="left">PASSWORD RESET</td>
</tr>
<tr>
 <td class="label">First / Last Name</td>
 <td class="data"><el:text name="fName" required="true" idx="*" size="10" max="16" value="${param.fName}" />
 <el:text name="lName" required="true" idx="*" size="16" max="14" value="${param.lName}" /></td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:addr name="eMail" required="${isHR}" idx="*" size="32" max="80" /><br />
 <span class="small">(We need your e-mail address to verify it's really you.)</span></td>
</tr>
<c:if test="${!empty dupeUsers}">
<tr class="title caps">
 <td colspan="2">MULTIPLE USERS NAMED ${userName} FOUND</td>
</tr>
<c:forEach var="pilot" items="${dupeUsers}">
<c:if test="${fn:isActive(pilot)}">
<c:set var="pCode" value="${empty pilot.pilotCode ? 'N/A' : pilot.pilotCode}" scope="page" />
<tr>
 <td><el:radio name="pilotCode" value="${pilot.hexID}" label="${pCode}" /></td>
 <td class="data"><span class="pri bld">${pilot.name}</span> (${pilot.rank.name}, ${pilot.equipmentType})
<content:filter roles="HR"> <el:link url="mailto:${pilot.email}">${pilot.email}</el:link></content:filter></td>
</tr>
</c:if></c:forEach>
</c:if>
<content:hasmsg>
<tr>
 <td colspan="2" class="error bld caps">PASSWORD RESET FAILURE - <content:sysmsg /></td>
</tr>
</content:hasmsg>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" label="RESET PASSWORD" type="submit" /></td>
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
