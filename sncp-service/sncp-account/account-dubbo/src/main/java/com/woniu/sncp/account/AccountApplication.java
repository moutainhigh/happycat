/**
 * <p>Copyright (c) Snail Game 2016</p>
 */
package com.woniu.sncp.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * @Title: AccountApplication
 * @Description:
 * @Author zhujing
 * @Date 2016/7/7
 * @Version V1.0
 */
@SpringBootApplication
@ImportResource({"classpath*:META-INF/spring/account-dubbo-provider.xml"})
public class AccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }

}
