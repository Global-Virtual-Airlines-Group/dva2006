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
<title>${exam.name} - ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;

if (!confirm("Have you scored all Questions? Hit OK to submit.")) return false;
setSubmit();
disableButton('ScoreButton');
return true;
}
<c:if test="${hasQImages}">
function viewImage(id, x, y)
{
var flags = 'height=' + y + ',width=' + x + ',menubar=no,toolbar=no,status=yes,scrollbars=yes';
var w = window.open('/exam_rsrc/' + id, 'questionImage', flags);
return true;
}
</c:if>
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="examscore.do" link="${exam}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<!-- Exam Title Bar -->
<tr class="title caps">
 <td colspan="2">${exam.name} EXAMINATION - ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Taken on</td>
 <td class="data"><fmt:date date="${exam.date}" /></td>
</tr>
<c:if test="${!empty exam.submittedOn}">
<c:set var="late" value="${(exam.submittedOn.time - exam.expiryDate.time) / 1000}" scope="request" />
<c:set var="lateH" value="${late / 3600}" scope="request" />
<c:set var="lateM" value="${(late % 3600) / 60}" scope="request" />
<c:set var="lateS" value="${late % 60}" scope="request" />
<tr>
 <td class="label">Submitted on</td>
 <td class="data"><fmt:date date="${exam.submittedOn}" />
<c:if test="${exam.submittedOn > exam.expiryDate}"><span class="error caps">
<c:if test="${lateH > 0}"><fmt:int value="${lateH}" /> hours, </c:if>
<c:if test="${lateM > 0}"><fmt:int value="${lateM}" /> minutes, </c:if>
<c:if test="${lateS > 0}"><fmt:int value="${lateS}" /> seconds</c:if> late</span>
</c:if>
 </td>
</tr>
</c:if>

<!-- Exam Questions -->
<c:forEach var="q" items="${exam.questions}">
<c:set var="mcCSS" value="${fn:isMultiChoice(q) ? 'opt1' : ''}" scope="request" />
<c:set var="hasImage" value="${q.size > 0}" scope="request" />
<!-- Question #${q.number} -->
<tr>
 <td class="label" rowspan="${hasImage ? '3' : '2'}" valign="top">Question #<fmt:int value="${q.number}" /></td>
 <td class="data ${mcCSS}">${q.question}</td>
</tr>
<c:if test="${hasImage}">
<tr>
 <td class="data small">RESOURCE - <span class="pri bld">${q.typeName}</span> image, <fmt:int value="${q.size}" />
 bytes <span class="sec">(<fmt:int value="${q.width}" /> x <fmt:int value="${q.height}" /> pixels)</span>
 <el:link className="pri bld" url="javascript:void viewImage('${fn:hex(q.ID)}', ${q.width}, ${q.height})">VIEW IMAGE</el:link></td>
</tr>
</c:if>
<tr>
 <td class="data small ${mcCSS}"><span class="${q.exactMatch ? 'warn' : 'sec'}">${q.correctAnswer}</span></td>
</tr>

<!-- Score / Answer -->
<tr>
 <td class="mid"><el:box className="small" name="Score${q.number}" value="true" checked="${fn:correct(q)}" label="Correct" /></td>
 <td class="data bld ${mcCSS}">${q.answer}</td>
</tr>
</c:forEach>

<!-- Examination Comments -->
<tr>
 <td class="label" valign="top">Scorer Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="80%" height="6">${exam.comments}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="ScoreButton" type="SUBMIT" className="BUTTON" label="SCORE EXAMINATION" /></td>
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
