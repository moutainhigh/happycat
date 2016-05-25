package cbss.core.trace.model;

import org.aspectj.lang.JoinPoint;

/**
 * 请求，处理，响应日志记录
 * 
 */
public class TraceApiController extends TraceCommon {
	public TraceApiController(JoinPoint joinPoint) {
		super(joinPoint);
	}
	// 调用接口端发起时间，客户端通过参数传入
	private String requestStartTime;
	// 调用接口的入参数据,注意屏蔽敏感数据
	private Object requestInParam;
	// 调用接口使用的accessId/accessType
	private Long requestAccessId;
	private String requestAccessType;
	// 接口接收时间
	private String receiveDate;
	// 接口处理结束时间
	private String receiveExecDate;
	// 接口处理耗时时间
	private String receiveSkipTime;
	// 接口响应内容，注意屏蔽敏感数据
	private Object receiveResponseData;
	// 接口响应错误码
	private String receiveEchoCode;
	// 其他信息，是一个Map
	private String ot;

}
