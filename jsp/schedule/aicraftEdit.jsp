<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Aircraft<c:if test="${!empty aircraft}"> - ${aircraft.name}</c:if></title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.name, 4, 'Aircraft Name')) return false;
if (!validateNumber(form.range, 1, 'Arcraft Range')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:sysdata var="airlines" name="apps" mapValues="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="aircraft.do" method="post" linkID="${aircraft.name}" op="save" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">AIRCRAFT PROFILE</td>
</tr>
<tr>
 <td class="label">Aircraft Name</td>
 <td class="data"><el:text name="name" idx="*" className="pri bld req" size="15" max="15" value="${aircraft.name}" /></td>
</tr>
<tr>
 <td class="label">Maximum Range</td>
 <td class="data"><el:text name="range" idx="*" className="req" size="4" max="5" value="${aircraft.range}" /> miles</td>
</tr>
<tr>
 <td class="label" valign="top">IATA Equipment Code(s)</td>
 <td class="data"><el:textbox name="iataCodes" idx="*" width="30" height="3">${aircraft.IATA}</el:textbox></td>
</tr>
<tr>
 <td class="label" valign="top">Web Applications</td>
 <td class="data"><el:check name="airlines" width="180" options="${airlines}" checked="${aircrat.apps}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE AIRCRAFT PROFILE" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
