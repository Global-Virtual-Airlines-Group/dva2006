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
<title><content:airline /> Staff Roster</title>
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
<view:table className="view" pad="default" space="default" cmd="staff">
<tr class="title">
 <td colspan="4" class="left caps"><content:airline /> STAFF ROSTER</td>
</tr>

<!-- Table Pilot Data -->
<c:set var="areaName" value="" scope="request" />
<c:forEach var="staff" items="${staffRoster}">
<c:if test="${(hasAreas && (areaName != staff.area))}">
<c:set var="areaName" value="${staff.area}" scope="request" />
<tr class="title">
 <td colspan="4" class="left caps"><fmt:text value="${areaName}" /></td>
</tr>
</c:if>
<tr>
 <td class="pri bld" width="15%">${staff.firstName} ${staff.lastName}</td>
 <td class="sec bld" width="15%"><fmt:text value="${staff.title}" /></td>
 <td width="20%"><el:link className="small" url="mailto:${staff.EMail}">${staff.EMail}</el:link></td>
 <td class="small left"><fmt:text value="${staff.body}" /></td>
</tr>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="4">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
