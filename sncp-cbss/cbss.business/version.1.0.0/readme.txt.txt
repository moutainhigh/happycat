api.{业务简称}	业务代表简称
api.parent		系统依赖jar环境
api.init		api服务启动环境初始化公共包
api.manager		api服务状态监控服务

git branch 说明

sncp-cbss-1.0.0
sncp-cbss-0.0.1

后面3位数字的说明
	第1位 api接口协议变更
		1	thrift
		0	Http-REST
	第2位 业务数字
		0	防沉迷
		1	手机下载中心配置
	第3位 针对第2位业务修复等跟进操作
		0.1	初始版本
		1.0	初始版本