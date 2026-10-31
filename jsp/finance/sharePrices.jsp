<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Share Prices</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:js name="common" />
<content:newRelic>
<content:cspHeader />
</content:newRelic>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="shareprices">
<tr class="title caps">
 <td colspan="5" class="left"><span class="nophone"><content:airline />&nbsp;</span>SHARE INFORMATION</td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:15%">YEAR</td>
 <td style="width:15%">SHARE PRICE</td>
 <td style="width:15%">TOTAL SHARES</td>
 <td class="left">TOTAL AMOUNT RAISED</td>
 <td><el:cmdbutton url="shareprice" op="edit" label="NEW SHARE PRICE" /></td>
</tr>

<!-- Table Share Price Data -->
<c:forEach var="sp" items="${viewContext.results}">
<view:row entry="${sp}">
 <td class="pri bld"><el:cmd url="shareprice" linkID="${sp.year}">${sp.year}</el:cmd></td>
 <td class="bld"><fmt:dec value="${sp.price / 100.0}" fmt="$##0.00" /></td>
 <td class="sec bld"><fmt:int value="${sp.count}" /></td>
 <td><fmt:dec value="${sp.price * 100.0 / sp.count}" fmt="$#,##0.00" /></td>
 <td class="small caps">FUNDRAISING IS CURRENTLY ${sp.active ? '' : 'IN'}ACTIVE</td>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="5">&nbsp;</td>
</tr>
</view:table>
<br>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
