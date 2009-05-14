<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
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
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form" pad="default" space="default">
<!-- Exam Title Bar -->
<tr class="title caps">
 <td colspan="2">I - ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Taken on</td>
 <td class="data"><fmt:date date="${exam.date}" /></td>
</tr>
<c:if test="${!empty exam.submittedOn}">
<tr>
 <td class="label">Submitted on</td>
 <td class="data"><fmt:date date="${exam.submittedOn}" />
<c:if test="${exam.submittedOn > exam.expiryDate}"><span class="error">${(exam.submittedOn.time - exam.expiryDate.time) / 60000}
 minutes late</span></c:if></td>
</tr>
</c:if>

<!-- Exam Questions -->
<c:set var="qnum" value="0" scope="page" />
<c:forEach var="q" items="${exam.questions}">
<c:set var="qnum" value="${qnum + 1}" scope="page" />
<!-- Question #${qnum} -->
<tr>
 <td class="label">Question #${qnum}</td>
 <td class="data">${q.question}<div class="sec small">${q.correctAnswer}</div></td>
</tr>
<tr>

<!-- Score / Answer -->
<c:choose>
<c:when test="${fn:correct(q)}">
 <td class="mid"><el:img caption="Correct" border="0" src="testing/pass.png" /></td>
</c:when>
<c:when test="${fn:incorrect(exam, q)}">
 <td class="mid"><el:img caption="Incorrect" border="0" src="testing/fail.png" /></td>
</c:when>
<c:otherwise>
 <td class="mid">&nbsp;</td>
</c:otherwise>
</c:choose>
 <td class="data">${q.answer}</td>
</tr>
</c:forEach>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td>&nbsp;</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
