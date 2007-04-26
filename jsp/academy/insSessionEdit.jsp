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
<title>Flight Academy Instruction - ${empty session ? 'New Session' : session.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="datePicker" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.instructor, 'Instructor Name')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
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
<el:form action="isession.do" linkID="${fn:dbID(session)}" op="save" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> FLIGHT ACADEMY INSTRUCTION SESSION</td>
</tr>
<tr>
 <td class="label">Pilot Name</td>
 <td class="data"><el:cmd url="profile" linkID="0x${pilot.ID}" className="pri bld">${pilot.name}</el:cmd>
 <span class="bld">(${pilot.pilotCode})</span>, ${pilot.rank}, ${pilot.equipmentType}</td>
</tr>
<tr>
 <td class="label">Instructor Name</td>
 <td class="data"><el:combo name="instructor" idx="*" size="1" className="req" options="${instructors}" value="${session.instructorID}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Course Name</td>
 <td class="data"><el:cmd url="course" linkID="0x${course.ID}" className="bld">${course.name}</el:cmd></td>
</tr>
<tr>
 <td class="label">Start Date/Time</td>
 <td class="data"><el:text name="startDate" idx="*" size="10" max="10" value="${fn:dateFmt(startTime, 'MM/dd/yyyy')}" className="req" />
 at <el:text name="startTime" idx="*" size="4" max="5" value="${fn:dateFmt(startTime, 'HH:mm')}" className="req" />
&nbsp;<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].startDate')" />
&nbsp;<span class="small">All dates/times are ${pageContext.request.userPrincipal.TZ.name}. (Format: ${dateFmt} HH:mm)</span></td>
</tr>
<tr>
 <td class="label">End Date/Time</td>
 <td class="data"><el:text name="endDate" idx="*" size="10" max="10" value="${fn:dateFmt(endTime, 'MM/dd/yyyy')}" className="req" />
 at <el:text name="endTime" idx="*" size="4" max="5" value="${fn:dateFmt(endTime, 'HH:mm')}" className="req" />
&nbsp;<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].endDate')" />
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
 <td class="label" valign="top">Remarks</td>
 <td class="data"><el:textbox name="remarks" idx="*" width="90%" height="6">${session.comments}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="UPDATE INSTRUCTION SESSION" /></td>
</tr>
</el:table>
<el:text name="course" type="hidden" value="${course.hexID}" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
