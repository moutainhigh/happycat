package cbss.api.manager.init.handler;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import cbss.core.errorcode.EchoInfo;

@ControllerAdvice
class GlobalExceptionHandler {

	@ExceptionHandler(value = Exception.class)
	@ResponseBody
	public EchoInfo<String> defaultErrorHandler(HttpServletRequest req, Exception e)
			throws Exception {
		e.printStackTrace();
		EchoInfo<String> r = new EchoInfo<String>();
		r.setMessage(e.getMessage());
		r.setMsgcode(EchoInfo.ERROR);
		r.setData(e.getMessage());
		r.setUrl(req.getRequestURL().toString());
		return r;
	}

}
