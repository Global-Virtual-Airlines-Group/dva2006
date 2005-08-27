<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Water Cooler Channel Administration</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/cooler/header.jsp" %> 
<%@include file="/jsp/cooler/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<view:table className="view" pad="default" space="default" cmd="staff">
<tr class="title caps">
 <td class="left" colspan="5">WATER COOLER CHANNELS</td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td width="30%">CHANNEL NAME / DESCRIPTION</td>
 <td width="10%">THREADS</td>
 <td width="10%">POSTS</td>
 <td width="15%">AIRLINES</td>
 <td>ROLES</td>
</tr>

<!-- Table Channel Data -->
<c:forEach var="channel" items="${channels}">
<tr>
 <td class="left">
 <el:cmd url="chprofile" linkID="${channel.name}" op="edit" className="bld">${channel.name}</el:cmd><br />
 <span class="small">${channel.description}</span></td>
 <td><fmt:int value="${channel.threadCount}" /></td>
 <td><fmt:int value="${channel.postCount}" /></td>
 <td class="sec bld"><fmt:list value="${channel.airlines}" delim=", " /></td>
 <td class="bld right"><fmt:list value="${channel.roles}" delim=", " /></td>
</tr>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="5">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</div>
</body>
</html>
