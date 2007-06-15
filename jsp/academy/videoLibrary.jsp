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
<title><content:airline /> Flight Academy Video Library</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body onload="void initLinks()">
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="tvlibrary">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="25%">TITLE</td>
 <td width="7%">&nbsp;</td>
 <td width="10%">SIZE</td>
<c:choose>
<c:when test="${access.canCreateVideo}">
 <td width="10%"><el:cmdbutton url="tvideo" op="edit" label="NEW VIDEO" /></td>
</c:when>
<c:otherwise>
 <td width="10%">&nbsp;</td>
</c:otherwise>
</c:choose>
 <td>VIDEO DESCRIPTION</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="video" items="${viewContext.results}">
<view:row entry="${video}">
<c:if test="${access.canEditVideo}">
 <td class="pri bld"><el:cmd url="tvideo" linkID="${video.fileName}" op="edit">${video.name}</el:cmd></td>
</c:if>
<c:if test="${!access.canEditVideo}">
 <td class="pri bld"><el:link url="/video/${video.fileName}">${video.name}</el:link></td>
</c:if>
 <td><el:link url="/video/${video.fileName}"><el:img src="library/${video.iconName}.png" caption="Download ${video.typeName} Video" x="32" y="32" border="0" /></el:link></td>
 <td class="sec bld"><fmt:int value="${video.size}" /></td>
 <td class="small left" colspan="2"><fmt:text value="${video.description}" /></td>
</view:row>
</c:forEach>

<!-- Download DiVX -->
<tr valign="middle">
 <td><a href="http://www.divx.com/divx/play/download/" rel="external"><el:img src="library/divx.png" border="0" caption="Download DivX Player" /></a></td>
 <td colspan="4">Some videos within the <content:airline /> Video Library require the 
<span class="pri bld">DiVX Player</span> in order to be viewed. If you are having difficulties viewing 
our videos, please click on the link to the left to download the latest version of the DiVX Player.<br />
This is a free download.</td>
</tr>

<!-- Scroll Bar row -->
<tr class="title">
 <td colspan="5">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
