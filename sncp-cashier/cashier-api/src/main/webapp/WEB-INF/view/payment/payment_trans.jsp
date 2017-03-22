<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<spring:htmlEscape defaultHtmlEscape="true" />
<%
	response.setHeader("Cache-Control",
			"no-cache, no-store, must-revalidate"); //HTTP 1.1
	response.setHeader("Pragma", "no-cache"); //HTTP 1.0
	response.setHeader("Referer", "page"); //
	response.setDateHeader("Expires", 0); //prevents caching at the proxy server
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>蜗牛支付跳转</title>
</head>
<body>
	<form method="post" style="display: none" id="this_form" target="_top" name="form_bot" action="/payment/page">
		<input type="hidden" name="orderno" value="${ret.orderNo }"/>
		<input type="hidden" name="money" value="${ret.money }"/>
		<input type="hidden" name="backendurl" value="${ret.backendurl }" />
		<input type="hidden" name="merchantid" value="${ret.merchantid }" />
		<input type="hidden" name="gameid" value="${ret.gameid }" />
		<input type="hidden" name="account" value="${ret.account }" />
		<input type="hidden" name="clientip" value="${ret.clientip }" />
		<input type="hidden" name="imprestmode" value="${ret.imprestmode }" />
		<input type="hidden" name="productname" value="${ret.productname }" />
		<input type="hidden" name="fontendurl" value="${ret.fontendurl }" />
		<input type="hidden" name="sign" value="${ret.sign }" />
		<input type="hidden" name="ext" value="${ret.ext }" />
	</form>
</body>
<script type="text/javascript">
		form_bot.submit();
</script>
</html>