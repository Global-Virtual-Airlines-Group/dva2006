<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Airline Transfer for ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.dbName, 'Airline Name')) return false;
if (!validateCombo(form.eqType, 'Equipment Program')) return false;

setSubmit();
disableButton('TransferButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="ranks" name="ranks" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="txairline.do" method="post" link="${pilot}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2">INTER-AIRLINE PILOT TRANSFER FOR ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Current Rank / Program</td>
 <td class="data">${pilot.rank}, <span class="sec">${pilot.equipmentType}</span></td>
</tr>
<tr>
 <td class="label">New Airline</td>
 <td class="data"><el:combo name="dbName" size="1" idx="*" options="${airlines}" firstEntry="< SELECT >" value="${param.dbName}" /></td>
</tr>
<c:if test="${!empty eqTypes}">
<tr>
 <td class="label">Equipment Program</td>
 <td class="data" valign="top"><el:combo name="eqType" size="1" idx="*" options="${eqTypes}" firstEntry="< SELECT >" /><br />
<c:forEach var="eqType" items="${eqTypes}">
${eqType.name} (Stage <fmt:int value="${eqType.stage}" />)<br />
</c:forEach></td>
</tr>
<tr>
 <td class="label">Rank</td>
 <td class="data"><el:combo name="rank" size="1" idx="*" options="${ranks}" value="${pilot.rank}" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button type="submit" className="BUTTON" label="${!empty eqTypes ? 'TRANSFER PILOT' : 'SELECT AIRLINE'}" /></td>
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
