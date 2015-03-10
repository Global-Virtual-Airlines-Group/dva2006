<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Volunteer Staff Posting - ${job.title}</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<c:set var="formURL" value="job.do" scope="page" />
<c:choose>
<c:when test="${access.canApply}">
<c:set var="formURL" value="jobapply.do" scope="page" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.body, l:32, t:'Application Text'});
golgotha.form.submit(f);
return true;
};
</script>
</c:when>
<c:when test="${access.canShortlist}">
<c:set var="formURL" value="jobsl.do" scope="page" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.sl, min:1, t:'Short-listed Applicant'});
golgotha.form.submit(f);
return true;
};
</script>
</c:when>
<c:when test="${access.canSelect}">
<c:set var="formURL" value="jobapprove.do" scope="page" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.sl, min:1, t:'Approved Applicant'});
golgotha.form.submit(f);
return true;
};
</script>
</c:when>
<c:otherwise>
<script type="text/javascript">
golgotha.local.validate = function(f) { return false; };
</script>
</c:otherwise>
</c:choose>
<script type="text/javascript">
golgotha.local.toggleBody = function(id)
{
var row = document.getElementById('desc' + id);
var linkDesc = document.getElementById('toggle' + id);
var visible = (row.style.display != 'none');
golgotha.util.display(row, !visible);
linkDesc.innerHTML = visible ? 'View' : 'Hide';
return true;
};
<c:if test="${access.canApply}">
golgotha.local.clearBody = function()
{
var f = document.forms[0];
if (confirm("Are you sure you want to clear what you've written?"))
	f.body.value = '';
	
return true;	
};</c:if>
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="${formURL}" link="${job}" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> VOLUNTEER STAFF POSTING - ${job.title}</td>
</tr>
<tr>
 <td class="label">Job Title</td>
 <td class="data pri bld">${job.title}</td>
</tr>
<tr>
 <td class="label">Job Summary</td>
 <td class="data">${job.summary}</td>
</tr>
<c:if test="${job.minLegs > 0}">
<tr>
 <td class="label">Minimum Flights</td>
 <td class="data"><fmt:int value="${job.minLegs}" /> Flight Legs</td>
</tr>
</c:if>
<c:if test="${job.minAge > 0}">
<tr>
 <td class="label">Minimum Time Active</td>
 <td class="data"><fmt:int value="${job.minAge}" /> days since joining <content:airline /></td>
</tr>
</c:if>
<tr>
 <td class="label">Created on</td>
 <td class="data"><fmt:date date="${job.createdOn}" fmt="d" /></td>
</tr>
<tr>
 <td class="label">Posting Closes on</td>
 <td class="data"><fmt:date date="${job.closesOn}" fmt="d" /></td>
</tr>
<tr>
 <td class="label top">Posting Status</td>
 <td class="data"><span class="pri bld">${job.statusName}</span>
<c:if test="${job.staffOnly}">
<br />
<span class="sec bld caps">This Job Posting is visible to <content:airline /> Staff members only</span></c:if></td>
</tr>
<content:filter roles="HR">
<c:set var="hireMgr" value="${pilots[job.hireManagerID]}" scope="page" />
<tr>
 <td class="label">Hiring Manager</td>
 <td class="data"><span class="bld">${hireMgr.name}</span> (${hireMgr.pilotCode})</td>
</tr>
</content:filter>
<tr>
 <td class="label top">Description</td>
 <td class="data"><fmt:msg value="${job.description}" /></td>
</tr>
<c:if test="${access.canViewApplicants && (!empty apps)}">
<tr class="title">
 <td colspan="2" class="left caps"><fmt:int value="${fn:sizeof(apps)}" /> Applications for this Position</td>
</tr>
<c:forEach var="app" items="${apps}">
<c:set var="pilot" value="${pilots[app.authorID]}" scope="page" />
<tr>
 <td class="label top">${pilot.name} (${pilot.pilotCode})<br />
<fmt:date date="${app.createdOn}" t="HH:mm" /></td>
 <td class="data top"><span class="bld">${pilot.rank.name}, ${pilot.equipmentType}</span>
 <fmt:int value="${pilot.legs}" /> legs, <fmt:dec value="${pilot.hours}" /> hours<br />
