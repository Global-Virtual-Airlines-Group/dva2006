<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ACARS Multi-Player Livery<c:if test="${!empty livery}"> - ${livery.description}</c:if></title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.airline, 'Airline')) return false;
if (!validateText(form.code, 3, 'Livery Code')) return false;
if (!validateText(form.desc, 10, 'Livery Description')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="livery.do" method="post" linkID="${empty livery ? '' : livery}" op="save" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> ACARS MULTI-PLAYER LIVERY PROFILE</td>
</tr>
<c:if test="${empty livery}">
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" idx="*" size="1" options="${airlines}" className="bld req" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Livery Code</td>
 <td class="data"><el:text name="code" idx="*" size="8" max="8" className="pri bld req" value="" /></td>
</tr>
</c:if>
<c:if test="${!empty livery}">
<tr>
 <td class="label">Airline</td>
 <td class="data bld">${livery.airline.name}</td>
</tr>
<tr>
 <td class="label">Livery Code</td>
 <td class="pri bld">${livery.code}</td>
</tr>
</c:if>
<tr>
 <td class="label">Description</td>
 <td class="data"><el:text name="desc" idx="*" size="64" max="80" className="req" value="${livery.description}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isDefault" idx="*" value="true" checked="${livery.default}" label="This is the default livery for the Airline" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE LIVERY PROFILE" />
<c:if test="${!empty livery}"> <el:cmdbutton url="liverydelete" linkID="${livery}" label="DELETE LIVERY" /></c:if></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
