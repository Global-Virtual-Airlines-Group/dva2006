<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Users</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:table className="view" pad="default" space="default">
<tr class="title">
 <td colspan="7" class="left">CURRENTLY LOGGED IN USERS</td>
</tr>

<!-- Pilot Title Bar -->
<tr class="title caps">
 <td width="10%">PILOT ID</td>
 <td width="17%">PILOT NAME</td>
 <td width="12%">RANK</td>
 <td width="12%">EQUIPMENT TYPE</td>
 <td width="19%">LOCATION</td>
 <td>JOINED ON</td>
</tr>

<!-- Pilot Data Bar -->
<c:forEach var="pilot" items="${pilots}">
<tr>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td class="bld"><el:cmd url="profile" linkID="0x${pilot.ID}">${pilot.name}</el:cmd></td>
 <td class="pri">${pilot.rank}</td>
 <td class="sec">${pilot.equipmentType}</td>
 <td>${pilot.location}</td>
 <td class="small"><fmt:date date="${pilot.createdOn}" fmt="d" d="EEEE MMMM dd, yyyy" /></td>
</tr>
<content:filter roles="Admin,HR">
<tr>
 <td colspan="6">Logged in since <fmt:date date="${pilot.lastLogin}" />, from ${pilot.loginHost}.</td>
</tr>
</content:filter>
</c:forEach>
<c:if test="${empty pilots}">
<tr>
 <td colspan="7" class="pri bld">NO CURRENTLY LOGGED IN WEB SITE USERS</td>
</tr>
</c:if>
<tr class="title">
 <td colspan="7">&nbsp;</td>
</tr>
</el:table>
</div>
</body>
</html>
