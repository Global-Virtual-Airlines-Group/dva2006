<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title>LLM Prompt - ${(empty prompt) ? 'NEW' : prompt.ID}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:googleAnalytics />
<content:js name="common" />
<content:newRelic>
<content:cspHeader />
</content:newRelic>
<script nonce="${contentSecurity.nonce}">
golgotha.local.validate = function(f) {
    if (!golgotha.form.check()) return false;
    golgotha.form.validate({f:f.id, l:4, t:'Prompt ID'});
    golgotha.form.validate({f:f.desc, l:10, t:'Prompt Description'});
    golgotha.form.validate({f:f.user, l:15, t:'User Prompt'});
    golgotha.form.validate({f:f.system, l:15, t:'System Prompt'});
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
<el:form action="prompt.do" method="post" linkID="${prompt.ID}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">LLM PROMPT - ${(empty prompt) ? 'NEW PROMPT' : prompt.ID}</td>
</tr>
<c:if test="${empty prompt}">
<tr>
 <td class="label">Prompt ID</td>
 <td class="data"><el:text name="id" className="pri bld caps" size="20" max="24" required="true" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Description</td>
 <td class="data"><el:text name="desc" size="64" max="96" required="true" value="${prompt.description}" /></td>
</tr>
<tr>
 <td class="label top">User Prompt</td>
 <td class="data"><el:textbox name="user" className="small" width="90%" height="5" resize="true" spellcheck="true">${prompt.user}</el:textbox></td>
</tr>
<tr>
 <td class="label top">System Prompt</td>
 <td class="data"><el:textbox name="system" className="small" width="90%" height="5" resize="true" spellcheck="true">${prompt.system}</el:textbox></td>
</tr>
<%@ include file="/jsp/auditLog.jspf" %>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE LLM PROMPT" /></td>
</tr>
</el:table>
</el:form>
<br>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
