<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Flight Tours</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="tours">
<!-- Table Header Bar-->
<tr class="title caps">
<c:if test="${access.canCreate}">
<td style="width:10%">NAME</td>
<td style="width:20%"><el:cmdbutton url="tour" op="edit" label="NEW TOUR" /></td>
</c:if>
<c:if test="${!access.canCreate}">
 <td colspan="2" style="width:30%;">NAME</td>
</c:if>
<td style="width:10%">START DATE</td>
<td style="width:10%">END DATE</td>
<td style="width:20%">NETWORKS</td>
<td style="width:10%">&nbsp;</td>
<td>FLIGHTS</td>
</tr>

<!-- Table Tour Data -->
<c:forEach var="t" items="${viewContext.results}">
<c:set var="ac" value="${accessMap[t]}" scope="page" />
<view:row entry="${t}">
<c:if test="${ac.canEdit}">
 <td class="pri bld" colspan="2"><el:cmd url="tour" link="${t}">${t.name}</el:cmd></td>
</c:if>
<c:if test="${!ac.canEdit}">
 <td class="pri bld" colspan="2">${t.name}</td>
</c:if>
 <td><fmt:date date="${t.startDate}" fmt="d" /></td>
 <td><fmt:date date="${t.endDate}" fmt="d" /></td>
<c:if test="${empty t.networks}">
 <td class="sec bld">OFFLINE</td>
</c:if>
<c:if test="${!empty t.networks}">
 <td class="sec small bld"><fmt:list value="${t.networks}" delim=", " /></td>
</c:if>
 <td class="small pri bld">${t.ACARSOnly ? 'ACARS Only' : "-"}</td>
 <td><fmt:int value="${t.flightCount}" /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7">&nbsp;<view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
