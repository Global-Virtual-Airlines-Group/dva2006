<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
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
<view:table className="view" cmd="staff">
<tr class="title">
 <td colspan="5" class="left caps"><content:airline /> STAFF ROSTER</td>
</tr>

<!-- Table Pilot Data -->
<c:set var="areaName" value="" scope="page" />
<c:forEach var="staff" items="${staffRoster}">
<c:set var="hasBlog" value="${fn:contains(blogIDs, staff.ID)}" scope="page" />
<c:if test="${(hasAreas && (areaName != staff.area))}">
<c:set var="areaName" value="${staff.area}" scope="page" />
<tr class="title">
 <td colspan="4" class="left caps"><fmt:text value="${areaName}" /></td>
</tr>
</c:if>
<tr>
 <td class="bld" width="25%"><span class="pri">${staff.name}</span><br />
<span class="sec small"><fmt:text value="${staff.title}" /></span></td>
 <td width="15%"><el:link className="small" url="mailto:${staff.email}">${staff.email}</el:link></td>
<c:if test="${hasBlog}">
 <td><el:cmd url="blog" link="${staff}" className="ter bld small">BLOG</el:cmd></td>
 <td class="small left"><fmt:text value="${staff.body}" /></td>
</c:if>
<c:if test="${!hasBlog}">
 <td colspan="2" class="small left"><fmt:text value="${staff.body}" /></td>
</c:if>
</tr>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="5">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
