<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<c:choose>
<c:when test="${!empty entry}">
<title><content:airline /> Newsletter - ${entry.name}</title>
</c:when>
<c:otherwise>
<title>New <content:airline /> Newsletter</title>
</c:otherwise>
</c:choose>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:js name="datePicker" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.title, l:10, t:'Newsletter Title'});
	golgotha.form.validate({f:f.category, t:'Newsletter Category'});
	golgotha.form.validate({f:f.desc, l:10, t:'Description'});
	golgotha.form.validate({f:f.newsDate, l:8, t:'Publishing Date'});
	golgotha.form.validate({f:f.file, ext:['pdf'], t:'Uploaded Newsletter'});
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
<content:sysdata var="dateFmt" name="time.date_format" />
<content:sysdata var="cats" name="airline.newsletters.categories" />
<content:enum var="securityOptions" className="org.deltava.beans.fleet.Security" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="newsletter.do" linkID="${entry.fileName}" op="save" method="post" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
<c:choose>
<c:when test="${!empty entry}">
 <td colspan="2"><content:airline /> NEWSLETTER - ${entry.name}</td>
</c:when>
<c:otherwise>
 <td colspan="2">NEW <content:airline /> NEWSLETTER</td>
</c:otherwise>
</c:choose>
</tr>
<tr>
 <td class="label">Newsletter Title</td>
 <td class="data"><el:text name="title" className="pri bld req" idx="*" size="48" max="80" value="${entry.name}" /></td>
</tr>
<tr>
 <td class="label">Category</td>
 <td class="data"><el:combo name="category" idx="*" size="1" className="req" options="${cats}" value="${entry.category}" firstEntry="[ CATEGORY ]" /></td>
</tr>
<tr>
 <td class="label top">Description</td>
 <td class="data"><el:textbox name="desc" idx="*" width="80%" height="4" className="req">${entry.description}</el:textbox></td>
</tr>
<tr>
 <td class="label">Publishing Date</td>
 <td class="data"><el:text name="newsDate" idx="*" size="10" max="10" className="req" value="${fn:dateFmt(entry.date, 'MM/dd/yyyy')}" />
&nbsp;<el:button label="CALENDAR" onClick="void show_calendar('forms[0].newsDate')" />
&nbsp;<span class="small">(Format: ${dateFmt})</span></td>
</tr>
<c:if test="${!empty entry}">
<tr>
 <td class="label">Document Size</td>
<c:if test="${entry.size > 0}">
 <td class="data sec bld"><fmt:int value="${entry.size}" /> bytes</td>
</c:if>
<c:if test="${entry.size == 0}">
 <td class="data warning bld caps">FILE NOT PRESENT ON FILESYSTEM</td>
</c:if>
</tr>
<tr>
 <td class="label">Download Statistics</td>
 <td class="data">Downloaded <b><fmt:int value="${entry.downloadCount}" /></b> times</td>
</tr>
</c:if>
<tr>
 <td class="label">Document Security</td>
 <td class="data"><el:combo name="security" idx="*" size="1" required="true" value="${entry.security}" options="${securityOptions}" /></td>
</tr>
<tr>
 <td class="label">Update File</td>
 <td class="data"><el:file name="file" className="small req" size="96" max="192" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="noNotify" idx="*" value="true" label="Don't send notification e-mail" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;
<c:if test="${access.canEdit || access.canCreate}">
<el:button ID="SaveButton" type="submit" label="SAVE MANUAL" />
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
