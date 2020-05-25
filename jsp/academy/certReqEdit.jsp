<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Flight Academy Certification Requirements - ${cert.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script async>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.name, l:10, t:'Certification Name'});
	golgotha.form.submit(f);
	return true;
};

golgotha.onDOMReady(function() { golgotha.form.resizeAll(); });
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="certreqs.do" linkID="${cert.name}" op="save" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">FLIGHT ACADEMY CERTIFICATION REQUIREMENTS - ${cert.name}</td>
</tr>

<!-- Existing Requirements -->
<c:set var="reqNum" value="0" scope="page" />
<c:forEach var="req" items="${cert.requirements}">
<c:set var="reqNum" value="${reqNum + 1}" scope="page" />
<!-- Requirement #<fmt:int value="${reqNum}" /> -->
<tr>
 <td class="label top">Requirement #<fmt:int value="${reqNum}" /></td>
 <td class="data"><el:textbox name="reqText${reqNum}" idx="*" width="80%" height="5" resize="true">${req.text}</el:textbox></td>
</tr>
<c:if test="${!empty cert.examNames}">
<tr>
 <td class="label">Examination</td>
 <td class="data"><el:combo name="reqExam${reqNum}" idx="*" size="1" value="${req.examName}" options="${cert.examNames}" firstEntry="-" /></td>
</tr>
</c:if>
</c:forEach>

<!-- Additional Requirements -->
<c:forEach var="addreqNum" begin="${reqNum + 1}" end="${reqNum + 5}" step="1">
<!-- Requirement #${addreqNum} -->
<tr>
 <td class="label top">Requirement #<fmt:int value="${addreqNum}" /></td>
 <td class="data"><el:textbox name="reqText${addreqNum}" idx="*" width="80%" height="5" resize="true" /></td>
</tr>
<c:if test="${!empty cert.examNames}">
<tr>
 <td class="label">Examination</td>
 <td class="data"><el:combo name="reqExam${addreqNum}" idx="*" size="1" value="-" options="${cert.examNames}" firstEntry="-" /></td>
</tr>
</c:if>
</c:forEach>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="doMore" idx="*" value="true" label="Add More Requirements" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr class="title">
 <td><el:button type="submit" label="SAVE CERTIFICATION REQUIREMENTS" /></td>
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
