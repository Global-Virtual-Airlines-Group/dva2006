<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
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
<script type="text/javascript">
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
<c:set var="dateFmt" value="${user.dateFormat}" scope="page" />
<c:set var="timeFmt" value="${user.timeFormat}" scope="page" />
<c:set var="entryDate" value="${empty entry ? now : entry.date}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="blogentry.do" method="post" op="save" link="${entry}" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><c:if test="${empty entry}">NEW </c:if>JOURNAL ENTRY</td>
</tr>
<tr> 
 <td class="label">Entry Title</td>
 <td class="data"><el:text name="title" idx="*" className="pri bld req" size="32" max="128" value="${entry.title}" /></td>
</tr>
<tr>
 <td class="label">Published on</td>
 <td class="data"><el:text name="entryDate" idx="*" size="10" max="10" value="${fn:dateFmt(entryDate, dateFmt)}" className="req" />
 at <el:text name="entryTime" idx="*" size="${fn:length(timeFmt) - 1}" max="${fn:length(timeFmt)}" value="${fn:dateFmt(entryDate, timeFmt)}" className="req" />
&nbsp;<span class="small">All dates/times are ${user.TZ.name}. (Format: ${dateFmt} ${timeFmt})</span></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isPrivate" idx="*" value="true" label="Journal Entry is Private" checked="${entry.private}" /><br />
<el:box name="isLocked" idx="*" value="true" label="Journal Entry is Locked" checked="${entry.locked}" /></td>
</tr>
<tr>
 <td class="label top">Entry Text</td>
 <td class="data"><el:textbox name="body" idx="*" width="90%" height="15" className="req">${entry.body}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
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
