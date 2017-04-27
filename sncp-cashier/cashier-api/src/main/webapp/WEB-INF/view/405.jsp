<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
	response.setHeader("Cache-Control",
			"no-cache, no-store, must-revalidate"); //HTTP 1.1
	response.setHeader("Pragma", "no-cache"); //HTTP 1.0
	response.setDateHeader("Expires", 0); //prevents caching at the proxy server
%>

<!DOCTYPE HTML>
<html class="not_found_con">
    <head>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
        <link rel="icon" type="image/x-icon" href="/images/favicon.ico" />    
		<link rel="shortcut icon" type="image/x-icon" href="/images/favicon.ico" />    
		<link rel="bookmark" type="image/x-icon" href="/images/favicon.ico" />
        <link href="//www3.woniu.com/includes/css/reset.css" type="text/css" rel="stylesheet">
        <link href="//www3.woniu.com/includes/css/common.css" type="text/css" rel="stylesheet">
        <link href="//www3.woniu.com/includes/css/index.css" type="text/css" rel="stylesheet">
        <link href="//www3.woniu.com/includes/css/custom.css" type="text/css" rel="stylesheet">
        <link href="//www3.woniu.com/pay/css/pay.css" type="text/css" rel="styleSheet"/>
        <title>蜗牛收银台</title>
        <meta name="Copyright" content="Copyright (c) 2011 苏州蜗牛数字科技股份有限公司" />
		<meta name="Keywords" content="提供最完善，最优质的游戏服务，了解蜗牛最及时的游戏资讯，拥有最真实的玩家互动社区" />
    </head>
    <body class="not_found_con">
    	  <div class="pay_header">
            <div class="logo_con">
                <img src="//www3.woniu.com/pay/images/top.png" />
                <img style="float: right;padding-top: 10px;" src="//www3.woniu.com/pay/images/customer_phone.png" />
            </div>
        </div>
        <div class="not_found">
            <img src="//www3.woniu.com/pay/images/404_img.jpg" />
            <p class="p1">405,无法完成支付。</p>
            <p class="p2" style="font-size:18px;">点击“返回首页”重新发起支付，或拨打客服热线。</p>
            <c:if test="${referer == null}">
            	<a href="javascript:void(0);" onclick="self.history.go(-2);return false;" class="go_back">返回首页</a>
            </c:if>
            <c:if test="${referer != null}">
            <a href="${referer}" class="go_back">返回首页</a>
            </c:if>
        </div>
    </body>
</html>

