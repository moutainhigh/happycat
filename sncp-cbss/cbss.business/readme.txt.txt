version 版本说明

a.b.c

a 代表 系统架构变化
	1 dubbo分布式服务框架
b 代表 依赖版本变化、框架功能优化升级
c 代表 业务应用依赖包版本升级

V1.0.0
	开发
		HTTP - Rest - JSON
			1 新建继承cbss.core.model.request.RequestParam类型，如FcmOnlinetimeRequestParam
			2 新建继承cbss.core.model.request.RequestDatas<第1步新建的类型>，如FcmRequestDatas<FcmOnlinetimeRequestParam>
			3 accessurl.properties 新增 
				spring.access.request.urls[序号]=uri
				spring.access.request.paramTypes[序号]=第2步新建的类型
				如
				spring.access.request.urls[0]=/cbss/api/fcm/onlinetime
				spring.access.request.paramTypes[0]=cbss.api.fcm.controller.FcmRequestDatas
			4 新建类注解如下:
				@RestController
				@RequestMapping(AccessAuthorizeFilterConfigures.BASE_CONTEXT)//BASE_CONTEXT=/cbss/api
				@Configuration
				public class FcmController {
						@SuppressWarnings("unchecked")
						@RequestMapping(value = "/fcm/onlinetime", method = RequestMethod.POST) //POST方式请求:/cbss/api/fcm/onlinetime
						@ResponseBody
						public EchoInfo<Object> fcmOnlineTime(@RequestBody FcmRequestDatas requestDatas) {
							//业务逻辑
						}

				}
			5 application.properties		spring配置文件，本地环境
				5.1 application-dev.properties	内网测试联调环境
				5.2 application-prod.properties	外网正式环境
			6 errorcode_zh_CN.properties	错误编码定义文件支持{0}占未符号
			7 修改src\main\resources\META-INF\spring\dubbo-consumer.xml 中
				7.1 dubbo:application name信息
				7.2 依赖dubbo-api接口信息
			9 线上端口说明:
				     11000
				对应:abcde

				每一位说明
				a:微服务
				b:dubbo用
				c:业务用
				d:业务用
				e:实际端口


			对接:

			1 Content-Type:application/json
			2 Request Header:
				增加 accessverify:
				增加 accessId:
				增加 accessType:
				增加 accessPasswd:


				2.1 说明:签名窜放到http-header信息里面,头信息的参数名为accessverify,值的规则是post传参的整个HttpBody+accessId+accessType+accessPasswd+accesskey,进行md5,md5的结果大小写不敏感
				
				2.2 示例
					1 参数数据是单条数据:
						{"paramdata":{"issuerId":"7","gameId":"10","aid":"133861"},"clientInfo":[{"startReqTime":请求方发起时间,long型,单位是毫秒,"clientUserIp":"用户IP","localReqIp":"发起请求的本机IP"}]}
					2 参数数据是多条格式相同数据:
						{"paramdatas":[{"issuerId":"7","gameId":"10","aid":"133861"}],"clientInfo":[{"startReqTime":请求方发起时间,long型,单位是毫秒,"clientUserIp":"用户IP","localReqIp":"发起请求的本机IP"}]}

				2.3 accessverify如下
					accessverify=md5({"paramdata":{"issuerId":"7","gameId":"10","aid":"133861"},"clientInfo":[{"startReqTime":请求方发起时间,long型,单位是毫秒,"clientUserIp":"用户IP","localReqIp":"发起请求的本机IP"}]}1612O3I4J21L2K3JA223L1J2H3S0DF18SKLJ)

			以上示例使用的accessid等参数为
			accessid:16
			accesspwd:2O3I4J21L2K3J
			accesstype:1
			accesskey(seed):A223L1J2H3S0DF18SKLJ

	TCP - Thrift
		1	实现接口com.woniu.sncp.cbss.api.core.thrift.Api.Iface，注意注解
			如:
				@NiftyHandler
				public class FcmApiService implements com.woniu.sncp.cbss.api.core.thrift.Api.Iface {...}
		2	继承类com.woniu.sncp.cbss.core.model.request.RequestParam，注意注解
			如:
				@NiftyParam
				public class FcmConfRequestParam extends RequestParam {...}
		3	第1步中的Invoke方法执行业务处理，不通业务使用 第2步的不同子类分离
			如
				@Override
				public Echo invoke(Access access, Data data, Signature signature)
						throws TException {
					String classname = data.getParam().getClassname();
					if (NciicRequestParam.class.getName().equals(classname)) {...}
				}
		4	POM.xml
			参考<artifactId>cbss.api.fcm</artifactId>项目