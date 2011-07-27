<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Academy Check Ride Scripts</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>
<c:set var="ac" value="${access['NEW']}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" cmd="crscripts">
<!-- Table Header Bar -->
<tr class="title">
 <td width="15%">CERTIFICATION</td>
 <td width="65%" class="left">CHECK RIDE DESCRIPTION</td>
 <td> <c:if test="${ac.canCreate}"><el:cmd url="arscript" op="edit">NEW SCRIPT</el:cmd></c:if></td>
</tr>

<!-- Table Script Data -->
<c:forEach var="sc" items="${scripts}">
<view:row entry="${sc}">
<c:set var="ac" value="${access[sc.certificationName]}" scope="page" />
<c:if test="${ac.canEdit}">
 <td><el:cmd url="arscript" linkID="${sc.certificationName}" className="pri bld" op="edit">${sc.certificationName}</el:cmd></td>
</c:if>
<c:if test="${!ac.canEdit}">
 <td class="pri bld">${sc.certificationName}</td>
</c:if>
 <td class="small left" colspan="2"><fmt:msg value="${sc.description}" bbCode="true" /></td>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="3">&nbsp;</td>
</tr>
</view:table>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
