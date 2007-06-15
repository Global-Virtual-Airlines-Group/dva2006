<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Oceanic Route for <fmt:date fmt="d" date="${route.date}" /></title>
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
<tr class="title caps">
 <td colspan="2">${route.typeName} ROUTES FOR <fmt:date fmt="d" date="${route.date}" /></td>
</tr>
<tr>
 <td class="label">Source</td>
 <td class="data">${route.source}</td>
</tr>
<tr>
 <td colspan="2" class="left">${route.route}</td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td>&nbsp;
<c:if test="${access.canDelete}">
<el:cmdbutton url="routedelete" op="${route.type}" linkID="${fn:dateFmt(route.date, 'MMddyyyy')}" label="DELETE TRACK DATA" />
</c:if>
 </td>
</tr>
</el:table>
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
