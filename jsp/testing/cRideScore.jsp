<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Check Ride</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{


return true;
}

function download(fileName)
{
self.location='/video/' + fileName;
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form action="crscore.do" method="POST" allowUpload="true" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">${fn:eqType(checkRide)} CHECK RIDE FOR ${pilot.name}</td>
</tr>
<tr>
 <td class="label">${fn:pending(checkRide) ? 'Assigned' : 'Scored'} by</td>
 <td class="data sec bld">${scorer.name}</td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data">${fn:eqType(checkRide)} (Stage <fmt:int value="${checkRide.stage}" />)</td>
</tr>
<tr>
 <td class="label">Assigned on</td>
 <td class="data"><fmt:date fmt="d" date="${checkRide.date}" /></td>
</tr>
<tr>
 <td class="label">Assigned by</td>
 <td class="data bld">${scorer.name}</td>
</tr>
<tr>
 <td class="label">Submitted on</td>
 <td class="data"><fmt:date fmt="d" date="${checkRide.submittedOn}" /></td>
</tr>
<c:if test="${access.canScore}">
<tr>
 <td class="label">Check Ride Status</td>
 <td class="data"><el:check type="radio" name="passFail" idx="*" options="${passFail}" />
<el:button onClick="void download('${checkRide.fileName}')" className="BUTTON" label="DOWNLOAD FLIGHT VIDEO" /></td>
</tr>
</c:if>
<c:if test="${access.canSubmit}">
<tr>
 <td class="label">Upload Flight Video</td>
 <td class="data"><el:file name="video" className="small" idx="*" size="80" max="144" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Comments</td>
 <td class="data valign="top"><el:textbox name="comments" idx="*" width="120" height="6">${checkRide.comments}</el:textbox></td>
</tr>
<c:if test="${checkRide.size > 0}">
<tr>
 <td class="label">Flight Video Size</td>
 <td class="data sec bld"><fmt:int value="${checkRide.size}" /> bytes</td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td>
<c:if test="${access.canDelete}">
 <el:cmdbutton url="examdelete" linkID="0x${checkRide.ID}" op="checkride" label="DELETE CHECK RIDE" />
</c:if>
<c:if test="${access.canScore}">
 <el:button ID="SubmitButton" type="SUBMIT" className="BUTTON" label="EVALUATE CHECK RIDE" />
</c:if>
 </td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</div>
</body>
</html>
