<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<content:sysdata var="airlineName" name="airline.name" />
<content:sysdata var="airlineBanner" name="airline.banner" />
<!-- Header Frame -->
<div id="header">
<el:img className="header" src="${airlineBanner}" caption="${airlineName}" />
<h1 class="header caps"><content:airline /><br />
IMAGE GALLERY</h1>
</div>
