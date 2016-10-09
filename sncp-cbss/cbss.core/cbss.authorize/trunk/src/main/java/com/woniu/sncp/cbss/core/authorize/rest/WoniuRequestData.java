package com.woniu.sncp.cbss.core.authorize.rest;


@java.lang.annotation.Target(value = { java.lang.annotation.ElementType.TYPE })
@java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Documented
@java.lang.annotation.Inherited
@org.springframework.stereotype.Component
public @interface WoniuRequestData {
	
	String uri() default "";
}
