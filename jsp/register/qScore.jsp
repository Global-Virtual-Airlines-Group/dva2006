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
<title>Questionnaire - ${applicant.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="qscore.do" linkID="0x${exam.ID}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
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
<!-- Question #${q.number} -->
<tr>
 <td class="label" rowspan="2" valign="top">Question #<fmt:int value="${q.number}" /></td>
 <td class="data">${q.question}</td>
</tr>
<tr>
 <td class="data sec">${q.correctAnswer}</td>
</tr>
<tr>

<!-- Score / Answer -->
<c:if test="${access.canScore}">
 <td class="mid"><input type="checkbox" class="check" name="Score${q.number}" value="1" <c:if test="${fn:correct(q)}">checked="checked"</c:if> />Correct</td>
</c:if>
<c:if test="${!access.canScore}">
 <td>&nbsp;</td>
</c:if>
 <td class="data bld">${q.answer}</td>
</tr>
</c:forEach>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td>&nbsp;
<c:if test="${access.canScore}">
 <td><el:button type="SUBMIT" className="BUTTON" label="SCORE QUESTIONNAIRE" /></td>
</c:if>
 </td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
