<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> System News Entry</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
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
 <td colspan="2"><content:airline /> SYSTEM NEWS ENTRY</td>
</tr>
<tr>
 <td class="label">Entry Title</td>
 <td class="data pri bld"><fmt:text value="${entry.subject}" /></td>
</tr>
<tr>
 <td class="label">Entry Date</td>
 <td class="data"><fmt:date fmt="d" date="${entry.date}" /></td>
</tr>
<tr>
 <td class="label top">Entry Text</td>
 <td class="data"><fmt:msg value="${entry.body}" bbCode="true" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;
<c:if test="${access.canDelete}">
<el:cmdbutton url="newsdelete" link="${entry}" label="DELETE SYSTEM NEWS ENTRY" />
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
