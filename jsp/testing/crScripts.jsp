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
<title><content:airline /> Check Ride Scripts</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="access" value="${accessMap['NEW']}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="crscripts">
<!-- Table Header Bar -->
<tr class="title">
 <td style="width:10%">AIRCRAFT TYPE</td>
 <td style="width:10%">EQUIPMENT PROGRAM</td>
 <td style="width:65%" class="left">DESCRIPTION</td>
 <td> <c:if test="${access.canCreate}"><el:cmd url="crscript" op="edit">NEW SCRIPT</el:cmd></c:if></td>
</tr>

<!-- Table Script Data -->
<c:forEach var="sc" items="${viewContext.results}">
<view:row entry="${sc}">
<c:set var="access" value="${accessMap[sc.equipmentType]}" scope="page" />
<c:if test="${access.canEdit}">
 <td><el:cmd url="crscript" linkID="${sc.equipmentType}" op="edit">${sc.equipmentType}</el:cmd></td>
</c:if>
<c:if test="${!access.canEdit}">
 <td class="pri bld">${sc.equipmentType}</td>
</c:if>
 <td class="sec bld">${sc.program}</td>
 <td class="small left" colspan="2"><fmt:msg value="${sc.description}" bbCode="true" /></td>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="4">&nbsp;</td>
</tr>
</view:table>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
