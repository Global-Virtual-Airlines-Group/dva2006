<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Password Reset</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:attr roles="HR" attr="isHR" value="true" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
<c:if test="${!empty dupeUsers}">
golgotha.form.validate({f:f.pilotCode, min:1, t:'Pilot Code'});</c:if>
golgotha.form.validate({f:f.fName, l:2, t:'First Name'});
golgotha.form.validate({f:f.lName, l:2, t:'Last Name'});
<content:filter roles="!HR">
golgotha.form.validate({f:f.eMail, l:10, t:'E-Mail Address', addr:true});</content:filter>
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
<el:form method="post" action="pwdreset.do" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title">
 <td colspan="2" class="left">PASSWORD RESET</td>
</tr>
<tr>
 <td class="label">First / Last Name</td>
 <td class="data"><el:text name="fName" required="true" idx="*" size="10" max="16" value="${param.fName}" />
 <el:text name="lName" required="true" idx="*" size="16" max="14" value="${param.lName}" /></td>
</tr>
<content:filter roles="!HR">
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:addr name="eMail" required="${isHR}" idx="*" size="32" max="80" /><br />
 <span class="small">(We need your e-mail address to verify it's really you.)</span></td>
</tr>
</content:filter>
<c:if test="${!empty dupeUsers}">
<tr class="title caps">
 <td colspan="2">MULTIPLE USERS NAMED ${userName} FOUND</td>
</tr>
<c:forEach var="pilot" items="${dupeUsers}">
<c:set var="pCode" value="${empty pilot.pilotCode ? 'N/A' : pilot.pilotCode}" scope="page" />
<tr>
 <td><el:radio name="pilotCode" value="${pilot.hexID}" label="${pCode}" /></td>
 <td class="data"><span class="pri bld">${pilot.name}</span> (${pilot.rank.name}, ${pilot.equipmentType})
<content:filter roles="HR"> <el:link url="mailto:${pilot.email}">${pilot.email}</el:link></content:filter></td>
</tr>
</c:forEach>
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
