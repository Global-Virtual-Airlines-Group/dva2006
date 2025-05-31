<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Document Library</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:googleAnalytics />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %>
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="doclibrary">
<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:25%">TITLE</td>
 <td style="width:5%">&nbsp;</td>
 <td class="nophone" style="width:7%">SIZE</td>
 <td class="nophone" style="width:8%">UPDATED</td>
 <td class="nophone" style="width:5%">VERSION</td>
<c:choose>
<c:when test="${access.canCreate}">
 <td style="width:10%"><el:cmdbutton url="doclib" op="edit" label="NEW MANUAL" /></td>
</c:when>
<c:otherwise>
 <td style="width:10%">&nbsp;</td>
</c:otherwise>
</c:choose>
 <td>DESCRIPTION</td>
</tr>

<!-- Table Data Section -->
<tr class="title caps">
 <td colspan="7" class="left">DOCUMENT LIBRARY - <content:airline /></td>
</tr>
<c:forEach var="doc" items="${docs}">
<c:set var="ac" value="${accessMap[doc]}" scope="page" />
<c:set var="docType" value="${doc.type}" scope="page" />
<c:set var="isValid" value="${doc.file().exists()}" scope="page" />
<view:row entry="${doc}">
<c:if test="${ac.canEdit}">
 <td class="pri bld"><el:cmd url="doclib" linkID="${doc.fileName}" op="edit">${doc.name}</el:cmd></td>
</c:if>
<c:if test="${!ac.canEdit}">
 <td class="pri bld"><el:link url="/library/${doc.fileName}">${doc.name}</el:link></td>
</c:if>
<c:choose>
<c:when test="${!isValid}">
 <td>&nbsp;</td>
</c:when>
<c:when test="${docType.name() == 'XLS'}">
 <td><el:link url="/library/${doc.fileName}"><el:img src="library/excel.png" className="noborder" caption="Download ${docType.description}" x="32" y="32" /></el:link></td>
</c:when>
<c:when test="${docType.name() == 'PDF'}">
 <td><el:link url="/library/${doc.fileName}"><el:img src="library/adobe.png" className="noborder" caption="Download ${docType.description}" x="32" y="32" /></el:link></td>
</c:when>
<c:otherwise>
 <td><el:link url="/library/${doc.fileName}"><el:img src="library/download.png" className="noborder" caption="Download File" x="32" y="32" /></el:link></td>
</c:otherwise>
</c:choose>
 <td class="sec bld nophone"><fmt:fileSize value="${doc.size}" /></td>
 <td class="small nophone"><fmt:date fmt="d" default="N/A" date="${doc.lastModified}" /></td>
 <td class="bld nophone"><fmt:int value="${doc.version}" /></td>
 <td class="small left" colspan="2"><fmt:text value="${doc.description}" /></td>
</view:row>
</c:forEach>

<!-- Download Adobe Acrobat Reader -->
<tr valign="middle">
 <td><a href="https://get.adobe.com/reader/" rel="external" target="_new"><el:img src="library/getacro.png" className="noborder" caption="Download Adobe Acrobat Reader" /></a></td>
 <td colspan="6">All manuals within the <content:airline /> Document Library require <span class="pri bld">Adobe Acrobat Reader</span> in order to be viewed. If you are having difficulties viewing our 
manuals, please click on the link to the left to download the latest version of Adobe Acrobat Reader. This is a free download.</td>
</tr>
<tr class="title">
 <td colspan="7">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
