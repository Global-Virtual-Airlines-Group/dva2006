<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><fmt:text value="${img.name}" /></title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:js name="imgLike" />
</head>
<content:copyright visible="false" />
<body onload="void golgotha.like.get(${img.hexID})">
<content:page>
<%@ include file="/jsp/gallery/header.jspf" %> 
<%@ include file="/jsp/gallery/sideMenu.jspf" %>
<content:sysdata var="db" name="airline.db" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="2" class="left">${img.name}</td>
</tr>
<tr>
 <td class="label">Created by</td>
 <td class="data"><el:cmd className="pri bld" url="profile" link="${author}">${author.name}</el:cmd>
 on <fmt:date fmt="d" date="${img.createdOn}" /></td>
</tr>
<tr>
 <td class="label">Image Description</td>
 <td class="data"><fmt:text value="${img.description}" /></td>
</tr>
<tr>
 <td class="label">Feedback</td>
 <td class="data"><span class="small" id="imgLikeTotal"></span><span class="small" id="imgLike"> <a onclick="javascript:void golgotha.like.exec(${img.hexID})">Like this Image</a></span></td>
</tr>
<tr class="mid">
 <td colspan="2"><img class="gallery" height="${img.height}" style="max-width:98%; width:${img.width}px;" src="/gallery/${db}/${img.hexID}.${img.typeName}" alt="${fn:escape(img.name)}, ${img.width}x${img.height} (<fmt:int value="${img.size}" /> bytes)" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="form">
<tr class="title mid">
 <td>&nbsp;
<c:if test="${access.canEdit}">
<el:cmdbutton ID="EditButton" url="image" link="${img}" op="edit" label="EDIT IMAGE" />
</c:if>
<c:if test="${access.canDelete}">
<el:cmdbutton ID="DeleteButton" url="imgdelete" link="${img}" label="DELETE IMAGE" />
</c:if>
 </td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
