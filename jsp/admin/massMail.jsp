<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Mass Mailing</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.subject, 7, 'Message Subject')) return false;
if (!validateText(form.body, 25, 'Message Body')) return false;
if (!validateCombo(form.eqType, 'Recipients Equipment Type')) return false;
if (!validateFile(form.fAttach, 'pdf,txt', 'Attached File')) return false;

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
<el:form action="massmail.do" method="post" allowUpload="true" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2">PILOT MASS E-MAIL MESSAGE</td>
</tr>
<tr>
 <td class="label">Message Subject</td>
 <td class="data"><el:text name="subject" idx="*" size="48" max="64" className="bld req" value="${param.subject}" /></td>
</tr>
<tr>
 <td class="label">Attached File</td>
 <td class="data"><el:file name="fAttach" idx="*" size="96" max="144" /></td>
</tr>
<tr>
 <td class="label">Recipient Program / Security Role</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" firstEntry="-" className="req" options="${eqTypes}" value="${param.eqType}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Message Text</td>
 <td class="data"><el:textbox name="body" className="req" idx="*" width="80%" height="15">${param.body}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SEND MESSAGE" /></td>
</tr>
</el:table>
</el:form>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
