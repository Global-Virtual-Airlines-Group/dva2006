<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Partner Information<c:if test="${!empty partner}"> - ${partner.name}</c:if></title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:googleAnalytics />
<content:js name="common" />
<content:cspHeader />
<script async>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.name, l:4, t:'Partner Name'});
	golgotha.form.validate({f:f.desc, l:15, t:'Partner Description'});
	golgotha.form.validate({f:f.priority, min:0, t:'Partner Sort Order'});
	golgotha.form.validate({f:f.img, ext:['jpg','png','gif'], t:'Banner Image', empty:true, maxSize:512});
	golgotha.form.submit(f);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="partner.do" op="save" method="post" link="${partner}" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> PARTNER INFORMATION</td>
</tr>
<tr>
 <td class="label">Partner Name</td>
 <td class="data"><el:text name="name" className="pri bld" required="true" idx="*" size="48" max="48" value="${partner.name}" /></td>
</tr>
<tr>
 <td class="label">Partner URL</td>
 <td class="data"><el:text name="url" required="true" idx="*" size="64" max="144" value="${partner.URL}" /></td>
</tr>
<tr>
 <td class="label">Sort Order</td>
 <td class="data"><el:text name="priority" required="true" size="1" max="2" value="${partner.priority}" />&nbsp;<span class="small ita">(Higher values will be placed higher in the list.)</span></td>
</tr>
<tr>
 <td class="label top">Banner Image</td>
 <td class="data"><el:file name="img" idx="*" className="small"  size="80" max="144" /><c:if test="${partner.hasImage}"><br />
<el:box name="deleteImg" value="true" idx="*" label="Delete Banner Image" /></c:if></td>
</tr>
<c:if test="${partner.referCount > 0}">
<tr>
 <td class="label">Referrals</td>
 <td class="data"><fmt:int value="${partner.referCount}" className="sec bld" /> referrals, last on <fmt:date date="${partner.lastRefer}" t="HH:mm" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Description</td>
 <td class="data"><el:textbox name="desc" required="true" width="90%" height="3" resize="true">${partner.description}</el:textbox></td> 
</tr>
<%@ include file="/jsp/auditLog.jspf" %>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE PARTNER INFORMATION" /><c:if test="${access.canDelete}">&nbsp;<el:cmdbutton url="partnerdelete" link="${partner}" label="DELETE PARTNER" /></c:if></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
