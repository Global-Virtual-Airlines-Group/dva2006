<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> NOTAM</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.subject, l:10, t:'Notice Title'});
golgotha.form.validate({f:f.body, l:15, t:'NOTAM Text'});
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
<el:form action="notamsave.do" method="post" link="${entry}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> NOTICE TO AIRMEN</td>
</tr>
<tr>
 <td class="label">Notice Title</td>
 <td class="data"><el:text name="subject" className="pri bld req" idx="*" size="64" max="96" value="${entry.subject}" /></td>
</tr>
<c:if test="${!empty entry}">
<tr>
 <td class="label">Notice Date</td>
 <td class="data"><fmt:date fmt="d" date="${entry.date}" /></td>
</tr>
</c:if>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="active" idx="*" value="true" label="Notice is In Effect" checked="${entry.active}" /><br />
<el:box name="isHTML" value="true" label="Notice is HTML" checked="${entry.isHTML}" />
<el:box name="noNotify" value="true" label="Don't send e-mail notification" /></td>
</tr>
<tr>
 <td class="label top">Entry Text</td>
 <td class="data"><el:textbox name="body" idx="*" width="90%" height="4" className="req" resize="true">${entry.body}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>
<c:if test="${access.canSave}">
<el:button type="submit" label="SAVE NOTAM" />
</c:if>
<c:if test="${access.canDelete}">
<el:cmdbutton url="newsdelete" op="notam" link="${entry}" label="DELETE NOTAM" />
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
