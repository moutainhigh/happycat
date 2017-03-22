<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>蜗牛支付测试同步跳转</title>
</head>
<body>
	<table>${errMsg}
		<c:if test="${errMsg != null and errMsg != ''}">
			<tr>
				<td>错误信息</td>
				<td>${errMsg}</td>
			</tr>
		</c:if>
		<tr>
			<td>订单号</td>
			<td>${orderNo}</td>
		</tr>
	</table>
</body>
</html>