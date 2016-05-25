package cbss.core.model.request;

import java.io.Serializable;

public interface Param extends Serializable {

	public boolean checkParamValueIn()
			throws ParamValueValidateException;
}
