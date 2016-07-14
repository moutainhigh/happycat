namespace java com.woniu.sncp.cbss.api.core.thrift

const string VERSION = "0.0.1"

/* 默认参数解析规则JSON
*/
const i32 PARAM_RESOLVE_TYPE_DEFAULT = 1
const string ECHO_DATA_RESOLVE_TYPE_DEFAULT = "1"
/*
* 默认签名规则
* DATA:
* 	upper(md5(notNull(Data.Param.params)?JSONObject.toJSONString(Data.Param.params):Data.Param.param))
*/
const i32 SIGNATURE_TYPE_DEFAULT = 0
const string TRACE_OPEN_CLIENT_STATE = "1"

/*
* 服务状态
*/
enum Status {
  SERVER_DEAD = 0,
  SERVER_ALIVE = 1,
  SERVER_STARTING = 2,
  SERVER_STOPPING = 3,
  SERVER_STOPPED = 4,
  SERVER_WARNING = 5,
  SERVER_FUTURE_STOPED = 6,
  SERVER_FUTURE_MAINTAIN = 7,
  DOMAINNAME_CHANGE = 8,
}

/**
* 权限数据
*/
struct Access {
	1: required i64 id,
	2: required i64 type,
	3: required string passwd,
	4: map<string,string> other
}
/**
* 1 请求数据
* 2 客户端发起接收响应,将响应中的UUID回传
*/
struct Data {
	1: required string version = VERSION,
	2: required ClientRequest clientRequest,
	3: required Param param,
	4: string traceState = TRACE_OPEN_CLIENT_STATE,
	5: string sessionId
}
/**
* 请求数据--客户端请求信息
*/
struct ClientRequest {
	1: required i64 time,
	2: required string clientUserIp,
	3: required string localReqIp,
	4: map<string,string> other
}
/**
* 请求数据--业务信息
*/
struct Param {
	1: required string classname,
	2: required i32 resolveType = PARAM_RESOLVE_TYPE_DEFAULT,
	3: required string param,
	4: list<string> params
}

/**
* 服务端回执数据
*/
struct Echo{
	1: required i64 msgcode,
	2: required string message,
	3: required string uuid,
	/**
	* Echo.resolveType为ECHO_DATA_RESOLVE_TYPE_DEFAULT,表示data是json格式
	*/
	4: required string data,
	5: required i64 time,
	6: required i32 nextSignType = SIGNATURE_TYPE_DEFAULT,
	7: required State serverState,
	8: string resolveType = ECHO_DATA_RESOLVE_TYPE_DEFAULT
}

/**
* 数据校验规则
*/
struct Signature{
	1: required i32 type = SIGNATURE_TYPE_DEFAULT,
	/**
	* 如果Data.Param.params不存在数据 且 Signature.type 为 SIGNATURE_TYPE_DEFAULT 使用 upper(md5(Data.Param.param+Access.id+Access.type+Access.passwd+Access.key))
	* 如果Data.Param.params 存在数据  且 Signature.type 为 SIGNATURE_TYPE_DEFAULT 使用 upper(md5(JSONObject.toJSONString(Data.Param.params)+Access.id+Access.type+Access.passwd+Access.key))
	*/
	2: required string signature
}

/**
* 服务端状态
*/
struct State{
	1: required Status status,
	/**
	* 当status为SERVER_FUTURE_STOPED或SERVER_FUTURE_MAINTAIN时，此值会出现一个时间点格式:yyyy-MM-dd HH:mm:ss,表示在此时间点会进行维护或停服务
	*/
	2: string futuretime,
	/**
	* 当status为DOMAINNAME_CHANGE时，此值一个新域名或逗号分隔的多个域名，使用人按照顺序逐个调用直到调用成功或每个都使用过，如域名:a.b.c,a1.b.c,a2.b.c,表示3个域名轮询调用
	*/
	3: string domanename
}

/**
* ApiService接口
*/
service Api {
	/**
	* invoke business api
	*/
	Echo invoke(1:Access access,2:Data data,3:Signature signature),

	/**
	* alert business api is over
	*/
	oneway void over(1:Access access,2:Data data,3:Signature signature),

	/**
	* check business service api status
	*/
	Echo status(1:Access access,2:Data data,3:Signature signature)
}