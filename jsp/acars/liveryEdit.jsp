<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Multi-Player Livery<c:if test="${!empty livery}"> - ${livery.description}</c:if></title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.airline, t:'Airline'});
golgotha.form.validate({f:f.code, l:3, t:'Livery Code'});
golgotha.form.validate({f:f.desc, l:10, t:'Livery Description'});
golgotha.form.submit(f);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="livery.do" method="post" linkID="${empty livery ? '' : livery}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
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
 <td class="data"><el:box name="isDefault" idx="*" value="true" checked="${livery.getDefault()}" label="This is the default livery for the Airline" /></td>
</tr>
<%@ include file="/jsp/auditLog.jspf" %> 
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE LIVERY PROFILE" /><c:if test="${!empty livery}">&nbsp;<el:cmdbutton url="liverydelete" linkID="${livery}" label="DELETE LIVERY" /></c:if></td>
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
