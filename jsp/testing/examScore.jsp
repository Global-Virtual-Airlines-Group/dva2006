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
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form method="POST" action="examscore.do" linkID="0x${exam.ID}" validate="return validate(this)">
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
<!-- Question #${q.number} -->
<tr>
 <td class="label" rowspan="2" valign="top">Question #<fmt:int value="${q.number}" /></td>
 <td class="data">${q.question}</td>
</tr>
<tr>
 <td class="data ${q.exactMatch ? 'warn' : 'sec'}">${q.correctAnswer}</td>
</tr>
<tr>

<!-- Score / Answer -->
 <td class="mid"><input type="checkbox" class="check" name="Score${q.number}" value="1" <c:if test="${fn:correct(q)}">checked="checked"</c:if> />Correct</td>
 <td class="data bld">${q.answer}</td>
</tr>
</c:forEach>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td>&nbsp;
<c:if test="${access.canScore}">
<el:button type="SUBMIT" className="BUTTON" label="SCORE EXAMINATION" />
</c:if>
 </td>
</tr>
</el:table>
</el:form>
<content:copyright />
</div>
</body>
</html>
