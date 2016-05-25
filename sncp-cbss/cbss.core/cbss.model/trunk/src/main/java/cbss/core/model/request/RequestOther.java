package cbss.core.model.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequestOther {
	private List<Map<String, Object>> other = new ArrayList<Map<String, Object>>(0);

	public Map<String, Object> getOtherFirst() {
		if(!other.isEmpty()){
			return getOther().get(0);
		}
		return null;
	}

	public List<Map<String, Object>> getOther() {
		return other;
	}

	public void setOther(List<Map<String, Object>> other) {
		this.other = other;
	}
}
