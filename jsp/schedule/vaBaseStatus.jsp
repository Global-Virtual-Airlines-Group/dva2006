<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> VABase Schedule Import Status</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<c:choose>
<c:when test="${isImport && isTailCode}">
<tr class="title">
 <td colspan="2">VABase FLEET IMPORT STATUS</td>
</tr>
<c:if test="${!empty msgs}">
<tr>
 <td class="label top">Import Messages</td>
 <td class="data"><c:forEach var="msg" items="${msgs}">
<fmt:text value="${msg}" /><br /></c:forEach></td>
</tr>
</c:if>
<c:if test="${!empty eqTypes}">
<tr>
 <td class="label top">Invalid Equipment</td>
 <td class="data"><fmt:list value="${eqTypes}" delim=", " /></td>
</tr>
</c:if>
</c:when>
<c:when test="${isImport}">
<tr class="title">
 <td colspan="2">VABase FLIGHT SCHEDULE IMPORT STATUS</td>
</tr>
<c:if test="${!empty msgs}">
<tr>
 <td class="label top">Import Messages</td>
 <td class="data"><c:forEach var="msg" items="${msgs}">
<fmt:text value="${msg}" /><br /></c:forEach></td>
</tr>
</c:if>
<c:if test="${!empty eqTypes}">
<tr>
 <td class="label top">Invalid Equipment</td>
 <td class="data"><fmt:list value="${eqTypes}" delim=", " /></td>
</tr>
</c:if>
<c:if test="${!empty airlines}">
<tr>
 <td class="label top">Invalid Airlines</td>
 <td class="data"><c:forEach var="code" items="${airlines}">
Invalid Airline Code - <el:cmd url="airline" linkID="${code}" op="edit" target="_new" className="bld">${code}</el:cmd><br /></c:forEach></td>
</tr>
</c:if>
<c:if test="${!empty airports}">
<tr>
 <td class="label top">Invalid Airports</td>
 <td class="data"><c:forEach var="code" items="${airports}">
Invalid Airport Code - <el:cmd url="airport" linkID="${code}" op="edit" target="_new" className="bld">${code}</el:cmd><br /></c:forEach></td>
</tr>
</c:if>
</c:when>
<c:when test="${isFilter}">
<tr class="title">
 <td colspan="2">VABase FLIGHT SCHEDULE FILTER STATUS</td>
</tr>
<tr>
 <td colspan="2" class="mid pri bld">Filter complete. <fmt:int value="${entriesLoaded}" /> Flight Schedule entries loaded.</td>
</tr>
</c:when>
</c:choose>
</el:table>
<c:if test="${isImport}">
<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:cmdbutton url="vaimport" label="IMPORT FLIGHT SCHEDULE DATA" /><c:if test="${hasRawSchedule}"> <el:cmdbutton url="vafilter" label="LOAD TODAY'S FLIGHT SCHEDULE" /></c:if></td>
</tr>
</el:table>
</c:if>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
