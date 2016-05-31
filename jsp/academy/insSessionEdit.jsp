<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title>Flight Academy Instruction - ${empty session ? "New Session" : session.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="datePicker" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.instructor, t:'Instructor Name'});
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

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="isession.do" link="${session}" op="save" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> FLIGHT ACADEMY INSTRUCTION SESSION</td>
</tr>
<tr>
 <td class="label">Pilot Name</td>
 <td class="data"><el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.name}</el:cmd>
 <span class="bld">(${pilot.pilotCode})</span>, ${pilot.rank.name}, ${pilot.equipmentType}</td>
</tr>
<tr>
 <td class="label">Instructor Name</td>
 <td class="data"><el:combo name="instructor" idx="*" size="1" className="req" options="${instructors}" value="${session.instructorID}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Course Name</td>
 <td class="data"><el:cmd url="course" link="${course}" className="bld">${course.name}</el:cmd></td>
</tr>
<tr>
 <td class="label">Start Date/Time</td>
 <td class="data"><el:text name="startDate" idx="*" size="10" max="10" value="${fn:dateFmt(startTime, 'MM/dd/yyyy')}" className="req" />
 at <el:text name="startTime" idx="*" size="4" max="5" value="${fn:dateFmt(startTime, 'HH:mm')}" className="req" />
&nbsp;<el:button label="CALENDAR" onClick="void show_calendar('forms[0].startDate')" />
&nbsp;<span class="small">All dates/times are ${pageContext.request.userPrincipal.TZ.name}. (Format: ${dateFmt} HH:mm)</span></td>
</tr>
<tr>
 <td class="label">End Date/Time</td>
 <td class="data"><el:text name="endDate" idx="*" size="10" max="10" value="${fn:dateFmt(endTime, 'MM/dd/yyyy')}" className="req" />
 at <el:text name="endTime" idx="*" size="4" max="5" value="${fn:dateFmt(endTime, 'HH:mm')}" className="req" />
&nbsp;<el:button label="CALENDAR" onClick="void show_calendar('forms[0].endDate')" />
&nbsp;<span class="small">All dates/times are ${pageContext.request.userPrincipal.TZ.name}. (Format: ${dateFmt} HH:mm)</span></td>
</tr>
<tr>
 <td class="label">Status</td>
 <td class="data sec"><el:combo name="status" idx="*" size="1" className="req" options="${statuses}" value="${session.statusName}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><c:if test="${!empty session}"><el:box name="noShow" idx="*" value="true" checked="${session.noShow}" label="Pilot did not attend Instruction Session" /><br /></c:if>
<el:box name="noSend" idx="*" value="true" label="Don't send notification e-mail" /></td>
</tr>
<tr>
 <td class="label top">Remarks</td>
 <td class="data"><el:textbox name="remarks" idx="*" width="90%" height="4" resize="true">${session.comments}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="UPDATE INSTRUCTION SESSION" /></td>
</tr>
</el:table>
<el:text name="course" type="hidden" value="${course.hexID}" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
