<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> ${library} Library Updated</title>
<content:css name="main" />
<content:js name="common" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
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
<c:if test="${fileExisted}">
The file ${entry.fileName} was delete from the file system.<br /></c:if>
</c:if>
<br />
To return to the ${library} Library, <el:cmd op="${libraryop}" url="${librarycmd}" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
