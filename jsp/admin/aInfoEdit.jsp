<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title>Virtual Airline - ${aInfo.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.domain, 6, 'Domain Name')) return false;
if (!validateText(form.db, 2, 'Database Name')) return false;

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
<el:form action="ainfo.do" method="post" linkID="${aInfo.code}" op="save" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">VIRTUAL AIRLINE PROFILE - ${aInfo.name}</td>
</tr>
<tr>
 <td class="label">Name</td>
 <td class="data pri bld">${aInfo.name}</td>
</tr>
<tr>
 <td class="label">Airline Code</td>
 <td class="data sec bld">${aInfo.code}</td>
</tr>
<tr>
 <td class="label">Domain Name</td>
 <td class="data"><el:text name="domain" idx="*" required="true" size="12" max="32" value="${aInfo.domain}" /></td>
</tr>
<tr>
 <td class="label">Database</td>
 <td class="data"><el:text name="db" idx="*" className="bld" required="true" size="5" max="12" value="${aInfo.DB}" /></td> 
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="canTX" className="small" label="Airline allows inbound Pilot transfers" value="true" checked="${aInfo.canTransfer}" /><br />
<el:box name="historicRestrict" className="small" label="Historic Routes require Historic Aircraft" value="true" checked="${aInfo.historicRestricted}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE VIRTUAL AIRLNE PROFILE" /></td>
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
