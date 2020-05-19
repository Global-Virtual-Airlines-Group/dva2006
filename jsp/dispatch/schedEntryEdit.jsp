<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title>ACARS Dispatcher Service Entry</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:js name="datePicker" />
<script async>
golgotha.local.validate = function(f) {
	if ((!golgotha.form.check()) || (!f.comments)) return false;
	golgotha.form.validate({f:f.startDate, t:'Service Start Date'});
	golgotha.form.validate({f:f.startTime, t:'Service Start Time'});
	golgotha.form.validate({f:f.endDate, t:'Service End Date'});
	golgotha.form.validate({f:f.endTime, t:'Service End Time'});
	golgotha.form.submit(f);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>
<content:sysdata var="dateFmt" name="time.date_format" />
<c:set var="tz" value="${pageCotnext.request.userPrincipal.TZ}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="dspentry.do" link="${entry}" op="save" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> ACARS DISPATCHER SERVICE SESSION</td>
</tr>
<tr>
 <td class="label">Start Date/Time</td>
 <td class="data"><el:text name="startDate" idx="*" size="10" max="10" value="${fn:dateFmt(startTime, 'MM/dd/yyyy')}" className="req" />
 at <el:text name="startTime" idx="*" size="4" max="5" value="${fn:dateFmt(startTime, 'HH:mm')}" className="req" />
&nbsp;<el:button label="CALENDAR" onClick="void show_calendar('forms[0].startDate')" />
&nbsp;<span class="small">All dates/times are ${tz.name}. (Format: ${dateFmt} HH:mm)</span></td>
</tr>
<tr>
 <td class="label">End Date/Time</td>
 <td class="data"><el:text name="endDate" idx="*" size="10" max="10" value="${fn:dateFmt(endTime, 'MM/dd/yyyy')}" className="req" />
 at <el:text name="endTime" idx="*" size="4" max="5" value="${fn:dateFmt(endTime, 'HH:mm')}" className="req" />
&nbsp;<el:button label="CALENDAR" onClick="void show_calendar('forms[0].endDate')" />
&nbsp;<span class="small">All dates/times are ${tz.name}. (Format: ${dateFmt} HH:mm)</span></td>
</tr>
<tr>
 <td class="label top">Remarks</td>
 <td class="data"><el:textbox name="comments" idx="*" width="90%" height="6">${entry.comments}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="UPDATE ACARS DISPATCHER SERVICE SESSION" />
<c:if test="${access.canDelete}">&nbsp;<el:cmdbutton url="dspentrydelete" link="${entry.ID}" label="DELETE SERVICE SESSION" /></c:if></td>
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
