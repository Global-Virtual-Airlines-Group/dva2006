<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="forumName" name="airline.forum" />
<html lang="en">
<head>
<title><content:airline /> ${forumName} Channel Administration</title>
<content:css name="main" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/cooler/header.jspf" %> 
<%@ include file="/jsp/cooler/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="channeladmin">
<tr class="title caps">
 <td class="left" colspan="6">${forumName} CHANNELS</td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:30%">CHANNEL NAME / DESCRIPTION</td>
 <td style="width:10%">THREADS</td>
 <td class="nophone" style="max-width:10%">POSTS</td>
 <td class="nophone" style="max-width:15%">AIRLINES</td>
 <td style="width:10%"><el:cmdbutton url="chprofile" op="edit" label="ADD CHANNEL" /></td>
 <td>READ ACCESS ROLES</td>
</tr>

<!-- Table Channel Data -->
<c:forEach var="channel" items="${channels}">
<view:row entry="${channel}">
 <td class="left">
 <el:cmd url="chprofile" linkID="${channel.name}" op="edit" className="pri bld">${channel.name}</el:cmd><br />
 <span class="small">${channel.description}</span></td>
 <td><fmt:int value="${channel.threadCount}" /></td>
 <td class="nophone"><fmt:int value="${channel.postCount}" /></td>
 <td class="sec bld nophone"><fmt:list value="${channel.airlines}" delim=", " /></td>
 <td class="bld right" colspan="2"><fmt:list value="${channel.readRoles}" delim=", " /></td>
</view:row>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="6"><view:legend width="110" labels="Active,Inactive,No New Posts" classes=" ,warn,opt2" /></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
