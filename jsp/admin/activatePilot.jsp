<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Reactivation</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.eMail, 'E-Mail Address')) return false;

setSubmit();
disableButton('ActivateButton');
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
<el:form action="activate.do" method="POST" link="${pilot}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2">PILOT REACTIVATION - ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data sec bld">${pilot.equipmentType}</td>
</tr>
<tr>
 <td class="label">Rank</td>
 <td class="data"><el:combo name="rank" idx="*" size="1" options="${eqType.ranks}" value="${pilot.rank}" /></td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:text name="eMail" idx="*" size="32" max="80" value="${pilot.email}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="ActivateButton" type="submit" className="BUTTON" label="REACTIVATE PILOT" /></td>
</tr>
</el:table>
<el:text name="op" type="hidden" value="force" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
