package com.woniu.sncp.cbss.core.authorize.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
// 表示在什么级别保存该注解信息
@Target(ElementType.METHOD)
// 表示该注解用于什么地方
@Documented
// 将此注解包含在 javadoc 中
@Inherited
// 允许子类继承父类中的注解
public @interface EchoRestControllerAspectType {
}
