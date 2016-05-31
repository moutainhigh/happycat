package cbss.core.model.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestClientInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * 调用方发起请求的时间
	 */
	private long startReqTime;

	private List<Map<String, Object>> other = new ArrayList<Map<String, Object>>(0);

	public long getStartReqTime() {
		return startReqTime;
	}

	public void setStartReqTime(long startReqTime) {
		this.startReqTime = startReqTime;
	}

	public Map<String, Object> getOtherFirst() {
		List<Map<String, Object>> list = getOther();
		if(list != null && !list.isEmpty()){
			list.get(0);
		}
		return new HashMap<String,Object>();
	}

	public List<Map<String, Object>> getOther() {
		return other;
	}

	public void setOther(List<Map<String, Object>> other) {
		this.other = other;
	}
}
