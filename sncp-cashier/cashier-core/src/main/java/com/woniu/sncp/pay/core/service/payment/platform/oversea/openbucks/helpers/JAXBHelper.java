package com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.helpers;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.util.Assert;

public class JAXBHelper {

	private final ConcurrentMap<Class<?>, JAXBContext> jaxbContexts = new ConcurrentHashMap<Class<?>, JAXBContext>(64);
	
	private JAXBHelper() {}
	
	private static class JAXBHelperInner {
		private static JAXBHelper INSTANCE = new JAXBHelper();
	}
	
	public static JAXBHelper getInstance() {
		return JAXBHelperInner.INSTANCE;
	}
	
    /**
     * java实体类转xml
     */
    /**
     * @param obj
     * @param encode 编码格式
     * @param format 是否格式化生成的xml串
     * @param fragment  是否省略xm头声明信息
     * @return
     */
    public String toXML(Object obj, String encode, boolean format, boolean fragment) {
    	
    	Marshaller marshaller = createMarshaller(obj.getClass());
        try {
        	marshaller.setProperty(Marshaller.JAXB_ENCODING, encode);
        	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, format);   
        	marshaller.setProperty(Marshaller.JAXB_FRAGMENT, fragment);
           
            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * xml转java实体类
     */
    @SuppressWarnings("unchecked")
    public <T> T fromXML(String xml, Class<T> valueType) {
        try {
        	Unmarshaller unmarshaller = createUnmarshaller(valueType);
        	return (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    protected final Marshaller createMarshaller(Class<?> clazz) {
		try {
			JAXBContext jaxbContext = getJaxbContext(clazz);
			Marshaller marshaller = jaxbContext.createMarshaller();
			return marshaller;
		} catch (JAXBException ex) {
			throw new HttpMessageConversionException(
					"Could not create Marshaller for class [" + clazz + "]: " + ex.getMessage(), ex);
		}
	}
    
    protected final Unmarshaller createUnmarshaller(Class<?> clazz) {
    	try {
    		JAXBContext jaxbContext = getJaxbContext(clazz);
    		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    		return unmarshaller;
    	}
    	catch (JAXBException ex) {
    		throw new HttpMessageConversionException(
    				"Could not create Unmarshaller for class [" + clazz + "]: " + ex.getMessage(), ex);
    	}
    }
    
    protected final JAXBContext getJaxbContext(Class<?> clazz) {
		Assert.notNull(clazz, "'clazz' must not be null");
		JAXBContext jaxbContext = this.jaxbContexts.get(clazz);
		if (jaxbContext == null) {
			try {
				jaxbContext = JAXBContext.newInstance(clazz);
				this.jaxbContexts.putIfAbsent(clazz, jaxbContext);
			}
			catch (JAXBException ex) {
				throw new HttpMessageConversionException(
						"Could not instantiate JAXBContext for class [" + clazz + "]: " + ex.getMessage(), ex);
			}
		}
		return jaxbContext;
	}
    
}
