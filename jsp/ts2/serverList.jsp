<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<html lang="en">
<head>
<title><content:airline /> TeamSpeak 2 Virtual Servers</title>
<content:css name="main" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="ts2servers">
<!-- Table Header Bar -->
<tr class="title">
 <td style="width:20%">NAME</td>
 <td class="nophone" style="width:5%">PORT</td>
 <td class="nophone" style="width:5%">USERS</td>
 <td class="nophone" style="width:25%">ROLES</td>
 <td style="width:10%"><el:cmdbutton url="ts2server" op="edit" label="NEW SERVER" /></td>
 <td class="left">DESCRIPTION</td>
</tr>

<!-- Table Server Data -->
<c:forEach var="server" items="${viewContext.results}">
<c:set var="accessRoles" value="${server.roles['access']}" scope="page" />
<view:row entry="${server}">
 <td><el:cmd url="ts2server" op="edit" link="${server}" className="pri bld">${server.name}</el:cmd></td>
 <td class="sec bld nophone">${server.port}</td>
 <td class="nophone"><fmt:int value="${server.maxUsers}" /></td>
 <td class="sec nophone"><c:if test="${empty accessRoles}">ALL</c:if><fmt:list value="${accessRoles}" delim=", " /></td>
 <td colspan="2" class="left small">${server.description}</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
