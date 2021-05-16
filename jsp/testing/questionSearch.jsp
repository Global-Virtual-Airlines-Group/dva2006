<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Examination Question Search</title>
<content:css name="main" />
<content:css name="view" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.searchStr, l:4, t:'Search Term'});
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
<el:form action="qpsearch.do" op="save" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Title Bar -->
<tr class="title caps">
 <td colspan="2"><span class="nophone">EXAMINATION </span>QUESTION PROFILE SEARCH</td>
</tr>
<tr>
 <td class='label'>Search Term</td>
 <td class="data"><el:text name="searchStr" idx="*" required="true" size="32" max="40" value="${param.searchStr}" /></td>
</tr>

<!-- Button Bar -->
<tr class="title">
 <td colspan="2" class="mid"><el:button type="submit" label="SEARCH QUESTION PROFILES" /></td>
</tr>
</el:table>
<c:if test="${doSearch}">
<br />
<!-- Search Results -->
<el:table className="view">
<tr class="title caps">
 <td colspan="5" class="left"><span class="nophone">EXAMINATION QUESTION PROFILE </span> SEARCH RESULTS</td>
</tr>
<c:choose>
<c:when test="${results.size() > 0}">
<!-- Table Header Bar -->
<tr class="title">
 <td style="width:6%">&nbsp;</td>
 <td style="width:12%">CORRECT / ASKED</td>
 <td style="width:6%">&nbsp;</td>
 <td style="width:7%">&nbsp;</td>
 <td class="right">QUESTION TEXT</td>
</tr>
<!-- Table Question data -->
<c:forEach var="q" items="${results}">
<tr>
<td><el:cmd className="pri bld" url="qprofile" link="${q}">VIEW</el:cmd></td>
 <td><fmt:int value="${q.passCount}" /> / <fmt:int value="${q.total}" /></td>
 <td><c:if test="${q.total > 0}"><fmt:dec value="${q.passCount * 100 / q.total}" fmt="##0.0" />%</c:if><c:if test="${q.total == 0}">-</c:if></td>
 <td>&nbsp;<c:if test="${fn:isMultiChoice(q)}"><el:img src="testing/multiChoice.png" caption="Multiple Choice" /></c:if>
<c:if test="${q.size > 0}"><el:img src="testing/image.png" caption="Image Resource" /></c:if></td>
 <td class="left small" colspan="2"><fmt:text value="${q.question}" /></td>
</tr>
</c:forEach>
</c:when>
<c:otherwise>
<tr>
 <td colspan="6" class="pri bld mid caps">No Examination Questions meeting your search criteria were found.</td> 
</tr>
</c:otherwise>
</c:choose>
</el:table>
</c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
