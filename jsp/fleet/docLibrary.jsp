<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Document Library</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
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
<c:forEach var="airline" items="${fn:keys(docs)}">
<tr class="title caps">
 <td colspan="7" class="left">DOCUMENT LIBRARY - ${airline.name}</td>
</tr>
<c:forEach var="doc" items="${docs[airline]}">
<c:set var="ac" value="${accessMap[doc]}" scope="page" />
<view:row entry="${doc}">
<c:if test="${ac.canEdit}">
 <td class="pri bld"><el:cmd url="doclib" linkID="${doc.fileName}" op="edit">${doc.name}</el:cmd></td>
</c:if>
<c:if test="${!ac.canEdit}">
 <td class="pri bld"><el:link url="/library/${doc.fileName}">${doc.name}</el:link></td>
</c:if>
 <td><el:link url="/library/${doc.fileName}"><el:img src="library/adobe.png" className="noborder" caption="Download PDF manual" x="36" y="36" /></el:link></td>
 <td class="sec bld nophone"><fmt:int value="${doc.size / 1024}" />K</td>
 <td class="small nophone"><fmt:date fmt="d" default="N/A" date="${doc.lastModified}" /></td>
 <td class="bld nophone"><fmt:int value="${doc.version}" /></td>
 <td class="small left" colspan="2"><fmt:text value="${doc.description}" /></td>
</view:row>
</c:forEach>
</c:forEach>
<!-- Download Adobe Acrobat Reader -->
<tr valign="middle">
 <td><a href="http://www.adobe.com/products/acrobat/readstep2.html" rel="external" target="_new"><el:img src="library/getacro.png" className="noborder" caption="Download Adobe Acrobat Reader" /></a></td>
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
<content:googleAnalytics />
</body>
</html>
