<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> TeamSpeak Channels</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="ts2channels">
<!-- Table Header Bar -->
<tr class="title">
 <td width="20%">CHANNEL NAME</td>
 <td width="25%">CHANNEL TOPIC</td>
 <td width="5%">USERS</td>
 <td width="15%"><el:cmdbutton url="ts2channel" op="edit" label="NEW CHANNEL" /></td>
 <td class="left">DESCRIPTION</td>
</tr>

<!-- Table Channel Data -->
<c:set var="serverID" value="${0}" scope="request" />
<c:forEach var="channel" items="${viewContext.results}">
<c:if test="${serverID != channel.serverID}">
<c:set var="server" value="${servers[channel.serverID]}" scope="request" />
<c:set var="serverID" value="${channel.serverID}" scope="request" />
<tr class="title">
 <td colspan="5" class="left caps">${server.name} - PORT ${server.port}</td>
</tr>
</c:if>
<view:row entry="${channel}">
 <td><el:cmd url="ts2channel" op="edit" link="${channel}" className="pri bld">${channel.name}</el:cmd></td>
 <td>${channel.topic}</td>
 <td class="sec bld"><fmt:int value="${channel.maxUsers}" /></td>
 <td colspan="2" class="left small">${channel.description}</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="5"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
