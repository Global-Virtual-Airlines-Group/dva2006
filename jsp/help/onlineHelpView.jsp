<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Help</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<el:form action="help.do" op="read" method="get" linkID="${help.title}" validate="return false">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td><content:airline /> ONLINE HELP</td>
</tr>
<tr>
 <td><el:textbox name="body" className="small" width="80%" height="8" readOnly="true">${help.body}</el:textbox></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</body>
<content:googleAnalytics />
</html>
