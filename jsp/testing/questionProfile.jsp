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
<title>Examination Question Profile</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<c:if test="${question.size > 0}">
<script language="JavaScript" type="text/javascript">
function viewImage(x, y)
{
var flags = 'height=' + y + ',width=' + x + ',menubar=no,toolbar=no,status=yes,scrollbars=yes';
var w = window.open('/exam_rsrc/${fn:hex(question.ID)}', 'questionImage', flags);
return true;
}
</script></c:if>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form" pad="default" space="default">
<!-- Question Title Bar -->
<tr class="title caps">
 <td colspan="2">${fn:isMultiChoice(question) ? 'MULTIPLE CHOICE ' : ''}EXAMINATION QUESTION PROFILE</td>
</tr>
<tr>
 <td class="label">Question Text</td>
 <td class="data bld">${question.question}</td>
</tr>
<c:if test="${fn:isMultiChoice(question)}">
<tr>
 <td class="label" valign="top">Answer Choices</td>
 <td class="data"><c:forEach var="choice" items="${question.choices}">${choice}<br />
</c:forEach></td>
</tr>
</c:if>
<tr>
 <td class="label">Correct Answer</td>
 <td class="data">${question.correctAnswer}</td>
</tr>
<tr>
 <td class="label">Pilot Examinations</td>
 <td class="data small"><fmt:list value="${question.examNames}" delim=", " /></td>
</tr>
<tr>
 <td class="label">Statistics</td>
<c:if test="${question.totalAnswers > 0}">
 <td class="data">Answered <fmt:int value="${question.totalAnswers}" /> times,
 <fmt:int value="${question.correctAnswers}" /> correctly 
 (<fmt:dec value="${question.correctAnswers / question.totalAnswers * 100}" />%)</td>
</c:if>
<c:if test="${question.totalAnswers == 0}">
 <td class="data bld">This Question has never been included in a Pilot Examination</td>
</c:if>
</tr>
<c:if test="${question.size > 0}">
<tr>
 <td class="label">Image Information</td>
 <td class="data"><span class="pri bld">${question.typeName}</span> image, <fmt:int value="${question.size}" />
 bytes <span class="sec">(<fmt:int value="${question.width}" /> x <fmt:int value="${question.height}" />
 pixels) <el:link className="pri bld small" url="javascript:void viewImage(${question.width},${question.height})">VIEW IMAGE</el:link></td>
</tr>
</c:if>
<tr>
 <td class="label">&nbsp;</td>
<c:if test="${question.active}">
 <td class="data ter bld caps">Question is Available</td>
</c:if>
<c:if test="${!question.active}">
 <td class="data error bld caps">Question is Not Available</td>
</c:if>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:cmdbutton url="qprofile" link="${question}" op="edit" label="EDIT QUESTION" />
<c:if test="${access.canDelete && (question.totalAnswers == 0)}">
 <el:cmdbutton url="qpdelete" link="${question}" label="DELETE QUESTION" />
</c:if>
</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
