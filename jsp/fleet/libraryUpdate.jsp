<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Fleet/Document Library Updated</title>
<content:css name="main" browserSpecific="true" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<br />
<c:if test="${installerCreated}">
<!-- Fleet Library entry created -->
<div class="updateHdr">Fleet Library Installler Added</div>
<br />
This Installer has been succesfully added to the <content:airline /> Fleet Library.<br />
<br />
To perform further maintenance on the Fleet Library, <el:cmd url="fleetlibrary" op="admin">Click Here</el:cmd>.<br />
To view the Fleet Library, <el:cmd url="fleetlibrary">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${installerUpdated}">
<!-- Fleet Library entry updated -->
<div class="updateHdr">Fleet Library Installler Updated</div>
<br />
This <content:airline /> Fleet Library Installer has been successfully updated.<br />
<br />
To perform further maintenance on the Fleet Library, <el:cmd url="fleetlibrary" op="admin">Click Here</el:cmd>.<br />
To view the Fleet Library, <el:cmd url="fleetlibrary">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${manualCreated}">
<!-- Document Library entry created -->
<div class="updateHdr">Document Library Manual Added</div>
<br />
This Manual has been succesfully added to the <content:airline /> Document Library.<br />
<br />
To return to the Document Library, <el:cmd url="doclibrary">Click Here</el:cmd>.
</c:if>
<c:if test="${manualUpdated}">
<!-- Document Library entry updated -->
<div class="updateHdr">Document Library Manual Updated</div>
<br />
This <content:airline /> Document Library entry has been successfully updated.<br />
<br />
To return to the Document Library, <el:cmd url="doclibrary">Click Here</el:cmd>.
</c:if>
<br />
<content:copyright />
</div>
</body>
</html>
