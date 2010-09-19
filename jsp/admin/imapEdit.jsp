<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<c:if test="${!empty mb.address}">
<title><content:airline /> IMAP Mailbox - ${mb.address}</title>
</c:if>
<c:if test="${empty mb.address}">
<title>New <content:airline /> IMAP Mailbox</title>
</c:if>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateEMail(form.IMAPAddr, 'E-Mail Address')) return false;
if (!validateNumber(form.IMAPQuota, 0, 'Mailbox Quota')) return false;

setSubmit();
disableButton('SaveButton');
disableButton('DeleteButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="IMAPServer" name="smtp.imap.server" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="imap.do" link="${mb}" op="save" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">IMAP MAILBOX FOR ${pilot.name} at ${IMAPServer}</td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:text name="IMAPAddr" idx="*" className="bld" size="22" max="32" value="${mb.address}" /></td>
</tr>
<tr>
 <td class="label">Mailbox Quota</td>
 <td class="data"><el:text name="IMAPQuota" idx="*" size="8" max="10" value="${mb.quota}" /> bytes</td>
</tr>
<tr>
 <td class="label">Mailbox Directory</td>
 <td class="data"><el:text name="IMAPPath" idx="*" size="36" max="64" value="${mb.mailDirectory}" /></td>
</tr>
<tr>
 <td class="label top">Mailbox Aliases</td>
 <td class="data"><el:textbox name="IMAPAliases" idx="*" width="40%" height="${(fn:sizeof(mb.aliases) > 4) ? fn:sizeof(mb.aliases) : 4}" resize="true">${aliases}</el:textbox></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="IMAPActive" idx="*" value="true" checked="${mb.active}" label="Mailbox is Active" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SAVE IMAP MAILBOX" />
<c:if test="${!empty mb.address}">
 <el:cmdbutton ID="DeleteButton" url="imapdelete" link="${mb}" label="DELETE IMAP MAILBOX" /> 
</c:if></td>
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
