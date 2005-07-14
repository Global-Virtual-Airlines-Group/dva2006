<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Examination Question Profile</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:table className="form" pad="default" space="default">
<!-- Question Title Bar -->
<tr class="title caps">
 <td colspan="2">EXAMINATION QUESTION PROFILE</td>
</tr>
<tr>
 <td class="label">Question Text</td>
 <td class="data bld">${question.question}</td>
</tr>
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
<tr>
 <td class="label">&nbsp;</td>
<c:if test="${eProfile.active}">
 <td class="data ter bld caps">Question is Available</td>
</c:if>
<c:if test="${!eProfile.active}">
 <td class="data error bld caps">Question is Not Available</td>
</c:if>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:cmdbutton url="qprofile" linkID="${question.ID}" op="edit" label="EDIT QUESTION" /></td>
</tr>
</el:table>
<content:copyright />
</div>
</body>
</html>
