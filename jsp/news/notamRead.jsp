<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> NOTAM</title>
<content:expire expires="3600" />
<content:css name="main" />
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
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> NOTICE TO AIRMEN</td>
</tr>
<tr>
 <td class="label">Notice Title</td>
 <td class="data pri bld">${entry.subject}</td>
</tr>
<tr>
 <td class="label">Notice Date</td>
 <td class="data"><fmt:date fmt="d" date="${entry.date}" /></td>
</tr>
<c:if test="${entry.active}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data ter bld caps">Notice to Airmen is currently In Effect</td>
</tr>
</c:if>
<tr>
 <td class="label top">Entry Text</td>
 <td class="data"><c:if test="${entry.isHTML}">${entry.body}</c:if>
<c:if test="${!entry.isHTML}"><fmt:text value="${entry.body}" /></c:if></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;
<c:if test="${access.canDelete}">
<el:cmdbutton url="newsdelete" op="NOTAM" link="${entry}" label="DELETE NOTAM" />
</c:if>
 </td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
