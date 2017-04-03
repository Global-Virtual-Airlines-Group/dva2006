<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Staff Roster</title>
<content:css name="main" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="staff">
<tr class="title">
 <td colspan="3" class="left caps"><content:airline /> STAFF ROSTER</td>
</tr>

<!-- Table Pilot Data -->
<c:set var="areaName" value="" scope="page" />
<c:forEach var="staff" items="${staffRoster}">
<c:if test="${(hasAreas && (areaName != staff.area))}">
<c:set var="areaName" value="${staff.area}" scope="page" />
<tr class="title">
 <td colspan="3" class="left caps"><fmt:text value="${areaName}" /></td>
</tr>
</c:if>
<tr>
 <td class="bld" style="width:25%; min-width:90px;"><span class="pri">${staff.name}</span><br />
<span class="sec small"><fmt:text value="${staff.title}" /></span></td>
 <td class="nophone"><el:link className="small" url="mailto:${staff.email}">${staff.email}</el:link></td>
 <td class="small left"><fmt:text value="${staff.body}" /></td>
</tr>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="3">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
