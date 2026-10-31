<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Share Price<c:if test="${!empty sp}"> - ${sp.year}</c:if></title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:js name="common" />
<content:googleAnalytics />
<content:newRelic>
<content:cspHeader />
</content:newRelic>
<script nonce="${contentSecurity.nonce}">
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	
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
<el:form action="shareprice.do" method="post" linkID="${sp.year}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline />&nbsp;</span>SHARE INFORMATION</td>
</tr>
<tr>
 <td class="label">Year</td>
 <td class="data"><el:text name="year" className="pri bld" required="true" idx="*" size="4" max="4" value="${sp.year}" /></td>
</tr>
<tr>
 <td class="label">Share Price</td>
 <td class="data"><el:text name="price" className="sec bld" required="true" idx="*" size="5" max="6" value="${sp.price / 100}" /></td>
 </tr>
<tr>
 <td class="label">Total Shares</td>
 <td class="data"><el:text name="count" className="bld" required="true" idx="*" size="4" max="5" value="${sp.count}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="active" value="true" checked="${sp.active}" label="Fundraising is currently Active" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE SHARE INFORMATION" /></td>
</tr>
</el:table>
</el:form>
<br>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
