<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> File Library</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="filelibrary">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="20%">TITLE</td>
 <td width="10%">&nbsp;</td>
 <td width="15%">AUTHOR</td>
 <td width="10%">SIZE</td>
<c:choose>
<c:when test="${access.canCreate}">
 <td width="10%"><el:cmdbutton url="userfile" op="edit" label="NEW FILE" /></td>
</c:when>
<c:otherwise>
 <td width="10%">&nbsp;</td>
</c:otherwise>
</c:choose>
 <td>DESCRIPTION</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="entry" items="${files}">
<c:set var="access" value="${accessMap[entry.fileName]}" scope="request" />
<c:set var="author" value="${authors[entry.authorID]}" scope="request" />
<c:set var="authorLoc" value="${userData[entry.authorID]}" scope="request" />
<view:row entry="${entry}">
<c:if test="${access.canEdit}">
 <td class="pri bld"><el:cmd url="userfile" linkID="${doc.fileName}" op="edit">${entry.name}</el:cmd></td>
</c:if>
<c:if test="${!access.canEdit}">
 <td class="pri bld"><el:link url="/usrlibrary/${entry.fileName}">${entry.name}</el:link></td>
</c:if>
 <td class="small bld"><el:link url="/usrlibrary/${entry.fileName}">DOWNLOAD</el:link></td>
 <td><el:profile location="${authorLoc}">${author.name}</el:profile></td>
 <td class="sec bld"><fmt:int value="${entry.size}" /></td>
 <td class="small left" colspan="2"><fmt:text value="${entry.description}" /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar row -->
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
