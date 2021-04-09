<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Volunteer Staff Posting</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:js name="datePicker" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.title, l:10, t:'Job Title'});
	golgotha.form.validate({f:f.summary, l:16, t:'Job Summary'});
	golgotha.form.validate({f:f.body, l:32, t:'Job Description'});
	golgotha.form.validate({f:f.hireMgr, t:'Hiring Manager'});
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
<el:form action="job.do" op="save" link="${job}" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> VOLUNTEER STAFF POSTING</td>
</tr>
<tr>
 <td class="label">Job Title</td>
 <td class="data"><el:text name="title" idx="*" className="bld req" size="32" max="48" value="${job.title}" /></td>
</tr>
<tr>
 <td class="label top">Job Summary</td>
 <td class="data"><el:text name="summary" idx="*" className="req" size="80" max="128" value="${job.summary}" /><br />
<span class="small ita">(This is a brief description of the position that is posted on the careers page.)</span></td>
</tr>
<tr>
 <td class="label">Minimum Legs</td>
 <td class="data"><el:text name="minLegs" idx="*" size="2" max="3" value="${job.minLegs}" /></td>
</tr>
<tr>
 <td class="label">Minimum Days Active</td>
 <td class="data"><el:text name="minAge" idx="*" size="2" max="4" value="${job.minAge}" /> days since joining <content:airline /></td>
</tr>
<tr>
 <td class="label">Posting Closes on</td>
 <td class="data"><el:text name="closeDate" idx="*" size="10" max="10" value="${fn:dateFmt(job.closesOn, 'MM/dd/yyyy')}" className="req" />
&nbsp;<el:button label="CALENDAR" onClick="void show_calendar('forms[0].closeDate')" /></td>
</tr>
<tr>
 <td class="label">Hiring Manager</td>
 <td class="data"><el:combo name="hireMgr" idx="*" size="1" className="req" options="${hireMgrs}" firstEntry="[ SELECT ]" value="${job.hireManagerID}" /></td>
</tr>
<tr>
 <td class="label">Posting Status</td>
 <td class="data"><el:combo name="status" idx="*" options="${statuses}" value="${job.statusName}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="staffOnly" idx="*" value="true" className="small" checked="${job.staffOnly}" label="Posting is visible to Staff members only" /></td>
</tr>
<tr>
 <td class="label top">Description</td>
 <td class="data"><el:textbox name="body" idx="*" width="90%" className="req" height="5" resize="true">${job.description}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE JOB POSTING" /></td>
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
