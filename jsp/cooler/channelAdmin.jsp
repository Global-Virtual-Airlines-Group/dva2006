<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="forumName" name="airline.forum" />
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ${forumName} Channel Administration</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/cooler/header.jspf" %> 
<%@ include file="/jsp/cooler/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="channeladmin">
<tr class="title caps">
 <td class="left" colspan="6">${forumName} CHANNELS</td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td width="30%">CHANNEL NAME / DESCRIPTION</td>
 <td width="10%">THREADS</td>
 <td width="10%">POSTS</td>
 <td width="10%">AIRLINES</td>
 <td width="10%"><el:cmdbutton url="chprofile" op="edit" label="ADD CHANNEL" /></td>
 <td>READ ACCESS ROLES</td>
</tr>

<!-- Table Channel Data -->
<c:forEach var="channel" items="${channels}">
<view:row entry="${channel}">
 <td class="left">
 <el:cmd url="chprofile" linkID="${channel.name}" op="edit" className="pri bld">${channel.name}</el:cmd><br />
 <span class="small">${channel.description}</span></td>
 <td><fmt:int value="${channel.threadCount}" /></td>
 <td><fmt:int value="${channel.postCount}" /></td>
 <td class="sec bld"><fmt:list value="${channel.airlines}" delim=", " /></td>
 <td class="bld right" colspan="2"><fmt:list value="${channel.readRoles}" delim=", " /></td>
</view:row>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="6"><view:legend width="90" labels="Active,Inactive" classes=" ,warn" /></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
