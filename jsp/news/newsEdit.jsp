<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> System News Entry</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script async>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.subject, l:10, t:'News Entry Title'});
	golgotha.form.validate({f:f.body, l:15, t:'News Entry Text'});
	golgotha.form.validate({f:f.bannerImg, ext:['jpg','png','gif'], t:'Banner Image', empty:true, maxSize:512});
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
<el:form action="newssave.do" method="post" allowUpload="true" link="${entry}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
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
 <td class="label">Banner Image</td>
 <td class="data"><el:file name="bannerImg" className="small" idx="*" size="80" max="144" /><c:if test="${entry.hasImage}"><br />
<el:box name="deleteImg" value="true" idx="*" label="Delete Banner Image" /></c:if></td>
</tr>
<tr>
 <td class="label top">Entry Text</td>
 <td class="data"><el:textbox name="body" idx="*" width="90%" height="4" className="req" resize="true">${entry.body}</el:textbox></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isHTML" value="true" label="News Entry is HTML" checked="${entry.isHTML}" /><c:if test="${empty entry}"><br />
<el:box name="noNotify" value="true" label="Don't send e-mail notification" /></c:if></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>
<c:if test="${access.canSave}"><el:button type="submit" label="SAVE SYSTEM NEWS ENTRY" /></c:if>
<c:if test="${access.canDelete}">&nbsp;<el:cmdbutton url="newsdelete" link="${entry}" label="DELETE SYSTEM NEWS ENTRY" /></c:if>
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
