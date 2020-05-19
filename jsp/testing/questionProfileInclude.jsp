<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title>Examination Question Profile</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:js name="examTake" />
<script async>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
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
<el:form action="qinclude.do" link="${question}" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Question Title Bar -->
<tr class="title caps">
 <td colspan="2">EXAMINATION QUESTION PROFILE</td>
</tr>
<tr>
 <td class="label">Question Text</td>
 <td class="data bld">${question.question}</td>
</tr>
<c:if test="${!fn:isMultiChoice(question)}">
<tr>
 <td class="label">Correct Answer</td>
 <td class="data">${question.correctAnswer}"</td>
</tr>
</c:if>
<tr>
 <td class="label">Owner Airline</td>
 <td class="data">${question.owner.name}</td>
</tr>
<tr>
 <td class="label">Airlines</td>
 <td class="data sec"><c:forEach var="airline" items="${question.airlines}">${airline.name} </c:forEach></td>
</tr>
<tr>
 <td class="label top">Pilot Examinations</td>
 <td class="data"><el:check name="examNames" idx="*" cols="5" width="160" newLine="true" className="small" checked="${question.examNames}" options="${examNames}" /></td>
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
 pixels)</span> <el:link className="pri bld small" url="javascript:void viewImage(${question.width},${question.height})">VIEW IMAGE</el:link></td>
</tr>
</c:if>
<c:if test="${fn:isMultiChoice(question)}">
<tr class="title caps">
 <td colspan="2">MULTIPLE CHOICE QUESTION</td>
</tr>
<tr>
 <td class="label top">Answer Choices</td>
 <td class="data small"><c:forEach var="choice" items="${question.choices}">${choice}<br /></c:forEach>
</tr>
<tr>
 <td class="label">Correct Answer</td>
 <td class="data">${question.correctAnswer}</td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="UPDATE QUESTION" /></td>
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
