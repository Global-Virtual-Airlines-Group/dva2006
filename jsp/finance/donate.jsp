<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<head>
<title>Donate to <content:airline /></title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:googleAnalytics />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:newRelic>
<content:cspHeader />
</content:newRelic>
<script nonce="${contentSecurity.nonce}">



</script>
<style>
div#ytdTotal {
	width: 80%;
	border-radius: 12px;
}
</style>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="donate.do" method="post" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">DONATE<span class="nophone"> TO <content:airline /></span></td>
</tr>
<tr>
 <td colspan="2"><content:airline /> depends on the continued generosity of its members to fund its operations. Your donations cover hosting fees at 
Amazon Web Services (AWS), domain registration, schedule data, Discord and other services that benefit all our members. We have operated on a shared 
fund-raising model since 2005, allowing our virtual airline to avoid dependence on any one financial donor. Each year, we open up fund raising to our
membership, providing for the purchase of <span class="ita">&quot;shares&quot;</span> within our parent organization 
<el:link target="_new" url="https://www.gvagroup.org/">Global Virtual Airlines Group</el:link> for a period of one year.<br>
<br>
We encourage smaller donations, from a broad donor base. (In fact, our founding document requires it.) Being a donor conveys few benefits beyond the 
satisfaction of contributing back to a community that we hope has given you pleasure and value during your time here, but it also makes you part of a 
dedicated and unbroken donor community that goes back over two decades, which is unheard of in the virtual airline comminity.<br>
<br>
Thank you for your support for <content:airline />!<br>
<br>
<!-- ${sp.hear} fund raising YTD totals -->
<div id="ytdTotal" class="mid">
<span class="pri bld">${sp.year} FUNDRAISING TOTALS</span><br>
<br>
Amount raised: <fmt:dec value="${sharesYTD * sp.price / 100.0}" fmt="$#,##0.00" /> of <fmt:dec value="${sp.price * sp.count / 100.0}" fmt="$#,##0.00" /><br>
<br>
<el:img y="80" x="${sharesYTD * 250.0 / sp.count}" caption="${sharesYTD} shares" src="cooler/bar_green.png" />
<el:img y="80" x="${(sp.count - sharesYTD) * 250.0 / sp.count}" caption="${sharesYTD} shares" src="cooler/bar_blue.png" />
</div>
<br>
<hr>
<br>
<!-- Insert Stripe stuff here -->

 
</tr>
<tr class="title">
 <td colspan="2">&nbsp;</td>
</tr>
</el:table>
</el:form>
<br>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
