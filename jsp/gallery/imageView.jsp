<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>${fn:escape(img.name)}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!form.score) return false;
if (!validateCombo(form.score, 'Feedback Rating')) return false;

setSubmit();
disableButton('VoteButton');
disableButton('EditButton');
disableButton('DeleteButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/gallery/header.jspf" %> 
<%@ include file="/jsp/gallery/sideMenu.jspf" %>
<content:sysdata var="db" name="airline.db" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="imgvote.do" linkID="0x${img.ID}" method="POST" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2" class="left">${img.name}</td>
</tr>
<tr>
 <td class="label">Created by</td>
 <td class="data"><el:cmd className="pri bld" url="profile" linkID="0x${author.ID}">${author.name}</el:cmd>
 on <fmt:date fmt="d" date="${img.createdOn}" /></td>
</tr>
<tr>
 <td class="label">Image Description</td>
 <td class="data">${img.description}</td>
</tr>
<tr>
 <td class="label">Feedback Rating</td>
<c:if test="${empty img.votes}">
 <td class="data sec bld caps">No Feedback has been given for this Image</td>
</c:if>
<c:if test="${!empty img.votes}">
 <td class="data pri bld caps"><fmt:int value="${img.voteCount}" /> ratings, Average:
<fmt:dec value="${img.score}" /></td>
</c:if>
</tr>
<tr class="mid">
 <td colspan="2"><img width="${img.width}" height="${img.height}" src="/gallery/${db}/0x<fmt:hex value="${thread.image}" />.${fn:lower(img.typeName)}" alt="${fn:escape(img.name)}, ${img.width}x${img.height} (<fmt:int value="${img.size}" /> bytes)" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="form" space="default" pad="default">
<tr class="title mid">
 <td>&nbsp;
<c:if test="${access.canVote}">
FEEDBACK <el:combo name="score" idx="*" size="1" options="${scores}" firstEntry="-" />&nbsp;
<el:button ID="VoteButton" type="submit" className="BUTTON" label="SUBMIT FEEDBACK" />
</c:if>
<c:if test="${access.canEdit}">
<el:cmdbutton ID="EditButton" url="image" linkID="0x${img.ID}" op="edit" label="EDIT IMAGE" />
</c:if>
<c:if test="${access.canDelete}">
<el:cmdbutton ID="DeleteButton" url="imgdelete" linkID="0x${img.ID}" label="DELETE IMAGE" />
</c:if>
 </td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
