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
<title><content:airline /> Security Roles</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="roles" name="security.roles" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="view" space="default" pad="default">
<!-- View Header Bar -->
<tr class="title caps">
 <td width="10%">ID</td>
 <td width="20%">PILOT NAME</td>
 <td width="15%">RANK</td>
 <td width="10%">EQUIPMENT</td>
 <td width="25%">E-MAIL</td>
 <td width="10%">LEGS</td>
 <td>HOURS</td>
</tr>

<c:forEach var="role" items="${roles}">
<c:set var="members" value="${roleMap[role]}" scope="request" />
<!-- ${role} -->
<tr class="title caps">
 <td colspan="7">SECURITY ROLE - ${role}</td>
</tr>

<c:if test="${fn:sizeof(members) > 0}">
<c:forEach var="pilot" items="${members}">
<view:row entry="${pilot}">
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td class="pri">${pilot.rank}</td>
 <td class="bld">${pilot.equipmentType}</td>
 <td class="small"><a href="mailto:${pilot.email}">${pilot.email}</a></td>
 <td><fmt:int value="${pilot.legs}" /></td>
 <td><fmt:dec value="${pilot.hours}" /></td>
</view:row>
</c:forEach>
</c:if>
<c:if test="${fn:sizeof(members) == 0}">
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
<content:googleAnalytics />
</body>
</html>
