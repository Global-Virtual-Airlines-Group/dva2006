<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> System News Entry</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.subject, 10, 'News Entry Title')) return false;
if (!validateText(form.body, 15, 'News Entry Text')) return false;

setSubmit();
disableButton('SubmitButton');
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
<el:form action="newssave.do" method="post" link="${entry}" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> SYSTEM NEWS ENTRY</td>
</tr>
<tr>
 <td class="label">Entry Title</td>
 <td class="data"><el:text name="subject" className="pri bld req" idx="*" size="64" max="96" value="${entry.subject}" /></td>
</tr>
<c:if test="${!empty entry}">
<tr>
 <td class="label">Entry Date</td>
 <td class="data"><fmt:date fmt="d" date="${entry.date}" /></td>
</tr>
</c:if>
<tr>
 <td class="label top">Entry Text</td>
 <td class="data"><el:textbox name="body" idx="*" width="90%" height="6" className="req">${entry.body}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td>
<c:if test="${access.canSave}">
<el:button type="SUBMIT" className="BUTTON" label="SAVE SYSTEM NEWS ENTRY" />
</c:if>
<c:if test="${access.canDelete}">
<el:cmdbutton url="newsdelete" link="${entry}" label="DELETE SYSTEM NEWS ENTRY" />
</c:if>
 </td>
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
