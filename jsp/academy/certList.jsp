<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Flight Academy Certifications</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="view">
<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:15%">CERTIFICATION</td>
 <td style="width:5%">CODE</td>
 <td style="width:5%">STAGE</td>
 <td style="width:14%">PREREQUISITES</td>
 <td style="width:8%">REQUIREMENTS</td>
 <td style="width:10%">AIRLINES</td>
 <td class="left">EXAMINATIONS</td>
</tr>

<!-- Table View data -->
<c:forEach var="cert" items="${certs}">
<view:row entry="${cert}">
 <td><el:cmd url="cert" linkID="${cert.name}" className="pri bld">${cert.name}</el:cmd></td>
 <td class="sec bld caps">${cert.code}</td>
 <td class="bld"><fmt:int value="${cert.stage}" /></td>
 <td class="sec bld">${cert.reqs.description}</td>
 <td><fmt:int value="${cert.reqCount}" /></td>
 <td class="sec small"><c:forEach var="al" items="${cert.airlines}" varStatus="alNext">${al.name}<c:if test="${!alNext.last}"><br /></c:if></c:forEach></td>
 <td class="left"><fmt:list value="${cert.examNames}" delim=", " empty="NONE" /></td>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title caps">
 <td colspan="7"> <c:if test="${access.canCreate}"><el:cmd url="cert" op="edit">NEW CERTIFICATION</el:cmd></c:if></td>
</tr>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
