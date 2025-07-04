<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Security Roles</title>
<content:css name="main" />
<content:css name="view" />
<content:googleAnalytics />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="roles" name="security.roles" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="view">
<!-- View Header Bar -->
<tr class="title caps">
 <td style="max-width:10%">ID</td>
 <td style="width:20%">PILOT NAME</td>
 <td style="width:15%">RANK</td>
 <td style="width:10%">EQUIPMENT</td>
 <td style="width:25%">E-MAIL</td>
 <td style="width:10%">LEGS</td>
 <td>HOURS</td>
</tr>

<c:forEach var="role" items="${roles}">
<c:set var="members" value="${roleMap[role]}" scope="page" />
<!-- ${role} -->
<tr class="title caps">
 <td colspan="7">SECURITY ROLE - ${role}</td>
</tr>

<c:if test="${members.size() > 0}">
<c:forEach var="pilot" items="${members}">
<view:row entry="${pilot}">
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td class="pri">${pilot.rank.name}</td>
 <td class="bld">${pilot.equipmentType}</td>
 <td class="small"><a href="mailto:${pilot.email}">${pilot.email}</a></td>
 <td><fmt:int value="${pilot.legs}" /></td>
 <td><fmt:dec value="${pilot.hours}" /></td>
</view:row>
</c:forEach>
</c:if>
<c:if test="${members.size() == 0}">
<tr>
 <td class="pri mid bld" colspan="7">NO MEMBERS OF THIS ROLE</td>
</tr>
</c:if>
</c:forEach>

<!-- Button Bar -->
<tr class="title caps">
 <td colspan="7">&nbsp;</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
