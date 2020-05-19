<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<c:set var="examName" value="${empty eProfile ? 'New Examination' : eProfile.name}" scope="page" />
<html lang="en">
<head>
<title>${examName}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script async>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.examName, l:10, t:'Examination Name'});
golgotha.form.validate({f:f.stage, min:1, t:'Examination Stage'});
golgotha.form.validate({f:f.minStage, min:0, t:'Examination Minimum Stage'});
golgotha.form.validate({f:f.questions, min:1, t:'Examination Size'});
golgotha.form.validate({f:f.passScore, min:0, t:'Passing Score'});
golgotha.form.validate({f:f.time, min:5, t:'Examination Duration'});
golgotha.form.validate({f:f.owner, t:'Owner'});
golgotha.form.validate({f:f.airline, min:1, t:'Airline'});
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
<content:sysdata var="airlines" name="apps" mapValues="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="eprofile.do" linkID="${eProfile.name}" op="save" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Exam Title Bar -->
<tr class="title caps">
 <td colspan="2">EXAMINATION PROFILE - ${examName}</td>
</tr>
<tr>
 <td class="label">Examination Name</td>
 <td class="data"><el:text name="examName" className="pri bld req" idx="*" size="32" max="48" value="${eProfile.name}" /></td>
</tr>
<tr>
 <td class="label">Limit to Equipment Program</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" firstEntry="N/A" options="${eqTypes}" value="${eProfile.equipmentType}" />
 <span class="small ita">This should be N/A for First Officer Examinations.</span></td>
</tr>
<tr>
 <td class="label">Stage</td>
 <td class="data"><el:text name="stage" idx="*" size="1" max="1" className="req" value="${eProfile.stage}" /></td>
</tr>
<tr>
 <td class="label">Minimum Stage</td>
 <td class="data"><el:text name="minStage" idx="*" size="1" max="1" className="req" value="${eProfile.minStage}" /></td>
</tr>
<tr>
 <td class="label">Questions</td>
 <td class="data"><el:text name="size" idx="*" size="2" max="2" className="req" value="${eProfile.size}" /></td>
</tr>
<tr>
 <td class="label">Passing Score</td>
 <td class="data"><el:text name="passScore" idx="*" size="2" max="2" className="req" value="${eProfile.passScore}" /></td>
</tr>
<tr>
 <td class="label">Testing Time</td>
 <td class="data"><el:text name="time" idx="*" size="3" max="4" className="req" value="${eProfile.time}" /> minutes</td>
</tr>
<tr>
 <td class="label">Owner Airline</td>
 <td class="data"><el:combo name="owner" idx="*" size="1" className="req" firstEntry="-" options="${airlines}" value="${eProfile.owner}" /></td>
</tr>
<tr>
 <td class="label">Airlines</td>
 <td class="data"><el:check name="airline" width="175" options="${airlines}" className="req" checked="${eProfile.airlines}" /></td>
</tr>
<tr>
 <td class="label top">Allowed Scorers</td>
 <td class="data"><span class="ita">Unselect all Scorers to allow anyone with Examination scoring access to score this Examination.</span><br /> 
<el:check name="scorerIDs" width="170" cols="5" options="${scorers}" className="small" checked="${eProfile.scorerIDs}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="active" idx="*" value="true" label="Examination is Active" checked="${eProfile.active}" /><br />
<el:box name="doNotify" idx="*" value="true" label="Notify Scorers when Submitted" checked="${eProfile.notify}" /><br />
<el:box name="isAcademy" className="sec" idx="*" value="true" label="This is a Fleet Academy Examination" checked="${eProfile.academy}" /></td>
</tr>
<%@ include file="/jsp/auditLog.jspf" %>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE EXAMINATION PROFILE" /></td>
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
