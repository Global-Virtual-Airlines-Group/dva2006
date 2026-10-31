<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Cost Entry</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:js name="common" />
<content:js name="datePicker" />
<content:googleAnalytics />
<content:newRelic>
<content:cspHeader />
</content:newRelic>
<script nonce="${contentSecurity.nonce}">
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:category, t:'Expense Category'});
	
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
<el:form action="airline.do" method="post" link="${entry}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">COST ENTRY</td>
</tr>
<tr>
 <td class="label">Category</td>
 <td class="data"><el:combo name="category" size="1" idx="*" required="true" value="${entry.category}" firstEntry="[ CATEGORY ]" options="${categories}" /></td>
</tr>
<tr>
 <td class="label">Date</td>
 <td class="data"><el:text name="date" required="true" idx="*" size="10" max="10" value="${fn:dateFmt(entry.date, 'MM/dd/yyyy')}" /></td>
</tr>
<tr>
 <td class="label">Amount</td>
 <td class="data"><el:text name="amount" idx="*" required="true" size="6" max="7" value="${category.amount / 100}" /> USD</td>
</tr>
<tr>
 <td class="label">Description</td>
 <td class="data"><el:text name="desc" idx="*" size="64" max="128" value="${entry.description}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE COST LEDGER ENTRY" /></td>
</tr>
</el:table>
</el:form>
<br>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
