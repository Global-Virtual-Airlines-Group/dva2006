<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Document Library</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body onload="void initLinks()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:filter roles="Fleet"><c:set var="isFleetMgr" value="${true}" scope="request" /></content:filter>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="doclibrary">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="25%">TITLE</td>
 <td width="5%">&nbsp;</td>
 <td width="10%">SIZE</td>
 <td width="5%">VERSION</td>
<c:choose>
<c:when test="${access.canCreate}">
 <td width="10%"><el:cmdbutton url="doclib" op="edit" label="NEW MANUAL" /></td>
</c:when>
<c:otherwise>
 <td width="10%">&nbsp;</td>
</c:otherwise>
</c:choose>
 <td>DESCRIPTION</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="airline" items="${fn:keys(docs)}">
<tr class="title caps">
 <td colspan="6" class="left">${airline.name}</td>
</tr>
<c:forEach var="doc" items="${docs[airline]}">
<view:row entry="${doc}">
<c:if test="${isFleetMgr}">
 <td class="pri bld"><el:cmd url="doclib" linkID="${doc.fileName}" op="edit">${doc.name}</el:cmd></td>
</c:if>
<c:if test="${!isFleetMgr}">
 <td class="pri bld"><el:link url="/library/${doc.fileName}">${doc.name}</el:link></td>
</c:if>
 <td><el:link url="/library/${doc.fileName}"><el:img src="library/adobe.png" caption="Download PDF manual" x="32" y="32" border="0" /></el:link></td>
 <td class="sec bld"><fmt:int value="${doc.size}" /></td>
 <td class="bld">${doc.version}</td>
 <td class="small left" colspan="2"><fmt:text value="${doc.description}" /></td>
</view:row>
</c:forEach>
</c:forEach>

<!-- Download Adobe Acrobat Reader -->
<tr valign="middle">
 <td><a href="http://www.adobe.com/products/acrobat/readstep2.html" rel="external"><el:img src="library/getacro.png" border="0" caption="Download Adobe Acrobat Reader" /></a></td>
 <td colspan="5">All manuals within the <content:airline /> Document Library require <span class="pri bld">Adobe 
Acrobat Reader 6</span> or newer in order to be viewed. If you are having difficulties viewing our 
manuals, please click on the link to the left to download the latest version of Adobe Acrobat Reader.<br /> 
This is a free download.</td>
</tr>

<!-- Scroll Bar row -->
<tr class="title">
 <td colspan="6">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
