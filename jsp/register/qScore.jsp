<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title>Questionnaire - ${applicant.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
if (!confirm("Have you scored all Questions? Hit OK to submit.")) return false;
golgotha.form.submit(f);
return true;
};
<c:if test="${hasQImages}">
golgotha.local.viewImage = function(id, x, y) {
	var flags = 'height=' + (y+45) + ',width=' + (x+45) + ',menubar=no,toolbar=no,status=yes,scrollbars=yes';
	return window.open('/exam_rsrc/' + id, 'questionImage', flags);
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
<el:form method="post" action="qscore.do" link="${exam}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Exam Title Bar -->
<tr class="title caps">
 <td colspan="2">INITIAL QUESTIONNAIRE - ${applicant.name}</td>
</tr>
<tr>
 <td class="label">Taken on</td>
 <td class="data"><fmt:date date="${exam.date}" /></td>
</tr>
<c:if test="${!empty exam.submittedOn}">
<tr>
 <td class="label">Submitted on</td>
 <td class="data"><fmt:date date="${exam.submittedOn}" /></td>
</tr>
</c:if>

<!-- Exam Questions -->
<c:forEach var="q" items="${exam.questions}">
<c:set var="hasImage" value="${q.size > 0}" scope="page" />
<!-- Question #${q.number} -->
<tr>
 <td class="label top" rowspan="${hasImage ? '3' : '2'}">Question #<fmt:int value="${q.number}" /></td>
 <td class="data">${q.question}</td>
</tr>
<c:if test="${hasImage}">
<tr>
 <td class="data small"><span class="pri bld">${q.typeName}</span> image, <fmt:int value="${q.size}" />
 bytes <span class="sec">(<fmt:int value="${q.width}" /> x <fmt:int value="${q.height}" /> pixels)</span>
 <el:link className="pri bld" url="javascript:void golgotha.local.viewImage('${q.hexID}', ${q.width}, ${q.height})">VIEW IMAGE</el:link></td>
</tr>
</c:if>
<tr>
 <td class="data sec">${q.correctAnswer}</td>
</tr>
<tr>

<!-- Score / Answer -->
<c:if test="${access.canScore}">
 <td class="mid"><input type="checkbox" class="check" name="Score${q.number}" value="true" <c:if test="${fn:correct(q)}">checked="checked"</c:if> />Correct</td>
</c:if>
<c:if test="${!access.canScore}">
 <td>&nbsp;</td>
</c:if>
 <td class="data bld">${q.answer}</td>
</tr>
</c:forEach>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;
<c:if test="${access.canScore}">
 <td><el:button type="submit" label="SCORE QUESTIONNAIRE" /></td>
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
