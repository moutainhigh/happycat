package com.woniu.sncp.pay.common.utils;


/**
 * 断言类
 * 如果不符，抛出java.lang.IllegalArgumentException
 * @author yanghao
 * @since 2010-5-14 
 *
 */
public class Assert extends org.springframework.util.Assert {
	
	/**
	 * 调用StringUtils
	 * @param text
	 */
	public static void hasText(String text) {
		hasText(text,"参数不允许为空");
	}
	
	public static void notNull(Object object) {
		notNull(object, "参数不允许为空");
	}
	
	/**
	 * Asserts that two objects are equal. If they are not
	 * an IllegalArgumentException is thrown.
	 */
	static public void assertEquals(Object expected, Object actual) {
	    assertEquals(null, expected, actual);
	}
	
	/**
 	 * Asserts that two objects refer to the same object. If they are not
 	 * an IllegalArgumentException is thrown with the given message.
 	 */
	static public void assertNotSame(String message, Object expected, Object actual) {
		if (expected == actual)
			failSame(message,actual);
	}
	
	static private void failSame(String message,Object actual) {
		String formatted= "";
 		if (message != null)
 			formatted= message + " ";
 		fail(formatted+"该值不能为" + actual);
	}
	
	/**
	 * Asserts that two objects are equal. If they are not
	 * an IllegalArgumentException is thrown with the given message.
	 */
	static public void assertEquals(String message, Object expected, Object actual) {
		if (expected == null && actual == null)
			return;
		if (expected != null && expected.equals(actual))
			return;
		failNotEquals(message, expected, actual);
	}
	
	static private void failNotEquals(String message, Object expected, Object actual) {
		// fixed 去掉详细信息 2012.01.09
		// fail(format(message, expected, actual));
		fail(message);
	}
	
	static String format(String message, Object expected, Object actual) {
		String formatted= "";
		if (message != null)
			formatted= message + " ";
		return formatted+"[期望值:"+expected+" 实际值:"+actual+"]";
	}
	
	/**
	 * Fails a test with the given message.
	 */
	static public void fail(String message) {
		throw new IllegalArgumentException(message);
	}
	
	public static void main(String[] args) {
		assertEquals("a", 2, 1);
	}
}
