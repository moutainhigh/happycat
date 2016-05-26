package cbss.core.trace;

import org.springframework.stereotype.Component;

@Component
public class Trace {
	public String traceException(String exception, String traceState, String traceMsg) {
		if ("1".equals(traceState)) {
			exception = exception + "|[Dg]" + traceMsg;
		}
		return exception;
	}
}
