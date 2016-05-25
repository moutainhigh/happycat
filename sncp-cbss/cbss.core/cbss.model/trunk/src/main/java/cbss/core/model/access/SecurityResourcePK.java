package cbss.core.model.access;

import java.io.Serializable;

/**
 * 表PP_SECURITY_RESOURCE-主键
 * 
 * @author wujian
 * @since 1.0
 */
public class SecurityResourcePK implements Serializable {

	/**
	 * 序列化值
	 */
	private static final long serialVersionUID = 1L;
	
	/**
     * ID - N_ID
     */
    private Long id;

    /**
     * 方法名 - S_NAME
     */
    private String methodName;
    
    /**
     * URL - S_URL
     */
    private String url;
    
    public SecurityResourcePK(){}
    
    public SecurityResourcePK(Long id, String methodName, String url){
    	this.id = id;
    	this.methodName = methodName;
    	this.url = url;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	} 
}
