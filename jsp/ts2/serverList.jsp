<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> TeamSpeak Virtual Servers</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="ts2servers">
<!-- Table Header Bar -->
<tr class="title">
 <td width="20%">NAME</td>
 <td width="5%">PORT</td>
 <td width="5%">USERS</td>
 <td width="25%">ROLES</td>
 <td width="10%"><el:cmdbutton url="ts2server" op="edit" label="NEW SERVER" /></td>
 <td class="left">DESCRIPTION</td>
</tr>

<!-- Table Server Data -->
<c:forEach var="server" items="${viewContext.results}">
<view:row entry="${server}">
 <td><el:cmd url="ts2server" op="edit" linkID="0x${server.ID}" className="pri bld">${server.name}</el:cmd></td>
 <td class="sec bld">${server.port}</td>
 <td><fmt:int value="${server.maxUsers}" /></td>
 <td class="sec"><c:if test="${empty server.roles}">ALL</c:if><fmt:list value="${server.roles}" delim=", " /></td>
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
</body>
</html>
