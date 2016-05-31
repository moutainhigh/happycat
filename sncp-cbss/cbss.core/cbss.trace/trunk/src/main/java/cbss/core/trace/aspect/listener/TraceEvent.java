package cbss.core.trace.aspect.listener;

import org.springframework.context.ApplicationEvent;

public class TraceEvent extends ApplicationEvent{

	public TraceEvent(Object source) {
		super(source);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
