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
<title><content:airline /> Journal - ${author.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.title, 8, 'Entry Title')) return false;
if (!validateText(form.body, 8, 'Entry Text')) return false;
if (!validateText(form.entryDateTime, 8, 'Publish Date')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/blog/header.jspf" %> 
<%@ include file="/jsp/blog/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="blogentry.do" method="post" op="save" link="${entry}" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><c:if test="${empty entry}">NEW </c:if>JOURNAL ENTRY</td>
</tr>
<tr>
 <td class="label">Entry Title</td>
 <td class="data"><el:text name="title" idx="*" className="pri bld req" size="32" max="128" value="${entry.title}" /></td>
</tr>
<tr>
 <td class="label">Published on</td>
 <td class="data"><el:text name="entryDateTime" idx="*" className="req" size="17" max="16" value="${fn:dateFmt(entry.date, 'MM/dd/yyyy HH:mm')}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isPrivate" idx="*" value="true" label="Journal Entry is Private" checked="${entry.private}" /><br />
<el:box name="isLocked" idx="*" value="true" label="Journal Entry is Locked" checked="${entry.locked}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Entry Text</td>
 <td class="data"><el:textbox name="body" idx="*" width="90%" height="15" className="req">${entry.body}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE JOURNAL ENTRY" /></td>
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
