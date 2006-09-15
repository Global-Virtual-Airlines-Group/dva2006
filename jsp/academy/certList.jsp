<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Academy Certifications</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="view" space="default" pad="default">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="15%">CERTIFICATION</td>
 <td width="5%">CODE</td>
 <td width="8%">STAGE</td>
 <td width="17%">PREREQUISITES</td>
 <td width="10%">REQUIREMENTS</td>
 <td>EXAMINATIONS</td>
</tr>

<!-- Table View data -->
<c:forEach var="cert" items="${certs}">
<view:row entry="${cert}">
 <td><el:cmd url="cert" linkID="${cert.name}" className="pri bld">${cert.name}</el:cmd></td>
 <td class="sec bld caps">${cert.code}</td>
 <td class="bld"><fmt:int value="${cert.stage}" /></td>
 <td class="sec bld">${cert.reqName}</td>
 <td><fmt:int value="${cert.reqCount}" /></td>
 <td class="left"><fmt:list value="${cert.examNames}" delim=", " /></td>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title caps">
 <td colspan="6"> <c:if test="${access.canCreate}"><el:cmd url="cert" op="edit">NEW CERTIFICATION</el:cmd></c:if></td>
</tr>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
