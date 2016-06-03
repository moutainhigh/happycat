package com.woniu.sncp.cbss.core.trace.aspect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解类 - 日志记录 - 作用域为普通方法<br />
 * 用于AOP方法标识，在配置了AOP时，有此注解的方法则会在日志中打印输入和返回参数<br />
 * 详细参考{@link ParamsLoggerAspectj}
 * 
 * @author yanghao
 * @since 2010-3-30
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
// 表示在什么级别保存该注解信息
@Target(ElementType.METHOD)
// 表示该注解用于什么地方
@Documented
// 将此注解包含在 javadoc 中
@Inherited
// 允许子类继承父类中的注解
public @interface ParamsAndReturningLog {
	/**
	 * 是否需要记录日志 - 默认为true
	 * 
	 * @return
	 */
	public boolean isLog() default true;
}
