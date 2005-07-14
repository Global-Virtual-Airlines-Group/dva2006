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
<title><content:airline /> Document Library</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>
<content:filter roles="Fleet">
<c:set var="isFleetMgr" value="${true}" scope="request" />
</content:filter>

<!-- Main Body Frame -->
<div id="main">
<view:table className="view" pad="default" space="default" cmd="doclibrary">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="25%">TITLE</td>
 <td width="5%">&nbsp;</td>
 <td width="10%">SIZE</td>
 <td width="5%">VERSION</td>
 <td>DESCRIPTION</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="doc" items="${docs}">
<view:row entry="${doc}">
<c:if test="${isFleetMgr}">
 <td class="pri bld"><el:cmd url="libedit" linkID="${doc.fileName}" op="manual">${doc.name}</el:cmd></td>
</c:if>
<c:if test="${!isFleetMgr}">
 <td class="pri bld"><el:link url="/library/${doc.fileName}">${doc.name}</el:link></td>
</c:if>
 <td><el:link url="/library/${doc.fileName}"><el:img src="library/adobe.png" caption="Download PDF manual" x="32" y="32" border="0" /></el:link></td>
 <td class="sec bld"><fmt:int value="${doc.size}" /></td>
 <td class="bld">${doc.version}</td>
 <td class="small left">${doc.description}</td>
</view:row>
</c:forEach>

<!-- Scroll Bar row -->
<tr class="title">
 <td colspan="5">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</div>
</body>
</html>
