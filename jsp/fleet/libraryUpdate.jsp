<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ${library} Library Updated</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:if test="${!isDelete}">
<!-- ${library} Library entry Created/Updated -->
<div class="updateHdr">${library} Library Entry ${fileAdded? 'Added' : 'Updated'}</div>
<br />
This <content:airline /> ${library} Library entry has been successfully ${fileAdded? 'added' : 'updated'}.<br />
</c:if>
<c:if test="${isDelete}">
<!-- ${library} Library entry Deleted -->
<div class="updateHdr">${library} Entry Deleted</div>
<br />
The <content:airline /> ${library} Library entry "${entry.name}" has been deleted from the database.<br />
</c:if>
<br />
To return to the ${library} Library, <el:cmd op="${libraryop}" url="${librarycmd}" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