Joined <content:airline /> on <fmt:date fmt="d" date="${pilot.createdOn}" />
<a href="javascript:void golgotha.local.toggleBody(${pilot.ID})">Click to <span id="toggle${pilot.ID}">View</span> Application</a>
<el:cmd url="profile" link="${pilot}">Click to view pilot profile.</el:cmd>
<c:choose>
<c:when test="${access.canShortlist}">
<hr />
<el:box name="sl" idx="*" value="${fn:hex(app.authorID)}" checked="${app.shortlisted}" label="Shortlist this Job Applicant" />
</c:when>
<c:when test="${app.shortlisted}">
<br />
<span class="sec bld caps">${pilot.name} has been short-listed for this Position</span>
<c:if test="${access.canSelect}">
<hr />
<el:box name="sl" idx="*" value="${fn:hex(app.authorID)}" checked="${app.approved}" label="Select this Job Applicant" />
</c:if>
</c:when>
<c:when test="${app.approved}">
<br />
<span class="pri bld caps">${pilot.name} has been Recommended for Hire to this Position</span>
</c:when>
</c:choose></td>
</tr>
<tr id="desc${pilot.ID}" style="display:none;">
 <td class="data top" colspan="2"><fmt:msg value="${app.body}" /></td>
</tr>
</c:forEach>
</c:if>
<c:if test="${access.canComment && (!empty job.comments)}">
<content:attr attr="isHR" value="true" roles="HR" />
<tr class="title">
 <td colspan="2" class="left caps">Job Posting Comments</td>
</tr>
<c:forEach var="comment" items="${job.comments}">
<c:set var="author" value="${pilots[comment.authorID]}" scope="page" />
<c:if test="${isHR || (job.hireManagerID == comment.authorID)}">
<tr>
 <td class="label top">${author.name} (${author.pilotCode})<br />
<fmt:date date="${comment.createdOn}" t="HH:mm" /></td>
 <td class="data top"><fmt:msg value="${comment.body}" bbCode="true" /></td>
</tr>
</c:if>
</c:forEach>
</c:if>
<c:if test="${access.canApply}">
<tr class="title">
 <td colspan="2" class="left caps">Apply for this <content:airline /> Volunteer Staff Position</td>
</tr>
<tr>
 <td class="label top">Application</td>
 <td class="data"><el:textbox style="float:left; margin-right:8px;" name="body" idx="*" width="75%" className="req" height="5" resize="true"></el:textbox>
<span><c:if test="${!empty profile}"><a href="javascript:void golgotha.local.useTemplate()">Use Saved Application</a><br /></c:if>
<a href="javascript:void golgotha.local.clearBody()">Clear Text</a></span>
<div style="clear:both;"></div>
<span class="small ita">Please provide any information about yourself that qualifies you for the position of ${job.title}.</span></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="saveProfile" idx="*" value="true" label="Save my Application on file for future positions" /><br />
<el:box name="autoSubmit" idx="*" value="true" label="Automatically submit this Application for future positions" /></td>
</tr>
</c:if>
</el:table>
<c:if test="${access.canApply || access.canEdit || access.canShortlist || access.canSelect || access.canDelete}">
<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><c:if test="${access.canApply}"><el:button ID="ApplyButton" type="submit" key="S" label="APPLY FOR JOB POSTING" /></c:if>
<c:if test="${access.canEdit}"> <el:cmdbutton ID="EditButton" url="job" op="edit" link="${job}" key="E" label="EDIT JOB POSTING" /></c:if>
<c:if test="${access.canShortlist}"> <el:button ID="SLButton" type="submit" label="SELECT POSITION SHORTLIST" /></c:if>
<c:if test="${access.canReset}"> <el:cmdbutton ID="ResetButton" url="jobreset" link="${job}" label="RESET POSITION SHORTLIST" /></c:if>
<c:if test="${access.canSelect}"> <el:button ID="SelectButton" type="submit" label="SELECT SHORTLISTED APPLICANT" /></c:if>
<c:if test="${access.canComplete}"> <el:cmdbutton ID="CompleteButton" url="jobcomplete" link="${job}" label="HIRE PROCESS COMPLETED" /></c:if>
<c:if test="${access.canDelete}"> <el:cmdbutton ID="DeleteButton" url="jobdelete" link="${job}" label="DELETE JOB POSTING" /></c:if></td>
</tr>
</el:table>
</c:if>
</el:form>
<c:if test="${access.canComment}">
<script type="text/javascript">
golgotha.local.commentValidate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.body, l:12, t:'Comment Text'});
golgotha.form.submit(f);
return true;
};
</script>
<br />
<el:form action="jobcomment.do" link="${job}" method="post" validate="return golgotha.form.wrap(golgotha.local.commentValidate, this)">
<el:table className="form">
<tr class="title caps">
 <td>NEW <content:airline /> JOB POSTING COMMENT</td>
</tr>
<tr>
 <td class="mid"><el:textbox name="body" width="85%" height="5" resize="true" /></td>
</tr>
</el:table>
<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td class="mid"><el:button ID="CommentButton" type="submit" label="SAVE NEW COMMENT" />
<content:filter roles="HR">
 <el:cmdbutton ID="CloneButton" url="jobclone" link="${job}" label="CLONE JOB POSTING" /></content:filter></td>
</tr>
</el:table>
</el:form>
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
